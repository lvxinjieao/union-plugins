package com.u8.sdk.impl;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.u8.sdk.IUser;
import com.u8.sdk.U8Code;
import com.u8.sdk.U8SDK;
import com.u8.sdk.UserExtraData;
import com.u8.sdk.impl.listeners.ISDKExitListener;
import com.u8.sdk.impl.listeners.ISDKListener;
import com.u8.sdk.impl.listeners.ISDKLoginListener;

import org.json.JSONObject;

/**
 * 当游戏层接入之后，如果不仅过打包工具打包，那么默认使用该类作为IUser接口的实现类，
 * 让用户得知，当前接口的调用是否正确
 */
public class SimpleUser implements IUser {

    @Override
    public boolean isSupportMethod(String methodName) {
        return true;
    }

    public SimpleUser(Activity activity) {

        DefaultSDKPlatform.getInstance().init(U8SDK.getInstance().getContext(), new ISDKListener() {

            @Override
            public void onSuccess() {
                Log.d("U8SDK", "default sdk init success");
                U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_SUCCESS, "init success");
            }

            @Override
            public void onFailed(int code) {
                Log.e("U8SDK", "default sdk inti failed.");
                U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_FAIL, "init failed");
            }
        });


    }

    @Override
    public void login() {

        DefaultSDKPlatform.getInstance().login(U8SDK.getInstance().getContext(), new ISDKLoginListener() {

            @Override
            public void onSuccess(String id, String name) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("userId", id);
                    json.put("username", name);
                    U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, json.toString());
                } catch (Exception e) {
                    U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int code) {

            }
        });
    }

    @Override
    public void login(String customData) {
        login();
    }

    @Override
    public void switchLogin() {
        login();
    }


    @Override
    public void logout() {
        tip("调用登出接口成功，测试界面无登出功能");
    }

    @Override
    public void submitExtraData(UserExtraData extraData) {
        tip("调用[提交扩展数据]接口成功，详细数据可以看logcat日志");
        DefaultSDKPlatform.getInstance().submitGameData(U8SDK.getInstance().getContext(), extraData);
    }

    @Override
    public void exit() {
        DefaultSDKPlatform.getInstance().exitSDK(U8SDK.getInstance().getContext(), new ISDKExitListener() {

            @Override
            public void onExit() {
                U8SDK.getInstance().getContext().finish();
                System.exit(0);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    @Override
    public void realNameRegister() {
        tip("游戏中暂时不需要调用该接口");
    }

    @Override
    public void queryAntiAddiction() {
        tip("游戏中暂时不需要调用该接口");
    }

    private void tip(String msg) {
        Toast.makeText(U8SDK.getInstance().getContext(), msg, Toast.LENGTH_LONG).show();
    }

}
