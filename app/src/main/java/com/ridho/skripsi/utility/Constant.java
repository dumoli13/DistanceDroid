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
    public static final UUID BLUETOOTH_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66");
    public static final int STATE_LISTENING = 1;
    public static final int STATE_CONNECTING=2;
    public static final int STATE_CONNECTED=3;
    public static final int STATE_CONNECTION_FAILED=4;
    public static final int STATE_MESSAGE_RECEIVED=5;

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
