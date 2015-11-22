package com.cube26.notificationactivationlib;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.AsyncTask;
import com.cube26.trendingnow.util.CLog;

public class SendAnalyticsDataPostRequest extends AsyncTask<String, Void, Void> {
    private List<NameValuePair> postData;

    public SendAnalyticsDataPostRequest(List<NameValuePair> pData) {
        postData = pData;
    }

    @Override
    protected Void doInBackground(String... arg0) {
        String URL = "";
        try {
            URL = Util.API_URL_ANALYTICS ;
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, Util.timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, Util.timeoutSocket);
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            HttpPost httppost = new HttpPost(URL);
            httppost.setEntity(new UrlEncodedFormEntity(postData));
            CLog.d(Util.TAGC26, "The URL being hit for analytics is ::::"+ URL);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            //send the data
            httpclient.execute(httppost, responseHandler);
        } catch (Exception e) {
            e.printStackTrace();
            CLog.d(Util.TAGC26, "Exception occured by sending analytics data by post::" + e.getMessage());
        } 
        return null;
    }
}