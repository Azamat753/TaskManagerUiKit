package com.lawlett.taskmanageruikit.quick.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class QuickModel implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String description;
    private String image;
    private int color;
    private String createData;
    private String personalTask;

    public QuickModel(String title, String description, String createData, String image, int color,String personalTask) {
        this.title = title;
        this.description = description;
        this.createData = createData;
        this.image = image;
        this.color = color;
        this.personalTask=personalTask;
    }

    public QuickModel() {
    }

    public long getId() {
        return id;
    }

    public String getPersonalTask() {
        return personalTask;
    }

    public void setPersonalTask(String personalTask) {
        this.personalTask = personalTask;
    }

    public String getCreateData() {
        return createData;
    }

    public void setCreateData(String createData) {
        this.createData = createData;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
