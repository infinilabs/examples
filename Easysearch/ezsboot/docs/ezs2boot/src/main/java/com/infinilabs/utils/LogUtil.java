package com.infinilabs.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogUtil {

    /**
     * 打印info级别日志
     *
     * @param params 参数
     */
    public static void info(String... params) {
        log.info(String.join(",", params));
    }

    /**
     * 打印info级别日志
     *
     * @param format 带有占位符的日志
     * @param args   占位符替换的内容
     */
    public static void formatInfo(String format, Object... args) {
        log.info(String.join(",", String.format(format, args)));
    }

    /**
     * 打印warn级别日志
     *
     * @param params 参数
     */
    public static void warn(String... params) {
        log.warn(String.join(",", params));
    }

    /**
     * 打印warn级别日志
     *
     * @param format 带有占位符的日志
     * @param args   占位符替换的内容
     */
    public static void formatWarn(String format, Object... args) {
        log.warn(String.join(",", String.format(format, args)));
    }

    /**
     * 打印error(error)级别日志
     *
     * @param params 参数
     */
    public static void error(String... params) {
        log.error(String.join(",", params));
    }

    /**
     * 打印error(error)级别日志
     *
     * @param format 带有占位符的日志
     * @param args   占位符替换的内容
     */
    public static void formatError(String format, Object... args) {
        log.error(String.join(",", String.format(format, args)));
    }
}