 package com.u8.sdk;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.quicksdk.Extend;
import com.quicksdk.QuickSDK;
import com.quicksdk.Sdk;
import com.quicksdk.entity.GameRoleInfo;
import com.quicksdk.entity.OrderInfo;
import com.quicksdk.entity.UserInfo;
import com.quicksdk.notifier.ExitNotifier;
import com.quicksdk.notifier.InitNotifier;
import com.quicksdk.notifier.LoginNotifier;
import com.quicksdk.notifier.LogoutNotifier;
import com.quicksdk.notifier.PayNotifier;
import com.quicksdk.notifier.SwitchAccountNotifier;

import org.json.JSONObject;

public class MyQuickSDK {

    private static MyQuickSDK instance;
    private static final int CODE_PERMISSION = 1;

    private String productCode;
    private String productKey;

    private boolean inited = false;

    public static MyQuickSDK getInstance() {
        if (instance == null) {
            instance = new MyQuickSDK();
        }
        return instance;
    }

    public void initSDK(SDKParams data) {
        try {

            String orientation = data.getString("Q_ORIENTATION");
            productCode = data.getString("Q_PRODUCT_CODE");
            productKey = data.getString("Q_PRODUCT_KEY");

            QuickSDK.getInstance().setIsLandScape("landscape".equalsIgnoreCase(orientation));

            U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

                @Override
                public void onActivityResult(int requestCode, int resultCode, Intent data) {
                    com.quicksdk.Sdk.getInstance().onActivityResult(U8SDK.getInstance().getContext(), requestCode, resultCode, data);
                }

                @Override
                public void onStart() {
                    com.quicksdk.Sdk.getInstance().onStart(U8SDK.getInstance().getContext());
                }

                @Override
                public void onPause() {
                    com.quicksdk.Sdk.getInstance().onPause(U8SDK.getInstance().getContext());
                }

                @Override
                public void onResume() {
                    com.quicksdk.Sdk.getInstance().onResume(U8SDK.getInstance().getContext());
                }

                @Override
                public void onNewIntent(Intent intent) {
                    com.quicksdk.Sdk.getInstance().onNewIntent(intent);
                }

                @Override
                public void onStop() {
                    com.quicksdk.Sdk.getInstance().onStop(U8SDK.getInstance().getContext());
                }

                @Override
                public void onDestroy() {
                    com.quicksdk.Sdk.getInstance().onDestroy(U8SDK.getInstance().getContext());
                }

                @Override
                public void onRestart() {
                    com.quicksdk.Sdk.getInstance().onRestart(U8SDK.getInstance().getContext());
                }


                @Override
                public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

                    if (requestCode == CODE_PERMISSION) {

                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            //申请成功
                            initQuickSDK();
                        } else {
                            //失败  这里逻辑以游戏为准 这里只是模拟申请失败 退出游戏    cp方可改为继续申请 或者其他逻辑
                            Log.w("U8SDK", "permission request failed. go to permission settings ui and stop app.");
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + U8SDK.getInstance().getContext().getPackageName()));
                            U8SDK.getInstance().getContext().startActivity(intent);
                        }
                    }
                }
            });
            checkPermissions();
            com.quicksdk.Sdk.getInstance().onCreate(U8SDK.getInstance().getContext());
        } catch (Exception e) {
            U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_FAIL, "init failed with exception:" + e.getMessage());
            e.printStackTrace();
        }
    }


    private void checkPermissions() {

        if (Build.VERSION.SDK_INT < 23) {
            initQuickSDK();
            return;
        }

        try {
            // check权限
            if ((ContextCompat.checkSelfPermission(U8SDK.getInstance().getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(U8SDK.getInstance().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                // 没有 ， 申请权限 权限数组
                ActivityCompat.requestPermissions(U8SDK.getInstance().getContext(), new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_PERMISSION);
            } else {
                initQuickSDK();
            }
        } catch (Exception e) {// 异常 继续申请
            ActivityCompat.requestPermissions(U8SDK.getInstance().getContext(), new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_PERMISSION);
        }
    }

    private void initQuickSDK() {

        if (inited) {
            Log.d("U8SDK", "sdk already inited, please don't init again.");
            return;
        }

        inited = true;

        Log.d("U8SDK", "begin to init quick sdk...");

        QuickSDK.getInstance().setInitNotifier(new InitNotifier() {

            @Override
            public void onSuccess() {
                U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_SUCCESS, "init success");
            }

            @Override
            public void onFailed(String message, String trace) {
                Log.e("U8SDK", "init failed. message:" + message + ";trace:" + trace);
            }
        })

                // 2.设置登录通知(必接)
                .setLoginNotifier(new LoginNotifier() {

                    @Override
                    public void onSuccess(UserInfo userInfo) {
                        if (userInfo != null) {
                            Log.d("U8SDK", "登陆成功" + "\n\r" + "UserID:  " + userInfo.getUID() + "\n\r" + "UserName:  " + userInfo.getUserName() + "\n\r" + "Token:  " + userInfo.getToken());

                            // 登录成功之后，进入游戏时，需要向渠道提交用户信息
                            try {
                                JSONObject json = new JSONObject();
                                json.put("token", userInfo.getToken());
                                json.put("uid", userInfo.getUID());
                                json.put("channel_code", Extend.getInstance().getChannelType());
                                U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, json.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onCancel() {
                        U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "user cancel");
                    }

                    @Override
                    public void onFailed(final String message, String trace) {
                        Log.e("U8SDK", "login failed. msg:" + message + ";trace:" + trace);
                    }

                })

                // 3.设置注销通知(必接)
                .setLogoutNotifier(new LogoutNotifier() {

                    @Override
                    public void onSuccess() {
                        U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "注销通知");
                    }

                    @Override
                    public void onFailed(String message, String trace) {
                        Log.e("U8SDK", "logout failed. msg:" + message + ";trace:" + trace);
                    }
                })
                // 4.设置切换账号通知(必接)
                .setSwitchAccountNotifier(new SwitchAccountNotifier() {

                    @Override
                    public void onSuccess(UserInfo userInfo) {
                        if (userInfo != null) {
                            Log.d("U8SDK", "切换账号成功" + "\n\r" + "UserID:  " + userInfo.getUID() + "\n\r" + "UserName:  " + userInfo.getUserName() + "\n\r"
                                    + "Token:  " + userInfo.getToken());

                            try {

                                JSONObject json = new JSONObject();
                                json.put("token", userInfo.getToken());
                                json.put("uid", userInfo.getUID());
                                json.put("channel_code", Extend.getInstance().getChannelType());

                                U8SDK.getInstance().onSwitchAccount(U8Code.CODE_SWITCH_ACCOUNT_SUCCESS, json.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailed(String message, String trace) {
                        Log.e("U8SDK", "switchAccount failed. msg:" + message + ";trace:" + trace);
                    }

                    @Override
                    public void onCancel() {
                    }
                })

                // 5.设置支付通知(必接)
                .setPayNotifier(new PayNotifier() {

                    @Override
                    public void onSuccess(String sdkOrderID, String cpOrderID, String extrasParams) {
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                    }

                    @Override
                    public void onCancel(String cpOrderID) {
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay cancel");
                    }

                    @Override
                    public void onFailed(String cpOrderID, String message, String trace) {
                        Log.e("U8SDK", "pay failed. msg:" + message + ";trace:" + trace);
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed");
                    }
                })

                // 6.设置退出通知(必接)
                .setExitNotifier(new ExitNotifier() {

                    @Override
                    public void onSuccess() {
                        // 进行游戏本身的退出操作，下面的finish()只是示例
                        U8SDK.getInstance().getContext().finish();
                        System.exit(0);
                    }

                    @Override
                    public void onFailed(String message, String trace) {
                        Log.e("U8SDK", "exit failed. msg:" + message + ";trace:" + trace);
                    }
                });

        Log.d("U8SDK", "productCode:" + productCode + ";productKey:" + productKey);
        Sdk.getInstance().init(U8SDK.getInstance().getContext(), productCode, productKey);

    }

    public void login() {
        try {
            com.quicksdk.User.getInstance().login(U8SDK.getInstance().getContext());
        } catch (Exception e) {
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed");
            e.printStackTrace();
        }
    }

    public void logout() {
        com.quicksdk.User.getInstance().logout(U8SDK.getInstance().getContext());
    }

    public void exit() {
        if (QuickSDK.getInstance().isShowExitDialog()) {
            Sdk.getInstance().exit(U8SDK.getInstance().getContext());
        } else {
            // 游戏调用自身的退出对话框，点击确定后，调用quick的exit接口
            new AlertDialog.Builder(U8SDK.getInstance().getContext()).setTitle("退出").setMessage("是否退出游戏?").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    Sdk.getInstance().exit(U8SDK.getInstance().getContext());
                }
            }).setNegativeButton("取消", null).show();
        }
    }

    GameRoleInfo roleInfo;

    public void submitGameData(UserExtraData data) {

        if (data.getDataType() == UserExtraData.TYPE_CREATE_ROLE || data.getDataType() == UserExtraData.TYPE_ENTER_GAME || data.getDataType() == UserExtraData.TYPE_LEVEL_UP) {
            roleInfo = new GameRoleInfo();
            roleInfo.setServerID(data.getServerID() + "");// 服务器ID
            roleInfo.setServerName(data.getServerName());// 服务器名称
            roleInfo.setGameRoleName(data.getRoleName());// 角色名称
            roleInfo.setGameRoleID(data.getRoleID());// 角色ID
            roleInfo.setGameUserLevel(data.getRoleLevel());// 等级
            roleInfo.setVipLevel(TextUtils.isEmpty(data.getVip()) ? "0" : data.getVip()); // 设置当前用户vip等级，必须为整型字符串
            roleInfo.setGameBalance(data.getMoneyNum() + ""); // 角色现有金额
            roleInfo.setPartyName(data.getPartyName()); // 设置帮派，公会名称
            roleInfo.setRoleCreateTime(data.getRoleCreateTime() + ""); // UC与1881渠道必传，值为10位数时间戳
            roleInfo.setPartyId(data.getPartyID()); // 360渠道参数，设置帮派id，必须为整型字符串
            roleInfo.setGameRoleGender(data.getRoleGender() + ""); // 360渠道参数
            roleInfo.setGameRolePower(data.getPower()); // 360渠道参数，设置角色战力，必须为整型字符串
            roleInfo.setPartyRoleId("0"); // 360渠道参数，设置角色在帮派中的id
            roleInfo.setPartyRoleName("无"); // 360渠道参数，设置角色在帮派中的名称
            roleInfo.setProfessionId(data.getProfessionID()); // 360渠道参数，设置角色职业id，必须为整型字符串
            roleInfo.setProfession(data.getProfessionName()); // 360渠道参数，设置角色职业名称
            roleInfo.setFriendlist("无"); // 360渠道参数，设置好友关系列表，格式请参考：http://open.quicksdk.net/help/detail/aid/190
            com.quicksdk.User.getInstance().setGameRoleInfo(U8SDK.getInstance().getContext(), roleInfo, data.getDataType() == UserExtraData.TYPE_CREATE_ROLE);
        }


    }

    public void pay(PayParams data) {
        try {

            GameRoleInfo roleInfo = new GameRoleInfo();
            roleInfo.setServerID(data.getServerId());// 服务器ID，其值必须为数字字符串
            roleInfo.setServerName(data.getServerName());// 服务器名称
            roleInfo.setGameRoleName(data.getRoleName());// 角色名称
            roleInfo.setGameRoleID(data.getRoleId());// 角色ID
            roleInfo.setGameUserLevel(data.getRoleLevel() + "");// 等级
            roleInfo.setVipLevel(TextUtils.isEmpty(data.getVip()) ? "0" : data.getVip());// VIP等级
            roleInfo.setGameBalance(data.getCoinNum() + "");// 角色现有金额
            roleInfo.setPartyName("");// 公会名字

            if (MyQuickSDK.this.roleInfo != null) {
                roleInfo.setRoleCreateTime(MyQuickSDK.this.roleInfo.getRoleCreateTime());
            } else {
                roleInfo.setRoleCreateTime(System.currentTimeMillis() / 1000 + "");
            }

            if (TextUtils.isEmpty(roleInfo.getRoleCreateTime())) {
                roleInfo.setRoleCreateTime(System.currentTimeMillis() / 1000 + "");
            }


            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setCpOrderID(data.getOrderID());// 游戏订单号
            orderInfo.setGoodsName(data.getProductName());// 产品名称
            orderInfo.setGoodsDesc(data.getProductDesc());
            // orderInfo.setGoodsName("月卡");
            orderInfo.setCount(1);// 购买数量，如购买"10元宝"则传10
            // orderInfo.setCount(1);// 购买数量，如购买"月卡"则传1
            orderInfo.setAmount(data.getPrice()); // 总金额（单位为元）
            orderInfo.setGoodsID(data.getProductId()); // 产品ID，用来识别购买的产品
            orderInfo.setExtrasParams(data.getOrderID()); // 透传参数

            com.quicksdk.Payment.getInstance().pay(U8SDK.getInstance().getContext(), orderInfo, roleInfo);

        } catch (Exception e) {
            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed with exception." + e.getMessage());
            e.printStackTrace();
        }
    }

}
