package star.poi.excel.easypoi;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author: Mark.ZSQ
 * @Date: 2021/8/10 3:51 下午
 * @Description: easyPoi导出excel工具类
 */
public class EasyPoiExcelUtil {
    private final static Logger log = LoggerFactory.getLogger(EasyPoiExcelUtil.class);

    /**
     * 1. 导出excel
     *
     * @param list           数据集合 存储的对象形式
     * @param title          标题名称
     * @param sheetName      sheet名称
     * @param pojoClass      实体对象
     * @param fileName       文件名称
     * @param isCreateHeader 是否创建表头
     * @param type           文件类型 HSSF, XSSF
     * @param response
     */
    public static void exportExcel(List<?> list, String title, String sheetName, Class<?> pojoClass, String fileName,
                                   boolean isCreateHeader, ExcelType type, HttpServletResponse response) {
        ExportParams exportParams = new ExportParams(title, sheetName, type);
        exportParams.setCreateHeadRows(isCreateHeader);
        defaultExport(list, pojoClass, fileName, response, exportParams);

    }

    /**
     * 2. 导出excel
     *
     * @param list     list的泛型是map格式
     * @param fileName 文件名称
     * @param type     文件类型 HSSF, XSSF
     * @param response
     */
    public static void exportExcel(List<Map<String, Object>> list, String fileName, ExcelType type, HttpServletResponse response) {
        defaultExport(list, fileName, type, response);
    }


    /**
     * 3. 多sheet页导出到excel
     *
     * @param list     数据集合列表
     * @param fileName 文件名称
     * @param type     文件类型 HSSF, XSSF
     * @param response
     */
    public static void exportMultiSheetExcel(List<ExportView> list, String fileName, ExcelType type, HttpServletResponse response) {
        List<Map<String, Object>> excel = new ArrayList<>();
        for (ExportView view : list) {
            Map<String, Object> sheet = new HashMap<>();
            sheet.put("title", view.getExportParams());
            sheet.put("entity", view.getCls());
            sheet.put("data", view.getDataList());
            excel.add(sheet);
        }
        exportExcel(excel, fileName, type, response);
    }


//============================================导入start=======================================================================

    /**
     * 1. 数据导入到excel
     *
     * @param filePath
     * @param titleRows
     * @param headerRows
     * @param pojoClass
     * @param <T>
     * @return
     */
    public static <T> List<T> importExcel(String filePath, Integer titleRows, Integer headerRows, Class<T> pojoClass) {
        if (StringUtils.isBlank(filePath)) {
            return null;
        }

        ImportParams params = new ImportParams();
        params.setTitleRows(titleRows);
        params.setHeadRows(headerRows);
        List<T> list = null;
        try {
            list = ExcelImportUtil.importExcel(new File(filePath), pojoClass, params);
        } catch (NoSuchElementException e) {
            throw new RuntimeException("模板不能为空");
        } catch (Exception e) {
            log.error("系统错误:", e);
            throw new RuntimeException(e.getMessage());
        }
        return list;
    }

    /**
     * 2. 数据导入到excel
     *
     * @param file
     * @param titleRows
     * @param headerRows
     * @param pojoClass
     * @param <T>
     * @return
     */
    public static <T> List<T> importExcel(MultipartFile file, Integer titleRows, Integer headerRows, Class<T> pojoClass) {
        if (file == null) {
            return null;
        }
        ImportParams params = new ImportParams();
        params.setTitleRows(titleRows);
        params.setHeadRows(headerRows);
        List<T> list = null;
        try {
            list = ExcelImportUtil.importExcel(file.getInputStream(), pojoClass, params);
        } catch (NoSuchElementException e) {
            throw new RuntimeException("excel文件不能为空");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return list;
    }

//============================================导入end=======================================================================


    /**
     * 下载excel的方法
     *
     * @param fileName
     * @param response
     * @param workbook
     */
    private static void downLoadExcel(String fileName, HttpServletResponse response, Workbook workbook) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static void defaultExport(List<?> list, Class<?> pojoClass, String fileName, HttpServletResponse response,
                                      ExportParams exportParams) {
        Workbook workbook = ExcelExportUtil.exportExcel(exportParams, pojoClass, list);
        if (workbook != null) ;
        downLoadExcel(fileName, response, workbook);
    }

    private static void defaultExport(List<Map<String, Object>> list, String fileName, ExcelType type, HttpServletResponse response) {
        Workbook workbook = ExcelExportUtil.exportExcel(list, type);
        if (workbook != null) ;
        downLoadExcel(fileName, response, workbook);
    }


    public static class ExportView {

        public ExportView() {

        }

        public ExportView(String sheetName, List<?> dataList, ExcelType type, Class<?> cls) {
            ExportParams exportParams = new ExportParams(null, sheetName, type);
            exportParams.setCreateHeadRows(false);
            this.exportParams = exportParams;
            this.dataList = dataList;
            this.cls = cls;
        }

        private ExportParams exportParams;
        private List<?> dataList;
        private Class<?> cls;

        public ExportParams getExportParams() {
            return exportParams;
        }

        public void setExportParams(ExportParams exportParams) {
            this.exportParams = exportParams;
        }

        public Class<?> getCls() {
            return cls;
        }

        public void setCls(Class<?> cls) {
            this.cls = cls;
        }

        public List<?> getDataList() {
            return dataList;
        }

        public void setDataList(List<?> dataList) {
            this.dataList = dataList;
        }

    }


}