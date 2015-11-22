package com.news.nytesttimes.datatypes;

import com.news.nytesttimes.parser.JSONParser;

import java.util.ArrayList;

public class DataController {
    ArrayList<News> news;
    ArrayList<News> newstopStories;
    static DataController instance;

    public DataController() {

        news = new ArrayList<News>();
        newstopStories = new ArrayList<News>();
    }

    public static DataController getInstance() {

        if (instance == null)
            instance = new DataController();
        return instance;


    }

    public void setArticles(String jsonString) {

        news = JSONParser.parsePopularArticles(jsonString);

    }

    public ArrayList<News> getNews() {
        return news;
    }

    public void setTopStories(String jsonString) {

        newstopStories = JSONParser.parseTopStories(jsonString);

    }

    public ArrayList<News> getTopStories() {
        return newstopStories;
    }


}
