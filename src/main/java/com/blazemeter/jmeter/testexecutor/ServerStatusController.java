package com.blazemeter.jmeter.testexecutor;

import com.blazemeter.jmeter.api.BlazemeterApi;
import com.blazemeter.jmeter.utils.BmLog;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 5/24/13
 * Time: 4:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerStatusController {

    private static ServerStatus serverStatus = ServerStatus.NOT_AVAILABLE;

    public enum ServerStatus {AVAILABLE, NOT_AVAILABLE}

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> task;
    private Thread connectionController;
    private static ServerStatusController serverStatusController = null;

    private ServerStatusController() {
    }


    public static ServerStatusController getServerStatusController() {
        if (serverStatusController == null) {
            serverStatusController = new ServerStatusController();
        }
        return serverStatusController;
    }

    public static ServerStatus getServerStatus() {
        return serverStatus;
    }

    public void start() {
        connectionController = new Thread(new Runnable() {
            @Override
            public void run() {
                String serverURL = BlazemeterApi.BmUrlManager.getServerUrl();
                ServerStatus latestServerStatus = serverStatus;

                try {
                    URL url = new URL(serverURL);
                    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                    httpConn.setInstanceFollowRedirects(false);
                    httpConn.setRequestMethod("HEAD");
                    httpConn.setConnectTimeout(5000);
                    httpConn.connect();
                    BmLog.debug("Connection with " + serverURL + " is OK.");
                    serverStatus = ServerStatus.AVAILABLE;
                    httpConn.disconnect();
                } catch (SocketTimeoutException e) {
                    BmLog.error("Connection with " + serverURL + " was not established, server is unavailable");
                    serverStatus = ServerStatus.NOT_AVAILABLE;
                } catch (MalformedURLException e) {
                    BmLog.error("SERVER URL is invalid! Check 'blazemeter.url' in jmeter.properties");
                    serverStatus = ServerStatus.NOT_AVAILABLE;
                } catch (java.net.ConnectException e) {
                    BmLog.error(serverURL + " is down ");
                    serverStatus = ServerStatus.NOT_AVAILABLE;
                } catch (ProtocolException e) {
                    BmLog.error("HTTP Request method was not set up for checking connection");
                    serverStatus = ServerStatus.NOT_AVAILABLE;
                } catch (IOException e) {
                    BmLog.error("Connection with" + serverURL + "was not established, server is unavailable");
                    serverStatus = ServerStatus.NOT_AVAILABLE;
                } finally {
                    if (!latestServerStatus.equals(serverStatus)) {
                        NotifyServerStatusChanged();
                    }
                }
            }
        });
        if (this.task == null || this.task.isDone()) {
            this.task = scheduler.scheduleAtFixedRate(connectionController, 1, 10, TimeUnit.SECONDS);
            BmLog.console("ConnectionController is started");
        }
    }

    public void stop() {
        if (this.task != null && !this.task.isDone()) {
            this.task.cancel(true);
            this.task = null;
            BmLog.console("ConnectionController is stopped");
        }
    }

    public interface ServerStatusChangedNotification {
        public void onServerStatusChanged();
    }

    public List<ServerStatusChangedNotification> serverStatusChangedNotificationListeners = new ArrayList<ServerStatusChangedNotification>();

    public void NotifyServerStatusChanged() {
        for (ServerStatusChangedNotification sscn : serverStatusChangedNotificationListeners) {
            sscn.onServerStatusChanged();
        }
    }
}
