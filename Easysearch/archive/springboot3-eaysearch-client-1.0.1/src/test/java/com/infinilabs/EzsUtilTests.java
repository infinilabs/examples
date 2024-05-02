package com.infinilabs;

import com.infinilabs.utils.EzsUtil;
import com.infinilabs.utils.LogUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class EzsUtilTests {

    @Autowired
    private EzsUtil ezsUtil;

    @Test
    void testCreateIndex() throws IOException {
        Boolean flag = ezsUtil.createIndex("easysearch_idx");
        if (flag) {
            LogUtil.info("Easysearch 索引创建成功");
        }
    }

    @Test
    void testDeleteIndex() throws IOException {
        Boolean flag = ezsUtil.deleteIndex("easysearch_idx");
        if (flag) {
            LogUtil.info("Easysearch 索引删除成功");
        }
    }
}