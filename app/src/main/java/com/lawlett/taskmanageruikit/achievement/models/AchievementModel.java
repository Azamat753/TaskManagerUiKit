package com.lawlett.taskmanageruikit.achievement.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "achievement")
public class AchievementModel {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private final Date date;
    private final int achievementAmount;
    private final Category category;

    public AchievementModel(Date date, int achievementAmount, Category category) {
        this.date = date;
        this.achievementAmount = achievementAmount;
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public int getAchievementAmount() {
        return achievementAmount;
    }

    public Category getCategory() {
        return category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public enum Category {
        PERSONAL,
        WORK,
        MEET,
        HOME,
        PRIVATE,
        DONE
    }
}
