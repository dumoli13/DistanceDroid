package com.ridho.skripsi.model;

import android.bluetooth.BluetoothClass;

public class BluetoothModel {

    String name;
    String address;
    BluetoothClass bluetoothClass;

    public BluetoothModel() {
    }

    public BluetoothModel(String name, String address, BluetoothClass bluetoothClass) {
        this.name = name;
        this.address = address;
        this.bluetoothClass = bluetoothClass;
    }
    public BluetoothModel(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BluetoothClass getBluetoothClass() {
        return bluetoothClass;
    }

    public void setBluetoothClass(BluetoothClass bluetoothClass) {
        this.bluetoothClass = bluetoothClass;
    }
}
