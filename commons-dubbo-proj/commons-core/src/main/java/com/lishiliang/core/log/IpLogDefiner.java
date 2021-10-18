package com.lishiliang.core.log;

import ch.qos.logback.core.PropertyDefinerBase;
import com.lishiliang.core.utils.IpUtils;

/**
* @author: lisl
*/
public class IpLogDefiner extends PropertyDefinerBase {

    @Override
    public String getPropertyValue() {
        return IpUtils.getIp();
    }

}