/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.oxootv.spagreen.video_service;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.tvprovider.media.tv.TvContractCompat;
import androidx.tvprovider.media.tv.WatchNextProgram;


/** Adds, updates, and removes the currently playing {@link PlaybackModel} from the "Watch Next" channel. */
public class WatchNextAdapter {

    private static final String TAG = "WatchNextAdapter";

    public void updateProgress(Context context, long channelId, PlaybackModel movie, long position, long duration) {
        Log.e("1234", String.format("Updating the movie (%d) in watch next. ChannelId: (%d)", movie.getId(), channelId));

        PlaybackModel entity = MockDatabase.findMovieById(context, channelId, movie.getId());
        if (entity == null) {
            Log.e("1234", String.format(
                            "Could not find movie in channel: channel id: %d, movie id: %d",
                            channelId, movie.getId()));
            return;
        }

        // TODO: step 12 add watch next program.
        WatchNextProgram program = createWatchNextProgram(channelId, entity, position, duration);
        if (entity.getWatchNextId() < 1L) {
            // Need to create program.
            Uri watchNextProgramUri =
                    context.getContentResolver()
                            .insert(
                                    TvContractCompat.WatchNextPrograms.CONTENT_URI,
                                    program.toContentValues());
            long watchNextId = ContentUris.parseId(watchNextProgramUri);
            entity.setWatchNextId(watchNextId);
            MockDatabase.saveMovie(context, channelId, entity);

            Log.d(TAG, "Watch Next program added: " + watchNextId);
        } else {
            // TODO: step 14 update program.
            // Update the progress and last engagement time of the program.
            context.getContentResolver()
                    .update(
                            TvContractCompat.buildWatchNextProgramUri(entity.getWatchNextId()),
                            program.toContentValues(),
                            null,
                            null);

            Log.d(TAG, "Watch Next program updated: " + entity.getWatchNextId());
        }
    }

    @NonNull
    private WatchNextProgram createWatchNextProgram(
            long channelId, PlaybackModel movie, long position, long duration) {
        // TODO: step 13 convert movie
        Uri posterArtUri = Uri.parse(movie.getBgImageUrl());
        Uri intentUri = AppLinkHelper.buildPlaybackUri(channelId, movie.getId(), position);

        WatchNextProgram.Builder builder = new WatchNextProgram.Builder();
        builder.setType(TvContractCompat.PreviewProgramColumns.TYPE_MOVIE)
                .setWatchNextType(TvContractCompat.WatchNextPrograms.WATCH_NEXT_TYPE_CONTINUE)
                .setLastEngagementTimeUtcMillis(System.currentTimeMillis())
                .setLastPlaybackPositionMillis((int) position)
                .setDurationMillis((int) duration)
                .setTitle(movie.getTitle())
                .setDescription(movie.getDescription())
                .setPosterArtUri(posterArtUri)
                .setIntentUri(intentUri);
        return builder.build();
    }

    public void removeFromWatchNext(Context context, long channelId, long movieId) {
        PlaybackModel movie = MockDatabase.findMovieById(context, channelId, movieId);
        if (movie == null || movie.getWatchNextId() < 1L) {
            Log.d(TAG, "No program to remove from watch next.");
            return;
        }

        // TODO: step 15 remove program
        int rows =
                context.getContentResolver()
                        .delete(
                                TvContractCompat.buildWatchNextProgramUri(movie.getWatchNextId()),
                                null,
                                null);
        Log.d(TAG, String.format("Deleted %d programs(s) from watch next", rows));

        // Sync our records with the system; remove reference to watch next program.
        movie.setWatchNextId(-1);
        MockDatabase.saveMovie(context, channelId, movie);
    }
}
