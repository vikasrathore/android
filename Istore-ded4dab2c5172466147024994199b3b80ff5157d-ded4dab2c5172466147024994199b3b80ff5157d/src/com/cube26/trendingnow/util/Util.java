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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cube26.trendingnow.util.CLog;

import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.cube26.trendingapps.analytics.EventDTO;
import com.cube26.trendingapps.analytics.SendAnalyticsData;
import com.cube26.trendingapps.webservices.DownloadService;
import com.cube26.trendingapps.webservices.FetchedAppData;
import com.cube26.trendingnow.FullScreenActivity;
import com.cube26.istore.R;

public class Util {

    public static final boolean IS_TABLET = false;
    public static final String APP_VERSION   = "4.2";
    public static final int DATABASE_VERSION = 2;
    public static final String APP_ANALYTICS_VERSION   = "2";
    public static boolean isWidgetConfigured = true;

    public static final String DATABASE_NAME = "FeedReader.db";
    public static final String DATABASE_PATH = "/data/com.cube26.trendingnow/databases/";
    public static final String PRODUCT_NAME  = "ta";

//        public static String API_URL                       = "http://106.185.52.29:1000/";
    public static String API_URL                       = "http://ta.intexvas.in/";
    public static String API_GCM_URL                   = "http://gcm.intexvas.in/";
    public static String API_POST_URL_ANALYTICS        = "http://ta.cube26intex-analytics.in/c.gif";
    public static String API_URL_ANALYTICS             = "http://ta.cube26intex-analytics.in/analytics";
    public static String API_URL_UPDATE                = "";
    public static String APP_LABEL_URL 				   = "";

    public static final String ACTIVATED_APP_AUTO     = "activated_auto";
    public static final String ACTIVATED_APP          = "activated";
    public static final String APP_ENGAGEMENT         = "app_engagement";
    public static final String DOWNLOADED_APP_AUTO    = "downloaded_auto";
    public static final String DOWNLOADED_APP         = "downloaded";
    public static final String INSTALLED_APP_AUTO     = "installed_auto";
    public static final String INSTALLED_APP          = "installed";
    public static final String CLICKED_APP_AUTO       = "clicked_app_auto";
    public static final String CLICKED_APP            = "clicked_app";
    public static final String INSTALLED_APPS_LIST    = "installed_apps_list";
    
    public static final String CATEGORY_MANUAL = "manual";
    public static final String CATEGORY_AUTO = "auto";
    public static final String CATEGORY_BANNER = "banner";
    public static final String CATEGORY_LIST = "list";
    public static final String APP_PACKAGE_NAME = "packageName";
    
    public static final String GCM_EVENT                    = "gcm_event";
    public static final String GCM_EVENT_RECEIVED           = "received";
    public static final String GCM_EVENT_DOWNLOADED         = "downloaded_gcm";
    public static final String GCM_EVENT_DISPLAYED          = "displayed";
    public static final String GCM_EVENT_OK_CLICKED         = "gcm_ok";
    public static final String GCM_EVENT_CANCEL_CLICKED     = "gcm_cancel";
    public static final String GCM_EVENT_INSTALLED          = "installed_gcm";
    public static final String GCM_EVENT_OPENED             = "opened_gcm";
    
    public static final String DEAL_APP_OPENED            = "deal_app_opened";
    public static final String DEAL_BROWSER_OPENED        = "deal_browser_opened";
    
    public static final String EVENT_SOURCE_WIDGET = "eventSourceWidget";
    public static final String EVENT_SOURCE_APP = "eventSourceApp";
    public static final String EVENT_REFRESH_BUTTON_CLICKED = "refresh_clicked";
    public static final String EVENT_MMX_SITE_OPENED = "mmx_site_opened";
    public static final String EVENT_LABEL = "other_event";

