package com.ridho.skripsi.view.dialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ridho.skripsi.R;
import com.ridho.skripsi.model.NearbyBluetoothModel;
import com.ridho.skripsi.utility.Constant;
import com.ridho.skripsi.view.adapter.NearbyDeviceAdapter;

import java.util.HashMap;
import java.util.Map;

import static com.ridho.skripsi.utility.Commons.calcBleDistance;
import static com.ridho.skripsi.utility.Constant.BLE_MAX_DISTANCE;

public class DeviceBottomSheetDialog extends BottomSheetDialog {
    private static final String TAG = "DOMS";
    private View view;
    private Context context;
    private TextView tvBtmsheetTitle;
    private RecyclerView deviceList;

    private BluetoothAdapter bluetoothAdapter;
    private Map<String, NearbyBluetoothModel> deviceMap = new HashMap<>();
    private NearbyDeviceAdapter nearbyDeviceAdapter;

    public DeviceBottomSheetDialog(@NonNull Context context) {
        super(context);
        this.context = context;

        view = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);
        setContentView(view);
    }

    public DeviceBottomSheetDialog(@NonNull Context context, Map<String, NearbyBluetoothModel> deviceMap) {
        super(context);
        this.context = context;
        nearbyDeviceAdapter = new NearbyDeviceAdapter(deviceMap);

        view = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);
        setContentView(view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvBtmsheetTitle = findViewById(R.id.tv_btmsheet_title);
        tvBtmsheetTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Bottom sheet clicked", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
        deviceList = findViewById(R.id.rv_devices);
        deviceList.setLayoutManager(new LinearLayoutManager(getContext()));
        deviceList.setAdapter(nearbyDeviceAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();
        bluetoothAdapter.startDiscovery();

        IntentFilter discoverDeviceFilter = new IntentFilter();
        discoverDeviceFilter.addAction(BluetoothDevice.ACTION_FOUND);
        discoverDeviceFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(bluetoothDiscoveryBrodcastReceiver, discoverDeviceFilter);
    }

    @Override
    public void dismiss() {
        context.unregisterReceiver(bluetoothDiscoveryBrodcastReceiver);
        super.dismiss();
    }

    private final BroadcastReceiver bluetoothDiscoveryBrodcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                double distance = calcBleDistance(rssi);
                if(distance < BLE_MAX_DISTANCE){

                    deviceMap.put(
                            device.getAddress(),
                            new NearbyBluetoothModel(
                                    device.getName(),
                                    device.getAddress(),
                                    distance,
                                    Constant.COLOR_LIBRARY[deviceMap.size()%Constant.COLOR_LIBRARY.length]
                            )
                    );
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "refreshDeviceList: start discovery");
                deviceMap.clear();
                bluetoothAdapter.startDiscovery();
            }
            nearbyDeviceAdapter.updateList(deviceMap);
        }
    };

}
