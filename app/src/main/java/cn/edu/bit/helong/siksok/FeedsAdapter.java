package cn.edu.bit.helong.siksok;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.edu.bit.helong.siksok.bean.Feed;
import cn.edu.bit.helong.siksok.views.DoubleClickImageView;

public class FeedsAdapter extends RecyclerView.Adapter<FeedsAdapter.MyViewHolder> {
//
//    ListItemClickListener listener;
    List<Feed> mFeeds = new ArrayList<>();
    Context mContext;
    public SQLiteDatabase favoritesDatabase;
    public AddToFavoritesListener addToFavoritesListener;

    //
     FeedsAdapter(Context context) {
        //mOnClickListener = listener;
        //List<Message> data,
        mContext = context;
    }

    public void insertFeed(@NonNull Feed feed) {
         mFeeds.add(feed);
    }

    @NonNull @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_feed, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public int getItemCount(){ return mFeeds.size();}

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
        DoubleClickImageView iv = (DoubleClickImageView)((MyViewHolder)viewHolder).mImageCover;

        String videourl = mFeeds.get(i).videoUrl;
        String videoInfo = mFeeds.get(i).studentId + "\n" + mFeeds.get(i).userName ;
        String imageurl = mFeeds.get(i).imageUrl;


        Glide.with(iv.getContext()).load(imageurl).into(iv);
        ((MyViewHolder) viewHolder).mVideoInfo.setText(videoInfo);

        iv.setOnDoubleClickListener(new DoubleClickImageView.DoubleClickListener (){
            @Override
            public void onDoubleClick (View view){
                Toast.makeText(mContext, "asd",Toast.LENGTH_SHORT).show();
                addToFavoritesListener.SpecialEffect();
                addToFavoritesListener.AddToDB(mFeeds.get(i));
            }

            @Override
            public void onSingleClick(View view) {
                Intent intent = new Intent(mContext, DetailPlayerActivity.class);
                intent.putExtra("videoUrl", videourl);
                mContext.startActivity(intent);
            }

        });
    }

    public interface AddToFavoritesListener {
         void SpecialEffect();
         boolean AddToDB(Feed feed);
    }

    public void setAddToFavoritesListener(FeedsAdapter.AddToFavoritesListener listener ){
        addToFavoritesListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageCover;
        public TextView mVideoInfo;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageCover = itemView.findViewById(R.id.iv_image_cover);
            mVideoInfo = itemView.findViewById(R.id.tv_feed_info);
        }
    }

}

//
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_feed, viewGroup, false);
//        return new MainActivity.MyViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
//        Message message = mData.get(i);
//        MyViewHolder viewHolder = (MyViewHolder) myViewHolder;
//        myViewHolder.updateUI(message, i);
//        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                listener.onListItemClick(i);
//            }
//        });
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return mData.size();
//    }
//

//
//    public interface ListItemClickListener {
//        void onListItemClick(int clickedItemIndex);
//    }
//
//
//    public void setListItemClickListener(FeedsAdapter.ListItemClickListener listener) {
//        this.listener = listener;
//    }





