/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.notificationactivationlib;

import java.net.URLEncoder;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.cube26.trendingnow.util.CLog;
import com.cube26.trendingnow.util.Util;
import com.cube26.notificationactivationlib.UnsentDataContract.FeedEntry;

public class SendAnalyticsData extends AsyncTask<String, Void, Void> {
    private Context ctx = null;
    private EventDTO eventToSend;
    private static String Analaytics_URL="http://ta.cube26intex-analytics.in/analytics";
    public SendAnalyticsData(Context context, EventDTO event) {
        ctx = context;
        eventToSend = event;
    }

    @Override
    protected Void doInBackground(String... arg0) {
        Boolean isError = false;
        String URL = "";
        try {
            URL = Analaytics_URL + "?user_id=" 
                    + URLEncoder.encode(Util.getDeviceId(ctx), "UTF-8")
                    + "&android_id=" + URLEncoder.encode(Util.getAndroidId(ctx), "UTF-8")
                    + "&country_code=" + URLEncoder.encode(Util.getCountryCode(ctx), "UTF-8")
                    + "&model=" +URLEncoder.encode( Util.getDeviceName(), "UTF-8")
                    + "&mcc=" + URLEncoder.encode(Util.getMccCode(ctx),"UTF-8")
					+ "&mnc=" + URLEncoder.encode(Util.getMncCode(ctx),"UTF-8")
					+ "&et=" + URLEncoder.encode(Util.getCurrentTimeInMillis(),"UTF-8")
					+ "&eTz=" + URLEncoder.encode(Util.getTimeZone(),"UTF-8")+ 
					"&app_version="+URLEncoder.encode(Util.getOwnAppVersion(ctx),
							"UTF-8")
					
                    + "&package_name="+ URLEncoder.encode(Util.getPackageName(ctx), "UTF-8")
                    + "&event="+URLEncoder.encode(eventToSend.toString(),"UTF-8");
            		

            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, Util.timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, Util.timeoutSocket);
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            HttpGet httpget = new HttpGet(URL);
            CLog.d(Util.TAGC26, "The URL being hit for analytics is ::::"+ URL);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            //send the data
            httpclient.execute(httpget, responseHandler);
        } catch (Exception e) {
            e.printStackTrace();
            CLog.d(Util.TAGC26, "Exception ::" + e.getMessage());
            isError = true;
        } finally {
            if (isError) {
                CLog.d(Util.TAGC26, "unable to send data to server, storing in db");
                UnsentDataDbHelper mDbHelper = new UnsentDataDbHelper(ctx);
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(FeedEntry.COLUMN_NAME_EVENT_CATEGORY, eventToSend.getEventCategory());
                values.put(FeedEntry.COLUMN_NAME_EVENT_ACTION, eventToSend.getEventAction());
                values.put(FeedEntry.COLUMN_NAME_EVENT_LABEL, eventToSend.getEventLabel());
                values.put(FeedEntry.COLUMN_NAME_EVENT_VALUE, eventToSend.getEventValue());
                values.put(FeedEntry.COLUMN_NAME_EVENT_TIME, eventToSend.getEventTime());
                db.insert(FeedEntry.TABLE_NAME, null, values);

                // Insert the new row, returning the primary key value of the new row
                db.close();
            }
        }
        return null;
    }
}