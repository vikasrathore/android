/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.trendingnow;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;

import com.cube26.celkonstore.R;
import com.cube26.trendingnow.util.CLog;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cube26.trendingapps.analytics.EventDTO;
import com.cube26.trendingapps.analytics.SendAnalyticsData;
import com.cube26.trendingapps.analytics.SendAnalyticsDataPostRequest;
import com.cube26.trendingapps.analytics.SendUnsentData;
import com.cube26.trendingapps.receivers.AppDownloadReceiver;
import com.cube26.trendingapps.ui.ImageNonViewAware;
import com.cube26.trendingapps.webservices.DownloadService;
import com.cube26.trendingapps.webservices.FetchedAppData;
import com.cube26.trendingapps.webservices.FetchedDealData;
import com.cube26.trendingapps.webservices.FetchedWidgetUpdateData;
import com.cube26.trendingapps.webservices.JsonResponseParser;
import com.cube26.trendingapps.webservices.WidgetUpdateService;
import com.cube26.trendingnow.WidgetReceiver.WidgetContentFetcher;
import com.cube26.trendingnow.util.AppVersion;
import com.cube26.trendingnow.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class WidgetReceiver extends AppWidgetProvider {

    //private Resources mRes;
    private RemoteViews mRemoteViews;
    private Context mContext;
    private ComponentName mThisWidget;
    private AppWidgetManager mManager;
    public static Map<String, Bitmap> mUrlBitmapMap = new HashMap<String, Bitmap>();
    public static final Object mLock = new Object();
    public ArrayList<FetchedAppData> mTrendingAppsArray = new ArrayList<FetchedAppData>();
    public ArrayList<FetchedAppData> mOrderedTrendingAppsArray = new ArrayList<FetchedAppData>();
    private int[] mIndexOrderArray = new int[Util.NUMBER_OF_FETCHED_APPS];
    private int[] mImageViewOrderedArray = new int[] { R.id.imageView1,
            R.id.imageView2, R.id.imageView3, R.id.imageView4, R.id.imageView5,
            R.id.imageView1back, R.id.imageView2back, R.id.imageView3back, R.id.imageView4back,
            R.id.imageView5back};
    private int[] mTextViewOrderedArray = new int[] { R.id.tvHolder11,
            R.id.tvHolder21, R.id.tvHolder31, R.id.tvHolder41, R.id.tvHolder51,
            R.id.tvHolder12, R.id.tvHolder22, R.id.tvHolder32, R.id.tvHolder42,
            R.id.tvHolder52};
    private int[] mColourTileOrderedArray = new int[] { R.id.ivColour11,
            R.id.ivColour21, R.id.ivColour31, R.id.ivColour41, R.id.ivColour51,
            R.id.ivColour12, R.id.ivColour22, R.id.ivColour32, R.id.ivColour42,
            R.id.ivColour52};
    private ArrayList<FetchedDealData> mTrendingDealData = new ArrayList<FetchedDealData>();

    private static DisplayImageOptions displayOptions;
    static {
        displayOptions = new DisplayImageOptions.Builder()
        .showImageForEmptyUri(R.drawable.dummy)
        .showImageOnFail(R.drawable.dummy)
        .resetViewBeforeLoading(true)  // default
        .delayBeforeLoading(10)
        .cacheInMemory(false) // default
        .cacheOnDisk(true) // default
        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
        .bitmapConfig(Bitmap.Config.RGB_565) // default
        .displayer(new SimpleBitmapDisplayer()) // default
        .handler(new Handler()) // default
        .build();
    }
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        CLog.d(Util.TAGC26, "App Version :: "+ Util.APP_VERSION);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).build();
        ImageLoader.getInstance().init(config);
        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        mThisWidget = new ComponentName(context, WidgetReceiver.class);
        mContext = context;
        mManager = appWidgetManager;
        //mRes = context.getResources();
        mRemoteViews.setOnClickPendingIntent(R.id.refreshWidget,
                buildButtonPendingIntent(mContext, Util.POSITION_REFRESH_WIDGET_BUTTON));
        
        mRemoteViews.setOnClickPendingIntent( R.id.layoutTopWidget,
                buildButtonPendingIntent(mContext, Util.POSITION_TRENDING_APP_LABEL));
        //mRemoteViews.setViewVisibility(R.id.lDeal, View.INVISIBLE);
        mManager.updateAppWidget(mThisWidget, mRemoteViews);
        try {
            Intent sendUnsentDataServiceIntent = new Intent(mContext,
                    SendUnsentData.class);
            mContext.startService(sendUnsentDataServiceIntent);
        }  catch (Exception e){
            CLog.d(Util.TAGC26, "Unsent data sending service not instantiated");
        }
        WidgetContentFetcher fetcher = new WidgetContentFetcher();
        fetcher.execute(Util.WIDGET_UPDATE_AUTOMATIC);
        AppDownloadReceiver.setAlarm(mContext);
    }
    
    public void setContext(Context context){
        mContext = context;
    }

    public PendingIntent buildButtonPendingIntent(Context context,
            Integer position) {
        Intent intent = new Intent();

        switch (position) {
            case Util.POSITION_REFRESH_WIDGET_BUTTON:
                intent.setAction(Util.UPDATE_ACTION);
                break;
            case Util.POSITION_DEAL_CONSTANT:
                if (mTrendingDealData.size() > 0) {
                    CLog.d(Util.TAGC26, "Setting deal click receiver for ::" +mTrendingDealData.get(0));
                    intent.setAction(Util.OPEN_DEAL_CLICK_RECEIVER);
                    intent.putExtra(Util.EXTRA_WIDGET_DEAL_DATA, mTrendingDealData.get(0));
                }
                break;
            case Util.POSITION_TRENDING_APP_LABEL:
                //intent.setAction(Util.APP_LABEL_URL_ACTION);
                break;
            default:
                intent.setAction(Util.OPEN_WIDGET_CLICK_RECEIVER);
                intent.putExtra("TYPE", position);
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_APPNAME, mTrendingAppsArray.get(position).getAppName());
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_APPRATING, mTrendingAppsArray.get(position).getAppRating());
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_APPDEV, mTrendingAppsArray.get(position).getAppDev());
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_FULLTEXT, mTrendingAppsArray.get(position).getAppFullText());
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_ICONURL, mTrendingAppsArray.get(position).getAppIconUrl());
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_APPURL, mTrendingAppsArray.get(position).getAppUrl());
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_IMAGE1, mTrendingAppsArray.get(position).getImage1());
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_IMAGE2, mTrendingAppsArray.get(position).getImage2());
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_IMAGE3, mTrendingAppsArray.get(position).getImage3());
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_APPTYPE, mTrendingAppsArray.get(position).getAppType());
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_DOWNLOAD, mTrendingAppsArray.get(position).getFlag_Download());
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_INSTALL, mTrendingAppsArray.get(position).getFlag_Install());
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_MD5, mTrendingAppsArray.get(position).getMD5());
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_PACKAGE, mTrendingAppsArray.get(position).getPackageName());
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_VIDEOURI, mTrendingAppsArray.get(position).getAppVideoUri());
                intent.putExtra(Util.EXTRA_WIDGET_ONCLICK_APKSIZE, mTrendingAppsArray.get(position).getApkSize());
                CLog.d(Util.TAGC26, "Generated a pending intent for app ::"+ mTrendingAppsArray.get(position).getAppName());
                break;
        }
        return PendingIntent.getBroadcast(context, position, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(Util.UPDATE_ACTION)) {
            CLog.d(Util.TAGC26, "App Version :: "+ Util.APP_VERSION);
            mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            mThisWidget = new ComponentName(context, WidgetReceiver.class);
            mManager = AppWidgetManager.getInstance(context);
            mContext = context;

            if(Util.isNetworkAvailable(mContext)){
                ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                        context).build();
                ImageLoader.getInstance().init(config);
                mRemoteViews.setViewVisibility(R.id.progressBar1, View.VISIBLE);
                mRemoteViews.setViewVisibility(R.id.tvErrorFetching, View.INVISIBLE);
                mManager.updateAppWidget(mThisWidget, mRemoteViews);
                Intent sendUnsentDataServiceIntent = new Intent(mContext,
                        SendUnsentData.class);
                mContext.startService(sendUnsentDataServiceIntent);
                mRemoteViews.setOnClickPendingIntent( R.id.refreshWidget,
                        buildButtonPendingIntent(mContext, Util.POSITION_REFRESH_WIDGET_BUTTON));
                mRemoteViews.setOnClickPendingIntent( R.id.layoutTopWidget,
                        buildButtonPendingIntent(mContext, Util.POSITION_TRENDING_APP_LABEL));
                Util.createAndSendAnalyticsData(Util.EVENT_LABEL, Util.CATEGORY_MANUAL, Util.APP_PACKAGE_NAME, Util.EVENT_REFRESH_BUTTON_CLICKED, mContext);
                WidgetContentFetcher fetcher_new = new WidgetContentFetcher();
                fetcher_new.execute(Util.WIDGET_UPDATE_MANUAL_REFRESH);
            }else{
                Toast.makeText(mContext, "No Internet Connection", Toast.LENGTH_LONG).show();
            }

        } else if (intent.getAction().equals(Util.DEAL_ACTION)) {
            String extraObtained = intent.getStringExtra(Util.EXTRA_WIDGET_DEAL_URL);
            mContext = context;
            if(Util.isNetworkAvailable(mContext)){
                Intent launchDealApplication = new Intent(android.content.Intent.ACTION_VIEW);
              //  new SendAnalyticsData(mContext, Util.CLICKED_APP +"deal_").execute(extraObtained);
                Util.createAndSendAnalyticsData(Util.CLICKED_APP, Util.CATEGORY_MANUAL, Util.APP_PACKAGE_NAME, extraObtained, mContext);
                try {
                    launchDealApplication.setData(Uri.parse(extraObtained));
                    launchDealApplication.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    CLog.d(Util.TAGC26, "Starting activity to show deal, intent :: "+ launchDealApplication);
                    mContext.startActivity(launchDealApplication);
                } catch (Exception e) {
                    CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
                    if (!extraObtained.startsWith("https://") && !extraObtained.startsWith("http://")){
                        extraObtained = "http://" + extraObtained;
                    }
                    mContext = context;
                    Intent launchDealOnClick = new Intent(android.content.Intent.ACTION_VIEW);
                    launchDealOnClick.setData(Uri.parse(extraObtained));
                    launchDealOnClick.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    CLog.d(Util.TAGC26, "Starting activity to show deal, intent :: "+ launchDealApplication);
                    mContext.startActivity(launchDealOnClick);
                }
            }else{
                Toast.makeText(mContext, "No Internet connection", Toast.LENGTH_LONG).show();
            }
        }else if(intent.getAction().equals(Util.APP_LABEL_URL_ACTION)){
        	// Commented out to disable app label redirection
//            CLog.d(Util.TAGC26, "Starting activity to show app label url");
//            mContext = context;
//            if(Util.isNetworkAvailable(mContext)){
//                try{
//                    Intent internetIntent = new Intent(Intent.ACTION_VIEW,
//                            Uri.parse(Util.APP_LABEL_URL));
//                    internetIntent.setComponent(new ComponentName(Util.DEFAULT_BROWSER_PACKAGE_NAME, Util.DEFAULT_BROWSER_CLASS_NAME));
//                    internetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    mContext.startActivity(internetIntent);
//                    new SendAnalyticsData(mContext, Util.EVENT_LABEL).execute(Util.EVENT_MMX_SITE_OPENED);
//                    CLog.d(Util.TAGC26, "Opening app label url in default browser");
//                }catch(Exception e){
//                    try{
//                        Intent openApp = mContext.getPackageManager().getLaunchIntentForPackage(Util.CHROME_BROWSER_PACKAGE_NAME);
//                        openApp.setData(Uri.parse(Util.APP_LABEL_URL));
//                        openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        context.startActivity(openApp);
//                        new SendAnalyticsData(mContext, Util.EVENT_LABEL).execute(Util.EVENT_MMX_SITE_OPENED);
//                        CLog.d(Util.TAGC26, "Opening app label url in chrome browser");
//                    }catch(Exception e1){
//                        try{
//                            Intent launchUrlOnClick = new Intent(android.content.Intent.ACTION_VIEW);
//                            launchUrlOnClick.setData(Uri.parse(Util.APP_LABEL_URL));
//                            launchUrlOnClick.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            mContext.startActivity(launchUrlOnClick);
//                            new SendAnalyticsData(mContext, Util.EVENT_LABEL).execute(Util.EVENT_MMX_SITE_OPENED);
//                            CLog.d(Util.TAGC26, "Starting activity to show app label url through any other app");
//                        }catch(Exception e2){
//                            CLog.d(Util.TAGC26, "Error opening browser");
//                        }
//                    }
//                }
//            }else{
//                Toast.makeText(mContext, "No Internet connection", Toast.LENGTH_LONG).show();
//            }
        }
    };

    public class WidgetUpdateTask extends AsyncTask<String, Void, FetchedWidgetUpdateData>{

        FetchedWidgetUpdateData widgetData;
        String result;
        List<NameValuePair> postData;
        
        @Override
        protected FetchedWidgetUpdateData doInBackground(String... arg0) {
            
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, Util.timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, Util.timeoutSocket);
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            postData = new ArrayList<NameValuePair>();
            try {
                String url =
                        Util.API_URL_UPDATE
                        + "?user_id=" + Util.getDeviceId(mContext)
                        + "&model=" + URLEncoder.encode(Util.getDeviceName(),"UTF-8")
                        + "&app_version=" + Util.getAppVersion(mContext)
                        + "&country_code="+ URLEncoder.encode(Util.getCountryCode(mContext), "UTF-8");
                HttpPost httppost = new HttpPost(url);
                postData.add(new BasicNameValuePair("model_name",Util.getDeviceName()));
                httppost.setEntity(new UrlEncodedFormEntity(postData));
                CLog.d(Util.TAGC26, "Url for app update :: "+ url);
                HttpResponse response = httpclient.execute(httppost);
                String result = EntityUtils.toString(response.getEntity());
                Log.d("JsResult", "Response"+response+"Result"+result);
                JSONObject widgetJSONObject = new JSONObject(result);
                
                widgetData = new FetchedWidgetUpdateData();
                String updateUrl= widgetJSONObject.getString("app_update_url").replaceAll("", "");
                CLog.d(Util.TAGC26, "Update url ::" + updateUrl);
                widgetData.setUpdateURL(updateUrl);
                widgetData.setVersion(widgetJSONObject.getString("app_version"));
                widgetData.setMd5(widgetJSONObject.getString("app_md5"));
                
            } catch (JSONException e) {
                CLog.e(Util.TAGC26, "JSONException in widget update " + "Error: " + e.toString()
                        + "::" + result);
            } catch (UnsupportedEncodingException e1) {
                CLog.e(Util.TAGC26,
                        "UnsupportedEncodingException in widget update " + e1.toString());
                e1.printStackTrace();
            } catch (ClientProtocolException e2) {
                CLog.e(Util.TAGC26, "ClientProtocolException in widget update " + e2.toString());
                e2.printStackTrace();
            } catch (IllegalStateException e3) {
                CLog.e(Util.TAGC26, "IllegalStateException in widget update " + e3.toString());
                e3.printStackTrace();
            } catch (IOException e4) {
                CLog.e(Util.TAGC26, "IOException in widget update " + e4.toString());
                e4.printStackTrace();
            } catch (Exception e5) {
                CLog.e(Util.TAGC26, "Unexcepted exception in widget update " + e5.toString());
            }
            return widgetData;
        }
        
        @Override
        protected void onPostExecute(FetchedWidgetUpdateData result) {
            super.onPostExecute(result);
            String appVersion = String.valueOf(Integer.MAX_VALUE);
            try {
                PackageInfo pInfo = mContext.getPackageManager()
                        .getPackageInfo(mContext.getPackageName(), 0);
                appVersion = pInfo.versionName;
                AppVersion currentAppversion = new AppVersion(appVersion);
                AppVersion serverAppVersion = new AppVersion(widgetData.getVersion());
                if (widgetData != null) {
                    if (serverAppVersion.compareTo(currentAppversion)>0) {
                        if (!Util.isServiceRunning(WidgetUpdateService.class, mContext)) {
                            Intent startWidgetUpdateServiceIntent = new Intent(
                                    mContext, WidgetUpdateService.class);
                            startWidgetUpdateServiceIntent.putExtra(Util.WIDGET_UPDATE_EXTRA,
                                    widgetData.getUpdateURL());
                            startWidgetUpdateServiceIntent.putExtra(Util.WIDGET_UPDATE_MD5,
                                    widgetData.getMd5());
                            startWidgetUpdateServiceIntent.putExtra("receiver",
                                    new WidgetUpdateDownloadReceiver(
                                            new Handler()));
                            //start the update service
                            mContext.startService(startWidgetUpdateServiceIntent);
                        }
                    }else{
                        CLog.d(Util.TAGC26, "Current app version " + appVersion + " :: Server app version :: "+widgetData.getVersion());
                    }
                }
            } catch (Exception e) {
                CLog.d(Util.TAGC26, "Exception ::" + e.getMessage());
            }
        }
    }

    public class WidgetContentFetcher extends
    AsyncTask<String, Void, ArrayList<FetchedAppData>> {

        boolean shouldUpdateWidget = true;
        boolean hasExceptionOccurred = false;
        FetchedWidgetUpdateData widgetDataObtained;
        FetchedDealData dealDataObtained;
        String appUpdateMethod;
        private int appActivationFrequency;
        @Override
        protected ArrayList<FetchedAppData> doInBackground(String... params) {
          
            appUpdateMethod = params[0];
            String result = "";
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, Util.timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, Util.timeoutSocket);
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            ArrayList<FetchedAppData> fetchedAppDataList = new ArrayList<FetchedAppData>();

            try {
                HttpGet httppost = new HttpGet(
                        Util.API_URL
                        + "?user_id=" + Util.getDeviceId(mContext)
                        + "&product=" + Util.PRODUCT_NAME
                        + "&model=" + URLEncoder.encode(Util.getDeviceName(),"UTF-8")
                        + "&mcc=" + URLEncoder.encode(Util.getMccCode(mContext),"UTF-8")
                        + "&mnc=" + URLEncoder.encode(Util.getMncCode(mContext),"UTF-8")
                        + "&et=" + URLEncoder.encode(Util.getCurrentTimeInMillis(),"UTF-8")
                        + "&eTz=" + URLEncoder.encode(Util.getTimeZone(),"UTF-8")
                        + "&app_version=" + Util.getAppVersion(mContext)
                        //+ "&installed_apps="+ URLEncoder.encode(Util.getAllInstalledApps(mContext).replace("[", "").replace("]", "").replace(",", ""), "UTF-8")
                        + "&country_code="+ URLEncoder.encode(Util.getCountryCode(mContext), "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);
                result = EntityUtils.toString(response.getEntity());

                JsonResponseParser responseParser = new JsonResponseParser();
                responseParser.parseJsonResponseString(result);
                
                widgetDataObtained = responseParser.getFetchedWidgetUpdateData();
                FetchedDealData fetchedDealData =responseParser.getFetchedDealData(); 
                if(fetchedDealData!=null){
                    mTrendingDealData.add(fetchedDealData);
                }
                List<FetchedAppData> fetchedAppDatas =responseParser.getFetchedAppDataList(); 
                if(fetchedAppDatas!=null && fetchedAppDatas.size()>0){
                    fetchedAppDataList.addAll(fetchedAppDatas);
                }
                
                List<FetchedAppData> fetchedBannerAppDatas =responseParser.getFetchedBannerAppsList(); 
                if(fetchedBannerAppDatas!=null && fetchedBannerAppDatas.size()>0){
                    fetchedAppDataList.addAll(fetchedBannerAppDatas);
                }

                appActivationFrequency = responseParser.getAppActivationFrequency();
                //No exception till now, so Writing data to file for cache purpose
                Util.writeSerializedDataToFile(mContext, result);
            } catch (JSONException e) {
                shouldUpdateWidget=false;
                hasExceptionOccurred =true;
                CLog.e(Util.TAGC26, "JSONException" + "Error: " + e.toString()
                        + "::" + result);
            } catch (UnsupportedEncodingException e1) {
                shouldUpdateWidget=false;
                hasExceptionOccurred =true;
                CLog.e(Util.TAGC26,
                        "UnsupportedEncodingException" + e1.toString());
                e1.printStackTrace();
            } catch (ClientProtocolException e2) {
                shouldUpdateWidget=false;
                hasExceptionOccurred =true;
                CLog.e(Util.TAGC26, "ClientProtocolException" + e2.toString());
                e2.printStackTrace();
            } catch (IllegalStateException e3) {
                shouldUpdateWidget=false;
                hasExceptionOccurred =true;
                CLog.e(Util.TAGC26, "IllegalStateException" + e3.toString());
                e3.printStackTrace();
            } catch (IOException e4) {
                shouldUpdateWidget=false;
                hasExceptionOccurred =true;
                CLog.e(Util.TAGC26, "IOException" + e4.toString());
                e4.printStackTrace();
            } catch (Exception e5) {
                shouldUpdateWidget=false;
                hasExceptionOccurred =true;
                CLog.e(Util.TAGC26, "Unexcepted exception" + e5.toString());
            }
            if(hasExceptionOccurred){
                try{
                    String dataStoredLocally = (String) Util.readSerializedDataFromFile(mContext);
                    CLog.e(Util.TAGC26, "Reading data from cache.");
                    JsonResponseParser responseParser = new JsonResponseParser();
                    responseParser.parseJsonResponseString(dataStoredLocally);

                    widgetDataObtained = responseParser.getFetchedWidgetUpdateData();
                    FetchedDealData fetchedDealData =responseParser.getFetchedDealData(); 
                    if(fetchedDealData!=null){
                        mTrendingDealData.add(fetchedDealData);
                    }
                    fetchedAppDataList.addAll(responseParser.getFetchedAppDataList());
                    fetchedAppDataList.addAll(responseParser.getFetchedBannerAppsList());
                    appActivationFrequency = responseParser.getAppActivationFrequency();
                }catch(Exception e){
                    CLog.e(Util.TAGC26, "Exception occurred while reading cached data :: " + e.getMessage());
                }    
            }
            return fetchedAppDataList;
        }

        @Override
        protected void onPostExecute(ArrayList<FetchedAppData> result) {
            synchronized (mLock) {
                mTrendingAppsArray = result;
            }
           /* String appVersion = String.valueOf(Integer.MAX_VALUE);
            try {
                PackageInfo pInfo = mContext.getPackageManager()
                        .getPackageInfo(mContext.getPackageName(), 0);
                appVersion = pInfo.versionName;
            } catch (Exception e) {
                CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
            }
            if (widgetDataObtained != null) {
                if (Float.parseFloat(widgetDataObtained.getVersion()) > Float.parseFloat(appVersion)) {
                    if (!Util.isServiceRunning(WidgetUpdateService.class, mContext)) {
                        Intent startWidgetUpdateServiceIntent = new Intent(
                                mContext, WidgetUpdateService.class);
                        startWidgetUpdateServiceIntent.putExtra(Util.WIDGET_UPDATE_EXTRA,
                                widgetDataObtained.getUpdateURL());
                        startWidgetUpdateServiceIntent.putExtra("receiver",
                                new WidgetUpdateDownloadReceiver(
                                        new Handler()));
                        //start the update service
                        mContext.startService(startWidgetUpdateServiceIntent);
                        mRemoteViews.setViewVisibility(R.id.progressBar1, View.INVISIBLE);
                        mManager.updateAppWidget(mThisWidget, mRemoteViews);
                    }
                }
            }*/
            boolean shouldSkipActivation = false;
            if(Util.getAppActivationCounterFromSharedPref(mContext)>=appActivationFrequency){
                Util.setAppActivationCounterInSharedPref(mContext, 1); // Resetting the counter
            }else{
                Util.setAppActivationCounterInSharedPref(mContext, Util.getAppActivationCounterFromSharedPref(mContext) + 1); // increase by 1
                shouldSkipActivation = true;
            }
            if (mTrendingAppsArray.size() == 0) {
                mRemoteViews.setViewVisibility(R.id.progressBar1, View.INVISIBLE);
                mRemoteViews.setViewVisibility(R.id.tvErrorFetching, View.VISIBLE);
                mManager.updateAppWidget(mThisWidget, mRemoteViews);
            } else if (mTrendingAppsArray.size() > 0) {
                if (mTrendingAppsArray.size() <= Util.NUMBER_OF_FETCHED_APPS
                        && mTrendingAppsArray.size() > 0) {
                    int mUnFilledArrayCount = Util.NUMBER_OF_FETCHED_APPS - mTrendingAppsArray.size();
                    FetchedAppData fetchedAppDataToFill;
                    for (int k = 0; k < mUnFilledArrayCount; k++) {
                        fetchedAppDataToFill = new FetchedAppData();
                        fetchedAppDataToFill.setAppName("");
                        fetchedAppDataToFill.setAppIconUrl("");
                        fetchedAppDataToFill.setAppUrl("");
                        fetchedAppDataToFill.setMD5("");

                        fetchedAppDataToFill.setAppType("social");
                        fetchedAppDataToFill.setActivationTime("-1");
                        fetchedAppDataToFill.setPackageName(" ");
                        fetchedAppDataToFill.setFlag_Download(false);
                        fetchedAppDataToFill.setFlag_Install(false);
                        fetchedAppDataToFill.setFlag_Premium(false);
                        mTrendingAppsArray.add(fetchedAppDataToFill);
                    }
                }
                if(!shouldSkipActivation){
                    for (int index = 0; index < Util.NUMBER_OF_FETCHED_APPS; index++) {
                        String activationTimeString = mTrendingAppsArray.get(index)
                                .getActivationTime();
                        Integer activationTime;
                        try {
                            activationTime = Integer.parseInt(activationTimeString);
                        } catch (Exception e) {
                            CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
                            activationTime = -1;
                        }
                        if (activationTime > 0) {
                            Integer hours = Integer.parseInt(activationTimeString.substring(0, 2));
                            Integer minutes = Integer.parseInt(activationTimeString.substring(2));
                            Calendar calNow = Calendar.getInstance();
                            Calendar calSet = (Calendar) calNow.clone();

                            calSet.set(Calendar.HOUR_OF_DAY, hours);
                            calSet.set(Calendar.MINUTE, minutes);
                            calSet.set(Calendar.SECOND, 0);
                            calSet.set(Calendar.MILLISECOND, 0);

                            if (calSet.compareTo(calNow) <= 0) {
                                calSet.add(Calendar.DATE, 1);
                            }

                            Intent alarmIntent = new Intent(Util.RECEIVER_ACTIVATION);
                            alarmIntent.putExtra(Util.EXTRA_PACKAGE_TO_ACTIVATE,
                                    mTrendingAppsArray.get(index).getPackageName());
                            alarmIntent.putExtra(Util.EXTRA_FORCE_TOUCH_ATTRIBUTES, mTrendingAppsArray.get(index).getForceClickAttributes());
                            alarmIntent.putExtra(Util.EXTRA_FORCE_CLOSE,mTrendingAppsArray.get(index).getForceCloseApp());
                            
                            PendingIntent pendingIntent = PendingIntent
                                    .getBroadcast(mContext, activationTime,
                                            alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                            AlarmManager alarmManager = (AlarmManager) mContext
                                    .getSystemService(Context.ALARM_SERVICE);
                            alarmManager.set(AlarmManager.RTC_WAKEUP,
                                    calSet.getTimeInMillis(), pendingIntent);
                        }
                    }
                }else{
                    CLog.d(Util.TAGC26, "Skipped app activation");
                }
                mRemoteViews.setViewVisibility(R.id.tvErrorFetching, View.INVISIBLE);
                mManager.updateAppWidget(mThisWidget, mRemoteViews);
                setUI();
                setWidgetClickListeners();
                loadBitmapArray();
                startDownloadingApps(mTrendingAppsArray);
//                BitmapArraySetter fetcher2 = new BitmapArraySetter();
//                fetcher2.execute("");
            }
            if (mTrendingDealData.size() >0) {
                loadDealBitmap(mTrendingDealData.get(0).getDealOfferUrl());
//                DealBitmapFetcher getMyDeal = new DealBitmapFetcher();
//                getMyDeal.execute("");
            } else if(shouldUpdateWidget){
                mRemoteViews.setViewVisibility(R.id.lDeal, View.INVISIBLE);
                mManager.updateAppWidget(mThisWidget, mRemoteViews);
            }
            
            // Check if manual refresh or automatic
            //if(Util.WIDGET_UPDATE_MANUAL_REFRESH.equalsIgnoreCase(appUpdateMethod)){
                // Starting a task to check for update from mmx server
//                WidgetUpdateTask updateTask = new WidgetUpdateTask();
//                updateTask.execute("");
//            }else{
//                CLog.d(Util.TAGC26, "Automatic refresh of widget. So not looking for any update");
//            }
           try {
                List<NameValuePair> postData = new ArrayList<NameValuePair>();
                postData.add(new BasicNameValuePair("user_id",Util.getDeviceId(mContext)));
                postData.add(new BasicNameValuePair("product",Util.PRODUCT_NAME));
                postData.add(new BasicNameValuePair("android_id",Util.getAndroidId(mContext)));
                postData.add(new BasicNameValuePair("model",Util.getDeviceName()));
                postData.add(new BasicNameValuePair("country_code",Util.getCountryCode(mContext)));
                postData.add(new BasicNameValuePair("ver",Util.APP_ANALYTICS_VERSION));
                postData.add(new BasicNameValuePair("eT", Util.getCurrentTimeInMillis()));
                postData.add(new BasicNameValuePair("eTz",Util.getTimeZone()));
                postData.add(new BasicNameValuePair("app_version", Util.getAppVersion(mContext)));
                
                EventDTO eventDTO = new EventDTO();
                eventDTO.setEventAction(Util.INSTALLED_APPS_LIST);
                eventDTO.setEventValue(Util.getAllInstalledApps(mContext).replace("[", "").replace("]", ""));
//                eventDTO.setEventName(Util.INSTALLED_APPS_LIST);
//                eventDTO.setPackageName(Util.getAllInstalledApps(mContext).replace("[", "").replace("]", ""));
                eventDTO.setEventTime(URLEncoder.encode(Util.getCurrentTimeInMillis(), "UTF-8"));

                postData.add(new BasicNameValuePair("events",new JSONArray().put(eventDTO.toString()).toString()));

                new SendAnalyticsDataPostRequest(postData).execute("");
                
                CLog.e(Util.TAGC26, "The country code is ::"+Util.getCountryCode(mContext)+"a");
            } catch (Exception e) {
                CLog.w(Util.TAGC26, "Exception incurred while sending complete analytics data ::"
                        + e.getMessage());
            }
        }
    }

    private void loadBitmapArray(){
        for (int index = 0; index < Util.NUMBER_OF_FETCHED_APPS; index++) {
            if (mOrderedTrendingAppsArray.size() > 0) {
                final FetchedAppData content = mOrderedTrendingAppsArray.get(index);
                loadBitmapItem(index, content);
            }
        }
    }
    
    private void loadDealBitmap(String imageUrl){
        ImageLoader.getInstance()
        .loadImage(imageUrl, displayOptions, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mRemoteViews.setImageViewBitmap(R.id.ivDealPic, loadedImage);
                //mRemoteViews.setImageViewBitmap(R.id.ivDealIcon, dealBitmaps[1]);
                mRemoteViews.setViewVisibility(R.id.lDeal, View.VISIBLE);
                mManager.updateAppWidget(mThisWidget, mRemoteViews);
            }
        });
    }
    private void loadBitmapItem(int index, final FetchedAppData content){
        mRemoteViews.setTextViewText(mTextViewOrderedArray[index],content.getAppName());
//        mRemoteViews.setImageViewBitmap(mColourTileOrderedArray[index],
//                selectColourForTile(content.getAppType()));
//        mRemoteViews.setInt(mColourTileOrderedArray[index], "setBackgroundColor", 
//                selectColourIntForTile(content.getAppType()));
        mRemoteViews.setInt(mColourTileOrderedArray[index], "setBackgroundColor", 
                selectColourIntForTile(content.getAppType()));
        mRemoteViews.setImageViewResource(mColourTileOrderedArray[index], selectColourIntForTile(content.getAppType()));
        mManager.updateAppWidget(mThisWidget, mRemoteViews);
        final int imageViewResourceId = mImageViewOrderedArray[index];
        
        //CLog.d(Util.TAGC26, "Image :: "+imageViewResourceId +" :: index :: "+index+" :: imageUri :: "+content.getAppIconUrl());
        
        ImageSize imageSize = new ImageSize(128, 128);
        ImageNonViewAware imageAware = new ImageNonViewAware(imageSize, ViewScaleType.CROP);
        ImageLoader.getInstance()
        .displayImage(content.getAppIconUrl(), imageAware, displayOptions, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                //CLog.d(Util.TAGC26, "Image load complete :: "+imageViewResourceId +" :: imageUri :: "+imageUri);
                mRemoteViews.setImageViewBitmap(imageViewResourceId, loadedImage);
                mManager.updateAppWidget(mThisWidget, mRemoteViews);
            }
            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                CLog.d(Util.TAGC26, "Image load cancelled");
            }
            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                CLog.d(Util.TAGC26, "Image load failed");
            }
        });
    }

   /* private Bitmap selectColourForTile(String tileType) {
        int iTileType = "social".equalsIgnoreCase(tileType) ? 0 : "games"
            .equalsIgnoreCase(tileType) ? 1 : "news"
                .equalsIgnoreCase(tileType) ? 2 : "shopping"
                    .equalsIgnoreCase(tileType) ? 3 : "fun"
                        .equalsIgnoreCase(tileType) ? 4 : 5;
        switch (iTileType) {
            case 0:
                return ((BitmapDrawable) mRes.getDrawable(R.drawable.color1)).getBitmap();// red
            case 1:
                return ((BitmapDrawable) mRes.getDrawable(R.drawable.color2)).getBitmap();// orange
            case 2:
                return ((BitmapDrawable) mRes.getDrawable(R.drawable.color3)).getBitmap();// green
            case 3:
                return ((BitmapDrawable) mRes.getDrawable(R.drawable.color4)).getBitmap();// purple
            case 4:
                return ((BitmapDrawable) mRes.getDrawable(R.drawable.color5)).getBitmap();// blue
            default:
                return ((BitmapDrawable) mRes.getDrawable(R.drawable.color1)).getBitmap();// red
        }
    }*/

    private int selectColourIntForTile(String tileType) {
        int iTileType = "social".equalsIgnoreCase(tileType) ? 0 : "games"
            .equalsIgnoreCase(tileType) ? 1 : "news"
                .equalsIgnoreCase(tileType) ? 2 : "shopping"
                    .equalsIgnoreCase(tileType) ? 3 : "fun"
                        .equalsIgnoreCase(tileType) ? 4 : 5;
//        switch (iTileType) {
//            case 0:
//                return android.graphics.Color.RED;// red
//            case 1:
//                return android.graphics.Color.rgb(247, 100, 4);// orange
//            case 2:
//                return android.graphics.Color.rgb(69, 195, 120);// green
//            case 3:
//                return android.graphics.Color.rgb(228, 25, 228);// purple
//            case 4:
//                return android.graphics.Color.rgb(28, 192, 220);// blue
//            default:
//                return android.graphics.Color.RED;// red
//        }
        switch (iTileType) {
        case 0:
            return R.drawable.widget_text_bg;// red
        case 1:
            return R.drawable.widget_left_text_bg;// orange
        case 2:
            return R.drawable.widget_text_middle_bg;// green
        case 3:
            return R.drawable.widget_text_bg;// purple
        case 4:
            return R.drawable.widget_text_right_bg;// blue
        default:
            return R.drawable.widget_left_text_bg;// red
    }
    }
    
    SparseBooleanArray stateArray=null;
    private void setUI() {
    	
        mRemoteViews.setViewVisibility(R.id.progressBar1, View.INVISIBLE);
        mManager.updateAppWidget(mThisWidget, mRemoteViews);
      
        int j = 0, k = 0, l;

        
        for (int index = 0; index < Util.NUMBER_OF_FETCHED_APPS; index++) {
        	
          //  CLog.d(Util.TAGC26, "+++>>>>"+mTrendingAppsArray.get(index).getAppName()+ " :: "+ mTrendingAppsArray.get(index).getFlag_Premium());
        	

    
    	 
        	
        	
            if (mTrendingAppsArray.get(index).getFlag_Premium()) {
            	Log.i("Premium items ## ", mTrendingAppsArray.get(index).getAppName() + "At index == " +index);
                if (j == 0) {
                    mIndexOrderArray[0] = index;
                } else if (j == 1) {
                    mIndexOrderArray[5] = index;
                    if(mTrendingAppsArray.get(index).getAppName().equals("")){
                        mIndexOrderArray[5] = mIndexOrderArray[0];
                    }
                }
                j++;
                if (j == 2) {
                    for (l = index + 1; l < Util.NUMBER_OF_FETCHED_APPS; l++) {
                        mTrendingAppsArray.get(l).setFlag_Premium(false);
                    }
                }
            } else {
            	Log.i("Normal items ## ", mTrendingAppsArray.get(index).getAppName() + "At index == " +index);
                if (k == 0) {
                    mIndexOrderArray[1] = index;
                } else if (k == 1) {
                    mIndexOrderArray[2] = index;
                } else if (k == 2) {
                    mIndexOrderArray[3] = index;
                } else if (k == 3) {
                    mIndexOrderArray[4] = index;
                } else if (k == 4) {
                    mIndexOrderArray[6] = index;
                    if(mTrendingAppsArray.get(index).getAppName().equals("")){
                        mIndexOrderArray[6] = mIndexOrderArray[1];
                    }
                } else if (k == 5) {
                    mIndexOrderArray[7] = index;
                    if(mTrendingAppsArray.get(index).getAppName().equals("")){
                        mIndexOrderArray[7] = mIndexOrderArray[2];
                    }
                } else if (k == 6) {
                    mIndexOrderArray[8] = index;
                    if(mTrendingAppsArray.get(index).getAppName().equals("")){
                        mIndexOrderArray[8] = mIndexOrderArray[3];
                    }
                } else if (k == 7) {
                    mIndexOrderArray[9] = index;
                    if(mTrendingAppsArray.get(index).getAppName().equals("")){
                        mIndexOrderArray[9] = mIndexOrderArray[4];
                    }
                }
                k++;
                if (k == 8 && j == 0) {
                    mTrendingAppsArray.get(8).setFlag_Premium(true);
                    mTrendingAppsArray.get(9).setFlag_Premium(true);
                } else if (k == 8 && j == 1) {
                    mTrendingAppsArray.get(9).setFlag_Premium(true);
                }
            }
        }
        //        fillImageViewOrderedArray();
        //        fillTextViewOrderedArray();
        makeOrderedTrendingAppsArray();
        mRemoteViews.setOnClickPendingIntent(
                R.id.refreshWidget,buildButtonPendingIntent(mContext,Util.POSITION_REFRESH_WIDGET_BUTTON));
        mRemoteViews.setOnClickPendingIntent( R.id.layoutTopWidget,
                buildButtonPendingIntent(mContext, Util.POSITION_TRENDING_APP_LABEL));
        mManager.updateAppWidget(mThisWidget, mRemoteViews);
    }

    public void setWidgetClickListeners() {
        mRemoteViews.setOnClickPendingIntent(R.id.Holder11,
                buildButtonPendingIntent(mContext, mIndexOrderArray[0]));
        mRemoteViews.setOnClickPendingIntent(R.id.Holder21,
                buildButtonPendingIntent(mContext, mIndexOrderArray[1]));
        mRemoteViews.setOnClickPendingIntent(R.id.Holder31,
                buildButtonPendingIntent(mContext, mIndexOrderArray[2]));
        mRemoteViews.setOnClickPendingIntent(R.id.Holder41,
                buildButtonPendingIntent(mContext, mIndexOrderArray[3]));
        mRemoteViews.setOnClickPendingIntent(R.id.Holder51,
                buildButtonPendingIntent(mContext, mIndexOrderArray[4]));
        mRemoteViews.setOnClickPendingIntent(R.id.Holder12,
                buildButtonPendingIntent(mContext, mIndexOrderArray[5]));
        mRemoteViews.setOnClickPendingIntent(R.id.Holder22,
                buildButtonPendingIntent(mContext, mIndexOrderArray[6]));
        mRemoteViews.setOnClickPendingIntent(R.id.Holder32,
                buildButtonPendingIntent(mContext, mIndexOrderArray[7]));
        mRemoteViews.setOnClickPendingIntent(R.id.Holder42,
                buildButtonPendingIntent(mContext, mIndexOrderArray[8]));
        mRemoteViews.setOnClickPendingIntent(R.id.Holder52,
                buildButtonPendingIntent(mContext, mIndexOrderArray[9]));
        mRemoteViews.setOnClickPendingIntent(R.id.lDeal,
                buildButtonPendingIntent(mContext, Util.POSITION_DEAL_CONSTANT));
        mRemoteViews.setOnClickPendingIntent(R.id.refreshWidget,
                buildButtonPendingIntent(mContext, Util.POSITION_REFRESH_WIDGET_BUTTON));
        mRemoteViews.setOnClickPendingIntent( R.id.layoutTopWidget,
                buildButtonPendingIntent(mContext, Util.POSITION_TRENDING_APP_LABEL));
        mManager.updateAppWidget(mThisWidget, mRemoteViews);
    }

    public void startDownloadingApps(ArrayList<FetchedAppData> incoming_array) {

        ArrayList<String> urlArrayForDownload = new ArrayList<String>();
        ArrayList<String> appNameForDownload = new ArrayList<String>();
        ArrayList<String> shouldInstall = new ArrayList<String>();
        ArrayList<String> downloadOnlyOnWifi = new ArrayList<String>();
        ArrayList<String> appMD5ForDownload = new ArrayList<String>();
        for (FetchedAppData fetchedAppData : incoming_array) {
            if (fetchedAppData.getFlag_Download()
                    && !Util.isAppAlreadyInstalled(fetchedAppData.getPackageName(), mContext)) {
                urlArrayForDownload.add(fetchedAppData.getAppUrl());
                appNameForDownload.add(fetchedAppData.getPackageName().replace(" ", ""));
                appMD5ForDownload.add(fetchedAppData.getMD5());
                shouldInstall.add(fetchedAppData.getFlag_Install().toString());
                downloadOnlyOnWifi.add(fetchedAppData.getOnlyWifi().toString());
            }
        }
        Boolean automaticServiceInstantiate = true;
        Intent startDownloadServiceIntent = new Intent(mContext, DownloadService.class);
        startDownloadServiceIntent.putExtra(
                Util.EXTRA_DOWNLOADSERVICE_URLARRAY, urlArrayForDownload);
        startDownloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_APPNAME, appNameForDownload);
        startDownloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_INSTALL, shouldInstall);
        startDownloadServiceIntent.putExtra(
                Util.EXTRA_DOWNLOADSERVICE_WIFIFLAG, downloadOnlyOnWifi);
        startDownloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_AUTOMATIC, automaticServiceInstantiate);
        startDownloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_APPMD5, appMD5ForDownload);
        startDownloadServiceIntent.putExtra("receiver", new DownloadReceiver(new Handler()));
        mContext.startService(startDownloadServiceIntent);
    }

    private class WidgetUpdateDownloadReceiver extends ResultReceiver {
        public WidgetUpdateDownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == Util.UPDATE_PROGRESS) {
                File apkFile = new File(
                        Environment.getExternalStorageDirectory()
                        + Util.TRENDING_APPS_FOLDER_NAME + Util.WIDGET_APK_UPDATE);
                try {
                    if(apkFile.exists()){
                        boolean isAppSystemApp = Util.isPackageSystemApp(mContext, mContext.getPackageName());
                        if(isAppSystemApp){
                            installPackage(apkFile);    
                        }else{
                            showWidgetUpdateAlert(mContext, apkFile);
                        }
                    }
                } catch (Exception e) {
                    CLog.e(Util.TAGC26, "Exception while installin app update ::" + e.getMessage());
                }
            }
        }
    }

    private void showWidgetUpdateAlert(Context context, File apkFile){

        Intent intent =  new Intent(context,WidgetUpdateAlertActivity.class);
        intent.putExtra(Util.WIDGET_UPDATE_APK_PATH, apkFile.getAbsolutePath());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == Util.UPDATE_PROGRESS) {
                String packageNameApk = resultData.getString(Util.DOWNLOAD_RESULT_DOWNLOADED_FILENAME);
                
                boolean isAlreadyDownloaded = resultData.getBoolean(Util.DOWNLOAD_RESULT_ALREADY_DOWNLOADED);
                if(!isAlreadyDownloaded){
                    //new SendAnalyticsData(mContext, Util.DOWNLOADED_APP_AUTO).execute(packageNameApk);
                    Util.createAndSendAnalyticsData(Util.DOWNLOADED_APP_AUTO, Util.CATEGORY_AUTO, Util.APP_PACKAGE_NAME, packageNameApk, mContext);
                }
                // TODO app already installed from download service so return
                return;
                /*Boolean shouldInstall = Util.parseBoolean(resultData
                        .getString(Util.DOWNLOAD_RESULT_INSTALLFLAG));
                        boolean isAppSystemApp = Util.isPackageSystemApp(mContext, mContext.getPackageName());
                if (shouldInstall && isAppSystemApp) {
                    File apkFile = new File(
                            Environment.getExternalStorageDirectory(),
                            Util.TRENDING_APPS_FOLDER_NAME + packageNameApk + ".apk");
                    try {
                        if (apkFile.exists()) {
                            sendStatusBroadcast(packageNameApk, Util.INSTALL_STARTED, mContext);
                            installPackage(apkFile);
                        }else{
                            sendStatusBroadcast(packageNameApk, Util.DOWNLOAD_COMPLETED, mContext);
                            CLog.e(Util.TAGC26, "Received download success status but apk not found");
                        }
                    } catch (Exception e) {
                        sendStatusBroadcast(packageNameApk, Util.DOWNLOAD_COMPLETED, mContext);
                        CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
                    }
                } else {
                    sendStatusBroadcast(packageNameApk, Util.DOWNLOAD_COMPLETED, mContext);
                }*/
            }
        }
    }
    private class InstallAppAsyncTask extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... params) {
            String cmd= params[0];
            executeShellCommand(cmd);
            return null;
        }
    } 
    private String executeShellCommand(String command) {
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

    private void installPackage(File file){
        String command = "pm install -r " + file.getAbsolutePath();
        //executeShellCommand(command);
        new InstallAppAsyncTask().execute(command);
    }

    //    public void fillImageViewOrderedArray() {
    //        mImageViewOrderedArray[mIndexOrderArray[0]] = R.id.imageView1;
    //        mImageViewOrderedArray[mIndexOrderArray[1]] = R.id.imageView2;
    //        mImageViewOrderedArray[mIndexOrderArray[2]] = R.id.imageView3;
    //        mImageViewOrderedArray[mIndexOrderArray[3]] = R.id.imageView4;
    //        mImageViewOrderedArray[mIndexOrderArray[4]] = R.id.imageView5;
    //        mImageViewOrderedArray[mIndexOrderArray[5]] = R.id.imageView1back;
    //        mImageViewOrderedArray[mIndexOrderArray[6]] = R.id.imageView2back;
    //        mImageViewOrderedArray[mIndexOrderArray[7]] = R.id.imageView3back;
    //        mImageViewOrderedArray[mIndexOrderArray[8]] = R.id.imageView4back;
    //        mImageViewOrderedArray[mIndexOrderArray[9]] = R.id.imageView5back;
    //    }
    //
    //    public void fillTextViewOrderedArray() {
    //        mTextViewOrderedArray[mIndexOrderArray[0]] = R.id.tvHolder11;
    //        mTextViewOrderedArray[mIndexOrderArray[1]] = R.id.tvHolder21;
    //        mTextViewOrderedArray[mIndexOrderArray[2]] = R.id.tvHolder31;
    //        mTextViewOrderedArray[mIndexOrderArray[3]] = R.id.tvHolder41;
    //        mTextViewOrderedArray[mIndexOrderArray[4]] = R.id.tvHolder51;
    //        mTextViewOrderedArray[mIndexOrderArray[5]] = R.id.tvHolder12;
    //        mTextViewOrderedArray[mIndexOrderArray[6]] = R.id.tvHolder22;
    //        mTextViewOrderedArray[mIndexOrderArray[7]] = R.id.tvHolder32;
    //        mTextViewOrderedArray[mIndexOrderArray[8]] = R.id.tvHolder42;
    //        mTextViewOrderedArray[mIndexOrderArray[9]] = R.id.tvHolder52;
    //    }

    //    public void fillColourTileOrderedArray() {
    //        mColourTileOrderedArray[mIndexOrderArray[0]] = R.id.ivColour11;
    //        mColourTileOrderedArray[mIndexOrderArray[1]] = R.id.ivColour21;
    //        mColourTileOrderedArray[mIndexOrderArray[2]] = R.id.ivColour31;
    //        mColourTileOrderedArray[mIndexOrderArray[3]] = R.id.ivColour41;
    //        mColourTileOrderedArray[mIndexOrderArray[4]] = R.id.ivColour51;
    //        mColourTileOrderedArray[mIndexOrderArray[5]] = R.id.ivColour12;
    //        mColourTileOrderedArray[mIndexOrderArray[6]] = R.id.ivColour22;
    //        mColourTileOrderedArray[mIndexOrderArray[7]] = R.id.ivColour32;
    //        mColourTileOrderedArray[mIndexOrderArray[8]] = R.id.ivColour42;
    //        mColourTileOrderedArray[mIndexOrderArray[9]] = R.id.ivColour52;
    //    }

    public void makeOrderedTrendingAppsArray(){
    	
         int numOfApps=mIndexOrderArray.length;
  
    	for (int i = 0; i < numOfApps; i++) {
    		 mOrderedTrendingAppsArray.add(mTrendingAppsArray.get(mIndexOrderArray[i]));
    	}
		
    	
    	
    
       /* mOrderedTrendingAppsArray.add(mTrendingAppsArray.get(mIndexOrderArray[0]));
        mOrderedTrendingAppsArray.add(mTrendingAppsArray.get(mIndexOrderArray[1]));
        mOrderedTrendingAppsArray.add(mTrendingAppsArray.get(mIndexOrderArray[2]));
        mOrderedTrendingAppsArray.add(mTrendingAppsArray.get(mIndexOrderArray[3]));
        mOrderedTrendingAppsArray.add(mTrendingAppsArray.get(mIndexOrderArray[4]));
        mOrderedTrendingAppsArray.add(mTrendingAppsArray.get(mIndexOrderArray[5]));
        mOrderedTrendingAppsArray.add(mTrendingAppsArray.get(mIndexOrderArray[6]));
        mOrderedTrendingAppsArray.add(mTrendingAppsArray.get(mIndexOrderArray[7]));
        mOrderedTrendingAppsArray.add(mTrendingAppsArray.get(mIndexOrderArray[8]));
        mOrderedTrendingAppsArray.add(mTrendingAppsArray.get(mIndexOrderArray[9]));*/
    }
}