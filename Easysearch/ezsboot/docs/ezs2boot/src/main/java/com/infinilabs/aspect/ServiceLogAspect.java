package com.infinilabs.aspect;

import com.infinilabs.utils.LogUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class ServiceLogAspect {

    private ThreadLocal<StopWatch> t = new ThreadLocal<>();

    /**
     * AOP通知：
     * 1. 前置通知：在方法调用之前执行
     * 2. 后置通知：在方法正常调用之后执行
     * 3. 环绕通知：在方法调用之前和之后，都分别可以执行的通知
     * 4. 异常通知：如果在方法调用过程中发生异常，则通知
     * 5. 最终通知：在方法调用之后执行
     */

    /**
     * 切面表达式：
     * execution 代表所要执行的表达式主体
     * 第一处 * 代表方法返回类型 *代表所有类型
     * 第二处 包名代表aop监控的类所在的包
     * 第三处 .. 代表该包以及其子包下的所有类方法
     * 第四处 * 代表类名，*代表所有类
     * 第五处 *(..) *代表类中的方法名，(..)表示方法中的任何参数
     *
     * @return
     * @throws Throwable
     */
    @Pointcut("execution(* com.infinilabs.controller..*.*(..))")
    public void controllerPerformance() {
    }

    @Pointcut("execution(* com.infinilabs.service..*.*(..))")
    public void servicePerformance() {
    }

    @Pointcut("execution(* com.infinilabs.dao..*.*(..))")
    public void repositoryPerformance() {
    }

    @Before("controllerPerformance()")
    public void startWatch() {
        StopWatch stopWatch = new StopWatch("controller");
        t.set(stopWatch);
    }

    @After("controllerPerformance()")
    public void endWatch() {
        Long takeTime = t.get().getTotalTimeMillis();
        if (takeTime > 3000) {
            LogUtil.formatError("-------- 执行结束，耗时：%s 毫秒 --------", takeTime);
        } else if (takeTime > 2000) {
            LogUtil.formatWarn("-------- 执行结束，耗时：%s 毫秒 --------", takeTime);
        } else {
            LogUtil.formatInfo("-------- 执行结束，耗时：%s 毫秒 --------", takeTime);
        }
        t.remove();
    }


    @Around("servicePerformance() || repositoryPerformance() ")
    public Object watchPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            if (t.get().isRunning()) {
                t.get().stop();
            }
            t.get().start(joinPoint.getSignature().toString());
        } catch (IllegalStateException e) {
            LogUtil.formatError("watch start error:", e);
        }

        Object proceed = joinPoint.proceed();

        try {
            if (t.get().isRunning()) {
                t.get().stop();
            }
        } catch (IllegalStateException e) {
            LogUtil.formatError("watch end error: %v", e);
        }

        return proceed;
    }
}