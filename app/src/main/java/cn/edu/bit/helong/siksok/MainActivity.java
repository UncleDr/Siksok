package cn.edu.bit.helong.siksok;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.edu.bit.helong.siksok.bean.Feed;
import cn.edu.bit.helong.siksok.bean.FeedResponse;
import cn.edu.bit.helong.siksok.bean.PostVideoResponse;
import cn.edu.bit.helong.siksok.db.FavoritesDbHelper;
import cn.edu.bit.helong.siksok.newtork.IMiniDouyinService;
import cn.edu.bit.helong.siksok.newtork.RetrofitManager;
import cn.edu.bit.helong.siksok.utils.ResourceUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final int FIRST_DB_VERSION = 1;
    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final String TAG = "Solution2C2Activity";
    private RecyclerView mRv;
    private List<Feed> mFeeds = new ArrayList<>();
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    public Button mBtn;
    private Button mBtnRefresh;
    public SQLiteDatabase favoritesDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRecyclerView();
        initBtns();
        initDateBase();


        /*实现双击点赞功能*/
//        mRv.getAdapter().ListItemClickListener
//                MyAdapter.ListItemClickListener listener = new MyAdapter.ListItemClickListener() {
//            @Override
//            public void onListItemClick(int clickedItemIndex) {
//                Intent intent = new Intent();
//                intent.setClass(Exercises3.this,Chatroom.class);
//                startActivity(intent);
//            }
//        };
//        myAdapter.setListItemClickListener(listener);
    }

    private void initBtns() {
        mBtn = findViewById(R.id.btn);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String s = mBtn.getText().toString();
                if (getString(R.string.select_an_image).equals(s)) {
                    chooseImage();
                } else if (getString(R.string.select_a_video).equals(s)) {
                    chooseVideo();
                } else if (getString(R.string.post_it).equals(s)) {
                    if (mSelectedVideo != null && mSelectedImage != null) {
                        postVideo();
                    } else {
                        throw new IllegalArgumentException("error data uri, mSelectedVideo = " + mSelectedVideo + ", mSelectedImage = " + mSelectedImage);
                    }
                } else if ((getString(R.string.success_try_refresh).equals(s))) {
                    mBtn.setText(R.string.select_an_image);
                }
            }
        });

        mBtnRefresh = findViewById(R.id.btn_refresh);
    }



    private void initRecyclerView() {
        mRv = findViewById(R.id.rv);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        manager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mRv.setLayoutManager(manager);
        MyAdapter myAdapter = new MyAdapter(MainActivity.this);
        mRv.setAdapter(myAdapter);
    }

    public void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);

    }


    public void chooseVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, PICK_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

        if (resultCode == RESULT_OK && null != data) {

            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                Log.d(TAG, "selectedImage = " + mSelectedImage);
                mBtn.setText(R.string.select_a_video);
            } else if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);
                mBtn.setText(R.string.post_it);
            }
        }
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(MainActivity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    private void postVideo() {
        mBtn.setText("POSTING...");
        mBtn.setEnabled(false);

        // if success, make a text Toast and show
        Retrofit retrofit = RetrofitManager.get("http://10.108.10.39:8080");

        retrofit.create(IMiniDouyinService.class).postVideo(RequestBody.create(MediaType.get("text/plain"),"1120151026"),
                RequestBody.create(MediaType.get("text/plain"),"何龙"),
                getMultipartFromUri("cover_image",mSelectedImage), getMultipartFromUri("video",mSelectedVideo)).
                enqueue(new Callback<PostVideoResponse>() {
                    @Override
                    public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                        Toast.makeText(MainActivity.this,"Success " + response.body(),Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<PostVideoResponse> call, Throwable throwable) {
                        Toast.makeText(MainActivity.this,throwable.getMessage(),Toast.LENGTH_LONG).show();

                    }
                });

    }

    public void fetchFeed(View view) {
        mBtnRefresh.setText("requesting...");
        mBtnRefresh.setEnabled(false);

        Retrofit retrofit = RetrofitManager.get("http://10.108.10.39:8080");

        retrofit.create(IMiniDouyinService.class).fetchFeed().
                enqueue(new Callback<FeedResponse>() {
                    @Override
                    public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                        mFeeds = response.body().feeds;
                        for (int i = 0; i < mFeeds.size(); i ++) {
                            ((MyAdapter)mRv.getAdapter()).insertFeed(mFeeds.get(i));
                            mRv.getAdapter().notifyItemInserted(i);
                        }
                        resetRefreshBtn();
                    }

                    @Override
                    public void onFailure(Call<FeedResponse> call, Throwable throwable) {
                        Toast.makeText(MainActivity.this, "fetch failed", Toast.LENGTH_SHORT).show();
                        resetRefreshBtn();

                    }
                });
    }

    private void resetRefreshBtn() {
        mBtnRefresh.setText(R.string.refresh_feed);
        mBtnRefresh.setEnabled(true);
    }

    private void initDateBase() {
        FavoritesDbHelper favoritesDbHelper = new FavoritesDbHelper(MainActivity.this, FIRST_DB_VERSION);
        favoritesDatabase = favoritesDbHelper.getReadableDatabase();
    }
}
