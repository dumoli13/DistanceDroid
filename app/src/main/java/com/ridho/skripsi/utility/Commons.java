package com.ridho.skripsi.utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;


import java.util.Random;

import static java.lang.Math.pow;

public class Commons {

    private static final String TAG = "DOMS Commons";

    public static int generateRandom(){
        Random random = new Random();
        return random.nextInt(9999-1000) +1000;
    }

    public static Bitmap drawableToBitmapConverter (int drawable){
        return BitmapFactory.decodeResource(Resources.getSystem(), drawable);
    }

    public static double calcBleDistance(double rssi){
        double distance = 0;
        double txPower = -60;
        double n = 3.2808;
        double ratio = rssi / txPower;

        if (rssi == 0.0)  distance = -1.0; // Cannot determine accuracy, return -1.
        else if (ratio < 1.0) distance = Math.pow(ratio, 10.0); //default ratio
        else distance = pow(10, ((-60- rssi)/(10*2)))*n; //rssi is greater than transmission strength
//
        return Math.floor(distance*100)/100;
    }

    public static boolean checkSupportBLE(Activity activity) {
        boolean isSupport = false;
        if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            isSupport = true;
        } else {
            Toast.makeText(activity, "This phone not support BLE", Toast.LENGTH_LONG).show();
        }
        return isSupport;
    }

    public static boolean checkGpsEnable(Activity activity) {
        boolean isEnable = false;
        LocationManager alm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)){
                isEnable = true;
            } else {
                Toast.makeText(activity, "Please turn on GPS", Toast.LENGTH_LONG).show();
            }
        }
        return isEnable;
    }

    public static void checkPermission(Activity activity){
        if (Build.VERSION.SDK_INT >= 23){
            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, Constant.PERMISSION_REQUEST_CODE);
        }
    }
}
