package com.infinilabs.model;

import lombok.Data;

import java.util.List;

@Data
public class P<T> {

    private List<T> records;

    private long total;

    private long size;

    private long current;

    private Object[] sortCursor;

    public P(List<T> records, long total, long size, long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
    }
}
