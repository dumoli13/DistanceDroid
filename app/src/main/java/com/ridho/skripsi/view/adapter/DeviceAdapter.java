package com.ridho.skripsi.view.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ridho.skripsi.R;
import com.ridho.skripsi.model.CustomBluetoothDevice;

import java.util.HashMap;
import java.util.Map;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.MenuViewHolder> {
    private static final String TAG = "DOMS";
    private Map<String, CustomBluetoothDevice> deviceMap = new HashMap<>();

    public DeviceAdapter(Map<String, CustomBluetoothDevice> newmapDevice){
        deviceMap.putAll(newmapDevice);
        Log.d(TAG, "DeviceAdapter: mapDevice.size() " + deviceMap.size());
    }

    public void updateList(Map<String, CustomBluetoothDevice> newMapDevice){
        deviceMap.clear();
        deviceMap.putAll(newMapDevice);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceAdapter.MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_detail, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceAdapter.MenuViewHolder holder, int position) {
        String[] keys = deviceMap.keySet().toArray(new String[0]);
        double distance = deviceMap.get(keys[position]).getDistance();
        String color = distance < 1 ? "#9B2226" :
                        distance < 2 ? "#C92853" :
                        distance < 3 ? "#0077B6" :
                        distance < 4 ? "#028090" : "#000000";

        holder.itemColor.setBackgroundResource(deviceMap.get(keys[position]).getColor());
        holder.tvName.setText(deviceMap.get(keys[position]).getName());
        holder.tvAddress.setText(deviceMap.get(keys[position]).getAddress());
        holder.tvDistance.setText(distance + " meters");
        holder.tvDistance.setTextColor(Color.parseColor(color));
    }

    @Override
    public int getItemCount() {
        return deviceMap.size();
    }


    static class MenuViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout itemColor;
        TextView tvName;
        TextView tvAddress;
        TextView tvDistance;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            itemColor = itemView.findViewById(R.id.item_color);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvDistance = itemView.findViewById(R.id.tv_distance);
        }
    }
}
