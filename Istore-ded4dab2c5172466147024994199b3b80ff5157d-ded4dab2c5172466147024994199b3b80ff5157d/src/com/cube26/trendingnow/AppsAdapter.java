package com.cube26.trendingnow;

import java.io.File;
import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.cube26.istore.R;
import com.cube26.trendingapps.ui.CircularProgressBar;
import com.cube26.trendingapps.ui.ProgressButton;
import com.cube26.trendingapps.webservices.DownloadService;
import com.cube26.trendingapps.webservices.FetchedAppData;
import com.cube26.trendingnow.util.CLog;
import com.cube26.trendingnow.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

public class AppsAdapter extends BaseAdapter{
    private Context mContext;
    private ArrayList<FetchedAppData> mAppsList;

    DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
    .resetViewBeforeLoading(true)  // default
    .delayBeforeLoading(10)
    .cacheInMemory(true) // default
    .cacheOnDisk(true) // default
    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
    .bitmapConfig(Bitmap.Config.RGB_565) // default
    .displayer(new SimpleBitmapDisplayer()) // default
    .handler(new Handler()) // default
    .build();

    private class ViewHolder {
        ImageView appImage;
        TextView appName;
        RatingBar appRating;
        TextView appDev;
        ImageButton appDesc;
        ImageButton appVideo;
        ImageView appActionButton;
        ProgressButton appProgress;
        CircularProgressBar appDownloadProgressBar;
    }

    public AppsAdapter(Context context, ArrayList<FetchedAppData> appDatas){
        mContext = context;
        mAppsList = appDatas;
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                mContext).build();
        ImageLoader.getInstance().init(config);
    }

    public void setProgressForPackage(String packageName, int progress){
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mAppsList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final FetchedAppData appData = mAppsList.get(position);
        if (convertView == null) {

            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = vi.inflate(R.layout.app_list_element, null);

            holder = new ViewHolder();
            holder.appName = (TextView) convertView.findViewById(R.id.app_name);
            holder.appImage = (ImageView) convertView.findViewById(R.id.app_image);
            holder.appRating = (RatingBar) convertView.findViewById(R.id.app_rating);
            holder.appDev = (TextView) convertView.findViewById(R.id.app_dev);
            holder.appDesc = (ImageButton) convertView.findViewById(R.id.description_btn);
            holder.appVideo = (ImageButton) convertView.findViewById(R.id.video_btn);
            holder.appActionButton = (ImageView) convertView.findViewById(R.id.app_action_button);
            holder.appProgress = (ProgressButton) convertView.findViewById(R.id.app_download_circular_progress);
            holder.appDownloadProgressBar = (CircularProgressBar) convertView.findViewById(R.id.app_download_progressBar);
            holder.appDownloadProgressBar.setVisibility(View.GONE);
            holder.appProgress.setPinned(false);
            holder.appProgress.setClickable(true);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.appName.setText(appData.getAppName());
        holder.appRating.setRating(appData.getAppRating());
        holder.appDev.setText(appData.getAppDev());
        holder.appDownloadProgressBar.setVisibility(View.GONE);
        holder.appActionButton.setTag(appData);
        final ImageView iv = holder.appActionButton;
        setActionButtonImage(iv, appData);
        holder.appActionButton.setOnClickListener(startInstallerOnClickListener);
        ImageLoader.getInstance().displayImage(appData.getAppIconUrl(), holder.appImage, displayImageOptions);
        holder.appDownloadProgressBar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                DownloadService.removeAppFromQueue(appData.getPackageName());
            }
        });
        final int itemPosition = position;
        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Util.buildAppClickIntent(mContext, mAppsList.get(itemPosition));    
            }
        });
        holder.appDesc.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Util.buildAppClickIntent(mContext, mAppsList.get(itemPosition));
            }
        });
//        if(mAppsList.get(itemPosition).getAppVideoUri().trim().length()>0){
//            holder.appVideo.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Util.playFullScreenVideo(mContext, mAppsList.get(itemPosition).getAppVideoUri());
//                }
//            });
//            holder.appVideo.setImageResource(R.drawable.icon_app_video);
//        }else{
//            holder.appVideo.setOnClickListener(null);
//            holder.appVideo.setImageResource(R.drawable.icon_app_video_disabled);
//        }
        return convertView;
    }

    private void setActionButtonImage(final ImageView tv, final FetchedAppData appData){
        int imageToShow = Util.getImageForListItemDownloadProgressByPackage(mContext, appData);
        tv.setVisibility(View.VISIBLE);
        tv.setImageResource(imageToShow);
    }
    private OnClickListener startInstallerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            FetchedAppData currentApp = (FetchedAppData) arg0.getTag();
            
            if (Util.isAppAlreadyInstalled(currentApp.getPackageName(), mContext)) {
                Util.openAppForPackageName(mContext, currentApp.getPackageName());
                return;
            } 
            String apkName = Environment.getExternalStorageDirectory()
                    + Util.TRENDING_APPS_FOLDER_NAME + currentApp.getPackageName().replace(" ", "") + ".apk";
            File apkFile = new File(apkName);
            if (!apkFile.exists()) {
                Util.startDownloadingApp(mContext, currentApp);
                return;
            }

            String currentFileMD5 = Util.md5(apkFile);
            if (currentFileMD5.equalsIgnoreCase(currentApp.getMD5())) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    CLog.d(Util.TAGC26, "Starting activity after checking md5, intent :: "+ intent);
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
                }
            } else {
                // incomplete file exist, most probably download is in progress
                ((ImageView)arg0).setImageResource(R.drawable.icon_app_download);
                ((ImageView)arg0).setOnClickListener(null);
            }
        }
    };
}
