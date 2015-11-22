package com.cube26.trendingnow;

import android.os.Bundle;
import com.cube26.istore.R;
import com.cube26.trendingnow.util.Util;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

public class FullScreenActivity extends YouTubeFailureRecoveryActivity {

    private YouTubePlayerView mVideoPlayerView;
    private YouTubePlayer mPlayer;
    private String mVideoId;
    public static final String VIDEO_KEY = "youtubevideoid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);
        mVideoPlayerView = (YouTubePlayerView) findViewById(R.id.fullscreen_player);
        Bundle bundle = getIntent().getExtras();
        mVideoId = bundle.getString(VIDEO_KEY);
        mVideoPlayerView.initialize(Util.YOUTUBE_DEVELOPER_KEY, this);
    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player,
            boolean wasRestored) {
        this.mPlayer = player; 
        if (!wasRestored) {
            mPlayer.cueVideo(mVideoId);
        }
    }

    @Override
    protected Provider getYouTubePlayerProvider() {
        return mVideoPlayerView;
    }

}
