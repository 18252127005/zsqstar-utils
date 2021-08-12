package star.poi.excel.poi.read.common;

import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Mark.ZSQ
 * @Date: 2021/8/12 4:38 下午
 * @Description: 自定义sheet基于Sax的解析处理器
 */
public class SheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {


    /**
     * 封装实体对象
     */
    private Map<String, String> map;

    /**
     * 实体对象集合
     */
    private List<Map<String, String>> mapsList = new ArrayList<>(MAX_EMPLOYEE);

    /**
     * 集合最大容量
     */
    private static final int MAX_EMPLOYEE = 1024;

    private Map<String, String> keyMap;
    private Map<String, String> fieldMapping;

    /**
     * 第几次插入数据，初始值为1
     */
    private int times = 1;

    /**
     * 总数据量
     */
    private int allCount = 0;


    /**
     * 当开始解析某一行的时候触发
     *
     * @param i
     */
    @Override
    public void startRow(int i) {
        if (i > 0) {
            map = new HashMap<>(16);
        } else {
            keyMap = new HashMap<>(16);
        }
    }

    /**
     * 当结束解析某一行的时候触发
     *
     * @param i
     */
    @Override
    public void endRow(int i) {
        if (map != null) {
            mapsList.add(map);
            allCount++;
        }

        //当读取的数据达到最大容量时 进行添加（类似于分批插入）
        if (mapsList.size() == MAX_EMPLOYEE) {
            // 假设有一个批量插入
            System.out.println("执行第" + times + "次插入");
            times++;
            mapsList.clear();
        }

    }

    /**
     * 对行中的每一个单元格进行处理
     *
     * @param cellName    单元格名称
     * @param value       数据
     * @param xssfComment 批注
     */
    @Override
    public void cell(String cellName, String value, XSSFComment xssfComment) {
        String prefix = cellName.substring(0, cellName.length() - 1);
        if (map != null) {
            map.put(keyMap.get(prefix), value);
        } else {
            keyMap.put(prefix, fieldMapping.get(value));
        }
    }

    public List<Map<String, String>> getEmployeeList() {
        return mapsList;
    }

    public int getAllCount() {
        return allCount;
    }

    public SheetHandler(@NotNull Map<String, String> fieldMapping) throws Exception {
        if (fieldMapping == null || fieldMapping.size() == 0) throw new Exception("字段中英文对应关系不能为空");

        this.fieldMapping = fieldMapping;
    }


}
