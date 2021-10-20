package com.lishiliang.core.log;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.lishiliang.core.utils.Context;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: lisl
 */
public class TraceIdConvert extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {

        return StringUtils.defaultString(Context.getCurrentContextTraceId(), "");
    }
}
