package cn.lili;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

/**
 * 基础API
 *
 * @author Chopper
 * @since 2020/11/17 3:38 下午
 */
@EnableCaching
@SpringBootApplication
@EnableHystrix
public class CommonApiApplication {

    public static void main(String[] args) {
        System.setProperty("rocketmq.client.logUseSlf4j","true");
        SpringApplication.run(CommonApiApplication.class, args);
    }

}