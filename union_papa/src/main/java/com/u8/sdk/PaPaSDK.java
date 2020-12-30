package com.u8.sdk;

import org.json.JSONObject;

import com.papa91.pay.callback.PPChangeAccountCallBack;
import com.papa91.pay.callback.PPLoginCallBack;
import com.papa91.pay.callback.PPayCallback;
import com.papa91.pay.callback.PpaLogoutCallback;
import com.papa91.pay.pa.activity.PaayActivity;
import com.papa91.pay.pa.business.LoginResult;
import com.papa91.pay.pa.business.PPayCenter;
import com.papa91.pay.pa.business.PaayArg;
import com.papa91.pay.pa.dto.LogoutResult;
import com.u8.sdk.log.Log;

public class PaPaSDK {

    private static PaPaSDK instance;

    private String appName;

    private int openUid;
    private int payRatio;

    public static PaPaSDK getInstance() {
        if (instance == null) {
            instance = new PaPaSDK();
        }
        return instance;
    }

    public void initSDK(SDKParams params) {
        try {

            appName = params.getString("PAPA_APPNAME");
            payRatio = params.getInt("PATA_RATIO");

            U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

                @Override
                public void onDestroy() {
                    PPayCenter.destroy();
                }

                @Override
                public void onPause() {
                    PPayCenter.onPause(U8SDK.getInstance().getContext());
                }

                @Override
                public void onResume() {
                    PPayCenter.onResume(U8SDK.getInstance().getContext());
                }
            });

            PPayCenter.init(U8SDK.getInstance().getContext());

