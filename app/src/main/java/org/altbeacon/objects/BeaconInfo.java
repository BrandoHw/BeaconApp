package org.altbeacon.objects;

import org.altbeacon.InternalMap.CategoryPoint;
import org.altbeacon.InternalMap.PinView;

public class BeaconInfo {
    private static final String TAG = BeaconInfo.class.getSimpleName();

    public String namespaceId;
    public String instanceId;
    public String location;
    public Boolean pinStatus;
    public CategoryPoint pinInfo;
    public long telemetryVersion;
    public long batteryMilliVolts;
    public long pduCount;
    public long upTime;
    public int userCount;

    public BeaconInfo(
            String namespaceId, String instanceId, String location) {
        this.namespaceId = namespaceId;
        this.instanceId = instanceId;
        this.location = location;
    }

    public void setPins(Boolean pinStatus, CategoryPoint pinInfo){
        this.pinStatus = pinStatus;
        this.pinInfo = pinInfo;
    }

    public void setTLM(long telemetryVersion, long batteryMilliVolts, long pduCount, long upTime, int userCount){
        this.telemetryVersion = telemetryVersion;
        this.batteryMilliVolts = batteryMilliVolts;
        this.pduCount = pduCount;
        this.upTime = upTime;
        this.userCount = userCount;
    }


}
