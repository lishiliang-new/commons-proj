package com.lishiliang.core.plugin.excel.write;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * @author lisl
 * @version 1.0
 * @date 2021-11-30 17:06
 * @desc :
 */
public class ExcelFastWriterTest {

    @Test
    public void writeExcel() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        String sql = " select * from test.table where id > ? limit 1000000 ";
        ExcelResultSetHandler handler = new ExcelResultSetHandler(dataSource(), sql, new Object[]{1});
        SheetInfo sheetInfo = new SheetInfo(handler);
        ExcelFastWriter.getInstance().writeExcel("E:\\home\\app\\files\\out/cesi.xlsx", sheetInfo);

        System.out.println(String.format("耗时【%d】ms", stopWatch.getTime()));
    }


    public DataSource dataSource() {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://192.168.70.153?autoReconnect=true&useUnicode=true&characterEncoding=UTF8&rewriteBatchedStatements=true&serverTimezone=Asia/Shanghai&useCursorFetch=true");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        return dataSource;
    }
}
