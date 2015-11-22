package com.news.nytesttimes.network;

/**
 * Created by vikasrathour on 14/10/15.
 */
public interface TestOperationListener {
    public void onOperationFinished(NewsOperation operation);

    public void onOperationFailed(NewsOperation operation);
}
