package com.mg.core.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * packageName com.mg.core.domain
 *
 * @author mj
 * @className Attachment
 * @date 2025/5/27
 * @description TODO
 */
@Data
public class Attachment extends BaseEntity implements Serializable {

    /**
     * 文件业务ID
     */
    private String businessId;

    /**
     * 附件OSS名称
     */
    private String name;

    /**
     * 附件原始名称
     */
    private String originalName;

    /**
     * 附件地址
     */
    private String url;

    /**
     * 附件大小
     */
    private String size;
}