package com.cube26.trendingnow;
/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import com.cube26.istore.R;
import com.cube26.trendingnow.util.CLog;
import com.cube26.trendingapps.analytics.SendAnalyticsData;
import com.cube26.trendingapps.webservices.FetchedAppData;
import com.cube26.trendingapps.webservices.JsonResponseParser;
import com.cube26.trendingnow.util.Util;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1234;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("feisty-tempest-840");
    }
    public static final String TAG = Util.TAGC26;

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        CLog.i(TAG, "GCM received ::"+ messageType);
        Util.dumpIntent(intent);
        try {
            if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    // This loop represents the service doing some work.
                    
                  //  new SendAnalyticsData(this, Util.GCM_EVENT_RECEIVED).execute(Util.GCM_EVENT);
                    Util.createAndSendAnalyticsData(Util.GCM_EVENT_RECEIVED, Util.CATEGORY_AUTO, Util.APP_PACKAGE_NAME, Util.GCM_EVENT, this);
                    String message              = intent.getExtras().getString("message"); 
                    String title                = intent.getExtras().getString("title");
                    String apps                 = intent.getExtras().getString("apps");
                    String okLbl                = intent.getExtras().getString("oklbl");
                    String cnclLbl              = intent.getExtras().getString("cnclbl");
                    String deleteOnCancelStr    = intent.getExtras().getString("doc");
                    String openAppStr           = intent.getExtras().getString("openapp");
                    String openAppIntervalStr   = intent.getExtras().getString("oai");
                    String notificationIconUrl  = intent.getExtras().getString("nicon");
                    
                    boolean deleteOnCancel = Util.parseBoolean(deleteOnCancelStr);
                    boolean openApp        = Util.parseBoolean(openAppStr);
                    int openAppInterval    = Util.parseInteger(openAppIntervalStr);

                    ArrayList<FetchedAppData> appsToDownload = getAppsForDownloadFromServer(apps);
                    ArrayList<String> appsPackagesSuccessfullyDownloaded = new ArrayList<String>();
                    ArrayList<String> appsNamesSuccessfullyDownloaded = new ArrayList<String>();
                    String appsNameSuccessfullyDownloaded = "";

                    for (FetchedAppData fetchedAppData : appsToDownload) {
                        String appPackage = fetchedAppData.getPackageName();
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

                        if (Util.isAppAlreadyInstalled(appPackage, getApplicationContext())) {
                            // do not download if app is already installed
                            continue;
                        }
                        
                        if (fetchedAppData.getOnlyWifi() && Util.isWifiConnected(getApplicationContext())==false) {
                            // do not download if wifi not connected
                            continue;
                        }
                        
                        //Check if we need to download
                        if (apkFile.exists()) {
                            String currentFileMD5 = Util.md5(apkFile);
                            String retrievedMD5 = fetchedAppData.getMD5();
                            if (currentFileMD5.equalsIgnoreCase(retrievedMD5)) {
                                // correct apk already downloaded, installed as required
                                appsPackagesSuccessfullyDownloaded.add(fetchedAppData.getPackageName());
                                appsNamesSuccessfullyDownloaded.add(fetchedAppData.getAppName());
                                appsNameSuccessfullyDownloaded += fetchedAppData.getAppName() +", ";
                                continue;
                            } else {
                                CLog.i(Util.TAGC26, "Deleting corrupted .apk file :: "+ apkFile.toString());
                                apkFile.delete();
                            }
                        }

                        // Start download
                        int downloadFlag = downloadApkFile(filePath,fetchedAppData.getAppUrl(), fetchedAppData.getMD5(), appPackage);
                        switch (downloadFlag) {
                            case Util.DOWNLOAD_COMPLETED:
                               // new SendAnalyticsData(this, Util.GCM_EVENT_DOWNLOADED).execute(appPackage);
                                Util.createAndSendAnalyticsData(Util.GCM_EVENT_DOWNLOADED, Util.CATEGORY_AUTO, Util.APP_PACKAGE_NAME, appPackage, this);
                                appsPackagesSuccessfullyDownloaded.add(fetchedAppData.getPackageName());
                                appsNamesSuccessfullyDownloaded.add(fetchedAppData.getAppName());
                                appsNameSuccessfullyDownloaded += fetchedAppData.getAppName() +", ";
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
                    if(appsPackagesSuccessfullyDownloaded.size()>0){
                        appsNameSuccessfullyDownloaded = appsNameSuccessfullyDownloaded.substring(0,appsNameSuccessfullyDownloaded.lastIndexOf(","));
                        message = message.replaceAll("---", appsNameSuccessfullyDownloaded);
                        sendNotification(title, message, okLbl, cnclLbl, deleteOnCancel, openApp, openAppInterval, appsPackagesSuccessfullyDownloaded, appsNamesSuccessfullyDownloaded, notificationIconUrl);
                       // new SendAnalyticsData(this, Util.GCM_EVENT_DISPLAYED).execute(Util.GCM_EVENT);
                        Util.createAndSendAnalyticsData(Util.GCM_EVENT_DISPLAYED, Util.CATEGORY_AUTO, Util.APP_PACKAGE_NAME, Util.GCM_EVENT, this);
                    }else{
                        CLog.d(Util.TAGC26, "No file downloaded to be installed. So no notification");
                    }


                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private ArrayList<FetchedAppData> getAppsForDownloadFromServer(String message){
        ArrayList<FetchedAppData> appsToDownload = new ArrayList<FetchedAppData>();
        try {
            String result = "";
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, Util.timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, Util.timeoutSocket);
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            String api = 
                    Util.API_URL
                    + "?user_id=" + Util.getDeviceId(this)
                    + "&model=" + URLEncoder.encode(Util.getDeviceName(),"UTF-8")
                    + "&mcc=" + URLEncoder.encode(Util.getMccCode(this),"UTF-8")
                    + "&mnc=" + URLEncoder.encode(Util.getMncCode(this),"UTF-8")
                    + "&et=" + URLEncoder.encode(Util.getCurrentTimeInMillis(),"UTF-8")
                    + "&eTz=" + URLEncoder.encode(Util.getTimeZone(),"UTF-8")
                    + "&product=" + Util.PRODUCT_NAME
                    + "&app_version=" + Util.getAppVersion(this)
                    + "&country_code="+ URLEncoder.encode(Util.getCountryCode(this), "UTF-8");
            CLog.d(Util.TAGC26, "Url being hit :: " + api);
            HttpGet httppost = new HttpGet(api);
            HttpResponse response = httpclient.execute(httppost);
            result = EntityUtils.toString(response.getEntity());

            JsonResponseParser responseParser = new JsonResponseParser();
            responseParser.parseJsonResponseString(result);
            ArrayList<FetchedAppData> fetchedAllApps = (ArrayList<FetchedAppData>)responseParser.getFetchedAllAppsList();

            //JSONObject gcmJsonObject = new JSONObject(message);
            JSONArray appsListJsonArray = new JSONArray(message);
            for (int i = 0; i < appsListJsonArray.length(); i++) {
                String appPackageName = appsListJsonArray.getString(i);
                for (FetchedAppData fetchedAppData : fetchedAllApps) {
                    if(appPackageName.equalsIgnoreCase(fetchedAppData.getPackageName())){
                        appsToDownload.add(fetchedAppData);
                    }
                }
            }
        } catch (Exception e) {
            CLog.e(Util.TAGC26, "GCM Error ::"+ e.getMessage());
        }
        return appsToDownload;
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

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String title, String msg, String okLbl, String cancelLbl, boolean deleteOnCancel, boolean openApp, int openAppInterval, ArrayList<String> appsToInstall, ArrayList<String> appNamesToInstall, String notificationUrl) {

        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Bitmap mIcon = null;
        try {
            CLog.d(Util.TAGC26, "Image download started");
            InputStream in = new java.net.URL(notificationUrl).openStream();
            mIcon = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            CLog.d(Util.TAGC26, "Image download failed");
            CLog.e(Util.TAGC26, e.getMessage());
            e.printStackTrace();
        }
        
        Intent installIntent = new Intent(this, GcmAppInstallReceiver.class);
        installIntent.putExtra(Util.EXTRA_GCM_BROADCAST_APP_LIST, appsToInstall);
        installIntent.putExtra(Util.EXTRA_GCM_BROADCAST_APP_NAMES_LIST, appNamesToInstall);
        installIntent.putExtra(Util.EXTRA_GCM_BROADCAST_INSTALL_FLAG, true);
        installIntent.putExtra(Util.EXTRA_GCM_BROADCAST_NOTIFICATION_ID, NOTIFICATION_ID);
        installIntent.putExtra(Util.EXTRA_GCM_BROADCAST_DELETE_ON_CANCEL, deleteOnCancel);
        installIntent.putExtra(Util.EXTRA_GCM_BROADCAST_OPEN_APP, openApp);
        installIntent.putExtra(Util.EXTRA_GCM_BROADCAST_OPEN_APP_INTERVAL, openAppInterval);

        Intent deleteApkIntent = new Intent(this, GcmAppInstallReceiver.class);
        deleteApkIntent.putExtra(Util.EXTRA_GCM_BROADCAST_APP_LIST, appsToInstall);
        deleteApkIntent.putExtra(Util.EXTRA_GCM_BROADCAST_APP_NAMES_LIST, appNamesToInstall);
        deleteApkIntent.putExtra(Util.EXTRA_GCM_BROADCAST_INSTALL_FLAG, false);
        deleteApkIntent.putExtra(Util.EXTRA_GCM_BROADCAST_NOTIFICATION_ID, NOTIFICATION_ID);
        deleteApkIntent.putExtra(Util.EXTRA_GCM_BROADCAST_DELETE_ON_CANCEL, deleteOnCancel);
        deleteApkIntent.putExtra(Util.EXTRA_GCM_BROADCAST_OPEN_APP, openApp);
        deleteApkIntent.putExtra(Util.EXTRA_GCM_BROADCAST_OPEN_APP_INTERVAL, openAppInterval);
        
        PendingIntent installPendingIntent = PendingIntent.getBroadcast(this, 0 ,installIntent  , PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(this, 1 ,deleteApkIntent  , PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_trending_apps)
        .setContentTitle(title)
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg)
        .setContentIntent(installPendingIntent);
        if(mIcon!=null){
            mBuilder.setLargeIcon(mIcon);
        }

        mBuilder.addAction(0, cancelLbl, deletePendingIntent);
        mBuilder.addAction(0, okLbl, installPendingIntent);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }
}
