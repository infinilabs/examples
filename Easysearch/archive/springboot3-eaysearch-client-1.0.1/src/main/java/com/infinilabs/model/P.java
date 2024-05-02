package com.infinilabs.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(title = "分页查询", name = "P")
public class P<T> {

    @Schema(title = "当前页数据")
    private List<T> records;

    @Schema(title = "总条数")
    private long total;

    @Schema(title = "每页条数")
    private long size;

    @Schema(title = "第几页")
    private long current;

    @Schema(title = "sortCursor 游标")
    private Object[] sortCursor;

    public P(List<T> records, long total, long size, long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
    }
}
