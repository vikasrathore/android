package com.news.nytesttimes.network;

/**
 * Created by vikasrathour on 30/09/15.
 */
public class NewsHttpRequest {

    static final int GET = 0;
    static final int POST = 1;
    static final int PUT = 2;


    private int requestOperationID;

    private boolean requireAuthentication = false;

    public boolean isRequireAuthentication() {
        return requireAuthentication;
    }

    private int requestID;

    private int imageNewsType;

    public int getImageNewsType() {
        return imageNewsType;
    }

    public void setImageNewsType(int imageNewsType) {
        this.imageNewsType = imageNewsType;
    }

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public NewsHttpRequest() {
    }

    public void setRequireAuthentication(boolean requireAuthentication) {
        this.requireAuthentication = requireAuthentication;
    }

    public int getRequestOperationID() {
        return requestOperationID;
    }

    public NewsHttpRequest(int requestID, String url, int requestType,
                           String requestData) {
        super();
        this.requestOperationID = requestID;
        this.url = url;
        this.requestType = requestType;
        this.requestData = requestData;
    }

    public void setRequestOperationID(int requestID) {
        this.requestOperationID = requestID;
    }


    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    private int requestType;
    private String requestData;
}