    public static final String UNSENT_DATA_ACTIVATED_APP_LIST = "com.cube26.trendingapps.unsent.activatedapplist";
    public static final String UNSENT_DATA_DOWNLOADED_APP_LIST = "com.cube26.trendingapps.unsent.downloadedapplist";
    public static final String UNSENT_DATA_INSTALLED_APP_LIST = "com.cube26.trendingapps.unsent.installedapplist";
    public static final String UNSENT_DATA_CLICKED_APP_LIST = "com.cube26.trendingapps.unsent.clickedapplist";
    public static final String UNSENT_DATA_RETURN_NUMBER_APPS_SENT = "com.cube26.trendingapps.unsentreturnnumberappssent";
    public static final String UNSENT_DATA_NUMBER_APPS_SENT = "com.cube26.trendingapps.unsentnumberappssent";
    public static final String UNSENT_DATA_RETURN_EVENT_NAME = "com.cube26.trendingapps.unsentreturneventname";
    public static final String UNSENT_DATA_RETURN_EVENT_COUNT = "com.cube26.trendingapps.unsentreturneventcount";
    public static final String UNSENT_DATA_EVENT_NAME = "com.cube26.trendingapps.unsenteventname";
    public static final String UNSENT_DATA_PACKAGES = "com.cube26.trendingapps.unsentpackages";
    public static final String UNSENT_DATA_EVENT_COUNT = "com.cube26.trendingapps.unsenteventcount";
    public static final String WIDGET_UPDATE_EXTRA = "com.cube26.trendingapps.updatelink";
    public static final String WIDGET_UPDATE_MD5 = "com.cube26.trendingapps.updatemd5";
    public static final String UPDATE_ACTION = "com.cube26.trendingapps.UPDATE";
    public static final String DEAL_ACTION = "com.cube26.trendingapps.DEAL";
    public static final String APP_LABEL_URL_ACTION = "com.cube26.trendingapps.APPLABEL_CLICK_ACTION";
    public static final String APP_DESCRIPTION_PAGE= "com.cube26.trendingnow.FullAppDescription";
    public static final String FULL_SCREEN_VIDEO_PAGE= "com.cube26.trendingnow.FullScreenActivity";
    public static final String OPEN_WIDGET_CLICK_RECEIVER = "com.cube26.trendingapps.OPENAPP";
    public static final String OPEN_DEAL_CLICK_RECEIVER = "com.cube26.trendingapps.OPENDEALRECEIVER";
    public static final String EXTRA_WIDGET_ONCLICK_APPNAME = "com.trendingapps.widgetonclick.appname";
    public static final String EXTRA_WIDGET_ONCLICK_APPRATING = "com.trendingapps.widgetonclick.apprating";
    public static final String EXTRA_WIDGET_ONCLICK_APPDEV = "com.trendingapps.widgetonclick.appdev";
    public static final String EXTRA_WIDGET_ONCLICK_FULLTEXT = "com.trendingapps.widgetonclick.fulltext";
    public static final String EXTRA_WIDGET_ONCLICK_ICONURL = "com.trendingapps.widgetonclick.iconurl";
    public static final String EXTRA_WIDGET_ONCLICK_APPURL = "com.trendingapps.widgetonclick.appurl";
    public static final String EXTRA_WIDGET_ONCLICK_IMAGE1 = "com.trendingapps.widgetonclick.image1";
    public static final String EXTRA_WIDGET_ONCLICK_IMAGE2 = "com.trendingapps.widgetonclick.image2";
    public static final String EXTRA_WIDGET_ONCLICK_IMAGE3 = "com.trendingapps.widgetonclick.image3";
    public static final String EXTRA_WIDGET_ONCLICK_APPTYPE = "com.trendingapps.widgetonclick.apptype";
    public static final String EXTRA_WIDGET_ONCLICK_APKSIZE = "com.trendingapps.widgetonclick.apksize";
    public static final String EXTRA_WIDGET_ONCLICK_DOWNLOAD = "com.trendingapps.widgetonclick.download";
    public static final String EXTRA_WIDGET_ONCLICK_INSTALL = "com.trendingapps.widgetonclick.install";
    public static final String EXTRA_WIDGET_ONCLICK_MD5 = "com.trendingapps.widgetonclick.md5";
    public static final String EXTRA_WIDGET_ONCLICK_PACKAGE = "com.trendingapps.widgetonclick.package";
    public static final String EXTRA_WIDGET_ONCLICK_VIDEOURI = "com.trendingapps.widgetonclick.videouri";
    public static final String EXTRA_FULLAPP_ONCLICK_APPNAME = "com.trendingapps.fullappsend.appname";
    public static final String EXTRA_FULLAPP_ONCLICK_APPRATING = "com.trendingapps.fullappsend.apprating";
    public static final String EXTRA_FULLAPP_ONCLICK_APPDEV = "com.trendingapps.fullappsend.appdev";
    public static final String EXTRA_FULLAPP_ONCLICK_FULLTEXT = "com.trendingapps.fullappsend.fulltext";
    public static final String EXTRA_FULLAPP_ONCLICK_ICONURL = "com.trendingapps.fullappsend.iconurl";
    public static final String EXTRA_FULLAPP_ONCLICK_APPURL = "com.trendingapps.fullappsend.appurl";
    public static final String EXTRA_FULLAPP_ONCLICK_IMAGE1 = "com.trendingapps.fullappsend.image1";
    public static final String EXTRA_FULLAPP_ONCLICK_IMAGE2 = "com.trendingapps.fullappsend.image2";
    public static final String EXTRA_FULLAPP_ONCLICK_IMAGE3 = "com.trendingapps.fullappsend.image3";
    public static final String EXTRA_FULLAPP_ONCLICK_APPTYPE = "com.trendingapps.fullappsend.apptype";
    public static final String EXTRA_FULLAPP_ONCLICK_DOWNLOAD = "com.trendingapps.fullappsend.download";
    public static final String EXTRA_FULLAPP_ONCLICK_INSTALL = "com.trendingapps.fullappsend.install";
    public static final String EXTRA_FULLAPP_ONCLICK_MD5 = "com.trendingapps.fullappsend.md5";
    public static final String EXTRA_FULLAPP_ONCLICK_PACKAGE = "com.trendingapps.fullappsend.package";
    public static final String RECEIVER_ACTIVATION = "com.trendingapps.activationreceiver";
    public static final String EXTRA_PACKAGE_TO_ACTIVATE = "com.trendingapps.packagetoactivate";
    public static final String EXTRA_FORCE_TOUCH_ATTRIBUTES = "com.trendingapps.forcetouchattributes";
    public static final String EXTRA_FORCE_CLOSE = "com.trendingapps.forceclose";
    public static final String EXTRA_DOWNLOADSERVICE_URLARRAY = "com.trendingapps.downloadservice.urlarray";
    public static final String EXTRA_DOWNLOADSERVICE_APPNAME = "com.trendingapps.downloadservice.appname";
    public static final String EXTRA_DOWNLOADSERVICE_APPMD5 = "com.trendingapps.downloadservice.appmd5";
    public static final String EXTRA_DOWNLOADSERVICE_AUTOMATIC = "com.trendingapps.downloadservice.automatic";
    public static final String EXTRA_DOWNLOADSERVICE_INSTALL = "com.trendingapps.downloadservice.install";
    public static final String EXTRA_DOWNLOADSERVICE_WIFIFLAG = "com.trendingapps.downloadservice.wififlag";
    public static final String EXTRA_BROADCAST_PACKAGE = "com.trendingapps.broadcast.package";
    public static final String EXTRA_BROADCAST_DOWNLOADPROGRESS = "com.trendingapps.broadcast.downloadprogress";
    public static final String EXTRA_BROADCAST_DOWNLOADPROGRESS_VALUE = "com.trendingapps.broadcast.downloadprogressvalue";
    public static final String EXTRA_BROADCAST_DOWNLOADPROGRESS_TOTAL = "com.trendingapps.broadcast.downloadprogresstotal";
    public static final String EXTRA_BROADCAST_DOWNLOADPROGRESS_FILESIZE = "com.trendingapps.broadcast.downloadprogressfilesize";
    public static final String EXTRA_BROADCAST_APPS_IN_QUEUE = "com.trendingapps.queuedapps";
    public static final String EXTRA_WIDGET_DEAL_URL = "com.trendingapps.widgetonclick.deal.url";
    public static final String EXTRA_WIDGET_DEAL_DATA = "com.trendingapps.widgetonclick.deal.data";
    public static final String EXTRA_FULLAPP_ONCLICK_VIDEOURL = "com.trendingapps.fullappsend.videourl";
    public static final String EXTRA_FULLAPP_ONCLICK_APK_SIZE = "com.trendingapps.fullappsend.apksize";

