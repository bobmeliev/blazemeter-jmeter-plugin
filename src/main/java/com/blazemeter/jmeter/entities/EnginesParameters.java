package com.blazemeter.jmeter.entities;

import com.blazemeter.jmeter.testexecutor.BmTestManager;

/**
 * Created by dzmitrykashlach on 2/12/14.
 */
public class EnginesParameters {
    private static EnginesParameters enginesParameters = null;

    private int numberOfUsers = 0;
    private int consoles = 1;
    private int engines = 0;
    private int servers = 0;
    private String engineSize = "m1.medium";
    private int userPerEngine = 0;


    public static EnginesParameters getEnginesParameters(int numberOfUsers) {
        if (enginesParameters == null) {
            enginesParameters = new EnginesParameters();
        }
        if (numberOfUsers != enginesParameters.numberOfUsers) {
            enginesParameters.countParameters(numberOfUsers);
        }

        return enginesParameters;
    }

    public String getEngineSize() {
        return engineSize;
    }

    public int getUserPerEngine() {
        return userPerEngine;
    }

    public int getConsoles() {
        return consoles;
    }

    public int getEngines() {
        return engines;
    }

    private void countParameters(int numberOfUsers) {

        UserInfo userInfo = BmTestManager.getInstance().getUserInfo();


        if (numberOfUsers <= 300) {
            this.userPerEngine = numberOfUsers;
        } else {
            this.servers = numberOfUsers / 300;
            if (this.servers < userInfo.getMaxEnginesLimit()) {
                if (numberOfUsers % 300 > 0) {
                    this.servers++;
                }
            } else {
                this.engineSize = "m1.large";
                this.servers = numberOfUsers / 600;
                if (numberOfUsers % 600 > 0) {
                    this.servers++;
                }
            }
            this.userPerEngine = numberOfUsers / this.servers;
        }
        if (this.servers > 1 & this.servers <= 14) {
            this.engines = this.servers - this.consoles;
        } else if (this.servers > 14) {
            this.consoles = 2;
            this.engines = this.servers - this.consoles;
        } else {
            this.engines = 0;
        }
    }
}
