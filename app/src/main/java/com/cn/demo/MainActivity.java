package com.cn.demo;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.u8.sdk.PayParams;
import com.u8.sdk.U8Code;
import com.u8.sdk.U8Order;
import com.u8.sdk.U8SDK;
import com.u8.sdk.UserExtraData;
import com.u8.sdk.platform.InitListener;
import com.u8.sdk.platform.Platform;
import com.u8.sdk.plugin.U8User;
import com.u8.sdk.verify.URealNameInfo;
import com.u8.sdk.verify.UToken;

public class MainActivity extends Activity implements View.OnClickListener {


    public static final String TAG = "SDK";

    public Button login, logout, switchLogin, submit, pay, exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = (Button) findViewById(R.id.login);
        logout = (Button) findViewById(R.id.logout);
        switchLogin = (Button) findViewById(R.id.switchLogin);
        submit = (Button) findViewById(R.id.submit);
        pay = (Button) findViewById(R.id.pay);
        exit = (Button) findViewById(R.id.exit);

        login.setOnClickListener(this);
        logout.setOnClickListener(this);
        switchLogin.setOnClickListener(this);
        submit.setOnClickListener(this);
        pay.setOnClickListener(this);
        exit.setOnClickListener(this);


        Platform.getInstance().init(MainActivity.this, new InitListener() {

            @Override
            public void onResult(int code, String string) {
                Log.i(TAG, "特殊事件的回调");
            }

            @Override
            public void onInitResult(int code, String result) {
                switch (code) {
                    case U8Code.CODE_INIT_SUCCESS:// 初始化成功
                        Log.i(TAG, "初始化成功");
                        break;
                    case U8Code.CODE_INIT_FAIL:// 初始化失败
                        Log.i(TAG, "初始化失败");
                        break;
                }
            }

            @Override
            public void onLoginResult(int code, UToken result) {
                switch (code) {
                    case U8Code.CODE_LOGIN_SUCCESS:// 登录成功
                        Log.i(TAG, "登录成功: " + result.toString());
                        //userID:U8Server生成的唯一用户ID，游戏服务器需要将游戏账户ID和该userID进行绑定。
                        //sdkUserID:渠道SDK平台用户唯一ID，一般不需要使用
                        //username:U8Server生成的用户名，比如234234234.uc,4353453453.baidu,65756756756.360
                        //sdkUserName:渠道SDK平台用户名，可能为空，部分渠道SDK没有返回用户名
                        //token:U8Server生成的会话ID，游戏服务器拿该字段去U8Server做二次登录验证
                        //extension:U8Server返回的扩展字段，部分渠道SDK需要，游戏中无需使用该字段的值
                        //timestamp:U8Server生成的时间戳，游戏服务器去U8Server做二次登录验证时，传给U8Server

                        int userID = result.getUserID();
                        String username = result.getUsername();
                        String sdkUserID = result.getSdkUserID();
                        String sdkUsername = result.getSdkUsername();
                        String token = result.getToken();
                        String extension = result.getExtension();
                        String timestamp = result.getTimestamp();
                        break;
                    case U8Code.CODE_LOGIN_FAIL://// 登录失败
                        Log.i(TAG, "登录失败");
                        break;
                }
            }

            @Override
            public void onSwitchAccount(int code, UToken result) {
                switch (code) {
                    case U8Code.CODE_SWITCH_ACCOUNT_SUCCESS:// 切换账号成功
                        Log.i(TAG, "切换账号成功: " + result.toString());
                        int userID = result.getUserID();
                        String username = result.getUsername();
                        String sdkUserID = result.getSdkUserID();
                        String sdkUsername = result.getSdkUsername();
                        String token = result.getToken();
                        String extension = result.getExtension();
                        String timestamp = result.getTimestamp();
                        break;
                    case U8Code.CODE_SWITCH_ACCOUNT_FAIL:// 切换账号失败
                        Log.i(TAG, "切换账号失败");
                        break;
                }
            }

            @Override
            public void onLogoutResult(int code, String string) {
                switch (code) {
                    case U8Code.CODE_LOGOUT_SUCCESS:// 登出账号成功
                        Log.i(TAG, "登出账号成功");
                        break;
                    case U8Code.CODE_LOGOUT_FAIL:// 登出账号账号失败
                        Log.i(TAG, "登出账号账号失败");
                        break;
                }
            }


            @Override
            public void onPayResult(int code, String string) {
                switch (code) {
                    case U8Code.CODE_PAY_SUCCESS:// 支付成功
                        Log.i(TAG, "支付成功");
                        break;
                    case U8Code.CODE_PAY_FAIL:// 支付失败
                        Log.i(TAG, "支付失败");
                        break;
                }
            }

            @Override
            public void onSinglePayResult(int code, U8Order order) {

            }

            @Override
            public void onExitResult(int code, String string) {
                if (code == U8Code.CODE_EXIT_GAME) {// 退出游戏
                    Log.i(TAG, "退出游戏");
                }
            }

            @Override
            public void onRealNameResult(URealNameInfo realNameInfo) {

            }

        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                Log.i(TAG, "-------------------login--------------------------");
                Platform.getInstance().login(MainActivity.this);
                break;

            case R.id.logout:
                Log.i(TAG, "-------------------logout--------------------------");
                Platform.getInstance().logout();
                break;

            case R.id.switchLogin:
                Log.i(TAG, "-------------------switchLogin--------------------------");
                Platform.getInstance().switchAccount();
                break;

            case R.id.submit:
                Log.i(TAG, "-------------------submit--------------------------");
                UserExtraData data = new UserExtraData();
                data.setDataType(1);
                data.setServerID(1);
                data.setServerName("server_name");
                data.setRoleID("role_id");
                data.setRoleName("role_name");
                data.setRoleLevel("1");
                data.setMoneyNum(1);
                data.setVip("1");
                data.setRoleCreateTime(System.currentTimeMillis() / 1000);
                data.setRoleLevelUpTime(System.currentTimeMillis() / 1000);
                Platform.getInstance().submitExtraData(data);
                break;

            case R.id.pay:
                Log.i(TAG, "-------------------pay--------------------------");
                PayParams params = new PayParams();
                params.setProductId(System.currentTimeMillis() + "");// 商品id
                params.setProductName("product_name");
                params.setProductDesc("product_desc");
                params.setPrice(1);// 单价:元
                params.setRatio(10);// 对话比例
                params.setBuyNum(1);// 数量
                params.setCoinNum(1);// 金额
                params.setServerId("1");
                params.setServerName("server_name");
                params.setRoleId("1");
                params.setRoleName("role_name");
                params.setRoleLevel(1);
                params.setPayNotifyUrl("");
                params.setVip("1");
                params.setExtension("extt");
                Platform.getInstance().pay(MainActivity.this, params);
                break;

            case R.id.exit:
                Log.i(TAG, "-------------------exit--------------------------");
                U8User.getInstance().exit();
                break;

            default:
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        U8SDK.getInstance().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        U8SDK.getInstance().onStart();
        super.onStart();
    }

    @Override
    public void onPause() {
        U8SDK.getInstance().onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        U8SDK.getInstance().onResume();
        super.onResume();
    }

    @Override
    public void onNewIntent(Intent newIntent) {
        U8SDK.getInstance().onNewIntent(newIntent);
        super.onNewIntent(newIntent);
    }

    @Override
    public void onStop() {
        U8SDK.getInstance().onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        U8SDK.getInstance().onDestroy();
        super.onDestroy();
    }

    @Override
    public void onRestart() {
        U8SDK.getInstance().onRestart();
        super.onRestart();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        U8SDK.getInstance().onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        U8SDK.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        if (U8User.getInstance().isSupport("exit")) {
            U8User.getInstance().exit();
        } else {
            this.finish();
            System.exit(0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (U8User.getInstance().isSupport("exit")) {
                U8User.getInstance().exit();
            } else {
                this.finish();
                System.exit(0);
            }
        }
        return true;
    }


}
