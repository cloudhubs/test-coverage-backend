package com.groupfour.testcoveragetool.controller;


import java.util.Date;

public class TimeBounds {
    private Date startTime;
    private Date endTime;

    public TimeBounds(Date startTime, Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
}
