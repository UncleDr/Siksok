package cn.edu.bit.helong.siksok;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;


public class DetailPlayerActivity extends AppCompatActivity {
    SiksokVideoPlayer detailPlayer;
    String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_detail_player);

        detailPlayer = (SiksokVideoPlayer) findViewById(R.id.detail_player);
        //增加title
        url = getIntent().getExtras().getString("videoUrl");

        detailPlayer.setUp(url, true, " ");
        detailPlayer.getTitleTextView().setVisibility(View.GONE);
        detailPlayer.setLooping(true);
        detailPlayer.setIsTouchWiget(false);
        detailPlayer.startPlayLogic();
    }

    private void loadCover(ImageView imageView, String url) {
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.place_holder);
        Glide.with(this.getApplicationContext())
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .frame(3000000)
                                .centerCrop()
                                .error(R.mipmap.error_image)//错误图
                                .placeholder(R.mipmap.place_holder))//占位图
                .load(url)
                .into(imageView);
    }

}
