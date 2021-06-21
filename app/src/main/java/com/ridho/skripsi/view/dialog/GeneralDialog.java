package com.ridho.skripsi.view.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.app.DialogFragment;

import com.ridho.skripsi.R;
import com.ridho.skripsi.utility.Constant;

public class GeneralDialog extends DialogFragment {

    Context context;
    String message = "";
    boolean isClosed = false;
    String flag;
    boolean cancelable;

    public GeneralDialog() {
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view = inflater.inflate(R.layout.distance_alert_dialog, null);
        setupCustomUI();

        ((TextView) view.findViewById(R.id.tv_message_description)).setText(message);
        ((RelativeLayout) view.findViewById(R.id.btn_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(isClosed) getActivity().finishAffinity();
            }
        });
        builder.setView(view);


        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(cancelable);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Window window = ((AlertDialog) dialog).getWindow();
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setBackgroundDrawableResource(R.drawable.bg_white);
            }
        });

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setupCustomUI(){
        switch (flag){
            case Constant.ALERT_DISTANCE_WARNING:
                isClosed = false;
                cancelable = true;
                break;
            case Constant.ALERT_NO_BLUETOOTH_DEVICE:
                isClosed = true;
                cancelable = false;
                setMessage("Device does not support bluetooth, application will be closed");
                break;
        }
    }
}
