package com.u8.sdk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.util.Log;

import com.muzhiwan.sdk.core.MzwSdkController;
import com.muzhiwan.sdk.core.callback.MzwInitCallback;
import com.muzhiwan.sdk.core.callback.MzwLoignCallback;
import com.muzhiwan.sdk.core.callback.MzwPayCallback;
import com.muzhiwan.sdk.core.callback.MzwPostGiftCodeCallback;
import com.muzhiwan.sdk.service.MzwOrder;

public class MZWSDK {

    private static MZWSDK instance;

    enum SDKState {
        StateDefault,
        StateIniting,
        StateInited,
        StateLogin,
        StateLogined
    }

    private SDKState state = SDKState.StateDefault;
    private boolean loginAfterInit = false;

    private int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    private MZWSDK() {

    }

    public static MZWSDK getInstance() {
        if (instance == null) {
            instance = new MZWSDK();
        }
        return instance;
    }

    private void parseSDKParams(SDKParams params) {
        String orn = params.getString("MZW_Orientation");
        if ("landscape".equalsIgnoreCase(orn)) {
            this.orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if ("portrait".equalsIgnoreCase(orn)) {
            this.orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else {
            this.orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
    }

    public void initSDK(SDKParams params) {
        this.parseSDKParams(params);
        this.initSDK();
    }

    public void initSDK() {
        state = SDKState.StateIniting;

        try {

            if (!SDKTools.isNetworkAvailable(U8SDK.getInstance().getContext())) {
                U8SDK.getInstance().runOnMainThread(new Runnable() {

                    @Override
                    public void run() {
                        showDialog();
                    }
                });
                return;
            }

            U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

                public void onDestroy() {
                    MzwSdkController.getInstance().destory();
                }

            });

            int orn = MzwSdkController.ORIENTATION_VERTICAL;
            if (this.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                orn = MzwSdkController.ORIENTATION_HORIZONTAL;
            }

            MzwSdkController.getInstance().init(U8SDK.getInstance().getContext(), orn, new MzwInitCallback() {

                @Override
                public void onResult(int code, String msg) {
                    Log.d("U8SDK", "muzhiwan init result:" + code + ";msg:" + msg);
                    if (code == 1) {
                        state = SDKState.StateInited;
                        U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_SUCCESS, "init success");
                        if (loginAfterInit) {
                            loginAfterInit = false;
                            login();
                        }
                    } else {
                        state = SDKState.StateDefault;
                        U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_FAIL, "init failed");
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            U8SDK.getInstance().onResult(U8Code.CODE_INIT_FAIL, "init failed. exception:" + e.getMessage());
        }
    }


    public void login() {
        if (state.ordinal() < SDKState.StateInited.ordinal()) {
            loginAfterInit = true;
            initSDK();
            return;
        }

        if (!SDKTools.isNetworkAvailable(U8SDK.getInstance().getContext())) {
            U8SDK.getInstance().onResult(U8Code.CODE_NO_NETWORK, "The network now is unavailable");
            return;
        }

        try {
            state = SDKState.StateLogin;
            MzwSdkController.getInstance().doLogin(new MzwLoignCallback() {

                @Override
                public void onResult(int code, String msg) {
                    Log.d("U8SDK", "muzhiwan login callback:" + code + ";msg:" + msg);
                    if (code == 1) {
                        state = SDKState.StateLogined;
                        String token = msg;
                        U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, token);
                    } else if (code == 6) {
                        Log.d("U8SDK", "logout success");
                        U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
                    } else {
                        state = SDKState.StateInited;
                        U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed.code:" + code);
                    }
                }
            });
        } catch (Exception e) {
            state = SDKState.StateInited;
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, e.getMessage());
            e.printStackTrace();
        }
    }

    public void logout() {
        MzwSdkController.getInstance().doLogout();
    }

    public void postGiftCode(String code) {
        MzwSdkController.getInstance().doPostGiftCode(code, new MzwPostGiftCodeCallback() {

            @Override
            public void onResult(int code, String msg) {
                if (code == 1) {
                    U8SDK.getInstance().onResult(U8Code.CODE_POST_GIFT_SUC, "post gift code success");
                } else {
                    U8SDK.getInstance().onResult(U8Code.CODE_POST_GIFT_FAILED, msg);
                }
            }
        });

    }

    private MzwOrder encodePayInfo(PayParams params) {
        MzwOrder order = new MzwOrder();
        order.setExtern(params.getOrderID());
        order.setMoney(params.getPrice());
        order.setProductdesc(params.getProductName());
        order.setProductid(params.getProductId());
        order.setProductname(params.getProductName());
        return order;
    }

    public void pay(PayParams params) {

//		if(!isLogined()){
//			U8SDK.getInstance().onResult(U8Code.CODE_INIT_FAIL, "The sdk is not logined.");
//			return;
//		}

        if (!SDKTools.isNetworkAvailable(U8SDK.getInstance().getContext())) {
            U8SDK.getInstance().onResult(U8Code.CODE_NO_NETWORK, "The network now is unavailable");
            return;
        }

        try {
            MzwOrder order = encodePayInfo(params);
            MzwSdkController.getInstance().doPay(order, new MzwPayCallback() {

                @Override
                public void onResult(int code, MzwOrder o) {
                    switch (code) {
                        case 1:
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                            break;
                        case 0:
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_CANCEL, "pay cancel");
                            break;
                        case -1:
                            //支付中，不需要处理
                            break;
                        case 5:
                            //支付完成
                            break;
                        default:
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed.code:" + code);
                            break;
                    }
                }

            });

        } catch (Exception e) {
            e.printStackTrace();
            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_UNKNOWN, "pay failed.exception:" + e.getMessage());
        }

    }

    public boolean isLogined() {
        return this.state.ordinal() >= SDKState.StateLogined.ordinal();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(U8SDK.getInstance().getContext());
        builder.setTitle("联网提示");
        builder.setMessage("当前没有联网，请联网之后再进行游戏");
        builder.setCancelable(true);
        builder.setPositiveButton("确　定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                U8SDK.getInstance().getContext().finish();
                System.exit(0);
            }
        });
        builder.show();
    }
}
