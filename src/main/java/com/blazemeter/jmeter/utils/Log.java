package com.blazemeter.jmeter.utils;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Created by dzmitrykashlach on 12/27/13.
 */
@Aspect
public class Log {
    private static Logger logger = LoggingManager.getLoggerFor("bm-logger");

    @Before("execution(* com.blazemeter.jmeter.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        logger.debug("Start execution : " + joinPoint.getSignature().getName());
    }

    @After("execution(* com.blazemeter.jmeter.*(..))")
    public void logAfter(JoinPoint joinPoint) {
        logger.debug("End execution : " + joinPoint.getSignature().getName());
    }
}
