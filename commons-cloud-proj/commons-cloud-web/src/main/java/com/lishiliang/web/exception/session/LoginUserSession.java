
 /**
 * @Title: LoginUserSession.java 
 * @Package:com.lishiliang.dubbo.web.session
 * @desc: TODO  
 * @author: lisl
 * @date
 */
 
package com.lishiliang.web.exception.session;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;


 
/**    
 * @desc: 登录用户的Session
 * @author: lisl
 */
@Scope(value="session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class LoginUserSession<T> implements Serializable {

    private static final long serialVersionUID = 1559368476483960562L;
    /**
     * 登录用户名
     */
    private String userName;
    /**
     * 登录用户对象
     */
    private T userVo;
    
    public String getUserName() {
    
        return userName;
    }
    
    public void setUserName(String userName) {
    
        this.userName = userName;
    }
    
    public T getUserVo() {
    
        return userVo;
    }
    
    public void setUserVo(T userVo) {
    
        this.userVo = userVo;
    }
    /**
     * @desc: 用户退出，清空Session
     */
    public void clearSession(){
        this.userName = null;
        this.userVo = null;
    }
    /**
     * @desc: 用户登录
     * @param userName
     * @param userVo
     */
    public void newSession(String userName, T userVo){
        this.userName = userName;
        this.userVo = userVo;
    }
}
