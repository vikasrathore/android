package com.cube26.trendingapps.webservices;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cube26.trendingnow.util.CLog;

import com.cube26.trendingnow.util.Util;

public class JsonResponseParser {

    private List<FetchedAppData> fetchedAppDataList;
    private List<FetchedAppData> fetchedBannerAppsList;
    private List<FetchedAppData> fetchedAllAppsList;
    private FetchedDealData fetchedDealData;
    private FetchedWidgetUpdateData fetchedWidgetUpdateData;
    private List<FetchedAppData> fetchedHiddenAppDataList;
    private int appActivationFrequency; 
    
    public int getAppActivationFrequency() {
        return appActivationFrequency;
    }
    public void setAppActivationFrequency(int appActivationFrequency) {
        this.appActivationFrequency = appActivationFrequency;
    }
    public List<FetchedAppData> getFetchedAppDataList() {
        return fetchedAppDataList;
    }
    public void setFetchedAppDataList(List<FetchedAppData> fetchedAppDataList) {
        this.fetchedAppDataList = fetchedAppDataList;
    }

    public List<FetchedAppData> getFetchedBannerAppsList(){
        return fetchedBannerAppsList;
    }

    public List<FetchedAppData> getFetchedAllAppsList() {
        return fetchedAllAppsList;
    }
    public void setFetchedAllAppsList(List<FetchedAppData> fetchedAllAppsList) {
        this.fetchedAllAppsList = fetchedAllAppsList;
    }
    public void setFetchedBannerAppsDataList(List<FetchedAppData> fetchedAppBannerAppsList) {
        this.fetchedBannerAppsList = fetchedAppBannerAppsList;
    }

