package com.u8.sdk.platform;

import android.app.Activity;
import android.text.TextUtils;

import com.u8.sdk.PayParams;
import com.u8.sdk.U8Code;
import com.u8.sdk.U8Order;
import com.u8.sdk.U8SDK;
import com.u8.sdk.UserExtraData;
import com.u8.sdk.base.ISDKListener;
import com.u8.sdk.plugin.U8Pay;
import com.u8.sdk.plugin.U8User;
import com.u8.sdk.utils.Logs;
import com.u8.sdk.utils.ResourceHelper;
import com.u8.sdk.verify.URealNameInfo;
import com.u8.sdk.verify.UToken;

/**
 * 游戏层调用接口， 所有游戏层需要调用的接口，都从U8Platform中调用 所有接口和回调都已经在主线程中调用了，调用者无需考虑线程问题
 */
public class Platform {

    private static Platform instance;

    private boolean isSwitchAccount = false;

    private Platform() {

    }

    public static Platform getInstance() {
        if (instance == null) {
            instance = new Platform();
        }
        return instance;
    }

    /**
     * SDK初始化，需要在游戏启动Activity的onCreate中调用
     *
     * @param activity
     * @param callback
     */
    public void init(Activity activity, final InitListener callback) {
        if (callback == null) {
            Logs.d("U8SDK", "InitListener must be not null.");
            return;
        }

        try {
            U8SDK.getInstance().setSDKListener(new ISDKListener() {

                @Override
                public void onResult(final int code, final String result) {
                    Logs.d("U8SDK", "SDK onResult: code:" + code + "----------result:" + result);
                    callback.onResult(code, result);
                }

                @Override
                public void onInitResult(int code, String result) {
                    Logs.d("U8SDK", "SDK 初始化： code=" + code + "----------result=" + result);
                    callback.onInitResult(code, result);
                }

                @Override
                public void onLoginResult(int code, String result) {
                    Logs.d("U8SDK", "SDK 登录成功: code=" + code + "----------result=" + result);
                    isSwitchAccount = false;
                }

                @Override
                public void onSwitchAccount(int code, String result) {
                    Logs.d("U8SDK", "SDK 切换帐号并登录成功: code=" + code + "----------result=" + result);
                    isSwitchAccount = true;
                }

                @Override
                public void onAuthResult(final UToken authResult) {
                    U8SDK.getInstance().runOnMainThread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                if (isSwitchAccount) {
                                    if (authResult.isSuc()) {
                                        callback.onSwitchAccount(U8Code.CODE_SWITCH_ACCOUNT_SUCCESS, authResult);
                                    } else {
                                        Logs.d("U8SDK", "switch account auth failed.");
                                    }
                                } else {
                                    if (!authResult.isSuc()) {
                                        callback.onLoginResult(U8Code.CODE_LOGIN_FAIL, null);
                                        return;
                                    }
                                    callback.onLoginResult(U8Code.CODE_LOGIN_SUCCESS, authResult);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                public void onLogoutResult(int code, String result) {
                    Logs.d("U8SDK", "SDK 登出成功：" + "code=" + code + "----------result=" + result);
                    callback.onLogoutResult(code, result);
                }

                @Override
                public void onRealNameResult(URealNameInfo realNameInfo) {  // 实名认证结果，或者实名认证查询结果
                    callback.onRealNameResult(realNameInfo);
                }

                @Override
                public void onPayResult(int code, String result) {
                    Logs.d("U8SDK", "支付结果:" + "code=" + code + "----------result=" + result);
                    callback.onPayResult(code, result);
                }

                @Override
                public void onSinglePayResult(int code, U8Order order) {
                    Logs.d("U8SDK", "单机支付结果:" + "code=" + code + "----------result=" + order.toString());
                }

                @Override
                public void onExitResult(int code, String result) {
                    Logs.d("U8SDK", "退出游戏:" + "code=" + code + "----------result=" + result);
                    callback.onExitResult(code, result);
                }
            });
            U8SDK.getInstance().init(activity);
            U8SDK.getInstance().onCreate();
        } catch (Exception e) {
            callback.onInitResult(U8Code.CODE_INIT_FAIL, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 登录，登录成功或者失败，会触发初始化回调中的onLoginResult
     */
    public void login(Activity activity) {
        U8SDK.getInstance().setContext(activity);
        U8SDK.getInstance().runOnMainThread(new Runnable() {

            @Override
            public void run() {
                U8User.getInstance().login();
            }
        });
    }

    /**
     * 直接登录接口， 部分渠道登录是多种方式，支持直接登录某种登录的时候， 可以用该接口调用。 常见loginType比如google,facebook等。
     *
     * @param activity
     * @param loginType
     */
    public void login(Activity activity, final String loginType) {
        U8SDK.getInstance().setContext(activity);
        U8SDK.getInstance().runOnMainThread(new Runnable() {

            @Override
            public void run() {
                if (U8User.getInstance().isSupport("login")) {
                    U8User.getInstance().login(loginType);
                } else {
                    U8User.getInstance().login();
                }
            }
        });
    }

    /**
     * 登出(选接)，登出没有回调
     */
    public void logout() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {

            @Override
            public void run() {
                if (U8User.getInstance().isSupport("logout"))
                    U8User.getInstance().logout();
            }
        });
    }

    /**
     * 切换帐号(可选)，登录成功回调同登录
     */
    public void switchAccount() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {

            @Override
            public void run() {
                U8User.getInstance().switchLogin();
            }
        });
    }

    /**
     * 提交游戏中角色数据（必接）
     */
    public void submitExtraData(final UserExtraData data) {

        U8SDK.getInstance().runOnMainThread(new Runnable() {

            @Override
            public void run() {
                if (TextUtils.isEmpty(data.getRoleID()) || TextUtils.isEmpty(data.getRoleName()) || TextUtils.isEmpty(data.getServerName()) || TextUtils.isEmpty(data.getRoleLevel())) {
                    Logs.e("U8SDK", "roleID, roleName, roleLevel, serverID, serverName cannot be null");
                    ResourceHelper.showTipStr(U8SDK.getInstance().getContext(), "roleID, roleName, roleLevel, serverID, serverName等几个字段必传");
                }
                U8User.getInstance().submitExtraData(data);
            }
        });
    }

    /**
     * 退出游戏，弹出确认框（必接）
     */
    public void exit() {

        U8SDK.getInstance().runOnMainThread(new Runnable() {

            @Override
            public void run() {
                if (U8User.getInstance().isSupport("exit")) {
                    U8User.getInstance().exit();
                }else{

                }
            }
        });
    }

    /**
     * 查询实名认证信息
     *
     * @return
     */
    public boolean queryAntiAddiction() {
        if (U8User.getInstance().isSupport("queryAntiAddiction")) {
            U8SDK.getInstance().runOnMainThread(new Runnable() {

                @Override
                public void run() {
                    U8User.getInstance().queryAntiAddiction();
                }
            });
            return true;
        }
        return false;
    }

    /**
     * 调用渠道的实名注册界面
     *
     * @return
     */
    public boolean realNameRegister() {
        if (U8User.getInstance().isSupport("realNameRegister")) {
            U8SDK.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    U8User.getInstance().realNameRegister();
                }
            });
            return true;
        }
        return false;
    }

    /**
     * 支付，支付成功或者失败，会触发初始化回调中onPayResult
     *
     * @param activity
     */
    public void pay(Activity activity, final PayParams data) {
        U8SDK.getInstance().setContext(activity);
        U8SDK.getInstance().runOnMainThread(new Runnable() {

            @Override
            public void run() {
                U8Pay.getInstance().pay(data);
            }
        });
    }


}
