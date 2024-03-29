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
package com.sampletv.spagreen.video_service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.PreviewProgram;
import androidx.tvprovider.media.tv.TvContractCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SyncProgramsJobService extends JobService {

    private static final String TAG = "SyncProgramsJobService";

    private SyncProgramsTask mSyncProgramsTask;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.d(TAG, "onStartJob(): " + jobParameters);

        final long channelId = getChannelId(jobParameters);
        if (channelId == -1L) {
            return false;
        }
        Log.d(TAG, "onStartJob(): Scheduling syncing for programs for channel " + channelId);

        mSyncProgramsTask =
                new SyncProgramsTask(getApplicationContext()) {
                    @Override
                    protected void onPostExecute(Boolean finished) {
                        super.onPostExecute(finished);
                        // Daisy chain listening for the next change to the channel.
                        TvUtil.scheduleSyncingProgramsForChannel(
                                SyncProgramsJobService.this, channelId);
                        mSyncProgramsTask = null;
                        jobFinished(jobParameters, !finished);
                    }
                };
        mSyncProgramsTask.execute(channelId);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mSyncProgramsTask != null) {
            mSyncProgramsTask.cancel(true);
        }
        return true;
    }

    private long getChannelId(JobParameters jobParameters) {
        PersistableBundle extras = jobParameters.getExtras();
        if (extras == null) {
            return -1L;
        }

        return extras.getLong(TvContractCompat.EXTRA_CHANNEL_ID, -1L);
    }

    /*
     * Syncs programs by querying the given channel id.
     *
     * If the channel is not browsable, the programs will be removed to avoid showing
     * stale programs when the channel becomes browsable in the future.
     *
     * If the channel is browsable, then it will check if the channel has any programs.
     *      If the channel does not have any programs, new programs will be added.
     *      If the channel does have programs, then a fresh list of programs will be fetched and the
     *          channel's programs will be updated.
     */
    private void syncPrograms(long channelId, List<PlaybackModel> initialMovies) {
        Log.d(TAG, "Sync programs for channel: " + channelId);
        List<PlaybackModel> movies = new ArrayList<>(initialMovies);

        try (Cursor cursor =
                getContentResolver()
                        .query(
                                TvContractCompat.buildChannelUri(channelId),
                                null,
                                null,
                                null,
                                null)) {
            if (cursor != null && cursor.moveToNext()) {
                Channel channel = Channel.fromCursor(cursor);
                if (!channel.isBrowsable()) {
                    Log.d(TAG, "Channel is not browsable: " + channelId);
                    deletePrograms(channelId, movies);
                } else {
                    Log.d(TAG, "Channel is browsable: " + channelId);
                    if (movies.isEmpty()) {
                        movies = createPrograms(channelId, MockMovieService.getList());
                    } else {
                        movies = updatePrograms(channelId, movies);
                    }
                    MockDatabase.saveMovies(getApplicationContext(), channelId, movies);
                }
            }
        }
    }

    private List<PlaybackModel> createPrograms(long channelId, List<PlaybackModel> movies) {

        List<PlaybackModel> moviesAdded = new ArrayList<>(movies.size());
        for (PlaybackModel movie : movies) {
            PreviewProgram previewProgram = buildProgram(channelId, movie);

            Uri programUri =
                    getContentResolver()
                            .insert(
                                    TvContractCompat.PreviewPrograms.CONTENT_URI,
                                    previewProgram.toContentValues());
            long programId = ContentUris.parseId(programUri);
            Log.d(TAG, "Inserted new program: " + programId);
            movie.setProgramId(programId);
            moviesAdded.add(movie);
        }

        return moviesAdded;
    }

    private List<PlaybackModel> updatePrograms(long channelId, List<PlaybackModel> movies) {

        // By getting a fresh list, we should see a visible change in the home screen.
        List<PlaybackModel> updateMovies = MockMovieService.getFreshList();
        for (int i = 0; i < movies.size(); ++i) {
            PlaybackModel old = movies.get(i);
            PlaybackModel update = updateMovies.get(i);
            long programId = old.getProgramId();

            getContentResolver()
                    .update(
                            TvContractCompat.buildPreviewProgramUri(programId),
                            buildProgram(channelId, update).toContentValues(),
                            null,
                            null);
            Log.d(TAG, "Updated program: " + programId);
            update.setProgramId(programId);
        }

        return updateMovies;
    }

    private void deletePrograms(long channelId, List<PlaybackModel> movies) {
        if (movies.isEmpty()) {
            return;
        }

        int count = 0;
        for (PlaybackModel movie : movies) {
            count +=
                    getContentResolver()
                            .delete(
                                    TvContractCompat.buildPreviewProgramUri(movie.getProgramId()),
                                    null,
                                    null);
        }
        Log.d(TAG, "Deleted " + count + " programs for  channel " + channelId);

        // Remove our local records to stay in sync with the TV Provider.
        MockDatabase.removeMovies(getApplicationContext(), channelId);
    }

    @NonNull
    private PreviewProgram buildProgram(long channelId, PlaybackModel movie) {
        Uri posterArtUri = Uri.parse(movie.getBgImageUrl());
        Uri appLinkUri = AppLinkHelper.buildPlaybackUri(channelId, movie.getId());
        Uri previewVideoUri = Uri.parse(movie.getVideoUrl());

        PreviewProgram.Builder builder = new PreviewProgram.Builder();
        builder.setChannelId(channelId)
                .setType(TvContractCompat.PreviewProgramColumns.TYPE_CLIP)
                .setTitle(movie.getTitle())
                .setDescription(movie.getDescription())
                .setPosterArtUri(posterArtUri)
                .setPreviewVideoUri(previewVideoUri)
                .setIntentUri(appLinkUri);
        return builder.build();
    }

    private class SyncProgramsTask extends AsyncTask<Long, Void, Boolean> {

        private final Context mContext;

        private SyncProgramsTask(Context context) {
            this.mContext = context;
        }
        @Override
        protected Boolean doInBackground(Long... channelIds) {
            List<Long> params = Arrays.asList(channelIds);
            if (!params.isEmpty()) {
                for (Long channelId : params) {
                    Subscription subscription =
                            MockDatabase.findSubscriptionByChannelId(mContext, channelId);
                    if (subscription != null) {
                        List<PlaybackModel> cachedMovies = MockDatabase.getMovies(mContext, channelId);
                        syncPrograms(channelId, cachedMovies);
                    }
                }
            }
            return true;
        }
    }
}
