package com.oxootv.spagreen.video_service;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.oxootv.spagreen.ui.activity.PlayerActivity;

/**
 * Delegates to the correct activity based on how the user entered the app.
 *
 * <p>Supports two options: view and play. The view option will open the channel for the user to be
 * able to view more programs. The play option will load the channel/program,
 * subscriptions/mediaContent start playing the movie.
 */
public class AppLinkActivity extends Activity {

    private static final String TAG = "AppLinkActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri uri = intent.getData();

        Log.v(TAG, uri.toString());

        if (uri.getPathSegments().isEmpty()) {
            Log.e(TAG, "Invalid uri " + uri);
            finish();
            return;
        }

        AppLinkHelper.AppLinkAction action = AppLinkHelper.extractAction(uri);
        switch (action.getAction()) {
            case AppLinkHelper.PLAYBACK:
                play((AppLinkHelper.PlaybackAction) action);
                break;
            case AppLinkHelper.BROWSE:
                browse((AppLinkHelper.BrowseAction) action);
                break;
            default:
                throw new IllegalArgumentException("Invalid Action " + action);
        }
    }

    private void browse(AppLinkHelper.BrowseAction action) {
        Subscription subscription =
                MockDatabase.findSubscriptionByName(this, action.getSubscriptionName());
        if (subscription == null) {
            Log.e(TAG, "Invalid subscription " + action.getSubscriptionName());
        } else {
            // TODO: Open an activity that has the movies for the subscription.
            Toast.makeText(this, action.getSubscriptionName(), Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private void play(AppLinkHelper.PlaybackAction action) {
        if (action.getPosition() == AppLinkHelper.DEFAULT_POSITION) {
            Log.d(TAG, "Playing program " + action.getMovieId()
                            + " from channel "
                            + action.getChannelId());
        } else {
            Log.d(TAG, "Continuing program "
                            + action.getMovieId()
                            + " from channel "
                            + action.getChannelId()
                            + " at time "
                            + action.getPosition());
        }

        PlaybackModel movie = MockDatabase.findMovieById(this, action.getChannelId(), action.getMovieId());
        if (movie == null) {
            Log.e(TAG, "Invalid program " + action.getMovieId());
        } else {
            startPlaying(action.getChannelId(), movie, action.getPosition());
        }
        finish();
    }

    private void startPlaying(long channelId, PlaybackModel movie, long position) {
        Intent playMovieIntent = new Intent(this, PlayerActivity.class);
        playMovieIntent.putExtra(VideoPlaybackActivity.EXTRA_VIDEO, movie);
        playMovieIntent.putExtra(VideoPlaybackActivity.EXTRA_CHANNEL_ID, channelId);
        playMovieIntent.putExtra(VideoPlaybackActivity.EXTRA_POSITION, position);
        startActivity(playMovieIntent);
    }
}
