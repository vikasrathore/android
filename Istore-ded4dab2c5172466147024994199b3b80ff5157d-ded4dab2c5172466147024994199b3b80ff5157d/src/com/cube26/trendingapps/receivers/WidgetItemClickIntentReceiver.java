/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.trendingapps.receivers;

import java.util.HashSet;
import java.util.Set;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.cube26.trendingnow.util.CLog;
import android.widget.Toast;

import com.cube26.trendingapps.analytics.SendAnalyticsData;
import com.cube26.istore.R;
import com.cube26.trendingnow.util.Util;

public class WidgetItemClickIntentReceiver extends BroadcastReceiver {

    Context mContext;

    @Override
    public void onReceive(Context context, Intent intentI) {
        mContext = context;
        if (!intentI.getAction().equals(Util.OPEN_WIDGET_CLICK_RECEIVER)) {
            CLog.w(Util.TAGC26, "Unexpected intent caught at item click receiver ::" + intentI);
            return;
        }
        String extra1 = intentI.getStringExtra(Util.EXTRA_WIDGET_ONCLICK_APPNAME);
        String extra2 = intentI.getStringExtra(Util.EXTRA_WIDGET_ONCLICK_APPDEV);
        String extra3 = intentI.getStringExtra(Util.EXTRA_WIDGET_ONCLICK_FULLTEXT);
        Float extra4 = intentI.getFloatExtra(Util.EXTRA_WIDGET_ONCLICK_APPRATING, (float) 5.0);
        String extra5 = intentI.getStringExtra(Util.EXTRA_WIDGET_ONCLICK_ICONURL);
        String extra6 = intentI.getStringExtra(Util.EXTRA_WIDGET_ONCLICK_IMAGE1);
        String extraImage2 = intentI.getStringExtra(Util.EXTRA_WIDGET_ONCLICK_IMAGE2);
        String extraImage3 = intentI.getStringExtra(Util.EXTRA_WIDGET_ONCLICK_IMAGE3);
        String extra9 = intentI.getStringExtra(Util.EXTRA_WIDGET_ONCLICK_APPURL);
        Boolean extra10 = intentI.getBooleanExtra(Util.EXTRA_WIDGET_ONCLICK_DOWNLOAD, false);
        Boolean extra13 = intentI.getBooleanExtra(Util.EXTRA_WIDGET_ONCLICK_INSTALL, false);
        String extra11 = intentI.getStringExtra(Util.EXTRA_WIDGET_ONCLICK_MD5);
        String extraPackageName = intentI.getStringExtra(Util.EXTRA_WIDGET_ONCLICK_PACKAGE);
        String extra14 = intentI.getStringExtra(Util.EXTRA_WIDGET_ONCLICK_APPTYPE);
        String extraVideoUri = intentI.getStringExtra(Util.EXTRA_WIDGET_ONCLICK_VIDEOURI);
        long extraApkSize = intentI.getLongExtra(Util.EXTRA_WIDGET_ONCLICK_APKSIZE, 0);
        
        CLog.e(Util.TAGC26, "Received a button click ::"+ intentI + " ::: appname ::"+ extra1);
        //new SendAnalyticsData(mContext, Util.CLICKED_APP).execute(extraPackageName);
        Util.createAndSendAnalyticsData(Util.CLICKED_APP, Util.CATEGORY_BANNER, Util.APP_PACKAGE_NAME, extraPackageName, mContext);
        if(extra1.equals("")){
            CLog.d(Util.TAGC26, "An empty slot was clicked.");
            return;
        }
        CLog.d(Util.TAGC26,"starting activity:" + extra1);
        if (Util.isAppAlreadyInstalled(extraPackageName, mContext)) {
            Intent openApp = mContext.getPackageManager().getLaunchIntentForPackage(extraPackageName);
            try {
                context.startActivity(openApp);
                SharedPreferences mSharedPrefs;
                mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                Set<String> set = new HashSet<String>(mSharedPrefs.getStringSet(Util.APP_ACTIVATION_PACKAGES, new HashSet<String>()));
                if(set.contains(extraPackageName)){
                  //  new SendAnalyticsData(mContext, Util.APP_ENGAGEMENT).execute(extraPackageName);
                    Util.createAndSendAnalyticsData(Util.APP_ENGAGEMENT, Util.CATEGORY_LIST, Util.APP_PACKAGE_NAME, extraPackageName, mContext);
                }else
                {
                    SharedPreferences.Editor editor = mSharedPrefs.edit();
                    set.add(extraPackageName);
                    editor.putStringSet(Util.APP_ACTIVATION_PACKAGES, set);
                    editor.commit();
                  //  new SendAnalyticsData(mContext, Util.ACTIVATED_APP).execute(extraPackageName);
                    Util.createAndSendAnalyticsData(Util.ACTIVATED_APP, Util.CATEGORY_LIST, Util.APP_PACKAGE_NAME, extraPackageName, mContext);
                }
            } catch (Exception e) {
                CLog.w(Util.TAGC26, "Exception raised on trying to start activity ::"+ e.getMessage());
                if(Util.isAppDisabled(extraPackageName, mContext)){
                    Toast.makeText(mContext, mContext.getString(R.string.msgAppDesabled), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Intent appDescPage = new Intent();
            appDescPage.setComponent(new ComponentName(Util.getPackageName(mContext),
                    Util.APP_DESCRIPTION_PAGE));
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APPNAME,extra1);
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APPDEV,extra2);
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_FULLTEXT,extra3);
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APPRATING, extra4);
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_ICONURL,extra5);
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_IMAGE1,extra6);
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_IMAGE2,extraImage2);
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_IMAGE3,extraImage3);
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APPURL,extra9);
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_DOWNLOAD,extra10);
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_INSTALL,extra13);
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_MD5,extra11);
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APPTYPE,extra14);
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_PACKAGE,extraPackageName);
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APK_SIZE, extraApkSize);
            appDescPage.putExtra(Util.EXTRA_FULLAPP_ONCLICK_VIDEOURL,extraVideoUri);
            appDescPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(appDescPage);
            } catch (ActivityNotFoundException e) {
                CLog.w(Util.TAGC26, "Exception raised on trying to start activity ::"+ e.getMessage());
            }
        }
    }
}