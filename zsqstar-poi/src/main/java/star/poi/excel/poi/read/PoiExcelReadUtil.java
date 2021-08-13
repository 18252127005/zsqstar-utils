package star.poi.excel.poi.read;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import star.poi.excel.poi.APoiExcelFuncUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static star.poi.common.constants.PoiConstant.OfficeSuffixes.OFFICE_EXCEL_XLS;
import static star.poi.common.constants.PoiConstant.OfficeSuffixes.OFFICE_EXCEL_XLSX;

/**
 * @author: Mark.ZSQ
 * @Date: 2021/8/11 4:46 下午
 * @Description: poi操作excel进行阅读功能
 */
public abstract class PoiExcelReadUtil extends APoiExcelFuncUtil {


    /**
     * 1.1 读取指定Sheet也的内容
     * sheetNo设置null, 默认是读取全文
     *
     * @param filepath filepath 文件全路径
     * @param sheetNo  sheet序号,从0开始,如果读取全文sheetNo设置null
     * @return 字符串值
     */
    public static String readExcel(String filepath, Integer sheetNo) throws EncryptedDocumentException,
            InvalidFormatException, IOException {
        Workbook workbook = getWorkbook(filepath);
        return readExcelString(workbook, sheetNo);
    }


    /**
     * 1.2 读取指定Sheet也的内容
     * sheetNo设置null, 默认是读取全文
     *
     * @param is      is 文件读取流
     * @param sheetNo sheet序号,从0开始,如果读取全文sheetNo设置null
     * @return 字符串值
     */
    public static String readExcel(InputStream is, Integer sheetNo) throws EncryptedDocumentException,
            InvalidFormatException, IOException {
        Workbook workbook = getWorkbook(is);
        return readExcelString(workbook, sheetNo);
    }


    /**
     * 1.3 读取指定Sheet也的内容
     * sheetNo设置null, 默认是读取全文
     *
     * @param is      is 文件读取流
     * @param sheetNo sheet序号,从0开始,如果读取全文sheetNo设置null
     * @return List<List < Map < Integer, Object>>>
     * 第一个集合是sheet集合, 子集合里面sheet里面行数据, 里面的map是某行的列数据
     */
    public static List<List<Map<Integer, Object>>> readExcelToListMapStructure(InputStream is, Integer sheetNo)
            throws EncryptedDocumentException, InvalidFormatException, IOException {
        // 初始化结构
        List<List<Map<Integer, Object>>> sheetList = Lists.newArrayList();

        // 获取excel对象
        Workbook workbook = getWorkbook(is);

        // 获取sheet的总计
        int numberOfSheets = 0;
        if (workbook != null) numberOfSheets = workbook.getNumberOfSheets();

        // 遍历sheet, 数据处理
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);

            // 行记录数
            int lastRowNum = 0;
            if (sheet != null) lastRowNum = sheet.getLastRowNum();

            List<Map<Integer, Object>> rowCellDataList = Lists.newArrayList();
            // 遍历row, 数据处理
            for (int rowIndex = 0; rowIndex < lastRowNum; rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                // 遍历cell, 数据处理
                Map<Integer, Object> cellDataMap = Maps.newHashMap();
                int lastCellNum = row.getLastCellNum();
                for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
                    Cell cell = row.getCell(cellIndex);
                    cell.setCellType(CellType.STRING);
                    String cellValue = cell.getStringCellValue().toString();

                    cellDataMap.put(cellIndex, cellValue);
                }
                rowCellDataList.add(cellDataMap);
            }
            sheetList.add(rowCellDataList);
        }

        return sheetList;
    }


