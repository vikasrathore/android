/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.notificationactivationlib;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.cube26.trendingnow.util.CLog;

import com.cube26.notificationactivationlib.UnsentDataContract.FeedEntry;

public class SendUnsentData extends IntentService {

    private List<EventDTO> eventsList;

    public SendUnsentData() {
        super("SendUnsentData");
    }

    @Override
    protected void onHandleIntent(Intent arg0) {

        File dbPath = getApplicationContext().getDatabasePath("FeedReader.db");
        CLog.d(Util.TAGC26, "Path where database is checked for existance ::"+dbPath);
        if (dbPath.exists()) {

            CLog.d(Util.TAGC26, "Db was found ... now sending unsent data");
            UnsentDataDbHelper mDbHelper = new UnsentDataDbHelper(getApplicationContext());
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            String[] projection = new String[] { FeedEntry._ID, FeedEntry.COLUMN_NAME_EVENT_CATEGORY, FeedEntry.COLUMN_NAME_EVENT_ACTION, FeedEntry.COLUMN_NAME_EVENT_LABEL, FeedEntry.COLUMN_NAME_EVENT_VALUE, FeedEntry.COLUMN_NAME_EVENT_TIME};

            eventsList   = new ArrayList<EventDTO>();

            Cursor cursorEvents = db.query(
                    FeedEntry.TABLE_NAME, 
                    projection, // The columns to return
                    null, // The columns for the WHERE clause
                    null, // The values for the WHERE clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    null // The sort order
                    );
            if(cursorEvents.moveToFirst()){
                eventsList = prepareListFromCursor(cursorEvents);
                CLog.d(Util.TAGC26,"Unsent Events :: " + eventsList.toString());
            }
            cursorEvents.close();

            String eventListString  = eventsList.toString();

            if(eventsList.size()==0)
            {
                db.close();
                CLog.d(Util.TAGC26, "No unsent data to send. So closing connection and returning.");
                return;
            }
            
            try{

                List<NameValuePair> postData = new ArrayList<NameValuePair>();
                postData.add(new BasicNameValuePair("user_id",Util.getDeviceId(getApplicationContext())));
                postData.add(new BasicNameValuePair("package_name",URLEncoder.encode(Util.getOwnPackageName(this),"UTF-8")));
                postData.add(new BasicNameValuePair("model",Util.getDeviceName()));
                postData.add(new BasicNameValuePair("country_code",Util.getCountryCode(getApplicationContext())));
                postData.add(new BasicNameValuePair("android_id",Util.getAndroidId(getApplicationContext())));
                postData.add(new BasicNameValuePair("eT",Util.getCurrentTimeInMillis()));
                postData.add(new BasicNameValuePair("eTz",Util.getTimeZone()));
                postData.add(new BasicNameValuePair("events",eventListString));

                String URL = Util.API_URL_ANALYTICS ;

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
                CLog.d(Util.TAGC26, "The analytics URL for unsent data ::"+ URL);

                String eventIds  = buildStringFromList(eventsList);
                
                if(eventListString.length()>0){
                    String sql = "delete from "+FeedEntry.TABLE_NAME+" where "+ FeedEntry._ID +" IN ("+eventIds+")";
                    CLog.e(Util.TAGC26, "Deleted events :: "+eventIds);
                    db.execSQL(sql);
                }

            }catch (Exception e){
                CLog.e(Util.TAGC26, "Error incurred while sending unsent data :: "+e.getMessage());
            }

            db.close();
        }
    }
    private String buildStringFromList(List<EventDTO> eventList){
        String outputStr ="";
        for (EventDTO eventDTO : eventList) {
            outputStr+="'"+eventDTO.getEventId()+"',";
        }
        if(outputStr.length()>0){
            outputStr = outputStr.substring(0, outputStr.lastIndexOf(","));
        }
        return outputStr;
    }

    private List<EventDTO> prepareListFromCursor(Cursor myEventCursor){
        List<EventDTO> result = new ArrayList<EventDTO>();
        if(myEventCursor.moveToFirst()){
            do{
                EventDTO eventDTO = new EventDTO();
                eventDTO.setEventId(myEventCursor.getString(myEventCursor.getColumnIndex(FeedEntry._ID)));
                eventDTO.setEventCategory(myEventCursor.getString(myEventCursor.getColumnIndex(FeedEntry.COLUMN_NAME_EVENT_CATEGORY)));
                eventDTO.setEventAction(myEventCursor.getString(myEventCursor.getColumnIndex(FeedEntry.COLUMN_NAME_EVENT_ACTION)));
                eventDTO.setEventLabel(myEventCursor.getString(myEventCursor.getColumnIndex(FeedEntry.COLUMN_NAME_EVENT_LABEL)));
                eventDTO.setEventValue(myEventCursor.getString(myEventCursor.getColumnIndex(FeedEntry.COLUMN_NAME_EVENT_VALUE)));
                eventDTO.setEventTime(myEventCursor.getString(myEventCursor.getColumnIndex(FeedEntry.COLUMN_NAME_EVENT_TIME)));
                result.add(eventDTO);
            }while(myEventCursor.moveToNext());
        }
        return result;
    }
}