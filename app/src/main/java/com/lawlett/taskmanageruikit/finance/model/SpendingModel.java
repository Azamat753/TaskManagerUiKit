package com.lawlett.taskmanageruikit.finance.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SpendingModel  {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String amount;
    private String description;
    private long date;

    public SpendingModel(String amount, String description,long date) {
        this.date = date;
        this.amount = amount;
        this.description = description;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDate() {
        return date;
    }
}
