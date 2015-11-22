package com.cube26.trendingapps.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.cube26.trendingnow.util.CLog;
import com.cube26.trendingnow.util.Util;

import com.cube26.trendingnow.util.AppDownloaderService;

public class AppDownloadReceiver extends BroadcastReceiver 
{    
	 private static int mUpdateFrequency = 6*60*60*1000; //Every 6 hours
//     private static int mUpdateFrequency = 5*60*1000; //Every 5 Minute
	 private static int mUpdateStartDiff = 60*1000; // start after 1 minute from current time..
	 private static final String TAG = Util.TAGC26;
	 
     @Override
     public void onReceive(Context context, Intent intent) 
     {   
         CLog.d(TAG, "inside receiver AppDownloadReceiver");
         
         AppDownloaderService appDownloaderService = new AppDownloaderService(context);
         appDownloaderService.checkForAppDownload();
         
         if(intent.getAction() != null){
             if(Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(intent.getAction())){
                 CLog.d(Util.TAGC26, "boot action received for AppDownloadReceiver");
                 setAlarm(context);
             }
         }
     }

     public static void setAlarm(Context context){
         cancelAlarm(context);
         CLog.d(TAG, "setting alarm for app download service");
    	 AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    	 Intent i = new Intent(context, AppDownloadReceiver.class);
    	 PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
    	 am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + mUpdateStartDiff, mUpdateFrequency, pi);
     }

     public static void cancelAlarm(Context context)
     {
    	 Intent i = new Intent(context, AppDownloadReceiver.class);
    	 PendingIntent sender = PendingIntent.getBroadcast(context, 0, i, 0);
    	 AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	 alarmManager.cancel(sender);
     }
}
