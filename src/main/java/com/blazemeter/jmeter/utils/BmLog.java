package com.blazemeter.jmeter.utils;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.log.Priority;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 3/26/12
 * Time: 14:46
 */
public class BmLog {
    private static final boolean writeToConsole = JMeterUtils.getPropDefault("blazemeter.console_write", false);
    private static final boolean writeToLog = JMeterUtils.getPropDefault("blazemeter.log_write", false);

    private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    private static Logger logger = LoggingManager.getLoggerFor("Blazemeter-plugin");
    private static final boolean debugEnabled = JMeterUtils.getPropDefault("blazemeter.debug_enabled", false);

    public static Logger getLogger() {
        return logger;
    }

    public static void console(String msg) {
        if (writeToConsole) {
            System.out.println(format.format(new Date()) + " - " + Thread.currentThread().getId() + " : " + msg);
        }

        if (writeToLog)
            logger.info(msg);
    }

    public static void debug(String msg) {
        if (debugEnabled) {
            if (!logger.isDebugEnabled()) {
                logger.setPriority(Priority.DEBUG);
            }
            if (writeToConsole) {
                System.out.println(format.format(new Date()) + " - " + Thread.currentThread().getId() + " : " + msg);
            }
            if (writeToLog)
                logger.debug(msg);

        }
    }

    public static void error(String msg) {
        error(msg, null);
    }

    public static void error(Throwable ex) {
        error("", ex);
    }

    public static void error(String msg, Throwable ex) {
        if (writeToConsole) {
            System.out.println(format.format(new Date()) + " - " + Thread.currentThread().getId() + " : " + msg);
        }
        if (writeToLog)
            logger.error(msg, ex);


    }
}