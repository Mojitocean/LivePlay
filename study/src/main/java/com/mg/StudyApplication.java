
package com.mg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


/**
 * packageName com.mg
 *
 * @author mj
 * @className StudyApplication
 * @date 2025/11/7
 * @description TODO
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class StudyApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudyApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ 学习模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}