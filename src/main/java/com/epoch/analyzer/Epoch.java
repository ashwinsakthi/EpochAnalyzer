package com.epoch.analyzer;

import java.util.Date;

public class Epoch {
    private int mActivityId;
    private Date mTimeStamp;

    public Epoch(int activityId, Date timestamp){
        mActivityId = activityId;
        mTimeStamp = timestamp;
    }

    public int getActivityId() {
        return mActivityId;
    }

    public void setActivityId(int activityId) {
        mActivityId = activityId;
    }

    public Date getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        mTimeStamp = timeStamp;
    }
}
