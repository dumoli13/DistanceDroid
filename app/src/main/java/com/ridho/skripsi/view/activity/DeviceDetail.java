package com.ridho.skripsi.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.LeadingMarginSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ridho.skripsi.R;
import com.ridho.skripsi.utility.Constant;
import com.ridho.skripsi.utility.SampleGattAttributes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DeviceDetail extends AppCompatActivity {

    // UI declaration from xml layout
    private TextView nameValue;
    private TextView addressValue;
    private TextView peripheralTextView;
    private TextView saturationValue;
    private TextView heartbeatValue;
    private ImageView btnBack;

    // Android system class for BLE communication
    private BluetoothAdapter btAdapter;
    private BluetoothGatt bluetoothGatt;

    // Array for enable BLE notification queue
    private final ArrayList<BluetoothGattDescriptor> descriptorWriteQueue = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        nameValue = findViewById(R.id.name_value);
        addressValue = findViewById(R.id.address_value);
        saturationValue = findViewById(R.id.saturation_value);
        heartbeatValue = findViewById(R.id.heartbeat_value);
        peripheralTextView = findViewById(R.id.PeripheralTextView);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView btnClearText = findViewById(R.id.btn_clear_text);
        btnClearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                peripheralTextView.setText("");
            }
        });

        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();

        connectDevice();

    }

    @Override
    public void onBackPressed() {
        // Disconnect device when user leave activity
        // disconnectDevice();
        super.onBackPressed();
    }

    // BLE GATT call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case 0:
                    DeviceDetail.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.append(indentedText(getTime()+" > Device disconnected\n") );
                        }
                    });
                    break;
                case 2:
                    DeviceDetail.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.append(indentedText(getTime()+" > Device connected\n") );
                        }
                    });

                    // discover services and characteristics for this device
                    gatt.discoverServices();

                    break;
                default:
                    DeviceDetail.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.append(indentedText(getTime()+" > BLE connection error\n"));
                        }
                    });
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // Loops through available Services.
            for (BluetoothGattService gattService : gatt.getServices()) {

                final String serviceUuid = gattService.getUuid().toString();
                System.out.println("Service discovered: " + serviceUuid);

                DeviceDetail.this.runOnUiThread(new Runnable() {
                    public void run() {
                        if (serviceUuid.equals(SampleGattAttributes.HEART_RATE_SERVICE) || serviceUuid.equals(SampleGattAttributes.SPO_RATE_SERVICE)) {
                            peripheralTextView.append(indentedText(getTime()+" > Service disovered: "+serviceUuid+"\n"));
                        }
                    }
                });

                // Loops through available Characteristics.
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                    final String charUuid = gattCharacteristic.getUuid().toString();
                    System.out.println("Characteristic discovered for service: " + charUuid);

                    // Create request heart rate characteristic notification
                    if (charUuid.equals(SampleGattAttributes.HEART_RATE_MEASUREMENT)) {
                        gatt.setCharacteristicNotification(gattCharacteristic, true);
                        BluetoothGattDescriptor desc = gattCharacteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                        // Add Request to queue
                        descriptorWriteQueue.add(desc);
                    }

                    // Create request SPO characteristic notification
                    if (charUuid.equals(SampleGattAttributes.SPO_RATE_MEASUREMENT)) {
                        gatt.setCharacteristicNotification(gattCharacteristic, true);
                        BluetoothGattDescriptor desc = gattCharacteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                        // Add Request to queue
                        descriptorWriteQueue.add(desc);
                    }

                    DeviceDetail.this.runOnUiThread(new Runnable() {
                        public void run() {
                            if (charUuid.equals(SampleGattAttributes.HEART_RATE_MEASUREMENT) || charUuid.equals(SampleGattAttributes.SPO_RATE_MEASUREMENT)) {
                                peripheralTextView.append(indentedText(getTime()+" > Characteristic discovered : "+charUuid+"\n"));
                            }
                        }
                    });

                }

                writeGattDescriptor();

            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            // Request sent, delete from queue
            descriptorWriteQueue.remove(descriptor);
            writeGattDescriptor();
            System.out.println("Request Notification: " + descriptor.getCharacteristic().getUuid());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            // Read and parsing data from device
            String rawData = Arrays.toString(characteristic.getValue());
            String data = rawData.replace("[","").replace("]","");

            String finalData = "";
            if (characteristic.getUuid().toString().equals(SampleGattAttributes.HEART_RATE_MEASUREMENT)) {
                finalData = "Heart Beat : " +data;
                System.out.println(finalData);
                heartbeatValue.setText(data);
                heartbeatValue.setTextColor(getColor(R.color.library12));
            } else if (characteristic.getUuid().toString().equals(SampleGattAttributes.SPO_RATE_MEASUREMENT)) {
                finalData = "SPO value : " +data;
                System.out.println(finalData);
                saturationValue.setText(data);
                if(Integer.parseInt(data) < 95) saturationValue.setTextColor(getColor(R.color.maroon_red));
                else saturationValue.setTextColor(getColor(R.color.library12));
            }

            String monitorData = finalData;
            DeviceDetail.this.runOnUiThread(new Runnable() {
                public void run() {
                    peripheralTextView.append(indentedText(getTime()+" > "+ monitorData +"\n"));
                }
            });
        }
    };

    private void writeGattDescriptor() {
        // Send request characteristic notification one by one
        if (descriptorWriteQueue.size() > 0) {
            bluetoothGatt.writeDescriptor(descriptorWriteQueue.get(0));
        }
    }

    private String getTime() {
        return new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault()).format(new Date());
    }

    private void connectDevice() {
        // Get intent data from device adapter
        final Intent intent = getIntent();
        final String btName = intent.getStringExtra(Constant.EXTRA_DEVICE_NAME);
        final String btAddress = intent.getStringExtra(Constant.EXTRA_DEVICE_ADDRESS);

        nameValue.setText(btName);
        addressValue.setText(btAddress);

        peripheralTextView.append(indentedText(getTime()+" > Connect to device : " +btAddress+ "\n"));
        BluetoothDevice btDevice = btAdapter.getRemoteDevice(btAddress);
        bluetoothGatt = btDevice.connectGatt(this, false, btleGattCallback);
    }

    private void disconnectDevice() {
        peripheralTextView.append(indentedText(getTime()+" > Disconnecting from device : " +addressValue.getText()+ "\n"));
        bluetoothGatt.disconnect();
    }

    static SpannableString indentedText(String text) {
        SpannableString result=new SpannableString(text);
        result.setSpan(new LeadingMarginSpan.Standard(0, 24),0,text.length(),0);
        return result;
    }
}