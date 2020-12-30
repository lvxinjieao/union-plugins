package com.u8.sdk.permission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.u8.sdk.utils.ResourceHelper;


public class U8ProtocolActivity extends Activity {

    private WebView webview;
    private String title;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ResourceHelper.getIdentifier(this, "R.layout.u8_protocol_webview"));
        Intent intent = getIntent();
        this.url = intent.getStringExtra("url");
        Log.d("U8SDK", "u8 protocol title:" + title + ";url:" + url);
        initWebView();
        initTile();
    }

    @SuppressLint("NewApi")
    private void initWebView() {
        this.webview = (WebView) ResourceHelper.getView(this, "R.id.u8_p_webprotocol");
        WebSettings webSettings = this.webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        // webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setDomStorageEnabled(false);

        webSettings.setSupportZoom(true);
        // 设置出现缩放工具
        webSettings.setBuiltInZoomControls(true);
        //扩大比例的缩放
        webSettings.setUseWideViewPort(true);
        //自适应屏幕
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);

//		this.webview.addJavascriptInterface(new JsCallInterface(), "platform");
        this.webview.setWebViewClient(new WebViewClient());

        this.webview.setWebChromeClient(new WebChromeClient());

        this.webview.loadUrl(this.url);

    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void initTile() {

//		RelativeLayout title_layout = (RelativeLayout) ResourceUtils.getView(this, "R.id.u8_p_titlebar");
//		title_layout.setVisibility(View.VISIBLE);
//
//		TextView title_tv = (TextView) ResourceUtils.getView(this, "R.id.u8_p_title");
//		title_tv.setText(this.title);

        LinearLayout back = (LinearLayout) ResourceHelper.getView(this, "R.id.u8_p_back");

        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (webview.canGoBack()) {
                    webview.goBack();
                } else {
                    finishPage();
                }
            }
        });

    }

    private void finishPage() {
        finish();
    }

    public static void showProtocol(Activity context, String protocolUrl) {
        try {
            Intent intent = new Intent(context, U8ProtocolActivity.class);
            intent.putExtra("url", protocolUrl);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showProtocol(Activity context) {
        try {
            Intent intent = new Intent(context, U8ProtocolActivity.class);
            intent.putExtra("url", U8AutoPermission.getInstance().getProtocolUrl());
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
