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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;
import com.cube26.trendingnow.util.CLog;

import com.cube26.trendingnow.util.Util;

public class DownloadService extends IntentService {

    private static int serviceInstantiateCount = 0;

    private ResultReceiver receiver = null;
    private String mPackageName = "";
    TimerTask mRepeatDownloadInProgress;
    Timer myTimer = new Timer();
    Intent mBroadcastIntent = null;
    public static ArrayList<String> mWaitingQueue = new ArrayList<String>();
    public static ArrayList<String> mAppsRemovedFromQueue = new ArrayList<String>();
    private static Context mContext;
    public static boolean shouldCancelCurrentDownload = false;

    public DownloadService() {
        super("DownloadService");
    }

    public static void removeAppFromQueue(String appPackageToBeRemoved){
        CLog.d(Util.TAGC26, "Received request to cancel download for package ::" + appPackageToBeRemoved);
        try{
            if(mWaitingQueue.contains(appPackageToBeRemoved)){
                mWaitingQueue.remove(appPackageToBeRemoved);
                mAppsRemovedFromQueue.add(appPackageToBeRemoved);
                sendCancelBroadcastForPackage(appPackageToBeRemoved);
            }else{
                shouldCancelCurrentDownload = true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private static void sendCancelBroadcastForPackage(String packageName){
        Intent intent = new Intent(Util.DOWNLOAD_RECEIVER_INTENT_NAME);
        intent.putExtra(Util.EXTRA_BROADCAST_PACKAGE, packageName);
        intent.putExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS, Util.DOWNLOAD_CANCELED);
        intent.putExtra(Util.EXTRA_BROADCAST_APPS_IN_QUEUE, mWaitingQueue);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = getApplicationContext();
        ArrayList<String> app_name = intent
                .getStringArrayListExtra(Util.EXTRA_DOWNLOADSERVICE_APPNAME);
        Boolean automaticFlagForIntent = intent.getBooleanExtra(Util.EXTRA_DOWNLOADSERVICE_AUTOMATIC, false);
        if(!automaticFlagForIntent && !mWaitingQueue.contains(app_name.get(0))){
            mWaitingQueue.add(app_name.get(0));
        }
        return super.onStartCommand(intent, flags, startId);
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
        receiver = (ResultReceiver) intent.getParcelableExtra("receiver");

        if(app_name!=null && app_name.size()==0){
            // No apps to download, so return
            return;
        }
        if(mAppsRemovedFromQueue.contains(app_name.get(0))){
            mAppsRemovedFromQueue.remove(app_name.get(0));
            return;
        }
        
        for (int appIndex = 0; appIndex < urlToDownload.size(); appIndex++) {
            if (wifiFlags != null && wifiFlags.size() > appIndex) {
                downloadOnlyOnWifi = Util.parseBoolean(wifiFlags.get(appIndex));
            }
            String appPackage = app_name.get(appIndex);
            // Remove the app from queue
            mWaitingQueue.remove(appPackage);
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
            sendStatusBroadcast(appPackage, Util.DOWNLOAD_STARTED, Util.DOWNLOAD_PROGRESS_TOTAL_DEFAULT, Util.DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT);
            int downloadFlag = downloadApkFile(filePath,urlToDownload.get(appIndex), md5.get(appIndex), appPackage);
            switch (downloadFlag) {
                case Util.DOWNLOAD_COMPLETED:
                    sendStatusBroadcast(appPackage, Util.DOWNLOAD_COMPLETED,  Util.DOWNLOAD_PROGRESS_TOTAL_DEFAULT, Util.DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT);
                    sendBroadcastForApkInstall(intent,appIndex,false, new File(filePath), appPackage);
                    break;
                case Util.DOWNLOAD_FAILED:
                    sendStatusBroadcast(appPackage, Util.DOWNLOAD_FAILED, Util.DOWNLOAD_PROGRESS_TOTAL_DEFAULT, Util.DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT);
                    apkFile.delete();
                    break;
                case Util.DOWNLOAD_CANCELED:
                    sendStatusBroadcast(appPackage, Util.DOWNLOAD_CANCELED, Util.DOWNLOAD_PROGRESS_TOTAL_DEFAULT, Util.DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT);
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
            long total = 0;
            int broadcastSendInterval = 0;
            while ((count = input.read(data)) != -1) {
                if(!shouldCancelCurrentDownload){
                    total += count;
                    broadcastSendInterval++;
                    if(broadcastSendInterval == Util.BROADCAST_SEND_INTERVAL){
                        sendStatusBroadcast(appPackage, Util.DOWNLOAD_PROGRESS, (float)total/(1024*1024), (float)file_size/(1024*1024));
                        broadcastSendInterval = 0;
                    }
                    output.write(data, 0, count);
                }else{
                    //                sendStatusBroadcast(appPackage, Util.DOWNLOAD_CANCELED, Util.DOWNLOAD_PROGRESS_TOTAL_DEFAULT, Util.DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT);
                    shouldCancelCurrentDownload = false;
                    output.flush();
                    output.close();
                    input.close();
                    return Util.DOWNLOAD_CANCELED;
                }
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
            Util.sendStatusBroadcast(packageName, Util.INSTALL_STARTED, mContext);
            Util.installPackage(installFile);
        }
        ArrayList<String> installFlags = receivedIntent
                .getStringArrayListExtra(Util.EXTRA_DOWNLOADSERVICE_INSTALL);
        ArrayList<String> app_name = receivedIntent
                .getStringArrayListExtra(Util.EXTRA_DOWNLOADSERVICE_APPNAME);
        String appPackage = app_name.get(index);
        String install = installFlags.get(index);
        receiver = (ResultReceiver) receivedIntent.getParcelableExtra("receiver");
        Bundle resultData = new Bundle();
        resultData.putString(Util.DOWNLOAD_RESULT_DOWNLOADED_FILENAME, appPackage);
        resultData.putString(Util.DOWNLOAD_RESULT_INSTALLFLAG, install);
        resultData.putBoolean(Util.DOWNLOAD_RESULT_ALREADY_DOWNLOADED, isAlreadyDownloaded);
        if(receiver == null) {
            CLog.e(Util.TAGC26, "can't send broadcast for apk download");
            return;
        }
        receiver.send(Util.UPDATE_PROGRESS, resultData);
    }

    private void sendStatusBroadcast(String appPackage, int downloadStatus, float total, float file_size) {
        mPackageName = appPackage;
        mBroadcastIntent = new Intent(Util.DOWNLOAD_RECEIVER_INTENT_NAME);
        if (downloadStatus == Util.DOWNLOAD_STARTED) {
            mRepeatDownloadInProgress = new TimerTask() {
                @Override
                public void run() {
                    mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_PACKAGE, mPackageName);
                    mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS,
                            Util.DOWNLOAD_STARTED);
                    mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_APPS_IN_QUEUE, mWaitingQueue);
                    LocalBroadcastManager.getInstance(getApplicationContext())
                    .sendBroadcast(mBroadcastIntent);
                }
            };
            CLog.d(Util.TAGC26, "Scheduling timer at time ::" + System.currentTimeMillis()
                    + ":: using timer ::" + myTimer.toString()
                    + ":: timerTask ::"  + mRepeatDownloadInProgress.toString());

            myTimer.scheduleAtFixedRate(mRepeatDownloadInProgress, 0, 1000);
        } else if (downloadStatus == Util.DOWNLOAD_FAILED) {
            //Send download failed broadcast
            mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_PACKAGE, appPackage);
            mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS, Util.DOWNLOAD_FAILED);
            mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_APPS_IN_QUEUE, mWaitingQueue);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mBroadcastIntent);

            CLog.d(Util.TAGC26, "Cancelling timer at time ::" + System.currentTimeMillis()
                    + ":: using timer ::" + myTimer.toString()
                    + "::: and timerTask :::" + mRepeatDownloadInProgress.toString());
            mRepeatDownloadInProgress.cancel();

        }else if (downloadStatus == Util.DOWNLOAD_COMPLETED) {
            //Send download failed broadcast
            mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_PACKAGE, appPackage);
            mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS, Util.DOWNLOAD_COMPLETED);
            mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_APPS_IN_QUEUE, mWaitingQueue);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mBroadcastIntent);

            CLog.d(Util.TAGC26, "Cancelling timer at time ::" + System.currentTimeMillis()
                    + ":: using timer ::" + myTimer.toString()
                    + "::: and timerTask :::" + mRepeatDownloadInProgress.toString());
            mRepeatDownloadInProgress.cancel();

        } else if (downloadStatus == Util.DOWNLOAD_CANCELED) {
            //Send download failed broadcast
            mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_PACKAGE, appPackage);
            mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS, Util.DOWNLOAD_CANCELED);
            mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_APPS_IN_QUEUE, mWaitingQueue);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mBroadcastIntent);
            mRepeatDownloadInProgress.cancel();

        } else if (downloadStatus == Util.DOWNLOAD_PROGRESS) {
            // Send download failed broadcast
            mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_PACKAGE,
                    appPackage);
            mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS, Util.DOWNLOAD_PROGRESS);
            mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS_TOTAL,
                    total);
            mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS_FILESIZE,
                    file_size);
            mBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_APPS_IN_QUEUE,
                    mWaitingQueue);
            LocalBroadcastManager.getInstance(getApplicationContext())
            .sendBroadcast(mBroadcastIntent);

            mRepeatDownloadInProgress.cancel();

        }else {
            CLog.d(Util.TAGC26, "cancelling due to completion timer at time ::"
                    + System.currentTimeMillis() + ":: using timer ::"
                    + myTimer.toString() + ":: timer task ::" + mRepeatDownloadInProgress.toString());
            mRepeatDownloadInProgress.cancel();
        }
    }
}