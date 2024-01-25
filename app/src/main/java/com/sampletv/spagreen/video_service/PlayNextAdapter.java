package com.sampletv.spagreen.video_service;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.tvprovider.media.tv.TvContractCompat;
import androidx.tvprovider.media.tv.WatchNextProgram;


public class PlayNextAdapter {
    private static final String TAG = "PlayNextAdapter";
    private static final String SCHEMA_URI_PREFIX = "oxootv://app/";
    public static final String PLAYBACK = "playback";
    private static final String URI_PLAY = SCHEMA_URI_PREFIX + PLAYBACK;


    public void updateProgress(Context context, PlaybackModel movie, long position, long duration){
        Log.e(TAG, String.format("Updating the movie (%d) in watch next.", movie.getId()));

        //add watch next program
        WatchNextProgram program = createPlayNextProgram(movie, position, duration);
        if (movie.getWatchNextId() < 1L){
            Uri watchNextProgramUri =
                    context.getContentResolver()
                    .insert(
                            TvContractCompat.WatchNextPrograms.CONTENT_URI,
                            program.toContentValues());
            long watchNextId = ContentUris.parseId(watchNextProgramUri);
            movie.setWatchNextId(watchNextId);

        }else {
            //update
            context.getContentResolver()
                    .update(
                            TvContractCompat.buildWatchNextProgramUri(
                                    movie.getWatchNextId()),
                            program.toContentValues(),
                            null, null
                    );
        }

    }

    @NonNull
    private WatchNextProgram createPlayNextProgram(PlaybackModel model, long position, long duration){
        Uri posterUri = Uri.parse(model.getBgImageUrl());
        Uri intentUri = buildPlaybackUri(model.getId(), position);

        WatchNextProgram.Builder builder = new WatchNextProgram.Builder();
        builder.setType(TvContractCompat.PreviewProgramColumns.TYPE_MOVIE)
                .setWatchNextType(TvContractCompat.WatchNextPrograms.WATCH_NEXT_TYPE_CONTINUE)
                .setLastEngagementTimeUtcMillis(System.currentTimeMillis())
                .setLastPlaybackPositionMillis((int) position)
                .setDurationMillis((int) duration)
                .setTitle(model.getTitle())
                .setDescription(model.getDescription())
                .setPosterArtUri(posterUri)
                .setIntentUri(intentUri);
        return builder.build();
    }

    private Uri buildPlaybackUri(long movieId, long position){
        return Uri.parse(URI_PLAY)
                .buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(String.valueOf(position))
                .build();

    }

    private long getChannelId(Context context){
        Subscription subscription =
                MockDatabase.getVideoSubscription(context);
        return Math.max(subscription.getChannelId(), 0L);
    }
}
