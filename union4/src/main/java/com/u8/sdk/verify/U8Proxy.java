package com.u8.sdk.verify;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.u8.sdk.PayParams;
import com.u8.sdk.U8SDK;
import com.u8.sdk.log.Log;
import com.u8.sdk.utils.EncryptUtils;
import com.u8.sdk.utils.GUtils;
import com.u8.sdk.utils.Logs;
import com.u8.sdk.utils.U8HttpUtils;

/**
 * U8Server 登录认证和下单相关逻辑
 */
public class U8Proxy {

    /***
     * 去U8Server进行SDK的登录认证，同时获取U8Server返回的token，userID,sdkUserID等信息
     */
    @SuppressLint("DefaultLocale")
    public static UToken auth(String result) {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("appID", U8SDK.getInstance().getAppID() + "");
            params.put("channelID", "" + U8SDK.getInstance().getCurrChannel());
            params.put("subChannelID", "" + U8SDK.getInstance().getSubChannel());    //PS:不加入签名
            params.put("extension", result);
            params.put("sdkVersionCode", U8SDK.getInstance().getSDKVersionCode());
            params.put("deviceID", GUtils.getDeviceID(U8SDK.getInstance().getContext()));

            StringBuilder sb = new StringBuilder();
            sb.append("appID=").append(U8SDK.getInstance().getAppID() + "")
                    .append("channelID=").append(U8SDK.getInstance().getCurrChannel())
                    .append("extension=").append(result).append(U8SDK.getInstance().getAppKey());

            String lowerCase = sb.toString();
//            Logs.i("union", "登录认证签名字符串：" + lowerCase);

            String sign = EncryptUtils.md5(lowerCase);
//            Logs.i("union", "登录认证签名值：" + sign);

            params.put("sign", sign);

            String authResult = U8HttpUtils.httpGet(U8SDK.getInstance().getAuthURL(), params);
            Logs.i("union", "登录认证返回结果：" + authResult);

            return parseAuthResult(authResult);
        } catch (Exception e) {
            Logs.e("union", "u8server auth exception." + e);
            e.printStackTrace();
        }
        return new UToken();
    }

