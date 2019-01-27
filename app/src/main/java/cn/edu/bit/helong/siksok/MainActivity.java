package cn.edu.bit.helong.siksok;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.edu.bit.helong.siksok.bean.Feed;
import cn.edu.bit.helong.siksok.bean.FeedResponse;
import cn.edu.bit.helong.siksok.bean.PostVideoResponse;
import cn.edu.bit.helong.siksok.db.FavoritesContract;
import cn.edu.bit.helong.siksok.db.FavoritesDbHelper;
import cn.edu.bit.helong.siksok.newtork.IMiniDouyinService;
import cn.edu.bit.helong.siksok.newtork.RetrofitManager;
import cn.edu.bit.helong.siksok.utils.ResourceUtils;
import cn.edu.bit.helong.siksok.views.OnViewPagerListener;
import cn.edu.bit.helong.siksok.views.ViewPagerLayoutManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {

    private static final int FIRST_DB_VERSION = 1;
    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRv;
    private List<Feed> mFeeds = new ArrayList<>();
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    public Button mBtn;
    private Button mBtnRefresh;
    public SQLiteDatabase favoritesDatabase;

    private static String[] PERMISSION_RECORDVIDEO = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    private static int REQUEST_PERMISSION_CODE = 1;

    private static final int REQUEST_EXTERNAL_CAMERA = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRecyclerView();
        initBtns();
        initDateBase();


        /*实现双击点赞功能*/
//        mRv.getAdapter().ListItemClickListener
//                FeedsAdapter.ListItemClickListener listener = new FeedsAdapter.ListItemClickListener() {
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

    private void playVideo(int position) {
        Log.i(TAG, "play video " + position + " rv:" + mRv.getChildCount());
        StandardGSYVideoPlayer player = mRv.getChildAt(position).findViewById(R.id.detail_player);
        player.startPlayLogic();
    }

    private void releaseVideo(int position) {
        Log.i(TAG, "release video " + position);

        StandardGSYVideoPlayer player = mRv.getChildAt(position).findViewById(R.id.detail_player);
        player.release();
    }

    private void initRecyclerView() {
        mRv = findViewById(R.id.rv);
        ViewPagerLayoutManager manager = new ViewPagerLayoutManager(this, OrientationHelper.VERTICAL);
        manager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            public void onInitComplete() {
                Log.e(TAG,"onInitComplete");
                playVideo(0);
            }

            @Override
            public void onPageRelease(boolean isNext,int position) {
                Log.e(TAG,"释放位置:"+position +" 下一页:"+isNext);
                int index = 0;
                if (isNext){
                    index = 0;
                }else {
                    index = 1;
                }

                releaseVideo(index);
            }

            @Override
            public void onPageSelected(int position,boolean isBottom) {
                Log.e(TAG,"选中位置:"+position+"  是否是滑动到底部:"+isBottom);

                playVideo(0);
            }

        });
        mRv.setLayoutManager(manager);
        FeedsAdapter feedsAdapter = new FeedsAdapter(MainActivity.this);
        feedsAdapter.setAddToFavoritesListener(new FeedsAdapter.AddToFavoritesListener() {
            @Override
            public void SpecialEffect() {

            }

            @Override
            public boolean AddToDB(Feed feed) {
                try{
                    ContentValues values = new ContentValues();
                    values.put(FavoritesContract.FeedEntry.COLUMN_NAME_NAME, feed.userName);
                    values.put(FavoritesContract.FeedEntry.COLUMN_NAME_NO, feed.studentId);
                    values.put(FavoritesContract.FeedEntry.COLUMN_NAME_URL_IMAGE, feed.imageUrl);
                    values.put(FavoritesContract.FeedEntry.COLUMN_NAME_URL_VIDEO, feed.videoUrl);
                    long newRowId = favoritesDatabase.insert(FavoritesContract.FeedEntry.TABLE_NAME, null, values);

                    Log.i("addtodb", "comein");
                }catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        });
        mRv.setAdapter(feedsAdapter);
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

    public MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
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
                        Toast.makeText(MainActivity.this,throwable.getMessage(),LENGTH_LONG).show();

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
                            ((FeedsAdapter)mRv.getAdapter()).insertFeed(mFeeds.get(i));
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
        favoritesDatabase = favoritesDbHelper.getWritableDatabase();
    }

    public void enterFavorites(View view) {
        Intent intent = new Intent();
        intent.setClass(this, FavoritesActivity.class);
        startActivity(intent);
    }

    public void enterCustomCamera(View view) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, PERMISSION_RECORDVIDEO, REQUEST_PERMISSION_CODE);
        else{
            startActivity(new Intent().setClass(this, CustomCameraActivity.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_CAMERA: {

                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("get camera success","Get camera permission success.");
                    startActivity(new Intent().setClass(this, CustomCameraActivity.class));
                }
                else {
                    Toast.makeText(this, "Get camera permission failed.", Toast.LENGTH_LONG).show();
                    Log.i("get camera failed","Get camera permission failed.");
                }
                break;

            }
        }
    }
}
