package com.lishiliang.core.configuration;
 
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 
import feign.Logger;
import org.springframework.context.annotation.Primary;


@Configuration
public class FeignLogConfiguration {


    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * 自定义日志系统代理feign日志系统
     */
    @Bean
    @Primary
    public Logger logger() {
        return new FeignLogger();
    }



    public class FeignLogger extends Logger {

        /**
         * 记录Feign调试日志
         * @param configKey FeignClient 类名#方法名
         * @param format 日志格式化字符串 如：%s%s
         * @param args 格式化参数
         */
        @Override
        protected void log(String configKey, String format, Object... args) {
            org.slf4j.Logger logger = LoggerFactory.getLogger(FeignLogger.class);
            logger.info(String.format(methodTag(configKey) + format, args));
        }
    }


}