package com.u8.sdk.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.u8.sdk.permission.utils.PermissionGridAdapter;
import com.u8.sdk.utils.Logs;
import com.u8.sdk.utils.ResourceHelper;

import java.util.ArrayList;
import java.util.List;

public class U8PermissionDialog extends Dialog {

    private Activity activity;
    private AlertDialog alertDialog;

    private List<PermissionGridAdapter.PermissionItemData> permissionLst;
    private PermissionGridAdapter permissionGridAdapter;

    private IProtocolListener listener;
    private List<U8PermissionInfo> permissions;
    private String protocolUrl;
    private String orientation;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what) {
//                case MsgTagID.TAG_PAY_ENTER:
//                    // 打开支付界面
//                    payView.setVisibility(View.VISIBLE);
//                    payView.startAnimation(leftin);
//                    break;
            }
        }
    };


    public U8PermissionDialog(Activity activity, int theme) {
        super(activity, theme);
        this.activity = activity;
        this.alertDialog = new AlertDialog.Builder(activity).create();
        this.alertDialog.setCancelable(false);
        this.alertDialog.setCanceledOnTouchOutside(false);
        this.alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
    }

    private void doShow() {
        this.alertDialog.show();
        WindowManager.LayoutParams attrs = this.alertDialog.getWindow().getAttributes();
        //Display display = activity.getWindowManager().getDefaultDisplay();
        WindowManager wm = (WindowManager) activity.getSystemService(activity.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);

        float ratio = 0.9f;
        if ("landscape".equalsIgnoreCase(this.orientation)) {
            ratio = 0.6f;
            if (this.permissions.size() <= 3) {
                ratio = 0.5f;
            }
        }

        attrs.width = (int) (dm.widthPixels * ratio);
        this.alertDialog.getWindow().setAttributes(attrs);

        Window window = this.alertDialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(0));
        window.setContentView(ResourceHelper.getIdentifier(activity, "R.layout.u8_permission_dialog_layout"));

        initGrid(window);
    }


    private void initGrid(Window view) {

        GridView grid = (GridView) ResourceHelper.getViewByWindow(view,
                "R.id.u8_permission_gridview");

        try {

            this.permissionLst = new ArrayList<PermissionGridAdapter.PermissionItemData>();
            this.permissionGridAdapter = new PermissionGridAdapter(this.getContext(), this.permissionLst, mHandler);

            final CheckBox agree = (CheckBox) ResourceHelper.getViewByWindow(view, "R.id.u8_protocol_checkbox");

            Button btn = (Button) ResourceHelper.getViewByWindow(view, "R.id.u8_btn_ok");
            btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    if (U8AutoPermission.getInstance().isAutoProtocol()) {
                        if (agree.isChecked()) {
                            doRequestPermission();
                        } else {
                            dismiss();
                            U8ProtocolDialog.showDialog(activity, protocolUrl, listener);
                        }
                    } else {
                        doRequestPermission();
                    }

                }
            });

            LinearLayout protocolLayout = (LinearLayout) ResourceHelper.getViewByWindow(view, "R.id.u8_protocol_tip_layout");
            if (!U8AutoPermission.getInstance().isAutoProtocol() && protocolLayout != null) {
                protocolLayout.setVisibility(View.GONE);
            } else {
                Logs.e("U8SDK", "R.id.u8_protocol_tip_layout not found");
            }

            TextView protocolName = (TextView) ResourceHelper.getViewByWindow(view, "R.id.u8_protocol_addr");
            protocolName.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    U8ProtocolActivity.showProtocol(activity, protocolUrl);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        //权限展示数据
        generateData();

        doScreenAdaptation(grid); // 分辨率适配，显示4个

        grid.setAdapter(this.permissionGridAdapter);

    }

//    private void gotoToProtocol(){
//        try{
//
//            Intent intent = new Intent(U8PermissionDialog.this.activity, U8ProtocolActivity.class);
//            intent.putExtra("url", this.protocolUrl);
//            activity.startActivity(intent);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }

    private void doRequestPermission() {

        dismiss();

        if (listener != null) {
            listener.onAgreed();
        }
    }

    private void generateData() {

        for (U8PermissionInfo permission : permissions) {
            PermissionGridAdapter.PermissionItemData itemData = new PermissionGridAdapter.PermissionItemData("R.drawable.u8_" + permission.getGroup().toLowerCase(), permission.getCname());
            this.permissionLst.add(itemData);
        }

    }

    private void doScreenAdaptation(GridView grid) {

        int size = this.permissionLst.size();
        int length = 80;
        DisplayMetrics dm = new DisplayMetrics();
        this.activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        float density = dm.density;
        int gridviewWidth = (int) (size * (length + 4) * density);
        int itemWidth = (int) (length * density);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                gridviewWidth, -2);
        grid.setLayoutParams(params);
        grid.setColumnWidth(itemWidth);
        grid.setHorizontalSpacing(3);
        grid.setStretchMode(GridView.NO_STRETCH);
        grid.setNumColumns(size);
    }

    public void dismiss() {

        if (this.alertDialog == null) {
            return;
        }

        try {
            this.alertDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void showDialog(Activity activity, String protocolUrl, String orientation, List<U8PermissionInfo> permissions, IProtocolListener listener) {

        Logs.d("U8SDK", "show permission dialog. the protocol url is :" + protocolUrl);

        U8PermissionDialog dialog = new U8PermissionDialog(activity, ResourceHelper.getIdentifier(activity, "R.style.u8_permission_dialog"));
        dialog.listener = listener;
        dialog.permissions = permissions;
        dialog.protocolUrl = protocolUrl;
        dialog.orientation = orientation;

        if (listener == null) {

            return;
        }

        if (permissions == null || permissions.size() == 0) {
            listener.onAgreed();
        }

        dialog.doShow();
    }
}
