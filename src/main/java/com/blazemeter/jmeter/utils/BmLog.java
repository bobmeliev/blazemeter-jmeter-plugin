package com.blazemeter.jmeter.utils;

import com.blazemeter.jmeter.constants.Constants;
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
    private static final boolean console_write = JMeterUtils.getPropDefault("blazemeter.console_write", false);
    private static final boolean log_write = JMeterUtils.getPropDefault("blazemeter.log_write", false);

    private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    private static Logger logger = LoggingManager.getLoggerFor(Constants.BM_LOGGER);

    public static Logger getLogger() {
        return logger;
    }

    public static void info(String msg) {
        if (console_write) {
            console(msg);
        }
        if (log_write)
            logger.info(msg);
    }

    public static void debug(String msg) {
        logger.setPriority(Priority.DEBUG);
        if (console_write) {
            console(msg);
        }
        if (log_write) {
            logger.debug(msg);
        }
        logger.unsetPriority();
    }



    public static void error(String msg, Throwable ex) {
        logger.setPriority(Priority.ERROR);
        if (console_write) {
            console(msg);

        }
        if (log_write) {
            logger.error(msg, ex);
        }
        logger.unsetPriority();
    }

    public static void error(String msg) {
        error(msg, null);
    }

    public static void error(Throwable ex) {
        error("", ex);
    }

    private static void console(String msg) {
        System.out.println(format.format(new Date()) + " - " + Thread.currentThread().getId() + " : " + msg);
    }
}