package com.ridho.skripsi.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Guideline;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ridho.skripsi.R;
import com.ridho.skripsi.model.NearbyBluetoothModel;
import com.ridho.skripsi.model.PairedBluetoothModel;
import com.ridho.skripsi.utility.BluetoothUtil;
import com.ridho.skripsi.utility.Commons;
import com.ridho.skripsi.utility.Constant;
import com.ridho.skripsi.utility.UserNotificationManager;
import com.ridho.skripsi.view.dialog.DeviceBottomSheetDialog;

import java.util.HashMap;
import java.util.Map;

import static com.ridho.skripsi.utility.Commons.calcBleDistance;
import static com.ridho.skripsi.utility.Constant.MESSAGE_DEVICE_NAME;
import static com.ridho.skripsi.utility.Constant.MESSAGE_READ;
import static com.ridho.skripsi.utility.Constant.MESSAGE_STATE_CHANGED;
import static com.ridho.skripsi.utility.Constant.MESSAGE_TOAST;
import static com.ridho.skripsi.utility.Constant.MESSAGE_WRITE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DOMS MainActivity";
    private static final int pairedDeviceRequestCode = 100;


    private LinearLayout layoutBleCount;
    private ConstraintLayout layoutTopOn,layoutTopOff, layoutBottom, layoutForRadius, layoutGetPaired, layoutSaturation, layoutHeartBeat;
    private TextView tvBluetoothDescription, tvMainTitle, tvSaturationValue, tvHeartbeatValue, tvBleCount;
    private ImageView ivBluetoothSwitch1, ivBluetoothSwitch2;
    private DeviceBottomSheetDialog deviceBottomSheetDialog;

    private static MainActivity instance;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothUtil bluetoothUtil;
    private Map<String, NearbyBluetoothModel> deviceMap = new HashMap<>();
    private PairedBluetoothModel pairedBluetoothModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        layoutTopOn = findViewById(R.id.layout_top_on);
        layoutTopOff = findViewById(R.id.layout_top_off);
        layoutBottom = findViewById(R.id.layout_bottom);
        layoutForRadius = findViewById(R.id.layout_for_radius);
        layoutGetPaired = findViewById(R.id.layout_get_paired);
        layoutGetPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListPairedDevice.class);
                MainActivity.this.startActivityForResult(intent, pairedDeviceRequestCode);

            }
        });

        layoutSaturation = findViewById(R.id.layout_saturation);
        layoutHeartBeat = findViewById(R.id.layout_heartbeat);

        tvBluetoothDescription = findViewById(R.id.tv_bluetooth_description);
        tvMainTitle = findViewById(R.id.tv_title);

        tvSaturationValue = findViewById(R.id.tv_saturation_value);
        tvSaturationValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "spo2:" + System.currentTimeMillis();
                bluetoothUtil.write(message.getBytes());
            }
        });

        tvHeartbeatValue = findViewById(R.id.tv_heartbeat_value);
        tvHeartbeatValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "bpm:"+ System.currentTimeMillis();
                bluetoothUtil.write(message.getBytes());
            }
        });

        layoutBleCount = findViewById(R.id.layout_ble_count);
        layoutBleCount.setOnClickListener(bluetoothCountClickListener);
        tvBleCount = findViewById(R.id.tv_ble_count);

        ivBluetoothSwitch1 = findViewById(R.id.iv_bluetooth_switch1);
        ivBluetoothSwitch1.setOnClickListener(bluetoothSwitchClickListener);
        ivBluetoothSwitch2 = findViewById(R.id.iv_bluetooth_switch2);
        ivBluetoothSwitch2.setOnClickListener(bluetoothSwitchClickListener);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG, "onStart: " + bluetoothAdapter);
        if(bluetoothAdapter == null){
            showMainMenu(false);
            UserNotificationManager.showGeneralError(this, Constant.ALERT_NO_BLUETOOTH_DEVICE);
        } else if(!bluetoothAdapter.isEnabled()){
            showMainMenu(false);
        } else{
            showMainMenu(true);
        }

        setupBluetoothBroadcast();
        bluetoothUtil = new BluetoothUtil(this, handler);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Commons.checkPermission(MainActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bluetoothUtil != null) bluetoothUtil.stop();

        unregisterReceiver(bluetoothBroadcastReceiver);
        unregisterReceiver(bluetoothDiscoveryBrodcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == pairedDeviceRequestCode && resultCode == RESULT_OK){
            Log.d(TAG, "onActivityResult: NAME: " + data.getStringExtra(Constant.EXTRA_DEVICE_NAME));
            Log.d(TAG, "onActivityResult: ADDRESS: " + data.getStringExtra(Constant.EXTRA_DEVICE_ADDRESS));
            pairedBluetoothModel = new PairedBluetoothModel(data.getStringExtra(Constant.EXTRA_DEVICE_NAME), data.getStringExtra(Constant.EXTRA_DEVICE_ADDRESS));

            bluetoothUtil.connect(bluetoothAdapter.getRemoteDevice(pairedBluetoothModel.getAddress()));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int grantResult : grantResults) {
            if (grantResult == -1) {
                UserNotificationManager.showErrorPermission(MainActivity.this);
                break;
            }
        }
    }

    public static MainActivity getInstace(){
        return instance;
    }

    private void setupBluetoothBroadcast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothBroadcastReceiver, intentFilter);

        if(bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();
        bluetoothAdapter.startDiscovery();
        IntentFilter discoverDeviceFilter = new IntentFilter();
        discoverDeviceFilter.addAction(BluetoothDevice.ACTION_FOUND);
        discoverDeviceFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothDiscoveryBrodcastReceiver, discoverDeviceFilter);
    }

    public void updateBleCount() {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                tvBleCount.setText(String.valueOf(deviceMap.size()));

                String[] keys = deviceMap.keySet().toArray(new String[0]);
                layoutForRadius.removeAllViewsInLayout();
                for(int i=0; i<keys.length; i++){
                    setDevice((float) deviceMap.get(keys[i]).getDistance(), deviceMap.get(keys[i]).getColor());
                }
            }
        });
    }

    View.OnClickListener bluetoothSwitchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            } else {
                bluetoothAdapter.enable();

                if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoveryIntent);
                }
            }
        }
    };

    View.OnClickListener bluetoothCountClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: bluetoothCountClickListener");
