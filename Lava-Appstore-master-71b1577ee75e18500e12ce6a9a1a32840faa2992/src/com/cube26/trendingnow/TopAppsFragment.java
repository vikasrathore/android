package com.cube26.trendingnow;

import java.io.File;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import com.cube26.celkonstore.R;
import com.cube26.trendingnow.util.CLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.cube26.trendingapps.ui.CircularProgressBar;
import com.cube26.trendingapps.webservices.DownloadService;
import com.cube26.trendingapps.webservices.FetchedAppData;
import com.cube26.trendingnow.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

@SuppressLint("ValidFragment")
public class TopAppsFragment extends Fragment {

    private Context mContext;
    public static final String BANNER_APP = "banner_app";
    public static final String POSITION = "POSITION";
    DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
    .resetViewBeforeLoading(true) // default
    .delayBeforeLoading(10).cacheInMemory(true) // default
    .cacheOnDisk(true) // default
    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
    .bitmapConfig(Bitmap.Config.RGB_565) // default
    .displayer(new SimpleBitmapDisplayer()) // default
    .handler(new Handler()) // default
    .build();
    private ImageView mBannerAppIcon;
    private TextView mBannerAppName;
    private TextView mBannerAppDev;
    private RatingBar mBannerAppRating;
    private ImageView mBannerAppsImage;
    private TextView mInfoVideo;
    private TextView mActionButton;
    private CircularProgressBar mAppProgressBar;
    private FetchedAppData mBannerAppData;
    private String mPackageName;
    private String mExtraMD5;

    public TopAppsFragment(Context context) {
        mContext = context;
    }

