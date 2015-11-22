/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.trendingapps.analytics;

import android.provider.BaseColumns;

public class UnsentDataContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public UnsentDataContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "unsentdata";
        public static final String COLUMN_NAME_EVENT_CATEGORY = "EVENT_CATEGORY";
        public static final String COLUMN_NAME_EVENT_ACTION = "EVENT_ACTION";
        public static final String COLUMN_NAME_EVENT_LABEL = "EVENT_LABEL";
        public static final String COLUMN_NAME_EVENT_VALUE = "EVENT_VALUE";
        public static final String COLUMN_NAME_EVENT_TIME = "EVENT_TIME";
    }


    public static final String TEXT_TYPE = " TEXT";
    public static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
            + FeedEntry.TABLE_NAME + " (" + FeedEntry._ID
            + " INTEGER PRIMARY KEY," + FeedEntry.COLUMN_NAME_EVENT_CATEGORY
            + TEXT_TYPE + COMMA_SEP + FeedEntry.COLUMN_NAME_EVENT_ACTION
            + TEXT_TYPE + COMMA_SEP + FeedEntry.COLUMN_NAME_EVENT_LABEL
            + TEXT_TYPE + COMMA_SEP + FeedEntry.COLUMN_NAME_EVENT_VALUE
            + TEXT_TYPE + COMMA_SEP + FeedEntry.COLUMN_NAME_EVENT_TIME
            + TEXT_TYPE +" )";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
            + FeedEntry.TABLE_NAME;

}