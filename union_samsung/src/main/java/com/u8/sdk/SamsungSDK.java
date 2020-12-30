package com.u8.sdk;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jumper.samsungaccount.IAccountListener;
import com.jumper.samsungaccount.SamsungAccount;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class SamsungSDK {

    private final int REQ_PERMISSION_T = 20201221;

    private static SamsungSDK instance;

    private SamsungAccount mSA = new SamsungAccount();

    private String appID;
    private String cpID;
    private String publicKey;
    private String gameName;
    private String currUserID;

    private Integer age = 0;

    private Map<String, String> goods;

    private SamsungSDK() {
        goods = new HashMap<>();
    }

    public static SamsungSDK getInstance() {
        if (instance == null) {
            instance = new SamsungSDK();
        }
        return instance;
    }

    public void initSDK(SDKParams data) {
        try {
            appID = data.getString("SM_APP_ID");
            cpID = data.getString("SM_CP_ID");
            publicKey = data.getString("SM_PUBLIC_KEY");
            gameName = data.getString("SM_GAME_NAME");
            boolean debugMode = data.getBoolean("SM_DEBUG_MODE");
            int urlModeInt = 0;

            if (debugMode) {
                urlModeInt = 1;
            }

            SamsungAccount.setUrlMode(urlModeInt);
            SamsungAccount.setPaymentMode(urlModeInt);

            loadGoods();

            U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

                @Override
                public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
                    if ((ContextCompat.checkSelfPermission(U8SDK.getInstance().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                            || (ContextCompat.checkSelfPermission(U8SDK.getInstance().getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
                            || (ContextCompat.checkSelfPermission(U8SDK.getInstance().getContext(), Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(U8SDK.getInstance().getContext(), "SDK运行必须拥有存储和设备信息权限，您可以稍后在设置中开启", Toast.LENGTH_SHORT).show();
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "sdk login failed. no permission");
                    } else {
                        doLogin();
                    }
                }
            });
            U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_SUCCESS, "sdk init success");
        } catch (Exception e) {
            e.printStackTrace();
            U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_FAIL, "sdk init failed with exception.");
        }
    }


    public void login() {
        //权限检查处理
        if ((ContextCompat.checkSelfPermission(U8SDK.getInstance().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(U8SDK.getInstance().getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(U8SDK.getInstance().getContext(), Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(U8SDK.getInstance().getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.GET_ACCOUNTS}, REQ_PERMISSION_T);
        } else {
            doLogin();
        }
    }

    public void doLogin() {
        try {

            // mActivity: 当前activity实例
            // szClientID: 用户id，从Samsung（pengtai）平台获取
            // szClientSecret: 用户secret，从Samsung（pengtai）平台获取
            // szApplyNo: 应用编号
            // IAccountListener: 登录结果回调接口
            // 返回值： 0表示调用成功， 其他表示调用失败；


            mSA.loginEnhance(U8SDK.getInstance().getContext(), appID, new IAccountListener() {
                @Override
                public void onResult(Map<ResultItem, Object> result) {
                    // 获取登录回调结果
                    String szErrorCode = (String) result.get(ResultItem.ErrorCode);
                    // 登录成功
                    if (szErrorCode != null && szErrorCode.compareTo(ErrorCode.OK.toString()) == 0) {
                        // 获取Uid, 支付、登入/登出等都用此Uid
                        String szUserID = (String) result.get(ResultItem.Uid);
                        // 获取用户名
                        String szUserName = (String) result.get(ResultItem.UserName);

                        age = (Integer) result.get(ResultItem.UserAge);

                        // 保存ID，为后续支付等准备
                        currUserID = szUserID;

                        try {
                            JSONObject json = new JSONObject();
                            json.put("uid", szUserID);
                            json.put("username", szUserName);
                            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, json.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {   // 登录失败   // 显示登录失败原因
                        String szResult = String.format("Login failed(%s):%s", szErrorCode, String.valueOf(result.get(ResultItem.ErrorMessage)));
                        Log.e("U8SDK", "sdk login failed." + szResult);
                        U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "sdk login failed");
                    }
                }
            }, null);

            int iRet = mSA.login(U8SDK.getInstance().getContext(), appID, new IAccountListener() {
                @Override
                public void onResult(Map<ResultItem, Object> result) {
                    // 获取登录回调结果
                    String szErrorCode = (String) result.get(ResultItem.ErrorCode);
                    Log.d("U8SDK", "sdk login result. code:" + szErrorCode);
                    // 登录成功
                    if (szErrorCode != null && szErrorCode.compareTo(ErrorCode.OK.toString()) == 0) {
                        // 获取Uid, 支付、登入/登出等都用此Uid
                        String szUserID = (String) result.get(ResultItem.Uid);
                        // 获取用户名
                        String szUserName = (String) result.get(ResultItem.UserName);

                        age = (Integer) result.get(ResultItem.UserAge);

                        // 保存ID，为后续支付等准备
                        currUserID = szUserID;

                        try {
                            JSONObject json = new JSONObject();
                            json.put("uid", szUserID);
                            json.put("username", szUserName);        //空
                            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, json.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {    // 登录失败    // 显示登录失败原因
                        String szResult = String.format("Login failed(%s):%s", szErrorCode, String.valueOf(result.get(ResultItem.ErrorMessage)));
                        Log.e("U8SDK", "sdk login failed." + szResult);
                        U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "sdk login failed");

                    }
                }
            }, null);

            if (iRet != 0) {
                // 登录接口调用失败，显示失败错误码
                Log.e("U8SDK", "sdk call login return failed. iRet:" + iRet);
                U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "sdk login failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "sdk login failed with exception");
        }
    }

    public void pay(PayParams data) {
        try {

            // 商品编号, 从后台查询
            String szGoodNo = getGoodInfo(data.getProductId());

            Log.d("U8SDK", "sdk pay begin. goodNo:" + szGoodNo);

            // 格式化json字符串, 只有金额为数字，其他为字符串
            // mUserID 为登录时获取的 Uid
            String szPayJson = String.format("{\"uId\":\"%s\",\"goodNo\":\"%s\",\"applyNo\":\"%s\",\"merchNo\":\"%s\",\"packageName\":\"%s\",\"publicKey\":\"%s\",\"tranNo\":\"%s\",\"amount\":%s, \"transmit\":%s}"
                    , currUserID, szGoodNo, appID, cpID, U8SDK.getInstance().getContext().getPackageName(), publicKey, data.getOrderID(), data.getPrice(), data.getOrderID());

            Log.d("U8SDK", "sdk pay begin. pay json:" + szPayJson);

            // 调用支付接口
            // mActivity: 当前 activity
            // szPayJson: 支付参数字符串
            // IAccountListener: 支付结果回调接口
            // UserData:	用户的回调数据，支付结果回调时可通过IAccountListener.ResultItem.UserData获取
            int iRet = mSA.pay(U8SDK.getInstance().getContext(), szPayJson, new IAccountListener() {

                @Override
                public void onResult(Map<ResultItem, Object> result) {

                    // 获取支付结果
                    String szErrorCode = (String) result.get(ResultItem.ErrorCode);

                    Log.d("U8SDK", "sdk pay result. code:" + szErrorCode);

                    if (szErrorCode != null && szErrorCode.compareTo(ErrorCode.OK.toString()) == 0) {
                        // 支付成功

                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "sdk pay success");
                    } else {
                        // 支付失败并获取错误码及错误信息
                        String szResult = String.format("Pay failed(%s):%s", szErrorCode, String.valueOf(result.get(ResultItem.ErrorMessage)));
                        Log.e("U8SDK", "sdk pay failed." + szResult);
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed");
                    }
                }
            }, null);

            if (iRet != SamsungAccount.ReturnCode.Success.ordinal()) {
                // 支付接口调用失败，显示失败错误码
                Log.e("U8SDK", "sdk call pay failed. iRet:" + iRet);
                U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed with exception.");
        }
    }


    /*
     * 登入记录示例
     */
    private void doLoginRecord() {
        // mUserID: 登录时获取的 Uid
        // mApplyNo: 应用编号
        // mApplyName: 应用名称
        // oper: Login为登入, Logout为登出
        // IAccountListener： 回调接口
        // 返回值： 0表示调用成功， 其他表示调用失败
        int iRet = mSA.record(currUserID, appID, gameName, SamsungAccount.RecordOperation.Login, new IAccountListener() {
            @Override
            public void onResult(Map<ResultItem, Object> result) {
                // 获取Uid
                String szUid = (String) result.get(ResultItem.Uid);

                // 获取回调结果
                String szErrorCode = (String) result.get(ResultItem.ErrorCode);
                if (szErrorCode != null && szErrorCode.compareTo(ErrorCode.OK.toString()) == 0) {
                    // 登入成功，显示回调结果

                } else {
                    String szResult = String.format("Uid:%s, login record failed:%s", szUid, String.valueOf(result.get(ResultItem.ErrorMessage)));
                    Log.e("U8SDK", "doLoginRecord failed." + szResult);
                }
            }
        }, null);

        if (iRet != SamsungAccount.ReturnCode.Success.ordinal()) {
            // 登入接口调用失败，显示失败错误码
            Log.e("U8SDK", "doLoginRecord call failed. iRet:" + iRet);
        }
    }

    /*
    登出记录示例
     */
    private void doLogoutRecord() {
        // mUserID: 登录时获取的 Uid
        // mApplyNo: 应用编号
        // mApplyName: 应用名称
        // oper: Login为登入, Logout为登出
        // IAccountListener： 回调接口
        // 返回值： 0表示调用成功， 其他表示调用失败
        int iRet = mSA.record(currUserID, appID, gameName, SamsungAccount.RecordOperation.Logout, new IAccountListener() {
            @Override
            public void onResult(Map<ResultItem, Object> result) {
                // 获取用户 Uid
                String szUid = (String) result.get(ResultItem.Uid);

                // 获取回调结果
                String szErrorCode = (String) result.get(ResultItem.ErrorCode);
                if (szErrorCode != null && szErrorCode.compareTo(ErrorCode.OK.toString()) == 0) {
                    // 登出成功，显示回调结果
                    String szResult = String.format("Uid:%s, logout record ok", szUid);

                } else {
                    // 登出失败，显示回调结果
                    String szResult = String.format("Uid:%s, logout record failed:%s", szUid, String.valueOf(result.get(ResultItem.ErrorMessage)));
                    Log.e("U8SDK", "doLogoutRecord failed." + szResult);
                }
            }
        }, null);

        if (iRet != SamsungAccount.ReturnCode.Success.ordinal()) {
            // 登出接口调用失败，显示失败错误码
            Log.e("U8SDK", "doLogoutRecord failed. iRet:" + iRet);

        }
    }

    // iEvent: 1: 登入公告； 2：登出公告
    private void doShowAnnouncement(int iEvent) {
        // Show announcement
        mSA.showAnnouncement(U8SDK.getInstance().getContext(), appID, iEvent, new IAccountListener() {
            @Override
            public void onResult(Map<ResultItem, Object> result) {
                Log.d("U8SDK", "sdk doShowAnnouncement called result.");
            }
        }, new Integer(iEvent));
    }


    private String getGoodInfo(String productID) {

        if (this.goods.containsKey(productID)) {
            return this.goods.get(productID);
        }
        return productID;
    }

    public void loadGoods() {

        String xmlGoods = SDKTools.getAssetConfigs(U8SDK.getInstance().getContext(), "samsung_pay.xml");

        if (xmlGoods == null) {
            Log.e("U8SDK", "fail to load samsung_pay.xml");
            return;
        }

        Log.d("U8SDK", "The samsung_pay Str:" + xmlGoods);

        XmlPullParser parser = Xml.newPullParser();

        try {
            parser.setInput(new StringReader(xmlGoods));

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String tag = parser.getName();
                        if ("good".equals(tag)) {
                            String productId = parser.getAttributeValue(null, "productID");
                            String wareid = parser.getAttributeValue(null, "waresid");

                            if (!this.goods.containsKey(productId)) {
                                this.goods.put(productId, wareid);
                            } else {
                                Log.e("U8SDK", "Curr Good: " + productId + " has duplicated.");
                            }

                            Log.d("U8SDK", "Curr Good: " + productId + "; waresid:" + wareid);
                        }
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void queryRealName() {
        U8SDK.getInstance().onResult(U8Code.CODE_ADDICTION_ANTI_RESULT, age + "");
    }
}
