/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.trendingapps.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.cube26.trendingapps.analytics.SendAnalyticsData;
import com.cube26.trendingapps.webservices.ActivationDTO;
import com.cube26.trendingapps.webservices.AppExtraKeyValuePair;
import com.cube26.trendingapps.webservices.FetchedDealData;
import com.cube26.trendingnow.util.CLog;
import com.cube26.trendingnow.util.Util;

public class WidgetDealClickReceiver extends BroadcastReceiver {
    FetchedDealData fetchedDealData;

    @Override
    public void onReceive(Context context, Intent intent) {

        fetchedDealData = (FetchedDealData)intent.getSerializableExtra(Util.EXTRA_WIDGET_DEAL_DATA);

        ActivationDTO activationDTO = fetchedDealData.getActivationDTO();
        String pckgName = activationDTO.getPackageName();

        if (Util.isAppAlreadyInstalled(pckgName, context)) {
            Intent activateApp = new Intent();
            try {
                activateApp.setComponent(new ComponentName(activationDTO.getPackageName(), activationDTO.getActivityName()));
                for (AppExtraKeyValuePair pair : activationDTO.getAppExtraKeyValuePairs()) {
                    activateApp.putExtra(pair.getKeyString(), pair.getValueString());
                }
                context.startActivity(activateApp);
               // new SendAnalyticsData(context, Util.DEAL_APP_OPENED).execute(pckgName);
                Util.createAndSendAnalyticsData(Util.DEAL_APP_OPENED, Util.CATEGORY_BANNER, Util.APP_PACKAGE_NAME, pckgName, context);
            } catch (Exception e) {
                CLog.d(Util.TAGC26, "Starting activity for deal failed for given parameter");
                activateApp = context.getPackageManager().getLaunchIntentForPackage(pckgName);
                try {
                    CLog.d(Util.TAGC26, "Starting activity for deal using launching intent, intent :: "+ activateApp);
                    context.startActivity(activateApp);
                   // new SendAnalyticsData(context, Util.DEAL_APP_OPENED).execute(pckgName);
                    Util.createAndSendAnalyticsData(Util.DEAL_APP_OPENED, Util.CATEGORY_BANNER, Util.APP_PACKAGE_NAME, pckgName, context);
                } catch (Exception e1) {
                    e.printStackTrace();
                    CLog.e(Util.TAGC26, "Starting activity for deal failed using launching intent ::" + e1.getMessage());
                    openDealInBrowser(context, fetchedDealData.getDealUrl(), pckgName);
                }
            }
        }else{
            openDealInBrowser(context, fetchedDealData.getDealUrl(), pckgName);
        }
    }

    private void openDealInBrowser(Context context, String webUrl, String pckgName){
        if(Util.isNetworkAvailable(context)){
            Intent launchDealApplication = new Intent(android.content.Intent.ACTION_VIEW);
            //new SendAnalyticsData(mContext, Util.CLICKED_APP +"deal_").execute(extraObtained);

            try {
                launchDealApplication.setData(Uri.parse(webUrl));
                launchDealApplication.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                CLog.d(Util.TAGC26, "Starting activity to show deal, intent :: "+ launchDealApplication);
                context.startActivity(launchDealApplication);
              //  new SendAnalyticsData(context, Util.DEAL_BROWSER_OPENED).execute(pckgName);
                Util.createAndSendAnalyticsData(Util.DEAL_BROWSER_OPENED, Util.CATEGORY_AUTO, Util.APP_PACKAGE_NAME, pckgName, context);
            } catch (Exception e) {
                CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
                try {
                    if (!webUrl.startsWith("https://") && !webUrl.startsWith("http://")){
                        webUrl = "http://" + webUrl;
                    }
                    Intent launchDealOnClick = new Intent(android.content.Intent.ACTION_VIEW);
                    launchDealOnClick.setData(Uri.parse(webUrl));
                    launchDealOnClick.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    CLog.d(Util.TAGC26, "Starting activity to show deal, intent :: "+ launchDealApplication);
                    context.startActivity(launchDealOnClick);
                 //   new SendAnalyticsData(context, Util.DEAL_BROWSER_OPENED).execute(pckgName);
                    Util.createAndSendAnalyticsData(Util.DEAL_BROWSER_OPENED, Util.CATEGORY_AUTO, Util.APP_PACKAGE_NAME, pckgName, context);
                } catch (Exception e2) {
                    // TODO: handle exception
                }
            }
        }else{
            Toast.makeText(context, "No Internet connection", Toast.LENGTH_LONG).show();
        }
    }
}
