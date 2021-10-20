
package com.lishiliang.core.exception;



public class BusinessRuntimeException extends RuntimeException {
    
    private static final long serialVersionUID = -1670833729936480395L;

    /**
     * 错误码
     */
    private String errCode;
    
    /**
     * 错误信息
     */
    private String errMsg;
    
    /**
     * 附加信息。
     */
    private String attach;
    
    public BusinessRuntimeException(String errCode, String errMsg) {
    
        super(String.format("[%s]%s", errCode, errMsg));
        this.errCode = errCode;
        this.errMsg = errMsg;
    }
    
    public BusinessRuntimeException(String errCode, String message, String attach) {
    
        super(String.format("[%s]%s|%s", errCode, message, attach));
        this.errCode = errCode;
        this.errMsg = message;
        this.attach = attach;
    }
    
    public BusinessRuntimeException(String errCode, String message, Throwable cause) {
    
        super(String.format("[%s]%s", errCode, message), cause);
        this.errCode = errCode;
        this.errMsg = message;
    }
    
    public BusinessRuntimeException(String errCode, String message, String attach, Throwable cause) {
    
        super(String.format("[%s]%s|%s", errCode, message, attach), cause);
        this.errCode = errCode;
        this.errMsg = message;
        this.attach = attach;
    }
    
    public String getErrCode() {
    
        return errCode;
    }
    
    public void setErrCode(String errCode) {
    
        this.errCode = errCode;
    }
    
    public String getErrMsg() {
    
        return errMsg;
    }
    
    public void setErrMsg(String errMsg) {
    
        this.errMsg = errMsg;
    }
    
    public String getAttach() {
    
        return attach;
    }
    
    public void setAttach(String attach) {
    
        this.attach = attach;
    }
    
}
