package com.lishiliang.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.lishiliang.core.model.DataModel;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public final class BuildUtils {


    //去空 去null splitter
    public static final Splitter splitter = Splitter.on(",").trimResults().omitEmptyStrings();

    //去null joiner
    public static final Joiner joiner = Joiner.on(",").skipNulls();



    /**
     * @return
     */
    public static DataModel bulidDataModel() {

        return bulidDataModel(null);
    }

    /**
     * @param obj
     * @param msg 自定义 msg
     * @return
     */
    public static DataModel bulidDataModel(Object obj, String msg) {

        return Builder.of(bulidDataModel(obj)).with(DataModel::setMsg, msg).build();
    }

    /**
     * 构建 DataModel
     * @param obj
     * @return
     */
    public static DataModel bulidDataModel(Object obj) {

        DataModel dataModel = new DataModel();
        dataModel.setMsg("操作完成");
        if (obj == null) {
            return dataModel;
        }
        //PageInfo类型处理
        if ("com.github.pagehelper.PageInfo".equalsIgnoreCase(obj.getClass().getName())) {
            Map map = JSON.parseObject(JSON.toJSONString(obj));
            dataModel.setCount((Integer)map.get("total"));
            dataModel.setData(map.get("list"));
        } else {
            dataModel.setData(obj);
        }

        return dataModel;
    }


    /**
     * 通用构建sql工具类
     */
    public static class SqlBuildUtils {


        /**
         * 通用修改sql
         * @param tableName
         * @param t
         * @param sql
         * @param params
         * @param <T>
         *  todo 有条件的自行拼接where条件
         */
        public static <T> void buildUpdateSql (String tableName, T t, StringBuilder sql, List<Object> params) {

            Map<String, Object> modelMap = JSON.parseObject(JSON.toJSONString(t));
            sql.append("update " + tableName + " set ");
            modelMap.forEach((k, v)->{
                sql.append(Utils.humpToLine(k)).append( " = ").append( " ?,");
                params.add(v);
            });
            sql.deleteCharAt(sql.length() - 1);

        }

        /**
         * 通用修改sql(可排除字段)
         * @param tableName
         * @param t
         * @param sql
         * @param params
         * @param excludeProps 排除字段集合
         * @param <T>
         */
        public static <T> void buildUpdateSql (String tableName, T t, StringBuilder sql, List<Object> params, List<String> excludeProps) {

            Map<String, Object> modelMap = JSON.parseObject(JSON.toJSONString(t));
            sql.append("update " + tableName + " set ");
            modelMap.forEach((k, v)->{
                if (CollectionUtils.isEmpty(excludeProps) || !excludeProps.contains(k)) {
                    sql.append(Utils.humpToLine(k)).append( " = ").append( " ?,");
                    params.add(v);
                }
            });
            sql.deleteCharAt(sql.length() - 1);

        }

        /**
         * 通用添加sql
         * @param tableName
         * @param excludeProps 需要排除的实体字段属性
         * @param t
         * @param params
         * @param <T>
         * @return
         */
        public static <T> StringBuilder buildInsertSql (String tableName,  T t, List<Object> params, List<String> excludeProps) {

            StringBuilder sql = new StringBuilder();
            //保留value 为 null的map
            Map<String, Object> modelMap = JSON.parseObject(JSON.toJSONString(t, SerializerFeature.WriteMapNullValue));

            sql.append("INSERT INTO " + tableName);
            StringBuilder colunmSql = new StringBuilder();
            StringBuilder valueSql = new StringBuilder();
            modelMap.forEach((k, v)->{
                if (CollectionUtils.isEmpty(excludeProps) || !excludeProps.contains(k)) {
                    colunmSql.append(", ").append(Utils.humpToLine(k));
                    valueSql.append(", ?");
                    params.add(v);
                }
            });
            sql.append(" ( ").append(colunmSql.deleteCharAt(0)).append(" ) ");
            sql.append(" values ( ").append(valueSql.deleteCharAt(0)).append(" ) ");
            return sql;
        }

        /**
         * 批量添加
         * @param tableName 表名
         * @param excludeProps 需要排除的实体字段
         * @param models 添加的数据
         * @param batchArgs sql args
         * @param <T>
         * @return
         */
        public static <T> StringBuilder buildBatchInsertSql (String tableName, List<T> models, List<Object[]> batchArgs, List<String> excludeProps) {

            TypeReference<List<Map<String, Object>>> typeReference = new TypeReference<List<Map<String, Object>>>(){};
            List<Map<String, Object>> modelMaps = JSON.parseObject(JSON.toJSONString(models, SerializerFeature.WriteMapNullValue), typeReference);

            StringBuilder sql = new StringBuilder("INSERT INTO " + tableName);
            StringBuilder colunmSql = new StringBuilder();
            StringBuilder valueSql = new StringBuilder();

            Iterables.forEach(modelMaps, (index, modelMap)->{
                List<Object> objects = Lists.newArrayList();
                modelMap.forEach((k, v)-> {
                    if (CollectionUtils.isEmpty(excludeProps) || !excludeProps.contains(k)) {
                        if (index == 0) {
                            colunmSql.append(", ").append(Utils.humpToLine(k));
                            valueSql.append(", ?");
                        }
                        objects.add(v);
                    }
                });
                batchArgs.add(objects.toArray());
            });

            sql.append(" ( ").append(colunmSql.deleteCharAt(0)).append(" ) ");
            return sql.append(" values ( ").append(valueSql.deleteCharAt(0)).append(" ) ");
        }

        /**
         * 构建inSql
         * @param inList
         * @param colunm
         * @param params
         * @param appendAnd
         */
        public static StringBuilder buildInSql(Collection inList, String colunm, List<Object> params, boolean appendAnd) {

            StringBuilder inSql = new StringBuilder();
            if (CollectionUtils.isNotEmpty(inList)) {
                inSql.append(appendAnd ? " and " : " where ").append(colunm).append(" in ( ");
                for(int i = 0, j = inList.size(); i < j; i++) {
                    if (i == j - 1) {
                        inSql.append(" ?) ");
                    } else {
                        inSql.append(" ?, ");
                    }
                }
                params.addAll(inList);
            }
            return inSql;
        }

        /**
         * 
         * @param inList
         * @param colunm
         * @param params
         * @param appendAnd
         * @return
         */
        public static StringBuilder buildInSql(Object[] inList, String colunm, List<Object> params, boolean appendAnd) {

            return buildInSql(Arrays.asList(inList), colunm, params , appendAnd);
        }
        

    }


}
