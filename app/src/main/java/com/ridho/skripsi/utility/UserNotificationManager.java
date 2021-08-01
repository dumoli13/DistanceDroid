package com.ridho.skripsi.utility;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.ridho.skripsi.R;
import com.ridho.skripsi.view.dialog.GeneralDialog;
import com.ridho.skripsi.view.activity.MainActivity;
import com.ridho.skripsi.view.dialog.PermissionAlertDialog;

public class UserNotificationManager {

    private static final String channelId = "SEND_TO_EVERYONE";
    private static final String channelName = "DistanceDroid";

    public static void showErrorNotification(Context context, String body) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Constant.IS_ALERT_NOTIFICATION_CLICKED, true);

        int uniqueID = Commons.generateRandom();

        PendingIntent pendingIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(intent)
                .getPendingIntent(uniqueID, PendingIntent.FLAG_ONE_SHOT);

//        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.US).format(new Date()));
        int id = 1;

        showNotification(context, context.getString(R.string.app_name), body, pendingIntent, id);
    }

    //Format: UserNotificationManager.showErrorNotification(getApplicationContext(), getString(R.string.distance_alert_body));
    private static void showNotification(Context context, String title, String body, PendingIntent pendingIntent, int uniqueID){
        NotificationManager notificationManager = context.getSystemService(android.app.NotificationManager.class);
        NotificationChannel channel = null;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            channel = new NotificationChannel(channelId, channelName, android.app.NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title != null? title : channelName)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(Commons.drawableToBitmapConverter(R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.color_notification, null))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);


        notificationManager.notify(uniqueID, notificationBuilder.build());
    }

    //Format: UserNotificationManager.showErrorPermission(MainActivity.this);
    public static void showErrorPermission(Activity activity){
        PermissionAlertDialog dialog = new PermissionAlertDialog();
        dialog.setContext(activity);
        dialog.show(activity.getFragmentManager(), "showErrorPermission");
    }

    //Format: UserNotificationManager.showDistanceDialog(MainActivity.this, Constant.ALERT_DISTANCE_WARNING, device.getName());
    public static void showDistanceDialog(Activity activity, String flag, String deviceName){
        StringBuilder message = new StringBuilder("You are too close with device: " + deviceName);

        GeneralDialog dialog = new GeneralDialog();
        dialog.setContext(activity);
        dialog.setFlag(flag);
        dialog.setMessage(message.toString());
        dialog.show(activity.getFragmentManager(), "showDistanceDialog");
    }

    //Format : UserNotificationManager.showGeneralError(MainActivity.this, Constant.ALERT_NO_BLUETOOTH_DEVICE);
    public static void showGeneralError(Activity activity, String flag){
        GeneralDialog dialog = new GeneralDialog();
        dialog.setContext(activity);
        dialog.setFlag(flag);
        dialog.show(activity.getFragmentManager(), "showDistanceDialog");
    }

}
