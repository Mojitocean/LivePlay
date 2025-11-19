package com.mg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * packageName com.mg
 *
 * @author mj
 * @className LivePlayApplication
 * @date 2025/11/19
 * @description TODO
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class LivePlayApplication {
    public static void main(String[] args) {
        SpringApplication.run(LivePlayApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ 直播模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}