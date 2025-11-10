package com.mg.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

/**
 * packageName com.mg.core.config
 *
 * @author mj
 * @className OssConfig
 * @date 2025/5/26
 * @description TODO
 */
@Data
@Primary
@ConfigurationProperties(prefix = "oss")
public class OssConfig {

    /**
     * 对象存储服务的URL
     */
    private String endpoint;

    /**
     * true path-style nginx 反向代理和S3默认支持 pathStyle {http://endpoint/bucketname} false
     * <p/>
     * supports virtual-hosted-style 阿里云等需要配置为 virtual-hosted-style
     * <p/>
     * 模式{http://bucketname.endpoint}
     */
    private Boolean pathStyleAccess = false;

    /**
     * 区域
     */
    private String region;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * Access key就像用户ID，可以唯一标识你的账户
     */
    private String accessKey;

    /**
     * Secret key是你账户的密码
     */
    private String secretKey;

    /**
     * 允许上传的文件格式
     */
    private String[] suffix;

}