    public static final String EXTRA_GCM_BROADCAST_APP_LIST = "com.trendingapps.gcm.broadcast.applist";
    public static final String EXTRA_GCM_BROADCAST_APP_NAMES_LIST = "com.trendingapps.gcm.broadcast.appnameslist";
    public static final String EXTRA_GCM_BROADCAST_INSTALL_FLAG = "com.trendingapps.gcm.broadcast.installflag";
    public static final String EXTRA_GCM_BROADCAST_NOTIFICATION_ID = "com.trendingapps.gcm.broadcast.notificationid";
    public static final String EXTRA_GCM_BROADCAST_DELETE_ON_CANCEL = "com.trendingapps.gcm.broadcast.deleteoncancel";
    public static final String EXTRA_GCM_BROADCAST_OPEN_APP = "com.trendingapps.gcm.broadcast.openapp";
    public static final String EXTRA_GCM_BROADCAST_OPEN_APP_INTERVAL = "com.trendingapps.gcm.broadcast.openappinterval";

    public static final String GCM_INSTALL_PACKAGES = "com.cube26.trendingapps.GCM_INSTALL_PACKAGES";
    public static final String GCM_OPEN_APP_FLAG = "com.cube26.trendingapps.GCM_OPEN_APP_FLAG";
    public static final String GCM_OPEN_APP_INTERVAL = "com.cube26.trendingapps.GCM_OPEN_APP_INTERVAL";
    
    public static final int UNSENT_DATA_SENDING_PROGRESS = 1111;
    public static final int UPDATE_PROGRESS = 5555;
    public static final int DOWNLOAD_STARTED = 0;
    public static final int DOWNLOAD_COMPLETED = 100;
    public static final int DOWNLOAD_FAILED = -1;
    public static final int DOWNLOAD_PROGRESS = 1;
    public static final int DOWNLOAD_CANCELED = 2;
    public static final int DOWNLOAD_PROGRESS_DEFAULT = 999;
    public static final int INSTALL_STARTED = 150;
    public static final int INSTALL_COMPLETED = 200;
    public static final long DOWNLOAD_PROGRESS_TOTAL_DEFAULT = -999;
    public static final long DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT = -777;

    public static final int timeoutConnection = 10000;
    public static final int timeoutSocket = 5000;
    public static final int NUMBER_OF_FETCHED_APPS = 10;
    public static final int BROADCAST_SEND_INTERVAL = 40;

    public static final int POSITION_REFRESH_WIDGET_BUTTON = NUMBER_OF_FETCHED_APPS + 1;
    public static final int POSITION_DEAL_CONSTANT = NUMBER_OF_FETCHED_APPS + 2;
    public static final int POSITION_TRENDING_APP_LABEL = NUMBER_OF_FETCHED_APPS + 3;

    public static final String PREFS_ANALYTICS_DATA = "com.trendingapps.sendingunsentdata";
    public static final String TRENDING_APPS_FOLDER_NAME = "/.trendingApps/";
    public static final String DOWNLOAD_RECEIVER_INTENT_NAME = "com.cube26.trendingapps.downloadreceiver";
    public static final String DOWNLOAD_RESULT_DOWNLOADED_FILENAME = "com.cube26.trendingapps.downloadedapk";
    public static final String DOWNLOAD_RESULT_INSTALLFLAG = "com.cube26.trendingapps.flaginstallgot";
    public static final String DOWNLOAD_RESULT_ALREADY_DOWNLOADED = "com.cube26.trendingapps.flagalreadydownloaded";