//=================================================================================================================================

    /***
     * 访问U8Server获取订单号
     */
    public static UOrder getOrder(PayParams data) {

        try {

            UToken tokenInfo = U8SDK.getInstance().getUToken();
            if (tokenInfo == null) {
                Logs.e("U8SDK", "用户没有登录，Token is null");
                return null;
            }

            Map<String, String> params = new HashMap<String, String>();
            params.put("userID", tokenInfo == null ? "0" : "" + tokenInfo.getUserID());
            params.put("productID", data.getProductId());
            params.put("productName", data.getProductName());
            params.put("productDesc", data.getProductDesc());
            params.put("money", "" + data.getPrice() * 100);
            params.put("roleID", "" + data.getRoleId());
            params.put("roleName", data.getRoleName());
            params.put("roleLevel", data.getRoleLevel() + "");
            params.put("serverID", data.getServerId());
            params.put("serverName", data.getServerName());
            params.put("extension", data.getExtension());
            params.put("notifyUrl", data.getPayNotifyUrl());

            params.put("signType", "md5");
            String sign = generateSign(tokenInfo, data);
            params.put("sign", sign);
            params.put("sdkVersionCode", U8SDK.getInstance().getSDKVersionCode());

            String orderResult = U8HttpUtils.httpPost(U8SDK.getInstance().getOrderURL(), params);
//            Logs.d("U8SDK", "获取订单结果：" + orderResult);
            return parseOrderResult(data.getProductId(), orderResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static UOrder parseOrderResult(String productID, String orderResult) {
        try {
            JSONObject jsonObj = new JSONObject(orderResult);
            int state = jsonObj.getInt("state");

            if (state != 1) {
                Logs.d("U8SDK", "get order failed. the state is " + state);
                return null;
            }
            JSONObject jsonData = jsonObj.getJSONObject("data");
            return new UOrder(jsonData.getString("orderID"), jsonData.optString("extension", ""), jsonData.optString("productID", productID));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
//=================================================================================================================================


    @SuppressLint("DefaultLocale")
    public static UCheckResult check(PayParams order) {
        try {
            if (order == null)
                return null;

            String t = System.nanoTime() + "";
            Map<String, String> params = new HashMap<String, String>();
            params.put("appID", U8SDK.getInstance().getAppID() + "");
            params.put("channelID", "" + U8SDK.getInstance().getCurrChannel());
            params.put("orderID", order.getOrderID());
            params.put("t", t);

            StringBuilder sb = new StringBuilder();
            sb.append("appID=").append(U8SDK.getInstance().getAppID() + "")
                    .append("channelID=").append(U8SDK.getInstance().getCurrChannel())
                    .append("orderID=").append(order.getOrderID())
                    .append("t=").append(t)
                    .append(U8SDK.getInstance().getAppKey());

            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();

            params.put("sign", sign);

            String result = U8HttpUtils.httpPost(U8SDK.getInstance().getPayCheckURL(), params);

            Logs.d("U8SDK", "pay check sign str:" + sb.toString());
            Logs.d("U8SDK", "pay check the sign is " + sign + " The http post result is " + result);

            if (TextUtils.isEmpty(result)) {
                return null;
            }

            try {
                JSONObject json = new JSONObject(result);
                int suc = json.optInt("suc", 0);
                int state = json.optInt("state", 0);

                return new UCheckResult(suc, state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Logs.e("U8SDK", "u8server check exception." + e);
            e.printStackTrace();
        }
        return null;
    }


    @SuppressLint("DefaultLocale")
    private static String generateSign(UToken token, PayParams data) throws UnsupportedEncodingException {

        StringBuilder sb = new StringBuilder();
        sb.append("userID=").append(token == null ? "0" : token.getUserID() + "").append("&")
                .append("productID=").append(data.getProductId() == null ? "" : data.getProductId()).append("&")
                .append("productName=").append(data.getProductName() == null ? "" : data.getProductName()).append("&")
                .append("productDesc=").append(data.getProductDesc() == null ? "" : data.getProductDesc()).append("&")
                .append("money=").append(data.getPrice() * 100).append("&")
                .append("roleID=").append(data.getRoleId() == null ? "" : data.getRoleId()).append("&")
                .append("roleName=").append(data.getRoleName() == null ? "" : data.getRoleName()).append("&")
                .append("roleLevel=").append(data.getRoleLevel()).append("&")
                .append("serverID=").append(data.getServerId() == null ? "" : data.getServerId()).append("&")
                .append("serverName=").append(data.getServerName() == null ? "" : data.getServerName()).append("&")
                .append("extension=").append(data.getExtension() == null ? "" : data.getExtension());

        //这里是游戏服务器自己的支付回调地址，可以在下单的时候， 传给u8server。
        //u8server 支付成功之后， 会优先回调这个地址。 如果不传， 则需要在u8server后台游戏管理中配置游戏服务器的支付回调地址

        //如果传notifyUrl，则notifyUrl参与签名
        if (!TextUtils.isEmpty(data.getPayNotifyUrl())) {
            sb.append("&notifyUrl=").append(data.getPayNotifyUrl());
        }

        sb.append(U8SDK.getInstance().getAppKey());

        String encoded = URLEncoder.encode(sb.toString(), "UTF-8");    //url encode

        Logs.d("U8SDK", "The encoded getOrderID sign is " + encoded);

        //这里用md5方式生成sign
        String sign = EncryptUtils.md5(encoded).toLowerCase();

        //如果签名方式是RSA，走下面方式
        //String privateKey = U8SDK.getInstance().getPayPrivateKey();
        //String sign = RSAUtils.sign(encoded, privateKey, "UTF-8", "SHA1withRSA");

        Logs.d("U8SDK", "The getOrderID sign is " + sign);
        return sign;
    }


    private static UToken parseAuthResult(String authResult) {

        if (authResult == null || TextUtils.isEmpty(authResult)) {
            return new UToken();
        }

        try {
            JSONObject jsonObj = new JSONObject(authResult);
            int state = jsonObj.getInt("state");

            if (state != 1) {
                Logs.d("U8SDK", "auth failed. the state is " + state);
                return new UToken();
            }

            JSONObject jsonData = jsonObj.getJSONObject("data");

            return new UToken(
                    jsonData.getInt("userID")
                    , jsonData.getString("sdkUserID")
                    , jsonData.getString("username")
                    , jsonData.getString("sdkUserName")
                    , jsonData.getString("token")
                    , jsonData.getString("extension")
                    , jsonData.optString("timestamp"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new UToken();
    }

    //、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、
//、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、
//、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、
//、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、
//、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、
    /**
     * 单机
     */
    public static boolean completePay(PayParams order) {

        try {
            if (order == null) return false;
            String t = System.nanoTime() + "";

            Map<String, String> params = new HashMap<String, String>();
            params.put("appID", U8SDK.getInstance().getAppID() + "");
            params.put("channelID", "" + U8SDK.getInstance().getCurrChannel());
            params.put("orderID", order.getOrderID());
            params.put("channelOrderID", order.getChannelOrderID() == null ? "" : order.getChannelOrderID());
            params.put("t", t);

            StringBuilder sb = new StringBuilder();
            sb.append("appID=").append(U8SDK.getInstance().getAppID() + "")
                    .append("channelID=").append(U8SDK.getInstance().getCurrChannel())
                    .append("orderID=").append(order.getOrderID())
                    .append("channelOrderID=").append(order.getChannelOrderID() == null ? "" : order.getChannelOrderID())
                    .append("t=").append(t)
                    .append(U8SDK.getInstance().getAppKey());

            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();

            params.put("sign", sign);

            String result = U8HttpUtils.httpPost(U8SDK.getInstance().getPayCompleteURL(), params);

            Log.d("U8SDK", "completePay sign str:" + sb.toString());
            Log.d("U8SDK", "completePay sign is " + sign);
            Log.d("U8SDK", "completePay result is " + result);

            if ("SUCCESS".equalsIgnoreCase(result)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e("U8SDK", "u8server completePay exception.", e);
            e.printStackTrace();
        }
        return false;
    }

}
