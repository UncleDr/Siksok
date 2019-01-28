package cn.edu.bit.helong.siksok;

import android.animation.Animator;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

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

    public void setFeeds(@NonNull List<Feed> feeds) {
         mFeeds = feeds;
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
        String videoInfo = mFeeds.get(i).userName;
        String imageurl = mFeeds.get(i).imageUrl;

//        int myheight = viewHolder.mImageCover.getLayoutParams().height;
//        int mywidth = viewHolder.mImageCover.getLayoutParams().width;

//        Glide.with(iv.getContext()).load(imageurl).into(iv);
        ((MyViewHolder) viewHolder).mVideoName.setText(videoInfo);
        viewHolder.mStudentId.setText(mFeeds.get(i).studentId);
        iv.setOnDoubleClickListener(new DoubleClickImageView.DoubleClickListener (){
            @Override
            public void onDoubleClick (View view, int x, int y){
                addToFavoritesListener.SpecialEffect();
                addToFavoritesListener.AddToDB(mFeeds.get(i));
                viewHolder.picClickGood.setVisibility(View.VISIBLE);
                viewHolder.picClickGood.setX(x - viewHolder.width/2);
                viewHolder.picClickGood.setY(y - viewHolder.height/2);
                viewHolder.picClickGood.playAnimation();
                viewHolder.picClickGood.addAnimatorListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        viewHolder.picClickGood.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }

            @Override
            public void onSingleClick(View view) {
//                Intent intent = new Intent(mContext, DetailPlayerActivity.class);
//                intent.putExtra("videoUrl", videourl);
//                mContext.startActivity(intent);
            }

        });

        Log.i(MainActivity.class.getSimpleName(), "bind " + i);

        viewHolder.detailPlayer.setUp(videourl, true, "");
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
        public TextView mVideoName;
        public TextView mStudentId;
        public StandardGSYVideoPlayer detailPlayer;
        public LottieAnimationView picClickGood;
        int height, width;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageCover = itemView.findViewById(R.id.iv_image_cover);


            mVideoName = itemView.findViewById(R.id.tv_feed_name);
            mStudentId = itemView.findViewById(R.id.tv_feed_id);
            picClickGood = itemView.findViewById(R.id.pic_click_good);
            height = picClickGood.getLayoutParams().height;
            width = picClickGood.getLayoutParams().width;

            picClickGood.setVisibility(View.INVISIBLE);
            detailPlayer = itemView.findViewById(R.id.detail_player);

            detailPlayer.getTitleTextView().setVisibility(View.GONE);
            detailPlayer.setLooping(true);
            detailPlayer.setIsTouchWiget(false);
            detailPlayer.getBackButton().setVisibility(View.GONE);
            detailPlayer.getStartButton().setVisibility(View.GONE);

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





