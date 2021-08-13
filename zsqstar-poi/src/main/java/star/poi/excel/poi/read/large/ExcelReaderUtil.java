package star.poi.excel.poi.read.large;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

/**
 * @author: Mark.ZSQ
 * @Date: 2021/8/13 10:17 上午
 * @Description:
 */
public class ExcelReaderUtil {
    //excel2003扩展名
    public static final String EXCEL03_EXTENSION = ".xls";
    //excel2007扩展名
    public static final String EXCEL07_EXTENSION = ".xlsx";

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
        long l = System.nanoTime();

        int totalRows = 0;
        if (fileName.endsWith(EXCEL03_EXTENSION)) { //处理excel2003文件
            ExcelXlsReader excelXls = new ExcelXlsReader();
            totalRows = excelXls.process(fileName);
        } else if (fileName.endsWith(EXCEL07_EXTENSION)) {//处理excel2007文件
            ExcelXlsxReaderWithDefaultHandler excelXlsxReader = new ExcelXlsxReaderWithDefaultHandler();
            totalRows = excelXlsxReader.process(fileName);
        } else {
            throw new Exception("文件格式错误，fileName的扩展名只能是xls或xlsx。");
        }


        long end = System.nanoTime();
        System.out.println((end - l) / 1000000000L + "秒");
        System.out.println("发送的总行数：" + totalRows);
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

    public static void main(String[] args) throws Exception {
        //String path="D:\\Github\\test.xls";
        //String path="D:\\H3CIDEA\\POIExcel\\test.xlsx";
        String path = "/Users/zhangshiqiang/company/Mark.ZSQ/testfile/产品对应关系导入模板01.xlsx";

        /*ExcelReaderUtil.readExcel(file2.getAbsolutePath(),"/home/test/tmp.xlsx");*/
        ExcelReaderUtil.readExcel(path);

        /*readXlsx(file2.getAbsolutePath());*/
    }
}
