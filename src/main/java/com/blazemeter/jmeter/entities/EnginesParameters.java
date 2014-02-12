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
    private StringBuilder engineSize = new StringBuilder("m1.medium");
    private int userPerEngine = 0;


    public static synchronized EnginesParameters getEnginesParameters(int numberOfUsers) {
        if (enginesParameters == null) {
            enginesParameters = new EnginesParameters();
        }
        if (numberOfUsers != enginesParameters.numberOfUsers) {
            enginesParameters.numberOfUsers = numberOfUsers;
            enginesParameters.countParameters(numberOfUsers);
        }

        return enginesParameters;
    }

    public String getEngineSize() {
        return engineSize.toString();
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

    private synchronized void countParameters(int numberOfUsers) {

        UserInfo userInfo = BmTestManager.getInstance().getUserInfo();


        if (numberOfUsers == 0) {
            this.userPerEngine = 0;
            this.engines = 0;
            this.consoles = 0;
            return;
        }

        if (numberOfUsers <= 300 & numberOfUsers > 0) {
            this.userPerEngine = numberOfUsers;
            this.engines = 0;
            this.consoles = 1;
            return;
        }


        if (numberOfUsers > 300) {
            this.servers = numberOfUsers / 300;
            if (this.servers <= userInfo.getMaxEnginesLimit()) {
                this.engineSize.setLength(0);
                this.engineSize.append("m1.medium");

                if (numberOfUsers / 300 > 0) {
                    this.servers = numberOfUsers / 300;
                    if (this.servers / 15 > 0) {
                        this.consoles = this.servers / 15;
                        this.engines = this.servers - this.consoles;

                    }
                    return;
                }
            }


            if (this.servers > userInfo.getMaxEnginesLimit()) {
                this.engineSize.setLength(0);
                this.engineSize.append("m1.large");
                this.servers = numberOfUsers / 600;
                if (numberOfUsers % 600 > 0) {
                    this.servers++;
                }
            }
            this.userPerEngine = numberOfUsers / this.servers;
        }

        if (this.servers > 1 & this.servers <= 14) {
            this.engines = this.servers - this.consoles;
        }
        if (this.servers > 14) {
            this.consoles = 2;
            this.engines = this.servers - this.consoles;
        }
    }
}
