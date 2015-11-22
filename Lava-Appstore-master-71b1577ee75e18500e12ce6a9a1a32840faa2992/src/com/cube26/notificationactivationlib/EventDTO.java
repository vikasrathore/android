package com.cube26.notificationactivationlib;

import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

public class EventDTO {
    
    private String eventId       = "";
    private String eventCategory = "";
    private String eventAction   = "";
    private String eventLabel    = "";
    private String eventValue    = "";
    private String eventTime     = "";
    private String eventTimeZone = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
    
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("eC",eventCategory );
            jsonObject.put("eA",eventAction );
            jsonObject.put("eL",eventLabel );
            jsonObject.put("eV",eventValue );
            jsonObject.put("eT",getEventTime() );
            jsonObject.put("eTz",eventTimeZone);
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventCategory() {
        return eventCategory;
    }
    public void setEventCategory(String eventCategory) {
        if(null==eventCategory){
            eventCategory="";
        }
        this.eventCategory = eventCategory;
    }
    public String getEventAction() {
        return eventAction;
    }
    public void setEventAction(String eventAction) {
        if(null==eventAction){
            eventAction="";
        }
        this.eventAction = eventAction;
    }
    public String getEventLabel() {
        return eventLabel;
    }
    public void setEventLabel(String eventLabel) {
        if(null==eventLabel){
            eventLabel="";
        }
        this.eventLabel = eventLabel;
    }
    public String getEventValue() {
        return eventValue;
    }
    public void setEventValue(String eventValue) {
        if(null==eventValue){
            eventValue="";
        }
        this.eventValue = eventValue;
    }
    public String getEventTime() {
        return "".equalsIgnoreCase(eventTime)?Util.getCurrentTimeInMillis():eventTime;
    }
    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }
}
