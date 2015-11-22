/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.trendingapps.receivers;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.cube26.trendingapps.analytics.SendAnalyticsData;
import com.cube26.trendingnow.util.CLog;
import com.cube26.trendingnow.util.Util;

public class AppInstallListener extends BroadcastReceiver {

    public Intent myBroadcastIntent = null;
    private Context mContext = null;
    private SharedPreferences mSharedPrefs;
    public static final int NOTIFICATION_ID = 234;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_PACKAGE_ADDED) || intent.getAction().equalsIgnoreCase(Intent.ACTION_PACKAGE_REPLACED)) {
            Uri uri = intent.getData();
            String pkg = uri != null ? uri.getSchemeSpecificPart() : null;
            //addShortcutForPackageName(mContext, pkg, intent);
            removeApk(pkg);
        }
    }

    private Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap); 
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void addShortcutForPackageName(Context context, String packageName, Intent intent) {
        try {
            File apkFile = null;
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_PACKAGE_ADDED)){
                apkFile = new File(Environment.getExternalStorageDirectory(),
                        Util.TRENDING_APPS_FOLDER_NAME + packageName + ".apk");
                if (!apkFile.exists()) {
                    return;
                }
                Intent shortcutIntent;
                PackageManager manager = context.getPackageManager();
                shortcutIntent = manager.getLaunchIntentForPackage(packageName);
                shortcutIntent.setAction(Intent.ACTION_MAIN);

                ApplicationInfo appInfo;
                appInfo = manager.getApplicationInfo(packageName, 0);
                Drawable icon = manager.getApplicationIcon(appInfo);
                Bitmap iconBitmap = drawableToBitmap(icon);
                String name = (String) manager.getApplicationLabel(appInfo);

                Intent addIntent = new Intent();
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, iconBitmap);
                addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                context.sendBroadcast(addIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        

    }
    private void removeApk(String pkg) {

        File apkFile = new File(Environment.getExternalStorageDirectory(),
                Util.TRENDING_APPS_FOLDER_NAME + pkg + ".apk");
        if (apkFile.exists()) {
            //delete the apk file and send a broadcast for install completion
            apkFile.delete();

            myBroadcastIntent = new Intent(Util.DOWNLOAD_RECEIVER_INTENT_NAME);
            sendStatusBroadcast(pkg, Util.INSTALL_COMPLETED, mContext);
           // new SendAnalyticsData(mContext, Util.INSTALLED_APP).execute(pkg);
            Util.createAndSendAnalyticsData(Util.INSTALLED_APP, Util.CATEGORY_MANUAL, Util.APP_PACKAGE_NAME, pkg, mContext);
            try {

                mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                Set<String> set = new HashSet<String>(mSharedPrefs.getStringSet(Util.GCM_INSTALL_PACKAGES, new HashSet<String>()));
                if(set.contains(pkg)){
                    
                    //new SendAnalyticsData(mContext, Util.GCM_EVENT_INSTALLED).execute(pkg);
                    Util.createAndSendAnalyticsData(Util.INSTALLED_APP, Util.CATEGORY_AUTO, Util.APP_PACKAGE_NAME, pkg, mContext);
                    SharedPreferences.Editor editor = mSharedPrefs.edit();
                    set.remove(pkg);
                    editor.putStringSet(Util.GCM_INSTALL_PACKAGES, set);
                    editor.commit();

                    boolean openApps = mSharedPrefs.getBoolean(Util.GCM_OPEN_APP_FLAG, true);
                    int openAppInterval = mSharedPrefs.getInt(Util.GCM_OPEN_APP_INTERVAL, 5000);
                    
                    sendInstalledNotification(mContext, pkg);
                    CLog.d(Util.TAGC26, "Should open apps ::"+ openApps + " :: "+ openAppInterval);
                    if(openApps){
                        OpenAppRunnable openAppRunnable = new OpenAppRunnable();
                        openAppRunnable.setParams(mContext, pkg, openAppInterval);
                        new Thread(openAppRunnable).start();
                    }
                }
            } catch (Exception e) {
                CLog.e(Util.TAGC26, "Exception in gcm after install things ::"+ e.getMessage());
            }
        }
    }
    
    private void sendInstalledNotification(Context context, String packageName){
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(packageName, NOTIFICATION_ID);
        
    }
    
    private class OpenAppRunnable implements Runnable{
        Context context;
        String packageName;
        long timeToWait;

        public void setParams(Context context, String packageName, long timeToWait){
            this.context      = context;
            this.packageName  = packageName;
            this.timeToWait   = timeToWait;
        }
        @Override
        public void run() {
            try {
                CLog.d(Util.TAGC26, "received app open intent for package :: "+ packageName);
                Thread.sleep(timeToWait);
                Util.openAppForPackageName(context, packageName);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendStatusBroadcast(String appPackage, int downloadStatus,
            Context myContext) {
        if (myBroadcastIntent != null) {
            myBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_PACKAGE, appPackage);
            myBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS,
                    downloadStatus);
            LocalBroadcastManager.getInstance(myContext).sendBroadcast(
                    myBroadcastIntent);
        }
    }
}