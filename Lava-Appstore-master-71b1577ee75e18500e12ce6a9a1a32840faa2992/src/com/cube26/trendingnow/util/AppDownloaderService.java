package com.cube26.trendingnow.util;

import java.util.ArrayList;

import android.content.Context;

import com.cube26.trendingapps.webservices.ContentFetcherTask;
import com.cube26.trendingapps.webservices.ContentFetcherTask.ContentFetcherCallback;
import com.cube26.trendingapps.webservices.FetchedAppData;

public class AppDownloaderService {

    private Context mContext;

    public AppDownloaderService(Context context) {
        this.mContext = context;
    }
    
    public void checkForAppDownload(){
        
        try {
            long firstInstallTime = mContext.getPackageManager()
            .getPackageInfo(mContext.getPackageName(), 0).firstInstallTime;
            
            CLog.d(Util.TAGC26, "First Install time :: "+ firstInstallTime);
            
            long lastUpdateTime = mContext.getPackageManager()
                    .getPackageInfo(mContext.getPackageName(), 0).lastUpdateTime;
            CLog.d(Util.TAGC26, "Last Update time :: "+ lastUpdateTime);
            
            // If update has been installed then check if update has been installed
            // If update has been installed then open the app
            if(firstInstallTime!=lastUpdateTime){
                String appVersionInSharedPrefs = Util.getAppVersionFromSharedPref(mContext);
                String appVersionInManifest =  Util.getAppVersion(mContext);
                
                if(!appVersionInManifest.equalsIgnoreCase(appVersionInSharedPrefs)){
                    Util.openTrendingApp(mContext);
                    Util.setAppVersionInSharedPref(mContext, appVersionInManifest);
                }
            }
        } catch (Exception e) {
            CLog.d(Util.TAGC26, "Error in finding install and update time :: "+ e.getMessage());
            e.printStackTrace();
        }
        
        ContentFetcherTask mFetcher = new ContentFetcherTask();
        mFetcher.setContext(mContext);
        mFetcher.setContentFetchCallback(new ContentFetcherCallback() {
            
            @Override
            public void onDataFetched(ArrayList<FetchedAppData> fetchedAppData, ArrayList<FetchedAppData> fetchedBannerAppsData) {
                // TODO Auto-generated method stub
                // Don't do nothing here, we need not to use the result given by the server in case of data fetched from here..
            }
        });
        mFetcher.execute(Util.WIDGET_UPDATE_AUTOMATIC);
    }
    
}
