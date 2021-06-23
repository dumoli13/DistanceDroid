package com.ridho.skripsi.view.adapter;

import android.util.Log;
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
import com.ridho.skripsi.model.PairedBluetoothModel;
import com.ridho.skripsi.view.ViewCallback.ListPairedDeviceViewCallback;

import java.util.HashMap;
import java.util.Map;

public class PairedDeviceAdapter extends RecyclerView.Adapter<PairedDeviceAdapter.MenuViewHolder> {
    private static final String TAG = "DOMS";
    private Map<String, PairedBluetoothModel> deviceMap = new HashMap<>();
    private ListPairedDeviceViewCallback callback;

    public PairedDeviceAdapter(ListPairedDeviceViewCallback callback){
        this.callback = callback;
    }

    public void updateList(Map<String, PairedBluetoothModel> newMapDevice){
        deviceMap.clear();
        deviceMap.putAll(newMapDevice);

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
        String[] keys = deviceMap.keySet().toArray(new String[0]);
        holder.tvName.setText(deviceMap.get(keys[position]).getName());
        holder.ivLogo.setImageResource(R.drawable.ic_desktop_logo);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "connecting to " + deviceMap.get(keys[position]).getName(), Toast.LENGTH_SHORT).show();
                callback.onItemClick(deviceMap.get(keys[position]));
            }
        });

    }

    @Override
    public int getItemCount() {
        return deviceMap.size();
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
