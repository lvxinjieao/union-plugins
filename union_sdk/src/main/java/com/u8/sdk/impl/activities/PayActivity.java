package com.u8.sdk.impl.activities;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.u8.sdk.PayParams;
import com.u8.sdk.impl.DefaultSDKPlatform;
import com.u8.sdk.impl.common.PayPlatformType;
import com.u8.sdk.impl.listeners.ISDKPayListener;
import com.u8.sdk.impl.services.SdkManager;
import com.u8.sdk.impl.widgets.GridPayAdapter;
import com.u8.sdk.impl.widgets.GridPayAdapter.GridPayTypeData;
import com.u8.sdk.utils.ResourceHelper;


/**
 * 支付界面
 *
 * @author chenjie.chen
 */
public class PayActivity extends Activity {

    public static final int TAG_PAY_TYPE_SELECTED = 101;
    public static final int TAG_HELP_BACK = 102;
    public static final int TAG_HELP_ENTER = 103;
    public static final int TAG_PAY_ENTER = 104;
    public static final int TAG_PAY_COIN = 105;
    public static final int TAG_PAY_OTHER = 106;
    //	public static final int TAG_RESULT_BACK = 107;
    public static final int TAG_RESULT_SUC_ENTER = 108;
    public static final int TAG_RESULT_FAIL_ENTER = 109;
    public static final int TAG_ORDER_RESULT = 110;
    public static final int TAG_PAY_RESULT = 111;
    public static final int TAG_SEND_RESULT = 112;


    private List<GridPayTypeData> payTypeList;
    private GridPayAdapter payAdapter;

    private GridPayTypeData currSelected;

    private LinearLayout payView;

    private Animation leftin;
    private Animation leftout;
    private Animation rightin;
    private Animation rightout;

    private boolean payed = false;


    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {

            switch (msg.what) {
                case TAG_PAY_ENTER:
                    //打开支付界面
                    payView.setVisibility(View.VISIBLE);
                    payView.startAnimation(leftin);
                    break;
                case TAG_PAY_TYPE_SELECTED:
                    //选中某个支付通道

                    int position = Integer.valueOf(msg.obj.toString());

                    for (GridPayTypeData item : payTypeList) {
                        item.isChecked = false;
                    }
                    GridPayTypeData item = payTypeList.get(position);
                    item.isChecked = true;
                    currSelected = item;
                    payAdapter.notifyDataSetChanged();
                    break;
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            initUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private void initUI() {

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        int layoutId = ResourceHelper.getIdentifier(this, "R.layout.x_pay");
        setContentView(layoutId);

        Button btnPay = (Button) ResourceHelper.getView(this, "R.id.x_paybtn");
        btnPay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                doPay();
            }
        });

        initAnimations();

        initGrid();

        this.currSelected = payTypeList.get(0);

