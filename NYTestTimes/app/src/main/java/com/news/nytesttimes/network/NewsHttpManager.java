package com.news.nytesttimes.network;


import com.news.nytesttimes.datatypes.DataController;
import com.news.nytesttimes.services.NewsService;
import com.news.nytesttimes.util.Keys;

/**
 * Created by vikasrathour on 30/09/15.
 */

public class NewsHttpManager implements HttpResponseListener {

    static NewsHttpManager newsHttpManager;
    public final static int DOWNLOAD_NEWS = 1;
    public final static int DOWNLOAD_NEWS_IMAGE = 2;
    public final static int DOWNLOAD_NEWS_TOP_STORIES = 3;
    public static final String RESPONSE_CODE = "response_code";


    public NewsHttpManager() {
    }

    public static NewsHttpManager getInstance() {
        if (newsHttpManager == null)

            newsHttpManager = new NewsHttpManager();
        return newsHttpManager;

    }


    private void sendHttpRequest(NewsHttpRequest httpRequest) {
        NewsHttpClient.getInstance().sendNewsRequest(httpRequest, this);

    }


    private void sendImageHttpRequest(NewsHttpRequest httpRequest) {
        NewsHttpClient.getInstance().sendImageDownloadRequest(httpRequest, this);

    }

    String TYPE_JSON = ".json";

    //http://api.nytimes.com/svc/search/v2/articlesearch.json?
// callback=svc_search_v2_articlesearch&q=earthquake&api-key=eddabc03f51de7a01ab85aaa2e77ef18:0:73306685
    public void sendArticleSearchDownloadRequest(TestOperationListener listener, String queryString) {
        this.listener = listener;
        NewsHttpRequest newsHttpRequest = new NewsHttpRequest();
        newsHttpRequest.setRequestOperationID(DOWNLOAD_NEWS);
        newsHttpRequest.setUrl(queryString + TYPE_JSON + "?" + "api-key=" + Keys.MostPopularAPI_Key);
        newsHttpRequest.setRequestType(NewsHttpRequest.GET);
        sendHttpRequest(newsHttpRequest);
    }

    public void sendTopStorieshDownloadRequest(TestOperationListener listener, String queryString) {
        this.listener = listener;
        NewsHttpRequest newsHttpRequest = new NewsHttpRequest();
        newsHttpRequest.setRequestOperationID(DOWNLOAD_NEWS_TOP_STORIES);
        newsHttpRequest.setUrl(queryString + TYPE_JSON + "?" + "api-key=" + Keys.TopStoriesAPI_Key);
        newsHttpRequest.setRequestType(NewsHttpRequest.GET);
        sendHttpRequest(newsHttpRequest);
    }

    TestOperationListener listener;

    public void sendImageDownloadRequest(String url, int newsID, TestOperationListener listener, int type) {
        this.listener = listener;
        NewsHttpRequest newsHttpRequest = new NewsHttpRequest();
        newsHttpRequest.setRequestOperationID(DOWNLOAD_NEWS_IMAGE);
        newsHttpRequest.setUrl(url);
        newsHttpRequest.setRequestType(NewsHttpRequest.GET);
        newsHttpRequest.setRequestID(newsID);
        newsHttpRequest.setImageNewsType(type);
        sendImageHttpRequest(newsHttpRequest);
    }

    @Override
    public void processHttpResponse(HttpResponseWrapper responseWrapper) {

        switch (responseWrapper.getRequest().getRequestOperationID()) {
            case DOWNLOAD_NEWS:
                processNewsResponse(responseWrapper);
                break;
            case DOWNLOAD_NEWS_IMAGE:
                processImageDownloadResponse(responseWrapper);
                break;
            case DOWNLOAD_NEWS_TOP_STORIES:
                processNewsResponse(responseWrapper);
                break;
            default:

        }

    }

    private void processNewsResponse(HttpResponseWrapper responseWrapper) {
        if (responseWrapper.getRequest().getRequestOperationID() == DOWNLOAD_NEWS)
            DataController.getInstance().setArticles(responseWrapper.getResponseBody());
        else if (responseWrapper.getRequest().getRequestOperationID() == DOWNLOAD_NEWS_TOP_STORIES)
            DataController.getInstance().setTopStories(responseWrapper.getResponseBody());
        NewsOperationBundle obBundle = new NewsOperationBundle();
        obBundle.putInt(RESPONSE_CODE, responseWrapper.getResponseCode());
        NewsOperation o = new NewsOperation(
                responseWrapper.getRequest().getRequestOperationID(), obBundle);
        listener.onOperationFinished(o);
    }


    private void processImageDownloadResponse(HttpResponseWrapper responseWrapper) {
        // DataController.getInstance().setTrains(responseWrapper.getResponseBody());
        NewsOperationBundle obBundle = new NewsOperationBundle();
        obBundle.putInt(RESPONSE_CODE, responseWrapper.getResponseCode());
        obBundle.putInt(NewsService.EXTRA_NEWS_ID, responseWrapper.getRequest().getRequestID());
        NewsOperation o = new NewsOperation(
                DOWNLOAD_NEWS_IMAGE, obBundle);

        if (responseWrapper.getRequest().getImageNewsType() == NewsService.EXTRA_NEWS_POPULAR)
            DataController.getInstance().getNews().get(responseWrapper.getRequest().getRequestID()).setImageByteArray(responseWrapper.getData());
        else if (responseWrapper.getRequest().getImageNewsType() == NewsService.EXTRA_NEWS_TOP_STOROIES)
            DataController.getInstance().getTopStories().get(responseWrapper.getRequest().getRequestID()).setImageByteArray(responseWrapper.getData());
        listener.onOperationFinished(o);
    }

}
