package com.lishiliang.core.utils;

import java.util.HashMap;
import java.util.Map;

public class Context {


    public static final String TRACE_ID = "traceId";

    private static final ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal() {
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<>();
        }
    };

    //InheritableThreadLocal可继承父线程Local信息
    private static final InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();


    public static void initInheritableThreadLocal(String traceId) {

        inheritableThreadLocal.set(traceId);
    }

    public static void initThreadLocal() {
        threadLocal.set(new HashMap<>());
    }

    public static String getCurrentContextTraceId() {
        return inheritableThreadLocal.get();
    }

    public static void removeTraceId() {
        inheritableThreadLocal.remove();
    }

    public static void removeThreadLocal() {
        threadLocal.remove();
    }

    public static void setAttachment(String key, Object param) {
        threadLocal.get().put(key, param);
    }

    public static Object getAttachment(String key) {
        return threadLocal.get().get(key);
    }

    public static Map<String, Object> getCurrentContextParam() {
        return threadLocal.get();
    }
}
