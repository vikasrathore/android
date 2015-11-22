/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.trendingnow.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.json.JSONObject;

import android.os.Environment;


public class CLog {

    private static boolean IS_CUBE_LOGGING_ON = false;
    private static CLog clog;
    private static final String LOG_FILE_NAME  = "tajson.txt";
    
    public static CLog getInstance(){
        if(null==clog){
            clog = new CLog();
            CLog.init();
        }
        return clog;
    }
    public static void init(){
        try {
            File file = new File(Environment.getExternalStorageDirectory(), LOG_FILE_NAME);
            if(!file.exists()){
                // No file found to keep the logging off
                return;
            }
          //Read text from file
            StringBuilder text = new StringBuilder();

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            JSONObject jsonObject = new JSONObject(text.toString());
            boolean isCubeLogOn = jsonObject.has("log")?jsonObject.getBoolean("log"):false;
            if(isCubeLogOn){
                IS_CUBE_LOGGING_ON = true;
            }
        } catch (Exception ex) {
        }
    }

    public static int v(String tag, String msg) {
        CLog.getInstance();
        if(IS_CUBE_LOGGING_ON)
            return android.util.Log.v(tag, msg);
        else
            return 0;
    }
    public static int v(String tag, String msg, Throwable tr) {
        CLog.getInstance();
        if(IS_CUBE_LOGGING_ON)
            return android.util.Log.v(tag, msg, tr);
        else
            return 0;
    }
    public static int d(String tag, String msg) {
        CLog.getInstance();
        if(IS_CUBE_LOGGING_ON)
            return android.util.Log.d(tag, msg);
        else
            return 0;
    }
    public static int d(String tag, String msg, Throwable tr) {
        CLog.getInstance();
        if(IS_CUBE_LOGGING_ON)
            return android.util.Log.d(tag, msg, tr);
        else
            return 0;
    }
    public static int i(String tag, String msg) {
        CLog.getInstance();
        if(IS_CUBE_LOGGING_ON)
            return android.util.Log.i(tag, msg);
        else
            return 0;
    }
    public static int i(String tag, String msg, Throwable tr) {
        CLog.getInstance();
        if(IS_CUBE_LOGGING_ON)
            return android.util.Log.i(tag, msg, tr);
        else
            return 0;
    }
    public static int w(String tag, String msg) {
        CLog.getInstance();
        if(IS_CUBE_LOGGING_ON)
            return android.util.Log.w(tag, msg);
        else
            return 0;
    }
    public static int w(String tag, String msg, Throwable tr) {
        CLog.getInstance();
        if(IS_CUBE_LOGGING_ON)
            return android.util.Log.w(tag, msg, tr);
        else
            return 0;
    }
    public static int w(String tag, Throwable tr) {
        CLog.getInstance();
        if(IS_CUBE_LOGGING_ON)
            return android.util.Log.w(tag, tr);
        else
            return 0;
    }
    public static int e(String tag, String msg) {
        CLog.getInstance();
        if(IS_CUBE_LOGGING_ON)
            return android.util.Log.e(tag, msg);
        else
            return 0;
    }
    public static int e(String tag, String msg, Throwable tr) {
        CLog.getInstance();
        if(IS_CUBE_LOGGING_ON)
            return android.util.Log.e(tag, msg, tr);
        else
            return 0;
    }
    public static int wtf(String tag, String msg) {
        CLog.getInstance();
        if(IS_CUBE_LOGGING_ON)
            return android.util.Log.wtf(tag, msg);
        else
            return 0;
    }
    public static int wtf(String tag, String msg, Throwable tr) {
        CLog.getInstance();
        if(IS_CUBE_LOGGING_ON)
            return android.util.Log.wtf(tag, msg, tr);
        else
            return 0;
    }
}
