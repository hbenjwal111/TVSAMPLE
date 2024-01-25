package com.sampletv.spagreen.video_service;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import androidx.core.os.BuildCompat;
import androidx.tvprovider.media.tv.TvContractCompat;

import com.sampletv.spagreen.R;

import java.util.Arrays;
import java.util.List;

public class VideoPlaybackActivity extends Activity {
    public static final String TAG = "VideoExampleActivity";
    private static final int MAKE_BROWSABLE_REQUEST_CODE = 9001;
    public static final String EXTRA_VIDEO = "com.oxootv.spagreen.recommendations.extra.MOVIE";
    public static final String EXTRA_CHANNEL_ID =
            "com.oxootv.spagreen.recommendations.extra.CHANNEL_ID";
    public static final String EXTRA_POSITION =
            "com.oxootv.spagreen.recommendations.extra.POSITION";

    private PlaybackModel playbackModel;
    boolean channelExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_example);

       /* playbackModel = (PlaybackModel) getIntent()
                .getSerializableExtra(VideoPlaybackActivity.EXTRA_VIDEO);
*/
        createChannel();

    }

    private void createChannel() {
        Subscription movieSubscription =
                MockDatabase.getVideoSubscription(this);
        channelExists = movieSubscription.getChannelId() > 0L;
        new AddChannelTask(this).execute(movieSubscription);
    }

    private class AddChannelTask extends AsyncTask<Subscription, Void, Long> {
        private final Context context;

        public AddChannelTask(Context context) {
            this.context = context;
        }

        @Override
        protected Long doInBackground(Subscription... varArgs) {
            List<Subscription> subscriptions = Arrays.asList(varArgs);
            if (subscriptions.size() != 1) {
                return -1L;
            }
            Subscription subscription = subscriptions.get(0);
            Long channelId = TvUtil.createChannel(context, subscription);
            subscription.setChannelId(channelId);
            MockDatabase.saveSubscription(context, subscription);

            TvUtil.scheduleSyncingProgramsForChannel(getApplicationContext(), channelId);
            return channelId;
        }

        @Override
        protected void onPostExecute(Long channelId) {
            super.onPostExecute(channelId);
            if (!channelExists)
                  promptUserToDisplayChannel(channelId);
        }
    }

    private void promptUserToDisplayChannel(long channelId) {
        // TODO: step 17 prompt user.
        Intent intent = new Intent(TvContractCompat.ACTION_REQUEST_CHANNEL_BROWSABLE);
        intent.putExtra(TvContractCompat.EXTRA_CHANNEL_ID, channelId);
        try {
            this.startActivityForResult(intent, MAKE_BROWSABLE_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Could not start activity: " + intent.getAction(), e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // TODO step 18 handle response
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "Channel Added", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Channel not added", Toast.LENGTH_LONG).show();
        }
    }

}
