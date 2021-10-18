package com.lishiliang.core.utils;


public class Enums {
    
    /**
     * @desc: 状态枚举
     * @author: lisl
     */
    public static enum STATUS {
        INVALID(0, "无效"),
        VALID(1, "有效");
        
        int code;
        String desc;
        
        private STATUS(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        public int getCode() {
        
            return code;
        }
        public String getDesc() {
        
            return desc;
        }
    }
}
