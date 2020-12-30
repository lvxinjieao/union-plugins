package com.u8.sdk.impl.services;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.u8.sdk.PayParams;
import com.u8.sdk.U8SDK;
import com.u8.sdk.UserExtraData;
import com.u8.sdk.impl.common.Consts;
import com.u8.sdk.impl.listeners.ISDKListener;
import com.u8.sdk.impl.listeners.ISDKLoginListener;
import com.u8.sdk.impl.listeners.ISDKPayListener;
import com.u8.sdk.impl.listeners.ISDKRegisterOnekeyListener;
import com.u8.sdk.utils.EncryptUtils;
import com.u8.sdk.utils.GUtils;
import com.u8.sdk.utils.ResourceHelper;
import com.u8.sdk.utils.U8HttpUtils;


public class SdkManager {

    private static SdkManager instance;

    private String lastUserID;

    public static SdkManager getInstance() {
        if (instance == null) {
            instance = new SdkManager();
        }
        return instance;
    }

    //SDK初始化
    public void init(Context context, final ISDKListener listener) {
        listener.onSuccess();
    }

    //提交游戏角色数据
    public void submitGameData(Activity context, UserExtraData data, final ISDKListener listener) {
        Log.d("U8SDK", "submitGameData called. the data is:");
        Log.d("U8SDK", data.toString());
        listener.onSuccess();
    }


    public void login(String username, String password, final ISDKLoginListener listener) {

        Log.d("U8SDK", "sdk login called. username:" + username + ";password:" + password);

        String curTime = System.currentTimeMillis() + "";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("name", username);
        params.put("password", password);
        params.put("gameAppId", U8SDK.getInstance().getAppID() + "");
        params.put("time", curTime);
        params.put("sign", EncryptUtils.md5Sign(params, U8SDK.getInstance().getAppKey()));

        String loginUrl = U8SDK.getInstance().getU8ServerURL() + Consts.IMPL_USER_LOGIN;
        final String url = loginUrl;

        GUtils.runInThread(new Runnable() {
            @Override
            public void run() {

                String content = U8HttpUtils.httpPost(url, params);

                Log.d("U8SDK", "default login result:" + content);

                try {

                    JSONObject json = new JSONObject(content);
                    int code = json.getInt("state");
                    if (code == Consts.CODE_SUCCESS) {
                        JSONObject jData = json.getJSONObject("data");
                        String userId = jData.optString("userId");
                        String username = jData.optString("username");
                        lastUserID = userId;
                        listener.onSuccess(userId, username);

                    } else {
                        listener.onFailed(Consts.CODE_FAILED);
                    }

                } catch (Exception e) {
                    listener.onFailed(Consts.CODE_FAILED);
                    e.printStackTrace();

                }

            }
        });
    }


    public void register(String name, String password, final ISDKLoginListener listener) {

        String curTime = System.currentTimeMillis() + "";

        final Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        params.put("password", password);
        params.put("gameAppId", U8SDK.getInstance().getAppID() + "");
        params.put("time", curTime);
        params.put("sign", EncryptUtils.md5Sign(params, U8SDK.getInstance().getAppKey()));

        String regUrl = U8SDK.getInstance().getU8ServerURL() + Consts.IMPL_USER_REG;
        final String url = regUrl;

        GUtils.runInThread(new Runnable() {
            @Override
            public void run() {

                String content = U8HttpUtils.httpPost(url, params);

                Log.d("U8SDK", "default register result:" + content);

                try {

                    JSONObject json = new JSONObject(content);
                    int code = json.getInt("state");
                    if (code == Consts.CODE_SUCCESS) {
                        JSONObject jData = json.getJSONObject("data");
                        String userId = jData.optString("userId");
                        String username = jData.optString("username");
                        lastUserID = userId;
                        listener.onSuccess(userId, username);

                    } else {
                        listener.onFailed(Consts.CODE_FAILED);
                    }

                } catch (Exception e) {
                    listener.onFailed(Consts.CODE_FAILED);
                    e.printStackTrace();

                }

            }
        });
    }

    public void registerOnekey(String extra, final ISDKRegisterOnekeyListener listener) {

        String curTime = System.currentTimeMillis() + "";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("gameAppId", U8SDK.getInstance().getAppID() + "");
        params.put("time", curTime);
        params.put("sign", EncryptUtils.md5Sign(params, U8SDK.getInstance().getAppKey()));

        String regUrl = U8SDK.getInstance().getU8ServerURL() + Consts.IMPL_USER_REG_FAST;
        final String url = regUrl;

        GUtils.runInThread(new Runnable() {
            @Override
            public void run() {

                String content = U8HttpUtils.httpPost(url, params);

                Log.d("U8SDK", "default fast register result:" + content);

                try {

                    JSONObject json = new JSONObject(content);
                    int code = json.getInt("state");
                    if (code == Consts.CODE_SUCCESS) {
                        JSONObject jData = json.getJSONObject("data");
                        String userId = jData.optString("userId");
                        String username = jData.optString("username");
                        String password = jData.optString("password");
                        lastUserID = userId;
                        listener.onSuccess(userId, username, password);

                    } else {
                        listener.onFailed(Consts.CODE_FAILED);
                    }

                } catch (Exception e) {
                    listener.onFailed(Consts.CODE_FAILED);
                    e.printStackTrace();

                }

            }
        });
    }

    public void pay(Activity context, PayParams params, final ISDKPayListener listener) {

        if (TextUtils.isEmpty(lastUserID)) {
            Log.d("U8SDK", "sdk now not logined. please login first.");
            ResourceHelper.showTip(context, "R.string.x_pay_no_login");
            return;
        }

        final Map<String, String> data = new LinkedHashMap<String, String>();
        data.put("uid", lastUserID);
        data.put("price", params.getPrice() + "");
        data.put("gameAppId", U8SDK.getInstance().getAppID() + "");
        data.put("productID", params.getProductId());
        data.put("productName", params.getProductName());
        data.put("orderID", params.getOrderID());
        data.put("time", System.currentTimeMillis() + "");
        data.put("payNotifyUrl", params.getExtension());
        data.put("sign", EncryptUtils.md5Sign(data, U8SDK.getInstance().getAppKey()));

        String payUrl = U8SDK.getInstance().getU8ServerURL() + Consts.IMPL_USER_PAY;
        final String url = payUrl;


        GUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                String content = U8HttpUtils.httpPost(url, data);
                Log.d("U8SDK", "default pay result:" + content);
                try {
                    JSONObject json = new JSONObject(content);
                    int code = json.getInt("state");
                    if (code == Consts.CODE_SUCCESS) {
                        listener.onSuccess("success");
                    } else {
                        listener.onFailed(Consts.CODE_FAILED);
                    }
                } catch (Exception e) {
                    listener.onFailed(Consts.CODE_FAILED);
                    e.printStackTrace();
                }
            }
        });
    }

}
