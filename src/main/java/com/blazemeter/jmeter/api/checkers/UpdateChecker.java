package com.blazemeter.jmeter.api.checkers;

import com.blazemeter.jmeter.api.BlazemeterApi;
import com.blazemeter.jmeter.entities.PluginUpdate;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.utils.BmLog;
import com.blazemeter.jmeter.utils.Utils;

import java.util.Date;

/**
 * Created by dzmitrykashlach on 3/7/14.
 */
public class UpdateChecker {
    private static long lastUpdateCheck = 0;

    private UpdateChecker() {
    }

    public static void checkForUpdates() {
        long now = new Date().getTime();
        if (lastUpdateCheck + 3600000 > now) {
            return;
        }

        lastUpdateCheck = now;
        new Thread(new Runnable() {
            @Override
            public void run() {
                BmTestManager bmTestManager = BmTestManager.getInstance();
                PluginUpdate update = BlazemeterApi.getInstance().getUpdate(bmTestManager.getUserKey());
                if (update != null && update.getVersion().isNewerThan(Utils.getPluginVersion())) {
                    BmLog.info(String.format("Update found from %s to %s", Utils.getPluginVersion().toString(true), update.getVersion().toString(true)));
                    bmTestManager.NotifyPluginUpdateReceived(update);
                } else {
                    BmLog.info("No update found");
                }
            }
        }).start();
    }
}
