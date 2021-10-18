package com.lishiliang.db.log;

import org.apache.commons.lang3.StringUtils;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

public class P6SpyLogger implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared,
        String sql, String url) {
        
        if (StringUtils.isNotBlank(sql)) {
            return now + " | took " + elapsed + "ms | " + "\n " + sql + ";";
        } 
        return "";
    }
}
