package com.news.nytesttimes.parser;

import com.news.nytesttimes.datatypes.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
public class JSONParser {
    static String TAG = "JSONParser";


    public static ArrayList<News> parseArticles(String jsonString) {
        ArrayList<News> news = new ArrayList<News>();
        try {
            JSONObject newsJO = new JSONObject(jsonString);
            JSONObject newsJO1 = newsJO.getJSONObject("response");
            JSONArray newsJA = newsJO1.getJSONArray("docs");
            int count = newsJA.length();

            for (int i = 0; i < count; i++) {
                JSONObject newsJObj = newsJA.getJSONObject(i);
                News newsItem = new News();
                newsItem.setSnippet(newsJObj.getJSONObject("headline").getString("main"));

                newsItem.setLead_paragraph(newsJObj.getString("snippet"));
                news.add(newsItem);

            }

        } catch (JSONException e) {

        }
        catch (NullPointerException e) {

        }
        return news;
    }


    public static ArrayList<News> parsePopularArticles(String jsonString) {
        ArrayList<News> news = new ArrayList<News>();
        try {
            JSONObject newsJO = new JSONObject(jsonString);

            JSONArray newsJA = newsJO.getJSONArray("results");
            int count = newsJA.length();
            News newsItem = null;
            for (int i = 0; i < count; i++) {
                JSONObject newsJObj = newsJA.getJSONObject(i);
                newsItem = new News(i);
                newsItem.setSnippet(newsJObj.getString("title"));
                newsItem.setLead_paragraph(newsJObj.getString("abstract"));
                newsItem.setNewsWebURL(newsJObj.getString("url"));
                if (newsJObj.optJSONArray("media") !=null&&newsJObj.optJSONArray("media").length()>0)
                newsItem.setThumbnail_URL(newsJObj.getJSONArray("media").getJSONObject(0).getJSONArray("media-metadata").getJSONObject(2).getString("url"));
                news.add(newsItem);

            }


        } catch (JSONException e) {

        }
        catch (NullPointerException e) {

        }
        return news;
    }


    public static ArrayList<News> parseTopStories(String jsonString) {
        ArrayList<News> news = new ArrayList<News>();
        try {
            JSONObject newsJO = new JSONObject(jsonString);

            JSONArray newsJA = newsJO.getJSONArray("results");
            int count = newsJA.length();
            News newsItem = null;
            for (int i = 0; i < count; i++) {
                JSONObject newsJObj = newsJA.getJSONObject(i);
                newsItem = new News(i);
                newsItem.setSnippet(newsJObj.getString("title"));
                newsItem.setNewsWebURL(newsJObj.getString("url"));
                newsItem.setLead_paragraph(newsJObj.getString("abstract"));
                if (newsJObj.optJSONArray("multimedia") !=null&&newsJObj.optJSONArray("multimedia").length()>2)
                newsItem.setThumbnail_URL(newsJObj.getJSONArray("multimedia").getJSONObject(2).getString("url"));
                news.add(newsItem);

            }


        } catch (JSONException e) {

        }
        catch (NullPointerException e) {

        }
        return news;
    }


}
