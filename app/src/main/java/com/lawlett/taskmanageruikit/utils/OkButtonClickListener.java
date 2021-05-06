package com.lawlett.taskmanageruikit.utils;

import com.lawlett.taskmanageruikit.finance.model.SpendingModel;

public interface OkButtonClickListener {
    void onClick(String amount);
    void onClick(SpendingModel spendingModel);
}
