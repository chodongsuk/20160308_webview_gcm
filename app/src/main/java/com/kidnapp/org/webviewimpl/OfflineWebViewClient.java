package com.kidnapp.org.webviewimpl;

import android.app.Activity;
import android.graphics.Bitmap;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by ajay.jadhao on 16/12/2015.
 */
public class OfflineWebViewClient extends WebViewClient {

    private Activity mActivity;

    public OfflineWebViewClient(Activity activity){
        mActivity = activity;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView mWebView, String url) {
        mWebView.loadUrl(url);
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        ((MainActivity)mActivity).showProgress();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        ((MainActivity)mActivity).getSupportActionBar().show();
        ((MainActivity)mActivity).hideSplashView();
        ((MainActivity)mActivity).showWebView();

        ((MainActivity)mActivity).hideProgress();
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view,
                                          HttpAuthHandler handler, String host, String realm) {

        handler.proceed("admin", "zip2015!!!");

    }

}
