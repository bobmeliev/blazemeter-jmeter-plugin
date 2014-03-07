package com.blazemeter.jmeter.entities;

import com.blazemeter.jmeter.constants.Constants;

/**
 * Created by dzmitrykashlach on 2/12/14.
 */
public class EnginesParameters {
    private static EnginesParameters enginesParameters = null;

    private int numberOfUsers = 0;
    private int consoles = 1;
    private int engines = 0;
    private int servers = 0;
    private StringBuilder engineSize = new StringBuilder(Constants.LARGE_ENGINE);
    private int userPerEngine = 0;


    public static synchronized EnginesParameters getEnginesParameters(Users users, int numberOfUsers) {
        if (enginesParameters == null) {
            enginesParameters = new EnginesParameters();
        }
        if (numberOfUsers != enginesParameters.numberOfUsers) {
            enginesParameters.numberOfUsers = numberOfUsers;
            enginesParameters.countParameters(users, numberOfUsers);
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

    private synchronized void countParameters(Users users, int numberOfUsers) {

        Plan plan = users.getPlan();
        int thrPerEngine = plan.getThreadsPerEngine();

        this.servers = numberOfUsers / thrPerEngine;
        this.engineSize.setLength(0);
        this.engineSize.append(Constants.LARGE_ENGINE);


        if (numberOfUsers / thrPerEngine > 0) {
            this.servers = numberOfUsers / thrPerEngine;
            if (this.servers / 15 > 0) {
                this.consoles = this.servers / 15;
                if (this.servers % 15 > 0) {
                    this.consoles++;
                }
            }
        }
        if (numberOfUsers % thrPerEngine > 0 | numberOfUsers == 0) {
            this.servers++;
        }

        this.engines = this.servers - this.consoles;
        this.userPerEngine = this.servers == 0 ? this.userPerEngine : numberOfUsers / this.servers;
    }
}
