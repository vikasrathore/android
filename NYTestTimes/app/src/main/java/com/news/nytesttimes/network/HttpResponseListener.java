package com.news.nytesttimes.network;

public interface HttpResponseListener {

    abstract void processHttpResponse(HttpResponseWrapper response);

}
