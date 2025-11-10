package com.mg.core.domain;

import com.fasterxml.jackson.annotation.JsonFilter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName com.mg.core.domain
 *
 * @author mj
 * @className PageEntity
 * @date 2025/5/31
 * @description TODO
 */
@Data
@JsonFilter("pageEntityFilter")
public abstract class PageEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分页参数
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 0, message = "页码不能小于0")
    private Integer pageNum = 1;
    /**
     * 分页参数
     */
    @NotNull(message = "条数不能为空")
    @Min(value = 1, message = "分页最小为1")
    private Integer pageSize = 10;
}