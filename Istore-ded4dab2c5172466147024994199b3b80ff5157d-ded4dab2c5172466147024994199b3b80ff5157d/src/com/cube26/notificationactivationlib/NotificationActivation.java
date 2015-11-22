package com.cube26.notificationactivationlib;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import com.cube26.trendingnow.util.CLog;

public class NotificationActivation {

    private Context mContext;

    public NotificationActivation(Context context) {
        this.mContext = context;
    }

    public void checkForNotificationAndActivation(){
        
        try {
            Intent sendUnsentDataServiceIntent = new Intent(mContext,
                    SendUnsentData.class);
            mContext.startService(sendUnsentDataServiceIntent);
        }  catch (Exception e){
            CLog.d(Util.TAGC26, "Unsent data sending service not instantiated");
        }
        
        NotificationAsyncTask notificationAsyncTask = new NotificationAsyncTask();
        notificationAsyncTask.execute("");
    }

    private class NotificationAsyncTask extends AsyncTask<String, String, String>{

        DataTransferObject dataTransferObject;
        ArrayList<NotificationDTO> notificationDTOs;
        ArrayList<ActivationDTO> activationDTOs;

        @Override
        protected String doInBackground(String... arg0) {

            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, Util.timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, Util.timeoutSocket);
            HttpClient httpclient = new DefaultHttpClient(httpParameters);

