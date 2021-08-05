package com.ridho.skripsi.utility;

import com.ridho.skripsi.R;

import java.util.UUID;

public class Constant {

    //Notification Manager
    public static final String ERROR_LOCATION_PERMISSION = "ERROR_LOCATION_PERMISSION";
    public static final String ALERT_DISTANCE_WARNING = "ALERT_DISTANCE_WARNING";
    public static final String ALERT_NO_BLUETOOTH_DEVICE = "ALERT_NO_BLUETOOTH_DEVICE";


    //Intent Extra
    public static final String IS_ALERT_NOTIFICATION_CLICKED = "IS_ALERT_NOTIFICATION_CLICKED";

    public static final int PERMISSION_REQUEST_CODE = 1000;

    //Bluetooth
    public static final double BLE_MAX_DISTANCE = 10;
    public static final double SHOW_NOTIF_DISTANCE = 2;

    public static final String EXTRA_DEVICE_NAME = "EXTRA_DEVICE_NAME";
    public static final String EXTRA_DEVICE_ADDRESS = "EXTRA_DEVICE_ADDRESS";


    public static final int MESSAGE_STATE_CHANGED = 100;
    public static final int MESSAGE_READ = 101;
    public static final int MESSAGE_WRITE = 102;
    public static final int MESSAGE_DEVICE_NAME = 103;
    public static final int MESSAGE_TOAST = 104;
    public static final String TOAST = "toast";
    public static final String DEVICE_NAME = "device_name";

    public static final int[] COLOR_LIBRARY = {
            R.color.library1,
            R.color.library2,
            R.color.library3,
            R.color.library4,
            R.color.library5,
            R.color.library6,
            R.color.library7,
            R.color.library8,
            R.color.library9,
            R.color.library10,
            R.color.library11,
            R.color.library12,
            R.color.library13,
            R.color.library14,
            R.color.library15
    };
}
