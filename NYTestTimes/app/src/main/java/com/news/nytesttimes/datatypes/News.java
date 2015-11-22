package com.news.nytesttimes.datatypes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class News {

    String snippet;
    String lead_paragraph;
    int newsType;
    String thumbnail_URL;
    Bitmap bitmap;
    int newsID;
    String newsWebURL;

    public String getNewsWebURL() {
        return newsWebURL;
    }

    public void setNewsWebURL(String newsWebURL) {
        this.newsWebURL = newsWebURL;
    }

    public int getNewsID() {
        return newsID;
    }

    public void setNewsID(int newsID) {
        this.newsID = newsID;
    }

    public void setImageByteArray(byte[] image) {

        if (bitmap == null && image != null) {
            bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        }
    }

    public Bitmap getLasTImageBitmap() {
        return bitmap;
    }

    public String getThumbnail_URL() {
        return thumbnail_URL;
    }

    public void setThumbnail_URL(String thumbnail_URL) {
        this.thumbnail_URL = thumbnail_URL;
    }

    //lead_paragraph
    public News() {
    }


    public News(int newsID) {
        this.newsID = newsID;
    }


    public News(String mainHeadline, int newsType) {
        this.snippet = mainHeadline;
        this.newsType = newsType;
    }

    public News(String mainHeadline) {
        this.snippet = mainHeadline;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getLead_paragraph() {
        return lead_paragraph;
    }

    public void setLead_paragraph(String lead_paragraph) {
        this.lead_paragraph = lead_paragraph;
    }

    public int getNewsType() {
        return newsType;
    }

    public void setNewsType(int newsType) {
        this.newsType = newsType;
    }
}
