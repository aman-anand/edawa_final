package com.amananand.edawamart;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    private static final int GALLERY_CAPTURE_IMAGE_REQUEST_CODE = 34225;
    WebView vistaWeb;
    private WebView webView;
    String data="";
    private Context context;
    private ProgressDialog progressDialog;
    private ConstraintLayout rootLayout;
    String str;
    CookieManager cookieManager;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        rootLayout = findViewById(R.id.rootLayout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            HandlePermission.requestAllPermission((Activity) context);
        }
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Image Upload in progress");
        cookieManager=CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        initWebView();




//        if (vistaWeb.getUrl().equals("http://edawamart.com/orders/upload-order-image")) {
//            Toast.makeText(this, "MAin Activity, url captured", Toast.LENGTH_SHORT).show();
//        }


    }

    private void initWebView() {
        WebChromeClient webChromeClient = new WebChromeClient();
//        MycustomClient mycustomClient=new MycustomClient();
//        WebViewClient webViewClient=new MycustomClient();
        Custom webViewClient = new Custom();

        vistaWeb = (WebView) findViewById(R.id.webview);

//        urlHandler = (UrlHandler) this;


        WebSettings webSettings = vistaWeb.getSettings();
        vistaWeb.setWebChromeClient(webChromeClient);
        vistaWeb.setWebViewClient(webViewClient);

        webSettings.setDomStorageEnabled(true);
        vistaWeb.clearCache(true);
        vistaWeb.clearHistory();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);


        vistaWeb.loadUrl("http://edawamart.com");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(),
                        "User cancelled gallery operation", Toast.LENGTH_SHORT)
                        .show();
                // action cancelled
            } else if (resultCode == RESULT_OK) {
                Uri selectedimg = data.getData();
                String[] FILE = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedimg, FILE, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                }
                int columnIndex = cursor.getColumnIndex(FILE[0]);
                String imagePath = cursor.getString(columnIndex);
                cursor.close();
                if (!imagePath.equals("")) {
//                    File file = new File(imagePath);
                    try {
                        uploadImageToServer(new File(imagePath));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "Error getting image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(),
                        "Sorry! gallery operation Failed.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void uploadImageToServer(File file) throws MalformedURLException {
//        Toast.makeText(this, "upload started", Toast.LENGTH_SHORT).show();
        progressDialog.show();
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        try {
            requestParams.put("order_image", file, "image/png");
            requestParams.put("data",data);
            requestParams.put("d","android");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "File Not Found", Toast.LENGTH_SHORT).show();
        }
        asyncHttpClient.post(this, "http://edawamart.com/orders/do_upload_order_image_android", requestParams,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        progressDialog.cancel();
                        try {
                            str=new String(responseBody,"utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        alertDialog.setTitle("Success")
                                .setMessage("Image Uploaded Successfully!")
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                        vistaWeb.loadUrl("http://edawamart.com");

                        rootLayout.setBackgroundResource(R.color.transparent);


                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(context, "upload failed", Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        progressDialog.cancel();
                        alertDialog.setTitle("Error")
                                .setMessage("Image Upload Failed")
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                        vistaWeb.loadUrl("http://edawamart.com");
                        rootLayout.setBackgroundResource(R.color.transparent);

                    }

                    @Override
                    public void onProgress(long bytesWritten, long totalSize) {
                        double progress = ((bytesWritten * 1.0 / totalSize) * 100);
                        progressDialog.setProgress((int) progress);
                        super.onProgress(bytesWritten, totalSize);

                    }


                });


    }

    public static String getCookieFromAppCookieManager(String url) throws MalformedURLException {
        CookieManager cookieManager = CookieManager.getInstance();
        if (cookieManager == null)
            return null;
        String rawCookieHeader = null;
        URL parsedURL = new URL(url);

        // Extract Set-Cookie header value from Android app CookieManager for this URL
        rawCookieHeader = cookieManager.getCookie(parsedURL.getHost());
        if (rawCookieHeader == null)
            return null;
        return rawCookieHeader;
    }

    class Custom extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("http://edawamart.com/orders/upload-order-image")) {
                String[] urlData=url.split("\\?data=");
                data=urlData[1];
                rootLayout.setBackgroundResource(R.color.black);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    if (HandlePermission.checkStoragePerms((Activity) context)) {
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                        startActivityForResult(Intent.createChooser(intent, "Choose Picture"), GALLERY_CAPTURE_IMAGE_REQUEST_CODE);
                    } else {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        alertDialog.setTitle("ERROR")
                                .setMessage("Permission not granted, cannot do this task")
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                        rootLayout.setBackgroundResource(R.color.transparent);


                    }
                }
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
            return false;
        }

    }
}