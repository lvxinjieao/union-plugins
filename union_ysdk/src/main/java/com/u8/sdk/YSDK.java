package com.u8.sdk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.tencent.ysdk.api.YSDKApi;
import com.tencent.ysdk.framework.common.BaseRet;
import com.tencent.ysdk.framework.common.eFlag;
import com.tencent.ysdk.framework.common.ePlatform;
import com.tencent.ysdk.module.AntiAddiction.model.AntiAddictRet;
import com.tencent.ysdk.module.pay.PayItem;
import com.tencent.ysdk.module.pay.PayListener;
import com.tencent.ysdk.module.pay.PayRet;
import com.tencent.ysdk.module.share.ShareApi;
import com.tencent.ysdk.module.share.ShareCallBack;
import com.tencent.ysdk.module.share.impl.IScreenImageCapturer;
import com.tencent.ysdk.module.share.impl.ShareRet;
import com.tencent.ysdk.module.user.UserApi;
import com.tencent.ysdk.module.user.UserLoginRet;
//import com.u8.sdk.permission.U8AutoPermission;
import com.u8.sdk.permission.utils.OSUtils;
import com.u8.sdk.utils.EncryptUtils;
import com.u8.sdk.utils.ResourceHelper;
import com.u8.sdk.utils.U8HttpUtils;
import com.u8.sdk.ysdk.permission.PermissionGen;
import com.u8.sdk.ysdk.permission.utils.PermissionUtils;

public class YSDK {

    private static final int CODE_YSDK_PERMISSION = 1000024;

    public static final int LOGIN_TYPE_QQ = 1;      //QQ登录类型
    public static final int LOGIN_TYPE_WX = 2;      //微信登录类型

    public static final int REQ_TYPE_QUERY = 1;     //查询余额
    public static final int REQ_TYPE_CHARGE = 2;    //扣款

    private int payType = 1;                        //支付类型，1：游戏币托管模式；2：道具直购模式
    private boolean fixedPay;                       //是否定额支付
    private boolean multiServers;                   //是否开启了分区
    private int ratio;                              //兑换比例
    private String coinIconName;                    //元宝名称
    private String queryUrl;                        //查询地址
    private String payUrl;                          //支付地址
    private String appKey;                          //沙箱appkey
    private boolean isCustomLogin = false;          //是否使用自定义布局
    public boolean useLogin = true;                 //启用登录

    private boolean logined = false;
    public int lastLoginType = 0;

    private String lastLoginResult = null;

    private volatile boolean inited = false;

    public boolean afterLogout = false;

    private boolean isLandscape = false;

    public boolean isLoginCustom = false;

    private boolean isPermissioned = true;

    private boolean loginAfterPermission = false;

    public Activity lastActivityWaithDestroy = null;

    public boolean mAntiAddictExecuteState = false;


