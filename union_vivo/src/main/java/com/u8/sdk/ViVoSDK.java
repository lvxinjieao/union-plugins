package com.u8.sdk;

import android.text.TextUtils;
import android.util.Log;

import com.u8.sdk.utils.EncryptUtils;
import com.u8.sdk.utils.U8HttpUtils;
import com.u8.sdk.vivo.HTTPSTrustManager;
import com.u8.sdk.vivo.VivoSignUtils;
import com.vivo.unionsdk.open.MissOrderEventHandler;
import com.vivo.unionsdk.open.OrderResultInfo;
import com.vivo.unionsdk.open.VivoAccountCallback;
import com.vivo.unionsdk.open.VivoConstants;
import com.vivo.unionsdk.open.VivoExitCallback;
import com.vivo.unionsdk.open.VivoPayCallback;
import com.vivo.unionsdk.open.VivoPayInfo;
import com.vivo.unionsdk.open.VivoRealNameInfoCallback;
import com.vivo.unionsdk.open.VivoRoleInfo;
import com.vivo.unionsdk.open.VivoUnionSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

            VivoUnionSDK.registerMissOrderEventHandler(U8SDK.getInstance().getApplication(), new MissOrderEventHandler() {


                @Override
                public void process(List list) {
                    /**
                     * 注意这里是查到未核销的订单
                     * 需要调用自己的逻辑完成道具核销后再调用我们的订单完成接口
                     * 切记！！！一定要走自己逻辑发送完道具后再调用完成接口！！！切记！切记！
                     * ！！！游戏根据订单号检查、补发商品！！！
                     * 自行完成补发逻辑  一定要完成道具补发后才能调用完成接口 此处一定要注意！！！
                     * 如果不处理直接调用完成则掉单无法解决
                     * 注意！！！注意！！！
                     * 游戏侧用你们自己的订单号cpOrderNumber来校验是否完成发货  发货完成上报我们的订单号transNo
                     */
                    checkOrder(list);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void checkOrder(final List<OrderResultInfo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        updateU8Order(list);
    }


    public void updateU8Order(final List<OrderResultInfo> orders) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    for (OrderResultInfo order : orders) {
                        /**
                         * ！！！游戏根据订单号检查、补发商品！！！
                         * 自行完成补发逻辑  一定要完成道具补发后才能调用完成接口 此处一定要注意！！！
                         * 如果不处理直接调用完成则掉单无法解决
                         * 此处只是用log演示处理  真正的逻辑需要游戏自己处理补发道具后再调用完成
                         * 注意！！！注意！！！
                         */
                        Log.i("U8SDK", "updateU8Order now to process: " + order.getCpOrderNumber());
                        resendMissingOrder(order);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void resendMissingOrder(OrderResultInfo orderInfo) {
        String time = getCurrTime();
        StringBuilder sb = new StringBuilder();
        sb.append("cpOrderNumber=").append(orderInfo.getCpOrderNumber())
                .append("&orderAmount=").append(orderInfo.getProductPrice())
                .append("&orderNumber=").append(orderInfo.getTransNo())
                .append("&payTime=").append(time)
                .append(appKey);

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
            List<String> list = new ArrayList<>();
            list.add(orderInfo.getTransNo());
            VivoUnionSDK.reportOrderComplete(list, true);
        }
    }

    private String getCurrTime() {
        long time = System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        String str = format.format(date);
        return str;
    }

    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    public void initOnCreate() {

        VivoUnionSDK.registerAccountCallback(U8SDK.getInstance().getContext(), new VivoAccountCallback() {

            @Override
            public void onVivoAccountLogout(int arg0) {
                U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
            }

            @Override
            public void onVivoAccountLoginCancel() {
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
                U8SDK.getInstance().onResult(fromUICalled ? U8Code.CODE_REAL_NAME_REG_SUC : U8Code.CODE_ADDICTION_ANTI_RESULT, age + "");
            }

            @Override
            public void onGetRealNameInfoFailed() {
                U8SDK.getInstance().onResult(fromUICalled ? U8Code.CODE_REAL_NAME_REG_SUC : U8Code.CODE_ADDICTION_ANTI_RESULT, "0");
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
            }
        });

    }

    public void openForum() {
        VivoUnionSDK.jumpTo(VivoConstants.JumpType.FORUM);
    }

    public void pay(final PayParams params) {
        try {

            Log.e("U8SDK", "The pay extension is " + params.getExtension());

            String sign = getSignature(params);

            VivoPayInfo payInfo = new VivoPayInfo.Builder()
                    .setAppId(appID)
                    .setCpOrderNo(params.getOrderID())
                    .setExtInfo(params.getOrderID())
                    .setNotifyUrl(params.getExtension())
                    .setOrderAmount(params.getPrice() * 100 + "")
                    .setProductDesc(params.getProductDesc())
                    .setProductName(params.getProductName())
                    .setBalance(params.getCoinNum() + "")
                    .setVipLevel(params.getVip() + "")
                    .setRoleId(params.getRoleId() + "")
                    .setRoleName(params.getRoleName() + "")
                    .setRoleLevel(params.getRoleLevel() + "")
                    .setServerName(params.getServerName() + "")
                    .setParty("none")
                    .setVivoSignature(sign)
                    .setExtUid(TextUtils.isEmpty(openID) ? "" : openID).build();

            VivoUnionSDK.payV2(U8SDK.getInstance().getContext(), payInfo, new VivoPayCallback() {

                @Override
                public void onVivoPayResult(int code, OrderResultInfo orderResultInfo) {

                    if (code == VivoConstants.PAYMENT_RESULT_CODE_SUCCESS) {
                        try {
                            List<String> list = new ArrayList<>();
                            list.add(orderResultInfo.getTransNo());
                            VivoUnionSDK.reportOrderComplete(list, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                    } else if (code == VivoConstants.PAYMENT_RESULT_CODE_CANCEL) {
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_CANCEL, "pay canceled");
                    } else if (code == VivoConstants.PAYMENT_RESULT_CODE_UNKNOWN) {
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "未知状态，请查询订单");
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

}
