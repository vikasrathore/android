/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.notificationactivationlib;

import java.util.HashSet;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.cube26.trendingnow.util.CLog;

public class AppActivationReceiver extends BroadcastReceiver {

    private SharedPreferences mSharedPrefs;
    @Override
    public void onReceive(Context ctx, Intent incomingIntent) {

        ActivationDTO activationDTO = (ActivationDTO)incomingIntent
                .getSerializableExtra(Util.ACTIVATION_EXTRA);
        String pckgName = activationDTO.getPackageName();
        if (Util.isAppAlreadyInstalled(pckgName, ctx)) {
            Intent activateApp = new Intent();
            mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            Set<String> set = new HashSet<String>(mSharedPrefs.getStringSet(Util.APP_ACTIVATION_PACKAGES, new HashSet<String>()));
            if(set.contains(pckgName)){
                CLog.d(Util.TAGC26, "Received activation intent but cancelling as already activated for package :: "+ pckgName);
                return;
            }
            try {
                activateApp.setComponent(new ComponentName(activationDTO.getPackageName(), activationDTO.getActivityName()));
                for (AppExtraKeyValuePair pair : activationDTO.getAppExtraKeyValuePairs()) {
                    activateApp.putExtra(pair.getKeyString(), pair.getValueString());
                }
                CLog.d(Util.TAGC26, "Starting activity at activation time, intent :: "+ activateApp);
                ctx.startActivity(activateApp);
                
                SharedPreferences.Editor editor = mSharedPrefs.edit();
                set.add(pckgName);
                editor.putStringSet(Util.APP_ACTIVATION_PACKAGES, set);
                editor.commit();
                SendAnalytics(activationDTO, ctx);
                return;
            } catch (Exception e) {
                CLog.d(Util.TAGC26, "Starting activity at activation time failed for given parameter");
            }
            activateApp = ctx.getPackageManager().getLaunchIntentForPackage(
                    pckgName);
            try {
                CLog.d(Util.TAGC26, "Starting activity at activation time using launching intent, intent :: "+ activateApp);
                ctx.startActivity(activateApp);

                SharedPreferences.Editor editor = mSharedPrefs.edit();
                set.add(pckgName);
                editor.putStringSet(Util.APP_ACTIVATION_PACKAGES, set);
                editor.commit();
                SendAnalytics(activationDTO, ctx);
            } catch (Exception e) {
                e.printStackTrace();
                CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
            }
        }
    }

    private void SendAnalytics(ActivationDTO activationDTO, Context context){
        EventDTO eventDTO = new EventDTO();
        eventDTO.setEventAction("Auto_activated");
        eventDTO.setEventCategory("Activation");
        eventDTO.setEventLabel("package_name");
        eventDTO.setEventValue(activationDTO.getPackageName());
        
        new SendAnalyticsData(context, eventDTO).execute("");
    }
}