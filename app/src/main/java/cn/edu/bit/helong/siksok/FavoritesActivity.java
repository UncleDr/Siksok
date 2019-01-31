package cn.edu.bit.helong.siksok;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import cn.edu.bit.helong.siksok.bean.Favorites;
import cn.edu.bit.helong.siksok.db.FavoritesContract;
import cn.edu.bit.helong.siksok.db.FavoritesDbHelper;

public class FavoritesActivity extends AppCompatActivity {

    private static final int FIRST_DB_VERSION = 1;

    public RecyclerView rv;
    SQLiteDatabase favoritesDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        FavoritesDbHelper favoritesDbHelper = new FavoritesDbHelper(this, FIRST_DB_VERSION);
        favoritesDatabase = favoritesDbHelper.getReadableDatabase();

        rv = findViewById(R.id.rv_favorites);
        GridLayoutManager layoutManager = new GridLayoutManager(this,3);
        rv.setLayoutManager(layoutManager);
        FavoritesAdapter favoritesAdapter = new FavoritesAdapter(favoritesDatabase);

        rv.setAdapter(favoritesAdapter);
    }


}
