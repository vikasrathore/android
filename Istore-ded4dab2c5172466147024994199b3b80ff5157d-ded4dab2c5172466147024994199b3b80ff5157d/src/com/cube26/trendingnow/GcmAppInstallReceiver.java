/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.trendingnow;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import com.cube26.istore.R;
import com.cube26.trendingapps.analytics.SendAnalyticsData;
import com.cube26.trendingnow.util.CLog;
import com.cube26.trendingnow.util.Util;

public class GcmAppInstallReceiver extends BroadcastReceiver {

    private SharedPreferences mSharedPrefs;
    public static final int NOTIFICATION_ID = 234;
    
    @Override
    public void onReceive(Context context, Intent intent) {

        ArrayList<String> appsToInstall = intent.getStringArrayListExtra(Util.EXTRA_GCM_BROADCAST_APP_LIST);
        ArrayList<String> appsNamesToInstall = intent.getStringArrayListExtra(Util.EXTRA_GCM_BROADCAST_APP_NAMES_LIST);
        boolean shouldInstall = intent.getBooleanExtra(Util.EXTRA_GCM_BROADCAST_INSTALL_FLAG, false);
        int notificationId = intent.getIntExtra(Util.EXTRA_GCM_BROADCAST_NOTIFICATION_ID, 0);
        boolean deleteOnCancel = intent.getBooleanExtra(Util.EXTRA_GCM_BROADCAST_DELETE_ON_CANCEL, true);
        boolean openApps = intent.getBooleanExtra(Util.EXTRA_GCM_BROADCAST_OPEN_APP, true);
        int openAppInterval = intent.getIntExtra(Util.EXTRA_GCM_BROADCAST_OPEN_APP_INTERVAL, 1000);
        
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);

        if(shouldInstall){
            CLog.d(Util.TAGC26, "Received install intent");
            //new SendAnalyticsData(context, Util.GCM_EVENT_OK_CLICKED).execute(Util.GCM_EVENT);
            Util.createAndSendAnalyticsData(Util.GCM_EVENT_OK_CLICKED, Util.CATEGORY_AUTO, Util.APP_PACKAGE_NAME, Util.GCM_EVENT, context);
            if(null!=appsToInstall && appsToInstall.size()>0){
                for (int i= 0; i< appsToInstall.size(); i++) {
                    
                    String appPackage = appsToInstall.get(i);
                    String appName = appsNamesToInstall.get(i);
                    
                    String folderPath = Environment.getExternalStorageDirectory()
                            + Util.TRENDING_APPS_FOLDER_NAME;
                    String filePath = folderPath + appPackage + ".apk";
                    File apkFile = new File(filePath);
                    if(apkFile.exists()){
                        CLog.d(Util.TAGC26, "GCM Install app :: "+ apkFile.getPath());
                        
                        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                        Set<String> set = new HashSet<String>(mSharedPrefs.getStringSet(Util.GCM_INSTALL_PACKAGES, new HashSet<String>()));
                        SharedPreferences.Editor editor = mSharedPrefs.edit();
                        set.add(appPackage);
                        editor.putStringSet(Util.GCM_INSTALL_PACKAGES, set);
                        editor.commit();
                        
                        editor = mSharedPrefs.edit();
                        editor.putBoolean(Util.GCM_OPEN_APP_FLAG, openApps);
                        editor.commit();
                        
                        editor = mSharedPrefs.edit();
                        editor.putInt(Util.GCM_OPEN_APP_INTERVAL, openAppInterval);
                        editor.commit();
                        
                        if(Util.isPackageSystemApp(context, context.getPackageName())){
                            sendInstallNotification(context, appName, appPackage);
                            Util.installPackage(apkFile);
                        }else{
                            Intent installIntent = new Intent(Intent.ACTION_VIEW);
                            installIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                context.startActivity(installIntent);
                            } catch (Exception e) {
                                CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
                            }
                        }
                    }
                }
            }
        }else{
            CLog.d(Util.TAGC26, "Received cancel intent");
         //   new SendAnalyticsData(context, Util.GCM_EVENT_CANCEL_CLICKED).execute(Util.GCM_EVENT);
            Util.createAndSendAnalyticsData(Util.GCM_EVENT_CANCEL_CLICKED, Util.CATEGORY_AUTO, Util.APP_PACKAGE_NAME, Util.GCM_EVENT, context);
            if(null!=appsToInstall && appsToInstall.size()>0 && deleteOnCancel){
                for (String appPackage : appsToInstall) {
                    String folderPath = Environment.getExternalStorageDirectory()
                            + Util.TRENDING_APPS_FOLDER_NAME;
                    String filePath = folderPath + appPackage + ".apk";
                    File apkFile = new File(filePath);
                    if(apkFile.exists())
                        apkFile.delete();
                }
            }
            openTrendingApp(context);
        }
    }
    
    private void openTrendingApp(Context context){
        Intent openApp = new Intent();
        openApp = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        try {
            context.startActivity(openApp);
            CLog.d(Util.TAGC26, "Starting trending app on cancel click");
        } catch (Exception e) {
            CLog.d(Util.TAGC26, "Error in starting trending app on cancel click"+ e.getMessage());
        }
    }
    
    private void sendInstallNotification(Context context, String appName, String packageName){
        NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.ic_trending_apps)
        .setContentTitle("Installing "+ appName +"...")
        .setProgress(100, 50, true);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(packageName, NOTIFICATION_ID, notification);
    }
}
