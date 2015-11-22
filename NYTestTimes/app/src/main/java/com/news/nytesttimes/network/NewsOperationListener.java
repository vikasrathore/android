package com.news.nytesttimes.network;

/**
 * Created by vikasrathour on 30/09/15.
 */
public interface NewsOperationListener {

    void operationFailed();

    void operationSucceeded();
}
