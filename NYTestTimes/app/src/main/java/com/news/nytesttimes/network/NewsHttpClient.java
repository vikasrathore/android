package com.news.nytesttimes.network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by vikasrathour on 30/09/15.
 */
public class NewsHttpClient {
    static NewsHttpClient newsHttpClient;
    String TAG = "NewsHttpClient";
    NewsOperationListener newsOperationListener;

    public NewsHttpClient() {
    }

    public static NewsHttpClient getInstance() {
        if (newsHttpClient == null)
            newsHttpClient = new NewsHttpClient();
        return newsHttpClient;
    }

    private String convertInputStreamToString(InputStream inputStream)
            throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    void sendNewsRequest(NewsHttpRequest newsHttpRequest, HttpResponseListener listener) {

        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        URL url = null;

        try {
            url = new URL(newsHttpRequest.getUrl());
        } catch (MalformedURLException e) {
            Log.i(TAG + "ERROR IN URL Creation", e.getMessage());
        }
        Log.i(TAG + "URL :::: ", newsHttpRequest.getUrl());
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            Log.i(TAG + "ERROR IN Open Con", e.getMessage());
        }

        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(false);
        httpURLConnection.setUseCaches(false);

        try {
            httpURLConnection.connect();
        } catch (IOException e) {
            Log.i(TAG + "ERROR IN CONNECT", e.getMessage());
        }

        String data;

        int contentLength = httpURLConnection.getContentLength();

        try {
            inputStream = httpURLConnection.getInputStream();
            data = convertInputStreamToString(inputStream);
            HttpResponseWrapper responseWrapper = new HttpResponseWrapper(httpURLConnection.getResponseCode(), data, newsHttpRequest);
            listener.processHttpResponse(responseWrapper);
        } catch (IOException e) {
            Log.i(TAG + "ERROR IN Get Stream", e.getMessage());
        } finally {
            closeConnection(httpURLConnection, inputStream, null);
            data = null;
        }

    }

    void sendImageDownloadRequest(NewsHttpRequest newsHttpRequest, HttpResponseListener listener) {

        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        URL url = null;

        try {
            url = new URL(newsHttpRequest.getUrl());
        } catch (MalformedURLException e) {

        }

        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            Log.i(TAG + "ERROR IN Open Con", e.getMessage());
        }

        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(false);
        httpURLConnection.setUseCaches(false);

        try {
            httpURLConnection.connect();
        } catch (IOException e) {
            Log.i(TAG + "ERROR IN CONNECT", e.getMessage());
        }


        int contentLength = httpURLConnection.getContentLength();

        try {
            inputStream = httpURLConnection.getInputStream();
        } catch (IOException e) {
            Log.i(TAG + "ERROR IN Get Stream", e.getMessage());
        }
        int progress = 0;
        byte[] data;
        int bufferSize = NewsHttpUtil.getNormalisedBufferSize(contentLength);

        try {
            if (contentLength <= bufferSize) {
                byte[] buffer = new byte[1024];
                int bytesReadCount;
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                while ((bytesReadCount = inputStream.read(buffer)) != -1) {

                    outputStream.write(buffer, 0, bytesReadCount);

                }

                data = outputStream.toByteArray();
                HttpResponseWrapper responseWrapper = new HttpResponseWrapper(httpURLConnection.getResponseCode(), data, newsHttpRequest);
                listener.processHttpResponse(responseWrapper);

            } else {
                byte[] buffer = new byte[1024];
                int bytesReadCount;
                double p = (((double) bufferSize / (double) contentLength)) * 100;
                int percent = (int) p;
                progress = percent;
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                while ((bytesReadCount = inputStream.read(buffer)) != -1) {

                    outputStream.write(buffer, 0, bytesReadCount);
                    progress = progress + percent;
                }

                data = outputStream.toByteArray();
                HttpResponseWrapper responseWrapper = new HttpResponseWrapper(httpURLConnection.getResponseCode(), data, newsHttpRequest);
                listener.processHttpResponse(responseWrapper);
            }
        }
         catch (NullPointerException npe)
         {

         }
        catch (IOException e) {
            newsOperationListener.operationFailed();
            e.printStackTrace();
        } finally {
            closeConnection(httpURLConnection, inputStream, null);
            data = null;
        }

    }

    private void closeConnection(HttpURLConnection con, InputStream is,
                                 OutputStream os) {
        if (con != null) {
            try {
                con.disconnect();
            } catch (Exception e) {
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
        if (os != null) {
            try {
                os.close();
            } catch (Exception e) {
            }
        }
    }


}
