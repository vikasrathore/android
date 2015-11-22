package com.cube26.notificationactivationlib;

import java.io.InputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.cube26.istore.R;
import com.cube26.trendingnow.util.CLog;


public class NotificationReceiver extends BroadcastReceiver{

    private NotificationDTO notificationDTO;
    @Override
    public void onReceive(Context ctxt, Intent incomingIntent) {
        
        CLog.d(Util.TAGC26, "Notification received. Creating notification");
        
        notificationDTO = (NotificationDTO)incomingIntent
                .getSerializableExtra(Util.NOTIFICATION_EXTRA);
        
        CLog.d(Util.TAGC26, "Starting image download task for notification");
        ImageDownloadTask imageDownloadTask = new ImageDownloadTask(ctxt,notificationDTO);
        imageDownloadTask.execute(notificationDTO.getIconUrl());
    }

    class ImageDownloadTask extends AsyncTask<String, Void, Bitmap>{

        private NotificationDTO notificationDTO;
        private NotificationCompat.Builder mNotificationBuilder;
        private int notificationId = 10;
        private Notification mNotification;
        private Context mContext;
        
        public ImageDownloadTask(Context context,NotificationDTO notificationDTO) {
            mContext = context;
            this.notificationDTO=notificationDTO;
        }
        @Override
        protected Bitmap doInBackground(String... arg) {
            String urldisplay = arg[0];
            Bitmap mIcon = null;
            try {
                CLog.d(Util.TAGC26, "Image download started");
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                CLog.d(Util.TAGC26, "Image downmoad failed");
                CLog.e(Util.TAGC26, e.getMessage());
                e.printStackTrace();
            }
            return mIcon;
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            try{
                notificationId = Integer.parseInt(notificationDTO.getId());
            // Build intent for notification content
            Intent viewIntent = new Intent(mContext,OpenNotificationReceiver.class);
            viewIntent.setAction(Util.NOTIFICATION_OPEN_APP_RECEIVER);
            viewIntent.putExtra(Util.NOTIFICATION_EXTRA, notificationDTO);
            PendingIntent viewPendingIntent =
                    PendingIntent.getBroadcast(mContext, 0, viewIntent, 0);
            
            mNotificationBuilder=  new NotificationCompat.Builder(mContext)
            .setContentTitle(notificationDTO.getTitle())
            .setContentText(notificationDTO.getContent())
            .setContentIntent(viewPendingIntent)
            .setSmallIcon(R.drawable.deals_icon);
            if(result!=null){
                mNotificationBuilder.setLargeIcon(result);
            }
            mNotification = mNotificationBuilder.build();
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(notificationId, mNotification);
            CLog.d(Util.TAGC26, "Notification displayed");
            
            EventDTO eventDTO = new EventDTO();
            eventDTO.setEventAction("Notification_Displayed");
            eventDTO.setEventCategory("Notification");
            eventDTO.setEventLabel("notification-id");
            eventDTO.setEventValue(notificationDTO.getId());
            
            new SendAnalyticsData(mContext, eventDTO).execute("");
            }catch(Exception e){
            }
        }
    }
}
