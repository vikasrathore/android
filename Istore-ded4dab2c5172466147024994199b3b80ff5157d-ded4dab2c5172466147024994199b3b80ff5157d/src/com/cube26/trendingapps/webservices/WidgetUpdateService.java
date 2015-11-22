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

import com.cube26.trendingnow.util.Util;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import com.cube26.trendingnow.util.CLog;

public class WidgetUpdateService extends IntentService {

    public WidgetUpdateService() {
        super("WidgetUpdateService");
    }

    private ResultReceiver receiver;

    @Override
    protected void onHandleIntent(Intent intent) {
        String urlToDownload = intent.getStringExtra(Util.WIDGET_UPDATE_EXTRA);
        String md5 = intent.getStringExtra(Util.WIDGET_UPDATE_MD5);
        
        receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
        String folderPath = Environment.getExternalStorageDirectory()
                + Util.TRENDING_APPS_FOLDER_NAME;
        String filePath = folderPath + Util.WIDGET_APK_UPDATE;
        
        File apkFile = new File(filePath);
        File folderDirectory = new File(folderPath);
        
        if (folderDirectory.exists() != true) {
            File makeDirectoryIfNeeded = new File(folderPath);
            makeDirectoryIfNeeded.mkdirs();
        }
        if (apkFile.exists()) {
            String currentFileMD5 = Util.md5(apkFile);
            if (currentFileMD5.equalsIgnoreCase(md5)) {
                Bundle resultData = new Bundle();
                receiver.send(Util.UPDATE_PROGRESS, resultData);
                return;
            }
            apkFile.delete();
        }
        try {
            if (!Util.isNetworkAvailable(getApplicationContext())) {
                CLog.w(Util.TAGC26,  "Internet Connection Not Available, can't update widget");
                return;
            }
            URL url = new URL(urlToDownload);
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(filePath);
            byte data[] = new byte[10240];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
            
            if (apkFile.exists()) {
                String currentFileMD5 = Util.md5(apkFile);
                if (currentFileMD5.equalsIgnoreCase(md5)) {
                    Bundle resultData = new Bundle();
                    receiver.send(Util.UPDATE_PROGRESS, resultData);
                }else{
                    CLog.e(Util.TAGC26, "Widgegt Update Error :: MD5 didnt match.");
                }
            }
        } catch (IOException e1) {
            CLog.e(Util.TAGC26, "IOException in widget update::" + e1.getMessage());
            apkFile.delete();
        }
    }
}