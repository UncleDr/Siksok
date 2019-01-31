package cn.edu.bit.helong.siksok;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTargetFactory;

import java.util.ArrayList;
import java.util.List;

import cn.edu.bit.helong.siksok.bean.Favorites;
import cn.edu.bit.helong.siksok.bean.Feed;
import cn.edu.bit.helong.siksok.db.FavoritesContract;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    SQLiteDatabase mFavoritesDatabase;
    List<Favorites> favoritesList = new ArrayList<>();


    public FavoritesAdapter (SQLiteDatabase sqLiteDatabase) {
        //Add all favorite video information to List<Favorites> in constructor.
        this.mFavoritesDatabase = sqLiteDatabase;
        String[] projection = {
                BaseColumns._ID,
                FavoritesContract.FeedEntry.COLUMN_NAME_NO,
                FavoritesContract.FeedEntry.COLUMN_NAME_NAME,
                FavoritesContract.FeedEntry.COLUMN_NAME_URL_IMAGE,
                FavoritesContract.FeedEntry.COLUMN_NAME_URL_VIDEO
        };

        String sortOrder = BaseColumns._ID.toString() + " DESC";
        Cursor cursor = null;
        try {
            cursor = mFavoritesDatabase.query(FavoritesContract.FeedEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    sortOrder);

            while (cursor.moveToNext()) {
                long no = cursor.getLong(cursor.getColumnIndex(FavoritesContract.FeedEntry.COLUMN_NAME_NO));
                String name = cursor.getString(cursor.getColumnIndex(FavoritesContract.FeedEntry.COLUMN_NAME_NAME));
                String videoUrl = cursor.getString(cursor.getColumnIndex(FavoritesContract.FeedEntry.COLUMN_NAME_URL_VIDEO));
                String imageUrl = cursor.getString(cursor.getColumnIndex(FavoritesContract.FeedEntry.COLUMN_NAME_URL_IMAGE));
                int ID = cursor.getInt(cursor.getColumnIndex(FavoritesContract.FeedEntry._ID));

                Favorites favorites = new Favorites(ID,
                        no,
                        name,
                        videoUrl,
                        imageUrl);
                favoritesList.add(favorites);
            }
        }finally {
            if(cursor != null)
                cursor.close();
        }
    }
    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_favorites, viewGroup, false) ;
        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder favoritesViewHolder, int i) {
        final int position = i;
        Favorites tmp = favoritesList.get(position);

        ImageView iv = favoritesViewHolder.ivLittleImageCover;
        Glide.with(iv.getContext()).load(tmp.getUrlImage()).into(iv);

        TextView tv = favoritesViewHolder.tvBriefInfo;

        tv.setText(tmp.getNo() + "\n" + tmp.getName());

        // Play video after clicking the video cover.
        iv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(v.getContext(), DetailPlayerActivity.class);
                intent.putExtra("videoUrl", tmp.getUrlVideo());
                v.getContext().startActivity(intent);
            }
        });

        // Delete a video data in database after a long click on a video cover.
        iv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String selection = FavoritesContract.FeedEntry._ID + " = ?";
                String[] selectionArgs = { String.valueOf(tmp.id) };
                int deletedRows = mFavoritesDatabase.delete(FavoritesContract.FeedEntry.TABLE_NAME,selection,selectionArgs);
                favoritesList.remove(position);
                notifyItemRemoved(position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoritesList.size();
    }

    public class FavoritesViewHolder extends RecyclerView.ViewHolder {

        public ImageView ivLittleImageCover ;
        public TextView tvBriefInfo;
        public FavoritesViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLittleImageCover = itemView.findViewById(R.id.iv_little_image_cover);
            tvBriefInfo = itemView.findViewById(R.id.tv_brief_info);
        }
    }
}
