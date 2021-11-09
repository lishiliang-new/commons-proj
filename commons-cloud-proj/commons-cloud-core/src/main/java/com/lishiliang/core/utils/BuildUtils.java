package com.lishiliang.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.lishiliang.model.DataModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

import java.util.*;
import java.util.function.Consumer;


public final class BuildUtils {




    /**
     * 通用构建DataModel工具类
     */
    public static class DataModelBuilder {
        /**
         * @return
         */
        public static DataModel build() {

            return build(null);
        }

        /**
         * @param obj
         * @param msg 自定义 msg
         * @return
         */
        public static DataModel build(Object obj, String msg) {

            return Builder.of(build(obj)).with(DataModel::setMsg, msg).build();
        }

        /**
         * 构建 DataModel
         * @param obj
         * @return
         */
        public static DataModel build(Object obj) {

            DataModel dataModel = new DataModel();
            dataModel.setMsg("操作完成");
            if (obj == null) {
                return dataModel;
            }
            //PageInfo类型处理
            if (obj instanceof PageInfo) {
                PageInfo pageInfo = (PageInfo)obj;
                dataModel.setCount(pageInfo.getTotal());
                dataModel.setData(pageInfo.getList());
            } else {
                dataModel.setData(obj);
            }

            return dataModel;
        }
    }


    /**
     * 通用构建sql工具类
     */
    public static class SqlBuilder {


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

    /**
     * Mapper对resultHandler参数 自定义结果处理器的 的构建器
     * fixme 传入resultHandler流式查询  返回值必须是void才生效
     */
    public static class ResultContainerBuilder {


        public static <T, R> ResultHandler<T> withResultOne(Consumer<T> action, R resultContainer) {
            return ResultContainer.build(action, resultContainer);
        }


        public static <T, R> ResultHandler<T> withResultList(Consumer<T> action, List<R> resultListContainer) {

            return ResultContainer.build(action, resultListContainer);
        }


        /**
         * @param action 本次操作 (单参数)
         * @param <T> 泛型 T=原始行数据
         * @return
         */
        public static <T> ResultContainer<T, PageInfo<T>> withPageInfo(Consumer<T> action) {
            PageInfo pageInfo = new PageInfo();
            pageInfo.setList(new ArrayList());
            return ResultContainer.build(action, pageInfo);
        }

        /**
         * @param action 本次操作 (单参数)
         * @param <T> 泛型 T=原始行数据
         * @return
         */
        public  <T> ResultHandler<T> withPageInfo(Consumer<T> action, PageInfo<T> pageInfoContainer) {

            return ResultContainer.build(action, pageInfoContainer);
        }


        /**
         * @param action 本次操作 (单参数)
         * @param <T> 泛型 T=原始行数据
         * @return
         */
        public static  <T, R> ResultHandler<T> withMap(Consumer<T> action, R mapContainer) {

            return ResultContainer.build(action, mapContainer);
        }

    }




    /**
     * mybatis流式查询结果容器
     * @param <T, R> 行泛型,返回值泛型
     */
    public static class ResultContainer<T, R> implements ResultHandler<T> {

        private R resultObject;
        //前一行数据
        private T preRow;
        //当前行号
        private int rowNum = 1;

        //对行数据操作
        private final Consumer<T> action;

        private boolean isPageInfo = false;

        public ResultContainer(Consumer<T> action, R container) {
            this.action = action;
            this.resultObject = container;
            this.isPageInfo = container instanceof PageInfo;
        }


        public static <T, R> ResultContainer build(Consumer<T> action, R container) {
            return new ResultContainer(action, container);
        }


        @Override
        public void handleResult(ResultContext<? extends T> resultContext) {
            T t = resultContext.getResultObject();
            preRow = t;
            action.accept(t);
            if (isPageInfo) {
                PageInfo pageInfo = (PageInfo)resultObject;
                pageInfo.getList().add(t);
            }
            rowNum++;
        }

        public R getResultObject() {
            return resultObject;
        }

        public Consumer<T> getAction() {
            return action;
        }
    }

}
