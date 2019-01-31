package cn.edu.bit.helong.siksok;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
        setStatusBarTransparent(this);

        initRecyclerView();
        initDateBase();
        fetchFeed();
    }

    public static void setStatusBarTransparent(Activity activity) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    private void playVideo(int position) {
        Log.i(TAG, "play video " + position + " rv:" + mRv.getChildCount());
        SiksokVideoPlayer player = mRv.getChildAt(position).findViewById(R.id.detail_player);
        player.startPlayLogic();
    }

    private void releaseVideo(int position) {
        Log.i(TAG, "release video " + position);

        SiksokVideoPlayer player = mRv.getChildAt(position).findViewById(R.id.detail_player);
        player.release();
    }

    private void initRecyclerView() {
        mRv = findViewById(R.id.rv);
        ViewPagerLayoutManager manager = new ViewPagerLayoutManager(this, OrientationHelper.VERTICAL);
        manager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            //Play video when a view is attached to the RecyclerView
            public void onInitComplete() {
                Log.e(TAG,"onInitComplete");
                playVideo(0);
            }

            @Override

            //Stop playing the playing video when a view is detached to the RecyclerView
            public void onPageRelease(boolean isNext,int position) {
                Log.e(TAG,"释放位置:"+position +" 下一页:"+isNext);
                int index = 0;
                if (isNext){
                    index = 0;
                }else {
                    index = 1;
                }

                releaseVideo(0);
            }

            @Override
            //Play video when the number of children in the ViewGroup is 1.
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
            //Add the video information to database.
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

        if (resultCode == RESULT_OK && requestCode == RECORD_REQUEST_CODE) {
            fetchFeed();
        }
    }

    public MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(MainActivity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }


    public void fetchFeed() {
        Retrofit retrofit = RetrofitManager.get("http://10.108.10.39:8080");

        retrofit.create(IMiniDouyinService.class).fetchFeed().
                enqueue(new Callback<FeedResponse>() {
                    @Override
                    /*
                     * Get the feeds and notify the adapter data has changed.
                     */
                    public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                        mFeeds = response.body().feeds;
                        ((FeedsAdapter)mRv.getAdapter()).setFeeds(mFeeds);
                        mRv.getAdapter().notifyDataSetChanged();
                    }

                    @Override

                    public void onFailure(Call<FeedResponse> call, Throwable throwable) {
                        Toast.makeText(MainActivity.this, "fetch failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initDateBase() {
        FavoritesDbHelper favoritesDbHelper = new FavoritesDbHelper(MainActivity.this, FIRST_DB_VERSION);
        favoritesDatabase = favoritesDbHelper.getWritableDatabase();
    }

    //Enter the favorite page.
    public void enterFavorites(View view) {
        Intent intent = new Intent();
        intent.setClass(this, FavoritesActivity.class);
        startActivity(intent);
    }

    public int RECORD_REQUEST_CODE = 1;


    public void enterCustomCamera(View view) {
        /* Apply all needy permission before enter the record page. */
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, PERMISSION_RECORDVIDEO, REQUEST_PERMISSION_CODE);
        else{
            startActivityForResult(new Intent().setClass(this, CustomCameraActivity.class),RECORD_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_CAMERA: {
                /* Enter the record page after getting all needy permission. */
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
