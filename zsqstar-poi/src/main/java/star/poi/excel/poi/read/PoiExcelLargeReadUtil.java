package star.poi.excel.poi.read;

import star.poi.common.constants.PoiConstant;
import star.poi.excel.poi.read.largecomponent.ExcelXlsReader;
import star.poi.excel.poi.read.largecomponent.ExcelXlsxReaderWithDefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

/**
 * @author: Mark.ZSQ
 * @Date: 2021/8/12 3:54 下午
 * @Description: poi操作excel进行阅读功能-大量数据处理
 * <p>
 * 数据量比较大(8万条以上)的excel文件解析，将excel文件解析为 行列坐标-值的形式存入map中，此方式速度快，内存耗损小 但只能读取excle文件
 * 提供处理单个sheet方法 processOneSheet(String  filename) 以及处理多个sheet方法 processAllSheets(String  filename)
 * 只需传入文件路径+文件名即可  调用处理方法结束后，只需 接收LargeExcelFileReadUtil.getRowContents()返回值即可获得解析后的数据
 */
public class PoiExcelLargeReadUtil extends PoiExcelReadUtil {

    /**
     * 每获取一条记录，即打印
     * 在flume里每获取一条记录即发送，而不必缓存起来，可以大大减少内存的消耗，这里主要是针对flume读取大数据量excel来说的
     *
     * @param sheetName
     * @param sheetIndex
     * @param curRow
     * @param cellList 数据列
     */
    public static void sendRows(String filePath, String sheetName, int sheetIndex, int curRow, List<String> cellList) {
        StringBuffer oneLineSb = new StringBuffer();
        oneLineSb.append(filePath);
        oneLineSb.append("--");
        oneLineSb.append("sheet" + sheetIndex);
        oneLineSb.append("::" + sheetName);//加上sheet名
        oneLineSb.append("--");
        oneLineSb.append("row" + curRow);
        oneLineSb.append("::");
        for (String cell : cellList) {
            oneLineSb.append(cell.trim());
            oneLineSb.append("|");
        }
        String oneLine = oneLineSb.toString();
        if (oneLine.endsWith("|")) {
            oneLine = oneLine.substring(0, oneLine.lastIndexOf("|"));
        }// 去除最后一个分隔符

        System.out.println(oneLine);
    }


    /**
     * 阅读excel, 自动区分03版还是07版
     *
     * @param fileName
     * @throws Exception
     */
    public static void readExcel(String fileName) throws Exception {
        int totalRows = 0;
        if (fileName.endsWith(PoiConstant.OfficeSuffixes.OFFICE_EXCEL_XLS)) { //处理excel2003文件
            ExcelXlsReader excelXls = new ExcelXlsReader();
            totalRows = excelXls.process(fileName);
        } else if (fileName.endsWith(PoiConstant.OfficeSuffixes.OFFICE_EXCEL_XLSX)) {//处理excel2007文件
            ExcelXlsxReaderWithDefaultHandler excelXlsxReader = new ExcelXlsxReaderWithDefaultHandler();
            totalRows = excelXlsxReader.process(fileName);
        } else {
            throw new Exception("文件格式错误，fileName的扩展名只能是xls或xlsx");
        }
    }


    /**
     * 拷贝到指定目录
     *
     * @param file
     * @param tmpDir
     * @throws Exception
     */
    public static void copyToTemp(File file, String tmpDir) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        File file1 = new File(tmpDir);
        if (file1.exists()) {
            file1.delete();
        }
        FileOutputStream fos = new FileOutputStream(tmpDir);
        byte[] b = new byte[1024];
        int n = 0;
        while ((n = fis.read(b)) != -1) {
            fos.write(b, 0, n);
        }
        fis.close();
        fos.close();
    }

}
