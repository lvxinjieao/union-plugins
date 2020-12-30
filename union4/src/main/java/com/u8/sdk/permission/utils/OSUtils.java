package com.u8.sdk.permission.utils;

import java.io.IOException;
import java.lang.reflect.Method;

import android.os.Build;
import android.util.Log;

public class OSUtils {

    private static final String KEY_EMUI_VERSION_CODE = "ro.build.version.emui";
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    private static boolean isPropertiesExist(String... keys) {
        try {
            BuildProperties prop = BuildProperties.newInstance();
            for (String key : keys) {
                String str = prop.getProperty(key);
                if (str == null)
                    return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void printOSInfo(){

        Log.d("U8SDK", "OS INFO BEGIN");
        Log.d("U8SDK", "Build.DISPLAY="+Build.DISPLAY);
        Log.d("U8SDK", "Build.MANUFACTURER="+Build.MANUFACTURER);
        try {
            BuildProperties prop = BuildProperties.newInstance();
            for (Object key : prop.keySet()) {
                String str = prop.getProperty(key.toString());
                if (str != null)
                    Log.d("U8SDK", key + "=" + str);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("U8SDK", "OS INFO END");

    }


    //当前设备是否需要权限申请， 比如小米等，虽然target设置为22，但是依然需要申请权限
    public static boolean isOSNeedPermission(){

        return isMIUI();
    }

    public static boolean isEMUI() {
        return isPropertiesExist(KEY_EMUI_VERSION_CODE);
    }

    public static boolean isMIUI() {
        return isPropertiesExist(KEY_MIUI_VERSION_CODE, KEY_MIUI_VERSION_NAME, KEY_MIUI_INTERNAL_STORAGE);
    }

    public static boolean isFlyme() {
        try {
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }

    public static boolean isOppoOS(){

        return Build.MANUFACTURER.equalsIgnoreCase("oppo");
    }

    public static boolean isVivoOS(){

        return Build.MANUFACTURER.equalsIgnoreCase("vivo");
    }

    
}
