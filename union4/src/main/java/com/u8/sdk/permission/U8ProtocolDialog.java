package com.u8.sdk.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.u8.sdk.utils.ResourceHelper;

/**
 * 游戏退出确认框
 */
public class U8ProtocolDialog extends Dialog {

    private Activity activity;
    private AlertDialog alertDialog;

    private IProtocolListener listener;
    private String protocolUrl;

    public U8ProtocolDialog(Activity activity, int theme) {
        super(activity, theme);
        this.activity = activity;
        this.alertDialog = new AlertDialog.Builder(activity).create();
        this.alertDialog.setCancelable(false);
        this.alertDialog.setCanceledOnTouchOutside(false);
        this.alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
    }

    private void doShow() {

        this.alertDialog.show();

        Window window = this.alertDialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(0));
        window.setContentView(ResourceHelper.getIdentifier(activity, "R.layout.u8_protocol_confirm_layout"));

        Button btnCancel = (Button) ResourceHelper.getViewByWindow(window, "R.id.u8_protocol_cancel");
        Button btnOK = (Button) ResourceHelper.getViewByWindow(window, "R.id.u8_protocol_ok");

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                ResourceHelper.showTip(activity, "R.string.u8_permission_exit_tip");
                exitGame();
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onAgreed();
                }
            }
        });

        TextView protocolName = (TextView) ResourceHelper.getViewByWindow(window, "R.id.u8_protocol_addr2");
        protocolName.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                U8ProtocolActivity.showProtocol(activity, protocolUrl);
            }
        });
    }

    private void exitGame() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    activity.finish();
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 3000);
    }

    public static void showDialog(Activity activity, String protocolUrl, IProtocolListener listener) {
        U8ProtocolDialog dialog = new U8ProtocolDialog(activity, ResourceHelper.getIdentifier(activity, "R.style.u8_permission_dialog"));
        dialog.activity = activity;
        dialog.protocolUrl = protocolUrl;
        dialog.listener = listener;
        dialog.doShow();
    }


    public void dismiss() {
        if (this.alertDialog != null) {
            try {
                this.alertDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
