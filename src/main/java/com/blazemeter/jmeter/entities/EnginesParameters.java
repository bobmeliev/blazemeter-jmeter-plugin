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

    public int getNumberOfUsers() {
        return numberOfUsers;
    }

    private synchronized void countParameters(Users users, int numberOfUsers) {

        Plan plan = users.getPlan();
        int thrPerEngine = plan.getThreadsPerEngine();

        this.servers = count_servers(this.numberOfUsers, thrPerEngine);
        this.engineSize.setLength(0);
        this.engineSize.append(Constants.LARGE_ENGINE);
        this.numberOfUsers = re_count_users(numberOfUsers, this.servers);
        this.consoles = count_consoles(this.servers);
        this.engines = this.servers > 0 ? this.servers - this.consoles : 0;
        this.userPerEngine = this.servers > 0 ? this.numberOfUsers / this.servers : this.numberOfUsers;

    }

    private synchronized int count_servers(int numberOfUsers, int thrPerEngine) {
        int servers = numberOfUsers / thrPerEngine;
        if (numberOfUsers % thrPerEngine > 0) {
            servers++;
        }
        return servers;
    }

    private synchronized int re_count_users(int numberOfUsers, int servers) {
        if ((numberOfUsers > 1 & servers > 0) && numberOfUsers % servers != 0) {
            int usersPerEngine = numberOfUsers / servers;
            usersPerEngine++;
            return usersPerEngine * servers;

        } else {
            return numberOfUsers;
        }
    }

    private synchronized int count_consoles(int servers) {
        int consoles = 1;
        if (servers > 15) {
            consoles = servers / 15;
            if (servers % 15 > 0) {
                consoles++;
            }
            return consoles;
        } else {
            return consoles;
        }
    }
}
