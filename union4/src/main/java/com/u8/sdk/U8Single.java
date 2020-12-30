package com.u8.sdk;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import com.u8.sdk.base.ISDKListener;
import com.u8.sdk.log.Log;
import com.u8.sdk.plugin.U8Pay;
import com.u8.sdk.utils.ResourceHelper;
import com.u8.sdk.utils.StoreUtils;
import com.u8.sdk.verify.U8Proxy;
import com.u8.sdk.verify.UCheckResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 单机游戏
 */
public class U8Single {

    private static final String PAY_STORE_KEY = "u8paystorekey";

    private Timer timer = new Timer();

    private static final int STATE_NORMAL = 0;
    private static final int STATE_PAY_SUC = 1;
    private static final int STATE_PAY_UNKOWN = 2;

    private static final int MAX_TRY_NUM = 3;
    private static final int TRY_INTERVAL_SECS = 10;

    private volatile boolean autoChecking = false;

    private List<String> orders;

    private U8Single() {
        orders = Collections.synchronizedList(new ArrayList<String>());
    }

    private static U8Single instance;

    public static U8Single getInstance() {
        if (instance == null) {
            instance = new U8Single();
        }
        return instance;
    }

    //单机支付回调
    private void onSinglePayResult(int code, PayParams params) {

        if (params == null) {
            return;
        }

        U8Order order = new U8Order(params.getOrderID(), params.getProductId(), params.getProductName(), params.getExtension());
        for (ISDKListener listener : U8SDK.getInstance().getAllListeners()) {
            listener.onSinglePayResult(code, order);
        }
    }

    public void handleResult(int code, String msg) {

        Log.d("U8SDK", "handleResult in U8SDKSingle. code:" + code + ";msg:" + msg);

        //如果支付成功并且是单机游戏，那么去u8server更新订单状态
        if (U8Pay.getInstance().getCurrPayParams() != null) {
            //正常支付的话，清理掉
            PayParams order = U8Pay.getInstance().getCurrPayParams();
            String orderID = order.getOrderID();
            PayParams exists = getOrder(orderID);

            if (exists == null) {
                Log.e("U8SDK", "order not exsits in local cache. already removed?" + orderID);
                return;
            }

            switch (code) {
                case U8Code.CODE_PAY_SUCCESS:
                case U8Code.CODE_PAYING:
                    //纯单机的话，渠道返回支付中的话，
                    updateOrderState(orderID, STATE_PAY_SUC);
                    orders.add(orderID);
                    if (U8Pay.getInstance().needQueryResult()) {
                        //需要去服务器端检查订单状态， 收到SDK服务器支付回调通知才行
                        Log.d("U8SDK", "pay success. start pay check task");
                        startTask(new PayCheckTask(), order);
                    } else {
                        //无须去服务器端检查订单状态，直接更新订单状态为完成
                        Log.d("U8SDK", "pay success. start pay complete task");
                        startTask(new PayCompleteTask(), order);
                    }
                    U8Pay.getInstance().setCurrPayParams(null);
                    break;
                case U8Code.CODE_PAY_CANCEL:
                case U8Code.CODE_PAY_FAIL:
                    //失败的订单，也从本地缓存中移除
                    removeOrder(orderID);
                    onSinglePayResult(code, order);
                    U8Pay.getInstance().setCurrPayParams(null);
                    break;
                case U8Code.CODE_PAY_UNKNOWN:
                    updateOrderState(orderID, STATE_PAY_UNKOWN);
                    orders.add(orderID);
                    if (U8Pay.getInstance().needQueryResult()) {
                        //需要去服务器端检查订单状态， 收到SDK服务器支付回调通知才行
                        startPayCheckTask(order);
                    } else {
                        //未知的订单，下次继续检查. 登录之后， 调用渠道的订单检查接口
                        //比如华为
                    }
                    U8Pay.getInstance().setCurrPayParams(null);
                    break;
            }


        }

        //丢单检查回调, msg就是orderID
        String orderID = msg;
        PayParams order = null;

        switch (code) {
            case U8Code.CODE_CHECK_ORDER_SUCCESS:
                updateOrderState(orderID, STATE_PAY_SUC);

                if (orders.contains(orderID)) {
                    orders.remove(orderID);
                }

                orders.add(orderID);
                order = getOrder(orderID);

                if (order != null) {
                    if (U8Pay.getInstance().needQueryResult()) {
                        Log.d("U8SDK", "pay sucesss. start check retry task.");
                        startTask(new PayCheckRetryTask(), order);
                    } else {
                        startTask(new PayCompleteRetryTask(), order);
                    }

                } else {
                    Log.e("U8SDK", "order not exists in local cache. ");
                }
                break;
            case U8Code.CODE_CHECK_ORDER_UNKNOWN:
                code = U8Code.CODE_PAY_UNKNOWN;
                break;
            case U8Code.CODE_CHECK_ORDER_FAILED:
                order = removeOrder(msg);
                code = U8Code.CODE_PAY_FAIL;
                onSinglePayResult(code, order);
                break;
        }
    }

