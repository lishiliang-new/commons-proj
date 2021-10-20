package com.lishiliang.db.rowmapper;

import org.apache.commons.collections4.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class BuildLocalRowMapper {


    /**
     * 构建自定义rowMapper
     * @param action 本次操作 (单参数)
     * @param clazz  实体类类型
     * @param <T, R> 入参,返回值泛型
     * @return
     */
    public static <T> LocalRowMapper build(Consumer<T> action, Class clazz) {

        return new LocalRowMapper<T>(clazz) {
            @Override
            public T mapRow(ResultSet rs, int rowNum) throws SQLException {
                T t =  super.mapRow(rs, rowNum);
                action.accept(t);
                return t;
            }
        };
    }

    /**
     * 构建自定义rowMapper
     * @param action 本次操作 (2个参数)
     * @param clazz  实体类类型
     * @param <T, U> 入参泛型
     * @return
     */
    public static <T, U> LocalRowMapper build(BiConsumer<T, U> action, U param, Class clazz) {

        return new LocalRowMapper<T>(clazz) {
            @Override
            public T mapRow(ResultSet rs, int rowNum) throws SQLException {
                T t =  super.mapRow(rs, rowNum);
                action.accept(t, param);
                return t;
            }
        };
    }

    /**
     * 构建自定义rowMapper
     * @param action 本次操作 (可接收)
     * @param clazz  实体类类型
     * @param <T, R> 入参,返回值泛型
     * @return
     */
    public static <T, R> LocalRowMapper build(Function<T, R> action, R param, Class clazz) {

        AtomicReference<R> rAtomicReference = new AtomicReference<>();
        LocalRowMapper localRowMapper =  new LocalRowMapper<T>(clazz) {
            @Override
            public T mapRow(ResultSet rs, int rowNum) throws SQLException {
                T t =  super.mapRow(rs, rowNum);
                rAtomicReference.set(action.apply(t));
                return t;
            }
        };
        param =  rAtomicReference.get();
        return localRowMapper;
    }


    /**
     * 构建LocalRowMapper 同时可进行数据分组
     * @param action 分组条件(字段,组合字段 : ()->object.getId + object.getCode或 Object::getId )
     * @param groupMap 分组存储
     * @param <T>
     * @return
     */
    public static <T, R> LocalRowMapper buildGroupMap(Function<T, R> action, Map<R, List<T>> groupMap, Class clazz) {

        return new LocalRowMapper<T>(clazz) {
            @Override
            public T mapRow(ResultSet rs, int rowNum) throws SQLException {
                T t =  super.mapRow(rs, rowNum);
                R key = action.apply(t);
                List<T> tList = groupMap.get(key);
                if (CollectionUtils.isEmpty(tList)) {
                    tList = new ArrayList<>();
                }
                tList.add(t);
                groupMap.put(key, tList);
                return t;
            }
        };
    }


}