//==================================================Sheet===============================================================


    /**
     * 2.1 根据文件路径获取Workbook对象
     *
     * @param filepath 文件全路径
     */
    public static Workbook getWorkbook(String filepath) throws EncryptedDocumentException,
            InvalidFormatException, IOException {
        InputStream is = null;
        Workbook wb = null;
        if (StringUtils.isBlank(filepath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        } else {
            String suffixes = getSuffixes(filepath);
            if (StringUtils.isBlank(suffixes)) {
                throw new IllegalArgumentException("文件后缀不能为空");
            }
            if (OFFICE_EXCEL_XLS.equals(suffixes) || OFFICE_EXCEL_XLSX.equals(suffixes)) {
                try {
                    is = new FileInputStream(filepath);
                    wb = WorkbookFactory.create(is);
                } finally {
                    if (is != null) {
                        is.close();
                    }
                    if (wb != null) {
                        wb.close();
                    }
                }
            } else {
                throw new IllegalArgumentException("该文件非Excel文件");
            }
        }
        return wb;
    }

    /**
     * 2.2 根据文件流获取Workbook对象
     *
     * @param is 文件流
     */
    public static Workbook getWorkbook(InputStream is) throws EncryptedDocumentException,
            InvalidFormatException, IOException {
        Workbook wb = null;
        try {
            wb = WorkbookFactory.create(is);
        } finally {
            if (is != null) {
                is.close();
            }
            if (wb != null) {
                wb.close();
            }
        }
        return wb;
    }

    /**
     * 获取workbook
     */
    private static Workbook getWorkbook(InputStream inputStream, String fileName) throws Exception {
        Workbook workbook = null;
        if (isExcel2003(fileName)) {
            //2003 版本的excel
            workbook = new HSSFWorkbook(inputStream);
        } else if (isExcel2007(fileName)) {
            //2007 版本的excel
            workbook = new XSSFWorkbook(inputStream);
        } else {
            throw new Exception("Excel文件格式有误！");
        }
        return workbook;
    }

//==================================================Workbook============================================================


    /**
     * 3.1 读取指定Sheet页的表头
     *
     * @param filepath filepath 文件全路径
     * @param sheetNo  sheet序号,从0开始,必填
     */
    public static Row readTitle(String filepath, int sheetNo)
            throws IOException, EncryptedDocumentException, InvalidFormatException {
        Row returnRow = null;
        Workbook workbook = getWorkbook(filepath);
        if (workbook != null) {
            Sheet sheet = workbook.getSheetAt(sheetNo);
            returnRow = readTitle(sheet);
        }
        return returnRow;
    }

    /**
     * 3.2 读取指定Sheet页的表头
     *
     * @param is      is 文件流
     * @param sheetNo sheet序号,从0开始,必填
     */
    public static Row readTitle(InputStream is, int sheetNo)
            throws IOException, EncryptedDocumentException, InvalidFormatException {
        Row returnRow = null;
        Workbook workbook = getWorkbook(is);
        if (workbook != null) {
            Sheet sheet = workbook.getSheetAt(sheetNo);
            returnRow = readTitle(sheet);
        }
        return returnRow;
    }


    /**
     * 3.3 读取Sheet页的表头
     *
     * @param sheet
     * @return
     */
    public static Row readTitle(Sheet sheet) throws IOException {
        Row returnRow = null;
        int totalRow = sheet.getLastRowNum();// 得到excel的总记录条数
        for (int i = 0; i < totalRow; i++) {// 遍历行
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            returnRow = sheet.getRow(0);
            break;
        }
        return returnRow;
    }

    /**
     * 3.4 读取Excel表格表头的内容
     *
     * @param inputStream
     * @return String 表头内容的数组
     */
    public static List<String> readTitle(InputStream inputStream, String fileName) throws Exception {
        List<String> titles = Lists.newArrayList();
        Workbook workbook = getWorkbook(inputStream, fileName);
        if (workbook == null) {
            return titles;
        }
        Sheet sheet = workbook.getSheetAt(0);
        //excel为空
        if (sheet.getLastRowNum() == 0 && sheet.getPhysicalNumberOfRows() == 0) {
            return titles;
        }
        //得到首行的row
        Row row = sheet.getRow(0);
        //标题总列数
        int colNum = row.getPhysicalNumberOfCells();
        for (int i = 0; i < colNum; i++) {
            titles.add(row.getCell(i).toString());
        }
        return titles;
    }


//==================================================Sheet-Title-Row=====================================================


    // 读取excel全部输入内容转为string
    private static String readExcelString(Workbook workbook, Integer sheetNo) {
        StringBuilder sb = new StringBuilder();
        if (workbook != null) {
            if (sheetNo == null) {
                int numberOfSheets = workbook.getNumberOfSheets();
                for (int i = 0; i < numberOfSheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet == null) {
                        continue;
                    }
                    sb.append(readExcelSheet(sheet));
                }
            } else {
                Sheet sheet = workbook.getSheetAt(sheetNo);
                if (sheet != null) {
                    sb.append(readExcelSheet(sheet));
                }
            }
        }
        return sb.toString();
    }

    // 根据名称获取文件后缀
    private static String getSuffixes(String filepath) {
        if (StringUtils.isBlank(filepath)) return "";

        int index = filepath.lastIndexOf(".");

        if (index == -1) return "";
        return filepath.substring(index + 1);
    }

    // 获取excel的的sheet的字符串
    private static String readExcelSheet(Sheet sheet) {
        StringBuilder sb = new StringBuilder();

        if (sheet != null) {
            int rowNos = sheet.getLastRowNum();// 得到excel的总记录条数
            for (int i = 0; i <= rowNos; i++) {// 遍历行
                Row row = sheet.getRow(i);
                if (row != null) {
                    int columNos = row.getLastCellNum();// 表头总共的列数
                    for (int j = 0; j < columNos; j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            cell.setCellType(CellType.STRING);
                            sb.append(cell.getStringCellValue() + " ");
                        }
                    }
                }
            }
        }

        return sb.toString();
    }

    // 是否是2003的excel，返回true是2003
    private static boolean isExcel2003(String filePath) {
        return filePath.matches("^.+\\.(?i)(xls)$");
    }

    // 是否是2007的excel，返回true是2007
    private static boolean isExcel2007(String filePath) {
        return filePath.matches("^.+\\.(?i)(xlsx)$");
    }

//==================================================Common封装=====================================================


    public static void main(String[] args) throws IOException, InvalidFormatException {
        ClassPathResource classPathResource = new ClassPathResource("temp/班级人员信息.xlsx");
        InputStream resourceAsStream = PoiExcelReadUtil.class.getClassLoader().getResourceAsStream("temp/班级人员信息.xls");

//        String s = readExcel(resourceAsStream, 0);
        List<List<Map<Integer, Object>>> listList = readExcelToListMapStructure(resourceAsStream, 0);
        System.err.println(listList);
    }
}
