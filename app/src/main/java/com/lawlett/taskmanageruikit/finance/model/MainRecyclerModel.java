package com.lawlett.taskmanageruikit.finance.model;

import java.io.Serializable;
public class MainRecyclerModel implements Serializable {
    private final String title;
    private final int image;

    public MainRecyclerModel(String title, int image) {
        this.title = title;
        this.image = image;
    }
    public String getTitle() {
        return title;
    }

    public int getImage() {
        return image;
    }
}
