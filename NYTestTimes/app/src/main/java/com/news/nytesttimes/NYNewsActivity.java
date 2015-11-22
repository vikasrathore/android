package com.news.nytesttimes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.news.nytesttimes.datatypes.DataController;
import com.news.nytesttimes.datatypes.News;
import com.news.nytesttimes.network.NewsHttpManager;
import com.news.nytesttimes.network.NewsOperation;
import com.news.nytesttimes.network.TestOperationListener;
import com.news.nytesttimes.services.NewsService;

import java.util.ArrayList;
import java.util.Locale;

public class NYNewsActivity extends AppCompatActivity implements ActionBar.TabListener, TestOperationListener, TopStoriesFragment.OnFragmentInteractionListener, PopularFragment.OnFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    PopularFragment most;
    TopStoriesFragment topStoriesFragment1;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news1);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_news1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    @Override
    public void onTopStoriesItemClicked(int index,String url) {
        if (url!=null)
            launchBrowser(url);
    }

    @Override
    public void onPopularFragmentInteraction(int index,String url) {
        if (url!=null)
        launchBrowser(url);
    }

    public void launchBrowser(String url)
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 0) {
                if (most == null)
                    most = PopularFragment.newInstance("Hi", "Hi");
                return most;
            }
            if (position == 1) {
                if (topStoriesFragment1 == null)
                    topStoriesFragment1 = TopStoriesFragment.newInstance("Bye", "Bye");
                return topStoriesFragment1;
            }

            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.most_popular);
                case 1:
                    return getString(R.string.top_stories);
            }
            return null;
        }
    }

    @Override
    public void onOperationFinished(NewsOperation operation) {

        if (operation.getOperationId() == NewsHttpManager.DOWNLOAD_NEWS) {
            downloadImages(operation.getOperationId());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    most.adapter.setNews(DataController.getInstance().getNews());
                    most.recyclerView.setAdapter(most.adapter);
                }
            });
        } else if (operation.getOperationId() == NewsHttpManager.DOWNLOAD_NEWS_TOP_STORIES) {
            downloadImages(operation.getOperationId());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    topStoriesFragment1.adapter.setNews(DataController.getInstance().getTopStories());
                    topStoriesFragment1.recyclerView.setAdapter(topStoriesFragment1.adapter);
                }
            });
        } else {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    topStoriesFragment1.adapter.setNews(DataController.getInstance().getTopStories());
                    topStoriesFragment1.recyclerView.setAdapter(topStoriesFragment1.adapter);
                    most.adapter.setNews(DataController.getInstance().getNews());
                    most.recyclerView.setAdapter(most.adapter);
                }
            });
        }


    }

    @Override
    public void onOperationFailed(NewsOperation operation) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NYNewsActivity.this, "Try Again later", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void downloadImages(int type) {
        ArrayList<News> news = null;
        int size = 0;
        if (type == NewsHttpManager.DOWNLOAD_NEWS) {
            news = DataController.getInstance().getNews();
            size = news.size();
            for (int i = 0; i < size; i++) {
                if (news.get(i).getThumbnail_URL() != null && !news.get(i).getThumbnail_URL().equals(""))
                    NewsService.startImageDownloadAction(this, news.get(i).getThumbnail_URL(), news.get(i).getNewsID(), NewsService.EXTRA_NEWS_POPULAR);

            }
        } else if (type == NewsHttpManager.DOWNLOAD_NEWS_TOP_STORIES) {
            news = DataController.getInstance().getTopStories();
            size = news.size();
            for (int i = 0; i < size; i++) {
                if (news.get(i).getThumbnail_URL() != null && !news.get(i).getThumbnail_URL().equals(""))
                    NewsService.startImageDownloadAction(this, news.get(i).getThumbnail_URL(), news.get(i).getNewsID(), NewsService.EXTRA_NEWS_TOP_STOROIES);

            }
        }


    }

}
