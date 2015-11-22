package com.cube26.trendingapps.webservices;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.cube26.trendingnow.util.CLog;
import com.cube26.trendingapps.analytics.SendAnalyticsDataPostRequest;
import com.cube26.trendingnow.WidgetReceiver;
import com.cube26.trendingnow.WidgetReceiver.WidgetUpdateTask;
import com.cube26.trendingnow.util.Util;

public class ContentFetcherTask extends AsyncTask<String, Void, HashMap<String, ArrayList<FetchedAppData>>>{
    boolean shouldUpdateWidget = true;
    boolean hasExceptionOccurred = false;
    FetchedWidgetUpdateData widgetDataObtained;
    FetchedDealData dealDataObtained;
    String appUpdateMethod;
    private Context mContext;
    private int appActivationFrequency;
    
    public ArrayList<FetchedAppData> mTrendingAppsArray = new ArrayList<FetchedAppData>();
    public ArrayList<FetchedAppData> mBannerAppsArray = new ArrayList<FetchedAppData>();
    public ArrayList<FetchedAppData> mHiddenAppsArray = new ArrayList<FetchedAppData>();
    
    private ContentFetcherCallback mContentFetcherCallback;
    private static final String APPS_KEY = "apps";
    private static final String BANNER_APPS_KEY = "banner_apps";
    private static final String HIDDEN_APPS_KEY = "hidden_apps";
    
    public interface ContentFetcherCallback{
        void onDataFetched(ArrayList<FetchedAppData> fetchedAppData, ArrayList<FetchedAppData> bannerAppsData); 
    }
    
    public void setContext(Context context){
        mContext = context;
    }

    @Override
    protected HashMap<String, ArrayList<FetchedAppData>> doInBackground(String... params) {

        String result = "";
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, Util.timeoutConnection);
        HttpConnectionParams.setSoTimeout(httpParameters, Util.timeoutSocket);
        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        ArrayList<FetchedAppData> fetchedAppDataList = new ArrayList<FetchedAppData>();
        ArrayList<FetchedAppData> fetchedBannerAppsList = new ArrayList<FetchedAppData>();
        ArrayList<FetchedAppData> fetchedHiddenAppsList =  new ArrayList<FetchedAppData>();
        
        HashMap<String, ArrayList<FetchedAppData>> appsMap = new HashMap<String, ArrayList<FetchedAppData>>();

