

package com.lishiliang.core.utils;



public enum ErrorCodes {
    
    ERR_PARAM("100000000","参数错误","参数错误"),
    
    ERR_DUPLICATE_KEY("100000001","数据已存在，请勿重复操作","数据操作失败，数据重复"),
    
    USER_NOT_LOGIN("100000002","用户未登录","用户未登录"),
    
    USER_NOT_AUTH("100000003","用户未授权","用户未授权"),
    
    REDIS_SCAN_IOEXCEPTION("100000004", "Redis在操作Scan命令时异常", "Redis在操作Scan命令时异常"),
    
    TIMEOUT_ERROR("100000005","RPC调用超时","RPC调用超时"),
    
    DB_ROW_MAPPER_ERROR("100000006","数据库字段映射异常","数据库字段映射异常"),
    
    DB_UPDATE_NOT_ONE("100000007","数据库更新记录不唯一","数据库更新记录不唯一"),
    
    DATASOURCE_CREATE_ERROR("100000008","数据源创建失败","数据源创建失败"),
    
    ERROR_FILE_REPORT("100000009","Excel文件创建失败","Excel文件创建失败"),
    
    ERROR_ZIP_FILE("100000010","ZIP文件创建失败","ZIP文件创建失败"),
    
    ERROR_UNZIP_FILE("100000011","解压ZIP文件失败","解压ZIP文件失败"),
    
    ERROR_ALLOW_HTML_FILE("100000012", "加载XSS过滤白名单异常,请检查文件 allow-html.json", "加载XSS过滤白名单异常,请检查文件 allow-html.json"),
    
    ERROR_HTTP_POST("100000013","HTTP请求失败","HTTP请求失败"),
    ;
    
    String code;
    String desc;
    String outDesc;
    
    ErrorCodes(String code,String desc,String outDesc){
        this.code = code;
        this.desc = desc;
        this.outDesc = outDesc;
    }
    public String getCode() {
    
        return code;
    }
    public String getDesc() {
    
        return desc;
    }
    public String getOutDesc() {
    
        return outDesc;
    }
}
