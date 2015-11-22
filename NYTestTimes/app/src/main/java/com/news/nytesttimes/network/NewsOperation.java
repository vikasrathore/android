package com.news.nytesttimes.network;

/**
 * Created by vikasrathour on 14/10/15.
 */
public class NewsOperation {
    private NewsOperationBundle bundle;
    private int operationId;


    public NewsOperation(int operationId) {
        this.operationId = operationId;

    }


    public NewsOperation(int operationId, NewsOperationBundle bundle) {
        super();
        this.bundle = bundle;
        this.operationId = operationId;
    }


    public int getOperationId() {
        return operationId;
    }

    public void setBundle(NewsOperationBundle bundle) {
        this.bundle = bundle;
    }


    public NewsOperationBundle getBundle() {
        return bundle;
    }

}
