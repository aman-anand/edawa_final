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
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int GALLERY_CAPTURE_IMAGE_REQUEST_CODE = 34225;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 34227;
    private static String IMAGE_DIRECTORY_NAME="Order Image";
    WebView vistaWeb;
    String data = "";
    String str;
    CookieManager cookieManager;
    private WebView webView;
    private Context context;
    private ProgressDialog progressDialog;
    private ConstraintLayout rootLayout;
    private Uri fileUri;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        rootLayout = findViewById(R.id.rootLayout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            HandlePermission.requestAllPermission((Activity) context);
        }
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Image Upload in progress");
        cookieManager = CookieManager.getInstance();
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
                        gohome();
                    }
                } else {
                    Toast.makeText(this, "Error getting image", Toast.LENGTH_SHORT).show();
                    gohome();
                }
            } else {
                Toast.makeText(getApplicationContext(),
                        "Sorry! gallery operation Failed.", Toast.LENGTH_SHORT)
                        .show();
                gohome();
            }
        }else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                String imagePath = fileUri.getPath();
                if (!TextUtils.isEmpty(imagePath)) {
                    try {
                        uploadImageToServer(new File(imagePath));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        gohome();
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
                gohome();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
                gohome();
            }
        }
    }

    private void gohome() {
        vistaWeb.loadUrl("http://edawamart.com");
    }

    private void uploadImageToServer(File file) throws MalformedURLException {
        progressDialog.setCancelable(false);
        progressDialog.show();
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        try {
            requestParams.put("order_image", file, "image/png");
            requestParams.put("data", data);
            requestParams.put("d", "android");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "File Not Found", Toast.LENGTH_SHORT).show();
            gohome();
        }
        asyncHttpClient.post(this, "http://edawamart.com/orders/do_upload_order_image_android", requestParams,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        progressDialog.cancel();
                        try {
                            str = new String(responseBody, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        alertDialog.setTitle("Success")
                                .setMessage("Image Uploaded Successfully!")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                       gohome();

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
                        gohome();
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

    @Override
    public void onBackPressed() {
        if (vistaWeb.canGoBack()) {
            vistaWeb.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void getImageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }
    public Uri getOutputMediaFileUri(int type) {
//        return Uri.fromFile(getOutputMediaFile(type));
//        System.out.println(Uri.fromFile(getOutputMediaFile(type)));
        return Uri.fromFile(getOutputMediaFile(type));
    }
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void getImageFromGallery() {
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
    }

    class Custom extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("http://edawamart.com/orders/upload-order-image")) {
                String[] urlData = url.split("\\?data=");
                if (urlData.length>1) {
                    data = urlData[1];
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_DayNight_Dialog);

                    TextView gallery, camera, cancel;
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View v = inflater.inflate(R.layout.dialog01, null);
                    gallery = v.findViewById(R.id.gallery);
                    camera = v.findViewById(R.id.camera);
                    cancel = v.findViewById(R.id.cancel);
                    builder.setView(v)
                            .setCancelable(false);
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    gallery.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getImageFromGallery();
                            alertDialog.cancel();
                        }
                    });
                    camera.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getImageFromCamera();
                            alertDialog.cancel();
                        }
                    });
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.cancel();
                            vistaWeb.loadUrl("http://edawamart.com");
                        }
                    });


                    rootLayout.setBackgroundResource(R.color.black);

                }else{
                    return super.shouldOverrideUrlLoading(view, url);

                }
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
            return false;
        }

    }
}