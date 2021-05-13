package com.lawlett.taskmanageruikit.tasksPage.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class HabitModel implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private int image;
    private String allDays;
    private int currentDay;
    private int myDay;

    public HabitModel(String title, int image, String allDays, int currentDay, int myDay) {
        this.title = title;
        this.image = image;
        this.allDays = allDays;
        this.currentDay = currentDay;
        this.myDay = myDay;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getAllDays() {
        return allDays;
    }

    public void setAllDays(String allDays) {
        this.allDays = allDays;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(int currentDay) {
        this.currentDay = currentDay;
    }

    public int getMyDay() {
        return myDay;
    }

    public void setMyDay(int myDay) {
        this.myDay = myDay;
    }

    @Override
    public String toString() {
        return "HabitModel{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", image=" + image +
                ", allDays='" + allDays + '\'' +
                ", currentDay=" + currentDay +
                ", myDay=" + myDay +
                '}';
    }
}
