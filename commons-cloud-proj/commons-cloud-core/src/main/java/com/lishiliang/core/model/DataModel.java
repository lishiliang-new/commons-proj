package com.lishiliang.core.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


public class DataModel implements Serializable {
    
    private static final long serialVersionUID = -3922593565045634123L;
    
    /**
     * 默认0成功
     */
    private Integer code = 0;
    private String msg;
    /**
     * 数据的总记录数
     */
    private long count;
    /**
     * 当前页码的数据
     */
    private List<?> dataList;
    /**
     * 返回对象数据
     */
    private Object data;
    /**
     * 附加数据
     */
    private Map<String, Object> additionalData;

    public DataModel(){}
    public DataModel(String msg) {
        this.msg = msg;
    }
    public DataModel(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    
    public Integer getCode() {
    
        return code;
    }
    
    public void setCode(int code) {
    
        this.code = code;
    }
    
    public String getMsg() {
    
        return msg;
    }
    
    public void setMsg(String msg) {
    
        this.msg = msg;
    }
    
    public long getCount() {
    
        return count;
    }
    
    public void setCount(long count) {
    
        this.count = count;
    }
    
    public List<?> getDataList() {
        return dataList;
    }

    public void setDataList(List<?> dataList) {
        this.dataList = dataList;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Map<String, Object> getAdditionalData() {
    
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
    
        this.additionalData = additionalData;
    }
}
