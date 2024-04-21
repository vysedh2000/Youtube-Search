package com.final_project.model;

import java.time.OffsetDateTime;

public class Snippet {
    private OffsetDateTime publishedAt;
    private String channelID;
    private String title;
    private String description;
    private Thumbnails thumbnails;
    private String channelTitle;
    private String liveBroadcastContent;
    private OffsetDateTime publishTime;

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(OffsetDateTime value) {
        this.publishedAt = value;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String value) {
        this.channelID = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public Thumbnails getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(Thumbnails value) {
        this.thumbnails = value;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String value) {
        this.channelTitle = value;
    }

    public String getLiveBroadcastContent() {
        return liveBroadcastContent;
    }

    public void setLiveBroadcastContent(String value) {
        this.liveBroadcastContent = value;
    }

    public OffsetDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(OffsetDateTime value) {
        this.publishTime = value;
    }
}