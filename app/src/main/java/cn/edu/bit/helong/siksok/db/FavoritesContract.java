package cn.edu.bit.helong.siksok.db;

import android.provider.BaseColumns;

public final class FavoritesContract {
    private FavoritesContract() {
    }

    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "my_favorites";
        public static final String COLUMN_NAME_URL_IMAGE = "url_image";
        public static final String COLUMN_NAME_URL_VIDEO = "url_video";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_NO = "no";
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_NAME + " TEXT," +
                    FeedEntry.COLUMN_NAME_NO + " INTEGER," +
                    FeedEntry.COLUMN_NAME_URL_VIDEO + " TEXT," +
                    FeedEntry.COLUMN_NAME_URL_IMAGE + " TEXT)" ;



    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
}
