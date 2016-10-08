package nus.me.loadview;

import android.content.Context;

/**
 * Created by nus on 16-10-8.
 */

public class Dp2Px {
    public static int Dp2Px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
