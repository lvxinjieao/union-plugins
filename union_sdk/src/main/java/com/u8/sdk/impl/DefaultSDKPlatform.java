package com.u8.sdk.impl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.u8.sdk.PayParams;
import com.u8.sdk.U8SDK;
import com.u8.sdk.UserExtraData;
import com.u8.sdk.impl.activities.LoginActivity;
import com.u8.sdk.impl.activities.PayActivity;
import com.u8.sdk.impl.common.Consts;
import com.u8.sdk.impl.listeners.ISDKExitListener;
import com.u8.sdk.impl.listeners.ISDKListener;
import com.u8.sdk.impl.listeners.ISDKLoginListener;
import com.u8.sdk.impl.listeners.ISDKPayListener;
import com.u8.sdk.impl.services.SdkManager;

public class DefaultSDKPlatform {

    private ISDKLoginListener loginListener;
    private ISDKPayListener payListener;
    private PayParams lastPayData;

    private static DefaultSDKPlatform instance;

    private DefaultSDKPlatform() {
    }

    public static DefaultSDKPlatform getInstance() {
        if (instance == null) {
            instance = new DefaultSDKPlatform();
        }
        return instance;
    }

    public void init(Context context, ISDKListener listener) {
        SdkManager.getInstance().init(context, listener);
    }

    public void login(Activity context, ISDKLoginListener listener) {
        this.loginListener = listener;
        context.startActivity(new Intent(context, LoginActivity.class));
    }

    public void submitGameData(Activity context, final UserExtraData data) {

        SdkManager.getInstance().submitGameData(context, data, new ISDKListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(U8SDK.getInstance().getContext(), data.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailed(int code) {

            }
        });
    }

    public void exitSDK(Activity context, final ISDKExitListener listener) {

        //游戏自己的退出确认框
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("退出确认");
        builder.setMessage("亲，现在还早，要不要再玩一会？");
        builder.setCancelable(true);
        builder.setPositiveButton("好吧",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        //这里什么都不用做
                        listener.onCancel();
                    }
                });
        builder.setNeutralButton("一会再玩",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        //退出游戏
                        listener.onExit();
                    }
                });
        builder.show();

    }

    public void pay(Activity context, PayParams data, ISDKPayListener listener) {
        this.payListener = listener;
        this.lastPayData = data;
        Toast.makeText(U8SDK.getInstance().getContext(), data.toString(), Toast.LENGTH_LONG).show();
        context.startActivity(new Intent(context, PayActivity.class));
    }

    public PayParams getLastPayData() {
        return this.lastPayData;
    }

    public void loginSucCallback(String userId, String username) {
        if (loginListener != null) {
            loginListener.onSuccess(userId, username);
        }
        loginListener = null;
    }

    public void loginFailCallback() {
        if (loginListener != null) {
            loginListener.onFailed(Consts.CODE_FAILED);
        }
        loginListener = null;
    }

    public void paySucCallback() {
        if (payListener != null) {
            payListener.onSuccess("");
        }
        payListener = null;
        lastPayData = null;
    }

    public void payFailCallback() {
        if (payListener != null) {
            payListener.onFailed(Consts.CODE_FAILED);
        }
        payListener = null;
        lastPayData = null;
    }
}
