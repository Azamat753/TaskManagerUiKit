package com.lawlett.taskmanageruikit.finance;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.utils.App;

public class AdviceText {
    private static final String[][] advice = {
            {App.getAppResources().getString((R.string.advice1_1)), App.getAppResources().getString((R.string.advice1_2))},
            {App.getAppResources().getString((R.string.advice2)), " "},
            {App.getAppResources().getString((R.string.advice3_1)), App.getAppResources().getString((R.string.advice3_2))},
            {App.getAppResources().getString((R.string.advice4_1)), App.getAppResources().getString((R.string.advice4_2))},
            {App.getAppResources().getString((R.string.advice5_1)), App.getAppResources().getString((R.string.advice5_2))},
            {App.getAppResources().getString((R.string.advice6_1)), App.getAppResources().getString((R.string.advice6_2))},
            {App.getAppResources().getString(R.string.advice7_1), App.getAppResources().getString((R.string.advice7_2))},
            {App.getAppResources().getString(R.string.advice8_1), App.getAppResources().getString(R.string.advice8_2)},
            {App.getAppResources().getString(R.string.advice9), " "},
            {App.getAppResources().getString(R.string.advice10_1), App.getAppResources().getString(R.string.advice10_2)},
            {App.getAppResources().getString((R.string.advice11_1)), App.getAppResources().getString(R.string.advice11_2)},
            {App.getAppResources().getString(R.string.advice12_1), App.getAppResources().getString(R.string.advice12_2)},
            {App.getAppResources().getString((R.string.advice13_1)), App.getAppResources().getString(R.string.advice13_2)},
            {App.getAppResources().getString(R.string.advice14_1), App.getAppResources().getString(R.string.advice14_2)},
            {App.getAppResources().getString(R.string.advice15), " "},
            {App.getAppResources().getString(R.string.advice16_1), App.getAppResources().getString(R.string.advice16_2)},
            {App.getAppResources().getString(R.string.advice17_1), String.valueOf(R.string.advice17_2)},
            {App.getAppResources().getString(R.string.advice18_1), App.getAppResources().getString(R.string.advice18_2)},
            {App.getAppResources().getString(R.string.advice19_1), App.getAppResources().getString(R.string.advice19_2)},
            {App.getAppResources().getString(R.string.advice20_1), App.getAppResources().getString(R.string.advice20_2)},
            {App.getAppResources().getString(R.string.advice21_1), App.getAppResources().getString(R.string.advice21_2)},
            {App.getAppResources().getString(R.string.advice22), " "},
            {App.getAppResources().getString(R.string.advice23), " "},
            {App.getAppResources().getString(R.string.advice24_1), App.getAppResources().getString(R.string.advice24_2)}};

    public static String getTitleAdvice(int position) {
        return advice[position][0];
    }

    public static String getDescAdvice(int position) {
        return advice[position][1];
    }
}
