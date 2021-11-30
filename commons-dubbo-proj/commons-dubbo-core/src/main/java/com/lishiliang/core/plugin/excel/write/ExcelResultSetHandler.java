package com.lishiliang.core.plugin.excel.write;

import com.lishiliang.core.exception.BusinessRuntimeException;
import com.lishiliang.core.utils.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author lisl
 * 用于jdbc流式查询的处理器
 */
public class ExcelResultSetHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExcelResultSetHandler.class);

    //最终存储数据 rows
    private List<String[]> resultObject = new LinkedList<>();

    @NonNull
    private DataSource dataSource;
    @NonNull
    //预处理sql
    private String sql;
    //sql 条件参数
    private Object[] params = new Object[]{};
    //流式查询每次拉取数据库数量
    int fetchSize = 10000;

    //队列中一批数量
    private int batchSize = 5000;

    //阻塞队列 传递数据
    private BlockingQueue queue = new LinkedBlockingQueue(1000);
    //开始标识
    private volatile boolean queryStart;
    //终止标识
    private volatile boolean stop = false;

    // 若以class作为映射 则使用class中的属性 否则使用查询中的所有属性
    private Class<?> targetClazz;
    private HashMap<String, Field> fieldMap;

    private int rowCount;
    private int columnCount;
    private int rowCellLength;
    @Nullable
    private int[] columnTypes;
    @Nullable
    private String[] columnNames;
    private String[] columnChangeNames;
    //属性在数组中的索引位置
    private HashMap<String, Integer> columnChangeIndex = new HashMap<>();

    //排除属性
    private Set<String> excludePropertyNames = new HashSet<>();


    public ExcelResultSetHandler(DataSource dataSource, String sql, Object[] params) {
        this.dataSource = dataSource;
        this.sql = sql;
        this.params = params;
    }
    public ExcelResultSetHandler(DataSource dataSource, String sql) {
        this.dataSource = dataSource;
        this.sql = sql;
    }

    public Class<?> getTargetClazz() {
        return targetClazz;
    }

    public ExcelResultSetHandler targetClazz(Class<?> targetClazz) {
        this.targetClazz = targetClazz;
        fieldMap = new HashMap<>();
        Field[] fields = targetClazz.getDeclaredFields();
        for (Field field : fields) {
            // 同时存入大小写，如果表中列名区分大小写且有列ID和列iD，则会出现异常。
            // 阿里开发公约，建议表名、字段名必须使用小写字母或数字；禁止出现数字开头，禁止两个下划线中间只出现数字。
            fieldMap.put(field.getName(), field);
            // fieldMap.put(getFieldNameUpper(field.getName()), field);
        }
        return this;
    }





    public final void beforeProcessRow(ResultSet rs) throws SQLException {

        logger.info("===> process row is begin");

        ResultSetMetaData rsmd = rs.getMetaData();
        this.columnCount = rsmd.getColumnCount();
        this.columnTypes = new int[this.columnCount];
        this.columnNames = new String[this.columnCount];
        this.columnChangeNames = new String[this.columnCount];
        for (int i = 0; i < this.columnCount; i++) {
            this.columnTypes[i] = rsmd.getColumnType(i + 1);
            this.columnNames[i] = lookupColumnName(rsmd, i + 1);
            //将数据库字段名改为驼峰
            String columnChangeName = convertUnderscoreNameToPropertyName(this.columnNames[i]);
            columnChangeIndex.put(columnChangeName, i);

            //若排除当前属性
            if (excludePropertyNames.contains(columnChangeName)) {
                this.columnChangeNames[i] = null;
                continue;
            }

            this.columnChangeNames[i] = targetClazz == null || fieldMap.containsKey(columnChangeName) ? columnChangeName : null;
            if (this.columnChangeNames[i] != null) {
                rowCellLength++;
            }
        }

    }


    /**
     *
     * @param rs
     * @param rowNum 从1开始
     * @throws SQLException
     */

    private void processRow(ResultSet rs, int rowNum) {

        String[] row = new String[rowCellLength];
        try {
            int rowIndex = 0;
            for (int i = 0; i < columnCount; i++) {
                if (columnChangeNames[i] != null) {
                    row[rowIndex++] = rs.getString(columnNames[i]);
                }
            }

            resultObject.add(row);

        } catch (Exception e) {
            logger.error("映射异常：rowNum{} msg {}" + e.getMessage(), rowNum, e);
            throw new BusinessRuntimeException(ErrorCodes.DB_ROW_MAPPER_ERROR.getCode(), ErrorCodes.DB_ROW_MAPPER_ERROR.getDesc());
        }

    }

    public void queryStart () {

        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(sql);
        if (stop) {
            logger.warn("查询已经终止");
            return;
        }

        logger.info("===> query is start");
        this.queryStart = true;

        Thread thread = new Thread() {
            @Override
            public void run() {
                Connection conn = null;
                ResultSet rs = null;
                try {
                    //执行查询
                    conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setFetchSize(fetchSize <= 0 ? 10000 : fetchSize);
                    //组装查询参数
                    for (int i = 0; i < params.length; i++) {
                        ps.setObject(i + 1, params[i]);
                    }
                    //执行查询
                    rs = ps.executeQuery();

                    handlerResultSet(rs);

                } catch (Exception e) {
                    logger.error("查询异常：{}" , e.getMessage());
                    throw new BusinessRuntimeException(ErrorCodes.DB_ROW_MAPPER_ERROR.getCode(), ErrorCodes.DB_ROW_MAPPER_ERROR.getDesc());
                } finally {
                    queryStart = false;
                    stop = true;
                    filish(conn, rs);
                }
            }
        };
        thread.start();

    }

    private void filish(Connection conn, ResultSet rs) {
        try {
            if (conn != null) {
                conn.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            logger.error(" close coon error msg :{}", e.getMessage());
        }
    }

    private void handlerResultSet(ResultSet rs) {

        try {
            //初始化字段数据
            beforeProcessRow(rs);

            //处理每一条数据
            while (!stop && rs.next()) {
                processRow(rs, ++rowCount);

                //每batchSize条数据则向队列添加数据
                if (rowCount % batchSize == 0) {
                    //超过队列最大元素 会进行阻塞 如果到了阻塞时间则为丢弃当前数据 todo
                    queue.offer(resultObject, 60 , TimeUnit.SECONDS);
                    resultObject = new LinkedList<>();
                }
            }

            //查询完成向队列中添加最后一批数据
            if (!resultObject.isEmpty()) {
                queue.offer(resultObject, 60, TimeUnit.SECONDS);
            }
            queryStart = false;
            logger.info("===> process row is end");

        } catch (InterruptedException e) {
            logger.error("线程中断异常: " + e.getMessage(), e);
            queue.clear();
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("映射异常：{}" + e.getMessage(), e);
            throw new BusinessRuntimeException(ErrorCodes.DB_ROW_MAPPER_ERROR.getCode(), ErrorCodes.DB_ROW_MAPPER_ERROR.getDesc());
        }

    }


    public ExcelResultSetHandler batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }


    public boolean isStart() {
        return queryStart;
    }



    public BlockingQueue getQueue() {
        return this.queue;
    }

    public int getRowCellLength() {
        return rowCellLength;
    }


    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public String[] getColumnChangeNames() {
        return columnChangeNames;
    }

    public Set<String> getExcludePropertyNames() {
        return excludePropertyNames;
    }

    public void setExcludePropertyNames(Set<String> excludePropertyNames) {
        this.excludePropertyNames = excludePropertyNames;
    }

    /**
     * fixme copy from JdbcUtils.
     * @param resultSetMetaData
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    public static String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
        String name = resultSetMetaData.getColumnLabel(columnIndex);
        if (!StringUtils.hasLength(name)) {
            name = resultSetMetaData.getColumnName(columnIndex);
        }
        return name;
    }

    /**
     * fixme copy from JdbcUtils.
     * Convert a column name with underscores to the corresponding property name using "camel case".
     * A name like "customer_number" would match a "customerNumber" property name.
     * @param name the column name to be converted
     * @return the name using "camel case"
     */
    public static String convertUnderscoreNameToPropertyName(@Nullable String name) {
        StringBuilder result = new StringBuilder();
        boolean nextIsUpper = false;
        if (name != null && name.length() > 0) {
            if (name.length() > 1 && name.charAt(1) == '_') {
                result.append(Character.toUpperCase(name.charAt(0)));
            }
            else {
                result.append(Character.toLowerCase(name.charAt(0)));
            }
            for (int i = 1; i < name.length(); i++) {
                char c = name.charAt(i);
                if (c == '_') {
                    nextIsUpper = true;
                }
                else {
                    if (nextIsUpper) {
                        result.append(Character.toUpperCase(c));
                        nextIsUpper = false;
                    }
                    else {
                        result.append(Character.toLowerCase(c));
                    }
                }
            }
        }
        return result.toString();
    }

}