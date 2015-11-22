/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.trendingnow;

import java.io.File;
import java.util.ArrayList;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;
import com.cube26.celkonstore.R;
import com.cube26.trendingnow.util.CLog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.cube26.trendingapps.analytics.SendAnalyticsData;
import com.cube26.trendingapps.webservices.DownloadService;
import com.cube26.trendingnow.util.Util;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

public class FullAppDescription extends YouTubeFailureRecoveryActivity implements YouTubePlayer.OnFullscreenListener{

    private TextView mProgressBytes;
    private TextView mProgressPercent;
    private RelativeLayout mProgressBarLayout;
    private ImageView mProgressCancel;
    private ImageView mProgressCancelForWaitingApps;

    TextView mAppName, mAppDev;
    WebView mAppFullDescription;
    TextView tvDescriptionHead;
    ImageView mImageView, mImageView2, mImageView3 , mImageAppIcon;
    ProgressBar mProgressBar;
    String mExtraAppName, mExtraMD5, mExtraAppDev, mExtraAppFullText;
    String mExtraAppUrl, mFlagInstall, mTileType ,mAppIconUrl;
    String mExtraAppImage1, mExtraAppImage2, mExtraAppImage3;
    private String mVideoUrl;
    RatingBar mAppRating;
    Float mExtraAppRating;
    Button mActionButton;
    ImageView mShowMorebtn;
    ImageView mActionButtonImage;
    String mPackageName = "";
    Boolean mDownloadFlag = false;
    private LinearLayout descriptionWrapperLayout;
    ArrayList<String> appsInQueue = new ArrayList<String>();
    String fullAppDescText, shortAppDescText;
    private long mAppSize;

    String[] screenshotImageUrls;
    LinearLayout screenshotContainer;
    boolean isShowingFullText = false;
    DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
    .resetViewBeforeLoading(true)  // default
    .delayBeforeLoading(10)
    .cacheInMemory(false) // default
    .cacheOnDisk(true) // default
    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
    .bitmapConfig(Bitmap.Config.RGB_565) // default
    .displayer(new SimpleBitmapDisplayer()) // default
    .handler(new Handler()) // default
    .build();
    private boolean mFullscreen;
    private YouTubePlayer mPlayer;

