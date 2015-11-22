package com.cube26.trendingnow.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import com.cube26.trendingnow.util.CLog;

import com.cube26.appupdate.AppUpdate;

public class UpdateReceiver extends BroadcastReceiver 
{    
    private static int mUpdateFrequency = 3*24*60*60*1000; //Every three days
    private static int mUpdateStartDiff = 60*1000; // start after 1 minute from current time..
    private static String mUpdateUrl = "http://update.celkonvas.in/api/";//"http://update.intexvas.in/api/";
    private static final String APP_NAME = "CelkonStore";
    private static final String TAG = "UpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) 
    {   
        CLog.d(TAG, "checking app for update");
        AppUpdate appUpdate = new AppUpdate(context, mUpdateUrl+APP_NAME, getOwnPackageName(context), APP_NAME);
        appUpdate.checkForUpdate();
        CLog.d(TAG, "checked app for update");
        if(intent.getAction() != null){
            if(Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(intent.getAction())){
                CLog.d(TAG, "boot action received");
                setAlarm(context);
            }
        }
    }

    public static void setAlarm(Context context){
        cancelAlarm(context);
        CLog.d(TAG, "setting alarm");
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, UpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + mUpdateStartDiff, mUpdateFrequency, pi);
    }

    public static void cancelAlarm(Context context)
    {
        Intent i = new Intent(context, UpdateReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public static void resetAlarm(Context context){
        cancelAlarm(context);
        setAlarm(context);
    }

    private static String getOwnPackageName(Context ctx){
        try {
            PackageInfo pInfo = ctx.getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            return pInfo.packageName;
        } catch (Exception e) {
            CLog.e(TAG, "Exception ::" + e.getMessage());
            return null;
        }
    }
}
