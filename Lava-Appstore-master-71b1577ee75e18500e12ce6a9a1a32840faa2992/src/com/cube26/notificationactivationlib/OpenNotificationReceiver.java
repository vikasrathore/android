package com.cube26.notificationactivationlib;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.cube26.trendingnow.util.CLog;

public class OpenNotificationReceiver extends BroadcastReceiver{

    private NotificationDTO notificationDTO;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        CLog.d(Util.TAGC26, "Opening notification");
        notificationDTO = (NotificationDTO)intent
                .getSerializableExtra(Util.NOTIFICATION_EXTRA);
        mContext = context;
        
        EventDTO eventDTO = new EventDTO();
        eventDTO.setEventAction("Notification_Opended");
        eventDTO.setEventCategory("Notification");
        eventDTO.setEventLabel("notification-id");
        eventDTO.setEventValue(notificationDTO.getId());
        
        new SendAnalyticsData(mContext, eventDTO).execute("");
        
        String packageName = notificationDTO.getPackageName();
        String activityName = notificationDTO.getActivityName();
        if (!"".equalsIgnoreCase(packageName)) {
            if (!"".equalsIgnoreCase(activityName)) {
                try{
                    Intent openIntent = new Intent();
                    openIntent.setComponent(new ComponentName(packageName, activityName));
                    openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    for (AppExtraKeyValuePair pair : notificationDTO.getAppExtraKeyValuePairs()) {
                        openIntent.putExtra(pair.getKeyString(), pair.getValueString());
                    }
                    mContext.startActivity(openIntent);
                    return;
                }catch(Exception e){

                }
            }
            try{
                Intent openIntent = mContext.getPackageManager().getLaunchIntentForPackage(
                        packageName);
                openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(openIntent);
                return;
            }catch(Exception e){

            }
        }
        String openUrl = notificationDTO.getRedirectionUrl();
        try{
            Intent internetIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(openUrl));
            internetIntent.setComponent(new ComponentName(Util.DEFAULT_BROWSER_PACKAGE_NAME, Util.DEFAULT_BROWSER_CLASS_NAME));
            internetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(internetIntent);
        }catch(Exception e){
            try{
                Intent openApp = mContext.getPackageManager().getLaunchIntentForPackage(Util.CHROME_BROWSER_PACKAGE_NAME);
                openApp.setData(Uri.parse(openUrl));
                openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(openApp);
                CLog.d(Util.TAGC26, "Opening notification url in chrome browser");
            }catch(Exception e1){
                try{
                    Intent launchUrlOnClick = new Intent(android.content.Intent.ACTION_VIEW);
                    launchUrlOnClick.setData(Uri.parse(openUrl));
                    launchUrlOnClick.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchUrlOnClick);
                    CLog.d(Util.TAGC26, "Starting activity to show notification url through any other app");
                }catch(Exception e2){
                    CLog.d(Util.TAGC26, "Error opening browser");
                }
            }
        }

    }
}
