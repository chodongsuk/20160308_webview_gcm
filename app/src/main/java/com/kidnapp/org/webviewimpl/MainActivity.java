package com.kidnapp.org.webviewimpl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.kidnapp.org.R;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String BASE_URL = "http://kidnapp.org/";
    public static final String CALL_NUMBER = "";
    public static final int FILECHOOSER_RESULTCODE = 2;
    public static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    private WebView mWebView;
    private ProgressBar pb;
    private LinearLayout liSplashView;
    private MyWebChromeClient mChromeClient;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        initViews();
        hideWebView();
        hideProgress();

        configureOfflineWebView(savedInstanceState);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.

            Intent intent = new Intent(this, RegistrationIntentService.class);

            startService(intent);
        }
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.i(TAG, "Registration Token sucessful " );
                    //Toast.makeText(context, "Registration done ", Toast.LENGTH_LONG).show();
                } else {
                    Log.i(TAG, "Registration Token failed " );
                    //Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private void configureOfflineWebView(Bundle savedInstanceState) {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setDomStorageEnabled(true);

// Set cache size to 8 mb by default. should be more than enough
        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);

        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        mChromeClient = new MyWebChromeClient(this);
        mWebView.setWebChromeClient(mChromeClient);

        mWebView.setWebViewClient(new OfflineWebViewClient(this));

        if ( !Utils.isNetworkAvailable(this) ) { // loading offline
//            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK );
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        }

        if (savedInstanceState == null) {
            // Load a page
//            mWebView.loadUrl(BASE_URL);
            loadURL();
        }

    }

    private void loadURL() {
        String strPush;
        Intent i = getIntent();
        if (i != null) {
            Bundle extra = i.getExtras();

            if (extra != null && extra.getString("com.parse.Data") != null) {
                strPush = extra.getString("com.parse.Data");
                List<String> urls = UrlExtractor.extractUrls(strPush);
                if (urls.size() > 0) {
                    String url = urls.get(0);
                    Log.d("URL: ", url);
                    mWebView.loadUrl("https://"+url);
                }else{
                    mWebView.loadUrl(BASE_URL);
                }
            } else {
                mWebView.loadUrl(BASE_URL);
            }
        }else {
            mWebView.loadUrl(BASE_URL);
        }
    }


    private void initViews() {
        liSplashView = (LinearLayout) findViewById(R.id.li_splashview);
        pb = (ProgressBar) findViewById(R.id.progress_bar);
        mWebView = (WebView) findViewById(R.id.webview);
    }

    private void callJavaScriptOfWebsite() {
//        Calling JavaScript via WebView loadUrl()
//
//        Before API level 19 (before Android 4.4 - Kitkat) you can use the WebView loadUrl() method like this:

        mWebView.loadUrl("javascript:theFunction('text')");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (mChromeClient.getUploadMessage() == null) return;
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            mChromeClient.getUploadMessage().onReceiveValue(result);
            mChromeClient.setUploadMessage(null);
        }if(requestCode == INPUT_FILE_REQUEST_CODE ) {
            if (mChromeClient.mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, intent);
                return;
            }
            Uri[] results = null;
            // Check that the response is a good one
            if(resultCode == RESULT_OK) {
                if(intent == null) {
                    // If there is not data, then we may have taken a photo
                    if(mChromeClient.mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mChromeClient.mCameraPhotoPath)};
                    }
                } else {
                    String dataString = intent.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            mChromeClient.mFilePathCallback.onReceiveValue(results);
            mChromeClient.mFilePathCallback = null;
            return;

        }

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
//        return true;
//    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_call){
            Utils.call(MainActivity.this, CALL_NUMBER);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && this.mWebView.canGoBack()) {
            this.mWebView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    protected void onSaveInstanceState(Bundle outState) {
        mWebView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the state of the WebView
        mWebView.restoreState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {

        if (mWebView.canGoBack()) {
            mWebView.goBack();
        }else {

            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Closing App")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mWebView.reload();
        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onResume", (Class[]) null)
                    .invoke(mWebView, (Object[]) null);

        } catch(ClassNotFoundException cnfe) {

        } catch(NoSuchMethodException nsme) {

        } catch(InvocationTargetException ite) {

        } catch (IllegalAccessException iae) {

        }
    }

    @Override
    public void onPause() {
        super.onPause();

        stopLoading();

    }

    private void stopLoading() {
//        mWebView.stopLoading();
        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause", (Class[]) null)
                    .invoke(mWebView, (Object[]) null);

        } catch(ClassNotFoundException cnfe) {

        } catch(NoSuchMethodException nsme) {

        } catch(InvocationTargetException ite) {

        } catch (IllegalAccessException iae) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLoading();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLoading();
    }

    public void showProgress(){
        pb.setVisibility(View.VISIBLE);
    }

    public void hideProgress(){
        pb.setVisibility(View.GONE);
    }

    public void showSplashView(){
        liSplashView.setVisibility(View.VISIBLE);
    }

    public void hideSplashView(){
        liSplashView.setVisibility(View.GONE);
    }

    public void showWebView(){
        mWebView.setVisibility(View.VISIBLE);
    }

    public void hideWebView(){
        mWebView.setVisibility(View.GONE);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
