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

import android.content.Context;

import com.oxootv.spagreen.Constants;
import com.oxootv.spagreen.R;
import com.oxootv.spagreen.model.VideoContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MockMovieService {

    private static List<PlaybackModel> list;
    private static long count = 0;

    public static List<Subscription> createUniversalSubscriptions(Context context) {

        String newForYou = context.getString(R.string.recommended_for_you);
        Subscription flagshipSubscription =
                Subscription.createSubscription(
                        newForYou,
                        context.getString(R.string.recommended_for_you),
                        AppLinkHelper.buildBrowseUri(newForYou).toString(),
                        R.drawable.logo);

        String trendingVideos = context.getString(R.string.recommended_for_you);
        Subscription videoSubscription =
                Subscription.createSubscription(
                        trendingVideos,
                        context.getString(R.string.recommended_for_you),
                        AppLinkHelper.buildBrowseUri(trendingVideos).toString(),
                        R.drawable.logo);

        String featuredFilms = context.getString(R.string.recommended_for_you);
        Subscription filmsSubscription =
                Subscription.createSubscription(
                        featuredFilms,
                        context.getString(R.string.recommended_for_you),
                        AppLinkHelper.buildBrowseUri(featuredFilms).toString(),
                        R.drawable.logo);

        return Arrays.asList(flagshipSubscription, videoSubscription, filmsSubscription);
    }

    public static List<PlaybackModel> getList() {
        List<PlaybackModel> movieList = new ArrayList<>();
        if (Constants.movieList != null){
            for (int i =0; i < Constants.movieList.size(); i++){
                VideoContent videoContent = Constants.movieList.get(i);
                movieList.add(buildMovieInfo(
                        "movie",
                        videoContent.getTitle(),
                        videoContent.getDescription(),
                        videoContent.getId(),
                        "",
                        videoContent.getStreamUrl(),
                        videoContent.getIsPaid(),
                        videoContent.getThumbnailUrl(),
                        videoContent.getPosterUrl()));

            }
        }

        return movieList;
       /*
        if (list == null || list.isEmpty()) {
            list = createMovieList();
        }
        return list;*/
    }

    /**
     * Shuffles the list of movies to make the returned list appear to be a different list from
     * {@link #getList()}.
     *
     * @return a list of movies in random order.
     */
    public static List<PlaybackModel> getFreshList() {
        List<PlaybackModel> shuffledMovies = new ArrayList<>(getList());
        Collections.shuffle(shuffledMovies);
        return shuffledMovies;
    }

    private static PlaybackModel buildMovieInfo(
            String category,
            String title,
            String description,
            String movieId,
            String studio,
            String videoUrl,
            String isPaid,
            String cardImageUrl,
            String backgroundImageUrl) {
        PlaybackModel movie = new PlaybackModel();
        movie.setId(count);
        incCount();
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setMovieId(movieId);
        movie.setCategory(category);
        movie.setCardImageUrl(cardImageUrl);
        movie.setBgImageUrl(backgroundImageUrl);
        movie.setVideoUrl(videoUrl);
        movie.setIsPaid(isPaid);
        movie.setVideoType("");
        movie.setVideo(null);
        movie.setVideoList(null);
        return movie;
    }

    private static void incCount() {
        count++;
    }
}
