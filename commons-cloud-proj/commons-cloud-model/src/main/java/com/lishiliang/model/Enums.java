package com.lishiliang.model;

public class Enums {

    /**
     * @author lisl
     * @desc 地区类型
     */
    public static enum AreaType {
        COUNTRR(1), //国家
        PROVINCE(2), //省,直辖市,自治区
        CITY(3), //市
        AREA(4); //区县

        int code;

        AreaType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    /**
     * @author lisl
     * @desc 删除标志
     */
    public static enum ActiveFlag {
        DELETE(0), //0-删除
        NORMAL(1); //1-正常

        int code;

        ActiveFlag(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}