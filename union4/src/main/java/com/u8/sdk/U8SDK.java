package com.u8.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.multidex.MultiDex;

import com.u8.sdk.analytics.UDAgent;
import com.u8.sdk.base.IActivityCallback;
import com.u8.sdk.base.ISDKListener;
import com.u8.sdk.base.PluginFactory;
import com.u8.sdk.log.Log;
import com.u8.sdk.permission.IAutoPermissionListener;
import com.u8.sdk.permission.U8AutoPermission;
import com.u8.sdk.plugin.U8Ad;
import com.u8.sdk.plugin.U8Analytics;
import com.u8.sdk.plugin.U8Download;
import com.u8.sdk.plugin.U8Pay;
import com.u8.sdk.plugin.U8Push;
import com.u8.sdk.plugin.U8Share;
import com.u8.sdk.plugin.U8User;
import com.u8.sdk.utils.Logs;
import com.u8.sdk.verify.U8Proxy;
import com.u8.sdk.verify.UToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class U8SDK {

    private static final String DEFAULT_PKG_NAME = "com.u8.sdk";
    private static final String APP_PROXY_NAME = "U8_APPLICATION_PROXY_NAME";
    private static final String APP_GAME_NAME = "U8_Game_Application";

    private static U8SDK instance;

    private Application application;
    private Activity activity;
    private Handler handler;

    private SDKParams developInfo;
    private Bundle bundle;

    private List<ISDKListener> listeners;

    private List<IActivityCallback> activityCallbacks;

    private List<IApplicationListener> applicationListeners;

    private String sdkUserID = null;
    private UToken tokenData = null;

    public U8SDK() {
        handler = new Handler(Looper.getMainLooper());
        listeners = new ArrayList<ISDKListener>();
        activityCallbacks = new ArrayList<IActivityCallback>(1);
        applicationListeners = new ArrayList<IApplicationListener>(2);
    }

    public static U8SDK getInstance() {
        if (instance == null) {
            instance = new U8SDK();
        }
        return instance;
    }

    public SDKParams getSDKParams() {
        return developInfo;
    }

    public Bundle getMetaData() {
        return bundle;
    }

    /**
     * 获取子渠道号
     *
     * @return
     */
    public int getSubChannel() {
        if (this.developInfo == null || !this.developInfo.contains("U8_Sub_Channel")) {
            return 0;
        } else {
            return this.developInfo.getInt("U8_Sub_Channel");
        }
    }

    /**
     * 获取当前SDK对应的渠道号
     */
    public int getCurrChannel() {
        if (this.developInfo == null || !this.developInfo.contains("U8_Channel")) {
            return 0;
        } else {
            return this.developInfo.getInt("U8_Channel");
        }
    }

    public int getAppID() {
        if (this.developInfo == null || !this.developInfo.contains("U8_APPID")) {
            return 0;
        }
        return this.developInfo.getInt("U8_APPID");
    }

    public String getAppKey() {
        if (this.developInfo == null || !this.developInfo.contains("U8_APPKEY")) {
            return "";
        }
        return this.developInfo.getString("U8_APPKEY");
    }

    public String getPayPrivateKey() {
        if (this.developInfo == null || !this.developInfo.contains("U8_PAY_PRIVATEKEY")) {
            return "";
        }
        return this.developInfo.getString("U8_PAY_PRIVATEKEY");
    }

    //是否是单机游戏
    public boolean isSingleGame() {
        if (this.developInfo == null || !this.developInfo.contains("U8_SINGLE_GAME")) {
            return false;
        }
        return this.developInfo.getBoolean("U8_SINGLE_GAME");
    }

    // 是否走登录验证
    public boolean isAuth() {
        return getAuthURL() != null;
    }

    // 是否客户端下单
    public boolean isGetOrder() {
        return getOrderURL() != null;
    }

    public String getOrderURL() {
        if (this.developInfo == null) {
            return null;
        }
        if (this.developInfo.contains("U8_ORDER_URL")) {
            return this.developInfo.getString("U8_ORDER_URL");
        }

        String baseUrl = getU8ServerURL();
        if (baseUrl == null) {
            return null;
        }
        return baseUrl + "/pay/getOrderID";
    }

    public String getAuthURL() {
        if (this.developInfo == null) {
            return null;
        }

        if (this.developInfo.contains("U8_AUTH_URL")) {
            return this.developInfo.getString("U8_AUTH_URL");
        }

        String baseUrl = getU8ServerURL();
        if (baseUrl == null) {
            return null;
        }
        return baseUrl + "/user/getToken";
    }

    public String getAnalyticsURL() {
        if (this.developInfo == null) {
            return null;
        }

        if (this.developInfo.contains("U8_ANALYTICS_URL")) {
            return this.developInfo.getString("U8_ANALYTICS_URL");
        }

        String baseUrl = getU8ServerURL();
        if (baseUrl == null) {
            return null;
        }
        return baseUrl + "/user";
    }

    public String getPayCompleteURL() {
        if (this.developInfo == null) {
            return null;
        }

        String baseUrl = getU8ServerURL();
        if (baseUrl == null) {
            return null;
        }
        return baseUrl + "/pay/complete";
    }

    public String getPayCheckURL() {
        if (this.developInfo == null) {
            return null;
        }

        String baseUrl = getU8ServerURL();
        if (baseUrl == null) {
            return null;
        }
        return baseUrl + "/pay/check";
    }

    // 获取u8server跟地址
    public String getU8ServerURL() {
        if (this.developInfo == null || !this.developInfo.contains("U8SERVER_URL")) {
            return null;
        }
        String url = this.developInfo.getString("U8SERVER_URL");
        if (url == null || url.trim().length() == 0) {
            return null;
        }

        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    // 是否使用u8server统计功能
    public boolean isUseU8Analytics() {
        if (this.developInfo == null || !this.developInfo.contains("U8_ANALYTICS")) {
            return false;
        }
        String use = this.developInfo.getString("U8_ANALYTICS");
        return "true".equalsIgnoreCase(use);
    }

    // 当前渠道SDK是否需要显示闪屏
    public boolean isSDKShowSplash() {
        if (this.developInfo == null || !this.developInfo.contains("U8_SDK_SHOW_SPLASH")) {
            return false;
        }
        String show = this.developInfo.getString("U8_SDK_SHOW_SPLASH");
        return "true".equalsIgnoreCase(show);
    }

    // 获取当前渠道SDK的版本号
    public String getSDKVersionCode() {
        if (this.developInfo == null || !this.developInfo.contains("U8_SDK_VERSION_CODE")) {
            return "";
        }
        return this.developInfo.getString("U8_SDK_VERSION_CODE");
    }

    // 获取当前渠道SDK的版本名称
    public String getSDKVersionName() {
        if (this.developInfo == null || !this.developInfo.contains("U8_SDK_VERSION_NAME")) {
            return "";
        }
        return this.developInfo.getString("U8_SDK_VERSION_NAME");
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////

    public void setSDKListener(ISDKListener listener) {
        if (!listeners.contains(listener) && listener != null) {
            this.listeners.add(listener);
        }
    }

    public void setActivityCallback(IActivityCallback callback) {
        if (!this.activityCallbacks.contains(callback) && callback != null) {
            this.activityCallbacks.add(callback);
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////

    public Application getApplication() {
        return this.application;
    }

    public String getSDKUserID() {
        return this.sdkUserID;
    }

    public UToken getUToken() {
        return this.tokenData;
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////

    public void onAppCreate(Application application) {
        this.application = application;
        for (IApplicationListener lis : applicationListeners) {
            lis.onProxyCreate();
        }
    }


    public void onAppCreateAll(Application application) {
        for (IApplicationListener lis : applicationListeners) {
            if (lis instanceof IFullApplicationListener) {
                ((IFullApplicationListener) lis).onProxyCreateAll();
            }
        }
    }

    public void onAppAttachBaseContext(Application application, Context context) {
        this.application = application;
        try {
            MultiDex.install(context);
            Log.init(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        applicationListeners.clear();

        try {
            PluginFactory.getInstance().loadPluginInfo(context);
            developInfo = PluginFactory.getInstance().getSDKParams(context);
            bundle = PluginFactory.getInstance().getMetaData(context);
            if (bundle.containsKey(APP_PROXY_NAME)) {
                String proxyAppNames = bundle.getString(APP_PROXY_NAME);
                String[] proxyApps = proxyAppNames.split(",");
                for (String proxy : proxyApps) {
                    if (!TextUtils.isEmpty(proxy)) {
                        Logs.d("U8SDK", "add a new application listener:" + proxy);
                        IApplicationListener listener = newApplicationInstance(application, proxy);
                        if (listener != null) {
                            applicationListeners.add(listener);
                        }
                    }
                }
            }

            if (bundle.containsKey(APP_GAME_NAME)) {
                String gameAppName = bundle.getString(APP_GAME_NAME);
                IApplicationListener listener = newApplicationInstance(application, gameAppName);
                if (listener != null) {
                    Logs.e("U8SDK", "add a game application listener:" + gameAppName);
                    applicationListeners.add(listener);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (IApplicationListener lis : applicationListeners) {
            lis.onProxyAttachBaseContext(context);
        }
    }

    public void onAppConfigurationChanged(Application application, Configuration newConfig) {
        for (IApplicationListener lis : applicationListeners) {
            lis.onProxyConfigurationChanged(newConfig);
        }
    }

    public void onTerminate() {
        for (IApplicationListener lis : applicationListeners) {
            lis.onProxyTerminate();
        }
        Log.destory();
    }

    @SuppressWarnings("rawtypes")
    private IApplicationListener newApplicationInstance(Application application, String proxyAppName) {

        if (proxyAppName == null || SDKTools.isNullOrEmpty(proxyAppName)) {
            return null;
        }

        if (proxyAppName.startsWith(".")) {
            proxyAppName = DEFAULT_PKG_NAME + proxyAppName;
        }

        try {
            Class clazz = Class.forName(proxyAppName);
            return (IApplicationListener) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 游戏调用抽象层的时候，需要在Activity的onCreate方法中调用该方法
     */
    public void init(Activity activity_) {
        this.activity = activity_;
        if (U8AutoPermission.getInstance().isDirectPermission()) {
            U8AutoPermission.getInstance().requestDangerousPermissions(activity, new IAutoPermissionListener() {

                @Override
                public void onAutoPermissionSuccess() {
                    doInit(activity);
                }

                @Override
                public void onAutoPermissionFailed(String[] deniedPermissions, String[] deniedForeverPermissions) {
                    Logs.e("U8SDK", "permission request failed with auto permission.");
                    doInit(activity);
                }
            });
        } else {
            doInit(activity);
        }
    }

    public void doInit(Activity activity) {
        this.activity = activity;
        try {
            if (isUseU8Analytics()) {
                UDAgent.getInstance().init(activity);
            }
            Logs.d("U8SDK", "begin init u8sdk");
            U8User.getInstance().init();
            U8Pay.getInstance().init();
            U8Push.getInstance().init();
            U8Share.getInstance().init();
            U8Analytics.getInstance().init();
            U8Download.getInstance().init();
            U8Ad.getInstance().init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runOnMainThread(Runnable runnable) {
        if (handler != null) {
            handler.post(runnable);
            return;
        }

        if (activity != null) {
            activity.runOnUiThread(runnable);
        }
    }

    public void setContext(Activity activity) {
        this.activity = activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Activity getContext() {
        return this.activity;
    }

    public Activity getActivity() {
        return this.activity;
    }

    public List<ISDKListener> getAllListeners() {
        return listeners;
    }


//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public void onBackPressed() {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onBackPressed();
            }
        }
    }

    public void onCreate() {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onCreate();
            }
        }
    }

    public void onStart() {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onStart();
            }
        }
    }

    public void onPause() {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onPause();
            }
        }
    }

    public void onResume() {

        if (U8AutoPermission.getInstance().isDirectPermission()) {
            // 权限申请跳转回来， 判断是否继续申请权限，还是初始化
            if (U8AutoPermission.getInstance().isJumpingPermission()) {
                Logs.d("U8SDK", "permission return from permission page. request again to check permission");
                U8AutoPermission.getInstance().requestDangerousPermissions(U8SDK.getInstance().getContext(),
                        new IAutoPermissionListener() {

                            @Override
                            public void onAutoPermissionSuccess() {
                                doInit(U8SDK.getInstance().getContext());
                            }

                            @Override
                            public void onAutoPermissionFailed(String[] deniedPermissions, String[] deniedForeverPermissions) {
                                Logs.e("U8SDK", "u8sdk with auto permission failed.");
                                doInit(U8SDK.getInstance().getContext());
                            }
                        });
                return;
            } else if (U8AutoPermission.getInstance().isJumpingWriteSettings()) {
                Logs.d("U8SDK", "permission return from write settings page. request again to check permission");
                U8AutoPermission.getInstance().requestWriteSettings(U8SDK.getInstance().getContext());
                return;
            }
        }

        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onResume();
            }
        }
    }

    public void onNewIntent(Intent newIntent) {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onNewIntent(newIntent);
            }
        }
    }

    public void onStop() {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onStop();
            }
        }
    }

    public void onDestroy() {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onDestroy();
            }
        }
    }

    public void onRestart() {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onRestart();
            }
        }
    }

    public void attachBaseContext(Activity activity, Context Context) {
        this.activity = activity;
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.attachBaseContext(Context);
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onConfigurationChanged(newConfig);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (U8AutoPermission.getInstance().isDirectPermission()) {
            U8AutoPermission.getInstance().onRequestPermissionsResult(activity, requestCode, permissions, grantResults);
        }

        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onResult(final int code, final String result) {
        Logs.d("U8SDK", "onResult in U8SDK. code:" + code + ";result:" + result);
        for (ISDKListener listener : listeners) {
            listener.onResult(code, result);
        }
    }

    public void onInitResult(int code, String result) {
        Logs.d("U8SDK", "InitResult in U8SDK. code:" + code + ";result:" + result);
        for (ISDKListener listener : listeners) {
            listener.onInitResult(code, result);
        }
    }

    public void onLoginResult(int code, String result) {
        Logs.d("U8SDK", "onLoginResult in U8SDK. code:" + code + ";result:" + result);
        for (ISDKListener listener : listeners) {
            listener.onLoginResult(code, result);
        }

        if (isAuth()) {
            startAuthTask(result);
        }
    }

    public void onSwitchAccount(int code, String result) {
        Logs.d("U8SDK", "onSwitchAccount in U8SDK. code:" + code + ";result:" + result);
        for (ISDKListener listener : listeners) {
            listener.onSwitchAccount(code, result);
        }
    }

    public void onLogoutResult(int code, String result) {
        Logs.d("U8SDK", "onLogoutResult in U8SDK. code:" + code + ";result:" + result);
        for (ISDKListener listener : listeners) {
            listener.onLogoutResult(code, result);
        }
    }

    public void onExitResult(int code, String result) {
        Logs.d("U8SDK", "onExitResult in U8SDK. code:" + code + ";result:" + result);
        for (ISDKListener listener : listeners) {
            listener.onExitResult(code, result);
        }
    }

    public void onSinglePayResult(final int code,final String result) {
        if (U8SDK.getInstance().isSingleGame()) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    U8Single.getInstance().handleResult(code, result);
                }
            });
        }
    }

    public void onPayResult(int code, String result) {
        Logs.d("U8SDK", "onPayResult in U8SDK. code:" + code + ";result:" + result);
        for (ISDKListener listener : listeners) {
            listener.onPayResult(code, result);
        }
    }



    private void onAuthResult(UToken token) {
        if (token.isSuc()) {
            this.sdkUserID = token.getSdkUserID();
            this.tokenData = token;
        }

        for (ISDKListener listener : listeners) {
            listener.onAuthResult(token);
        }
    }

    // 默认的AsyncTask的执行顺序可能会有些影响，导致队列中的任务并不能被及时执行
    private void startAuthTask(String result) {
        Logs.d("U8SDK", "begin to startAuthTask...");
        AuthTask authTask = new AuthTask();
        if (Build.VERSION.SDK_INT >= 11) { // Build.VERSION_CODES.HONEYCOMB
            authTask.executeOnExecutor(Executors.newCachedThreadPool(), result);
        } else {
            authTask.execute(result);
        }
    }


    class AuthTask extends AsyncTask<String, Void, UToken> {

        @Override
        protected UToken doInBackground(String... args) {
            String result = args[0];
            Logs.d("union", "开始登陆认证...");
            UToken token = U8Proxy.auth(result);
            return token;
        }

        protected void onPostExecute(UToken token) {
            try {
                onAuthResult(token);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
