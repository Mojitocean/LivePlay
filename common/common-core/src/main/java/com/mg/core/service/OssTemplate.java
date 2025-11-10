package com.mg.core.service;

import com.mg.core.config.OssConfig;
import com.mg.core.domain.Attachment;
import com.mg.core.exception.ServerException;
import com.mg.core.utils.DateUtil;
import com.mg.core.utils.IdGenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * packageName com.mg.core.service
 *
 * @author mj
 * @className OssTemplate
 * @date 2025/5/26
 * @description TODO
 */

@Log4j2
@RequiredArgsConstructor
public class OssTemplate implements InitializingBean {

    private static final long PART_SIZE = 100 * 1024 * 1024; // 100MB
    private final OssConfig config;
    // 模拟缓存：可替换为 Redis 或数据库持久化
    private final Map<String, String> uploadIdCache = new ConcurrentHashMap<>();
    private final Map<String, Set<Integer>> uploadedPartCache = new ConcurrentHashMap<>();
    private String bucketName;
    private S3Client s3Client;

    @Override
    public void afterPropertiesSet() {
        this.bucketName = config.getBucketName();
        S3Configuration s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(config.getPathStyleAccess())
                .chunkedEncodingEnabled(false)
                .build();
        this.s3Client = S3Client.builder()
                .region(Region.of(config.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey())))
                .endpointOverride(URI.create(config.getEndpoint()))
                .serviceConfiguration(s3Configuration)
                .build();
        log.info("初始化OSS成功!");
    }

    /**
     * 检查文件合法性
     *
     * @param suffix
     * @return
     */
    public boolean checkFileSuffix(String suffix) {
        return StringUtils.isNotBlank(suffix)
                ? Arrays.stream(config.getSuffix()).noneMatch(suffix.toLowerCase()::endsWith)
                : false;
    }

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    public Attachment uploadFile(MultipartFile file) {
        return uploadNormal(file);
    }


    /**
     * 下载文件
     *
     * @param objectKey
     * @return
     */
    public InputStream downloadFile(String objectKey) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        return s3Client.getObject(getRequest);
    }

    /**
     * 普通上传
     *
     * @param file
     * @return
     */
    private Attachment uploadNormal(MultipartFile file) {
        String objectKey = generateObjectKey(file);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .metadata(buildMetadata(file))
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();
        try (InputStream is = file.getInputStream()) {
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return buildAttachment(objectKey, file);
        } catch (IOException e) {
            throw new ServerException("上传文件失败", e);
        }
    }

    /**
     * 拷贝数组
     *
     * @param source
     * @param length
     * @return
     */
    private byte[] copyOf(byte[] source, int length) {
        byte[] result = new byte[length];
        System.arraycopy(source, 0, result, 0, length);
        return result;
    }

    /**
     * 生成对象Key
     */
    private String generateObjectKey(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (StringUtils.isNotBlank(originalFilename) && originalFilename.contains(".")) {
            ext = "." + StringUtils.substringAfterLast(originalFilename, ".");
        }
        return IdGenUtil.nextIdStr() + ext;
    }

    /**
     * 构建元数据
     */
    private Map<String, String> buildMetadata(MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("originalName", Base64.getEncoder().encodeToString(Objects.requireNonNull(file.getOriginalFilename()).getBytes(StandardCharsets.UTF_8)));
        metadata.put("size", String.valueOf(file.getSize()));
        metadata.put("contentType", file.getContentType());
        return metadata;
    }

    /**
     * 构建文件访问URL
     */
    private String buildFileUrl(String objectKey) {
        return "/" + bucketName + "/" + objectKey;
    }

    /**
     * 构建附件信息
     */
    private Attachment buildAttachment(String objectKey, MultipartFile file) {
        Attachment attachment = new Attachment();
        attachment.setId(IdGenUtil.nextIdStr());
        attachment.setName(objectKey);
        attachment.setOriginalName(file.getOriginalFilename());
        attachment.setUrl(buildFileUrl(objectKey));
        attachment.setSize(String.valueOf(file.getSize()));
        attachment.setCreateTime(DateUtil.getNow());
        return attachment;
    }
}