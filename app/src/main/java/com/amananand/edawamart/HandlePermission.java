package com.amananand.edawamart;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;

/**
 * Created by DELL on 10/4/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class HandlePermission implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int REQUEST_PERMISSION_CODE = 1;
    //    public static boolean permissionCamera = false;
    public static boolean permissionStorage = false;

    private static final String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,

    };

    public HandlePermission() {

    }

    public static boolean checkStoragePerms(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkLocationPerms(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

//    private void checkCameraPerms() {
//        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//            permissionCamera = true;
//        }
//    }

    public static void requestAllPermission(Activity activity) {
//        AlertBuilder.getInstance().getDialog(activity,
//                "Please allow all the permissions or else some or all the features may not work"
//                , 5, 3);
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_PERMISSION_CODE);
//        checkCameraPerms();
//        checkStoragePerms();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;


        }
    }


}
