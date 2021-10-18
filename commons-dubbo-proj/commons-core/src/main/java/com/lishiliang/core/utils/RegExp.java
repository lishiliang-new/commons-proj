package com.lishiliang.core.utils;

public class RegExp {
    public static final String MOBILE = "^(\\+86)?1[3-9]\\d{9}$";
    public static final String MOBILE_NO_86 = "^1[3-9]\\d{9}$";
    public static final String BANK_CARD_NO = "^\\d{15,19}$";
    public static final String IDENTITY_NO = "^\\d{17}[0-9a-zA-Z]$|^\\d{15}$";
    public static final String REAL_NAME = "^[\\u4e00-\\u9fa5]{1,128}$";
    public static final String BANK_TYPE = "^\\d{4}$";
    public static final String CREDIT_CVV = "^\\d{1,6}$";
    public static final String CREDIT_EXPIRE = "^\\d{2}0[1-9]$|^\\d{2}1[012]$";
    public static final String SMS_VER_CODE = "^[a-zA-Z0-9]{6,10}$";
    public static final String INPUT_CHARSET = "^GBK$|^UTF-8$";
    public static final String OUT_TRADE_NO = "^\\w{1,32}$";
    public static final String PARTNER = "^\\d{10}$";
    public static final String SIGN_TYPE = "^MD5$";
    public static final String TRADE_NO = "^\\d{32}$";
    public static final String CHARGE_NO = "^\\d{25}$";
    public static final String PAY_TOTAL_NO = "^\\d{22}$";
    public static final String USER_CODE = "^(\\+86)?1[0-9]{10}$|^\\d{1,9}$|^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$";
    public static final String BIND_NO = "^[\\d]{26}$";
    public static final String SMS_SESSION_ID = "^[\\d]{25}$";
    public static final String BOOL = "^0|1$";
    public static final String TRANSFER_NO = "^\\d{25}$";
    public static final String WITHDRAW_BIND_NO = "^\\d{22}$";
    public static final String AMOUNT = "^[1-9]\\d{0,11}$";
    public static final String TRANSFER_BIND_NO = "^[\\d]{22}$";
    public static final String PAY_CHANNEL = "^1|2$";
    public static final String CARD_TYPE = "^1|2$";
    public static final String yyyyMMddHHmmss = "^\\d{14}$";//严格判断太复杂了，影响性能。
    public static final String PERSON_ID = "^\\d+$";
    public static final String SERIAL_NUMBER = "^\\d{25}$";
    public static final String EMAIL = "^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$";
}