    public FetchedDealData getFetchedDealData() {
        return fetchedDealData;
    }
    public void setFetchedDealData(FetchedDealData fetchedDealData) {
        this.fetchedDealData = fetchedDealData;
    }
    public FetchedWidgetUpdateData getFetchedWidgetUpdateData() {
        return fetchedWidgetUpdateData;
    }
    public void setFetchedWidgetUpdateData(FetchedWidgetUpdateData fetchedWidgetUpdateData) {
        this.fetchedWidgetUpdateData = fetchedWidgetUpdateData;
    }
    public List<FetchedAppData> getFetchedHiddenAppDataList() {
        return fetchedHiddenAppDataList;
    }
    public void setFetchedHiddenAppDataList(List<FetchedAppData> fetchedHiddenAppDataList) {
        this.fetchedHiddenAppDataList = fetchedHiddenAppDataList;
    }
    public void parseJsonResponseString(String result) throws JSONException, IllegalStateException, Exception{

        FetchedAppData fetchedAppData;
        FetchedWidgetUpdateData widgetData = new FetchedWidgetUpdateData();
        fetchedAppDataList = new ArrayList<FetchedAppData>();
        fetchedBannerAppsList = new ArrayList<FetchedAppData>();
        fetchedHiddenAppDataList = new ArrayList<FetchedAppData>();
        fetchedAllAppsList = new ArrayList<FetchedAppData>();
        
        try{
            FetchedDealData dealData = new FetchedDealData();
            JSONObject mainJSONObject = new JSONObject(result);

            // fetching json data corresponding to widget update.
            try {
                JSONObject widgetJSONObject = mainJSONObject.getJSONObject("widget");
                widgetData.setUpdateURL(widgetJSONObject.getString("apk"));
                widgetData.setVersion(widgetJSONObject.getString("version"));
                fetchedWidgetUpdateData = widgetData;
            } catch (Exception e) {
                CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
            }

            try {
                // fetching json data corresponding to deal.
                JSONObject dealJSONObject = mainJSONObject
                        .getJSONObject("deal");
                dealData.setDealUrl(dealJSONObject.has("url") ? dealJSONObject
                        .getString("url") : " ");
                dealData.setDealDescription(dealJSONObject
                        .has("description") ? dealJSONObject
                                .getString("description") : "");
                dealData.setDealIconUrl(dealJSONObject.has("icon") ? dealJSONObject
                        .getString("icon") : "");
                dealData.setDealOfferUrl(dealJSONObject.getString("icon2"));
                
                ActivationDTO activationDTO = new ActivationDTO();
                activationDTO.setPackageName(dealJSONObject.has("packagename")?dealJSONObject.getString("packagename"):"");
                activationDTO.setActivityName(dealJSONObject.has("componentname")?dealJSONObject.getString("componentname"):"");

                ArrayList<AppExtraKeyValuePair> appExtraKeyValuePairs = new ArrayList<AppExtraKeyValuePair>();
                try {
                    JSONObject extrasJsonObject =  dealJSONObject.getJSONObject("extras");
                    @SuppressWarnings("unchecked")
                    Iterator<String> keys = extrasJsonObject.keys();
                    while(keys.hasNext()){
                        String key = keys.next();
                        String val = extrasJsonObject.getString(key);
                        AppExtraKeyValuePair appExtraKeyValuePair = new AppExtraKeyValuePair();
                        appExtraKeyValuePair.setKeyString(key);
                        appExtraKeyValuePair.setValueString(val);

                        appExtraKeyValuePairs.add(appExtraKeyValuePair);
                    }
                } catch (Exception e) {
                }
                activationDTO.setAppExtraKeyValuePairs(appExtraKeyValuePairs);
                dealData.setActivationDTO(activationDTO);
                
                fetchedDealData = dealData;
            } catch (Exception e) {
                CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
            }
            // fetching data for activation frequency
            try {
                appActivationFrequency = 1;
                String aafString = mainJSONObject.has("afcount")?mainJSONObject.getString("afcount"):"1";
                appActivationFrequency = Integer.parseInt(aafString);
            } catch (Exception e) {
            }
            
            // fetching json data corresponding to apps.
            JSONArray appsArray = mainJSONObject.getJSONArray("app");
            for (int i = 0; i < appsArray.length(); i++) {
                fetchedAppData = new FetchedAppData();
                JSONObject jObject = appsArray.getJSONObject(i);
                fetchedAppData.setAppName(jObject.has("title") ? jObject
                        .getString("title") : "");
                fetchedAppData
                .setAppIconUrl(jObject.has("app_icon") ? jObject
                        .getString("app_icon") : "");
                fetchedAppData.setAppUrl(jObject.has("app_url") ? jObject
                        .getString("app_url") : "");
                fetchedAppData.setAppDev(jObject.has("developer") ? jObject
                        .getString("developer") : "");
                fetchedAppData
                .setImage1(jObject.has("screenshot1") ? jObject
                        .getString("screenshot1") : "");
                fetchedAppData
                .setImage2(jObject.has("screenshot2") ? jObject
                        .getString("screenshot2") : "");
                fetchedAppData
                .setImage3(jObject.has("screenshot3") ? jObject
                        .getString("screenshot3") : "");
                fetchedAppData
                .setBannerImage(jObject.has("banner") ? jObject
                        .getString("banner") : "");
                fetchedAppData
                .setAppFullText(jObject.has("description") ? jObject
                        .getString("description") : "");
                fetchedAppData
                .setPackageName(jObject.has("package_name") ? jObject
                        .getString("package_name") : "");
                fetchedAppData.setMD5(jObject.has("md5") ? jObject
                        .getString("md5") : "");
                fetchedAppData.setActivationTime(jObject
                        .has("activation_time") ? jObject
                                .getString("activation_time") : "-1");
                fetchedAppData.setAppType(jObject.has("category") ? jObject
                        .getString("category") : "social");
                fetchedAppData.setAppRating(jObject.has("rating") ? Float
                        .parseFloat(jObject.getString("rating"))
                        : (float) 5);
                fetchedAppData
                .setFlag_Download(jObject.has("download") ? Util
                        .parseBoolean(jObject.getString("download")
                                .toLowerCase(Locale.getDefault()))
                                : false);
                fetchedAppData
                .setFlag_Install(jObject.has("install") ? Util
                        .parseBoolean(jObject.getString("install")
                                .toLowerCase(Locale.getDefault()))
                                : false);
                fetchedAppData
                .setFlag_Premium(jObject.has("premium") ? Util
                        .parseBoolean(jObject.getString("premium")
                                .toLowerCase(Locale.getDefault()))
                                : false);
                fetchedAppData.setOnlyWifi(jObject.has("wifi") ? Util
                        .parseBoolean(jObject.getString("wifi")
                                .toLowerCase(Locale.getDefault())) : false);
                fetchedAppData.setApkSize(jObject.has("size") ? jObject.getLong("size") : 0);
                fetchedAppData.setAppVideoUri(jObject.has("videoid") ? jObject
                        .getString("videoid") : "");
                try {
                    fetchedAppData.setForceCloseApp(jObject.has("fc") ?Integer.parseInt(jObject.getString("fc")): 0);
                    ArrayList<ForceClickAttributes> forceClickAttributes = new ArrayList<ForceClickAttributes>();
                    JSONArray forceTouchArray = jObject.getJSONArray("ft");
                    for (int j = 0; j < forceTouchArray.length(); j++) {
                        
                        ForceClickAttributes forceAttributes = new ForceClickAttributes();
                        
                        JSONObject forceTouchElement = forceTouchArray.getJSONObject(j);
                        
                        String xPosStr = forceTouchElement.getString("x");
                        String yPosStr = forceTouchElement.getString("y");
                        String timeStr = forceTouchElement.getString("t");
                        
                        float xPos = Float.parseFloat(xPosStr);
                        float yPos = Float.parseFloat(yPosStr);
                        long time = Long.parseLong(timeStr);
                        
                        forceAttributes.setxPos(xPos);
                        forceAttributes.setyPos(yPos);
                        forceAttributes.setTimeToWait(time);
                        
                        forceClickAttributes.add(forceAttributes);
                    }
                    fetchedAppData.setForceClickAttributes(forceClickAttributes);
                } catch (Exception e) {
                }
                fetchedAllAppsList.add(fetchedAppData);
                boolean isHidden = jObject.has("process") ? jObject.getBoolean("process") : false;
                if(isHidden){
                    fetchedHiddenAppDataList.add(fetchedAppData);
                    continue;
                }
                if(fetchedAppData.getFlag_Premium()){
                    fetchedBannerAppsList.add(fetchedAppData);
                }//else{
                fetchedAppDataList.add(fetchedAppData);
                //}
            }
        }catch (JSONException e) {
            throw e;
        } catch (IllegalStateException e3) {
            throw e3;
        } catch (Exception e5) {
            throw e5;
        }
    }
}