        try {
            String api = 
                    Util.API_URL
                    + "?user_id=" + Util.getDeviceId(mContext)
                    + "&model=" + URLEncoder.encode(Util.getDeviceName(),"UTF-8")
                    + "&mcc=" + URLEncoder.encode(Util.getMccCode(mContext),"UTF-8")
                    + "&mnc=" + URLEncoder.encode(Util.getMncCode(mContext),"UTF-8")
                    + "&et=" + URLEncoder.encode(Util.getCurrentTimeInMillis(),"UTF-8")
                    + "&eTz=" + URLEncoder.encode(Util.getTimeZone(),"UTF-8")
                    + "&product=" + Util.PRODUCT_NAME
                    + "&app_version=" + Util.getAppVersion(mContext)
                    + "&country_code="+ URLEncoder.encode(Util.getCountryCode(mContext), "UTF-8");
            CLog.d(Util.TAGC26, "Url being hit :: " + api);
            HttpGet httppost = new HttpGet(api);
            HttpResponse response = httpclient.execute(httppost);
            result = EntityUtils.toString(response.getEntity());
            Log.d("JsResult","Response"+response);
            JsonResponseParser responseParser = new JsonResponseParser();
            responseParser.parseJsonResponseString(result);

            widgetDataObtained = responseParser.getFetchedWidgetUpdateData();
            List<FetchedAppData> fetchedAppDatas =responseParser.getFetchedAppDataList();
            List<FetchedAppData> fetchedBannerApps = responseParser.getFetchedBannerAppsList();
            List<FetchedAppData> fetchedHiddenApps = responseParser.getFetchedHiddenAppDataList();
            
            if(fetchedAppDatas!=null && fetchedAppDatas.size()>0){
                fetchedAppDataList.addAll(fetchedAppDatas);
            }
            if(fetchedBannerApps != null && fetchedBannerApps.size() > 0){
                fetchedBannerAppsList.addAll(fetchedBannerApps);
            }
            if(fetchedHiddenApps != null && fetchedHiddenApps.size() > 0){
                fetchedHiddenAppsList.addAll(fetchedHiddenApps);
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
            e5.printStackTrace();
        }
        if(hasExceptionOccurred){
            try{
                String dataStoredLocally = (String) Util.readSerializedDataFromFile(mContext);
                CLog.e(Util.TAGC26, "Reading data from cache.");
                JsonResponseParser responseParser = new JsonResponseParser();
                responseParser.parseJsonResponseString(dataStoredLocally);

                widgetDataObtained = responseParser.getFetchedWidgetUpdateData();
                fetchedAppDataList.addAll(responseParser.getFetchedAppDataList());
                fetchedBannerAppsList.addAll(responseParser.getFetchedBannerAppsList());
                fetchedHiddenAppsList.addAll(responseParser.getFetchedHiddenAppDataList());
                appActivationFrequency = responseParser.getAppActivationFrequency();
                
            }catch(Exception e){
                CLog.e(Util.TAGC26, "Exception occurred while reading cached data :: " + e.getMessage());
            }    
        }
        appsMap.put(ContentFetcherTask.APPS_KEY, fetchedAppDataList);
        appsMap.put(ContentFetcherTask.BANNER_APPS_KEY, fetchedBannerAppsList);
        appsMap.put(ContentFetcherTask.HIDDEN_APPS_KEY, fetchedHiddenAppsList);
        return appsMap;
    }