    public static final String WIDGET_APK_UPDATE = "widgetupdate.apk";
    public static final String TAGC26 = "Cube26-TrendingApps";
    //Adding string constants for use in launcher
    public static final String CUBE_WIDGET_RECEIVER_NAME = "com.cube26.trendingnow.WidgetReceiver";
    public static final String CUBE_KEY_WIDGET_ID_TO_FIX = "WIDGET_ID_TO_FIX";

    public static final String SCREENSHOT_IMAGES = "com.cube26.trendingnow.screenshotimages";
    public static final String SCREENSHOT_IMAGE_POSITION = "com.cube26.trendingnow.screenshotimageposition";
    public static final String CACHED_FILE_NAME = "trendingcachefile";
    public static final String WIDGET_UPDATE_APK_PATH = "com.cube26.trendingapps.widgetupdateapkpath";
    public static final String WIDGET_UPDATE_RECEIVER = "com.cube26.trendingapps.APPUPDATE_ALERT";
    public static final int WIDGET_UPDATE_PENDING_INTENT_ID = 16;
    public static final String APP_ACTIVATION_PACKAGES = "com.cube26.trendingapps.APP_ACTIVATION_PACKAGES";
    public static final String DEFAULT_BROWSER_PACKAGE_NAME = "com.android.browser";
    public static final String DEFAULT_BROWSER_CLASS_NAME = "com.android.browser.BrowserActivity";
    public static final String WIDGET_UPDATE_AUTOMATIC = "com.cube26.trendingapps.update.automatic";
    public static final String WIDGET_UPDATE_MANUAL_REFRESH = "com.cube26.trendingapps.update.manual";
    public static final String CHROME_BROWSER_PACKAGE_NAME = "com.android.chrome";
    public static final String YOUTUBE_DEVELOPER_KEY = "AIzaSyCDXW2t6uqpgnhgEG8iF6xA5iZHOU1wbrU";
    public static final String YOUTUBE_PREFIX = "=";

    public static final String AMBIENT_AUTH_TOKEN_KEY = "ambient.auth.token.key";
    public static final String TA_APP_VERSION_KEY = "ta.app.version.key";
    public static final String APP_ACTIVATION_FREQUENCY_KEY = "app.activation.frequency.key";
    public static final String APP_ACTIVATION_COUNTER_KEY = "app.activation.counter.key";

    public static final int REQ_START_STANDALONE_PLAYER = 1;
    public static final int REQ_RESOLVE_SERVICE_MISSING = 2;

    private static char[] hexDigits = "0123456789abcdef".toCharArray();

    public static String getDeviceName() {
        String model = Build.MODEL;
        return model != null ? model : "";
    }

