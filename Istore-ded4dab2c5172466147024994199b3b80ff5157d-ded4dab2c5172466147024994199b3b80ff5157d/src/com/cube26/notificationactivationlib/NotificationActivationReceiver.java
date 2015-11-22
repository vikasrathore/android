package com.cube26.notificationactivationlib;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.cube26.trendingnow.util.CLog;

public class NotificationActivationReceiver extends BroadcastReceiver 
{    
    @Override
    public void onReceive(Context context, Intent intent) 
    {   
        CLog.d(Util.TAGC26, "Checking for notification");

        NotificationActivation notificationActivation = new NotificationActivation(context);
        notificationActivation.checkForNotificationAndActivation();

        if(intent.getAction() != null){
            if(Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(intent.getAction())){
                CLog.d(Util.TAGC26, "boot action received");
                setAlarmForNotificationAndActivation(context);
            }
        }
    }

    public static void setAlarmForNotificationAndActivation(Context context){
        cancelAlarm(context);
        CLog.d(Util.TAGC26, "Setting Alarm");
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, NotificationActivationReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + Util.UPDATE_START_DIFF, Util.getUpdateFrequencyFromSharedPrefs(context), pi);
    }

    public static void cancelAlarm(Context context)
    {
        Intent i = new Intent(context, NotificationActivationReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