            PPayCenter.initChangAccountCallback(new PPChangeAccountCallBack() {
                @Override
                public void onChangeAccount() {
                    //这里可以去做切换账户退回游戏登录界面等操作
                    U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "这里可以去做切换账户退回游戏登录界面等操作");
                }
            });
            U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_SUCCESS, "init success");
        } catch (Exception e) {
            U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_FAIL, e.getMessage());
            e.printStackTrace();
        }
    }

    private void onLoginSuccess(int uid, String token) {
        try {
            JSONObject json = new JSONObject();
            json.put("uid", uid);
            json.put("token", token);
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void login() {
        try {
            PPayCenter.login(U8SDK.getInstance().getContext(), new PPLoginCallBack() {
                @Override
                public void onLoginFinish(LoginResult result) {
                    Log.d("U8SDK", "papa login result:" + result.getCode() + ";msg:" + result.getMessage());
                    switch (result.getCode()) {
                        case LoginResult.LOGIN_CODE_APPID_NOT_FOUND:
                            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "appid not found");
                            break;
                        case LoginResult.LOGIN_CODE_SUCCESS://登录成功
                            openUid = result.getOpenUid();//返回openUid
                            String token = result.getToken();
                            onLoginSuccess(openUid, token);
                            break;
                        case LoginResult.LOGIN_CODE_FAILED://登录失败
                            String message = result.getMessage();//失败详情
                            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, message);
                            break;
                        case LoginResult.LOGIN_CODE_CANCEL:// 登录取消
                            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "user cancel");
                            break;
                        case LoginResult.NOT_INIT://没有调用 PPayCenter.init(activity);
                            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed");
                            break;
                    }
                }
            });
        } catch (Exception e) {
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, e.getMessage());
            e.printStackTrace();
        }
    }

    public void switchLogin() {
        PPayCenter.changeAccount(new PPLoginCallBack() {
            @Override
            public void onLoginFinish(LoginResult result) {
                Log.d("U8SDK", "papa switch account result:" + result.getCode() + ";msg:" + result.getMessage());
                switch (result.getCode()) {
                    case LoginResult.LOGIN_CODE_APPID_NOT_FOUND:
                        U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "appid not found");
                        break;

                    case LoginResult.LOGIN_CODE_SUCCESS://登录成功
                        openUid = result.getOpenUid();//返回openUid
                        String token = result.getToken();
                        onLoginSuccess(openUid, token);
                        break;

                    case LoginResult.LOGIN_CODE_FAILED://登录失败
                        String message = result.getMessage();//失败详情
                        U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, message);
                        break;

                    case LoginResult.LOGIN_CODE_CANCEL:// 登录取消
                        break;

                    case LoginResult.NOT_INIT://没有调用 PPayCenter.init(activity);
                        U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed");
                        break;

                }
            }
        });
    }

    public void submitExtraData(UserExtraData data) {
        if (data.getDataType() == UserExtraData.TYPE_CREATE_ROLE) {
            PPayCenter.createRole(data.getRoleName(), data.getRoleID(), data.getServerID() + "");
        } else if (data.getDataType() == UserExtraData.TYPE_ENTER_GAME) {
            PPayCenter.enterGame(data.getRoleName(), data.getServerID() + "", data.getRoleID(), data.getPower());
        }
    }

    public void exitSDK() {
        PPayCenter.loginOut(U8SDK.getInstance().getContext(), openUid, new PpaLogoutCallback() {
            @Override
            public void onLoginOut(LogoutResult logoutResult) {
                switch (logoutResult.getCode()) {
                    case LogoutResult.LOGOUT_CODE_OUT:
                        U8SDK.getInstance().getContext().finish();
                        System.exit(0);
                        break;
                    case LogoutResult.LOGOUT_CODE_BBS:
                        break;
                }
                Log.d("U8SDK", "是否是退出 " + logoutResult.getCode() + "  loggetMessage=" + logoutResult.getMessage());
            }
        });
    }

    public void enterUserCenter() {
        PPayCenter.userCenter(U8SDK.getInstance().getContext());
    }

    public void queryRealName() {
        int result = PPayCenter.getAccountRealName(openUid);
        if (result == 1) {
            //已经实名
            U8SDK.getInstance().onResult(U8Code.CODE_REAL_NAME_REG_SUC, "2");
        } else if (result == 2) {
            //未实名
            U8SDK.getInstance().onResult(U8Code.CODE_REAL_NAME_REG_SUC, "0");
        } else if (result == 3) {
            //未成年
            U8SDK.getInstance().onResult(U8Code.CODE_REAL_NAME_REG_SUC, "1");
        }
    }

    public void pay(PayParams data) {
        try {

            Log.d("U8SDK", "papa pay data extension:" + data.getExtension());

            PaayArg paayArg = new PaayArg();
            paayArg.APP_NAME = appName;
            paayArg.APP_ORDER_ID = data.getOrderID();
            paayArg.GAME_SERVER = data.getServerId();
            paayArg.APP_USER_ID = data.getRoleId();
            paayArg.APP_USER_NAME = data.getRoleName();
            paayArg.MONEY_AMOUNT = data.getPrice() + "";
            paayArg.NOTIFY_URI = data.getExtension();
            paayArg.PRODUCT_ID = data.getProductId();

            if (payRatio > 0) {
                paayArg.PRODUCT_NAME = (payRatio * data.getPrice()) + data.getProductName();
            } else {
                paayArg.PRODUCT_NAME = data.getProductName();
            }

            paayArg.PA_OPEN_UID = openUid;//调用登录方法，得到该值
            paayArg.APP_EXT1 = "";
            paayArg.APP_EXT2 = "";

            PPayCenter.pay(paayArg, new PPayCallback() {

                @Override
                public void onPayFinished(int status) {
                    Log.d("U8SDK", "pay result status:" + status);
                    String mmm = "";
                    switch (status) {
                        case PaayActivity.PAPAPay_RESULT_CODE_SUCCESS:
                            mmm = "支付成功";
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                            break;
                        case PaayActivity.PAPAPay_RESULT_CODE_FAILURE:
                            mmm = "支付失败";
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed");
                            break;
                        case PaayActivity.PAPAPay_RESULT_CODE_CANCEL:
                            mmm = "支付取消";
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_CANCEL, "pay cancelled");
                            break;
                        case PaayActivity.PAPAPay_RESULT_CODE_WAIT:
                            mmm = "支付等待";
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_UNKNOWN, "pay state unknown");
                            break;
                        default:
                            mmm = "未知错误，状态:" + status;
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed");
                    }
                    Log.d("U8SDK", mmm);
                }
            });
        } catch (Exception e) {
            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, e.getMessage());
            e.printStackTrace();
        }
    }
}
