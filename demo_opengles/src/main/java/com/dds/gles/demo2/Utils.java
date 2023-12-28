package com.dds.gles.demo2;

import android.content.Context;
import android.content.res.Configuration;

public class Utils {
    /**
     * 判断是否为平板
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
