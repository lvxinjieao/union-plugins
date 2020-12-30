package com.u8.sdk;

import com.sogou.game.common.callback.LoginListener;
import com.sogou.game.common.callback.OnExitCallback;
import com.sogou.game.common.callback.PayCallback;
import com.sogou.game.common.callback.SwitchUserListener;
import com.sogou.game.sdk.SogouGamePlatform;
import com.sogou.game.sdk.core.FloatMenu;
import com.sogou.game.user.UserInfo;

import org.json.JSONObject;

public class SouGouSDK {

    private static SouGouSDK instance;

    private FloatMenu mFloatMenu;

    private int gid;
    private String appKey;
    private String gameName;
    private int orientation = 1;

    private SouGouSDK() {

    }

    public static SouGouSDK getInstance() {
        if (instance == null) {
            instance = new SouGouSDK();
        }
        return instance;
    }

    private void parseSDKParams(SDKParams params) {
        this.gid = params.getInt("SG_GID");
        this.appKey = params.getString("SG_APPKEY");
        this.gameName = params.getString("SG_GAME_NAME");
        String t = params.getString("SG_ORIENTATION");
        if ("landscape".equalsIgnoreCase(t)) {
            orientation = 0;
        }
    }


    public void initSDK(SDKParams params) {

        try {
            parseSDKParams(params);

            U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

                @Override
                public void onPause() {
                    if (mFloatMenu != null)
                        mFloatMenu.hide();
                }

                @Override
                public void onResume() {
                    showFloat();
                }

                @Override
                public void onDestroy() {
                    if (mFloatMenu != null) {
                        mFloatMenu.release();
                    }
                }
            });

            SogouGamePlatform.getInstance().init(new SogouGamePlatform.Builder()
                    .context(U8SDK.getInstance().getApplication())
                    .appKey(this.appKey)
                    .appName(this.gameName)
                    .gid(this.gid)
                    .developMode(false)
                    .sdkOrientation(orientation)
                    .showLog(true)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String encodeLoginResult(UserInfo userInfo) {
        try {
            JSONObject json = new JSONObject();
            json.put("sessionKey", userInfo.getSessionKey());
            json.put("userID", userInfo.getUserId());
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showFloat() {
        if (mFloatMenu == null) return;
        mFloatMenu.show();
    }

    public void login() {
        SogouGamePlatform.getInstance().login(U8SDK.getInstance().getContext(), new LoginListener() {

            @Override
            public void loginSuccess(int code, UserInfo userInfo) {
                U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, encodeLoginResult(userInfo));
                U8SDK.getInstance().runOnMainThread(new Runnable() {

                    @Override
                    public void run() {
                        if (mFloatMenu == null)
                            mFloatMenu = SogouGamePlatform.getInstance().createFloatMenu(U8SDK.getInstance().getContext(), new SwitchUserListener() {

                                @Override
                                public void switchFail(int code, String msg) {
                                    U8SDK.getInstance().onSwitchAccount(U8Code.CODE_SWITCH_ACCOUNT_FAIL, "code:" + code + " msg:" + msg);
                                }

                                @Override
                                public void switchSuccess(int code, UserInfo userInfo) {
                                    U8SDK.getInstance().onSwitchAccount(U8Code.CODE_SWITCH_ACCOUNT_SUCCESS, encodeLoginResult(userInfo));
                                }
                            }, true);
                        showFloat();
                    }
                });
            }

            @Override
            public void loginFail(int code, String msg) {
                U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed. msg:" + msg);
            }
        });
    }

    public void switchAccount() {
        SogouGamePlatform.getInstance().switchUser(U8SDK.getInstance().getContext(), new SwitchUserListener() {

            @Override
            public void switchFail(int code, String msg) {
                U8SDK.getInstance().onSwitchAccount(U8Code.CODE_SWITCH_ACCOUNT_FAIL, "switchFail code:" + code + " msg:" + msg);
            }

            @Override
            public void switchSuccess(int code, UserInfo userInfo) {
                U8SDK.getInstance().onSwitchAccount(U8Code.CODE_SWITCH_ACCOUNT_SUCCESS, encodeLoginResult(userInfo));
            }
        });
    }

    public void exitSDK() {
        SogouGamePlatform.getInstance().exit(U8SDK.getInstance().getContext(), new OnExitCallback() {
            @Override
            public void onCompleted() {// 游戏方执行退出游戏操作
                U8SDK.getInstance().getContext().finish();
                System.exit(0);
            }
        });
    }

    public void pay(PayParams params) {

        SogouGamePlatform.getInstance().pay(U8SDK.getInstance().getContext(), params.getProductName(), params.getPrice(), params.getOrderID(), false, new PayCallback() {

            // 支付成功回调,游戏方可以做后续逻辑处理
            // 收到该回调说明提交订单成功，但成功与否要以服务器回调通知为准
            @Override
            public void paySuccess(String orderId, String appData) {   // orderId是订单号，appData是游戏方自己传的透传消息
                U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "pay success");
            }

            @Override
            public void payFail(int code, String orderId, String appData) {
                // 支付失败情况下,orderId可能为空
//                if (orderId != null) {
//                    Log.e("U8SDK", "payFail code:" + code + "orderId:" + orderId + " appData:" + appData);
//                } else {
//                    Log.e("U8SDK", "payFail code:" + code + " appData:" + appData);
//                }
                U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed");
            }
        });
    }

    public void logout() {
        SogouGamePlatform.getInstance().logout(U8SDK.getInstance().getContext());
    }
}