    public static String getAllInstalledApps(Context ctx) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pmgr = ctx.getPackageManager();
        final List<ResolveInfo> pkgAppsList = pmgr.queryIntentActivities(
                mainIntent, 0);
        final List<String> installedAppList = new ArrayList<String>(
                pkgAppsList.size());
        for (ResolveInfo ri : pkgAppsList) {
            installedAppList.add(ri.activityInfo.packageName);
        }
        String allApps = installedAppList.toString();
        return allApps;
    }

    public static String getDeviceId(Context context) {
//        TelephonyManager TelephonyMgr = (TelephonyManager) context
//                .getSystemService(Context.TELEPHONY_SERVICE);
//        String id;
//        TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(context);
//        boolean isDualSIM = telephonyInfo.isDualSIM();
//        if(isDualSIM){
//            String imeiSIM1 = telephonyInfo.getImeiSIM1();
//            String imeiSIM2 = telephonyInfo.getImeiSIM2();
//            id = imeiSIM1 + "," + imeiSIM2;
//        }else{
//            id = TelephonyMgr.getDeviceId();
//        }
    	String id=TelephonyInfo.getImeis(context);
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

    public static boolean isAppDisabled(String packageName, Context context) {
        PackageManager pm = context.getPackageManager();
        boolean disabled = false;
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName,0);

            disabled = !ai.enabled;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return disabled;
    }

    public boolean isPackageExisted(String targetPackage, Context context) {
        List<ApplicationInfo> packages;
        packages = context.getPackageManager().getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(targetPackage))
                return true;
        }
        return false;
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi != null && mWifi.isConnected();
    }

    public static String getPackageName(Context context) {
        String myPackageName = context.getPackageName();
        return myPackageName;
    }

    public static boolean parseBoolean(String inputString) {
        Boolean returnBoolean;
        try {
            returnBoolean = Boolean.parseBoolean(inputString);
        } catch (Exception e) {
            returnBoolean = false;
        }
        return returnBoolean;
    }
    public static int parseInteger(String inputString) {
        int returnInt=0;
        try {
            returnInt = Integer.parseInt(inputString);
        } catch (Exception e) {
            returnInt = 0;
        }
        return returnInt;
    }
    public static boolean checkDataBase(String dbPath) {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(dbPath, null,
                    SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
        } catch (SQLiteException e) {
            // database doesn't exist yet.
            CLog.e(TAGC26,
                    "Database doesn't exist. Exception raised ::"
                            + e.getMessage());
        }
        return checkDB != null ? true : false;
    }

    public static String md5(File fileToCheck) {
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

    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String capitalizeWords(String myString) {
        int stringLength = myString.length(), i;
        String capitalizedString = "";
        boolean firstword = true;
        String stringForAddUpper = myString.toUpperCase(Locale.getDefault());
        String stringForAddLower = myString.toLowerCase(Locale.getDefault());
        for (i = 0; i < stringLength; i++) {
            if (firstword) {
                capitalizedString = capitalizedString
                        + stringForAddUpper.charAt(i);
            } else {
                capitalizedString = capitalizedString
                        + stringForAddLower.charAt(i);
            }
            firstword = false;
            if (myString.charAt(i) == ' ') {
                firstword = true;
            }
        }
        return capitalizedString;
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

    public static void writeSerializedDataToFile(Context context, String data){
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(CACHED_FILE_NAME, Context.MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.close();
        } catch (IOException ex) {
            CLog.e(TAGC26, "Error while writing data to file for cache.");
        }
    }

    public static Object readSerializedDataFromFile(Context context){
        StringBuffer stringBuffer = new StringBuffer();
        try {
            File file = new File(context.getFilesDir(),CACHED_FILE_NAME);
            if(!file.exists()){
                file =copyAssetsFileToInternalFolder(context);
            }
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(
                    context.openFileInput(CACHED_FILE_NAME)));
            String inputString;

            while ((inputString = inputReader.readLine()) != null) {
                stringBuffer.append(inputString + "\n");
            }
        } catch (NotSerializableException ex) {
            CLog.e(TAGC26, "Error while reading cache data from file : "+ex.getMessage());
        } catch (IOException ex) {
            CLog.e(TAGC26, "Error while read ing cache data from file : "+ex.getMessage());
        }
        return stringBuffer.toString();
    }

    public static File copyAssetsFileToInternalFolder(Context ctx){
        AssetManager am = ctx.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = am.open(CACHED_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(inputStream == null){
            return null;
        }
        return createFileFromInputStream(ctx, inputStream);
    }




    private static File createFileFromInputStream(Context ctx, InputStream inputStream) {
        try{
            String destPath = ctx.getFilesDir()+File.separator+CACHED_FILE_NAME;
            new File(destPath).createNewFile();
            OutputStream outputStream = new FileOutputStream(destPath);
            byte buffer[] = new byte[1024];
            int length = 0;
            while((length=inputStream.read(buffer)) != -1) {
                outputStream.write(buffer,0,length);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
            return new File(destPath);
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getLinkifiedText(String text){
        String textWithoutTags = text.replaceAll("<[^>]*>"," ");
        String textWithTagsHttp = new String(textWithoutTags);
        try {
            //http link
            String httpRegex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

            Pattern httpPattern = Pattern.compile(httpRegex);//Pattern.compile("(https?|ftp):[^ ,:*)([/]]*");
            Matcher httpMatcher = httpPattern.matcher(textWithoutTags);
            // Check all occurrence
            while (httpMatcher.find()) {
                String httpUrl = httpMatcher.group();

                textWithTagsHttp = textWithTagsHttp.replaceAll(Pattern.quote(httpUrl), "<a href=\""+httpUrl+"\">"+httpUrl+"</a>");
            }
            return textWithTagsHttp;
        } catch (Exception e) {
            CLog.e(TAGC26, "Error while linkifying text : "+e.getMessage());
            return textWithTagsHttp;
        }
    }

    public static void buildAppClickIntent(Context context, FetchedAppData appData){
        Intent intent = new Intent();

        intent.setComponent(new ComponentName(Util.getPackageName(context),
                Util.APP_DESCRIPTION_PAGE));
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APPNAME, appData.getAppName());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APPRATING, appData.getAppRating());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APPDEV, appData.getAppDev());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_FULLTEXT, appData.getAppFullText());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_ICONURL, appData.getAppIconUrl());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APPURL, appData.getAppUrl());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_IMAGE1, appData.getImage1());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_IMAGE2, appData.getImage2());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_IMAGE3, appData.getImage3());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APPTYPE, appData.getAppType());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_DOWNLOAD, appData.getFlag_Download());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_INSTALL, appData.getFlag_Install());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_MD5, appData.getMD5());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_PACKAGE, appData.getPackageName());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_VIDEOURL, appData.getAppVideoUri());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APK_SIZE, appData.getApkSize());
        context.startActivity(intent);
    }

    public static void playFullScreenVideo(Context context, String video_id){
        Intent appDescPage = new Intent();
        appDescPage.setComponent(new ComponentName(Util.getPackageName(context),
                Util.FULL_SCREEN_VIDEO_PAGE));
        appDescPage.putExtra(FullScreenActivity.VIDEO_KEY, video_id);
        appDescPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(appDescPage);
        } catch (ActivityNotFoundException e) {
            CLog.w(Util.TAGC26, "Exception raised on trying to start activity ::"+ e.getMessage());
        }
    }

    /**
     * this returns {@link FetchedAppData} from package. Here we assume that {@link ArrayList} has {@link FetchedAppData} that have unique packages.
     * @param package name of app
     * @return +ve int or -1 if app is not found.  
     */
    public static int getAppPositionFromPackage(ArrayList<FetchedAppData> appDatas, String appPackage){
        if(appDatas==null)
            return -1;
        for(FetchedAppData appData : appDatas){
            if(appData.getPackageName().equalsIgnoreCase(appPackage)){
                return appDatas.indexOf(appData);
            }
        }
        return -1;
    }

    @SuppressWarnings("unused")
    private static boolean canResolveIntent(Context context, Intent intent) {
        List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        return resolveInfo != null && !resolveInfo.isEmpty();
    }

    public static String getTextForListItemDownloadProgressByPackage(Context context, FetchedAppData appData){
        if (Util.isAppAlreadyInstalled(appData.getPackageName(), context)) {
            return "Open";
        } else {
            File existingFile = new File(
                    Environment.getExternalStorageDirectory(),
                    Util.TRENDING_APPS_FOLDER_NAME + appData.getPackageName() + ".apk");
            if (existingFile.exists()) {
                String currentMD5 = Util.md5(existingFile);
                if (currentMD5.equalsIgnoreCase(appData.getMD5())) {
                    return "Install";
                }else{
                    CLog.d(Util.TAGC26, "A file exists. We don't know whether corrupted or download in progress.");
                    if ( Util.isServiceRunning(DownloadService.class, context)) {
                        return "Waiting..";
                    }else{
                        existingFile.delete();
                        return "Download";
                    }
                }
            } else {
                return "Download";
            }
        }

    }

    public static int getImageForListItemDownloadProgressByPackage(Context context, FetchedAppData appData){
        if (Util.isAppAlreadyInstalled(appData.getPackageName(), context)) {
            return R.drawable.ic_open;
        } else {
            return R.drawable.ic_download;
        }
    }
    private static class InstallAppAsyncTask extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... params) {
            String cmd= params[0];
            executeShellCommand(cmd);
            return null;
        }
    } 
    private static String executeShellCommand(String command) {
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String response = output.toString();
        return response;
    }

    public static void installPackage(File file){
        String command = "pm install -r " + file.getAbsolutePath();
        //executeShellCommand(command);
        new InstallAppAsyncTask().execute(command);
    }
    public static View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public static void openAppForPackageName(Context context, String packageName){
        Intent openApp = new Intent();
        openApp = context.getPackageManager().getLaunchIntentForPackage(packageName);
        try {
            CLog.d(Util.TAGC26, "Starting activity for package :: " + packageName + ", intent :: "+ openApp);
            context.startActivity(openApp);
            SharedPreferences mSharedPrefs;
            mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> set = new HashSet<String>(mSharedPrefs.getStringSet(Util.APP_ACTIVATION_PACKAGES, new HashSet<String>()));
            if(set.contains(packageName)){
                //new SendAnalyticsData(context, Util.APP_ENGAGEMENT).execute(packageName);
                Util.createAndSendAnalyticsData(Util.APP_ENGAGEMENT, Util.CATEGORY_LIST, Util.APP_PACKAGE_NAME, packageName, context);
            }else
            {
                SharedPreferences.Editor editor = mSharedPrefs.edit();
                set.add(packageName);
                editor.putStringSet(Util.APP_ACTIVATION_PACKAGES, set);
                editor.commit();
             //   new SendAnalyticsData(context, Util.ACTIVATED_APP).execute(packageName);
                Util.createAndSendAnalyticsData(Util.ACTIVATED_APP, Util.CATEGORY_LIST, Util.APP_PACKAGE_NAME, packageName, context);
            }
        } catch (Exception e) {
            CLog.e(Util.TAGC26, "ActivityNotFound ::" + e.getMessage());
            if(Util.isAppDisabled(packageName, context)){
                Toast.makeText(context, context.getString(R.string.msgAppDesabled), Toast.LENGTH_LONG).show();
            }
        }
    }
    public static void startDownloadingApps(Context context, ArrayList<FetchedAppData> appsList) {

        ArrayList<String> urlArrayForDownload = new ArrayList<String>();
        ArrayList<String> appNameForDownload = new ArrayList<String>();
        ArrayList<String> shouldInstall = new ArrayList<String>();
        ArrayList<String> downloadOnlyOnWifi = new ArrayList<String>();
        ArrayList<String> appMD5ForDownload = new ArrayList<String>();
        for (FetchedAppData fetchedAppData : appsList) {
            if (fetchedAppData.getFlag_Download()
                    && !isAppAlreadyInstalled(fetchedAppData.getPackageName(), context)) {
                urlArrayForDownload.add(fetchedAppData.getAppUrl());
                appNameForDownload.add(fetchedAppData.getPackageName().replace(" ", ""));
                appMD5ForDownload.add(fetchedAppData.getMD5());
                shouldInstall.add(fetchedAppData.getFlag_Install().toString());
                downloadOnlyOnWifi.add(fetchedAppData.getOnlyWifi().toString());
            }
        }
        Boolean automaticServiceInstantiate = true;
        Intent startDownloadServiceIntent = new Intent(context, DownloadService.class);
        startDownloadServiceIntent.putExtra(
                Util.EXTRA_DOWNLOADSERVICE_URLARRAY, urlArrayForDownload);
        startDownloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_APPNAME, appNameForDownload);
        startDownloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_INSTALL, shouldInstall);
        startDownloadServiceIntent.putExtra(
                Util.EXTRA_DOWNLOADSERVICE_WIFIFLAG, downloadOnlyOnWifi);
        startDownloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_AUTOMATIC, automaticServiceInstantiate);
        startDownloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_APPMD5, appMD5ForDownload);
        startDownloadServiceIntent.putExtra("receiver", new DownloadReceiver(context, new Handler()));
        if(urlArrayForDownload.size() > 0){
            context.startService(startDownloadServiceIntent);
        }
    }


    private static class DownloadReceiver extends ResultReceiver {
        private Context mContext;
        public DownloadReceiver(Context context, Handler handler) {
            super(handler);
            mContext = context;

        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == Util.UPDATE_PROGRESS) {
                String packageNameApk = resultData.getString(Util.DOWNLOAD_RESULT_DOWNLOADED_FILENAME);
                //                Boolean shouldInstall = Util.parseBoolean(resultData
                //                        .getString(Util.DOWNLOAD_RESULT_INSTALLFLAG));
                boolean isAlreadyDownloaded = resultData.getBoolean(Util.DOWNLOAD_RESULT_ALREADY_DOWNLOADED);
                if(!isAlreadyDownloaded){
                    //new SendAnalyticsData(mContext, Util.DOWNLOADED_APP_AUTO).execute(packageNameApk);
                }
                // TODO app already installed from download service so return
                return;
                /*boolean isAppSystemApp = Util.isPackageSystemApp(mContext, mContext.getPackageName());
                if (shouldInstall && isAppSystemApp) {
                    File apkFile = new File(
                            Environment.getExternalStorageDirectory(),
                            Util.TRENDING_APPS_FOLDER_NAME + packageNameApk + ".apk");
                    try {
                        if (apkFile.exists()) {
                            sendStatusBroadcast(packageNameApk, Util.INSTALL_STARTED, mContext);
                            installPackage(apkFile);
                        }else{
                            //sendStatusBroadcast(packageNameApk, Util.DOWNLOAD_COMPLETED, mContext);
                            CLog.e(Util.TAGC26, "Received download success status but apk not found");
                        }
                    } catch (Exception e) {
                        //sendStatusBroadcast(packageNameApk, Util.DOWNLOAD_COMPLETED, mContext);
                        CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
                    }
                } else {
                    //sendStatusBroadcast(packageNameApk, Util.DOWNLOAD_COMPLETED, mContext);
                }*/
            }
        }
    }

    public static void sendStatusBroadcast(String appPackage, int downloadStatus,
            Context myContext) {
        Intent myBroadcastIntent = new Intent(Util.DOWNLOAD_RECEIVER_INTENT_NAME);

        myBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_PACKAGE, appPackage);
        myBroadcastIntent.putExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS, downloadStatus);
        LocalBroadcastManager.getInstance(myContext).sendBroadcast(myBroadcastIntent);
    }
	/**
	 * to create event and send that has to be send to analytics server
	 * 
	 * @param eventAction
	 * @param eventCategory
	 * @param eventLabel
	 * @param eventValue
	 * @param context
	 */
