package com.sampletv.spagreen.video_service;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.leanback.app.VideoFragment;
import androidx.leanback.app.VideoFragmentGlueHost;
import androidx.leanback.app.VideoSupportFragment;
import androidx.leanback.app.VideoSupportFragmentGlueHost;
import androidx.leanback.media.MediaPlayerAdapter;
import androidx.leanback.media.PlaybackGlue;
import androidx.leanback.media.PlaybackTransportControlGlue;
import androidx.leanback.widget.PlaybackControlsRow;
import androidx.tvprovider.media.tv.TvContractCompat;

public class PlaybackFragment extends VideoFragment {
    private static final String TAG = PlaybackFragment.class.getSimpleName();
    private SimplePlaybackTransportControlGlue<MediaPlayerAdapter> mMediaPlayerGlue;
    private long mChannelId;
    private long mStartingPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mChannelId = getActivity().getIntent().getLongExtra(VideoPlaybackActivity.EXTRA_CHANNEL_ID, -1L);
        mStartingPosition = getActivity().getIntent().getLongExtra(VideoPlaybackActivity.EXTRA_POSITION, -1L);

        final PlaybackModel model = (PlaybackModel)
                getActivity().getIntent()
                        .getSerializableExtra(VideoPlaybackActivity.EXTRA_VIDEO);

        Log.e(TAG,"VideoUrl: " + model.getVideoUrl());
        VideoFragmentGlueHost glueHost = new VideoFragmentGlueHost(PlaybackFragment.this);
        mMediaPlayerGlue =
                new SimplePlaybackTransportControlGlue<>(
                        getActivity(), new MediaPlayerAdapter(getActivity()));
        mMediaPlayerGlue.setHost(glueHost);
        mMediaPlayerGlue.setRepeatMode(PlaybackControlsRow.RepeatAction.NONE);
        mMediaPlayerGlue.addPlayerCallback(
                new PlaybackGlue.PlayerCallback() {
                    WatchNextAdapter watchNextAdapter = new WatchNextAdapter();

                    @Override
                    public void onPlayStateChanged(PlaybackGlue glue) {
                        super.onPlayStateChanged(glue);
                        long position = mMediaPlayerGlue.getCurrentPosition();
                        long duration = mMediaPlayerGlue.getDuration();
                        Log.e(TAG, "channelId: " + mChannelId);
                        Log.e(TAG, "watchNextId: " + model.getWatchNextId());
                        watchNextAdapter.updateProgress(getContext(), mChannelId, model, position, duration);
                    }

                    @Override
                    public void onPlayCompleted(PlaybackGlue glue) {
                        super.onPlayCompleted(glue);
                        watchNextAdapter.removeFromWatchNext(getContext(), mChannelId, model.getId());
                    }
                });

        mMediaPlayerGlue.setTitle(model.getTitle());
        mMediaPlayerGlue.setSubtitle(model.getDescription());
        mMediaPlayerGlue.getPlayerAdapter().setDataSource(
                Uri.parse(model.getVideoUrl()));
        seekToStartPosition();
        playWhenReady(mMediaPlayerGlue);

    }

    private void playWhenReady(PlaybackGlue glue) {
        if (glue.isPrepared()) {
            glue.play();
        } else {
            glue.addPlayerCallback(
                    new PlaybackGlue.PlayerCallback() {
                        @Override
                        public void onPreparedStateChanged(PlaybackGlue glue) {
                            if (glue.isPrepared()) {
                                glue.removePlayerCallback(this);
                                glue.play();
                            }
                        }
                    });
        }
    }

    @Override
    public void onPause() {
        if (mMediaPlayerGlue != null) {
            mMediaPlayerGlue.pause();
        }
        super.onPause();
    }

    private void seekToStartPosition() {
        // Skip ahead if given a starting position.
        if (mStartingPosition > -1L) {
            if (mMediaPlayerGlue.isPrepared()) {
                Log.d("VideoFragment", "Is prepped, seeking to " + mStartingPosition);
                mMediaPlayerGlue.seekTo(mStartingPosition);
            } else {
                mMediaPlayerGlue.addPlayerCallback(
                        new PlaybackGlue.PlayerCallback() {
                            @Override
                            public void onPreparedStateChanged(PlaybackGlue glue) {
                                super.onPreparedStateChanged(glue);
                                if (mMediaPlayerGlue.isPrepared()) {
                                    mMediaPlayerGlue.removePlayerCallback(this);
                                    Log.d(TAG, "In callback, seeking to " + mStartingPosition);
                                    mMediaPlayerGlue.seekTo(mStartingPosition);
                                }
                            }
                        });
            }
        }
    }

}
