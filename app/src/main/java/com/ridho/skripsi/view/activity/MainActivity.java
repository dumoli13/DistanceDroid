package com.ridho.skripsi.view.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ridho.skripsi.R;
import com.ridho.skripsi.model.NearbyBluetoothModel;
import com.ridho.skripsi.utility.Commons;
import com.ridho.skripsi.utility.Constant;
import com.ridho.skripsi.utility.UserNotificationManager;
import com.ridho.skripsi.view.adapter.NearbyDeviceAdapter;
import com.ridho.skripsi.view.adapter.PairedDeviceAdapter;

import java.util.HashMap;
import java.util.Map;

import static com.ridho.skripsi.utility.Commons.calcBleDistance;
import static com.ridho.skripsi.utility.Constant.BLE_MAX_DISTANCE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DOMS MainActivity";
    private static final int pairedDeviceRequestCode = 100;

    private ConstraintLayout layoutTopOff, layoutBottom, layoutGetPaired;
    private ScrollView layoutTopOn;
    private TextView tvBluetoothDescription;
    private ImageView ivBluetoothSwitch1, ivBluetoothSwitch2;




    private RecyclerView deviceList;
    private NearbyDeviceAdapter nearbyDeviceAdapter;


    private static MainActivity instance;
    private BluetoothAdapter bluetoothAdapter;
    private Map<String, NearbyBluetoothModel> deviceMap = new HashMap<>();

    private boolean showNotif = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        nearbyDeviceAdapter = new NearbyDeviceAdapter(deviceMap);
        deviceList = findViewById(R.id.rv_devices);
        deviceList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        deviceList.setAdapter(nearbyDeviceAdapter);

        layoutTopOn = findViewById(R.id.layout_top_on);
        layoutTopOff = findViewById(R.id.layout_top_off);
        layoutBottom = findViewById(R.id.layout_bottom);
        layoutGetPaired = findViewById(R.id.layout_get_paired);
        layoutGetPaired.setOnClickListener(getPairedListener);

        tvBluetoothDescription = findViewById(R.id.tv_bluetooth_description);

        ivBluetoothSwitch1 = findViewById(R.id.iv_bluetooth_switch1);
        ivBluetoothSwitch1.setOnClickListener(bluetoothSwitchClickListener);
        ivBluetoothSwitch2 = findViewById(R.id.iv_bluetooth_switch2);
        ivBluetoothSwitch2.setOnClickListener(bluetoothSwitchClickListener);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG, "onStart: " + bluetoothAdapter);
        if(bluetoothAdapter == null){
            showMainMenu(false);
            UserNotificationManager.showGeneralError(this, Constant.ALERT_NO_BLUETOOTH_DEVICE);
        } else {
            if(bluetoothAdapter.isEnabled()){
                showMainMenu(true);
            } else{
                showMainMenu(false);
            }
            setupBluetoothBroadcast();
        }

//        activateBleScanner();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Commons.checkPermission(MainActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(bluetoothBroadcastReceiver);
        unregisterReceiver(bluetoothDiscoveryBrodcastReceiver);
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

    View.OnClickListener getPairedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(Commons.checkSupportBLE(MainActivity.this) && Commons.checkGpsEnable(MainActivity.this)){
                Intent intent = new Intent(MainActivity.this, ListOfDevice.class);
                MainActivity.this.startActivity(intent);
            }
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
                            showMainMenu(true);
                            break;
                        case BluetoothAdapter.STATE_OFF:
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
                if(distance < BLE_MAX_DISTANCE){
//                    if(showNotif && distance <= SHOW_NOTIF_DISTANCE){
//                        showNotif = false;
//                        UserNotificationManager.showErrorNotification(getApplicationContext(), getString(R.string.distance_alert_body));
//                        UserNotificationManager.showDistanceDialog(MainActivity.this, Constant.ALERT_DISTANCE_WARNING, device.getName());
//                    }
                    int color = Constant.COLOR_LIBRARY[deviceMap.size() % Constant.COLOR_LIBRARY.length];
                    NearbyBluetoothModel nearbyBluetoothModel = new NearbyBluetoothModel(device.getName(), device.getAddress(), distance, color);
                    deviceMap.put(device.getAddress(), nearbyBluetoothModel);
                    Log.d(TAG, "onReceive: device map size " + deviceMap.size());
                    nearbyDeviceAdapter.updateList(deviceMap);
                }
                else Toast.makeText(getApplicationContext(), device.getName()+" distance= " + distance, Toast.LENGTH_SHORT).show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                deviceMap.clear();
                showNotif = true;
                bluetoothAdapter.startDiscovery();
            }
        }
    };

    private void setViewBluetoothOn(){
        tvBluetoothDescription.setText(getString(R.string.on));
    }
    private void setViewBluetoothConnected(){
        tvBluetoothDescription.setText(getString(R.string.connected));
    }

    private void showMainMenu(boolean isVisible){
        if(isVisible){
            layoutTopOn.setVisibility(View.VISIBLE);
            layoutBottom.setVisibility(View.VISIBLE);
            layoutTopOff.setVisibility(View.GONE);
            ivBluetoothSwitch1.setImageResource(R.drawable.ic_switch_on);
            setViewBluetoothOn();
        } else{
            layoutTopOn.setVisibility(View.GONE);
            layoutBottom.setVisibility(View.GONE);
            layoutTopOff.setVisibility(View.VISIBLE);
            ivBluetoothSwitch1.setImageResource(R.drawable.ic_switch_off);
            tvBluetoothDescription.setText(getString(R.string.off));
        }
    }

    private void activateBleScanner(){BluetoothLeScanner bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        bleScanner.startScan(new ScanCallback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                Log.d(TAG, "onScanResult: getDeviceName: " + result.getDevice().getName());
                Log.d(TAG, "onScanResult: getDeviceAddress: " + result.getDevice().getAddress());
                Log.d(TAG, "onScanResult: getRssi: " + result.getRssi());
                Log.d(TAG, "onScanResult: getTxPower: " + result.getTxPower());
                Log.d(TAG, "---------------------------------------------------------------------");
            }
        });
    }
}
