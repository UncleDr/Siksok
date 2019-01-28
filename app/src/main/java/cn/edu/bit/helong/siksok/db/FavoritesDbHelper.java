package cn.edu.bit.helong.siksok.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static cn.edu.bit.helong.siksok.db.FavoritesContract.SQL_CREATE_ENTRIES;

public class FavoritesDbHelper extends SQLiteOpenHelper {

    public static  int DATABASE_VERSION ;
    public static final String DATABASE_NAME = "FavoritesContract.db";

    public FavoritesDbHelper(Context context, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION = version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

//        for(int i = oldVersion; i < newVersion;  i++) {
//            switch (i) {
//                case 1:
//                    try {
//                        db.execSQL("ALTER TABLE " + FavoritesContract.FeedEntry.TABLE_NAME + " ADD " + EXTRA + " INTEGER");
//                    }catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                default:
//                    break;
//            }
//        }
//        db.execSQL(SQL_DELETE_ENTRIES);
//        onCreate(db);
    }

}
