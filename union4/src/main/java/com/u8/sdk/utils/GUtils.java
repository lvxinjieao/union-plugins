package com.u8.sdk.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

public class GUtils {

    private static final String marshmallowMacAddress = "02:00:00:00:00:00";
    private static final String fileAddressMac = "/sys/class/net/wlan0/address";

    protected static String uuid;

    public static String getDeviceID(Context context) {
        if (uuid == null) {
            generateDeviceID(context);
        }
        return uuid.replace("-", "");
    }

    @SuppressLint({"NewApi", "MissingPermission"})
    public static void generateDeviceID(Context context) {

        if (uuid == null) {
            synchronized (GUtils.class) {
                if (uuid == null) {
                    final SharedPreferences prefs = context.getSharedPreferences("g_device_id.xml", 0);
                    final String id = prefs.getString("device_id", null);

                    if (id != null) {
                        // Use the ids previously computed and stored in the prefs file
                        uuid = id;
                    } else {
                        final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

                        // Use the Android ID unless it's broken, in which case fallback on deviceId,
                        // unless it's not available, then fallback on a random number which we store
                        // to a prefs file
                        try {
                            if (!"9774d56d682e549c".equals(androidId)) {
                                uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8")).toString();
                            } else {
                                String deviceId = null;
                                try {
                                    deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")).toString() : UUID.randomUUID().toString();
                            }
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }

                        // Write the value out to the prefs file
                        prefs.edit().putString("device_id", uuid.toString()).commit();

                    }
                }
            }
        }

    }


    //获取mac地址
    @SuppressLint({"MissingPermission"})
    public static String getMacAddress(Context context) {

        try {
            WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInf = wifiMan.getConnectionInfo();

            if (wifiInf != null && marshmallowMacAddress.equals(wifiInf.getMacAddress())) {
                String result = null;
                try {
                    result = getAdressMacByInterface();
                    if (result != null) {
                        return result;
                    } else {
                        result = getAddressMacByFile(wifiMan);
                        return result;
                    }
                } catch (IOException e) {
                    Log.e("MobileAccess", "Erreur lecture propriete Adresse MAC");
                } catch (Exception e) {
                    Log.e("MobileAcces", "Erreur lecture propriete Adresse MAC ");
                }
            } else {
                if (wifiInf != null && wifiInf.getMacAddress() != null) {
                    return wifiInf.getMacAddress();
                } else {
                    return "";
                }
            }
            return marshmallowMacAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "null";

    }

    @SuppressLint("NewApi")
    private static String getAdressMacByInterface() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (nif.getName().equalsIgnoreCase("wlan0")) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }

                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:", b));
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }

        } catch (Exception e) {
            Log.e("MobileAcces", "Erreur lecture propriete Adresse MAC ");
        }
        return null;
    }

    @SuppressLint({"MissingPermission"})
    private static String getAddressMacByFile(WifiManager wifiMan) throws Exception {
        String ret;
        int wifiState = wifiMan.getWifiState();

        wifiMan.setWifiEnabled(true);
        File fl = new File(fileAddressMac);
        FileInputStream fin = new FileInputStream(fl);
        ret = crunchifyGetStringFromStream(fin);
        fin.close();

        boolean enabled = WifiManager.WIFI_STATE_ENABLED == wifiState;
        wifiMan.setWifiEnabled(enabled);
        return ret;
    }

    private static String crunchifyGetStringFromStream(InputStream crunchifyStream) throws IOException {
        if (crunchifyStream != null) {
            Writer crunchifyWriter = new StringWriter();

            char[] crunchifyBuffer = new char[2048];
            try {
                Reader crunchifyReader = new BufferedReader(new InputStreamReader(crunchifyStream, "UTF-8"));
                int counter;
                while ((counter = crunchifyReader.read(crunchifyBuffer)) != -1) {
                    crunchifyWriter.write(crunchifyBuffer, 0, counter);
                }
            } finally {
                crunchifyStream.close();
            }
            return crunchifyWriter.toString();
        } else {
            return "No Contents";
        }
    }


    /**
     * 获取屏幕分辨率
     *
     * @param activity
     * @return
     */
    public static String getScreenDpi(Activity activity) {

        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        return dm.widthPixels + "×" + dm.heightPixels;

    }

    public static String generateUrlSortedParamString(Map<String, String> params, String splitChar, boolean nullExcluded) {

        StringBuffer content = new StringBuffer();
        // 按照key做排序
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key) == null ? "" : params.get(key).toString();
            if (nullExcluded && value.length() == 0) {
                continue;
            }
            content.append(key + "=" + value);
            if (!TextUtils.isEmpty(splitChar)) {
                content.append(splitChar);
            }
        }
        if (content.length() > 0 && !TextUtils.isEmpty(splitChar)) {
            content.deleteCharAt(content.length() - 1);
        }

        return content.toString();
    }

    public static void runInThread(Runnable run) {
        Thread t = new Thread(run);
        t.start();
    }

    @SuppressLint("NewApi")
    public static String[] getLackedPermissions(Activity context, String[] permissions) {

        if (permissions == null || permissions.length <= 0) return null;

        if (Build.VERSION.SDK_INT >= 23) {
            List<String> lacks = new ArrayList<String>();
            for (String p : permissions) {
                if (context.checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                    lacks.add(p);
                }
            }
            return lacks.toArray(new String[lacks.size()]);
        }
        return null;
    }

}