            try {
                HttpGet httppost = new HttpGet(Util.API
                        + "?user_id=" + Util.getDeviceId(mContext)
                        + "&android_id=" + URLEncoder.encode(Util.getAndroidId(mContext), "UTF-8")
                        + "&package_name="+ URLEncoder.encode(Util.getOwnPackageName(mContext), "UTF-8")
                        + "&model=" + URLEncoder.encode(Util.getDeviceName(),"UTF-8")
                        + "&country_code="+ URLEncoder.encode(Util.getCountryCode(mContext), "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);
                String result = EntityUtils.toString(response.getEntity());

                JSONObject mainJSONObject = new JSONObject(result);

                try {
                    long updateFrequency = mainJSONObject.has("update-frequency")?mainJSONObject.getLong("update-frequency"):Util.getUpdateFrequencyFromSharedPrefs(mContext);
                    String onOffFlag     = mainJSONObject.has("on-off")?mainJSONObject.getString("on-off"):Util.getOnOffFlagFromSharedPref(mContext);
                    
                    Util.setOnOffFlag(mContext, onOffFlag);
                    CLog.d(Util.TAGC26, "Checking update frequency");
                    if(updateFrequency != Util.getUpdateFrequencyFromSharedPrefs(mContext)){
                        CLog.d(Util.TAGC26, "Change in update frequency");
                        Util.setUpdateFrequency(mContext, updateFrequency);
                        NotificationActivationReceiver.setAlarmForNotificationAndActivation(mContext);
                        return null;
                    }
                    
                    if(Util.SERVER_FLAG_OFF.equalsIgnoreCase(onOffFlag)){
                        CLog.d(Util.TAGC26, "Notification and activation flag off. So returning.");
                        return null;
                    }
                    
                    dataTransferObject = new DataTransferObject();

                    JSONArray notificationsArray = mainJSONObject.getJSONArray("notification");
                    notificationDTOs = new ArrayList<NotificationDTO>();
                    for (int i = 0; i < notificationsArray.length(); i++) {
                        JSONObject notificationJsonObject = notificationsArray.getJSONObject(i);

                        NotificationDTO notificationDTO = new NotificationDTO();
                        notificationDTO.setId(notificationJsonObject.has("notificationid")?notificationJsonObject.getString("notificationid"):"");
                        notificationDTO.setTitle(notificationJsonObject.has("title")?notificationJsonObject.getString("title"):"");
                        notificationDTO.setContent(notificationJsonObject.has("content")?notificationJsonObject.getString("content"):"");
                        notificationDTO.setTime(notificationJsonObject.has("time")?notificationJsonObject.getString("time"):"");
                        notificationDTO.setIconUrl(notificationJsonObject.has("icon")?notificationJsonObject.getString("icon"):"");
                        notificationDTO.setPackageName(notificationJsonObject.has("packagename")?notificationJsonObject.getString("packagename"):"");
                        notificationDTO.setActivityName(notificationJsonObject.has("componentname")?notificationJsonObject.getString("componentname"):"");
                        notificationDTO.setRedirectionUrl(notificationJsonObject.has("uri")?notificationJsonObject.getString("uri"):"");

                        ArrayList<AppExtraKeyValuePair> appExtraKeyValuePairs = new ArrayList<AppExtraKeyValuePair>();
                        try {
                            JSONObject extrasJsonObject =  notificationJsonObject.getJSONObject("extras");
                            @SuppressWarnings("unchecked")
                            Iterator<String> keys = extrasJsonObject.keys();
                            while(keys.hasNext()){
                                String key = keys.next();
                                String val = extrasJsonObject.getString(key);
                                AppExtraKeyValuePair appExtraKeyValuePair = new AppExtraKeyValuePair();
                                appExtraKeyValuePair.setKeyString(key);
                                appExtraKeyValuePair.setValueString(val);

                                appExtraKeyValuePairs.add(appExtraKeyValuePair);
                            }
                        } catch (Exception e) {
                        }
                        notificationDTO.setAppExtraKeyValuePairs(appExtraKeyValuePairs);

                        notificationDTOs.add(notificationDTO);
                    }

                    JSONArray activationsArray = mainJSONObject.getJSONArray("app");
                    activationDTOs = new ArrayList<ActivationDTO>();
                    for (int i = 0; i < activationsArray.length(); i++) {
                        JSONObject activationJsonObject = activationsArray.getJSONObject(i);

                        ActivationDTO activationDTO = new ActivationDTO();
                        activationDTO.setPackageName(activationJsonObject.has("packagename")?activationJsonObject.getString("packagename"):"");
                        activationDTO.setActivityName(activationJsonObject.has("componentname")?activationJsonObject.getString("componentname"):"");
                        activationDTO.setActivationTime(activationJsonObject.has("time")?activationJsonObject.getString("time"):"");

                        ArrayList<AppExtraKeyValuePair> appExtraKeyValuePairs = new ArrayList<AppExtraKeyValuePair>();
                        try {
                            JSONObject extrasJsonObject =  activationJsonObject.getJSONObject("extras");
                            @SuppressWarnings("unchecked")
                            Iterator<String> keys = extrasJsonObject.keys();
                            while(keys.hasNext()){
                                String key = keys.next();
                                String val = extrasJsonObject.getString(key);
                                AppExtraKeyValuePair appExtraKeyValuePair = new AppExtraKeyValuePair();
                                appExtraKeyValuePair.setKeyString(key);
                                appExtraKeyValuePair.setValueString(val);

                                appExtraKeyValuePairs.add(appExtraKeyValuePair);
                            }
                        } catch (Exception e) {
                        }
                        activationDTO.setAppExtraKeyValuePairs(appExtraKeyValuePairs);

                        activationDTOs.add(activationDTO);
                    }

                    dataTransferObject.setNotificationDTOs(notificationDTOs);
                    dataTransferObject.setActivationDTOs(activationDTOs);

                } catch (Exception e) {
                    CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
                }
            }catch(Exception e){
                CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(dataTransferObject==null){
                return;
            }
            try {
                for (NotificationDTO notificationDTO : dataTransferObject.getNotificationDTOs()) {
                    createNotification(notificationDTO);
                }

                for (ActivationDTO activationDTO : dataTransferObject.getActivationDTOs()) {
                    createActivation(activationDTO);
                }
            } catch (Exception e) {
                CLog.e(Util.TAGC26, "Exception while creating notification and activation::" + e.getMessage());
            }
        }

        private void createNotification(NotificationDTO notificationDTO){
            String notificationTimeString = notificationDTO.getTime();
            long notificationTime;
            try {
                notificationTime = Long.parseLong(notificationTimeString);
            } catch (Exception e) {
                CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
                notificationTime = -1;
            }
            if (notificationTime > 0) {
                Calendar calSet = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calSet.setTimeInMillis(notificationTime-TimeZone.getDefault().getOffset(notificationTime));
                
                Calendar calNow = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                CLog.d(Util.TAGC26, "Setting alarm for notification");
                if (calSet.compareTo(calNow) <= 0) {
                    CLog.d(Util.TAGC26, "Notification time already passed ::"+ calSet.toString());
                    return;
                }

                Intent alarmIntent = new Intent(mContext, NotificationReceiver.class);
                alarmIntent.setAction(Util.NOTIFICATION_RECEIVER);
                alarmIntent.putExtra(Util.NOTIFICATION_EXTRA, notificationDTO);
                PendingIntent pendingIntent = PendingIntent
                        .getBroadcast(mContext, (int)notificationTime,
                                alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarmManager = (AlarmManager) mContext
                        .getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC,
                        calSet.getTimeInMillis(), pendingIntent);
            }
        }

        private void createActivation(ActivationDTO activationDTO){
            String activationTimeString = activationDTO.getActivationTime();
            long activationTime;
            try {
                activationTime = Long.parseLong(activationTimeString);
            } catch (Exception e) {
                CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
                activationTime = -1;
            }
            if (activationTime > 0) {
                Calendar calSet = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calSet.setTimeInMillis(activationTime-TimeZone.getDefault().getOffset(activationTime));

                Calendar calNow = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                CLog.d(Util.TAGC26, "Setting alarm for activation");
                if (calSet.compareTo(calNow) <= 0) {
                    CLog.d(Util.TAGC26, "Activation time already passed for package name ::"+ activationDTO.getPackageName());
                    return;
                }

                Intent alarmIntent = new Intent(mContext, AppActivationReceiver.class);
                alarmIntent.setAction(Util.ACTIVATION_RECEIVER);
                alarmIntent.putExtra(Util.ACTIVATION_EXTRA, activationDTO);
                PendingIntent pendingIntent = PendingIntent
                        .getBroadcast(mContext, (int)activationTime,
                                alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarmManager = (AlarmManager) mContext
                        .getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC,
                        calSet.getTimeInMillis(), pendingIntent);
            }
        }
    }
}
