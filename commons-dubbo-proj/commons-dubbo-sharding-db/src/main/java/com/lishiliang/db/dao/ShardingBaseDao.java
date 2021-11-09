
 /**
 * @Title: ShardingBaseDao.java 
 * @Package:com.lishiliang.sharding.db.dao
 * @desc: TODO  
 * @author: lisl
 * @date
 */

 package com.lishiliang.db.dao;

import java.util.List;
import java.util.Map;

import com.lishiliang.core.exception.BusinessRuntimeException;
import com.lishiliang.core.utils.AssertUtils;
import com.lishiliang.core.utils.ErrorCodes;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;



/**
 * @desc 分库分表数据源
 */
@Repository
public class ShardingBaseDao extends BaseDao {

    @Autowired
    protected SqlSessionTemplate shardingSqlSessionTemplate;
    
    @Autowired
    protected JdbcTemplate shardingJdbcTemplate;
    
    @Autowired
    protected TransactionTemplate shardingTransactionTemplate;
    
    /**
     * 查询列表，若无记录则返回null
     * @return
     */
    protected List<Map<String, Object>> queryForListInShardingMaster(String sql, Object... objs) {
        return queryForList(shardingJdbcTemplate, sql, objs);
    }
    
    /**
     * 查询列表，若无记录则返回null
     * @return
     */
    protected <T> List<T> queryForListWithMapperInShardingMaster(String sql, RowMapper<T> rowMapper, Object... objs) {
        return shardingJdbcTemplate.query(sql, rowMapper, objs);
    }
    
    /**
     * 查询数据，若无记录则返回null
     * @return
     */
    protected <T> T queryForObjectWithMapperInShardingMaster(String sql, RowMapper<T> rowMapper, Object... objs) {
        
        List<T> results = shardingJdbcTemplate.query(sql, objs, new RowMapperResultSetExtractor<>(rowMapper, 1));
        return DataAccessUtils.uniqueResult(results);
    }

    /**
     * 查询列表，若无记录则返回null
     * @return
     */
    protected List<Map<String, Object>> queryForShardingList(JdbcTemplate jdbcTemplate, String sql, Object... objs) {
        return jdbcTemplate.queryForList(sql, objs);
    }
    /**
     * @desc: 更新数据库，且只有1条数据被更新或插入
     * @param sql
     * @param batchArgs
     */
    protected void updateForShardingOne(String sql, Object ... batchArgs){
        
        try {
            int count = shardingJdbcTemplate.update(sql, batchArgs);
            AssertUtils.isEqual(1, count, ErrorCodes.DB_UPDATE_NOT_ONE.getCode(), ErrorCodes.DB_UPDATE_NOT_ONE.getDesc());
            
        } catch (DuplicateKeyException e) {
            throw new BusinessRuntimeException(ErrorCodes.ERR_DUPLICATE_KEY.getCode(), ErrorCodes.ERR_DUPLICATE_KEY.getDesc());
        }
    }
    
    /**
     * 批量插入数据(同一执行SQL)
     * @param sql
     * @param objsList
     * @param errCode 重复主键异常时,错误码
     * @param errMsg 重复主键异常时,错误描述
     */
    protected void batchShardingInsert(String sql, List<Object[]> objsList) {
        shardingJdbcTemplate.batchUpdate(sql, objsList);
    }
    
    /**
     * 批量修改数据(同一执行SQL)
     * @param sql
     * @param objsList
     */
    protected void batchShardingUpdate(final String sql, final List<Object[]> objsList, final String message, 
            final String errorCode) {
        shardingJdbcTemplate.batchUpdate(sql, objsList);
    }
    
    /**
     * 批量更新,重复主键抛出异常
     * @param sql sql语句
     * @param batchArgs 批量更新的参数
     * @param errCode 重复主键时,抛出的错误码
     * @param errMsg 重复主键时的异常信息描述
     * @return
     */
    protected void batchShardingUpdateForDuplicateKey(String sql, List<Object[]> batchArgs) {
        try {
            shardingJdbcTemplate.batchUpdate(sql, batchArgs);
        } catch (DuplicateKeyException e) {
            throw new BusinessRuntimeException(ErrorCodes.ERR_DUPLICATE_KEY.getCode(), ErrorCodes.ERR_DUPLICATE_KEY.getDesc());
        }
    }
}
