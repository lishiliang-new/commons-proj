package com.lishiliang.core.plugin.excel.write;

import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author lisl
 * @version 1.0
 * @date 2021-11-28 17:31
 * @desc :
 */
public class SheetInfo {

    @NonNull
    private ExcelResultSetHandler rsHandler;

    private String sheetName = "sheet";

    //单sheet总行数 超过该数据量会自动分sheet 最多1000000 默认 1000000
    private Integer totalRows = 1000000;

    //一个工作簿最多10张sheet表 默认1张 最多 totalSheet*totalRows条数据
    private int totalSheet = 1;

    //当前是第几张sheet表
    private int sheetNum = 0;

    private List<List<String>> sheetHeader = new ArrayList<>();

    private String rowStyle;

    private Predicate filter;

    public String getSheetName() {
        return sheetName;
    }

    public SheetInfo(@NonNull ExcelResultSetHandler rsHandler) {
        this.rsHandler = rsHandler;
    }

    public ExcelResultSetHandler getRsHandler() {
        return rsHandler;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public List<List<String>> getSheetHeader() {
        return sheetHeader;
    }

    public void setSheetHeader(List<List<String>> sheetHeader) {
        this.sheetHeader = sheetHeader;
    }

    public String getRowStyle() {
        return rowStyle;
    }

    public void setRowStyle(String rowStyle) {
        this.rowStyle = rowStyle;
    }

    public Predicate getFilter() {
        return filter;
    }

    public void setFilter(Predicate filter) {
        this.filter = filter;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        totalRows = totalRows > 1000000 ? 1000000 : totalRows;
        totalRows = totalRows <= 0 ? 1000000 : totalRows;
        this.totalRows = totalRows;
    }

    public int getSheetNum() {
        return sheetNum;
    }

    public void setSheetNum(int sheetNum) {
        this.sheetNum = sheetNum;
    }

    public int getTotalSheet() {
        return totalSheet;
    }

    public void setTotalSheet(int totalSheet) {
        //一个工作簿最多10张sheet表 最多 10*totalRows条数据
        totalSheet = totalSheet > 10 ? 10 : totalSheet;
        totalSheet = totalSheet < 1 ? 1 : totalSheet;
        this.totalSheet = totalSheet;
    }
}
