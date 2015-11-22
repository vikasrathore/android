package com.news.nytesttimes.network;

import java.util.Hashtable;

/**
 * Created by vikasrathour on 14/10/15.
 */

public class NewsOperationBundle extends Hashtable<String, Object> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public void putInt(String key, int value) {
        put(key, new Integer(value));
    }

    public double getDouble(String key) {
        return ((Double) get(key)).doubleValue();
    }

    public void putDouble(String key, double value) {
        put(key, new Double(value));
    }

    public int getInt(String key) {
        return ((Integer) get(key)).intValue();
    }

    public void putString(String key, String value) {
        put(key, value);
    }

    public String getString(String key) {
        return ((String) get(key));
    }

    public void putBoolean(String key, boolean sucess) {
        put(key, new Boolean(sucess));
    }

    public boolean getBoolean(String key) {
        return ((Boolean) get(key)).booleanValue();
    }

    public void putObject(String key, Object object) {
        put(key, object);
    }


    public Object getObject(String key) {
        return get(key);
    }


}
