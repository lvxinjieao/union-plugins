package com.u8.sdk.plugin;

import java.util.List;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

import com.u8.sdk.IAdditionalPay;
import com.u8.sdk.IPay;
import com.u8.sdk.PayParams;
import com.u8.sdk.SDKTools;
import com.u8.sdk.U8SDK;
import com.u8.sdk.U8Single;
import com.u8.sdk.base.PluginFactory;
import com.u8.sdk.impl.SimpleDefaultPay;
import com.u8.sdk.utils.Logs;
import com.u8.sdk.verify.U8Proxy;
import com.u8.sdk.verify.UOrder;


public class U8Pay {


    private static U8Pay instance;

    private IPay payPlugin;

    private volatile PayParams currPayParams = null;

    private U8Pay() {

    }

    public static U8Pay getInstance() {
        if (instance == null) {
            instance = new U8Pay();
        }
        return instance;
    }

    public void init() {
        this.payPlugin = (IPay) PluginFactory.getInstance().initPlugin(IPay.PLUGIN_TYPE);
        if (this.payPlugin == null) {
            this.payPlugin = new SimpleDefaultPay();
        }
    }

    public boolean isSupport(String method) {
        if (this.payPlugin == null) {
            return false;
        }
        return this.payPlugin.isSupportMethod(method);
    }

    /***
     * 支付接口（弹出支付界面）
     * @param data
     */
    public void pay(PayParams data) {

        if (this.payPlugin == null) {
            return;
        }

        if(U8SDK.getInstance().isSingleGame()){
            if(this.currPayParams != null){
                Toast.makeText(U8SDK.getInstance().getContext(), "上一次支付未完成，请稍后再试，或重启再试", Toast.LENGTH_LONG).show();
                return;
            }
        }

        this.currPayParams = data;

        Logs.d("U8SDK", "****PayParams Print Begin****");
        Logs.d("U8SDK", "productId=" + data.getProductId());
        Logs.d("U8SDK", "productName=" + data.getProductName());
        Logs.d("U8SDK", "productDesc=" + data.getProductDesc());
        Logs.d("U8SDK", "buyNum=" + data.getBuyNum());
        Logs.d("U8SDK", "price=" + data.getPrice());
        Logs.d("U8SDK", "coinNum=" + data.getCoinNum());
        Logs.d("U8SDK", "serverId=" + data.getServerId());
        Logs.d("U8SDK", "serverName=" + data.getServerName());
        Logs.d("U8SDK", "roleId=" + data.getRoleId());
        Logs.d("U8SDK", "roleName=" + data.getRoleName());
        Logs.d("U8SDK", "roleLevel=" + data.getRoleLevel());
        Logs.d("U8SDK", "vip=" + data.getVip());
        Logs.d("U8SDK", "orderID=" + data.getOrderID());
        Logs.d("U8SDK", "extension=" + data.getExtension());
        Logs.d("U8SDK", "****PayParams Print End****");

        if (U8SDK.getInstance().isGetOrder()) {
            startOrderTask(data);
        } else {
            this.payPlugin.pay(data);
        }
    }


    public boolean needQueryResult() {
        if (!(payPlugin instanceof IAdditionalPay)) {
            Logs.w("U8SDK", "IPay error. single pay channel must implement IAdditionalPay interface");
            return false;
        }
        IAdditionalPay plugin = (IAdditionalPay) this.payPlugin;
        return plugin.needQueryResult();
    }


    //默认的AsyncTask的执行顺序可能会有些影响，导致队列中的任务并不能被及时执行
    @SuppressLint("NewApi")
    private void startOrderTask(PayParams data) {
        GetOrderTask authTask = new GetOrderTask(data);
        if (Build.VERSION.SDK_INT >= 11) {
            authTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            authTask.execute();
        }
    }

    @SuppressLint("NewApi")
    class GetOrderTask extends AsyncTask<Void, Void, UOrder> {

        private PayParams data;

        public GetOrderTask(PayParams data) {
            this.data = data;
        }

        protected void onPreExecute() {
        }

        @Override
        protected UOrder doInBackground(Void... args) {
            Logs.d("U8SDK", "开始从服务端获取订单号...");
            UOrder result = U8Proxy.getOrder(data);
            return result;
        }

        protected void onPostExecute(UOrder order) {
            try {
                if (order == null) {
                    Logs.e("U8SDK", "从服务端获取订单号失败");
                    currPayParams = null;
                    Toast.makeText(U8SDK.getInstance().getContext(), "获取订单号失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                data.setOrderID(order.getOrder());
                data.setExtension(order.getExtension());
                data.setProductId(order.getProductID());

//                Logs.d("U8SDK", "获取订单号成功 orderID:" + order.getOrder());
//                Logs.d("U8SDK", "获取订单号成功 extension:" + order.getExtension());
//                Logs.d("U8SDK", "获取订单号成功 productId:" + order.getProductID());

                if(U8SDK.getInstance().isSingleGame()){
                    //如果是单机渠道，保存订单信息，支付完成之后，移除订单
                    U8Single.getInstance().storeOrder(data);
                }

                payPlugin.pay(data);
            } catch (Exception e) {
                currPayParams = null;
                e.printStackTrace();
            }
        }
    }

    public PayParams getCurrPayParams() {
        return currPayParams;
    }

    public void setCurrPayParams(PayParams order) {
        this.currPayParams = order;
    }

}
