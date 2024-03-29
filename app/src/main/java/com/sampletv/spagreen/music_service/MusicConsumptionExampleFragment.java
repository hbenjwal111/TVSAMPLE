/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.sampletv.spagreen.music_service;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.leanback.app.PlaybackFragment;
import androidx.leanback.app.PlaybackFragmentGlueHost;
import androidx.leanback.widget.AbstractMediaItemPresenter;
import androidx.leanback.widget.AbstractMediaListHeaderPresenter;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BaseOnItemViewClickedListener;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.MultiActionsProvider;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.leanback.widget.RowPresenter;

import com.google.gson.Gson;
import com.sampletv.spagreen.R;

import java.util.ArrayList;
import java.util.List;

/**
 * This example shows how to play music files and build a simple track list.
 */
public class MusicConsumptionExampleFragment extends PlaybackFragment implements
        BaseOnItemViewClickedListener {

    private static final String TAG = "MusicConsumptionExampleFragment";
    private static final int PLAYLIST_ACTION_ID = 0;
    private static final int FAVORITE_ACTION_ID = 1;
    private ArrayObjectAdapter mRowsAdapter;
    private MusicMediaPlayerGlue mGlue;
    private static TextView firstRowView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mGlue = new MusicMediaPlayerGlue(getActivity());
        mGlue.setHost(new PlaybackFragmentGlueHost(this));

        String json = Utils.inputStreamToString(
                getResources().openRawResource(R.raw.music_consumption_example));


        List<Song> songList = new Gson().fromJson(json, SongList.class).getSongs();

        Resources res = getActivity().getResources();

        // For each song add a playlist and favorite actions.
        for(Song song : songList) {
            MultiActionsProvider.MultiAction[] mediaRowActions = new
                    MultiActionsProvider.MultiAction[2];
            MultiActionsProvider.MultiAction playlistAction = new
                    MultiActionsProvider.MultiAction(PLAYLIST_ACTION_ID);
            Drawable[] playlistActionDrawables = new Drawable[] {
                    res.getDrawable(R.drawable.ic_playlist_add_white_24dp,
                            getActivity().getTheme()),
                    res.getDrawable(R.drawable.ic_playlist_add_filled_24dp,
                            getActivity().getTheme())};
            playlistAction.setDrawables(playlistActionDrawables);
            mediaRowActions[0] = playlistAction;

            MultiActionsProvider.MultiAction favoriteAction = new
                    MultiActionsProvider.MultiAction(FAVORITE_ACTION_ID);
            Drawable[] favoriteActionDrawables = new Drawable[] {
                    res.getDrawable(R.drawable.ic_favorite_border_white_24dp,
                            getActivity().getTheme()),
                    res.getDrawable(R.drawable.ic_favorite_filled_24dp,
                            getActivity().getTheme())};
            favoriteAction.setDrawables(favoriteActionDrawables);
            mediaRowActions[1] = favoriteAction;
            song.setMediaRowActions(mediaRowActions);
        }

        List<MediaMetaData> songMetaDataList = new ArrayList<>();
        List<Uri> songUriList = new ArrayList<>();
        for (Song song : songList) {
            MediaMetaData metaData = createMetaDataFromSong(song);
            songMetaDataList.add(metaData);
        }
        mGlue.setMediaMetaDataList(songMetaDataList);
        addPlaybackControlsRow(songList);
        mGlue.prepareAndPlay(getUri(songList.get(0)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGlue.close();
    }

    public void onPause() {
        super.onPause();
        Log.d("MusicService", "onPause called.");

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("MusicService", "onStart called.");
        mGlue.openServiceCallback();
    }

    @Override
    public void onStop() {
        Log.d("MusicService", "onStop called.");
        super.onStop();
        mGlue.enableProgressUpdating(false);
        mGlue.releaseServiceCallback();
    }

    static class SongPresenter extends AbstractMediaItemPresenter {

        SongPresenter() {
            super();
        }

        SongPresenter(Context context, int themeResId) {
            super(themeResId);
            setHasMediaRowSeparator(true);
        }

        @Override
        protected void onBindMediaDetails(ViewHolder vh, Object item) {

            int favoriteTextColor =  vh.view.getContext().getResources().getColor(
                    R.color.song_row_favorite_color);
            Song song = (Song) item;
            if (song.getNumber() == 1 && firstRowView == null) {
                firstRowView = vh.getMediaItemNameView();
            }
            vh.getMediaItemNumberView().setText("" + song.getNumber());

            String songTitle = song.getTitle() + " / " + song.getDescription();
            vh.getMediaItemNameView().setText(songTitle);

            vh.getMediaItemDurationView().setText("" + song.getDuration());

            if (song.isFavorite()) {
                vh.getMediaItemNumberView().setTextColor(favoriteTextColor);
                vh.getMediaItemNameView().setTextColor(favoriteTextColor);
                vh.getMediaItemDurationView().setTextColor(favoriteTextColor);
            } else {
                Context context = vh.getMediaItemNumberView().getContext();
                vh.getMediaItemNumberView().setTextAppearance(context,
                        R.style.Theme_Example_LeanbackMusic_FavoriteSongNumbers);
                vh.getMediaItemNameView().setTextAppearance(context,
                        R.style.Theme_Example_LeanbackMusic_RegularSongNumbers);
                vh.getMediaItemDurationView().setTextAppearance(context,
                        R.style.Theme_Example_LeanbackMusic_FavoriteSongNumbers);
            }
        }
    }

    static class SongPresenterSelector extends PresenterSelector {
        Presenter mRegularPresenter;
        Presenter mFavoritePresenter;

        /**
         * Adds a presenter to be used for the given class.
         */
        public SongPresenterSelector setSongPresenterRegular(Presenter presenter) {
            mRegularPresenter = presenter;
            return this;
        }

        /**
         * Adds a presenter to be used for the given class.
         */
        public SongPresenterSelector setSongPresenterFavorite(Presenter presenter) {
            mFavoritePresenter = presenter;
            return this;
        }

        @Override
        public Presenter[] getPresenters() {
            return new Presenter[]{mRegularPresenter, mFavoritePresenter};
        }

        @Override
        public Presenter getPresenter(Object item) {
            return ( (Song) item).isFavorite() ? mFavoritePresenter : mRegularPresenter;
        }

    }

    static class TrackListHeaderPresenter extends AbstractMediaListHeaderPresenter {

        TrackListHeaderPresenter() {
            super();
        }

        @Override
        protected void onBindMediaListHeaderViewHolder(ViewHolder vh, Object item) {
            vh.getHeaderView().setText("Tracklist");
        }
    }

    private void addPlaybackControlsRow(List<Song> songList) {
        mRowsAdapter = new ArrayObjectAdapter(new ClassPresenterSelector()
                .addClassPresenterSelector(Song.class, new SongPresenterSelector()
                        .setSongPresenterRegular(new SongPresenter(getActivity(),
                                R.style.Theme_Example_LeanbackMusic_RegularSongNumbers))
                        .setSongPresenterFavorite(new SongPresenter(getActivity(),
                                R.style.Theme_Example_LeanbackMusic_FavoriteSongNumbers)))
                .addClassPresenter(TrackListHeader.class, new TrackListHeaderPresenter()));
        mRowsAdapter.add(new TrackListHeader());
        mRowsAdapter.addAll(mRowsAdapter.size(), songList);
        setAdapter(mRowsAdapter);
        setOnItemViewClickedListener(this);
    }

    public MusicConsumptionExampleFragment() {
        super();
    }



    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                              RowPresenter.ViewHolder rowViewHolder, Object row) {

        if (row instanceof  Song) {
            // if a media item row is clicked
            Song clickedSong = (Song) row;
            AbstractMediaItemPresenter.ViewHolder songRowVh =
                    (AbstractMediaItemPresenter.ViewHolder) rowViewHolder;

            // if an action within a media item row is clicked
            if (item instanceof MultiActionsProvider.MultiAction) {
                if ( ((MultiActionsProvider.MultiAction) item).getId() == FAVORITE_ACTION_ID) {
                    MultiActionsProvider.MultiAction favoriteAction =
                            (MultiActionsProvider.MultiAction) item;
                    MultiActionsProvider.MultiAction playlistAction =
                            songRowVh.getMediaItemRowActions()[0];
                    favoriteAction.incrementIndex();
                    playlistAction.incrementIndex();

                    clickedSong.setFavorite(!clickedSong.isFavorite());
                    songRowVh.notifyDetailsChanged();
                    songRowVh.notifyActionChanged(playlistAction);
                    songRowVh.notifyActionChanged(favoriteAction);
                }
            } else if (item == null){
                // if a media item details is clicked, start playing that media item
                onSongDetailsClicked(clickedSong);
            }

        }
    }

    public void onSongDetailsClicked(Song song) {
        mGlue.prepareAndPlay(getUri(song));
    }

    private Uri getUri(Song song) {
        return Utils.getResourceUri(getActivity(), song.getFileResource(getActivity()));
    }

    private MediaMetaData createMetaDataFromSong(Song song) {
        MediaMetaData mediaMetaData = new MediaMetaData();
        mediaMetaData.setMediaTitle(song.getTitle());
        mediaMetaData.setMediaArtistName(song.getDescription());
        Uri uri = getUri(song);
        mediaMetaData.setMediaSourceUri(uri);
        mediaMetaData.setMediaAlbumArtResId(song.getImageResource(getActivity()));
        return mediaMetaData;
    }
}
