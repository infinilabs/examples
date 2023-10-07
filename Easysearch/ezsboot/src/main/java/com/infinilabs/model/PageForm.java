package com.infinilabs.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "基础分页信息", name = "PageForm")
public class PageForm {

    @Schema(title = "页条数", example = "20")
    private int pageSize = 20;

    @Schema(title = "第几页", example = "1")
    private int pageNum = 1;

    @Schema(title = "排序字段: 可选: 不同列表排序不同需再协商", example = "desc")
    private String orderBy;

    @Schema(title = "排序规则 true升序 false降序")
    private Boolean orderType = false;

    @Schema(title = "排序游标")
    private Object[] sortCursor;

    @Schema(title = "查询所有: 默认查询今日及所有未审核单子")
    private boolean isAll = false;
}