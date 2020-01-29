package org.altbeacon.InternalMap;


import org.altbeacon.network.LocationTimeStamp;

/**
 * Employee information to be displayed in a list for each beacon
 */
public class EmployeeCard {
    private static final String TAG = "EmployeeCard";

    private final String name;
    private final String job;
    private final String duration;


    public EmployeeCard(
            String name, String job, String duration) {
        this.name = duration;
        this.job = job;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public String getDuration() {
        return duration;
    }

    public String getJob() {
        return job;
    }
}
