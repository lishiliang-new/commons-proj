package com.lishiliang.core.plugin.excel.write;

import com.lishiliang.core.exception.BusinessRuntimeException;
import com.lishiliang.core.utils.ErrorCodes;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @author lisl
 * @version 1.0
 * @date 2021-11-26 16:51
 * @desc : 高效excel导出 并且不会造成oom
 * fixme 使用该工具类datasource必须支持流式查询 即url需要添加useCursorFetch=true参数 如：jdbc:mysql://127.0.0.1?autoReconnect=true&useUnicode=true&characterEncoding=UTF8&rewriteBatchedStatements=true&useCursorFetch=true"
 */
public class ExcelFastWriter {

    private static final Logger logger = LoggerFactory.getLogger(ExcelFastWriter.class);

    public static ExcelFastWriter getInstance() {
        return new ExcelFastWriter();
    }

    /**
     * @desc
     * @param outFile
     * @return
     */
    public void writeExcel(String outFile, SheetInfo sheetInfo) {
        FileOutputStreamWapper out = null;
        try {
            writeExcelWithOutputStream(out = new FileOutputStreamWapper(outFile), sheetInfo);
        } catch (Exception e) {
            logger.error("report excel error msg：{}", e.getMessage());
            throw new BusinessRuntimeException(ErrorCodes.ERROR_FILE_REPORT.getCode(), ErrorCodes.ERROR_FILE_REPORT.getDesc(), e.getMessage(), e);
        } finally {
            out.relClose();
        }

    }

    public void writeExcel(OutputStream out, SheetInfo sheetInfo) {

        try  {
            writeExcelWithOutputStream(out, sheetInfo);
        } catch (Exception e) {
            logger.error("report excel error msg：{}", e.getMessage());
            throw new BusinessRuntimeException(ErrorCodes.ERROR_FILE_REPORT.getCode(), ErrorCodes.ERROR_FILE_REPORT.getDesc(), e.getMessage(), e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                logger.error("close error msg：{}", e.getMessage());
            }
        }

    }

    /**
     * @desc
     * @param out
     * @return
     */
    public void writeExcelWithOutputStream(OutputStream out, SheetInfo sheetInfo) throws Exception {

        final SXSSFWorkbook workbook = new SXSSFWorkbook();
        final MultiplyWriter multiplyWriter = new MultiplyWriter(workbook);

        //开启查询
        sheetInfo.getRsHandler().queryStart();

        //初始化工作簿的sheet表
        List<SXSSFSheet> sheets = new ArrayList<>();
        for (int i = 0; i < sheetInfo.getTotalSheet(); i++) {
            sheets.add(crateSheet(workbook, sheetInfo));
        }

        //创建一个导出任务
        Runnable task = createTask(workbook, multiplyWriter, sheets ,sheetInfo);
        //执行任务 （向临时文件持久化数据）
        new Thread(task).start();
        logger.info("开始刷盘");
        //循环从临时文件的输入流写入到输出流中 直到multiplyFinished终止（即不再向临时文件持久化数据的时候终止）
        multiplyWriter.multiplyWrite(out);
        logger.info("导出结束");

    }

    private Runnable createTask(SXSSFWorkbook workbook, MultiplyWriter multiplyWriter, List<SXSSFSheet> sheets, SheetInfo sheetInfo) {

        return  ()->{
            createRow(workbook, multiplyWriter, sheets, sheetInfo);
        };
    }

    private int createHeader(SXSSFWorkbook workbook, SXSSFSheet sheet, SheetInfo sheetInfo) {
        List<List<String>> headers = sheetInfo.getSheetHeader();
//        CellStyle cellStyle = createHeaderCellStyle(workbook);

        int rowNum = 0;
        for (List<String> header : headers) {
            SXSSFRow row = sheet.createRow(rowNum++);
            int cellNum = 0;
            for (String headerCell : header) {
                SXSSFCell cell = row.createCell(cellNum++);
//                cell.setCellStyle(cellStyle);
                cell.setCellValue(headerCell);
            }
        }
        return rowNum;
    }

    private CellStyle createHeaderCellStyle(SXSSFWorkbook workbook) {
        CellStyle newCellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short)14);
        font.setBold(true);
        newCellStyle.setFont(font);
        newCellStyle.setWrapText(true);
        newCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        newCellStyle.setAlignment(HorizontalAlignment.CENTER);
        newCellStyle.setLocked(true);
        newCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        newCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        newCellStyle.setBorderBottom(BorderStyle.THIN);
        newCellStyle.setBorderLeft(BorderStyle.THIN);
        return newCellStyle;
    }


    private void createRow(SXSSFWorkbook workbook, MultiplyWriter multiplyWriter, List<SXSSFSheet> sheets, SheetInfo sheetInfo) {
        try {
            ExcelResultSetHandler rsHandler = sheetInfo.getRsHandler();
            BlockingQueue<List<String[]>> queue = rsHandler.getQueue();

            SXSSFSheet sheet = sheets.remove(0);
            int headerLength = createHeader(workbook, sheet, sheetInfo);
            int rowNum = headerLength;

            while (rsHandler.isStart() || !queue.isEmpty()) {
                if (!queue.isEmpty()) {
                    List<String[]> data  = queue.poll();
                    for (String[] rowData : data) {
                        SXSSFRow row = sheet.createRow(rowNum++);
                        int length = rsHandler.getRowCellLength();
                        for (int i = 0; i < length; i++) {
                            row.createCell(i).setCellValue(rowData[i]);
                        }
                    }
                    //刷新数据到临时文件
                    sheet.flushRows(data.size());
                    if (rowNum >= sheetInfo.getTotalRows() + headerLength) {
                        if (sheets.size() == 0) {
                            rsHandler.setStop(true);
                            break;
                        }
                        //刷新Writer中的余留数据
                        multiplyWriter.flushWriter(sheet);

                        //使用一个新的sheet
                        sheet = sheets.remove(0);
                        headerLength = createHeader(workbook, sheet, sheetInfo);
                        rowNum = headerLength;
                    }
                }

            }

            multiplyWriter.flushWriter(sheet);
            //flush多余的sheet表
            for (SXSSFSheet sxssfSheet : sheets) {
                multiplyWriter.flushWriter(sxssfSheet);
            }

        } catch (Exception e) {
            finished(workbook, multiplyWriter, false);
            throw new BusinessRuntimeException(ErrorCodes.ERR_PARAM.getCode(), e.getMessage());
        }

        finished(workbook, multiplyWriter, true);

    }


    private SXSSFSheet crateSheet(SXSSFWorkbook workbook, SheetInfo sheetInfo) {
        int sheetNum = sheetInfo.getSheetNum();
        String sheetName = String.format("%s%d", sheetInfo.getSheetName(), sheetNum);
        SXSSFSheet sheet = workbook.createSheet(sheetName);
        sheetInfo.setSheetNum(++sheetNum);
        return sheet;
    }


    /**
     * 删除临时文件
     * @param workbook
     * @param multiplyWriter
     * @param
     */
    public void finished(SXSSFWorkbook workbook, MultiplyWriter multiplyWriter, boolean always){
        while (always && !multiplyWriter.isFinished()){
        }
        workbook.dispose();
    }



    

}