    @SuppressLint("NewApi")
    private void startPayCheckTask(PayParams order) {
        PayCheckTask task = new PayCheckTask();
        if (Build.VERSION.SDK_INT >= 11) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, order);
        } else {
            task.execute(order);
        }
    }

    private void onPayCheckResult(PayParams order, UCheckResult result) {

        if (result == null || result.getState() != 1) {
            orders.remove(order.getOrderID());
            ResourceHelper.showTipStr(U8SDK.getInstance().getContext(), "订单状态检查失败，稍后将自动重试");
            return;
        }

        Log.d("U8SDK", "onPayCheckResult called. pay success");
        orders.remove(order.getOrderID());
        removeOrder(order.getOrderID());
        onSinglePayResult(U8Code.CODE_PAY_SUCCESS, order);

    }


    private void onPayCheckRetryResult(PayParams order, UCheckResult result) {

        orders.remove(order.getOrderID());

        if (result == null || result.getState() != 1) {

            Log.d("U8SDK", "pay check retry failed. will auto retry again. order id:" + order.getOrderID());

        } else {
            removeOrder(order.getOrderID());
            onSinglePayResult(U8Code.CODE_PAY_SUCCESS, order);
        }

    }

    private void onPayCompleteResult(boolean suc, PayParams order) {
        Log.d("U8SDK", "onPayCompleteResult:" + suc);
        orders.remove(order.getOrderID());
        if (suc) {
            removeOrder(order.getOrderID());
            onSinglePayResult(U8Code.CODE_PAY_SUCCESS, order);
        } else {
            ResourceHelper.showTipStr(U8SDK.getInstance().getContext(), "支付发货失败，稍后将自动重试");
        }
    }

    private void onPayCompleteRetryResult(boolean suc, PayParams order) {
        Log.d("U8SDK", "onPayCompleteRetryResult:" + suc);
        orders.remove(order.getOrderID());
        if (suc) {
            removeOrder(order.getOrderID());
            onSinglePayResult(U8Code.CODE_PAY_SUCCESS, order);
        }
    }


    //一分钟自动检查一次丢单
    public void startAutoTask() {

        if (autoChecking) {
            Log.w("U8SDK", "auto task already started. just igore.");
            return;
        }

        autoChecking = true;

        if (timer == null) {
            timer = new Timer();
        }

        if (U8Pay.getInstance().needQueryResult()) {
            timer.schedule(autoCheckTask, 60000, 60000);
        } else {
            timer.schedule(autoCompleteTask, 60000, 60000);
        }
    }

    public void stopAutoTask() {
        autoChecking = false;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    //单机游戏， 支付成功之后， 通过该task向u8server通知， 告诉u8server将订单号的状态设置为完成
    class PayCompleteTask extends AsyncTask<PayParams, Void, Boolean> {

        private PayParams payParams;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = SDKTools.showProgressTip(U8SDK.getInstance().getContext(), "正在完成支付，请稍后...");
        }

        @Override
        protected Boolean doInBackground(PayParams... args) {
            payParams = args[0];
            Log.d("U8SDK", "begin to send pay complete req..." + payParams.getOrderID());
            return U8Proxy.completePay(payParams);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            onPayCompleteResult(result, payParams);
            SDKTools.hideProgressTip(progressDialog);
        }
    }

    class PayCompleteRetryTask extends AsyncTask<PayParams, Void, Boolean> {

        private PayParams payParams;

        @Override
        protected Boolean doInBackground(PayParams... args) {
            payParams = args[0];
            Log.d("U8SDK", "begin to send pay complete retry req..." + payParams.getOrderID());
            return U8Proxy.completePay(payParams);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            onPayCompleteRetryResult(result, payParams);
        }

    }

    //单机游戏， 支付成功之后，通过该接口检查订单是否成功
    class PayCheckTask extends AsyncTask<PayParams, Void, UCheckResult> {

        private PayParams order;
        private ProgressDialog processTip;

        @Override
        protected void onPreExecute() {
            processTip = SDKTools.showProgressTip(U8SDK.getInstance().getContext(), "正在完成支付，请稍后...");
        }

        @Override
        protected UCheckResult doInBackground(PayParams... args) {
            order = args[0];
            Log.d("U8SDK", "begin to send pay check req..." + order.getOrderID());
            int tryNum = 0;
            UCheckResult result = null;
            do {
                if (tryNum > 0) {
                    try {
                        Thread.sleep(TRY_INTERVAL_SECS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                tryNum++;
                result = U8Proxy.check(order);
            } while ((result == null || result.getState() != 1) && tryNum < MAX_TRY_NUM);
            return result;
        }

        @Override
        protected void onPostExecute(UCheckResult result) {
            SDKTools.hideProgressTip(processTip);
            onPayCheckResult(order, result);
        }
    }

    class PayCheckRetryTask extends AsyncTask<PayParams, Void, UCheckResult> {

        private PayParams payParams;

        @Override
        protected UCheckResult doInBackground(PayParams... args) {
            payParams = args[0];
            Log.d("U8SDK", "begin to send pay check retry req..." + payParams.getOrderID());
            return U8Proxy.check(payParams);
        }

        protected void onPostExecute(UCheckResult result) {
            onPayCheckRetryResult(payParams, result);
        }

    }


    private TimerTask autoCheckTask = new TimerTask() {

        @Override
        public void run() {

            List<PayParams> orders = getCachedOrders();
            Log.d("U8SDK", "begin auto check failed orders");

            if (orders == null || orders.size() == 0) {
                Log.d("U8SDK", "there is no order in cache.");
                return;
            }


            for (final PayParams order : orders) {

                if (order.getState() != STATE_PAY_SUC) {
                    Log.d("U8SDK", "order state is not suc. just ignore." + order.getOrderID());
                    continue;
                }

                if (orders.contains(order.getOrderID())) {
                    Log.d("U8SDK", "order current in handling orders. just ignore." + order.getOrderID());
                    continue;
                }

                final UCheckResult result = U8Proxy.check(order);

                U8SDK.getInstance().runOnMainThread(new Runnable() {

                    @Override
                    public void run() {
                        onPayCheckRetryResult(order, result);
                    }
                });
            }

        }
    };


    private TimerTask autoCompleteTask = new TimerTask() {

        @Override
        public void run() {

            List<PayParams> orders = getCachedOrders();
            if (orders == null || orders.size() == 0) {
                return;
            }

            for (final PayParams order : orders) {

                if (order.getState() != STATE_PAY_SUC) {
                    continue;
                }

                if (orders.contains(order.getOrderID())) {
                    continue;
                }

                final boolean suc = U8Proxy.completePay(order);

                U8SDK.getInstance().runOnMainThread(new Runnable() {

                    @Override
                    public void run() {
                        onPayCompleteRetryResult(suc, order);
                    }
                });

            }

        }
    };


    ////////////////////////////////////////////////Order Cache///////////////////////////////////////////////////////
    public List<PayParams> getCachedOrders() {

        String c = StoreUtils.getString(U8SDK.getInstance().getContext(), PAY_STORE_KEY);
        if (TextUtils.isEmpty(c)) {

            return null;
        }

        try {

            JSONArray array = new JSONArray(c);
            List<PayParams> result = new ArrayList<PayParams>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject j = array.getJSONObject(i);

                String orderID = j.optString("orderId", null);
                if (TextUtils.isEmpty(orderID)) {
                    continue;
                }
                Log.d("U8SDK", "begin get pay order in cache. orderID:" + orderID);

                try {

                    PayParams order = new PayParams();
                    order.setOrderID(orderID);
                    order.setProductId(j.optString("productId"));
                    order.setProductName(j.optString("productName"));
                    order.setExtension(j.optString("extension"));
                    order.setState(j.optInt("state", 0));

                    result.add(order);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void updateOrderState(String orderID, int state) {
        Log.d("U8SDK", "begin update order state:" + orderID);
        if (TextUtils.isEmpty(orderID)) {
            return;
        }

        String g = StoreUtils.getString(U8SDK.getInstance().getContext(), PAY_STORE_KEY);
        if (TextUtils.isEmpty(g)) {
            g = "[]";
        }

        try {

            JSONArray array = new JSONArray(g);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                if (orderID.equals(item.optString("orderId", ""))) {
                    item.put("state", state);
                    break;
                }
            }

            StoreUtils.putString(U8SDK.getInstance().getContext(), PAY_STORE_KEY, array.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void storeOrder(PayParams p) {

        Log.d("U8SDK", "begin store order:");

        String g = StoreUtils.getString(U8SDK.getInstance().getContext(), PAY_STORE_KEY);
        if (TextUtils.isEmpty(g)) {
            g = "[]";
        }

        try {

            if (p != null) {
                Log.d("U8SDK", "store order pay data info:");
                JSONObject extra = new JSONObject();
                extra.put("orderId", p.getOrderID());
                extra.put("productId", p.getProductId());
                extra.put("productName", p.getProductName());
                extra.put("extension", p.getExtension());
                extra.put("state", 0);
                JSONArray array = new JSONArray(g);
                array.put(extra);
                StoreUtils.putString(U8SDK.getInstance().getContext(), PAY_STORE_KEY, array.toString());
                Log.d("U8SDK", "store order success." + extra.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public PayParams getOrder(String orderID) {

        Log.d("U8SDK", "begin to remove order from store." + orderID);
        if (TextUtils.isEmpty(orderID)) {
            return null;
        }

        String g = StoreUtils.getString(U8SDK.getInstance().getContext(), PAY_STORE_KEY);
        if (TextUtils.isEmpty(g)) {
            g = "[]";
        }

        try {
            JSONArray array = new JSONArray(g);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                if (orderID.equals(item.optString("orderId", ""))) {
                    Log.d("U8SDK", "get order from store:" + orderID);
                    PayParams order = new PayParams();
                    order.setOrderID(orderID);
                    order.setProductId(item.optString("productId"));
                    order.setProductName(item.optString("productName"));
                    order.setExtension(item.optString("extension"));
                    order.setState(item.optInt("state", 0));
                    return order;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PayParams removeOrder(String orderID) {

        Log.d("U8SDK", "begin to remove order from store." + orderID);

        if (TextUtils.isEmpty(orderID)) {
            return null;
        }

        String g = StoreUtils.getString(U8SDK.getInstance().getContext(), PAY_STORE_KEY);
        if (TextUtils.isEmpty(g)) {
            g = "[]";
        }

        PayParams order = null;

        try {

            JSONArray array = new JSONArray(g);
            JSONArray left = new JSONArray();
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                if (!orderID.equals(item.optString("orderId", ""))) {
                    left.put(item);
                } else {
                    Log.d("U8SDK", "remove order from store:" + orderID);
                    order = new PayParams();
                    order.setOrderID(orderID);
                    order.setProductId(item.optString("productId"));
                    order.setProductName(item.optString("productName"));
                    order.setExtension(item.optString("extension"));
                    order.setState(item.optInt("state", 0));
                }
            }

            StoreUtils.putString(U8SDK.getInstance().getContext(), PAY_STORE_KEY, left.toString());

            Log.d("U8SDK", "remove order success." + orderID);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return order;
    }


    @SuppressLint("NewApi")
    private void startTask(AsyncTask<PayParams, ?, ?> task, PayParams order) {
        //PayCompleteTask task = new PayCompleteTask();
        if (Build.VERSION.SDK_INT >= 11) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, order);
        } else {
            task.execute(order);
        }
    }
}