public static void createAndSendAnalyticsData(String eventAction,
			String eventCategory, String eventLabel, String eventValue,
			Context context) {
		EventDTO eventDTO = new EventDTO();
		eventDTO.setEventAction(eventAction);
		eventDTO.setEventCategory(eventCategory);
		eventDTO.setEventLabel(eventLabel);
		eventDTO.setEventValue(eventValue);
		new SendAnalyticsData(context, eventDTO).execute("");
	}



	/**
	 * getting appversion
	 * 
	 * @param context
	 * @return apps own app version from manifest string
	 */
	public static String getOwnAppVersion(Context context) {
		String appVersion = "1.0";

		try {
			appVersion = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			if (e != null) {
				Log.i(TAGC26, "Error retreiving app version :: "
						+ context.getPackageName());
				e.printStackTrace();
			}
		}

		return appVersion;
	}
    public static void startDownloadingApp(Context context, FetchedAppData app) {
        if (!Util.isNetworkAvailable(context)) {
            Toast.makeText(context,
                    "Internet Connection Not Available", Toast.LENGTH_SHORT).show();
            return;
        }
        //        setUIState(Util.DOWNLOAD_STARTED, Util.DOWNLOAD_PROGRESS_TOTAL_DEFAULT, Util.DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT);

        // flags to pass in the download service intent
        ArrayList<String> urlSending     = new ArrayList<String>();
        ArrayList<String> appnameSending = new ArrayList<String>();
        ArrayList<String> appMd5Sending  = new ArrayList<String>();
        ArrayList<String> appInstall     = new ArrayList<String>();

        urlSending.add(app.getAppUrl());
        appnameSending.add(app.getPackageName().replace(" ", ""));
        appMd5Sending.add(app.getMD5());
        appInstall.add(String.valueOf(app.getFlag_Install()));
        Intent downloadServiceIntent = new Intent(context, DownloadService.class);

        downloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_URLARRAY, urlSending);
        downloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_APPNAME, appnameSending);
        downloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_APPMD5, appMd5Sending);
        downloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_INSTALL, appInstall);
        downloadServiceIntent.putExtra("receiver", new DownloadReceiver(context, new Handler()));

        //start the APK download
        context.startService(downloadServiceIntent);

        // send the analytics data
       // new SendAnalyticsData(context, Util.DOWNLOADED_APP).execute(app.getPackageName());
        Util.createAndSendAnalyticsData(Util.DOWNLOADED_APP, Util.CATEGORY_AUTO, Util.APP_PACKAGE_NAME, app.getPackageName(), context);
    }

    public static void openAppLabelUrlInBrowser(Context context){
    	// Commented out to disable app label redirection
//        if(Util.isNetworkAvailable(context)){
//            CLog.d(Util.TAGC26, "Starting activity to show app label url");
//            try{
//                Intent internetIntent = new Intent(Intent.ACTION_VIEW,
//                        Uri.parse(Util.APP_LABEL_URL));
//                internetIntent.setComponent(new ComponentName(Util.DEFAULT_BROWSER_PACKAGE_NAME, Util.DEFAULT_BROWSER_CLASS_NAME));
//                internetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(internetIntent);
//                new SendAnalyticsData(context, Util.EVENT_LABEL).execute(Util.EVENT_MMX_SITE_OPENED);
//            }catch(Exception e){
//                try{
//                    Intent openApp = context.getPackageManager().getLaunchIntentForPackage(Util.CHROME_BROWSER_PACKAGE_NAME);
//                    openApp.setData(Uri.parse(Util.APP_LABEL_URL));
//                    openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    context.startActivity(openApp);
//                    new SendAnalyticsData(context, Util.EVENT_LABEL).execute(Util.EVENT_MMX_SITE_OPENED);
//                    CLog.d(Util.TAGC26, "Opening app label url in chrome browser");
//                }catch(Exception e1){
//                    try{
//                        Intent launchUrlOnClick = new Intent(android.content.Intent.ACTION_VIEW);
//                        launchUrlOnClick.setData(Uri.parse(Util.APP_LABEL_URL));
//                        launchUrlOnClick.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        context.startActivity(launchUrlOnClick);
//                        new SendAnalyticsData(context, Util.EVENT_LABEL).execute(Util.EVENT_MMX_SITE_OPENED);
//                        CLog.d(Util.TAGC26, "Starting activity to show app label url through any other app");
//                    }catch(Exception e2){
//                        CLog.d(Util.TAGC26, "Error opening browser");
//                    }
//                }
//            }
//        }else{
//            Toast.makeText(context, "No Internet connection", Toast.LENGTH_LONG).show();
//        }
    }
    @SuppressLint("DefaultLocale")
    public static String getSizeFromBytes(long size_in_bytes){
        float appSize = 0;
        appSize = (float)size_in_bytes/(1024*1024);
        if(appSize >= 1){
            return ("" + String.format("%.2f", appSize) + " MB");
        }else {
            appSize = (float)size_in_bytes/1024;
            if(appSize > 0 && !String.format("%.2f", appSize).equalsIgnoreCase("0.00")){
                return ("" + String.format("%.2f", appSize) + " KB");
            }else{
                return (size_in_bytes > 1 ? size_in_bytes + " Bytes" : size_in_bytes + " Byte");
            }
        }
    }

    public static String getAndroidId(Context context){
        String android_id = Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);
        return android_id;
    }

    public static String getCurrentTimeInMillis(){
        return String.valueOf(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());
    }

    public static String getTimeZone(){
        return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
    }
    public static String getAuthTokenFromSharedPref(Context ctx){
        SharedPreferences mSharedPrefs;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mSharedPrefs.getString(Util.AMBIENT_AUTH_TOKEN_KEY, "");
    }

    public static void setAuthTokenInSharedPref(Context ctx, String onOffFlag){
        SharedPreferences mSharedPrefs;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(Util.AMBIENT_AUTH_TOKEN_KEY, onOffFlag);
        editor.commit();
    }
    public static String getAppVersionFromSharedPref(Context ctx){
        SharedPreferences mSharedPrefs;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mSharedPrefs.getString(Util.TA_APP_VERSION_KEY, "");
    }

    public static void openTrendingApp(Context context){
        Intent openApp = new Intent();
        openApp = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        try {
            context.startActivity(openApp);
            CLog.d(Util.TAGC26, "Starting trending app on cancel click");
        } catch (Exception e) {
            CLog.d(Util.TAGC26, "Error in starting trending app on cancel click"+ e.getMessage());
        }
    }
    public static void setAppVersionInSharedPref(Context ctx, String appVersion){
        SharedPreferences mSharedPrefs;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(Util.TA_APP_VERSION_KEY, appVersion);
        editor.commit();
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
    public static int getAppActivationFrequencyFromSharedPref(Context ctx){
        SharedPreferences mSharedPrefs;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mSharedPrefs.getInt(Util.APP_ACTIVATION_FREQUENCY_KEY, 1);
    }

    public static void setAppActivationFrequencyInSharedPref(Context ctx, int frequency){
        SharedPreferences mSharedPrefs;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putInt(Util.APP_ACTIVATION_FREQUENCY_KEY, frequency);
        editor.commit();
    }
    public static int getAppActivationCounterFromSharedPref(Context ctx){
        SharedPreferences mSharedPrefs;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mSharedPrefs.getInt(Util.APP_ACTIVATION_COUNTER_KEY, 1);
    }

    public static void setAppActivationCounterInSharedPref(Context ctx, int frequency){
        SharedPreferences mSharedPrefs;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putInt(Util.APP_ACTIVATION_COUNTER_KEY, frequency);
        editor.commit();
    }

    public static void dumpIntent(Intent i){

        Bundle bundle = i.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            CLog.e(TAGC26,"Dumping Intent start");
            while (it.hasNext()) {
                String key = it.next();
                CLog.e(TAGC26,"[" + key + "=" + bundle.get(key)+"]");
            }
            CLog.e(TAGC26,"Dumping Intent end");
        }
    }
}