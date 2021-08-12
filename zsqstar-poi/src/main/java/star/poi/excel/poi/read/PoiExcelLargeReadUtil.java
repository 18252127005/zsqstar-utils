package star.poi.excel.poi.read;

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

}
