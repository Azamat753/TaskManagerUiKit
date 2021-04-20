package com.lawlett.taskmanageruikit.tasks;

import java.io.Serializable;

public class CategoryModel implements Serializable {
    String title;
    int image;

    public CategoryModel(String title, int image) {
        this.title = title;
        this.image = image;
    }
}
