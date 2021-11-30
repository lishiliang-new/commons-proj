package com.lishiliang.db.rowmapper;

import com.lishiliang.core.exception.BusinessRuntimeException;
import com.lishiliang.core.utils.ErrorCodes;
import com.lishiliang.core.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.Nullable;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;


public class LocalRowMapper<T> implements RowMapper<T> {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalRowMapper.class);
    
    private Class<?> targetClazz;
    
    private HashMap<String, Field> fieldMap;


    private int columnCount;
    @Nullable
    private int[] columnTypes;
    @Nullable
    private String[] columnNames;
    @Nullable
    private String[] columnChangeNames;

    public LocalRowMapper(Class<?> targetClazz) {
    
        this.targetClazz = targetClazz;
        fieldMap = new HashMap<>();
        Field[] fields = targetClazz.getDeclaredFields();
        for (Field field : fields) {
            // 同时存入大小写，如果表中列名区分大小写且有列ID和列iD，则会出现异常。
            // 阿里开发公约，建议表名、字段名必须使用小写字母或数字；禁止出现数字开头，禁止两个下划线中间只出现数字。
            fieldMap.put(field.getName(), field);
            // fieldMap.put(getFieldNameUpper(field.getName()), field);
        }
    }
    
    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {

        if (rowNum == 0) {

            logger.info("开始进行映射");

            ResultSetMetaData rsmd = rs.getMetaData();
            this.columnCount = rsmd.getColumnCount();
            this.columnTypes = new int[this.columnCount];
            this.columnNames = new String[this.columnCount];
            this.columnChangeNames = new String[this.columnCount];
            for (int i = 0; i < this.columnCount; i++) {
                this.columnTypes[i] = rsmd.getColumnType(i + 1);
                //数据库表字段为单词和下划线
                this.columnNames[i] = JdbcUtils.lookupColumnName(rsmd, i + 1);
                //将数据库字段名改为驼峰
                String columnChangeName = JdbcUtils.convertUnderscoreNameToPropertyName(this.columnNames[i]);
                this.columnChangeNames[i] = fieldMap.containsKey(columnChangeName) ? columnChangeName : null;
            }
            // could also get column names
        }

        return processRow(rs, rowNum);
    }


    private T processRow(ResultSet rs, int rowNum) {
        T obj = null;

        try {
            obj = (T) targetClazz.newInstance();

            for (int i = 0; i < columnCount; i++) {

                String columnName = this.columnNames[i];
                String columnChangeName = this.columnChangeNames[i];

                if(columnChangeName == null){
                    //若model中未定义该字段时，不做处理
                    continue;
                }
                //model中的字段名为驼峰
                Class fieldClazz = fieldMap.get(columnChangeName).getType();
                Field field = fieldMap.get(columnChangeName);
                field.setAccessible(true);

                // fieldClazz == Character.class || fieldClazz == char.class
                if (fieldClazz == int.class || fieldClazz == Integer.class) { // int
                    Object value = rs.getObject(columnName);
                    BigDecimal decimalValue = new BigDecimal(Objects.toString(value, "0"));
                    field.set(obj, value == null ? null : decimalValue.intValue());
                } else if (fieldClazz == boolean.class || fieldClazz == Boolean.class) { // boolean
                    field.set(obj, rs.getBoolean(columnName));
                } else if (fieldClazz == String.class) { // string
                    field.set(obj, rs.getString(columnName));
                } else if (fieldClazz == float.class) { // float
                    Object value = rs.getObject(columnName);
                    BigDecimal decimalValue = new BigDecimal(Objects.toString(value, "0.0"));
                    field.set(obj, value == null ? null : decimalValue.floatValue());
                } else if (fieldClazz == double.class || fieldClazz == Double.class) { // double
                    Object value = rs.getObject(columnName);
                    BigDecimal decimalValue = new BigDecimal(Objects.toString(value, "0.0"));
                    field.set(obj, value == null ? null : decimalValue.doubleValue());
                } else if (fieldClazz == BigDecimal.class) { // bigdecimal
                    field.set(obj, rs.getBigDecimal(columnName));
                } else if (fieldClazz == short.class || fieldClazz == Short.class) { // short
                    Object value = rs.getObject(columnName);
                    BigDecimal decimalValue = new BigDecimal(Objects.toString(value, "0"));
                    field.set(obj, value == null ? null : decimalValue.shortValue());
                } else if (fieldClazz == Date.class) { // date
                    field.set(obj, rs.getTimestamp(columnName));
                } else if (fieldClazz == Timestamp.class) { // timestamp
                    field.set(obj, rs.getTimestamp(columnName));
                } else if (fieldClazz == Long.class || fieldClazz == long.class) { // long
                    Object value = rs.getObject(columnName);
                    BigDecimal decimalValue = new BigDecimal(Objects.toString(value, "0"));
                    field.set(obj, value == null ? null : decimalValue.longValue());
                }
                field.setAccessible(false);
            }
        } catch (Exception e) {
            logger.error("映射异常：" + e.getMessage(), e);
            throw new BusinessRuntimeException(ErrorCodes.DB_ROW_MAPPER_ERROR.getCode(), ErrorCodes.DB_ROW_MAPPER_ERROR.getDesc());
        }
        return obj;
    }
}
