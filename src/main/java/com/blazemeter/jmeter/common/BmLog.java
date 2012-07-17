package com.blazemeter.jmeter.common;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 3/26/12
 * Time: 14:46
 */
public class BmLog {
    private static final boolean printConsole=JMeterUtils.getPropDefault("blazemeter.console_print",false);
    private static final boolean writeConsole=JMeterUtils.getPropDefault("blazemeter.console_write",false);
    private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

    private static Logger logger = LoggingManager.getLoggerFor("Blazemeter-plugin");

    public static void console(String msg){
        if(printConsole)
        {
            System.out.println(format.format(new Date())+" - "+Thread.currentThread().getId()+" : "+msg);
        }

        if(writeConsole)
            logger.info(msg);
    }


    public static void error(String msg){
        error(msg, null);
    }
    public static void error(Throwable ex){
        error("",ex);
    }
    public static void error(String msg, Throwable ex){
        logger.error(msg,ex);
    }
}