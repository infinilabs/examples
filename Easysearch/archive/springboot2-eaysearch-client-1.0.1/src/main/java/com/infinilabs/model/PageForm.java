package com.infinilabs.model;

import lombok.Data;

@Data
public class PageForm {

    private int pageSize = 20;

    private int pageNum = 1;

    private String orderBy;

    private Boolean orderType = false;

    private Object[] sortCursor;

    private boolean isAll = false;
}
