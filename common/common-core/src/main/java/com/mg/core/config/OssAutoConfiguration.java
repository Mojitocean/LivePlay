package com.mg.core.config;

import com.mg.core.service.OssTemplate;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * packageName com.mg.core.config
 *
 * @author mj
 * @className OssAutoConfiguration
 * @date 2025/5/26
 * @description TODO
 */
@AllArgsConstructor
@EnableConfigurationProperties({OssConfig.class})
public class OssAutoConfiguration {
    private final OssConfig config;

    @Bean
    @Primary
    @ConditionalOnMissingBean(OssTemplate.class)
    @ConditionalOnProperty(name = "oss.enable", havingValue = "true")
    public OssTemplate ossTemplate() {
        return new OssTemplate(config);
    }
}