package com.cube26.trendingnow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.cube26.dataassist.general.CL_GenUtil;
import com.cube26.celkonstore.R;
import com.cube26.notificationactivationlib.NotificationActivationReceiver;
import com.cube26.trendingapps.receivers.AppDownloadReceiver;
import com.cube26.trendingapps.ui.CirclePageIndicator;
import com.cube26.trendingapps.ui.CircularProgressBar;
import com.cube26.trendingapps.ui.DepthPageTransformer;
import com.cube26.trendingapps.webservices.ContentFetcherTask;
import com.cube26.trendingapps.webservices.ContentFetcherTask.ContentFetcherCallback;
import com.cube26.trendingapps.webservices.FetchedAppData;
import com.cube26.trendingnow.util.CLog;
import com.cube26.trendingnow.util.UpdateReceiver;
import com.cube26.trendingnow.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class TrendingAppActivity extends FragmentActivity {

	int index = 0;
	
    private ViewPager mTopAppsPager;
    private TopAppsPagerAdapter mTopAppsPagerAdapter;
    private CirclePageIndicator mTopAppsPagedIndicator;
    private static final int TOP_APPS_CACHE_LIMIT = 10;
    private Context mContext;
    private ContentFetcherTask mFetcher;
    public ArrayList<FetchedAppData> mTrendingAppsArray = new ArrayList<FetchedAppData>();
    public ArrayList<FetchedAppData> mOrderedTrendingAppsArray = new ArrayList<FetchedAppData>();
    public static final Object mLock = new Object();
    private ImageView mLoadingImage;
    private ListView mNormalAppsList;
    private ArrayList<FetchedAppData> mFetchedAppData;
    private ArrayList<FetchedAppData> mFetchedBannerAppDatas;

    String SENDER_ID = "726662682762";
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    String regid;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    
    String TAG = Util.TAGC26;
    private static final String TAG_DB="cube26"; 
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting Trending Activity");
        setContentView(R.layout.activity_trending_app);
        boolean success;
        int result = Settings.Secure.getInt(getContentResolver(),
        Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
        if (result == 0) {
            success = Settings.Secure.putString(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, "1");    
        }
        
       
        
        mContext = TrendingAppActivity.this;
        mLoadingImage = (ImageView) findViewById(R.id.loading_indicator);
        mNormalAppsList = (ListView) findViewById(R.id.normal_apps_list);
        
        // Initialise logging library
        CLog.init();
        
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext()).build();
        ImageLoader.getInstance().init(config);

        //sets alarm manager to check for apps on server every three days
        AppDownloadReceiver.setAlarm(mContext);

       // starting app update library
        try{
            UpdateReceiver.setAlarm(this);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        
        //starting data assist, right now it won't send anything
        CL_GenUtil.runDC(this);
        
      //  start app activation and notification
        try{
            NotificationActivationReceiver.setAlarmForNotificationAndActivation(this);
        }catch(Exception e){
            CLog.e(Util.TAGC26, e.getMessage());
        }
     
        // Start app download service
        try{
            AppDownloadReceiver.setAlarm(this);
        }catch(Exception e){
            CLog.e(Util.TAGC26, e.getMessage());
        }
        
     // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(this);

            if (regid.isEmpty()) {
                registerInBackground();
            }else{
                sendRegistrationIdToBackend(regid);
            }
        } else {
            CLog.i(TAG, "No valid Google Play Services APK found.");
        }
        
        String appVersionInSharedPrefs = Util.getAppVersionFromSharedPref(mContext);
        String appVersionInManifest =  Util.getAppVersion(mContext);
        
        if(!appVersionInManifest.equalsIgnoreCase(appVersionInSharedPrefs)){
            Util.setAppVersionInSharedPref(mContext, appVersionInManifest);
        }
        
        mFetcher = new ContentFetcherTask();
        mFetcher.setContext(mContext);
        mFetcher.setContentFetchCallback(new ContentFetcherCallback() {

            @Override
            public void onDataFetched(ArrayList<FetchedAppData> fetchedAppData, ArrayList<FetchedAppData> fetchedBannerAppsData) {
                mLoadingImage.setVisibility(View.GONE);
                mFetchedAppData = fetchedAppData;
                mFetchedBannerAppDatas = fetchedBannerAppsData;
                setPagers(fetchedAppData,fetchedBannerAppsData);
            }
        });
        mFetcher.execute(Util.WIDGET_UPDATE_AUTOMATIC);
    }

   
   
    @Override
    protected void onResume() {
        setPagers(mFetchedAppData, mFetchedBannerAppDatas);
        super.onResume();
     // Check device for Play Services APK.
        checkPlayServices();
        
       
        if(mNormalAppsList != null){
            if(mNormalAppsList.getCount() > index)
            	mNormalAppsList.setSelectionFromTop(index, 0);
            else
            	mNormalAppsList.setSelectionFromTop(0, 0);
        }
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	index = mNormalAppsList.getFirstVisiblePosition();
    }

    private void setPagers(ArrayList<FetchedAppData> fetchedAppData, ArrayList<FetchedAppData> fetchedBannerAppsData){
        FrameLayout topBannerAppsLayout = (FrameLayout) findViewById(R.id.topBannerAppsLayout);
        if(fetchedBannerAppsData != null && fetchedBannerAppsData.size() > 0){
            mTopAppsPager = (ViewPager) findViewById(R.id.top_apps_pager);
            mTopAppsPagedIndicator = (CirclePageIndicator) findViewById(R.id.top_apps_indicator);
            mTopAppsPager.setPageTransformer(true, new DepthPageTransformer());
            mTopAppsPagerAdapter = new TopAppsPagerAdapter(getSupportFragmentManager());
            mTopAppsPagerAdapter.setBannerApps(fetchedBannerAppsData);
            mTopAppsPager.setAdapter(mTopAppsPagerAdapter);
            mTopAppsPagedIndicator.setViewPager(mTopAppsPager);
            mTopAppsPager.setOffscreenPageLimit(TOP_APPS_CACHE_LIMIT);
            topBannerAppsLayout.setVisibility(View.VISIBLE);
        }else{
            topBannerAppsLayout.setVisibility(View.GONE);
        }

        if(fetchedAppData != null && fetchedAppData.size() > 0){
            mNormalAppsList.setVisibility(View.VISIBLE);
            AppsAdapter adapter = new AppsAdapter(mContext, fetchedAppData);
            mNormalAppsList.setAdapter(adapter);
        }
    }

    private class TopAppsPagerAdapter extends FragmentPagerAdapter {
        private int mAppsCount;
        private ArrayList<FetchedAppData> bannerAppsData = new ArrayList<FetchedAppData>();
        public void setBannerApps(ArrayList<FetchedAppData> appDatas){
            bannerAppsData = appDatas;
            mAppsCount = bannerAppsData.size();
            if(mAppsCount <= 1){
                mTopAppsPagedIndicator.setVisibility(View.GONE);
            }
        }
        public TopAppsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putSerializable(TopAppsFragment.BANNER_APP, bannerAppsData.get(position));
            TopAppsFragment fragment = new TopAppsFragment(mContext);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return mAppsCount;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this)
        .registerReceiver( appDownloadStatusReceiver,
                new IntentFilter(Util.DOWNLOAD_RECEIVER_INTENT_NAME));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this)
        .unregisterReceiver((appDownloadStatusReceiver));
    }

    private BroadcastReceiver appDownloadStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS, 0);
            float total = intent.getFloatExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS_TOTAL, Util.DOWNLOAD_PROGRESS_TOTAL_DEFAULT);
            float file_size = intent.getFloatExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS_FILESIZE, Util.DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT);
            String extraPackageName = intent.getStringExtra(Util.EXTRA_BROADCAST_PACKAGE);
            int progress = (int) ((total * 100) / file_size);
            setUIState(status, extraPackageName, progress);
            ArrayList<String> appsInQueue = new ArrayList<String>();
            appsInQueue.addAll(intent.getStringArrayListExtra(Util.EXTRA_BROADCAST_APPS_IN_QUEUE)!=null?intent.getStringArrayListExtra(Util.EXTRA_BROADCAST_APPS_IN_QUEUE): new ArrayList<String>());
            if(appsInQueue != null && appsInQueue.size() > 0){
                for(String appName : appsInQueue){
                    setUIState(Util.DOWNLOAD_STARTED, appName, 0);
                }
            }
        }
    };
    private void setUIState(int statusUI, String appPackage, int progress){

        int positionOfAppInListView = Util.getAppPositionFromPackage(mFetchedAppData, appPackage);
        if(positionOfAppInListView != -1){
            View item = Util.getViewByPosition(positionOfAppInListView, mNormalAppsList);
            CircularProgressBar starting_download_bar = (CircularProgressBar) item.findViewById(R.id.app_download_progressBar);
            ImageView actionButton = (ImageView) item.findViewById(R.id.app_action_button);

            switch (statusUI) {
                case Util.DOWNLOAD_STARTED:
                    CLog.wtf(Util.TAGC26, " download started but waiting for it in featured fragment ");
                    actionButton.setVisibility(View.GONE);
                    starting_download_bar.setProgress(0);
                    starting_download_bar.setVisibility(View.VISIBLE);
                    starting_download_bar.invalidate();
                    break;
                case Util.DOWNLOAD_COMPLETED:
                    actionButton.setVisibility(View.VISIBLE);
                    //                    actionButton.setText("Install");
                    actionButton.setImageResource(R.drawable.ic_open);
                    starting_download_bar.setProgress(0);
                    starting_download_bar.setVisibility(View.GONE);
                    break;
                case Util.DOWNLOAD_FAILED:
                    //                Toast.makeText(this, "Download Failed", Toast.LENGTH_LONG).show();
                    actionButton.setVisibility(View.VISIBLE);
                    //                    actionButton.setText("Download");
                    actionButton.setImageResource(R.drawable.ic_download);
                    starting_download_bar.setProgress(0);
                    starting_download_bar.setVisibility(View.GONE);
                    break;
                case Util.DOWNLOAD_CANCELED:
                    //                Toast.makeText(this, "Download Canceled", Toast.LENGTH_LONG).show();
                    actionButton.setVisibility(View.VISIBLE);
                    //                    actionButton.setText("Download");
                    actionButton.setImageResource(R.drawable.ic_download);
                    starting_download_bar.setProgress(0);
                    starting_download_bar.setVisibility(View.GONE);
                    break;
                case Util.INSTALL_STARTED:
                    actionButton.setVisibility(View.INVISIBLE);
                    //actionButton.setText("Installing..");
                    starting_download_bar.setProgress(0);
                    starting_download_bar.setVisibility(View.GONE);
                    break;
                case Util.INSTALL_COMPLETED:
                    actionButton.setVisibility(View.VISIBLE);
                    //                    actionButton.setText("Open");
                    //                    actionButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.open_grey, 0, 0);
                    actionButton.setImageResource(R.drawable.ic_open);
                    starting_download_bar.setProgress(0);
                    starting_download_bar.setVisibility(View.GONE);
                    break;
                case Util.DOWNLOAD_PROGRESS:
                    //CLog.wtf(Util.TAGC26, "download progress :: " + progress);
                    actionButton.setVisibility(View.GONE);
                    starting_download_bar.setVisibility(View.VISIBLE);
                    starting_download_bar.setProgress(progress);
                    starting_download_bar.invalidate();
                    break;
                default:
                    CLog.e(Util.TAGC26, "Error. Unexpected value received");
            }
        }else{
            //            CLog.wtf(Util.TAGC26, "app packaage :: " + appPackage);
        }
    }
    
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                CLog.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        CLog.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            CLog.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            CLog.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    CLog.i(Util.TAGC26, "Inside register in back ");
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(TrendingAppActivity.this);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    CLog.i(Util.TAGC26, "GCM Reg Id :: "+ msg);
                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend(regid);

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(TrendingAppActivity.this, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    CLog.i(Util.TAGC26,"Error in GCM register ::"+ msg);
                }
                return msg;
            }

        }.execute(null, null, null);
    }
    
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        return getSharedPreferences("GCMPrefs",
                Context.MODE_PRIVATE);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend(final String regid) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                Context ctx = TrendingAppActivity.this;
                try {
                    List<NameValuePair> postData = new ArrayList<NameValuePair>();
                    postData.add(new BasicNameValuePair("user_id",Util.getDeviceId(ctx)));
                    postData.add(new BasicNameValuePair("reg_id", regid));
                    postData.add(new BasicNameValuePair("model",Util.getDeviceName()));
                    postData.add(new BasicNameValuePair("product",Util.PRODUCT_NAME));
                    postData.add(new BasicNameValuePair("mcc",Util.getMccCode(ctx)));
                    postData.add(new BasicNameValuePair("mnc", Util.getMncCode(ctx)));
                    postData.add(new BasicNameValuePair("country_code",Util.getCountryCode(ctx)));
                    postData.add(new BasicNameValuePair("android_id",Util.getAndroidId(ctx)));
                    postData.add(new BasicNameValuePair("app_version", Util.getAppVersion(ctx)));
                    
                    String URL = Util.API_GCM_URL + "registrations" ;
                    //String URL = "http://106.187.47.119/registrations" ;

                    HttpParams httpParameters = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParameters,
                            Util.timeoutConnection);
                    HttpConnectionParams.setSoTimeout(httpParameters,
                            Util.timeoutSocket);
                    HttpClient httpclient = new DefaultHttpClient(httpParameters);
                    HttpPost httppost = new HttpPost(URL);
                    httppost.setEntity(new UrlEncodedFormEntity(postData));
                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    
                    httpclient.execute(httppost, responseHandler);
                    
                } catch (Exception ex) {
                    CLog.d(Util.TAGC26, "Exception in sending gcm reg id:: "+ ex.getMessage());
                }
                return msg;
            }

        }.execute(null, null, null);
    }
}
