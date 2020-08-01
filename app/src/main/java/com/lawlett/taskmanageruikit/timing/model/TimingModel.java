package com.lawlett.taskmanageruikit.timing.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class TimingModel implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;

    public String timerTitle;
    public String timerMinutes;
    public String timerDay;

    public String stopwatchTitle;
    public String stopwatchMinutes;
    public String stopwatchDay;

    public TimingModel(String timerTitle, String timerMinutes, String timerDay, String stopwatchTitle, String stopwatchMinutes, String stopwatchDay) {
        this.timerTitle = timerTitle;
        this.timerMinutes = timerMinutes;
        this.timerDay = timerDay;
        this.stopwatchTitle = stopwatchTitle;
        this.stopwatchMinutes = stopwatchMinutes;
        this.stopwatchDay = stopwatchDay;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTimerTitle() {
        return timerTitle;
    }

    public void setTimerTitle(String timerTitle) {
        this.timerTitle = timerTitle;
    }

    public String getTimerMinutes() {
        return timerMinutes;
    }

    public void setTimerMinutes(String timerMinutes) {
        this.timerMinutes = timerMinutes;
    }

    public String getTimerDay() {
        return timerDay;
    }

    public void setTimerDay(String timerDay) {
        this.timerDay = timerDay;
    }

    public String getStopwatchTitle() {
        return stopwatchTitle;
    }

    public void setStopwatchTitle(String stopwatchTitle) {
        this.stopwatchTitle = stopwatchTitle;
    }

    public String getStopwatchMinutes() {
        return stopwatchMinutes;
    }

    public void setStopwatchMinutes(String stopwatchMinutes) {
        this.stopwatchMinutes = stopwatchMinutes;
    }

    public String getStopwatchDay() {
        return stopwatchDay;
    }

    public void setStopwatchDay(String stopwatchDay) {
        this.stopwatchDay = stopwatchDay;
    }
}
