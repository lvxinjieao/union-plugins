package com.u8.sdk;


import android.app.Application;
import android.util.Log;

import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.ApiCallback;
import com.nearme.game.sdk.callback.GameExitCallback;
import com.nearme.game.sdk.common.model.biz.PayInfo;
import com.nearme.game.sdk.common.model.biz.ReportUserGameInfoParam;

import org.json.JSONObject;


public class OppoSDK {

    private static OppoSDK instance;

    public String appSecret;
    public String appKey;

    private OppoSDK() {

    }

    public static OppoSDK getInstance() {
        if (instance == null) {
            instance = new OppoSDK();
        }
        return instance;
    }


    public void initSDK(SDKParams params, Application context) {
        this.parseSDKParams(params);
        this.initSDK(context);
    }

    private void parseSDKParams(SDKParams params) {
        this.appSecret = params.getString("app_secret");
        this.appKey = params.getString("app_key");
    }

    public void initSDK(Application context) {
        try {
            Log.d("U8SDK", "init oppo when application created");
            GameCenterSDK.init(appSecret, context);
            Log.d("U8SDK", "init oppo completed on application created");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onActivityCreate() {
        Log.d("U8SDK", "init success");
        U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_SUCCESS, "init success");
    }


    public void switchAccount() {
        login();
    }


    public void login() {
        try {
            GameCenterSDK.getInstance().doLogin(U8SDK.getInstance().getContext(), new ApiCallback() {

                @Override
                public void onSuccess(String content) {

                    GameCenterSDK.getInstance().doGetTokenAndSsoid(new ApiCallback() {
                        @Override
                        public void onSuccess(String result) {
                            try {
                                Log.d("U8SDK", "get token success:" + result);
                                JSONObject json = new JSONObject(result);
                                String token = json.getString("token");
                                String ssoid = json.getString("ssoid");
                                String loginResult = encodeLoginResult(token, ssoid);
                                U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, loginResult);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(String content, int code) {
                            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, content);
                        }
                    });
                }

                @Override
                public void onFailure(String content, int code) {
                    U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, content);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed. exception:" + e.getMessage());
        }
    }

    public String encodeLoginResult(String token, String ssoid) {
        JSONObject json = new JSONObject();
        try {
            json.put("token", token);
            json.put("ssoid", ssoid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }


    public void logout() {
        U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "登出");
    }

    public void sendExtraData(UserExtraData data) {
        ReportUserGameInfoParam user = new ReportUserGameInfoParam(data.getRoleID(), data.getRoleName(), Integer.valueOf(data.getRoleLevel()), String.valueOf(data.getServerID()), data.getServerName(), "chapter", null);

        GameCenterSDK.getInstance().doReportUserGameInfoData(user, new ApiCallback() {

            @Override
            public void onSuccess(String arg0) {
                Log.d("U8SDK", "sendExtraData success");
            }

            @Override
            public void onFailure(String arg0, int arg1) {
                Log.d("U8SDK", "sendExtraData failed." + arg0);
            }
        });
    }


    public void sdkExit() {
        GameCenterSDK.getInstance().onExit(U8SDK.getInstance().getContext(), new GameExitCallback() {

            @Override
            public void exitGame() {
                U8SDK.getInstance().getContext().finish();
                System.exit(0);
            }
        });
    }

    public void pay(PayParams params) {
        String notifyUrl = params.getExtension();
        Log.d("U8SDK", "The extension is" + params.getExtension());
        PayInfo pay = new PayInfo(params.getOrderID(), "", params.getPrice() * 100);
        pay.setProductName(params.getProductName());
        pay.setProductDesc(params.getProductDesc());
        pay.setCallbackUrl(notifyUrl);

        GameCenterSDK.getInstance().doPay(U8SDK.getInstance().getContext(), pay, new ApiCallback() {

            @Override
            public void onSuccess(String content) {
                Log.d("U8SDK", "pay success");
                U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "pay success");
            }

            @Override
            public void onFailure(String content, int code) {
                Log.d("U8SDK", "pay failed.content:" + content);
                U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, content);
            }
        });
    }


}