    public TopAppsFragment() {
    }
    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity())
        .registerReceiver( appDownloadStatusReceiver,
                new IntentFilter(Util.DOWNLOAD_RECEIVER_INTENT_NAME));
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity())
        .unregisterReceiver((appDownloadStatusReceiver));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_top_apps, container, false);
        mContext = getActivity();
        mBannerAppsImage = (ImageView) rootView
                .findViewById(R.id.bannerAppImage);
        mBannerAppData = (FetchedAppData) getArguments()
                .getSerializable(TopAppsFragment.BANNER_APP);
        mBannerAppIcon = (ImageView) rootView.findViewById(R.id.info_app_image);
        mBannerAppDev = (TextView) rootView.findViewById(R.id.info_app_dev);
        mBannerAppName = (TextView) rootView.findViewById(R.id.info_app_name);
        mInfoVideo = (TextView) rootView.findViewById(R.id.video_text);
        mActionButton = (TextView) rootView.findViewById(R.id.download_text);
        mAppProgressBar = (CircularProgressBar) rootView.findViewById(R.id.app_download_progressBar);
        mPackageName = mBannerAppData.getPackageName();
        mExtraMD5 = mBannerAppData.getMD5();
        mBannerAppRating = (RatingBar) rootView
                .findViewById(R.id.info_app_rating);
        mBannerAppDev.setText(mBannerAppData.getAppDev());
        mBannerAppName.setText(mBannerAppData.getAppName());
        mBannerAppRating.setRating(mBannerAppData.getAppRating());
        ImageLoader.getInstance().displayImage(mBannerAppData.getAppIconUrl(),
                mBannerAppIcon, displayImageOptions);
        ImageLoader.getInstance().displayImage(mBannerAppData.getBannerImage(),
                mBannerAppsImage, displayImageOptions);
        //setUpUI();

        mAppProgressBar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                DownloadService.removeAppFromQueue(mPackageName);
            }
        });
        mBannerAppIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buildAppClickIntent(mBannerAppData);
            }
        });
        mBannerAppsImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buildAppClickIntent(mBannerAppData);
            }
        });
        if(mBannerAppData.getAppVideoUri().trim().length()>0){
            mInfoVideo.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.playFullScreenVideo(mContext, mBannerAppData.getAppVideoUri());
                }
            });
            mInfoVideo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_banner_video, 0, 0);
        }else{
            mInfoVideo.setOnClickListener(null);
            mInfoVideo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_banner_video_disabled, 0, 0);
        }
        return rootView;
    }


    private void buildAppClickIntent(FetchedAppData appData) {
        Intent intent = new Intent();

        intent.setComponent(new ComponentName(Util.getPackageName(mContext),
                Util.APP_DESCRIPTION_PAGE));
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APPNAME,
                appData.getAppName());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APPRATING,
                appData.getAppRating());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APPDEV, appData.getAppDev());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_FULLTEXT,
                appData.getAppFullText());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_ICONURL,
                appData.getAppIconUrl());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APPURL, appData.getAppUrl());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_IMAGE1, appData.getImage1());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_IMAGE2, appData.getImage2());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_IMAGE3, appData.getImage3());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APPTYPE,
                appData.getAppType());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_DOWNLOAD,
                appData.getFlag_Download());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_INSTALL,
                appData.getFlag_Install());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_MD5, appData.getMD5());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_PACKAGE,
                appData.getPackageName());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_VIDEOURL,
                appData.getAppVideoUri());
        intent.putExtra(Util.EXTRA_FULLAPP_ONCLICK_APK_SIZE, appData.getApkSize());

        mContext.startActivity(intent);
    }

    private OnClickListener startInstallerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            String apkName = Environment.getExternalStorageDirectory()
                    + Util.TRENDING_APPS_FOLDER_NAME + mBannerAppData.getPackageName().replace(" ", "") + ".apk";
            File apkFile = new File(apkName);
            if (!apkFile.exists()) {
                Util.startDownloadingApp(getActivity(), mBannerAppData);
                return;
            }

            String currentFileMD5 = Util.md5(apkFile);
            if (currentFileMD5.equalsIgnoreCase(mBannerAppData.getMD5())) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    CLog.d(Util.TAGC26, "Starting activity after checking md5, intent :: "+ intent);
                    startActivity(intent);
                    getActivity().finish();
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

    private OnClickListener openAppOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Util.openAppForPackageName(mContext, mPackageName);
        }
    };

    private void setUpUI(){
        if (Util.isAppAlreadyInstalled(mPackageName, mContext)) {
            setUIState(Util.INSTALL_COMPLETED, mPackageName, Util.DOWNLOAD_PROGRESS_DEFAULT);
        } else {
            File existingFile = new File(
                    Environment.getExternalStorageDirectory(),
                    Util.TRENDING_APPS_FOLDER_NAME + mPackageName + ".apk");
            if (existingFile.exists()) {
                String currentMD5 = Util.md5(existingFile);
                if (currentMD5.equalsIgnoreCase(mExtraMD5)) {
                    setUIState(Util.DOWNLOAD_COMPLETED, mPackageName, Util.DOWNLOAD_PROGRESS_DEFAULT);
                }else{
                    CLog.d(Util.TAGC26, "A file exists. We don't know whether corrupted or download in progress.");
                    if ( Util.isServiceRunning(DownloadService.class, mContext)) {
                        setUIState(Util.DOWNLOAD_STARTED, mPackageName, Util.DOWNLOAD_PROGRESS_DEFAULT);
                    }else{
                        setUIState(Util.DOWNLOAD_FAILED, mPackageName, Util.DOWNLOAD_PROGRESS_DEFAULT);
                        existingFile.delete();
                    }
                }
            } else {
                setUIState(Util.DOWNLOAD_FAILED, mPackageName, Util.DOWNLOAD_PROGRESS_DEFAULT);
            }
        }
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
        if(mPackageName.equalsIgnoreCase(appPackage)){
            CLog.d(Util.TAGC26, "REceived braodcast for banner app ==== " + progress);

            switch (statusUI) {
                case Util.DOWNLOAD_STARTED:
                    CLog.wtf(Util.TAGC26, " download started but waiting for it in featured fragment ");
                    mActionButton.setVisibility(View.GONE);
                    mAppProgressBar.setProgress(0);
                    mAppProgressBar.setVisibility(View.VISIBLE);
                    mAppProgressBar.invalidate();
                    break;
                case Util.DOWNLOAD_COMPLETED:
                    mActionButton.setText("Install");
                    //mActionButtonImage.setVisibility(View.VISIBLE);
                    mAppProgressBar.setVisibility(View.GONE);
                    mActionButton.setOnClickListener(startInstallerOnClickListener);
                    break;
                case Util.DOWNLOAD_FAILED:
                    mActionButton.setText("Download");
                    //mActionButtonImage.setVisibility(View.VISIBLE);
                    mActionButton.setVisibility(View.VISIBLE);
                    mAppProgressBar.setVisibility(View.GONE);
                    mActionButton.setOnClickListener(startInstallerOnClickListener);
                    break;
                case Util.DOWNLOAD_CANCELED:
                    mActionButton.setText("Download");
                    //mActionButtonImage.setVisibility(View.VISIBLE);
                    mActionButton.setVisibility(View.VISIBLE);
                    mAppProgressBar.setVisibility(View.GONE);
                    mActionButton.setOnClickListener(startInstallerOnClickListener);
                    break;
                case Util.INSTALL_STARTED:
                    mActionButton.setText("Installing");
                    mActionButton.setVisibility(View.VISIBLE);
                    mAppProgressBar.setVisibility(View.GONE);
                    //mActionButtonImage.setVisibility(View.INVISIBLE);
                    mActionButton.setOnClickListener(null);
                    break;
                case Util.INSTALL_COMPLETED:
                    mActionButton.setText("Open");
                    mAppProgressBar.setVisibility(View.GONE);
                    mActionButton.setVisibility(View.VISIBLE);
                    mActionButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.open, 0, 0);
                    mActionButton.setOnClickListener(openAppOnClickListener);
                    break;
                case Util.DOWNLOAD_PROGRESS:
                    CLog.d(Util.TAGC26, "Received progress :: "+ progress );
                    mActionButton.setVisibility(View.GONE);
                    mAppProgressBar.setVisibility(View.VISIBLE);
                    mAppProgressBar.setProgress(progress);
                    mAppProgressBar.invalidate();
                    break;
                default:
                    Toast.makeText(mContext,
                            "Error. Unexpected value received", Toast.LENGTH_LONG).show();
            }
        }else{
            //            CLog.wtf(Util.TAGC26, "app packaage :: " + appPackage);
        }
    }
}
