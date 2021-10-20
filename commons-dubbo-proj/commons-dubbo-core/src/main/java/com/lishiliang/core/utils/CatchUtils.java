package com.lishiliang.core.utils;

import com.lishiliang.core.exception.BusinessRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class CatchUtils<T> {

    private static final Logger logger = LoggerFactory.getLogger(CatchUtils.class);
    
    private static final String INTERRUPTED_EXCEPTION_MSG = "线程中断异常";
    
    /**
     * 优雅处理异常 (不抛出)
     * @param proException
     */
    public static void catchException(ProcessException proException) {
        try {
            proException.process();
        } catch (InterruptedException e) {
            logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    //静态调用参数
    public static <T> void catchException(StaticProcessException<T> proException, T param) {
        try {

            proException.process(param);
        } catch (InterruptedException e) {
            logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    /**
     * 优雅处理异常 (并抛出RuntimeException)
     * @param proException
     */
    public static void catchExceptionAndThrow(ProcessException proException) {
        try {
            proException.process();
        } catch (InterruptedException e) {
            logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    public static <T> void catchExceptionAndThrow(StaticProcessException<T> proException, T param) {
        try {
            proException.process(param);
        } catch (InterruptedException e) {
            logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }
    }


    //抛出BusinessRuntimeException异常
    public static void catchExceptionAndThrow(ProcessException proException, ErrorCodes errorCodes) {
        try {
            proException.process();
        } catch (InterruptedException e) {
            logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error(e.toString());
            throw new BusinessRuntimeException(errorCodes.code, errorCodes.desc);
        }
    }

    public static <T> void catchExceptionAndThrow(StaticProcessException<T> proException, T param, ErrorCodes errorCodes) {
        try {
            proException.process(param);
        } catch (InterruptedException e) {
            logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error(e.toString());
            throw new BusinessRuntimeException(errorCodes.code, errorCodes.desc);
        }
    }

    //自定义errMsg
    public static void catchExceptionAndThrow(ProcessException proException, ErrorCodes errorCodes, String errMsg) {
        try {
            proException.process();
        } catch (InterruptedException e) {
            logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error(e.toString());
            throw new BusinessRuntimeException(errorCodes.code, errMsg);
        }
    }

    public static <T> void catchExceptionAndThrow(StaticProcessException<T> proException, T param, ErrorCodes errorCodes, String errMsg) {
        try {
            proException.process(param);
        } catch (InterruptedException e) {
            logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error(e.toString());
            throw new BusinessRuntimeException(errorCodes.code, errMsg);
        }
    }


    @FunctionalInterface
    public interface ProcessException {
        void process() throws Exception;
    }

    @FunctionalInterface
    public interface StaticProcessException<T> {
        void  process(T param) throws Exception;
    }



    /**
     * 有返回值的异常处理
     */
    public static class Receive  {

        /**
         * catch 并可接收
         * @param receiveWithCatch
         * @param <R>
         * @return
         */
        public static <R> R catchException(ReceiveWithCatch<R> receiveWithCatch) {
            try {
                return receiveWithCatch.process();
            } catch (InterruptedException e) {
                logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error(e.toString());
            }
            return null;
        }

        /**
         * @param receiveWithCatch 静态调用参数 如 :Class::forName
         * @param t
         * @param <T>
         * @param <R>
         * @return
         */
        public static <T, R> R catchException(StaticReceiveWithCatch<T, R> receiveWithCatch, T t) {
            try {
                return receiveWithCatch.process(t);
            } catch (InterruptedException e) {
                logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error(e.toString());
            }
            return null;
        }


        /**
         * 可接收 异常时抛出RuntimeException异常
         * @param receiveWithCatch
         * @param <R>
         * @return
         */
        public static <R> R catchExceptionAndThrow(ReceiveWithCatch<R> receiveWithCatch) {
            try {
                return receiveWithCatch.process();
            } catch (InterruptedException e) {
                logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error(e.toString());
                throw new RuntimeException(e);
            }
            return null;
        }

        /**
         *
         * @param receiveWithCatch 静态调用参数
         * @param param
         * @param <T>
         * @param <R>
         * @return
         */
        public static <T, R> R catchExceptionAndThrow(StaticReceiveWithCatch<T, R> receiveWithCatch, T param) {
            try {
                return receiveWithCatch.process(param);
            } catch (InterruptedException e) {
                logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error(e.toString());
                throw new RuntimeException(e);
            }
            return null;
        }

        /**
         * 可接收 异常时抛出BusinessRuntimeException异常
         * @param receiveWithCatch
         * @param <R>
         * @return
         */
        public static <R> R catchExceptionAndThrow(ReceiveWithCatch<R> receiveWithCatch, ErrorCodes errorCodes) {
            try {
                return receiveWithCatch.process();
            } catch (InterruptedException e) {
                logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error(e.toString());
                throw new BusinessRuntimeException(errorCodes.code, errorCodes.desc);
            }
            return null;
        }

        public static <T, R> R catchExceptionAndThrow(StaticReceiveWithCatch<T, R> receiveWithCatch, T param, ErrorCodes errorCodes) {
            try {
                return receiveWithCatch.process(param);
            } catch (InterruptedException e) {
                logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error(e.toString());
                throw new BusinessRuntimeException(errorCodes.code, errorCodes.desc);
            }
            return null;
        }

        //自定义errMsg
        public static <R> R catchExceptionAndThrow(ReceiveWithCatch<R> receiveWithCatch, ErrorCodes errorCodes, String errMsg) {
            try {
                return receiveWithCatch.process();
            } catch (InterruptedException e) {
                logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error(e.toString());
                throw new BusinessRuntimeException(errorCodes.code, errMsg);
            }
            return null;
        }

        public static <T, R> R catchExceptionAndThrow(StaticReceiveWithCatch<T, R> receiveWithCatch, T param, ErrorCodes errorCodes, String errMsg) {
            try {
                return receiveWithCatch.process(param);
            } catch (InterruptedException e) {
                logger.error(INTERRUPTED_EXCEPTION_MSG + e.getMessage(), e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error(e.toString());
                throw new BusinessRuntimeException(errorCodes.code, errMsg);
            }
            return null;
        }

        @FunctionalInterface
        public interface ReceiveWithCatch<R> {
            R process() throws Exception;
        }

        @FunctionalInterface
        public interface StaticReceiveWithCatch<T, R> {
            R process(T t) throws Exception;
        }
    }

}
