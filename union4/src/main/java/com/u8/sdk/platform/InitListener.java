package com.u8.sdk.platform;

import com.u8.sdk.U8Order;
import com.u8.sdk.verify.URealNameInfo;
import com.u8.sdk.verify.UToken;


/**
 * U8SDK初始化回调
 */
public interface InitListener {

    public void onResult(int code, String msg);

    /**
     * 初始化结果
     */
    public void onInitResult(int code, String msg);

    /**
     * U8平台登录回调
     */
    public void onLoginResult(int code, UToken token);

    /**
     * 游戏中通过SDK切换到新账号的回调，游戏收到该回调，需要引导用户重新登录，重新加载该新用户对应的角色数据
     */
    public void onSwitchAccount(int code, UToken token);

    /**
     * 用户登出回调（需要收到该回调需要返回游戏登录界面，并调用login接口，打开SDK登录界面）
     */
    public void onLogoutResult(int code, String msg);

    /**
     * 支付结果回调
     */
    public void onPayResult(int code, String msg);

    /**
     * 单机付结果回调
     */
    public void onSinglePayResult(int code, U8Order order);

    /**
     * 退出结果
     */
    public void onExitResult(int code, String msg);


    /**
     * SDK实名认证查询结果
     */
    public void onRealNameResult(URealNameInfo realNameInfo);

}
