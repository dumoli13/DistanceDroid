package com.ridho.skripsi.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ridho.skripsi.R;
import com.ridho.skripsi.model.NearbyBluetoothModel;
import com.ridho.skripsi.model.BluetoothModel;
import com.ridho.skripsi.view.ViewCallback.ListPairedDeviceViewCallback;

import java.util.HashMap;
import java.util.Map;

public class PairedDeviceAdapter extends RecyclerView.Adapter<PairedDeviceAdapter.MenuViewHolder> {
    private Map<String, BluetoothModel> pairedDeviceMap = new HashMap<>();
    private ListPairedDeviceViewCallback callback;

    public PairedDeviceAdapter(ListPairedDeviceViewCallback callback){
        this.callback = callback;
    }

    public void updateList(Map<String, BluetoothModel> newMapDevice){
        pairedDeviceMap.clear();
        pairedDeviceMap.putAll(newMapDevice);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PairedDeviceAdapter.MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paired_device, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PairedDeviceAdapter.MenuViewHolder holder, int position) {
        String[] keys = pairedDeviceMap.keySet().toArray(new String[0]);
        int deviceType = pairedDeviceMap.get(keys[position]).getBluetoothClass().getDeviceClass();
        holder.tvName.setText(pairedDeviceMap.get(
                keys[position]).getName() != null
                ? pairedDeviceMap.get(keys[position]).getName()
                : pairedDeviceMap.get(keys[position]).getAddress() );

        holder.ivLogo.setImageResource(
                deviceType == 268
                        ? R.drawable.ic_desktop_logo
                        : deviceType == 524
                        ? R.drawable.ic_phone_logo
                        : deviceType == 1028 || deviceType == 1032
                        ?  R.drawable.ic_audio_logo : R.drawable.ic_device_logo);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "connecting to " + pairedDeviceMap.get(keys[position]).getName(), Toast.LENGTH_SHORT).show();
                callback.onItemClick(pairedDeviceMap.get(keys[position]));
            }
        });

    }

    @Override
    public int getItemCount() {
        return pairedDeviceMap.size();
    }


    static class MenuViewHolder extends RecyclerView.ViewHolder{

        ConstraintLayout layout;
        TextView tvName;
        ImageView ivLogo;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout_device);
            tvName = itemView.findViewById(R.id.tv_name);
            ivLogo = itemView.findViewById(R.id.item_logo);
        }
    }
}