    private BroadcastReceiver appDownloadStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS, 0);
            float total = intent.getFloatExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS_TOTAL, Util.DOWNLOAD_PROGRESS_TOTAL_DEFAULT);
            float file_size = intent.getFloatExtra(Util.EXTRA_BROADCAST_DOWNLOADPROGRESS_FILESIZE, Util.DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT);

            String extraPackageName = intent.getStringExtra(Util.EXTRA_BROADCAST_PACKAGE);
            appsInQueue = intent.getStringArrayListExtra(Util.EXTRA_BROADCAST_APPS_IN_QUEUE);

            if (mPackageName != null && appsInQueue != null) {
                if ( appsInQueue.contains(mPackageName) ) {
                    setUIState(Util.DOWNLOAD_STARTED, Util.DOWNLOAD_PROGRESS_TOTAL_DEFAULT, Util.DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT);
                    return;
                }
            }
            if (mPackageName == null) {
                return;
            }
            if (!mPackageName.equalsIgnoreCase(extraPackageName)) {
                CLog.w(Util.TAGC26, "unexpected package name" + extraPackageName);
                return;
            }

            setUIState(status, total, file_size);
            if(status == Util.DOWNLOAD_COMPLETED){
            }else if (status == Util.DOWNLOAD_FAILED){
                Toast.makeText(context, "Download Failed", Toast.LENGTH_LONG).show();
            }else if(status == Util.DOWNLOAD_CANCELED){
                //                Toast.makeText(context, "Download Cancelled", Toast.LENGTH_LONG).show();
            }
            //CLog.d(Util.TAGC26, "Value of download progress received:"+ status);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext())
        .registerReceiver( appDownloadStatusReceiver,
                new IntentFilter(Util.DOWNLOAD_RECEIVER_INTENT_NAME));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext())
        .unregisterReceiver((appDownloadStatusReceiver));
    }


    private void setUIState(int statusUI, float total, float file_size){

        switch (statusUI) {
            case Util.DOWNLOAD_STARTED:
                mActionButton.setText("Starting..");
                mActionButton.setVisibility(View.VISIBLE);
                mActionButtonImage.setVisibility(View.VISIBLE);
                mProgressBarLayout.setVisibility(View.INVISIBLE);
                mProgressCancelForWaitingApps.setVisibility(View.VISIBLE);
                mActionButton.setOnClickListener(null);
                mActionButtonImage.setOnClickListener(null);
                break;
            case Util.DOWNLOAD_COMPLETED:
                mActionButton.setText("Install");
                mActionButton.setVisibility(View.VISIBLE);
                mActionButtonImage.setVisibility(View.VISIBLE);
                mProgressBarLayout.setVisibility(View.INVISIBLE);
                mProgressCancelForWaitingApps.setVisibility(View.GONE);
                mActionButton.setOnClickListener(startInstallerOnClickListener);
                mActionButtonImage.setOnClickListener(startInstallerOnClickListener);
                break;
            case Util.DOWNLOAD_FAILED:
                mActionButton.setText("Download  " + "(" + Util.getSizeFromBytes(mAppSize) + ")");
                mActionButton.setVisibility(View.VISIBLE);
                mActionButtonImage.setVisibility(View.VISIBLE);
                mProgressBarLayout.setVisibility(View.INVISIBLE);
                mProgressCancelForWaitingApps.setVisibility(View.GONE);
                mActionButton.setOnClickListener(startInstallerOnClickListener);
                mActionButtonImage.setOnClickListener(startInstallerOnClickListener);
                mActionButtonImage.setImageResource(R.drawable.install);
                break;
            case Util.DOWNLOAD_CANCELED:
                mActionButton.setText("Download" + "(" +Util.getSizeFromBytes(mAppSize) + ")");
                mActionButton.setVisibility(View.VISIBLE);
                mActionButtonImage.setVisibility(View.VISIBLE);
                mProgressBarLayout.setVisibility(View.INVISIBLE);
                mProgressCancelForWaitingApps.setVisibility(View.GONE);
                mActionButton.setOnClickListener(startInstallerOnClickListener);
                mActionButtonImage.setOnClickListener(startInstallerOnClickListener);
                mActionButtonImage.setImageResource(R.drawable.install);
                break;
            case Util.INSTALL_STARTED:
                mActionButton.setText("Installing");
                mActionButton.setVisibility(View.VISIBLE);
                mActionButtonImage.setVisibility(View.VISIBLE);
                mProgressBarLayout.setVisibility(View.INVISIBLE);
                mProgressCancelForWaitingApps.setVisibility(View.GONE);
                mActionButton.setOnClickListener(null);
                mActionButtonImage.setOnClickListener(null);
                break;
            case Util.INSTALL_COMPLETED:
                mActionButton.setText("Open");
                mActionButtonImage.setVisibility(View.VISIBLE);
                mActionButton.setVisibility(View.VISIBLE);
                mActionButtonImage.setImageResource(R.drawable.open);
                mProgressBarLayout.setVisibility(View.INVISIBLE);
                mProgressCancelForWaitingApps.setVisibility(View.GONE);
                mActionButton.setOnClickListener(openAppOnClickListener);
                mActionButtonImage.setOnClickListener(openAppOnClickListener);
                break;
            case Util.DOWNLOAD_PROGRESS:
                int progress = (int) ((total * 100) / file_size);
                if(!(progress == Util.DOWNLOAD_PROGRESS_DEFAULT)){
                    mProgressBarLayout.setVisibility(View.VISIBLE);
                    mActionButton.setVisibility(View.INVISIBLE);
                    mActionButtonImage.setVisibility(View.INVISIBLE);
                    mProgressCancelForWaitingApps.setVisibility(View.GONE);
                    mProgressPercent.setText("" + progress + "%");
                    mProgressBytes.setText("" + String.format("%.2f", total) + "/" + String.format("%.2f", file_size) + " MB");
                    mProgressBar.setProgress(progress);
                }else{
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mProgressBarLayout.setVisibility(View.INVISIBLE);
                    mProgressCancelForWaitingApps.setVisibility(View.VISIBLE);
                }
                mActionButton.setOnClickListener(null);
                mActionButtonImage.setOnClickListener(null);
                break;
            default:
                Toast.makeText(getApplicationContext(),
                        "Error. Unexpected value received", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.description_page);

        ActionBar actionBar = getActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("");
        actionBar.setCustomView(R.layout.action_bar_view);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        LinearLayout descriptionTopHeader = (LinearLayout) actionBar.getCustomView().findViewById(R.id.descriptionTopHeader);
        descriptionTopHeader.setOnClickListener(appLabelOnClickListner);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext()).build();
        ImageLoader.getInstance().init(config);

        initializeViewElements();

        if (getIntent() == null) {
            CLog.w(Util.TAGC26, "intent is null");
            return;
        }
        Bundle bundle     = getIntent().getExtras();
        setThePageUI(bundle);
        mProgressCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadService.removeAppFromQueue(mPackageName); 
            }
        });

        mProgressCancelForWaitingApps.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                DownloadService.removeAppFromQueue(mPackageName);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.full_app_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_share:
                openShare();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private OnClickListener openAppOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Util.openAppForPackageName(FullAppDescription.this, mPackageName);
        }
    };

    private OnClickListener appLabelOnClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Util.openAppLabelUrlInBrowser(FullAppDescription.this);
        }
    };
    private void openShare(){
        CLog.e(Util.TAGC26, "Share button clicked for app ::"+mExtraAppName);
        String apkUrl = mExtraAppUrl;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this amazing app!!" );
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey buddy!! I am using " + mExtraAppName
                + ". Go ahead and try it out over here : "
                +apkUrl);
        try {
            CLog.d(Util.TAGC26, "Starting activity, intent called:: "+ shareIntent);
            startActivity(Intent.createChooser(shareIntent, "Share download url via..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(FullAppDescription.this,
                    "There is no app installed for sharing purpose.",
                    Toast.LENGTH_SHORT).show();
            CLog.e(Util.TAGC26,
                    "ActivityNotFoundException ::" + ex.getMessage());
        }
    }

    private OnClickListener startInstallerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            String apkName = Environment.getExternalStorageDirectory()
                    + Util.TRENDING_APPS_FOLDER_NAME + mPackageName.replace(" ", "") + ".apk";
            File apkFile = new File(apkName);
            if (!apkFile.exists()) {
                startDownloadService();
                return;
            }

            String currentFileMD5 = Util.md5(apkFile);
            if (currentFileMD5.equalsIgnoreCase(mExtraMD5)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    CLog.d(Util.TAGC26, "Starting activity after checking md5, intent :: "+ intent);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
                }
            } else {
                // incomplete file exist, most probably download is in progress
                mActionButton.setText("Downloading ...");
                mActionButton.setOnClickListener(null);
            }
        }
    };

    public void startDownloadService() {
        if (!Util.isNetworkAvailable(getApplicationContext())) {
            Toast.makeText(getApplicationContext(),
                    "Internet Connection Not Available", Toast.LENGTH_SHORT).show();
            return;
        }
        setUIState(Util.DOWNLOAD_STARTED, Util.DOWNLOAD_PROGRESS_TOTAL_DEFAULT, Util.DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT);

        // flags to pass in the download service intent
        ArrayList<String> urlSending     = new ArrayList<String>();
        ArrayList<String> appnameSending = new ArrayList<String>();
        ArrayList<String> appMd5Sending  = new ArrayList<String>();
        ArrayList<String> appInstall     = new ArrayList<String>();

        urlSending.add(mExtraAppUrl);
        appnameSending.add(mPackageName.replace(" ", ""));
        appMd5Sending.add(mExtraMD5);
        appInstall.add(mFlagInstall);
        Intent downloadServiceIntent = new Intent(getApplicationContext(), DownloadService.class);

        downloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_URLARRAY, urlSending);
        downloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_APPNAME, appnameSending);
        downloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_APPMD5, appMd5Sending);
        downloadServiceIntent.putExtra(Util.EXTRA_DOWNLOADSERVICE_INSTALL, appInstall);
        downloadServiceIntent.putExtra("receiver", new DownloadReceiver(new Handler()));

        //start the APK download
        startService(downloadServiceIntent);

        // send the analytics data
      //  new SendAnalyticsData(getApplicationContext(), Util.DOWNLOADED_APP).execute(mPackageName);
        Util.createAndSendAnalyticsData(Util.DOWNLOADED_APP, Util.CATEGORY_MANUAL, Util.APP_PACKAGE_NAME, mPackageName, getApplicationContext());
    }

    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if(resultCode == Util.UPDATE_PROGRESS){
                //                String packageNameApk = resultData.getString(Util.DOWNLOAD_RESULT_DOWNLOADED_FILENAME);
                //                Util.sendStatusBroadcast(packageNameApk, Util.DOWNLOAD_COMPLETED, getApplicationContext());
            }
        }
    }

    @Override
    protected void onNewIntent (Intent intent) {
        super.onNewIntent(intent);
        CLog.d(Util.TAGC26, "New intent " + intent);
        if (intent == null) {
            CLog.w(Util.TAGC26, "intent is null");
            return;
        }
        Bundle bundle = intent.getExtras();
        setThePageUI(bundle);
    }

    private void setThePageUI(Bundle bundleReceived){
        mImageView.setImageResource(R.drawable.placeholder);
        mExtraAppName     = bundleReceived.getString(Util.EXTRA_FULLAPP_ONCLICK_APPNAME);
        mExtraAppDev      = bundleReceived.getString(Util.EXTRA_FULLAPP_ONCLICK_APPDEV);
        mExtraAppFullText = bundleReceived.getString(Util.EXTRA_FULLAPP_ONCLICK_FULLTEXT);
        mExtraAppImage1   = bundleReceived.getString(Util.EXTRA_FULLAPP_ONCLICK_IMAGE1);
        mExtraAppImage2   = bundleReceived.getString(Util.EXTRA_FULLAPP_ONCLICK_IMAGE2);
        mExtraAppImage3   = bundleReceived.getString(Util.EXTRA_FULLAPP_ONCLICK_IMAGE3);
        mExtraAppRating   = bundleReceived.getFloat( Util.EXTRA_FULLAPP_ONCLICK_APPRATING);
        mTileType         = bundleReceived.getString(Util.EXTRA_FULLAPP_ONCLICK_APPTYPE);
        mExtraAppUrl      = bundleReceived.getString(Util.EXTRA_FULLAPP_ONCLICK_APPURL);
        mDownloadFlag     = bundleReceived.getBoolean(Util.EXTRA_FULLAPP_ONCLICK_DOWNLOAD);
        mExtraMD5         = bundleReceived.getString(Util.EXTRA_FULLAPP_ONCLICK_MD5);
        mPackageName      = bundleReceived.getString(Util.EXTRA_FULLAPP_ONCLICK_PACKAGE);
        mAppIconUrl       = bundleReceived.getString(Util.EXTRA_FULLAPP_ONCLICK_ICONURL);
        mFlagInstall      = String.valueOf(bundleReceived.getBoolean(Util.EXTRA_FULLAPP_ONCLICK_INSTALL));
        mVideoUrl         = bundleReceived.getString(Util.EXTRA_FULLAPP_ONCLICK_VIDEOURL);
        mAppSize          = bundleReceived.getLong(Util.EXTRA_FULLAPP_ONCLICK_APK_SIZE);

        CLog.e(Util.TAGC26, "App name ::"+ mExtraAppName + " :: appdev ::"+ mExtraAppDev);
        if (Util.isAppAlreadyInstalled(mPackageName, getApplicationContext())) {
            setUIState(Util.INSTALL_COMPLETED, Util.DOWNLOAD_PROGRESS_TOTAL_DEFAULT, Util.DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT);
        } else {
            File existingFile = new File(
                    Environment.getExternalStorageDirectory(),
                    Util.TRENDING_APPS_FOLDER_NAME + mPackageName + ".apk");
            if (existingFile.exists()) {
                String currentMD5 = Util.md5(existingFile);
                if (currentMD5.equalsIgnoreCase(mExtraMD5)) {
                    setUIState(Util.DOWNLOAD_COMPLETED, Util.DOWNLOAD_PROGRESS_TOTAL_DEFAULT, Util.DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT);
                }else{
                    CLog.d(Util.TAGC26, "A file exists. We don't know whether corrupted or download in progress.");
                    if ( Util.isServiceRunning(DownloadService.class, getApplicationContext())) {
                        setUIState(Util.DOWNLOAD_STARTED, Util.DOWNLOAD_PROGRESS_TOTAL_DEFAULT, Util.DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT);
                    }else{
                        setUIState(Util.DOWNLOAD_FAILED, Util.DOWNLOAD_PROGRESS_TOTAL_DEFAULT, Util.DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT);
                        existingFile.delete();
                    }
                }
            } else {
                setUIState(Util.DOWNLOAD_FAILED, Util.DOWNLOAD_PROGRESS_TOTAL_DEFAULT, Util.DOWNLOAD_PROGRESS_FILE_SIZE_DEFAULT);
            }
        }
        mAppName.setText(mExtraAppName);
        mAppDev.setText(mExtraAppDev);
        String txt = mExtraAppFullText;

        WebSettings settings = mAppFullDescription.getSettings();
        settings.setDefaultTextEncodingName("utf-8");
        mAppFullDescription.setWebViewClient(descriptionWebViewClient);
        txt = Util.getLinkifiedText(txt);
        txt = txt.replace("\n", "<br/>");
        String text = "<html><head>"
                + "<style type=\"text/css\">body{color: #9FA1A4;} br{ display:block; line-height:0px;}"
                + "</style></head><body>" + "<p align=\"justify\">" + txt  + "</p> </body></html>";
        fullAppDescText =text;
        if(mExtraAppFullText.length()>300 && !Util.IS_TABLET){
            shortAppDescText = mExtraAppFullText.substring(0, 300);
            shortAppDescText = Util.getLinkifiedText(shortAppDescText);
            shortAppDescText = shortAppDescText.replace("\n", "<br/>");
            shortAppDescText = "<html><head>"
                    + "<style type=\"text/css\">body{color: #9FA1A4;} br{ display:block; line-height:0px;}"
                    + "</style></head><body>" + "<p align=\"justify\">" + shortAppDescText  + "</p> </body></html>";
            mAppFullDescription.loadDataWithBaseURL(null, shortAppDescText, "text/html", "UTF-8", null);
            mShowMorebtn.setBackgroundResource(R.drawable.icon_more_text);
            isShowingFullText =false;

        }else{
            isShowingFullText = true;
            mAppFullDescription.loadDataWithBaseURL(null, fullAppDescText, "text/html", "UTF-8", null);
            mShowMorebtn.setVisibility(View.GONE);
        }
        mShowMorebtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(isShowingFullText){
                    isShowingFullText =false;
                    showTextInWebView(shortAppDescText);
                    mShowMorebtn.setBackgroundResource(R.drawable.icon_more_text);
                }else{
                    isShowingFullText =true;
                    showMoreTextInWebView(fullAppDescText);
                    mShowMorebtn.setBackgroundResource(R.drawable.icon_less_text);
                }
            }
        });

        mAppFullDescription.setBackgroundColor(Color.TRANSPARENT);
        mAppFullDescription.setVisibility(View.VISIBLE);
        mAppRating.setRating(mExtraAppRating);

        CLog.d(Util.TAGC26, "App icon url = "+ mAppIconUrl);
        ImageLoader.getInstance().displayImage(mAppIconUrl, mImageAppIcon , displayImageOptions);

        screenshotImageUrls = new String[3];
        screenshotImageUrls[0]=mExtraAppImage1;
        screenshotImageUrls[1]=mExtraAppImage2;
        screenshotImageUrls[2]=mExtraAppImage3;

        screenshotContainer.removeAllViews();
        if(mVideoUrl != null && !mVideoUrl.trim().isEmpty()){
            CLog.wtf(Util.TAGC26, "video url ==>>" + mVideoUrl);
            loadAppVideo(mVideoUrl);
        }else{
            YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.app_video_view);
            youTubeView.setVisibility(View.GONE);
        }
        loadScreenShots(mExtraAppImage1, 0);
        loadScreenShots(mExtraAppImage2, 1);
        loadScreenShots(mExtraAppImage3, 2);
    }

    private void showMoreTextInWebView(String text){
        if(descriptionWrapperLayout.getChildCount()>0){
            WebView webview = (WebView)descriptionWrapperLayout.getChildAt(0);
            webview.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
            return;
        }
        showTextInWebView(text);
    }
    private void showTextInWebView(String text){
        descriptionWrapperLayout.removeAllViews();
        WebView descWebView = new WebView(this);
        descWebView = new WebView(this);
        descWebView.setBackgroundColor(Color.TRANSPARENT);
        WebSettings settings = mAppFullDescription.getSettings();
        settings.setDefaultTextEncodingName("utf-8");
        descWebView.setWebViewClient(descriptionWebViewClient);
        descWebView.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
        descriptionWrapperLayout.addView(descWebView, 0, new LinearLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT)));
    }
    private void loadScreenShots(String imgUrl, final int position){
        LayoutInflater inflater = getLayoutInflater();
        View imageLayout = inflater.inflate(R.layout.item_gallery_image,null, false);
        ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
        ImageLoader.getInstance().displayImage(imgUrl, imageView, displayImageOptions);
        imageLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startImagePagerActivity(position);
            }
        });
        screenshotContainer.addView(imageLayout);
    }

    private void loadAppVideo(String imgUrl){
        YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.app_video_view);
        youTubeView.setVisibility(View.VISIBLE);
        youTubeView.initialize(Util.YOUTUBE_DEVELOPER_KEY, this);
        if(mPlayer!=null){
            try{
                mPlayer.cueVideo(mVideoUrl);
            }catch(Exception ise){
                CLog.d(Util.TAGC26, "Exception while changing video url for player");
            }
        }
    }

    private void initializeViewElements(){
        mProgressBar        = (ProgressBar) findViewById(R.id.downloadProgressBar);
        mAppName            = (TextView) findViewById(R.id.tvAppName);
        mAppDev             = (TextView) findViewById(R.id.tvAppDevName);
        mAppFullDescription = (WebView) findViewById(R.id.tvFullAppDescription);
        mAppRating          = (RatingBar) findViewById(R.id.rbAppRating);
        mImageView          = (ImageView) findViewById(R.id.ivScreenshot1);
        tvDescriptionHead   = (TextView) findViewById(R.id.tvDescriptionTitle);
        mActionButton       = (Button) findViewById(R.id.bAppActionButton);
        mActionButtonImage  = (ImageView) findViewById(R.id.ivActionButton);
        screenshotContainer = (LinearLayout) findViewById(R.id.screenshotContainer);
        mImageAppIcon       = (ImageView) findViewById(R.id.imageAppIcon);
        mShowMorebtn        = (ImageView) findViewById(R.id.btnShowMore);
        mProgressPercent    = (TextView) findViewById(R.id.progress_precentage);
        mProgressBarLayout  = (RelativeLayout) findViewById(R.id.progressBarLayout);
        mProgressCancel     = (ImageView) findViewById(R.id.progress_cancel);
        mProgressBytes      = (TextView) findViewById(R.id.progress_bytes);
        descriptionWrapperLayout      = (LinearLayout) findViewById(R.id.descriptionWrapperLayout);
        mProgressCancelForWaitingApps = (ImageView) findViewById(R.id.progress_cancel_for_download_waiting);
    }

    private void startImagePagerActivity(int position) {
        Intent intent = new Intent(this, ImagePagerActivity.class);
        intent.putExtra(Util.SCREENSHOT_IMAGES, screenshotImageUrls);
        intent.putExtra(Util.SCREENSHOT_IMAGE_POSITION, position);
        startActivity(intent);
    }

    private WebViewClient descriptionWebViewClient = new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            CLog.d(Util.TAGC26, "Url from description : "+url);
            if(Util.isNetworkAvailable(FullAppDescription.this)){
                Intent launchUrlOnClick = new Intent(android.content.Intent.ACTION_VIEW);
                launchUrlOnClick.setData(Uri.parse(url));
                launchUrlOnClick.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                CLog.d(Util.TAGC26, "Starting activity to show app label url");
                startActivity(launchUrlOnClick);
            }else{
                Toast.makeText(FullAppDescription.this, "No Internet connection", Toast.LENGTH_LONG).show();
            }
            return true;
        }
    };

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
            boolean wasRestored) {
        CLog.d(Util.TAGC26, "Youtube initialized with url :: "+mVideoUrl);
        mPlayer = player;
        if (!wasRestored) {
            //                    player.loadVideo(mVideoUrl);
            player.cueVideo(mVideoUrl);
        }
        player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI);
        player.setOnFullscreenListener(this);

    }

    @Override
    protected Provider getYouTubePlayerProvider() {
        LayoutInflater inflater = getLayoutInflater();
        View videoLayout = inflater.inflate(R.layout.item_gallery_video,null, false);
        YouTubePlayerView youTubeView = (YouTubePlayerView) videoLayout.findViewById(R.id.youtube_view);
        return youTubeView;
    }

    @Override
    public void onFullscreen(boolean isFullScreen) {
        CLog.wtf(Util.TAGC26, "on Full screen :: " + isFullScreen);
        mFullscreen = isFullScreen;

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if(!mFullscreen){
            super.onBackPressed();
            CLog.wtf(Util.TAGC26, "on back pressed");
        }else{
            if(mPlayer != null){
                mPlayer.setFullscreen(false);
            }
        }
    }


public static Bitmap drawableToBitmap (Drawable drawable) {
    if (drawable instanceof BitmapDrawable) {
        return ((BitmapDrawable)drawable).getBitmap();
    }

    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.RGB_565);
    Canvas canvas = new Canvas(bitmap); 
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);

    return bitmap;
}


}
