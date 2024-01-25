package com.oxootv.spagreen.video_service;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;

import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.oxootv.spagreen.ui.activity.PlayerActivity;

public class MediaSessionHelper {
    MediaSession mSession;
    private ExoPlayer player;
    private Context mContext;
    private PlaybackModel movie;
    private boolean isPlaying;
    int mState = PlaybackState.STATE_PLAYING;
    long position = -1L;

    public MediaSessionHelper(ExoPlayer player, Context mContext, PlaybackModel movie, boolean isPlaying) {
        this.player = player;
        this.mContext = mContext;
        this.movie = movie;
        this.isPlaying = isPlaying;

        createMediaSession();
    }

    private void createMediaSession(){
        if (mSession == null) {
            mSession = new MediaSession(mContext, "MediaSession");
            mSession.setCallback(callback);
            mSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSession.FLAG_HANDLES_MEDIA_BUTTONS);
            mSession.setActive(true);

            Intent intent = new Intent(mContext.getApplicationContext(), PlayerActivity.class);
            intent.putExtra(VideoPlaybackActivity.EXTRA_VIDEO, movie);
            intent.putExtra(VideoPlaybackActivity.EXTRA_POSITION, position);
            PendingIntent pi = PendingIntent.getActivity(mContext.getApplicationContext(), 99, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mSession.setSessionActivity(pi);

        }
    }

    public void releaseMediaSession() {
        if (mSession != null)
            mSession.release();
    }

    public void updatePlaybackState(){
        position = PlaybackState.PLAYBACK_POSITION_UNKNOWN;
        if (this.player != null){
            position = player.getCurrentPosition();
        }
        PlaybackState.Builder  stateBuilder = new
                PlaybackState.Builder()
                .setActions(getAvailableActions());
        if (!isPlaying){
            mState = PlaybackState.STATE_PAUSED;
        }
        stateBuilder.setState(mState, position, 1.0f);
        mSession.setPlaybackState(stateBuilder.build());

       /* PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE);
        int state = PlaybackState.STATE_PLAYING;
        if (playing){
            state = PlaybackState.STATE_PAUSED;
        }
        stateBuilder.setState(state, player.getCurrentPosition(), 1.0f);
        mSession.setPlaybackState(stateBuilder.build());*/

    }
    private long getAvailableActions(){
        long actions = PlaybackState.ACTION_PLAY_PAUSE |
                PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackState.ACTION_PLAY_FROM_SEARCH;

        if (mState == PlaybackState.STATE_PLAYING){
            actions |= PlaybackState.ACTION_PAUSE;
        }else {
            actions |= PlaybackState.ACTION_PLAY;
        }
        return actions;
    }

    public void stopMediaSession(){
        if (mSession.isActive()){
            mSession.setActive(false);
            mSession.release();
        }
        releaseMediaSession();
    }
    public void updateMetadata(){
        final MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();
        metadataBuilder.putString(
                MediaMetadata.METADATA_KEY_DISPLAY_TITLE, movie.getTitle());
        metadataBuilder.putString(
                MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, movie.getDescription());
        metadataBuilder.putString(
                MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,movie.getCardImageUrl());
        metadataBuilder.putString(
                MediaMetadata.METADATA_KEY_ARTIST, movie.getCategory());
        if (player != null){
            metadataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, player.getDuration());
        }
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, movie.getTitle());
        Glide.with(mContext.getApplicationContext())
                .asBitmap()
                .load(Uri.parse(movie.getCardImageUrl()))
                .into(new CustomTarget<Bitmap>( ) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, resource);
                        mSession.setMetadata(metadataBuilder.build());
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

    }

    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();


    private MediaSession.Callback callback = new MediaSession.Callback() {
        @Override
        public void onPlay() {
            super.onPlay();
            Log.e("123456", "Play");
            updateMetadata();
            //resister audi receiver
            mContext.registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
        }

        @Override
        public void onPrepare() {
            super.onPrepare();
        }

        @Override
        public void onPrepareFromUri(Uri uri, Bundle extras) {
            super.onPrepareFromUri(uri, extras);
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.e("123456", "Pause");
           player.setPlayWhenReady(true);
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            Log.e("123456", "Skip to next");
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            Log.e("123456", "Skip to previous");
        }

        @Override
        public void onStop() {
            super.onStop();
            Log.e("123456", "Stop");
            stopMediaSession();
            //unregister audio receiver
            mContext.unregisterReceiver(myNoisyAudioStreamReceiver);
        }

    };


    private class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                // Pause the playback
                player.setPlayWhenReady(false);
            }
        }
    }
}
