package com.u8.sdk.analytics;

import android.app.Activity;

import com.u8.sdk.U8SDK;
import com.u8.sdk.utils.EncryptUtils;
import com.u8.sdk.utils.GUtils;
import com.u8.sdk.utils.Logs;
import com.u8.sdk.utils.U8HttpUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UDManager {

    private static UDManager instance;

    private UDManager() {

    }

    public static UDManager getInstance() {
        if (instance == null) {
            instance = new UDManager();
        }
        return instance;
    }

    public void submitUserInfo(Activity activity, String url, String appKey, UUserLog log) {
        try {
            Logs.d("U8SDK", "begin submit user info to u8server:" + log.getOpType());
            Map<String, String> params = new HashMap<String, String>();
            params.put("userID", log.getUserID() + "");
            params.put("appID", log.getAppID() + "");
            params.put("channelID", log.getChannelID() + "");
            params.put("serverID", log.getServerID());
            params.put("serverName", log.getServerName());
            params.put("roleID", log.getRoleID());
            params.put("roleName", log.getRoleName());
            params.put("roleLevel", log.getRoleLevel());
            params.put("deviceID", log.getDeviceID());
            params.put("opType", log.getOpType() + "");

            StringBuilder sb = new StringBuilder();
            sb.append("appID=").append(log.getAppID())
                    .append("channelID=").append(log.getChannelID())
                    .append("deviceID=").append(log.getDeviceID())
                    .append("opType=").append(log.getOpType())
                    .append("roleID=").append(log.getRoleID())
                    .append("roleLevel=").append(log.getRoleLevel())
                    .append("roleName=").append(log.getRoleName())
                    .append("serverID=").append(log.getServerID())
                    .append("serverName=").append(log.getServerName())
                    .append("userID=").append(log.getUserID())
                    .append(appKey);

            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();

            params.put("sign", sign);

            url = url + "/addUserLog";

            String result = U8HttpUtils.httpGet(url, params);

            Logs.d("U8SDK", "The sign is " + sign + " The result is " + result);

            if (result != null && result.trim().length() > 0) {

                JSONObject jsonObj = new JSONObject(result);
                int state = jsonObj.getInt("state");

                if (state != 1) {
                    Logs.d("U8SDK", "submit user info failed. the state is " + state);
                } else {
                    Logs.d("U8SDK", "submit user info success");
                }
            }

        } catch (Exception e) {
            Logs.e("U8SDK", "submit user info failed.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void submitDeviceInfo(Activity activity, String url, String appKey, UDevice device) {
        try {
            Logs.d("U8SDK", "begin submit device info to u8server");
            Map<String, String> params = new HashMap<String, String>();
            params.put("appID", device.getAppID() + "");
            params.put("deviceID", device.getDeviceID());
            params.put("mac", device.getMac());
            params.put("deviceType", device.getDeviceType());
            params.put("deviceOS", device.getDeviceOS() + "");

            String dpi = device.getDeviceDpi();
            dpi = dpi.replace("×", "#");

            params.put("deviceDpi", dpi);
            params.put("channelID", device.getChannelID() + "");
            params.put("subChannelID", (device.getSubChannelID() == null ? "0" : device.getSubChannelID() + ""));        //PS:不加入签名

            StringBuilder sb = new StringBuilder();
            sb.append("appID=").append(device.getAppID() + "")
                    .append("channelID=").append(device.getChannelID())
                    .append("deviceDpi=").append(dpi)
                    .append("deviceID=").append(device.getDeviceID())
                    .append("deviceOS=").append(device.getDeviceOS())
                    .append("deviceType=").append(device.getDeviceType())
                    .append("mac=").append(device.getMac())
                    .append(appKey);

            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();

            params.put("sign", sign);

            url = url + "/addDevice";

            String result = U8HttpUtils.httpGet(url, params);

            Logs.d("U8SDK", "sign str:" + sb.toString());
            Logs.d("U8SDK", "The sign is " + sign + " The result is " + result);

            if (result != null && result.trim().length() > 0) {
                JSONObject jsonObj = new JSONObject(result);
                int state = jsonObj.getInt("state");
                if (state != 1) {
                    Logs.d("U8SDK", "submit device info failed. the state is " + state);
                } else {
                    Logs.d("U8SDK", "submit device info success");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logs.e("U8SDK", "submit device info failed.\n" + e.getMessage());
        }
    }

    /**
     * 手机用户设备信息
     *
     * @param activity
     * @param appID
     * @param channelID
     * @param subChannelID
     * @return
     */
    public UDevice collectDeviceInfo(Activity activity, Integer appID, Integer channelID, Integer subChannelID) {
        try {
            UDevice device = new UDevice();
            device.setAppID(appID);
            device.setChannelID(channelID);
            device.setSubChannelID(U8SDK.getInstance().getSubChannel());
            device.setDeviceID(GUtils.getDeviceID(activity));
            device.setMac(GUtils.getMacAddress(activity));
            device.setDeviceType(android.os.Build.MODEL);
            device.setDeviceOS(1);
            device.setDeviceDpi(GUtils.getScreenDpi(activity));
            return device;
        } catch (Exception e) {
            e.printStackTrace();
            Logs.e("U8SDK", e.getMessage());
        }
        return null;
    }
}
