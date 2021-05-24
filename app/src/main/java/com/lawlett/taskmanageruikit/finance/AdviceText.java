package com.lawlett.taskmanageruikit.finance;

import android.content.Context;

import com.lawlett.taskmanageruikit.R;

public class AdviceText {
    public static String getAdvice(Context context, int firstPos, int secondPos) {
        final String[][] advice = {
                {context.getString((R.string.advice1_1)), context.getString((R.string.advice1_2))},
                {context.getString((R.string.advice2)), " "},
                {context.getString((R.string.advice3_1)), context.getString((R.string.advice3_2))},
                {context.getString((R.string.advice4_1)), context.getString((R.string.advice4_2))},
                {context.getString((R.string.advice5_1)), context.getString((R.string.advice5_2))},
                {context.getString((R.string.advice6_1)), context.getString((R.string.advice6_2))},
                {context.getString(R.string.advice7_1), context.getString((R.string.advice7_2))},
                {context.getString(R.string.advice8_1), context.getString(R.string.advice8_2)},
                {context.getString(R.string.advice9), " "},
                {context.getString(R.string.advice10_1), context.getString(R.string.advice10_2)},
                {context.getString((R.string.advice11_1)), context.getString(R.string.advice11_2)},
                {context.getString(R.string.advice12_1), context.getString(R.string.advice12_2)},
                {context.getString((R.string.advice13_1)), context.getString(R.string.advice13_2)},
                {context.getString(R.string.advice14_1), context.getString(R.string.advice14_2)},
                {context.getString(R.string.advice15), " "},
                {context.getString(R.string.advice16_1), context.getString(R.string.advice16_2)},
                {context.getString(R.string.advice17_1), context.getString(R.string.advice17_2)},
                {context.getString(R.string.advice18_1), context.getString(R.string.advice18_2)},
                {context.getString(R.string.advice19_1), context.getString(R.string.advice19_2)},
                {context.getString(R.string.advice20_1), context.getString(R.string.advice20_2)},
                {context.getString(R.string.advice21_1), context.getString(R.string.advice21_2)},
                {context.getString(R.string.advice22), " "},
                {context.getString(R.string.advice23), " "},
                {context.getString(R.string.advice24_1), context.getString(R.string.advice24_2), ""},
                {context.getString(R.string.advice25_1), ""},
                {context.getString(R.string.advice26_1), ""},
                {context.getString(R.string.advice27_1), context.getString(R.string.advice27_2)},
                {context.getString(R.string.advice28_1), ""},
                {context.getString(R.string.advice29_1), ""},
                {context.getString(R.string.advice30_1), ""},
                {context.getString(R.string.advice_31_1), ""},
                {context.getString(R.string.advice32_1), context.getString(R.string.advice_32_2)},
                {context.getString(R.string.advice_33_1), ""},
                {context.getString(R.string.advice_34_1), ""},
                {context.getString(R.string.advice35_1), context.getString(R.string.advice_35_2)},
                {context.getString(R.string.advice_36_1), context.getString(R.string.advice36_2)},
                {context.getString(R.string.advice37_1), ""},
                {context.getString(R.string.advice38_1), context.getString(R.string.advice38_2)},
                {context.getString(R.string.advice39_1), context.getString(R.string.advice39_2)},
                {context.getString(R.string.advice40_1), ""},
                {context.getString(R.string.advice41_1), context.getString(R.string.advice_41_2)},
                {context.getString(R.string.advice42_1), context.getString(R.string.advice42_2)},
                {context.getString(R.string.advice43_1), ""},
                {context.getString(R.string.advice44_1), context.getString(R.string.advice44_2)},
                {context.getString(R.string.advice45_1), ""},
                {context.getString(R.string.advice46_1), context.getString(R.string.advice46_2)},
                {context.getString(R.string.advice47_1), ""},
                {context.getString(R.string.advice48_1), context.getString(R.string.advice48_2)}};

        return advice[firstPos][secondPos];
    }

    public static String getTitleAdvice(int position, Context context) {
        return getAdvice(context, position, 0);
    }

    public static String getDescAdvice(int position, Context context) {
        return getAdvice(context, position, 1);
    }
}
