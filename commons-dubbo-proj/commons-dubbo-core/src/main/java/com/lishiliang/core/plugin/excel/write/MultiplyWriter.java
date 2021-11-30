package com.lishiliang.core.plugin.excel.write;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.poi.openxml4j.util.ZipArchiveThresholdInputStream;
import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.openxml4j.util.ZipFileZipEntrySource;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.streaming.SheetDataWriter;
import org.apache.poi.xssf.usermodel.XSSFChartSheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author lisl
 * @version 1.0
 * @date 2021-11-30 11:18
 * @desc :
 * fixme 大部分copy自SXSSFWorkbook 主要实现输入流和输出流同时进行
 */
public class MultiplyWriter {

    private static final Logger logger = LoggerFactory.getLogger(ExcelFastWriter.class);

    private final XSSFWorkbook _wb;
    private Map<SXSSFSheet,XSSFSheet> _sxFromXHash;
    private Map<XSSFSheet,SXSSFSheet> _xFromSxHash;
    private boolean isFinished;

    public MultiplyWriter(SXSSFWorkbook workbook) {
        Objects.requireNonNull(workbook);

        this._wb = workbook.getXSSFWorkbook();
        try {
            _sxFromXHash = (HashMap) FieldUtils.readDeclaredField(workbook, "_sxFromXHash", true);
            _xFromSxHash = (HashMap) FieldUtils.readDeclaredField(workbook, "_xFromSxHash", true);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    /**
     * @see SXSSFWorkbook#write
     * @param stream
     * @throws Exception
     */
    public void multiplyWrite(OutputStream stream) throws Exception {
        //Save the template
        File tmplFile = TempFile.createTempFile("poi-sxssf-multiply-template", ".xlsx");
        boolean deleted;
        try {
            try (FileOutputStream os = new FileOutputStream(tmplFile)) {
                _wb.write(os);
            }

            //Substitute the template entries with the generated sheet data files
            try (ZipSecureFile zf = new ZipSecureFile(tmplFile);
                 ZipFileZipEntrySource source = new ZipFileZipEntrySource(zf)) {
                multiplyInjectData(source, stream);
            }
        } finally {
            deleted = tmplFile.delete();
            multiplyFinished();
        }
        if(!deleted) {
            throw new IOException("Could not delete temporary file after processing: " + tmplFile);
        }
    }

    public void multiplyFinished() {
        this.isFinished = true;
    }

    /**
     * @see SXSSFWorkbook#injectData
     * @throws Exception
     */
    protected void multiplyInjectData(ZipEntrySource zipEntrySource, OutputStream out) throws Exception {
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(out);
        try {
            Enumeration<? extends ZipArchiveEntry> en = zipEntrySource.getEntries();
            while (en.hasMoreElements()) {
                ZipArchiveEntry ze = en.nextElement();
                ZipArchiveEntry zeOut = new ZipArchiveEntry(ze.getName());
                zeOut.setSize(ze.getSize());
                zeOut.setTime(ze.getTime());
                zos.putArchiveEntry(zeOut);
                try (final InputStream is = zipEntrySource.getInputStream(ze)) {
                    if (is instanceof ZipArchiveThresholdInputStream) {
                        // #59743 - disable Threshold handling for SXSSF copy
                        // as users tend to put too much repetitive data in when using SXSSF :)
                        ((ZipArchiveThresholdInputStream) is).setGuardState(false);
                    }
                    XSSFSheet xSheet = getSheetFromZipEntryName(ze.getName());
                    if (xSheet != null && !(xSheet instanceof XSSFChartSheet)) {
                        SXSSFSheet sxSheet = getSXSSFSheet(xSheet);
                        InputStream xis = null;
                        try {
                            //todo
                            xis = getWorksheetXMLInputStreamNotClose(sxSheet);
                            copyMultiplyStreamAndInjectWorksheet(is, zos, xis, sxSheet);
                        } finally {
                            if (xis != null) {
                                xis.close();
                            }
                        }
                    } else {
                        IOUtils.copy(is, zos);
                    }
                }finally {
                    zos.closeArchiveEntry();
                }
            }
        } finally {
            zos.finish();
            zipEntrySource.close();
        }
    }

    /**
     * @see SXSSFSheet#getWorksheetXMLInputStream
     * @param sxSheet
     * @return
     * @throws Exception
     */
    private InputStream getWorksheetXMLInputStreamNotClose(SXSSFSheet sxSheet) throws Exception {
        SheetDataWriter sheetDataWriter = (SheetDataWriter) FieldUtils.readDeclaredField(sxSheet, "_writer", true);
        return sheetDataWriter.getWorksheetXMLInputStream();
    }

    /**
     * @see SXSSFWorkbook#copyStreamAndInjectWorksheet
     * @param in
     * @param out
     * @param worksheetData
     * @param sxSheet
     * @throws IOException
     */
    private static void copyMultiplyStreamAndInjectWorksheet(InputStream in, OutputStream out, InputStream worksheetData, SXSSFSheet sxSheet) throws IOException {
        InputStreamReader inReader = new InputStreamReader(in, StandardCharsets.UTF_8);
        OutputStreamWriter outWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(worksheetData);
        boolean needsStartTag = true;
        int c;
        int pos=0;
        String s="<sheetData";
        int n=s.length();
        //Copy from "in" to "out" up to the string "<sheetData/>" or "</sheetData>" (excluding).
        while(((c=inReader.read())!=-1))
        {
            if(c==s.charAt(pos))
            {
                pos++;
                if(pos==n)
                {
                    if ("<sheetData".equals(s))
                    {
                        c = inReader.read();
                        if (c == -1)
                        {
                            outWriter.write(s);
                            break;
                        }
                        if (c == '>')
                        {
                            // Found <sheetData>
                            outWriter.write(s);
                            outWriter.write(c);
                            s = "</sheetData>";
                            n = s.length();
                            pos = 0;
                            needsStartTag = false;
                            continue;
                        }
                        if (c == '/')
                        {
                            // Found <sheetData/
                            c = inReader.read();
                            if (c == -1)
                            {
                                outWriter.write(s);
                                break;
                            }
                            if (c == '>')
                            {
                                // Found <sheetData/>
                                break;
                            }

                            outWriter.write(s);
                            outWriter.write('/');
                            outWriter.write(c);
                            pos = 0;
                            continue;
                        }

                        outWriter.write(s);
                        outWriter.write('/');
                        outWriter.write(c);
                        pos = 0;
                        continue;
                    }
                    else
                    {
                        // Found </sheetData>
                        break;
                    }
                }
            }
            else
            {
                if(pos>0) {
                    outWriter.write(s,0,pos);
                }
                if(c==s.charAt(0))
                {
                    pos=1;
                }
                else
                {
                    outWriter.write(c);
                    pos=0;
                }
            }
        }
        outWriter.flush();
        if (needsStartTag)
        {
            outWriter.write("<sheetData>\n");
            outWriter.flush();
        }
        //Copy the worksheet data to "out".
        out = new BufferedOutputStream(out);
        // fixme 这里是重点
        logger.info("复制当前 sheet表：{}", sxSheet.getSheetName());
        while (!sxSheet.areAllRowsFlushed()){
            copy(bufferedInputStream, out);
        }
        copy(bufferedInputStream, out);
        out.flush();

        outWriter.write("</sheetData>");
        outWriter.flush();
        //Copy the rest of "in" to "out".
        while(((c=inReader.read())!=-1)) {
            outWriter.write(c);
        }
        outWriter.flush();

    }

    public static void copy(BufferedInputStream bfi, OutputStream out) throws IOException {
        final byte[] buff = new byte[8192];
        for (int count; (count = bfi.read(buff)) != -1;) {
            if (count > 0) {
                out.write(buff, 0, count);
            }
        }
    }

    private XSSFSheet getSheetFromZipEntryName(String sheetRef)
    {
        for(XSSFSheet sheet : _sxFromXHash.values())
        {
            if(sheetRef.equals(sheet.getPackagePart().getPartName().getName().substring(1))) {
                return sheet;
            }
        }
        return null;
    }

    SXSSFSheet getSXSSFSheet(XSSFSheet sheet)
    {
        return _xFromSxHash.get(sheet);
    }

    //FIXME
    public void flushWriter(SXSSFSheet sheet) throws Exception
    {
        logger.info("结束sheet表：{}", sheet.getSheetName());

        SheetDataWriter sheetDataWriter = (SheetDataWriter) FieldUtils.readDeclaredField(sheet, "_writer", true);
        Writer _out = (Writer) FieldUtils.readDeclaredField(sheetDataWriter, "_out", true);
        //刷新Writer中余留数据
        _out.flush();
        //设置（allFlushed）终止标识
        sheet.flushRows();
        //关闭
        _out.close();

        logger.info("sheet表：{} 结束成功", sheet.getSheetName());
    }


}
