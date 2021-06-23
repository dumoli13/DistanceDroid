package com.ridho.skripsi.model;

public class NearbyBluetoothModel {

    String name;
    String address;
    double distance;
    int color;

    public NearbyBluetoothModel() {
    }

    public NearbyBluetoothModel(String name, String address, double distance, int color) {
        this.name = name;
        this.address = address;
        this.distance = distance;
        this.color = color;
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
