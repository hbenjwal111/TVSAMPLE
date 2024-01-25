package com.sampletv.spagreen.ui.activity;

import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.tvprovider.media.tv.TvContractCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import com.sampletv.spagreen.R;
import com.sampletv.spagreen.adapter.ServerAdapter;
import com.sampletv.spagreen.adapter.SubtitleListAdapter;
import com.sampletv.spagreen.database.DatabaseHelper;
import com.sampletv.spagreen.model.Subtitle;
import com.sampletv.spagreen.model.Videos;
import com.sampletv.spagreen.utils.PreferenceUtils;
import com.sampletv.spagreen.utils.ToastMsg;
import com.sampletv.spagreen.video_service.MediaSessionHelper;
import com.sampletv.spagreen.video_service.PlaybackModel;
import com.sampletv.spagreen.video_service.VideoPlaybackActivity;
import com.sampletv.spagreen.video_service.WatchNextAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PlayerActivity extends Activity {
    private static final String TAG = "PlayerActivity";
    private static final String CLASS_NAME = "com.oxoo.spagreen.ui.activity.PlayerActivity";
    private static final int MAKE_BROWSABLE_REQUEST_CODE = 9001;
    boolean doubleBackToExitPressedOnce = false;
    boolean channelExists;
    private PlayerView exoPlayerView;
    private SimpleExoPlayer player;
    private RelativeLayout rootLayout;
    private MediaSource mediaSource;
    private boolean isPlaying;
    private List<Videos> videosses = new ArrayList<>();
    private Videos videoss = null;
    private String url = "";
    private String videoType = "";
    private String category = "";
    private int visible;
    private ImageButton serverButton, fastForwardButton, subtitleButton;
    private TextView movieTitleTV, movieDescriptionTV;
    private ImageView posterImageView;
    private RelativeLayout seekBarLayout;
    private TextView liveTvTextInController;
    private ProgressBar progressBar;
    private PowerManager.WakeLock wakeLock;
    private MediaSession session;
    private long mChannelId;
    private long mStartingPosition;
    private PlaybackModel model;
    private MediaSessionHelper mediaSessionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mChannelId = getIntent().getLongExtra(VideoPlaybackActivity.EXTRA_CHANNEL_ID, -1L);
        mStartingPosition = getIntent().getLongExtra(VideoPlaybackActivity.EXTRA_POSITION, -1L);

        model = (PlaybackModel) getIntent().getSerializableExtra(VideoPlaybackActivity.EXTRA_VIDEO);

        assert model != null;
        url = model.getVideoUrl();
        videoType = model.getVideoType();
        category = model.getCategory();
        if (model.getVideo() != null)
            //videoss = model.getVideo();
        if (model.getCategory().equals("movie") && mChannelId > -1L && model.getIsPaid().equals("1")) {
            //Paid Content from Channel
            //check user has subscription or not
            //if not, send user to VideoDetailsActivity
            DatabaseHelper db = new DatabaseHelper(PlayerActivity.this);
            final String status = db.getActiveStatusData().getStatus();
            if (!status.equals("active") || !PreferenceUtils.isValid(PlayerActivity.this)) {
                Intent intent = new Intent(PlayerActivity.this, VideoDetailsActivity.class);
                intent.putExtra("type", model.getCategory());
                intent.putExtra("id", model.getMovieId());
                intent.putExtra("thumbImage", model.getCardImageUrl());
                startActivity(intent, null);
                finish();
            }
        }

        intiViews();

      //  initVideoPlayer(url, videoType);
    }

    private void intiViews() {
        progressBar = findViewById(R.id.progress_bar);
        exoPlayerView = findViewById(R.id.player_view);
        rootLayout = findViewById(R.id.root_layout);
        movieTitleTV = findViewById(R.id.movie_title);
        movieDescriptionTV = findViewById(R.id.movie_description);
        posterImageView = findViewById(R.id.poster_image_view);
        serverButton = findViewById(R.id.img_server);
        subtitleButton = findViewById(R.id.img_subtitle);
        fastForwardButton = findViewById(R.id.exo_ffwd);
        liveTvTextInController = findViewById(R.id.live_tv);
        seekBarLayout = findViewById(R.id.seekbar_layout);
        if (category.equalsIgnoreCase("tv")) {
            serverButton.setVisibility(View.GONE);
            subtitleButton.setVisibility(View.GONE);
            //seekBarLayout.setVisibility(View.GONE);
            fastForwardButton.setVisibility(View.GONE);
            liveTvTextInController.setVisibility(View.VISIBLE);
        }

        if (category.equalsIgnoreCase("tvseries")) {
            serverButton.setVisibility(View.GONE);
            //hide subtitle button if there is no subtitle
            if (videoss != null) {
                if (videoss.getSubtitle().isEmpty()) {
                    subtitleButton.setVisibility(View.GONE);
                }
            } else {
                subtitleButton.setVisibility(View.GONE);
            }
        }

        if (category.equalsIgnoreCase("movie")) {
            if (model.getVideoList() != null)
                videosses.clear();
           // videosses = model.getVideoList();
            //hide subtitle button if there is no subtitle
            if (videoss != null) {
                if (videoss.getSubtitle().isEmpty()) {
                    subtitleButton.setVisibility(View.GONE);
                }
            } else {
                subtitleButton.setVisibility(View.GONE);
            }
            if (videosses != null) {
                if (videosses.size() < 1)
                    serverButton.setVisibility(View.GONE);
            }

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "My Tag:");

        subtitleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //open subtitle dialog
                openSubtitleDialog();
            }
        });

        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open server dialog
                openServerDialog(videosses);
            }
        });


        //set title, description and poster in controller layout
        movieTitleTV.setText(model.getTitle());
        movieDescriptionTV.setText(model.getDescription());
        Picasso.get()
                .load(model.getCardImageUrl())
                .placeholder(R.drawable.poster_placeholder)
                .centerCrop()
                .resize(120, 200)
                .error(R.drawable.poster_placeholder)
                .into(posterImageView);
    }

    @Override
    protected void onUserLeaveHint() {
        Log.e("RemoteKey", "DPAD_HOME");
        /** Use pressed home button **/
        //time to set media session active
        super.onUserLeaveHint();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_MOVE_HOME:

                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (!exoPlayerView.isControllerVisible()) {
                    exoPlayerView.showController();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                Log.e("RemoteKey", "DPAD_DOWN");
                if (!exoPlayerView.isControllerVisible()) {
                    exoPlayerView.showController();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                Log.e("RemoteKey", "DPAD_RIGHT");
                if (!exoPlayerView.isControllerVisible()) {
                    exoPlayerView.showController();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                Log.e("RemoteKey", "DPAD_LEFT");
                if (!exoPlayerView.isControllerVisible()) {
                    exoPlayerView.showController();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                Log.e("RemoteKey", "DPAD_CENTER");
                if (!exoPlayerView.isControllerVisible()) {
                    exoPlayerView.showController();
                }
                break;
            case KeyEvent.KEYCODE_BACK:
                Log.e("RemoteKey", "DPAD_BACK");
                if (exoPlayerView.isControllerVisible()) {
                    exoPlayerView.hideController();
                } else {
                    if (doubleBackToExitPressedOnce) {
                        //releasePlayer();
                        mediaSessionHelper.stopMediaSession();
                        finish();
                    } else {
                        handleBackPress();
                    }
                }

                break;
            case KeyEvent.KEYCODE_ESCAPE:
                Log.e("RemoteKey", "DPAD_ESCAPE");
               /* if (!exoPlayerView.isControllerVisible()){
                    exoPlayerView.showController();
                }else {
                    releasePlayer();
                    finish();
                }*/
                break;
        }
        return false;
    }

    private void handleBackPress() {
        this.doubleBackToExitPressedOnce = true;
        //Toast.makeText(this, "Please click BACK again to exit.", Toast.LENGTH_SHORT).show();
        new ToastMsg(PlayerActivity.this).toastIconSuccess("Please click BACK again to exit.");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

    }

    private void openServerDialog(List<Videos> videosses) {
        if (videosses != null) {
            List<Videos> videossList = new ArrayList<>();
            videossList.clear();

            for (Videos videoss : videosses) {
                if (!videoss.getFileType().equalsIgnoreCase("embed")) {
                    videossList.add(videoss);
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
            View view = LayoutInflater.from(PlayerActivity.this).inflate(R.layout.layout_server_tv, null);
            RecyclerView serverRv = view.findViewById(R.id.serverRv);
            ServerAdapter serverAdapter = new ServerAdapter(PlayerActivity.this, videossList, "movie");
            serverRv.setLayoutManager(new LinearLayoutManager(PlayerActivity.this));
            serverRv.setHasFixedSize(true);
            serverRv.setAdapter(serverAdapter);

            Button closeBt = view.findViewById(R.id.close_bt);

            builder.setView(view);

            final AlertDialog dialog = builder.create();
            dialog.show();

            closeBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            final ServerAdapter.OriginalViewHolder[] viewHolder = {null};
            serverAdapter.setOnItemClickListener(new ServerAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(View view, Videos obj, int position, ServerAdapter.OriginalViewHolder holder) {
                    Intent playerIntent = new Intent(PlayerActivity.this, PlayerActivity.class);
                    PlaybackModel video = new PlaybackModel();
                    video.setId(model.getId());
                    video.setTitle(model.getTitle());
                    video.setDescription(model.getDescription());
                    video.setCategory("movie");
                    video.setVideo(obj);
                    video.setVideoList(model.getVideoList());
                    video.setVideoUrl(obj.getFileUrl());
                    video.setVideoType(obj.getFileType());
                    video.setBgImageUrl(model.getBgImageUrl());
                    video.setCardImageUrl(model.getCardImageUrl());
                    video.setIsPaid(model.getIsPaid());

                    playerIntent.putExtra(VideoPlaybackActivity.EXTRA_VIDEO, video);

                    startActivity(playerIntent);
                    dialog.dismiss();
                    finish();
                }
            });
        } else {
            new ToastMsg(this).toastIconError(getString(R.string.no_other_server_found));
        }
    }

    private void openSubtitleDialog() {
        if (videoss != null) {
            if (!videoss.getSubtitle().isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
                View view = LayoutInflater.from(PlayerActivity.this).inflate(R.layout.layout_subtitle_dialog, null);
                RecyclerView serverRv = view.findViewById(R.id.serverRv);
                SubtitleListAdapter adapter = new SubtitleListAdapter(PlayerActivity.this, videoss.getSubtitle());
                serverRv.setLayoutManager(new LinearLayoutManager(PlayerActivity.this));
                serverRv.setHasFixedSize(true);
                serverRv.setAdapter(adapter);

                Button closeBt = view.findViewById(R.id.close_bt);

                builder.setView(view);
                final AlertDialog dialog = builder.create();
                dialog.show();

                closeBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                //click event
                adapter.setListener(new SubtitleListAdapter.OnSubtitleItemClickListener() {
                    @Override
                    public void onSubtitleItemClick(View view, Subtitle subtitle, int position, SubtitleListAdapter.SubtitleViewHolder holder) {
                    //    setSelectedSubtitle(mediaSource, subtitle.getUrl());
                        dialog.dismiss();
                    }
                });

            } else {
                new ToastMsg(this).toastIconError(getResources().getString(R.string.no_subtitle_found));
            }
        } else {
            new ToastMsg(this).toastIconError(getResources().getString(R.string.no_subtitle_found));
        }
    }

   /* private void setSelectedSubtitle(MediaSource mediaSource, String url) {
        MergingMediaSource mergedSource;
        if (url != null) {
            Uri subtitleUri = Uri.parse(url);

            Format subtitleFormat = Format.createTextSampleFormat(
                    null, // An identifier for the track. May be null.
                    MimeTypes.TEXT_VTT, // The mime type. Must be set correctly.
                    Format.NO_VALUE, // Selection flags for the track.
                    "en"); // The subtitle language. May be null.

            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(PlayerActivity.this,
                    Util.getUserAgent(PlayerActivity.this, CLASS_NAME), new DefaultBandwidthMeter());


            MediaSource subtitleSource = new SingleSampleMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(subtitleUri, subtitleFormat, C.TIME_UNSET);


            mergedSource = new MergingMediaSource(mediaSource, subtitleSource);
            player.prepare(mergedSource, false, false);
            player.setPlayWhenReady(true);
            //resumePlayer();

        } else {
            Toast.makeText(PlayerActivity.this, "there is no subtitle", Toast.LENGTH_SHORT).show();
        }
    }

    public void initVideoPlayer(String url, String type) {
        if (player != null) {
            player.release();
        }

        progressBar.setVisibility(VISIBLE);
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new
                AdaptiveTrackSelection.Factory(bandwidthMeter);

        TrackSelector trackSelector = new
                DefaultTrackSelector(videoTrackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance((Context) PlayerActivity.this, trackSelector);
        exoPlayerView.setPlayer(player);
        // below 2 lines will make screen size to fit
        exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        player.setPlayWhenReady(true);

        Uri uri = Uri.parse(url);

        switch (type) {
            case "hls":
                mediaSource = hlsMediaSource(uri, PlayerActivity.this);
                break;
            case "youtube":
                extractYoutubeUrl(url, PlayerActivity.this, 18);
                break;
            case "youtube-live":
                extractYoutubeUrl(url, PlayerActivity.this, 133);
                break;
            case "rtmp":
                mediaSource = rtmpMediaSource(uri);
                break;
            default:
                mediaSource = mediaSource(uri, PlayerActivity.this);
                break;
        }

        if (!type.contains("youtube")) {
            player.prepare(mediaSource, true, false);
            exoPlayerView.setPlayer(player);
            player.setPlayWhenReady(true);
        }

        seekToStartPosition();

        player.addListener(new Player.DefaultEventListener() {
            final WatchNextAdapter watchNextAdapter = new WatchNextAdapter();

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    isPlaying = true;
                    progressBar.setVisibility(View.GONE);
                    createChannel();
                    //create media session
                    mediaSessionHelper = new MediaSessionHelper(player, PlayerActivity.this, model, isPlaying);
                    mediaSessionHelper.updateMetadata();
                    mediaSessionHelper.updatePlaybackState();

                } else if (playbackState == Player.STATE_READY) {
                    isPlaying = false;
                    progressBar.setVisibility(View.GONE);
                    //add watch next card
                    long position = player.getCurrentPosition();
                    long duration = player.getDuration();
                    mediaSessionHelper.updateMetadata();
                    mediaSessionHelper.updatePlaybackState();

                    Log.e("123456", "id: " + model.getId());
                    watchNextAdapter.updateProgress(PlayerActivity.this, 119, model, position, duration);

                } else if (playbackState == Player.STATE_BUFFERING) {
                    isPlaying = false;
                    progressBar.setVisibility(VISIBLE);
                    player.setPlayWhenReady(true);

                } else if (playbackState == Player.STATE_ENDED) {
                    //remove now playing card
                    mediaSessionHelper.stopMediaSession();
                    //mediaSessionHelper.stopMediaSession();
                    watchNextAdapter.removeFromWatchNext(PlayerActivity.this, 119, model.getId());
                } else {
                    // player paused in any state
                    isPlaying = false;
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        exoPlayerView.setControllerVisibilityListener(new PlayerControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                visible = visibility;
            }
        });
    }

    private void seekToStartPosition() {
        // Skip ahead if given a starting position.
        if (mStartingPosition > -1L) {
            if (player.getPlayWhenReady()) {
                Log.d("VideoFragment", "Is prepped, seeking to " + mStartingPosition);
                player.seekTo(mStartingPosition);
            }
        }
    }

    private MediaSource mp3MediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), "ExoplayerDemo");
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        Handler mainHandler = new Handler();
        return new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, mainHandler, null);
    }

    private MediaSource mediaSource(Uri uri, Context context) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer")).
                createMediaSource(uri);
    }

    private MediaSource rtmpMediaSource(Uri uri) {
        MediaSource videoSource = null;

        RtmpDataSourceFactory dataSourceFactory = new RtmpDataSourceFactory();
        videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);

        return videoSource;
    }

    @SuppressLint("StaticFieldLeak")
    private void extractYoutubeUrl(String url, final Context context, final int tag) {

        new YouTubeExtractor(context) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles != null) {
                    int itag = tag;
                    String dashUrl = ytFiles.get(itag).getUrl();

                    try {
                        MediaSource source = mediaSource(Uri.parse(dashUrl), context);
                        player.prepare(source, true, false);
                        //player.setPlayWhenReady(false);
                        exoPlayerView.setPlayer(player);
                        player.setPlayWhenReady(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.extract(url, true, true);

    }

    private MediaSource hlsMediaSource(Uri uri, Context context) {

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "oxoo"), bandwidthMeter);

        MediaSource videoSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);

        return videoSource;

    }

    @Override
    public void onBackPressed() {
        if (visible == View.GONE) {
            exoPlayerView.showController();
        } else {
            releasePlayer();
            super.onBackPressed();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        if (wakeLock != null)
            wakeLock.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        wakeLock.acquire(10 * 60 * 1000L *//*10 minutes*//*);
    }

    private void releasePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.stop();
            player.release();
            player = null;
            exoPlayerView.setPlayer(null);
        }
    }

    private long getChannelId() {
        return mChannelId;
    }

    private void createChannel() {
        Subscription movieSubscription =
                MockDatabase.getVideoSubscription(this);
        channelExists = movieSubscription.getChannelId() > 0L;
        new AddChannelTask(this).execute(movieSubscription);
    }

    private void promptUserToDisplayChannel(long channelId) {
        // TODO: step 17 prompt user.
        TvContractCompat.requestChannelBrowsable(this, channelId);
        *//*Intent intent = new Intent(TvContractCompat.ACTION_REQUEST_CHANNEL_BROWSABLE);
        intent.putExtra(TvContractCompat.EXTRA_CHANNEL_ID, channelId);
        try {
            this.startActivityForResult(intent, MAKE_BROWSABLE_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Could not start activity: " + intent.getAction(), e);
        }*//*
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

    @SuppressLint("StaticFieldLeak")
    private class AddChannelTask extends AsyncTask<Subscription, Void, Long> {
        private final Context context;

        public AddChannelTask(Context context) {
            this.context = context;
        }

        @Override
        protected Long doInBackground(Subscription... varArgs) {
            List<Subscription> subscriptions = Collections.singletonList(varArgs);
            if (subscriptions.size() != 1) {
                return -1L;
            }
            Subscription subscription = subscriptions.get(0);
            long channelId = TvUtil.createChannel(context, subscription);
            subscription.setChannelId(channelId);
            MockDatabase.saveSubscription(context, subscription);

            TvUtil.scheduleSyncingProgramsForChannel(getApplicationContext(), channelId);
            mChannelId = channelId;
            Log.e("1234", "AsyncTask: " + mChannelId);
            return channelId;
        }

        @Override
        protected void onPostExecute(Long channelId) {
            super.onPostExecute(channelId);
            if (!channelExists)
                promptUserToDisplayChannel(channelId);
        }
    }*/
}


