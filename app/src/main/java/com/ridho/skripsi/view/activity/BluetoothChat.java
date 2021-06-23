package com.ridho.skripsi.view.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ridho.skripsi.R;
import com.ridho.skripsi.utility.BluetoothUtil;
import com.ridho.skripsi.utility.Constant;

import static com.ridho.skripsi.utility.Constant.MESSAGE_DEVICE_NAME;
import static com.ridho.skripsi.utility.Constant.MESSAGE_READ;
import static com.ridho.skripsi.utility.Constant.MESSAGE_STATE_CHANGED;
import static com.ridho.skripsi.utility.Constant.MESSAGE_TOAST;
import static com.ridho.skripsi.utility.Constant.MESSAGE_WRITE;

public class BluetoothChat extends AppCompatActivity {
    private static String TAG = "DOMS BluetoothCHat";

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothUtil bluetoothUtil;

    private EditText etSpo2;
    private EditText etBpm;
    private Button btnSendSpo2;
    private Button btnSendBpm;
    private Button btnDiscoverable;

    private String connectedDevice;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case MESSAGE_STATE_CHANGED:
                    switch (message.arg1) {
                        case BluetoothUtil.STATE_NONE:
                        case BluetoothUtil.STATE_LISTEN:
                            setState("Not Connected");
                            break;
                        case BluetoothUtil.STATE_CONNECTING:
                            setState("Connecting...");
                            break;
                        case BluetoothUtil.STATE_CONNECTED:
                            setState("Connected: " + connectedDevice);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] buffer1 = (byte[]) message.obj;
                    String outputBuffer = new String(buffer1);
                    Log.d(TAG, "handleMessage: MESSAGE_WRITE " + outputBuffer);
                    break;
                case MESSAGE_READ:
                    byte[] buffer = (byte[]) message.obj;
                    String inputBuffer = new String(buffer, 0, message.arg1);
                    Log.d(TAG, "handleMessage: MESSAGE_READ " + inputBuffer);
                    break;
                case MESSAGE_DEVICE_NAME:
                    connectedDevice = message.getData().getString(Constant.DEVICE_NAME);
                    Toast.makeText(context, connectedDevice, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(context, message.getData().getString(Constant.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    private void setState(CharSequence subTitle) {
        getSupportActionBar().setSubtitle(subTitle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_chat);

        context = this;

        initElement();
        initBluetooth();
        bluetoothUtil = new BluetoothUtil(context, handler);
    }

    private void initElement() {
        etSpo2 = findViewById(R.id.et_spo2);
        etBpm = findViewById(R.id.et_bpm);
        btnSendSpo2 = findViewById(R.id.btn_send_spo2);
        btnSendBpm = findViewById(R.id.btn_send_bpm);
        btnDiscoverable = findViewById(R.id.btn_discoverable);

        btnSendSpo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etSpo2.getText().toString();
                if(!message.isEmpty()){
                    message = "spo2:" + message;
                    bluetoothUtil.write(message.getBytes());
                    etSpo2.setText("");
                }
            }
        });

        btnSendBpm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etBpm.getText().toString();
                if(!message.isEmpty()){
                    message = "bpm:" + message;
                    bluetoothUtil.write(message.getBytes());
                    etBpm.setText("");
                }
            }
        });

        btnDiscoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.enable();
                }

                if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoveryIntent);
                }
            }
        });
    }

    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "No bluetooth found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothUtil != null) {
            bluetoothUtil.stop();
        }
    }
}
