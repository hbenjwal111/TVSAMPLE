package com.sampletv.spagreen.video_service;

import androidx.annotation.NonNull;

import com.sampletv.spagreen.model.Videos;

import java.io.Serializable;
import java.util.List;

public class PlaybackModel implements Serializable {
    private long id;
    private String movieId;
    private String title;
    private String description;
    private String bgImageUrl;
    private String cardImageUrl;
    private String videoUrl;
    private String videoType;
    private String category;
    private List<Videos> videoList;
    private String isPaid;
    private Videos video;
    private long programId;
    private long watchNextId;

    public PlaybackModel() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBgImageUrl() {
        return bgImageUrl;
    }

    public void setBgImageUrl(String bgImageUrl) {
        this.bgImageUrl = bgImageUrl;
    }

    public String getCardImageUrl() {
        return cardImageUrl;
    }

    public void setCardImageUrl(String cardImageUrl) {
        this.cardImageUrl = cardImageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getVideoType() {
        return videoType;
    }

    public void setVideoType(String videoType) {
        this.videoType = videoType;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Videos> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<Videos> videoList) {
        this.videoList = videoList;
    }

    public Videos getVideo() {
        return video;
    }

    public void setVideo(Videos video) {
        this.video = video;
    }

    public long getProgramId() {
        return programId;
    }

    public void setProgramId(long programId) {
        this.programId = programId;
    }

    public String getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(String isPaid) {
        this.isPaid = isPaid;
    }

    public long getWatchNextId() {
        return watchNextId;
    }

    public void setWatchNextId(long watchNextId) {
        this.watchNextId = watchNextId;
    }

    @NonNull
    @Override
    public String toString() {
        return "PlaybackModel{"
                + "id="
                + id
                +", title='"
                + title
                + '\''
                + ", description='"
                + description
                + '\''
                + ", category='"
                + category
                + '\''
                + ", bgImageUrl='"
                + bgImageUrl
                + '\''
                + ", videoUrl='"
                + videoUrl
                + '\''
                + ", videoType='"
                + videoType
                + '\''
                + ", cardImageUrl='"
                + cardImageUrl
                + '\''
                + ", video='"
                + video
                + '\''
                + ", videoList='"
                + videoList
                + '\''
                + ", programId='"
                + programId
                + '\''
                + ", watchNextId='"
                + watchNextId
                + '\''
                + ", isPaid='"
                + isPaid
                + '\''
                + '}';
    }
}
