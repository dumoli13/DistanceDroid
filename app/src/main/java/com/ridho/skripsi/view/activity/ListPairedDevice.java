package com.ridho.skripsi.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ridho.skripsi.R;
import com.ridho.skripsi.model.NearbyBluetoothModel;
import com.ridho.skripsi.model.PairedBluetoothModel;
import com.ridho.skripsi.utility.Constant;
import com.ridho.skripsi.view.ViewCallback.ListPairedDeviceViewCallback;
import com.ridho.skripsi.view.adapter.PairedDeviceAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.ridho.skripsi.utility.Commons.calcBleDistance;

public class ListPairedDevice extends AppCompatActivity implements ListPairedDeviceViewCallback {

    private static final String TAG = "DOMS ListPairedDevice";

    private TextView tvBluetoothStatus;
    private ImageView ivBluetoothSwitch;
    private RecyclerView deviceList;
    private ConstraintLayout layoutOff;
    private ConstraintLayout layoutOn;

    private PairedDeviceAdapter adapter;
    private BluetoothAdapter bluetoothAdapter;
    private Map<String, PairedBluetoothModel> deviceMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_paired_device);

        tvBluetoothStatus = findViewById(R.id.tv_bluetooth_status);
        ivBluetoothSwitch = findViewById(R.id.iv_bluetooth_switch);
        ivBluetoothSwitch.setOnClickListener(bluetoothSwitchClickListener);
        layoutOff = findViewById(R.id.layout_off);
        layoutOn = findViewById(R.id.layout_on);

        adapter = new PairedDeviceAdapter(this);

        deviceList = findViewById(R.id.rv_paired_device);
        deviceList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        deviceList.setAdapter(adapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceMap.put(device.getName(), new PairedBluetoothModel(device.getName(), device.getAddress(),device.getBluetoothClass()));
            }
            adapter.updateList(deviceMap);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(bluetoothBroadcastReceiver);
    }

    View.OnClickListener bluetoothSwitchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            } else {
                bluetoothAdapter.enable();
            }
        }
    };

    @Override
    public void onItemClick(PairedBluetoothModel model) {
        Log.d(TAG, "onItemClick: " + model.getName());
        Intent intent = new Intent();
        intent.putExtra(Constant.EXTRA_DEVICE_NAME, model.getName());
        intent.putExtra(Constant.EXTRA_DEVICE_ADDRESS, model.getAddress());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void showMainMenu(boolean isVisible){
        if(isVisible){
            layoutOff.setVisibility(View.GONE);
            layoutOn.setVisibility(View.VISIBLE);
            tvBluetoothStatus.setText("On");
            ivBluetoothSwitch.setImageResource(R.drawable.ic_switch_on);
        } else{
            layoutOff.setVisibility(View.VISIBLE);
            layoutOn.setVisibility(View.GONE);
            tvBluetoothStatus.setText("Off");
            ivBluetoothSwitch.setImageResource(R.drawable.ic_switch_off);

        }
    }

    private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch(state) {
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "onReceive: bluetooth status: STATE_ON");
                        showMainMenu(true);
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: bluetooth status: STATE_OFF");
                        showMainMenu(false);
                        break;
                }
            }
        }
    };
}