        mHandler.sendEmptyMessage(TAG_PAY_ENTER);
    }

    private void initGrid() {

        TextView helpBtn = (TextView) ResourceHelper.getView(this, "R.id.x_payhelpabout");
        helpBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(TAG_HELP_ENTER);
            }
        });

        this.payView = (LinearLayout) ResourceHelper.getView(this, "R.id.x_pay_main");

        LinearLayout btnBack = (LinearLayout) ResourceHelper.getView(this, "R.id.x_pay_back");
        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                DefaultSDKPlatform.getInstance().payFailCallback();
                PayActivity.this.finish();
            }
        });

        PayParams data = DefaultSDKPlatform.getInstance().getLastPayData();
        if (data != null) {
            TextView money = (TextView) ResourceHelper.getView(this, "R.id.x_payorderinfomsg");
            money.setText("¥" + data.getPrice());
        }


        GridView grid = (GridView) ResourceHelper.getView(this, "R.id.x_paygridview");
        this.payTypeList = new ArrayList<GridPayTypeData>();
        this.payAdapter = new GridPayAdapter(this, this.payTypeList, mHandler);

        doGenerateTestData();        //TODO:这里支付方式，回头改为服务器端控制
        doScreenAdaptation(grid);    //分辨率适配，显示4个

        grid.setAdapter(this.payAdapter);

    }


    public void initAnimations() {

        this.leftin = AnimationUtils.loadAnimation(this, ResourceHelper.getIdentifier(this, "R.anim.x_appear_to_left"));
        this.leftout = AnimationUtils.loadAnimation(this, ResourceHelper.getIdentifier(this, "R.anim.x_disappear_to_left"));
        this.rightin = AnimationUtils.loadAnimation(this, ResourceHelper.getIdentifier(this, "R.anim.x_appear_to_right"));
        this.rightout = AnimationUtils.loadAnimation(this, ResourceHelper.getIdentifier(this, "R.anim.x_disappear_to_right"));

    }

    private void doPay() {

        if (this.currSelected == null) {
            ResourceHelper.showTip(this, "R.string.x_pay_select_tip");
            return;
        }

        final PayParams data = DefaultSDKPlatform.getInstance().getLastPayData();
        if (data == null) {
            Log.e("U8SDK", "sdk pay data is null");
            return;
        }

        switch (this.currSelected.typeID) {
            case ALIPAY:
                //PayManager.getInstance().payWithAlipay(data, Constants.ORDER_TYPE_GAME,  mHandler);
                ResourceHelper.showTip(this, "R.string.x_pay_method_tip");
                break;
            case WEIXIN:

                ResourceHelper.showTip(this, "R.string.x_pay_method_tip");

                break;
            case UNION:
                ResourceHelper.showTip(this, "R.string.x_pay_method_tip");
                break;
            case XCOIN:
                SdkManager.getInstance().pay(this, data, new ISDKPayListener() {

                    @Override
                    public void onSuccess(String content) {
                        DefaultSDKPlatform.getInstance().paySucCallback();
                        PayActivity.this.finish();
                    }

                    @Override
                    public void onFailed(int code) {
                        DefaultSDKPlatform.getInstance().payFailCallback();
                        PayActivity.this.finish();
                    }
                });
                break;
        }

    }

    @SuppressLint("WrongConstant")
    private void doScreenAdaptation(GridView grid) {

        int size = this.payTypeList.size();
        int length = 67;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        float density = dm.density;
        int gridviewWidth = (int) (size * (length + 4) * density);
        int itemWidth = (int) (length * density);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(gridviewWidth, -2);
        grid.setLayoutParams(params);
        grid.setColumnWidth(itemWidth);
        grid.setHorizontalSpacing(3);
        grid.setStretchMode(0);
        grid.setNumColumns(size);
    }

//	private void goToView(View from, View to){
//		
//		from.setVisibility(View.GONE);
//		from.startAnimation(leftout);
//		to.setVisibility(View.VISIBLE);
//		to.startAnimation(leftin);		
//	}
//	
//	private void backToView(View from, View to){
//		
//		from.setVisibility(View.GONE);
//		from.startAnimation(rightout);
//		to.setVisibility(View.VISIBLE);
//		to.startAnimation(rightin);			
//	}

    private void doGenerateTestData() {
        GridPayTypeData item1 = new GridPayTypeData();
        item1.typeID = PayPlatformType.ALIPAY;
        item1.imgID = ResourceHelper.getIdentifier(this, "R.drawable.x_c_zfb");
        item1.name = ResourceHelper.getString(this, "R.string.x_pay_t_alipay");
        item1.isChecked = false;
        item1.canChecked = true;
        this.currSelected = item1;

        GridPayTypeData item2 = new GridPayTypeData();
        item2.typeID = PayPlatformType.WEIXIN;
        item2.imgID = ResourceHelper.getIdentifier(this, "R.drawable.x_c_weixin");
        item2.name = ResourceHelper.getString(this, "R.string.x_pay_t_weixin");
        item2.isChecked = false;
        item2.canChecked = true;

        GridPayTypeData item3 = new GridPayTypeData();
        item3.typeID = PayPlatformType.UNION;
        item3.imgID = ResourceHelper.getIdentifier(this, "R.drawable.x_c_payco");
        item3.name = ResourceHelper.getString(this, "R.string.x_pay_t_union");
        item3.isChecked = false;
        item3.canChecked = true;

        GridPayTypeData item4 = new GridPayTypeData();
        item4.typeID = PayPlatformType.XCOIN;
        item4.imgID = ResourceHelper.getIdentifier(this, "R.drawable.x_c_xmpay");
        item4.name = ResourceHelper.getString(this, "R.string.x_pay_t_xcoin");
        item4.isChecked = true;
        item4.canChecked = true;


        this.payTypeList.add(item4);
        this.payTypeList.add(item1);
        this.payTypeList.add(item2);
        this.payTypeList.add(item3);

    }


    public void onBackPressed() {
        if (this.payed) {
            this.payed = false;
        } else {
            DefaultSDKPlatform.getInstance().payFailCallback();
        }
        this.finish();
    }

}
