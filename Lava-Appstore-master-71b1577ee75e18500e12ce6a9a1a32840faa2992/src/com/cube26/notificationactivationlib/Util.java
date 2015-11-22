package com.cube26.notificationactivationlib;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import com.cube26.trendingnow.util.CLog;

public class Util {

    public static final String API = "http://notification.celkonvas.in/";//"http://notification.intexvas.in/";
    public static final String API_URL_ANALYTICS = "http://notification.celkonvas.in/analytics";//"http://notification.intexvas.in/analytics";

    public static int UPDATE_FREQUENCY = 30*60*1000; //Every 30 minutes.
    public static int UPDATE_START_DIFF = 60*1000; // start after 1 minute from current time..

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NotifActiv.db";
    //public static final String API = "http://notification.cube26.com";

    public static final int timeoutConnection = 10000;
    public static final int timeoutSocket = 5000;
    public static final String TAGC26 = "Cube26-NotifActiv";
    public static final String ACTIVATION_RECEIVER = "com.cube26.activation.ACTIVATION_RECEIVER";
    public static final String NOTIFICATION_RECEIVER = "com.cube26.notification.NOTIFICATION_RECEIVER";
    public static final String NOTIFICATION_OPEN_APP_RECEIVER = "com.cube26.notification.OPEN_APP_RECEIVER";

    public static final String ACTIVATION_EXTRA = "com.cube26.notification.ACTIVATION_EXTRA";
    public static final String NOTIFICATION_EXTRA = "com.cube26.activation.ACTIVATION_EXTRA";

    public static final String APP_ACTIVATION_PACKAGES = "com.cube26.activation.APP_ACTIVATION_PACKAGES";

    public static final String DEFAULT_BROWSER_PACKAGE_NAME = "com.android.browser";
    public static final String DEFAULT_BROWSER_CLASS_NAME = "com.android.browser.BrowserActivity";
    public static final String CHROME_BROWSER_PACKAGE_NAME = "com.android.chrome";
    public static final String UPDATE_FREQ_KEY = "update_freq";
    public static final String SERVER_ON_OFF = "on_off";
    public static final String DEFAULT_SERVER_ON_OFF_FLAG = "off";
    public static final String SERVER_FLAG_OFF = "off";
    
    private static char[] hexDigits = "0123456789abcdef".toCharArray();

    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public static String getMncCode(Context context){
        TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tel.getNetworkOperator();
        int mnc = 0;
        try {

            if (networkOperator != null) {
                mnc = Integer.parseInt(networkOperator.substring(3));
            }
        } catch (Exception e) {
        }
        return String.valueOf(mnc);
    }
    public static String getMccCode(Context context){
        TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tel.getNetworkOperator();
        int mcc = 0;
        try {
            if (networkOperator != null) {
                mcc = Integer.parseInt(networkOperator.substring(0, 3));
            }
        } catch (Exception e) {
        }
        return String.valueOf(mcc);
    }
    public static String getAppVersion(Context context){
        String appVersion = "";
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            appVersion = pInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return appVersion;
    }
    public static String getDeviceName() {
        String model = Build.MODEL;
        return model != null ? model : "";
    }

    public static String getMd5(File fileToCheck) {
        String md5 = "";

        try {
            byte[] bytes = new byte[4096];
            int read = 0;
            MessageDigest digest = MessageDigest.getInstance("MD5");

            FileInputStream is = new FileInputStream(fileToCheck);
            while ((read = is.read(bytes)) != -1) {
                digest.update(bytes, 0, read);
            }

            byte[] messageDigest = digest.digest();

            StringBuilder sb = new StringBuilder(32);

            for (byte b : messageDigest) {
                sb.append(hexDigits[(b >> 4) & 0x0f]);
                sb.append(hexDigits[b & 0x0f]);
            }

            md5 = sb.toString();
            is.close();
        } catch (Exception e) {
            CLog.e(TAGC26, "Error while checking md5 of file.");
        }
        return md5;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isPackageSystemApp(Context context, String packageName){
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                return true;
            }
        } catch (NameNotFoundException e) {
            CLog.e(TAGC26, "Error while checking app for system flag.");
        }
        return false;
    }
    public static String getDeviceId(Context context) {
        TelephonyManager TelephonyMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String id;
        TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(context);
        boolean isDualSIM = telephonyInfo.isDualSIM();
        if(isDualSIM){
            String imeiSIM1 = telephonyInfo.getImeiSIM1();
            String imeiSIM2 = telephonyInfo.getImeiSIM2();
            id = imeiSIM1 + "," + imeiSIM2;
        }else{
            id = TelephonyMgr.getDeviceId();
        }
        return id != null ? id : "";
    }

    public static String getCountryCode(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getSimCountryIso();
        return countryCode != null && !countryCode.equalsIgnoreCase("") ? countryCode : "in";
    }

    public static boolean isAppAlreadyInstalled(String packageName, Context context) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    public static String getOwnPackageName(Context ctx){
        try {
            PackageInfo pInfo = ctx.getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            return pInfo.packageName;
        } catch (Exception e) {
            CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
            return null;
        }
    }

    public static String getCurrentTimeInMillis(){
        return String.valueOf(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());
    }

    public static String getOnOffFlagFromSharedPref(Context ctx){
        SharedPreferences mSharedPrefs;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mSharedPrefs.getString(Util.SERVER_ON_OFF, Util.DEFAULT_SERVER_ON_OFF_FLAG);
    }
    public static long getUpdateFrequencyFromSharedPrefs(Context ctx){
        SharedPreferences mSharedPrefs;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mSharedPrefs.getLong(Util.UPDATE_FREQ_KEY, UPDATE_FREQUENCY);
    }
    
    public static void setUpdateFrequency(Context ctx, long updateFrequency){
        SharedPreferences mSharedPrefs;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putLong(Util.UPDATE_FREQ_KEY, updateFrequency);
        editor.commit();
    }
    
    public static void setOnOffFlag(Context ctx, String onOffFlag){
        SharedPreferences mSharedPrefs;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(Util.SERVER_ON_OFF, onOffFlag);
        editor.commit();
    }
    
    @SuppressWarnings("rawtypes")
    public static int getResourseIdByName(String packageName, String className, String name) {
        Class r = null;
        int id = 0;
        try {
            r = Class.forName(packageName + ".R");

            Class[] classes = r.getClasses();
            Class desireClass = null;

            for (int i = 0; i < classes.length; i++) {
                if (classes[i].getName().split("\\$")[1].equals(className)) {
                    desireClass = classes[i];

                    break;
                }
            }

            if (desireClass != null) {
                id = desireClass.getField(name).getInt(desireClass);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return id;
    }
    public static String getAndroidId(Context context){
        String android_id = Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);
        return android_id;
    }
    public static String getTimeZone(){
        return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
    }
}
