package com.news.nytesttimes.network;


public class HttpResponseWrapper {


    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public HttpResponseWrapper(int responseCode, String responseBody,
                               NewsHttpRequest request) {
        super();
        this.responseCode = responseCode;
        this.responseBody = responseBody;
        this.request = request;
    }


    public HttpResponseWrapper(int responseCode, byte[] data,
                               NewsHttpRequest request) {
        super();
        this.responseCode = responseCode;
        this.data = data;
        this.request = request;
    }

    int responseCode;

    private String responseBody;

    byte[] data;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    private NewsHttpRequest request;

    public NewsHttpRequest getRequest() {
        return request;
    }

    public void setRequest(NewsHttpRequest request) {
        this.request = request;
    }


}
