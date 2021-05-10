package com.lawlett.taskmanageruikit.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class FinancePreference {
    private final SharedPreferences preferences;

    public FinancePreference(Context context) {
        preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public void setIncomeAmount(String amount) {
        preferences.edit().putString("INCOME_MODEL_KEY", amount).apply();
    }

    public String getIncomeAmount() {
        return preferences.getString("INCOME_MODEL_KEY", "0");
    }

    public void setSavingsAmount(String amount) {
        preferences.edit().putString("SAVINGS_MODEL_KEY", amount).apply();
    }

    public String getSavingsAmount() {
        return preferences.getString("SAVINGS_MODEL_KEY", "0");
    }

    public void setSpendingAmount(String amount) {
        preferences.edit().putString("SPENDING_MODEL_KEY", amount).apply();
    }

    public String getSpendingAmount() {
        return preferences.getString("SPENDING_MODEL_KEY", "0");
    }
    public void setBalance(String amount){
        preferences.edit().putString("BALANCE_KEY",amount).apply();
    }
    public String getBalance(){
        return preferences.getString("BALANCE_KEY","0");
    }
}
