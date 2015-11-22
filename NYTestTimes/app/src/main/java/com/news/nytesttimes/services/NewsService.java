package com.news.nytesttimes.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.news.nytesttimes.NYNewsActivity;
import com.news.nytesttimes.network.NewsHttpManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NewsService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_NEWS = "NEWS";
    private static final String ACTION_NEWS_IMAGE = "NEWS_IMAGE";
    private static final String ACTION_NEWS_TOP_STORIES = "TOP_STORIES";

    // TODO: Rename parameters
    private static final String EXTRA_QUERY = "source";
    private static final String EXTRA_IMAGE_URL = "url";
    public static final String EXTRA_NEWS_ID = "newsid";
    public static final int EXTRA_NEWS_POPULAR = 0;
    public static final int EXTRA_NEWS_TOP_STOROIES = 1;
    private static final String NEWS_TYPE = "type";
    private static Context listener;
    String TAG = "TestIntentService";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startSearchTopStoriesction(Context context, String query) {
        Intent intent = new Intent(context, NewsService.class);
        listener = context;
        intent.setAction(ACTION_NEWS_TOP_STORIES);
        intent.putExtra(EXTRA_QUERY, query);

        context.startService(intent);
    }

    public static void startSearchNewsAction(Context context, String query) {
        Intent intent = new Intent(context, NewsService.class);
        listener = context;
        intent.setAction(ACTION_NEWS);
        intent.putExtra(EXTRA_QUERY, query);

        context.startService(intent);
    }


    public static void startImageDownloadAction(Context context, String url, int newsID, int type) {
        Intent intent = new Intent(context, NewsService.class);
        listener = context;
        intent.setAction(ACTION_NEWS_IMAGE);
        intent.putExtra(EXTRA_NEWS_ID, newsID);
        intent.putExtra(NEWS_TYPE, type);
        intent.putExtra(EXTRA_IMAGE_URL, url);
        context.startService(intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Image downloaded");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "  onStartCommand ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "  onStart ");
        super.onStart(intent, startId);
    }


    public NewsService() {
        super("TestIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {

            final String action = intent.getAction();
            if (ACTION_NEWS.equals(action)) {
                final String source = intent.getStringExtra(EXTRA_QUERY);
                NewsHttpManager.getInstance().sendArticleSearchDownloadRequest((NYNewsActivity) listener, source);
            } else if (ACTION_NEWS_TOP_STORIES.equals(action)) {
                final String source = intent.getStringExtra(EXTRA_QUERY);
                NewsHttpManager.getInstance().sendTopStorieshDownloadRequest((NYNewsActivity) listener, source);
            } else if (ACTION_NEWS_IMAGE.equals(action)) {
                final String url = intent.getStringExtra(EXTRA_IMAGE_URL);
                final int id = intent.getIntExtra(EXTRA_NEWS_ID, 0);

                NewsHttpManager.getInstance().sendImageDownloadRequest(url, id, (NYNewsActivity) listener, intent.getIntExtra(NEWS_TYPE, 0));
            }

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, " onDestroy ");
    }

    @Override
    public boolean stopService(Intent name) {
        Log.i(TAG, " stopService ");
        return super.stopService(name);

    }
}