    final String[] permissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_SMS
    };

    private static YSDK instance;

    public static YSDK getInstance() {
        if (instance == null) {
            instance = new YSDK();
        }
        return instance;
    }


    private void parseSDKParams(SDKParams params) {
        fixedPay = params.getBoolean("WG_FIXEDPAY");
        coinIconName = params.getString("WG_COIN_ICON_NAME");
        multiServers = params.getBoolean("WG_MULTI_SERVERS");
        queryUrl = params.getString("WG_QUERY_URL");
        payUrl = params.getString("WG_PAY_URL");
        ratio = params.getInt("WG_RATIO");
        appKey = params.getString("M_APP_KEY");
        payType = params.getInt("M_PAY_TYPE");
        isCustomLogin = params.getBoolean("M_CUSTOM_LOGIN");
        if (params.contains("M_USE_LOGIN")) {
            useLogin = params.getBoolean("M_USE_LOGIN");
        }
        isLandscape = isLandscape();
    }

    public boolean isStartLogined() {
        return logined;
    }

    public boolean isCustomLogin() {
        return isCustomLogin;
    }

    public void initSDK(SDKParams params) {
        if (!inited) {
            Log.d("U8SDK", "begin init ysdk");
            parseSDKParams(params);

            U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

                @Override
                public void onActivityResult(int requestCode, int resultCode, Intent data) {
                    Log.d("U8SDK", "onActivityResult call in ysdk");
                    YSDKApi.onActivityResult(requestCode, resultCode, data);
                }

                @Override
                public void onStop() {
                    Log.d("U8SDK", "onStop call in ysdk");
                    YSDKApi.onStop(U8SDK.getInstance().getContext());
                }


                @Override
                public void onResume() {
                    Log.d("U8SDK", "onResume call in ysdk");
                    YSDKApi.onResume(U8SDK.getInstance().getContext());
                }

                @Override
                public void onRestart() {
                    Log.d("U8SDK", "onRestart call in ysdk");
                    YSDKApi.onRestart(U8SDK.getInstance().getContext());
                }

                @Override
                public void onPause() {
                    Log.d("U8SDK", "onPause call in ysdk");
                    YSDKApi.onPause(U8SDK.getInstance().getContext());
                }

                @Override
                public void onNewIntent(Intent newIntent) {
                    Log.d("U8SDK", "onNewIntent call in ysdk");
                    YSDKApi.handleIntent(newIntent);
                }

                @Override
                public void onDestroy() {
                    Log.d("U8SDK", "onDestroy call in ysdk");
                    YSDKApi.onDestroy(U8SDK.getInstance().getContext());
                }

                @Override
                public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
                    Log.d("U8SDK", "onRequestPermissionsResult for ysdk");
                    PermissionGen.onCustomRequestPermissionsResult(YSDK.this, requestCode, permissions, grantResults);
                }
            });
        }

        Log.d("U8SDK", "begin init ysdk in onCreate...");
        initYSDK();
        //权限申请
        if (PermissionUtils.isOverMarshmallow() || OSUtils.isMIUI()) {
            Log.d("U8SDK", "begin to request permissions for ysdk");
            checkAndRequestPermission();
        }
    }

    private void checkAndRequestPermission() {

        if (PermissionGen.isNeedPermissions(U8SDK.getInstance().getContext(), permissions)) {

            //部分游戏，比如小米青春版，可能会因为申请权限，造成游戏界面闪，这里延迟调用
            isPermissioned = false;
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

                @Override
                public void run() {
                    Log.d("U8SDK", "ysdk need to request permissions.");
                    PermissionGen.with(U8SDK.getInstance().getContext())
                            .addRequestCode(CODE_YSDK_PERMISSION)
                            .permissions(permissions).request();
                }
            }, 1500);
        } else {
            Log.d("U8SDK", "ysdk does not need to request permissions.");
        }
    }

    public void permissionReqSuccess() {
        Log.d("U8SDK", "permission request success. now to init ysdk.");
        this.isPermissioned = true;

        if (this.loginAfterPermission) {
            this.loginAfterPermission = false;
            login(lastLoginType);
        }

    }

    @SuppressLint("InlinedApi")
    public void permissionReqFailed() {
        Log.e("U8SDK", "ysdk permissions request failed.");
        showPermissionDialog(U8SDK.getInstance().getContext());
    }


    private String parseDeniedPermissionNames() {

        List<String> deniedPermissions = PermissionUtils.findDeniedPermissions(U8SDK.getInstance().getContext(), permissions);

        StringBuilder sb = new StringBuilder();

        Activity context = U8SDK.getInstance().getContext();

        for (String m : deniedPermissions) {
            if (Manifest.permission.READ_PHONE_STATE.equals(m)) {
                sb.append("【手机状态】");
            } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(m)) {
                sb.append("【存储】");
            } else if (Manifest.permission.ACCESS_FINE_LOCATION.equals(m)) {
                sb.append("【位置】");
            } else if (Manifest.permission.READ_SMS.equals(m)) {
                sb.append("【短信】");
            }
        }

        return sb.toString();

    }

    private void showPermissionDialog(Activity context) {
        try {

            String title = "权限说明";

            String msg = "为了可以正常游戏，您需要手动开启：%s权限";
            msg = String.format(msg, parseDeniedPermissionNames());

            String openTxt = "去开启";
            String denyTxt = "残忍拒绝";

            AlertDialog.Builder builder = new AlertDialog.Builder(U8SDK.getInstance().getContext());
            builder.setTitle(title);
            builder.setMessage(msg);
            builder.setCancelable(true);
            builder.setPositiveButton(denyTxt,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            //这里什么都不用做
                            Log.d("U8SDK", "user cancel to open permission. so app kill self.");
                            U8SDK.getInstance().getContext().finish();
                            System.exit(0);
                        }
                    });
            builder.setNeutralButton(openTxt,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + U8SDK.getInstance().getContext().getPackageName()));
                            U8SDK.getInstance().getContext().startActivity(intent);
                            U8SDK.getInstance().getContext().finish();
                            System.exit(0);
                        }
                    });
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void initYSDK() {

        Log.d("U8SDK", "begin call onCreate of ysdk...");
        try {
            YSDKApi.onCreate(U8SDK.getInstance().getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!inited) {
            try {
                YSDKApi.setUserListener(new YSDKCallback());
                YSDKApi.setBuglyListener(new YSDKCallback());
                YSDKApi.setAntiAddictListener(new YSDKCallback());
            } catch (Exception e) {
                e.printStackTrace();
            }

            YSDKApi.setScreenCapturer(new IScreenImageCapturer() {

                @Override

                public Bitmap caputureImage() {
                    try {
                        String path = U8SDK.getInstance().getContext().getExternalFilesDir("screenshot").getAbsoluteFile() + "/" + SystemClock.currentThreadTimeMillis() + ".png";
                        Log.d("U8SDK", "curr thread:" + Thread.currentThread().getName());
                        Log.d("U8SDK", "ysdk capture img called. path:" + path);
                        U8SDK.getInstance().onResult(U8Code.CODE_CAPTURE_IMG, path);
                        File f = new File(path);
                        long t = SystemClock.currentThreadTimeMillis();
                        while (SystemClock.currentThreadTimeMillis() - t < 3000 && !f.exists()) {
                            Thread.sleep(500);
                        }

                        if (f.exists()) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(path, options);
                            options.inSampleSize = calculateInSampleSize(options);
                            options.inJustDecodeBounds = false;
                            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                            return bitmap;
                        }
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

            });

            YSDKApi.handleIntent(U8SDK.getInstance().getContext().getIntent());

            if (!useLogin) {
                UserApi.getInstance().login(ePlatform.Guest);
            }

            try {
                ShareApi.getInstance().regShareCallBack(new ShareCallBack() {
                    @Override
                    public void onSuccess(ShareRet ret) {
                        U8SDK.getInstance().onResult(U8Code.CODE_SHARE_SUCCESS, "share success");
                        Log.d("U8SDK", "分享成功！  分享路径：" + ret.shareType.name() + " 透传信息：" + ret.extInfo);
                    }

                    @Override
                    public void onError(ShareRet ret) {
                        U8SDK.getInstance().onResult(U8Code.CODE_SHARE_FAILED, "share failed");
                        Log.d("U8SDK", "分享失败  分享路径：" + ret.shareType.name() + " 错误码：" + ret.retCode + " 错误信息：" + ret.retMsg + " 透传信息：" + ret.extInfo);
                    }

                    @Override
                    public void onCancel(ShareRet ret) {
                        U8SDK.getInstance().onResult(U8Code.CODE_SHARE_FAILED, "share cancel");
                        Log.d("U8SDK", "分享用户取消！  分享路径：" + ret.shareType.name() + " 透传信息：" + ret.extInfo);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            inited = true;
            U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_SUCCESS, "init success");
        }
    }


    // 获取当前登录平台
    public ePlatform getPlatform() {
        UserLoginRet ret = new UserLoginRet();
        YSDKApi.getLoginRecord(ret);
        if (ret.flag == eFlag.Succ) {
            return ePlatform.getEnum(ret.platform);
        }
        return ePlatform.None;
    }

    public void logout() {
        Log.d("U8SDK", "logout from sdk");
        afterLogout = true;
        lastLoginResult = null;
        YSDKApi.logout();
        U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "logout from sdk");
    }

    public void switchLogin() {
        Log.d("U8SDK", "switch login...");
        afterLogout = true;
        lastLoginResult = null;
        login();
    }

    public void login() {
        Log.d("U8SDK", "ysdk login begin...");
        try {
            if (lastLoginResult != null) {
                Log.d("U8SDK", "already auto logined. callback result directly." + lastLoginResult);
                U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, lastLoginResult);
                lastLoginResult = null;
                return;
            }
            openLoginUI();
        } catch (Exception e) {
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, e.getMessage());
            e.printStackTrace();
        }
    }


    public void submitExtraData(UserExtraData data) {
        try {
            YSDKApi.reportGameRoleInfo(data.getServerID() + "", data.getServerName(), data.getRoleID(), data.getRoleName(), data.getRoleCreateTime(), Long.valueOf(data.getRoleLevel()), data.getRoleLevelUpTime(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void openLoginUI() {
        if (isLoginCustom) {
            Log.d("U8SDK", "current is login custom type. no need to open login ui.");
            return;
        }
        Intent intent = new Intent(U8SDK.getInstance().getContext(), ChooseLoginTypeActivity.class);
        U8SDK.getInstance().getContext().startActivity(intent);
    }

    public void login(int loginType) {

        if (!isPermissioned) {
            Log.d("U8SDK", "login called. but now not permissioned. wait permission to end. login will be called auto");
            lastLoginType = loginType;
            loginAfterPermission = true;
            return;
        }

        if (lastActivityWaithDestroy != null) {
            try {
                Log.d("U8SDK", "call ysdk onCreate before login to switch activity...");
                //PS：这里处理ysdk在多Activity下面账号切换的bug
                //多Activity下， ysdk微信登录之后， 导致QQ登录没有回调。 因为微信登录之后， 会将当前activity设置为第一个activity，而不是当前activity。
                //导致QQ登录的时候， 当前activity是第一个activity而收不到回调。
                //这里onCreate api调用之后，可以将当前activity切换为正确的activity
                YSDKApi.onCreate(U8SDK.getInstance().getContext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lastActivityWaithDestroy = null;
            }
        }

        logined = true;
        lastLoginType = loginType;
        ePlatform platform = getPlatform();
        Log.d("U8SDK", "call login. login platform:" + platform);

        switch (loginType) {
            case LOGIN_TYPE_QQ:
                switch (platform) {
                    case QQ:
                        if (afterLogout) {//QQ已经登录
                            YSDKApi.logout();
                            YSDKApi.login(ePlatform.QQ);
                        } else {
                            letUserLogin(true);
                        }
                        break;
                    case None:
                        YSDKApi.login(ePlatform.QQ);
                        break;
                    default:
                        YSDKApi.logout();
                        YSDKApi.login(ePlatform.QQ);
                        Toast.makeText(U8SDK.getInstance().getContext(), "请重新点击QQ登录", Toast.LENGTH_LONG).show();
                }
                break;

            case LOGIN_TYPE_WX:
                switch (platform) {
                    case WX:
                        if (afterLogout) {//微信已经登录
                            Log.d("U8SDK", "login wx to switch user...");
                            YSDKApi.logout();
                            YSDKApi.login(ePlatform.WX);
                        } else {
                            letUserLogin(true);
                        }
                        break;
                    case None:
                        YSDKApi.login(ePlatform.WX);
                        break;
                    default:
                        YSDKApi.logout();
                        YSDKApi.login(ePlatform.WX);
                        Toast.makeText(U8SDK.getInstance().getContext(), "请重新点击QQ登录", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    //拉去异常号
    public void showDiffLogin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(U8SDK.getInstance().getContext());
        builder.setTitle("异账号提示");
        builder.setMessage("你当前拉起的账号与你本地的账号不一致，请选择使用哪个账号登陆：");
        builder.setPositiveButton("本地账号",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        //Toast.makeText(U8SDK.getInstance().getContext(), "选择使用本地账号", Toast.LENGTH_LONG).show();
                        if (!YSDKApi.switchUser(false)) {
                            U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "");
                        }
                    }
                });
        builder.setNeutralButton("拉起账号",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        //Toast.makeText(U8SDK.getInstance().getContext(), "选择使用拉起账号", Toast.LENGTH_LONG).show();
                        if (!YSDKApi.switchUser(true)) {
                            U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "");
                        }
                    }
                });
        builder.show();
    }

    @SuppressLint("NewApi")
    private boolean isUserLogined() {
        UserLoginRet ret = new UserLoginRet();
        YSDKApi.getLoginRecord(ret);
        return !ret.getAccessToken().isEmpty();
    }

    private static String openId;
    private static String openKey = "";
    private static String pf;
    private static String pfKey;

    // 平台授权成功,让用户进入游戏. 由游戏自己实现登录的逻辑
    public void letUserLogin(final boolean autoLogin) {

        U8SDK.getInstance().runOnMainThread(new Runnable() {

            @Override
            public void run() {
                UserLoginRet ret = new UserLoginRet();
                YSDKApi.getLoginRecord(ret);
                Log.d("U8SDK", "flag: " + ret.flag);
                Log.d("U8SDK", "platform: " + ret.platform);
                if (ret.ret != BaseRet.RET_SUCC) {
                    Log.d("U8SDK", "UserLogin error!!!");
                    if (autoLogin && logined && !isLoginCustom) {
                        openLoginUI();
                        return;
                    } else {
                        //U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed.");
                        openLoginUI();
                        return;
                    }
                }

                YSDKApi.setAntiAddictGameStart();        //开始实名时长统计

                openId = ret.open_id;
                int requesttype = -1; // 0:qq  1:微信
                String accessToken = ret.getAccessToken();
                if (ret.platform == ePlatform.PLATFORM_ID_QQ) {
                    requesttype = 0;
                    openKey = ret.getPayToken();
                } else if (ret.platform == ePlatform.PLATFORM_ID_WX) {
                    requesttype = 1;
                    openKey = accessToken;
                } else if (ret.platform == ePlatform.PLATFORM_ID_GUEST) {
                    requesttype = 3;
                }

                pf = ret.pf;
                pfKey = ret.pf_key;

                JSONObject json = new JSONObject();
                try {
                    json.put("accountType", requesttype);
                    json.put("openid", ret.open_id);
                    json.put("openkey", accessToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (logined) {//说明是登录之后调用的，触发的回调
                    logined = false;
                    U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, json.toString());
                } else {
                    //如果是初始化自动登录回调， 那么这里先缓存， 等客户端调用login接口的时候，直接返回自动登录结果。
                    Log.d("U8SDK", "cache auto login result:" + json.toString());
                    lastLoginResult = json.toString();
                }
            }
        });
    }

    private PayParams payData;

    public void payForItem(PayParams data) {

        PayItem item = new PayItem();
        item.id = data.getProductId();
        item.name = data.getProductName();
        item.desc = data.getProductDesc();
        item.price = data.getPrice() * ratio;
        item.num = 1;

        int resID = ResourceHelper.getIdentifier(U8SDK.getInstance().getContext(), "R.drawable." + coinIconName);
        Bitmap bmp = BitmapFactory.decodeResource(U8SDK.getInstance().getContext().getResources(), resID);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] resData = baos.toByteArray();

        YSDKApi.buyGoods(false, multiServers ? payData.getServerId() : "1", item, appKey, resData, data.getOrderID(), data.getOrderID(), new PayListener() {

            @Override
            public void OnPayNotify(PayRet ret) {

                Log.d("U8SDK", "pay for item result:" + ret.toString());

                if (PayRet.RET_SUCC == ret.ret) {
                    switch (ret.payState) {
                        case PayRet.PAYSTATE_PAYSUCC:
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                            break;
                        case PayRet.PAYSTATE_PAYCANCEL:
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_CANCEL, "pay cancel");
                            break;
                        case PayRet.PAYSTATE_PAYERROR:
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed");
                            break;
                        case PayRet.PAYSTATE_PAYUNKOWN:
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_UNKNOWN, "pay unknown");
                            break;
                    }
                } else {
                    switch (ret.flag) {
                        case eFlag.Login_TokenInvalid:
                            Log.d("U8SDK", "local token invalid. you now to login again.");
                            //U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed");
                            try {
                                YSDKApi.logout();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "");
                            break;

                        case eFlag.Pay_User_Cancle:
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_CANCEL, "pay cancel");
                            break;

                        default:
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed");
                    }
                }

            }
        });

    }

    public void pay(PayParams data) {

        if (payType == 1) {

            if (!useLogin) {
                Log.e("U8SDK", "pay for coin must use login. but now not use login.");
                U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed");
                return;
            }

            payForCoin(data);
        } else {
            payForItem(data);
        }

    }

    public void payForCoin(PayParams data) {
        payData = data;
        QueryReqTask payReqTask = new QueryReqTask(true);
        payReqTask.execute(REQ_TYPE_QUERY + "", payData.getOrderID(), (multiServers ? payData.getServerId() : "1"));
    }

    public void payInternal(final int leftMoney) {

        Log.d("U8SDK", "payInternal Start");

        U8SDK.getInstance().runOnMainThread(new Runnable() {

            @Override
            public void run() {
                try {
                    String zoneID = "1";
                    if (multiServers) {
                        //如果在腾讯后台配置了分区，那么游戏层穿过来的服务器ID，必须和后台配置的一致
                        zoneID = payData.getServerId();
                    }

                    int savedValue = payData.getPrice() * ratio - leftMoney;

                    boolean isCanChange = !fixedPay;
                    int resID = ResourceHelper.getIdentifier(U8SDK.getInstance().getContext(), "R.drawable." + coinIconName);
                    Bitmap bmp = BitmapFactory.decodeResource(U8SDK.getInstance().getContext().getResources(), resID);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] resData = baos.toByteArray();
                    YSDKApi.recharge(zoneID, savedValue + "", isCanChange, resData, payData.getOrderID(), new YSDKCallback());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void chargeWhenPaySuccess() {
        //支付成功向U8Server发送通知，调用查询余额接口并扣费

        if (payData == null) {
            Log.e("U8SDK", "the payData is null");
            return;
        }

        PayReqTask payReqTask = new PayReqTask(true);
        payReqTask.execute(REQ_TYPE_CHARGE + "", payData.getOrderID(), (multiServers ? payData.getServerId() : "1"));
        payData = null;
    }


    private String reqCharge(int reqType, String orderID, String serverID) {

        if (reqType == REQ_TYPE_CHARGE && TextUtils.isEmpty(payUrl)) {
            Log.d("U8SDK", "the pay url is not config");
            return null;
        }

        if (reqType == REQ_TYPE_QUERY && TextUtils.isEmpty(queryUrl)) {
            Log.e("U8SDK", "the query url is not config");
            return null;
        }

        try {

            ePlatform p = getPlatform();
            int accountType = 0;
            if (p == ePlatform.WX) {
                accountType = 1;
            }

            Map<String, String> params = new HashMap<String, String>();
            params.put("orderID", orderID);
            params.put("channelID", U8SDK.getInstance().getCurrChannel() + "");
            params.put("userID", U8SDK.getInstance().getUToken().getUserID() + "");
            params.put("accountType", accountType + "");
            params.put("openID", openId);
            params.put("openKey", openKey);
            params.put("pf", pf);
            params.put("pfkey", pfKey);
            params.put("zoneid", serverID);
            params.put("sign", generateSign(params));

            String url = null;
            if (reqType == REQ_TYPE_QUERY) {
                url = queryUrl;
            } else {
                url = payUrl;
            }

            if (!url.startsWith("http")) {
                url = U8SDK.getInstance().getU8ServerURL() + url;
            }

            String result = U8HttpUtils.httpGet(url, params);

            Log.d("U8SDK", "the pay req to u8server response : " + result);

            return result;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return "";
    }

    private String generateSign(Map<String, String> params) {

        StringBuffer content = new StringBuffer();

        // 按照key做排序
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key) == null ? "" : params.get(key).toString();
            if (value != null) {
                content.append(key + "=" + value);
            } else {
                content.append(key + "=");
            }
        }

        String signData = content.toString();

        signData = signData + U8SDK.getInstance().getAppKey();
        return EncryptUtils.md5(signData).toLowerCase(Locale.getDefault());

    }

    public void executeInstruction(AntiAddictRet ret) {
        final int modal = ret.modal;
        switch (ret.type) {
            case AntiAddictRet.TYPE_TIPS:
            case AntiAddictRet.TYPE_LOGOUT:
                if (!mAntiAddictExecuteState) {
                    mAntiAddictExecuteState = true;
                    AlertDialog.Builder builder = new AlertDialog.Builder(U8SDK.getInstance().getContext());
                    builder.setTitle(ret.title);
                    builder.setMessage(ret.content);
                    builder.setPositiveButton("知道了",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    if (modal == 1) {
                                        // 强制用户下线
                                        Log.d("U8SDK", "user logout because of antiaddict limit...");
                                        U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "");
                                    }
                                    changeExecuteState(false);
                                }
                            });
                    builder.setCancelable(false);
                    builder.show();
                    // 已执行指令
                    YSDKApi.reportAntiAddictExecute(ret, System.currentTimeMillis());
                }

                break;

            case AntiAddictRet.TYPE_OPEN_URL:
                if (!mAntiAddictExecuteState) {
                    mAntiAddictExecuteState = true;
                    View popwindowView = View.inflate(U8SDK.getInstance().getContext(), ResourceHelper.getIdentifier(U8SDK.getInstance().getContext(), "R.layout.pop_window_web_layout"), null);
                    WebView webView = popwindowView.findViewById(ResourceHelper.getIdentifier(U8SDK.getInstance().getContext(), "R.id.pop_window_webview"));
                    Button closeButton = popwindowView.findViewById(ResourceHelper.getIdentifier(U8SDK.getInstance().getContext(), "R.id.pop_window_close"));

                    WebSettings settings = webView.getSettings();
                    settings.setJavaScriptEnabled(true);
                    webView.setWebViewClient(new WebViewClient());
                    webView.loadUrl(ret.url);

                    final PopupWindow popupWindow = new PopupWindow(popwindowView, 1000, 1000);
                    popupWindow.setTouchable(true);
                    popupWindow.setOutsideTouchable(false);
                    popupWindow.setBackgroundDrawable(new BitmapDrawable());

                    closeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (modal == 1) {
                                Log.d("U8SDK", "user logout from antiaddict. modal == 1");
                                U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "");
                            }
                            popupWindow.dismiss();
                            changeExecuteState(false);
                        }
                    });

                    popupWindow.showAtLocation(popwindowView, Gravity.CENTER, 0, 0);
                    // 已执行指令
                    YSDKApi.reportAntiAddictExecute(ret, System.currentTimeMillis());
                }
                break;
        }
    }

    private void changeExecuteState(boolean state) {
        mAntiAddictExecuteState = state;
    }

    static class QueryReqTask extends AsyncTask<String, Void, String> {

        private boolean showTip = false;

        public QueryReqTask(boolean showTip) {
            this.showTip = showTip;
        }

        protected void onPreExecute() {
            if (showTip) {
                U8SDK.getInstance().runOnMainThread(new Runnable() {

                    @Override
                    public void run() {
                        showProgressDialog("正在查询余额，请稍后...");

                    }
                });

            }

        }

        @Override
        protected String doInBackground(String... args) {
            String opType = args[0];
            String orderID = args[1];
            String zoneID = args[2];
            return YSDK.getInstance().reqCharge(Integer.valueOf(opType), orderID, zoneID);
        }

        protected void onPostExecute(final String result) {

            U8SDK.getInstance().runOnMainThread(new Runnable() {

                @Override
                public void run() {
                    hideProgressDialog();
                }
            });

            if (TextUtils.isEmpty(result)) {
                showTip("查询余额失败");
                return;
            }

            try {
                JSONObject json = new JSONObject(result);
                int state = json.getInt("state");

                if (state == 1) {
                    final int money = json.getInt("money");
                    Log.d("U8SDk", "the left money is " + money);
                    if (showTip && money > 0) {
                        //showTip("当前有"+money+"余额，目前需要："+(payData.getPrice() * ratio));
                        final int leftRmby = money / YSDK.getInstance().ratio;
                        if (leftRmby >= YSDK.getInstance().payData.getPrice()) {

                            U8SDK.getInstance().runOnMainThread(new Runnable() {

                                @Override
                                public void run() {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(U8SDK.getInstance().getContext());
                                    builder.setTitle("购买确认");
                                    builder.setMessage(String.format("您当前拥有%s元余额，是否使用余额支付？", leftRmby + ""));
                                    builder.setCancelable(true);
                                    builder.setPositiveButton("确 定",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int whichButton) {

                                                    //确定就用余额支付
                                                    YSDK.getInstance().chargeWhenPaySuccess();

                                                }
                                            });
                                    builder.setNeutralButton("取 消",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int whichButton) {
                                                    //如果取消，就直接支付，不使用余额
                                                    YSDK.getInstance().payInternal(0);
                                                }
                                            });
                                    builder.show();
                                }
                            });
                            return;
                        } else {

                            U8SDK.getInstance().runOnMainThread(new Runnable() {

                                @Override
                                public void run() {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(U8SDK.getInstance().getContext());
                                    builder.setTitle("购买确认");
                                    builder.setMessage(String.format("您当前拥有%s元余额，是否使用这部分余额？", leftRmby + ""));
                                    builder.setCancelable(true);
                                    builder.setPositiveButton("确 定",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int whichButton) {

                                                    //确定就用余额支付
                                                    //chargeWhenPaySuccess();
                                                    YSDK.getInstance().payInternal(money);

                                                }
                                            });
                                    builder.setNeutralButton("取 消",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int whichButton) {
                                                    //如果取消，就直接支付，不使用余额
                                                    YSDK.getInstance().payInternal(0);
                                                }
                                            });
                                    builder.show();
                                }
                            });

                            return;
                        }

                    } else {
                        Log.d("U8SDK", "the query result is " + result);
                        YSDK.getInstance().payInternal(0);
                    }

                }

            } catch (JSONException e) {
                U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, e.getMessage());
                e.printStackTrace();
            }
        }

    }

    static class PayReqTask extends AsyncTask<String, Void, String> {

        private boolean showTip = false;

        public PayReqTask(boolean showTip) {
            this.showTip = showTip;
        }

        protected void onPreExecute() {
            if (showTip) {
                U8SDK.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgressDialog("正在处理,请稍候...");
                    }
                });
            }
        }

        @Override
        protected String doInBackground(String... args) {
            String opType = args[0];
            String orderID = args[1];
            String zoneID = args[2];
            return YSDK.getInstance().reqCharge(Integer.valueOf(opType), orderID, zoneID);
        }

        protected void onPostExecute(final String result) {

            U8SDK.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    hideProgressDialog();
                }
            });

            try {
                JSONObject json = new JSONObject(result);
                int state = json.getInt("state");

                if (state == 1) {
                    U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                    //showTip("支付成功,到账可能稍有延迟");
                } else {
                    U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay fail");
                    //showTip("支付失败,重新登录后,会重新查询并支付");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_UNKNOWN, e.getMessage());
            }

        }

    }

    private static int calculateInSampleSize(BitmapFactory.Options options) {

        int maxWL = 1152;
        int maxHL = 786;

        int maxWP = 668;
        int maxHP = 1272;

        float maxW = maxWP;
        float maxH = maxHP;

        if (YSDK.getInstance().isLandscape) {
            maxW = maxWL;
            maxH = maxHL;
        }

        float originalWidth = options.outWidth;
        float originalHeight = options.outHeight;

        Log.d("U8SDK", "curr original width:" + originalWidth + ";original height:" + originalHeight);

        int inSampleSize = 1;

        if (originalWidth > maxW || originalHeight > maxH) {
            float widthRatio = originalWidth / maxW;
            float heightRatio = originalHeight / maxH;
            inSampleSize = (int) Math.ceil(Math.max(widthRatio, heightRatio));
        }

        Log.d("U8SDK", "curr in sample size:" + inSampleSize);

        return inSampleSize;


    }

    private static void showProgressDialog(String msg) {
        if (loadingActivity != null) {
            return;
        }

        loadingActivity = new ProgressDialog(U8SDK.getInstance().getContext());
        loadingActivity.setIndeterminate(true);
        loadingActivity.setCancelable(false);
        loadingActivity.setMessage(msg);
        loadingActivity.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {

            }
        });
        loadingActivity.show();
    }

    private static void hideProgressDialog() {
        if (loadingActivity == null) {
            return;
        }
        loadingActivity.dismiss();
        loadingActivity = null;
    }

    private static ProgressDialog loadingActivity = null;

    public static void showTip(final String tip) {
        U8SDK.getInstance().runOnMainThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(U8SDK.getInstance().getContext(), tip, Toast.LENGTH_SHORT).show();

            }
        });
    }

    public static boolean isLandscape() {
        Configuration mConfiguration = U8SDK.getInstance().getContext().getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            return true;
        }
        return false;
    }
}
