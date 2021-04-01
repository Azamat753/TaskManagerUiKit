package com.lawlett.taskmanageruikit.help.recycler;

public class HelpModel {
    String title;
    String description;
    int video;

    public HelpModel(String title, String description, int video) {
        this.title = title;
        this.description = description;
        this.video = video;
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

    public int getVideo() {
        return video;
    }

    public void setVideo(int video) {
        this.video = video;
    }
}
