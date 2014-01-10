package com.blazemeter.jmeter.utils;

import org.apache.log.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Created by dzmitrykashlach on 12/27/13.
 */
@Aspect
public class Log {
    private static Logger logger = BmLog.getLogger();

    @Pointcut
            ("execution(* com.blazemeter.jmeter.testexecutor.RemoteTestRunnerGui.configure(..))")
    private void remoteTestRunnerGui() {
    }

    @Pointcut
            ("execution(* com.blazemeter.jmeter.testexecutor.BmTestManager.NotifyTestInfoChanged())")
    private void bmTestManager() {
    }

    @Around("remoteTestRunnerGui()")
    public void logRemoteTestRunnerGui(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] methodArgs = joinPoint.getArgs();
        logger.debug("LOGGING ASPECT: Call method " + methodName + " with args " + methodArgs);
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
        }
        logger.debug("LOGGING ASPECT: Method " + methodName + " returns " + result);
    }

    @Around("bmTestManager()")
    public void logNotify(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] methodArgs = joinPoint.getArgs();
        logger.debug("LOGGING ASPECT: Call method " + methodName + " with args " + methodArgs);
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
        }
        logger.debug("LOGGING ASPECT: Method " + methodName + " returns " + result);
    }
}