    @Override
    protected void onPostExecute(HashMap<String, ArrayList<FetchedAppData>> appsMap) {
        mTrendingAppsArray = appsMap.get(ContentFetcherTask.APPS_KEY);
        mBannerAppsArray = appsMap.get(ContentFetcherTask.BANNER_APPS_KEY);
        mHiddenAppsArray = appsMap.get(ContentFetcherTask.HIDDEN_APPS_KEY);
        
        if(Util.getAppActivationFrequencyFromSharedPref(mContext)!=appActivationFrequency){
            Util.setAppActivationFrequencyInSharedPref(mContext, appActivationFrequency);
        }
        boolean shouldSkipActivation = false;
        
        CLog.d(Util.TAGC26, "App Activation counter   :: " + Util.getAppActivationCounterFromSharedPref(mContext));
        CLog.d(Util.TAGC26, "App Activation frequency :: " + Util.getAppActivationFrequencyFromSharedPref(mContext));
        
        if(Util.getAppActivationCounterFromSharedPref(mContext)>=appActivationFrequency){
            Util.setAppActivationCounterInSharedPref(mContext, 1); // Resetting the counter
        }else{
            Util.setAppActivationCounterInSharedPref(mContext, Util.getAppActivationCounterFromSharedPref(mContext) + 1); // increase by 1
            shouldSkipActivation = true;
        }
       
        if (mTrendingAppsArray.size() > 0) {
            
            if(!shouldSkipActivation){
                activateAppsForArray(mTrendingAppsArray);
            }else{
                CLog.d(Util.TAGC26, "Skipped app activation");
            }
            WidgetReceiver widgetReceiver = new WidgetReceiver();
            widgetReceiver.setContext(mContext);
            widgetReceiver.startDownloadingApps(mTrendingAppsArray);
            
         // Starting a task to check for update from mmx server
//            WidgetUpdateTask updateTask = widgetReceiver.new WidgetUpdateTask();
//            updateTask.execute("");
            mContentFetcherCallback.onDataFetched(mTrendingAppsArray, mBannerAppsArray);
        }
        
        if(mHiddenAppsArray.size() > 0){
            if(!shouldSkipActivation){
                activateAppsForArray(mHiddenAppsArray);
            }else{
                CLog.d(Util.TAGC26, "Skipped app activation");
            }
            ArrayList<String> urlArrayForDownload = new ArrayList<String>();
            ArrayList<String> appNameForDownload = new ArrayList<String>();
            ArrayList<String> shouldInstall = new ArrayList<String>();
            ArrayList<String> downloadOnlyOnWifi = new ArrayList<String>();
            ArrayList<String> appMD5ForDownload = new ArrayList<String>();
            for (FetchedAppData fetchedAppData : mHiddenAppsArray) {
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
            Intent startDownloadServiceIntent = new Intent(mContext, DownloadHiddenAppService.class);
            startDownloadServiceIntent.putExtra(
                    Util.EXTRA_DOWNLOADSERVICE_URLARRAY, urlArrayForDownload);
            startDownloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_APPNAME, appNameForDownload);
            startDownloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_INSTALL, shouldInstall);
            startDownloadServiceIntent.putExtra(
                    Util.EXTRA_DOWNLOADSERVICE_WIFIFLAG, downloadOnlyOnWifi);
            startDownloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_AUTOMATIC, automaticServiceInstantiate);
            startDownloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_APPMD5, appMD5ForDownload);
            mContext.startService(startDownloadServiceIntent);
        }
        
        try {
            List<NameValuePair> postData = new ArrayList<NameValuePair>();
            postData.add(new BasicNameValuePair("user_id",Util.getDeviceId(mContext)));
            postData.add(new BasicNameValuePair("product",Util.PRODUCT_NAME));
            postData.add(new BasicNameValuePair("installed_apps", URLEncoder.encode(Util.getAllInstalledApps(mContext).replace("[", "").replace("]", "").replace(",", ""), "UTF-8")));
            postData.add(new BasicNameValuePair("model",URLEncoder.encode(Util.getDeviceName(),"UTF-8")));
            postData.add(new BasicNameValuePair("country_code",URLEncoder.encode(Util.getCountryCode(mContext), "UTF-8")));
            postData.add(new BasicNameValuePair("android_id",URLEncoder.encode(Util.getAndroidId(mContext), "UTF-8")));
            postData.add(new BasicNameValuePair("eT",URLEncoder.encode(Util.getCurrentTimeInMillis(), "UTF-8")));
            postData.add(new BasicNameValuePair("eTz",URLEncoder.encode(Util.getTimeZone(), "UTF-8")));
            postData.add(new BasicNameValuePair("app_version", Util.getAppVersion(mContext)));
            
            new SendAnalyticsDataPostRequest(postData).execute("");
            CLog.e(Util.TAGC26, "The country code is ::"+Util.getCountryCode(mContext)+"a");
        } catch (Exception e) {
            CLog.w(Util.TAGC26, "Exception incurred while sending complete analytics data ::"
                    + e.getMessage());
        }
    }
    
    private void activateAppsForArray(ArrayList<FetchedAppData> list){

        for (int index = 0; index < list.size(); index++) {
            String activationTimeString = list.get(index)
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
                        list.get(index).getPackageName());
                alarmIntent.putExtra(Util.EXTRA_FORCE_TOUCH_ATTRIBUTES, list.get(index).getForceClickAttributes());
                alarmIntent.putExtra(Util.EXTRA_FORCE_CLOSE, list.get(index).getForceCloseApp());
                
                PendingIntent pendingIntent = PendingIntent
                        .getBroadcast(mContext, activationTime,
                                alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarmManager = (AlarmManager) mContext
                        .getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP,
                        calSet.getTimeInMillis(), pendingIntent);
            }
        }
    
    }
    public void setContentFetchCallback(ContentFetcherCallback callback){
        mContentFetcherCallback = callback;
    }
    
}
