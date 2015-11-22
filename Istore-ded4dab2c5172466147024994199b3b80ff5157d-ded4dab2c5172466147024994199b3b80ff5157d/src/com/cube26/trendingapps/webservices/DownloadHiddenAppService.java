/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.trendingapps.webservices;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import com.cube26.trendingnow.util.CLog;

import com.cube26.trendingapps.analytics.SendAnalyticsData;
import com.cube26.trendingnow.util.Util;

public class DownloadHiddenAppService extends IntentService {

    private static int serviceInstantiateCount = 0;

    TimerTask mRepeatDownloadInProgress;
    Timer myTimer = new Timer();
    Intent mBroadcastIntent = null;

    public DownloadHiddenAppService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        CLog.e(Util.TAGC26,
                "Service started at time ::" + System.currentTimeMillis()
                + " ::: timer initialized as ::" + myTimer.toString()
                + " ::: instantiation count ::" + serviceInstantiateCount++);
        ArrayList<String> urlToDownload = intent
                .getStringArrayListExtra(Util.EXTRA_DOWNLOADSERVICE_URLARRAY);
        ArrayList<String> app_name = intent
                .getStringArrayListExtra(Util.EXTRA_DOWNLOADSERVICE_APPNAME);
        ArrayList<String> md5 = intent
                .getStringArrayListExtra(Util.EXTRA_DOWNLOADSERVICE_APPMD5);
        ArrayList<String> wifiFlags = intent
                .getStringArrayListExtra(Util.EXTRA_DOWNLOADSERVICE_WIFIFLAG);
        Boolean downloadOnlyOnWifi = false;

        if(app_name!=null && app_name.size()==0){
            // No apps to download, so return
            return;
        }
        
        for (int appIndex = 0; appIndex < urlToDownload.size(); appIndex++) {
            if (wifiFlags != null && wifiFlags.size() > appIndex) {
                downloadOnlyOnWifi = Util.parseBoolean(wifiFlags.get(appIndex));
            }
            String appPackage = app_name.get(appIndex);
            String folderPath = Environment.getExternalStorageDirectory()
                    + Util.TRENDING_APPS_FOLDER_NAME;
            String filePath = folderPath + appPackage + ".apk";
            File apkFile = new File(filePath);
            File folderDirectory = new File(folderPath);
            if (!folderDirectory.exists()) {
                //create folder if required
                File trendingAppsDir = new File(folderPath);
                trendingAppsDir.mkdirs();
            }

            //Check if we need to download
            if (apkFile.exists()) {
                String currentFileMD5 = Util.md5(apkFile);
                String retrievedMD5 = md5.get(appIndex);
                if (currentFileMD5.equalsIgnoreCase(retrievedMD5)) {
                    // correct apk already downloaded, installed as required
                    sendBroadcastForApkInstall(intent, appIndex, true, apkFile, appPackage);
                    continue;
                } else {
                    CLog.i(Util.TAGC26, "Deleting corrupted .apk file :: "+ apkFile.toString());
                    apkFile.delete();
                }
            }
            if (Util.isAppAlreadyInstalled(appPackage, getApplicationContext())) {
                // do not download if app is already installed
                continue;
            }

            if (downloadOnlyOnWifi && Util.isWifiConnected(getApplicationContext())==false) {
                // do not download if wifi not connected
                continue;
            }

            // Start download
            int downloadFlag = downloadApkFile(filePath,urlToDownload.get(appIndex), md5.get(appIndex), appPackage);
            switch (downloadFlag) {
                case Util.DOWNLOAD_COMPLETED:
                   // new SendAnalyticsData(this, Util.DOWNLOADED_APP_AUTO).execute(appPackage);
                    Util.createAndSendAnalyticsData(Util.DOWNLOADED_APP_AUTO, Util.CATEGORY_AUTO, Util.APP_PACKAGE_NAME, appPackage, this);
                    sendBroadcastForApkInstall(intent,appIndex,false, new File(filePath), appPackage);
                    break;
                case Util.DOWNLOAD_FAILED:
                    apkFile.delete();
                    break;
                case Util.DOWNLOAD_CANCELED:
                    apkFile.delete();
                    break;
                default:
                    break;
            }
        }
    }

    private int downloadApkFile(String pathOfFile, String urlToSet, String md5Checksum, String appPackage) {
        try {
            if (!Util.isNetworkAvailable(getApplicationContext())) {
                CLog.w(Util.TAGC26,  "Internet Connection Not Available, can't download");
                return Util.DOWNLOAD_FAILED;
            }
            URL url = new URL(urlToSet);
            URLConnection connection = url.openConnection();
            long file_size = connection.getContentLength();
            CLog.wtf(Util.TAGC26, " apk size :: " + file_size);
            connection.connect();
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(pathOfFile);
            byte data[] = new byte[10240];  //downlod 10k chunk
            int count = 0;
            while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
            CLog.wtf(Util.TAGC26, "md5 from server :: " + md5Checksum);
            CLog.wtf(Util.TAGC26, "md5 from from apk :: " + Util.md5(new File(pathOfFile)));
            if(!md5Checksum.equals(Util.md5(new File(pathOfFile)))){
                CLog.w(Util.TAGC26, "File downloaded but corrupt");
                return Util.DOWNLOAD_FAILED;
            }
        } catch (IOException e) {
            CLog.e(Util.TAGC26, "IOException at URL::"+ urlToSet+ " :: " + e.getMessage());
            return Util.DOWNLOAD_FAILED;
        }
        return Util.DOWNLOAD_COMPLETED;
    }

    private void sendBroadcastForApkInstall(Intent receivedIntent, int index, boolean isAlreadyDownloaded, File installFile, String packageName) {
        if(Util.isPackageSystemApp(getApplicationContext(), getApplicationContext().getPackageName())){
            Util.installPackage(installFile);
        }
    }

}