//            UserNotificationManager.showErrorNotification(getApplicationContext(), getString(R.string.distance_alert_body));
//            UserNotificationManager.showDistanceDialog(MainActivity.this, Constant.ALERT_DISTANCE_WARNING, new String[]{"haha", "hehe", "hihi"});
//            UserNotificationManager.showGeneralError(MainActivity.this, Constant.ALERT_NO_BLUETOOTH_DEVICE);

            deviceBottomSheetDialog = new DeviceBottomSheetDialog(MainActivity.this, deviceMap);
            deviceBottomSheetDialog.show();
        }
    };

    private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_ON:
                            Log.d(TAG, "onReceive: bluetooth status: STATE_ON");
                            showMainMenu(true);
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            Log.d(TAG, "onReceive: bluetooth status: STATE_OFF");
                            showMainMenu(false);
                            break;
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Log.d(TAG, "onReceive: bluetooth status: BluetoothDevice.ACTION_ACL_CONNECTED " + device.getName());
                    setViewBluetoothConnected();

                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.d(TAG, "onReceive: bluetooth status: BluetoothDevice.ACTION_ACL_DISCONNECTED");
                    setViewBluetoothOn();

                    break;
            }
        }
    };

    private final BroadcastReceiver bluetoothDiscoveryBrodcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                double distance = calcBleDistance(rssi);
                int i = Constant.COLOR_LIBRARY[deviceMap.size()%Constant.COLOR_LIBRARY.length];
                NearbyBluetoothModel nearbyBluetoothModel = new NearbyBluetoothModel(device.getName(), device.getAddress(), distance, i);
                deviceMap.put(device.getName(), nearbyBluetoothModel);

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                deviceMap.clear();
                layoutForRadius.removeAllViewsInLayout();
                bluetoothAdapter.startDiscovery();
            }
            MainActivity.getInstace().updateBleCount();
        }
    };


    private void setViewBluetoothOff(){
        tvBluetoothDescription.setText(getString(R.string.off));
        ivBluetoothSwitch1.setImageResource(R.drawable.ic_switch_off);
    }
    private void setViewBluetoothOn(){
        tvBluetoothDescription.setText(getString(R.string.on));
        ivBluetoothSwitch1.setImageResource(R.drawable.ic_switch_on);
        layoutGetPaired.setVisibility(View.VISIBLE);
        layoutSaturation.setVisibility(View.GONE);
        layoutHeartBeat.setVisibility(View.GONE);
    }
    private void setViewBluetoothConnected(){
        tvBluetoothDescription.setText(getString(R.string.connected));
        ivBluetoothSwitch1.setImageResource(R.drawable.ic_switch_on);
        layoutGetPaired.setVisibility(View.GONE);
        layoutSaturation.setVisibility(View.VISIBLE);
        layoutHeartBeat.setVisibility(View.VISIBLE);
    }

    private void showMainMenu(boolean isVisible){
        if(isVisible){
            layoutTopOn.setVisibility(View.VISIBLE);
            layoutBottom.setVisibility(View.VISIBLE);
            layoutTopOff.setVisibility(View.GONE);
            setViewBluetoothOn();
        } else{
            layoutTopOn.setVisibility(View.GONE);
            layoutBottom.setVisibility(View.GONE);
            layoutTopOff.setVisibility(View.VISIBLE);
            setViewBluetoothOff();
        }
    }

    private void setDevice(float distance, int color){

        //Distance <= 0.4 meter considered stick/touch owner so, we made it touch the center's surface
        float anchor = Math.max(0.125f * distance, 0.05f);
        Guideline guidelineLeft = getNewGuideline(this, ConstraintLayout.LayoutParams.VERTICAL);
        guidelineLeft.setId(View.generateViewId());
        guidelineLeft.setGuidelinePercent(0.5f-anchor);
        layoutForRadius.addView(guidelineLeft);

        Guideline guidelineRight = getNewGuideline(this, ConstraintLayout.LayoutParams.VERTICAL);
        guidelineRight.setId(View.generateViewId());
        guidelineRight.setGuidelinePercent(0.5f+anchor);
        layoutForRadius.addView(guidelineRight);

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(0, 0);
        layoutParams.leftToLeft = guidelineLeft.getId();
        layoutParams.rightToRight = guidelineRight.getId();
        layoutParams.topToTop = ConstraintSet.PARENT_ID;
        layoutParams.bottomToBottom = ConstraintSet.PARENT_ID;
        layoutParams.dimensionRatio = "1:1";

        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setId(View.generateViewId());
        imageView.setBackgroundResource(R.drawable.circle_blue_outer);
        imageView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(color)));
        imageView.setLayoutParams(layoutParams);
        layoutForRadius.addView(imageView);

    }

    private Guideline getNewGuideline(Context context, int orientation) {
        Guideline guideline = new Guideline(context);
        guideline.setId(Guideline.generateViewId());
        ConstraintLayout.LayoutParams lp =
                new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT);
        lp.orientation = orientation;
        guideline.setLayoutParams(lp);

        return guideline;
    }


    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            Log.d(TAG, "handleMessage: " + message.what);
            switch (message.what) {
                case MESSAGE_STATE_CHANGED:
                    switch (message.arg1) {
                        case BluetoothUtil.STATE_NONE:
                        case BluetoothUtil.STATE_LISTEN:
                            tvBluetoothDescription.setText("Not Connected");
                            break;
                        case BluetoothUtil.STATE_CONNECTING:
                            tvBluetoothDescription.setText("Connecting...");
                            break;
                        case BluetoothUtil.STATE_CONNECTED:
                            tvBluetoothDescription.setText("Connected");
                            break;
                    }
                    break;
                case MESSAGE_READ:
                    byte[] buffer = (byte[]) message.obj;
                    String inputBuffer = new String(buffer, 0, message.arg1);
                    Log.d(TAG, "MainActivity handleMessage: MESSAGE_READ " + inputBuffer);
                    String[] result = inputBuffer.split(":");
                    if(result[0].equals("spo2")) tvSaturationValue.setText(result[1]);
                    else if(result[0].equals("bpm")) tvHeartbeatValue.setText(result[1]);
                    break;
                case MESSAGE_WRITE:
                    byte[] buffer1 = (byte[]) message.obj;
                    String outputBuffer = new String(buffer1);
                    Log.d(TAG, "handleMessage: MESSAGE_WRITE " + outputBuffer);
                    break;
                case MESSAGE_DEVICE_NAME:
                    String connectedDevice = message.getData().getString(Constant.DEVICE_NAME);
                    Log.d(TAG, "MainActivity handleMessage: MESSAGE_DEVICE_NAME "+ connectedDevice);
                    Toast.makeText(getApplicationContext(), connectedDevice, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Log.d(TAG, "MainActivity handleMessage: MESSAGE_TOAST " + message.getData().getString(Constant.TOAST));
                    Toast.makeText(getApplicationContext(), message.getData().getString(Constant.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });
}