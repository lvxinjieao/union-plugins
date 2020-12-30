package com.u8.sdk.base;

import com.u8.sdk.U8Order;
import com.u8.sdk.verify.URealNameInfo;
import com.u8.sdk.verify.UToken;

public interface ISDKListener {

    public void onResult(int code, String result);

    public void onInitResult(int code, String result);

    public void onLoginResult(int code, String result);

    public void onAuthResult(UToken authResult);

    public void onSwitchAccount(int code, String result);

    public void onLogoutResult(int code, String result);

    public void onPayResult(int code, String result);

    public void onSinglePayResult(int code, U8Order order);

    public void onExitResult(int code, String result);

    public void onRealNameResult(URealNameInfo realNameInfo);
}
