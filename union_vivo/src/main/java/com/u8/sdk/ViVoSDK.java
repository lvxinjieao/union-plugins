package com.u8.sdk;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.u8.sdk.utils.EncryptUtils;
import com.u8.sdk.utils.U8HttpUtils;
import com.u8.sdk.vivo.HTTPSTrustManager;
import com.u8.sdk.vivo.VivoSignUtils;
import com.vivo.unionsdk.open.OrderResultEventHandler;
import com.vivo.unionsdk.open.OrderResultInfo;
import com.vivo.unionsdk.open.QueryOrderCallback;
import com.vivo.unionsdk.open.VivoAccountCallback;
import com.vivo.unionsdk.open.VivoConstants;
import com.vivo.unionsdk.open.VivoExitCallback;
import com.vivo.unionsdk.open.VivoPayCallback;
import com.vivo.unionsdk.open.VivoPayInfo;
import com.vivo.unionsdk.open.VivoQueryOrderInfo;
import com.vivo.unionsdk.open.VivoRealNameInfoCallback;
import com.vivo.unionsdk.open.VivoRoleInfo;
import com.vivo.unionsdk.open.VivoUnionSDK;
import com.vivo.unionsdk.open.VivoConstants.JumpType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ViVoSDK {

    public final static String KEY_LOGIN_RESULT = "LoginResult";
    public final static String KEY_SWITCH_ACCOUNT = "switchAccount";
    public final static int REQ_CODE_LOGIN = 1000;
    public final static int REQ_CODE_SWITCH = 1001;
    public final static int REQ_CODE_PAY = 1002;

    private static ViVoSDK instance;

    private String cpID;
    private String appID;
    private String appKey;

    private String openID;

    private ViVoSDK() {

    }

    public static ViVoSDK getInstance() {
        if (instance == null) {
            instance = new ViVoSDK();
        }
        return instance;
    }

    public void initSDK(SDKParams params) {
        try {
            this.cpID = params.getString("Vivo_CpID");
            this.appID = params.getString("Vivo_AppID");
            this.appKey = params.getString("Vivo_AppKey");

            HTTPSTrustManager.allowAllSSL();

            VivoUnionSDK.initSdk(U8SDK.getInstance().getApplication(), this.appID, false);

            Log.i("U8SDK", "cpID:" + cpID);
            Log.i("U8SDK", "appID:" + appID);
            Log.i("U8SDK", "appKey:" + appKey);

            VivoUnionSDK.registerOrderResultEventHandler(new OrderResultEventHandler() {
                @Override
                public void process(OrderResultInfo orderResultInfo) {
                    Log.i("U8SDK", "vivo sdk order process result: " + orderResultInfo.toString());
                    queryOrderResult(orderResultInfo.getCpOrderNumber(), orderResultInfo.getTransNo(),
                            orderResultInfo.getProductPrice(), new QueryOrderCallback() {
                                @Override
                                public void onResult(int i, OrderResultInfo orderResultInfo) {

                                    if (orderResultInfo != null) {
                                        Log.d("U8SDK", "vivo sdk called success order result. " + orderResultInfo.getCpOrderNumber());
                                    } else {
                                        Log.d("U8SDK", "vivo sdk called failed order result. " + i);
                                    }

                                    switch (i) {
                                        case OrderResultInfo.STATUS_PAY_SUCCESS:// 查询到订单支付成功
                                            updateU8Order(orderResultInfo);
                                            U8SDK.getInstance().onResult(U8Code.CODE_CHECK_ORDER_SUCCESS, orderResultInfo.getCpOrderNumber());
                                            break;
                                        case OrderResultInfo.STATUS_PAY_UNTREATED:// 查询接口传参错误
                                            break;
                                    }
                                }
                            });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void initOnCreate() {
        VivoUnionSDK.registerAccountCallback(U8SDK.getInstance().getContext(), new VivoAccountCallback() {

            @Override
            public void onVivoAccountLogout(int arg0) {
                U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "登出成功");
            }

            @Override
            public void onVivoAccountLoginCancel() {
                Log.d("U8SDK", "login canceled.");
                U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login canceled");
            }

            @Override
            public void onVivoAccountLogin(String userName, String openID, String authToken) {
                U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, encodeLoginResult(openID, userName, authToken));
            }
        });
        U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_SUCCESS, "init success");
    }

    public void login() {
        VivoUnionSDK.login(U8SDK.getInstance().getContext());
    }

    private String encodeLoginResult(String openid, String name, String token) {
        this.openID = openid;
        JSONObject json = new JSONObject();
        try {
            json.put("openid", openid);
            json.put("name", name);
            json.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    public void submitExtraData(UserExtraData data) {
        VivoRoleInfo roleInfo = new VivoRoleInfo(data.getRoleID(), data.getRoleLevel(), data.getRoleName(), data.getServerID() + "", data.getServerName());
        VivoUnionSDK.reportRoleInfo(roleInfo);
    }

    public void queryRealName(final boolean fromUICalled) {

        VivoUnionSDK.getRealNameInfo(U8SDK.getInstance().getContext(), new VivoRealNameInfoCallback() {
            @Override
            public void onGetRealNameInfoSucc(boolean isRealname, int age) {

                Log.d("U8SDK", "vivo real name query success.isRealName:" + isRealname + ";age:" + age);
                U8SDK.getInstance().onResult(
                        fromUICalled ? U8Code.CODE_REAL_NAME_REG_SUC : U8Code.CODE_ADDICTION_ANTI_RESULT, age + "");

            }

            @Override
            public void onGetRealNameInfoFailed() {
                Log.e("U8SDK", "vivo real name query failed.");
                U8SDK.getInstance().onResult(
                        fromUICalled ? U8Code.CODE_REAL_NAME_REG_SUC : U8Code.CODE_ADDICTION_ANTI_RESULT, "0");
            }
        });

    }

    public void sdkExit() {

        VivoUnionSDK.exit(U8SDK.getInstance().getContext(), new VivoExitCallback() {

            @Override
            public void onExitConfirm() {
                U8SDK.getInstance().getContext().finish();
                System.exit(0);
            }

            @Override
            public void onExitCancel() {
                // TODO Auto-generated method stub

            }
        });

    }

    public void openForum() {
        VivoUnionSDK.jumpTo(JumpType.FORUM);
    }

    public void pay(final PayParams params) {
        try {

            Log.e("U8SDK", "The pay extension is " + params.getExtension());

            String sign = getSignature(params);

            VivoPayInfo payInfo = new VivoPayInfo.Builder().setAppId(appID).setCpOrderNo(params.getOrderID())
                    .setExtInfo(params.getOrderID()).setNotifyUrl(params.getExtension())
                    .setOrderAmount(params.getPrice() * 100 + "").setProductDesc(params.getProductDesc())
                    .setProductName(params.getProductName()).setBalance(params.getCoinNum() + "")
                    .setVipLevel(params.getVip() + "").setRoleId(params.getRoleId() + "")
                    .setRoleName(params.getRoleName() + "").setRoleLevel(params.getRoleLevel() + "")
                    .setServerName(params.getServerName() + "").setParty("none").setVivoSignature(sign)
                    .setExtUid(TextUtils.isEmpty(openID) ? "" : openID).build();

            Map t = payInfo.toMapParams();
            for (Object en : t.keySet()) {
                Log.d("U8SDK", "payInfo=>  " + en + "=" + t.get(en));
            }

            VivoUnionSDK.payV2(U8SDK.getInstance().getContext(), payInfo, new VivoPayCallback() {

                @Override
                public void onVivoPayResult(int code, OrderResultInfo orderResultInfo) {

                    Log.d("U8SDK", "vivo sdk pay result. code:" + code + ";order:" + orderResultInfo.getTransNo());

                    if (code == VivoConstants.PAYMENT_RESULT_CODE_SUCCESS) {
                        VivoUnionSDK.sendCompleteOrderNotification(orderResultInfo);
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                    } else if (code == VivoConstants.PAYMENT_RESULT_CODE_CANCEL) {
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_CANCEL, "pay canceled");
                    } else if (code == VivoConstants.PAYMENT_RESULT_CODE_UNKNOWN) {
                        Log.d("U8SDK", "vivo to query order status.");

                        queryOrderResult(params.getOrderID(), orderResultInfo.getTransNo(), params.getPrice() * 100 + "", new QueryOrderCallback() {

                            @Override
                            public void onResult(int i, OrderResultInfo orderResultInfo) {
                                switch (i) {
                                    case OrderResultInfo.STATUS_PAY_SUCCESS:// 查询到订单支付成功
                                        updateU8Order(orderResultInfo);
                                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "sdk pay success");
                                        break;
                                    case OrderResultInfo.STATUS_PAY_UNTREATED:// 查询接口传参错误

                                        break;
                                }
                            }
                        });
                    } else {
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed.");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed. exception:" + e.getMessage());
        }
    }

    public String getSignature(PayParams orderBean) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("appId", appID);
        params.put("cpOrderNumber", orderBean.getOrderID());
        params.put("extInfo", orderBean.getOrderID());
        params.put("notifyUrl", orderBean.getExtension());
        params.put("orderAmount", orderBean.getPrice() * 100 + "");
        params.put("productDesc", orderBean.getProductDesc());
        params.put("productName", orderBean.getProductName());

        params.put("balance", orderBean.getCoinNum() + "");
        params.put("vip", orderBean.getVip() == null ? "" : orderBean.getVip());
        params.put("level", orderBean.getRoleLevel() + "");
        params.put("party", "none");
        params.put("roleId", orderBean.getRoleId() + "");
        params.put("roleName", orderBean.getRoleName() + "");
        params.put("serverName", orderBean.getServerName() + "");
        return VivoSignUtils.getVivoSign(params, this.appKey);
    }

    public void queryOrderResult(String cpOrderNumber, String vivoTransNo, String price,
                                 QueryOrderCallback queryOrderCallback) {

        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("appId", appID);
        params.put("cpId", cpID);
        params.put("cpOrderNumber", cpOrderNumber);
        params.put("orderNumber", vivoTransNo);
        params.put("orderAmount", price);
        params.put("version", "1.0.0");
        final String signature = VivoSignUtils.getVivoSign(params, appKey);

        VivoQueryOrderInfo.Builder builder = new VivoQueryOrderInfo.Builder(signature);
        builder.appId(appID).cpId(cpID).cpOrderNumber(cpOrderNumber).orderNumber(vivoTransNo).orderAmount(price);

        VivoUnionSDK.queryOrderResult(builder.build(), queryOrderCallback);
    }

    public void updateU8Order(final OrderResultInfo orderInfo) {

        Log.d("U8SDK", "begin update u8 order...");

        if (orderInfo == null) {
            Log.d("U8SDK", "updateU8Order failed. orderInfo is null");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    String time = getCurrTime();

                    StringBuilder sb = new StringBuilder();
                    sb.append("cpOrderNumber=").append(orderInfo.getCpOrderNumber())
                            .append("&orderAmount=").append(orderInfo.getProductPrice())
                            .append("&orderNumber=").append(orderInfo.getTransNo())
                            .append("&payTime=").append(time).append(appKey);

                    String sign = EncryptUtils.md5(sb.toString());

                    Log.d("U8SDK", "vivo u8 update order sign str:" + sb.toString() + "; sign:" + sign);

                    final String url = U8SDK.getInstance().getU8ServerURL() + "/pay/vivo/updateOrder";
                    Map<String, String> parmas = new HashMap<String, String>();
                    parmas.put("cpOrderNumber", orderInfo.getCpOrderNumber());
                    parmas.put("orderAmount", orderInfo.getProductPrice());
                    parmas.put("orderNumber", orderInfo.getTransNo());
                    parmas.put("payTime", time);
                    parmas.put("signature", sign);

                    String result = U8HttpUtils.httpPost(url, parmas);

                    Log.d("U8SDK", "vivo u8 update order result:" + result);

                    if ("success".equals(result)) {
                        VivoUnionSDK.sendCompleteOrderNotification(orderInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String getCurrTime() {
        long time = System.currentTimeMillis();// long now = android.os.SystemClock.uptimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(time);
        String t1 = format.format(d1);
        return t1;
    }
}
