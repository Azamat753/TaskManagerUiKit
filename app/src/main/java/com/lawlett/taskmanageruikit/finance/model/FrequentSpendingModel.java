package com.lawlett.taskmanageruikit.finance.model;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class FrequentSpendingModel {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private final String name;
    private final int image;
    private String amount;

    public FrequentSpendingModel(String name, int image, String amount) {
        this.name = name;
        this.image = image;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public int getImage() {
        return image;
    }

    public String getAmount() {
        return amount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

}
