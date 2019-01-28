package cn.edu.bit.helong.siksok;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.*;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Policy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.edu.bit.helong.siksok.bean.PostVideoResponse;
import cn.edu.bit.helong.siksok.newtork.IMiniDouyinService;
import cn.edu.bit.helong.siksok.newtork.RetrofitManager;
import cn.edu.bit.helong.siksok.utils.AutoFocusCallback;
import cn.edu.bit.helong.siksok.utils.UriUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.widget.Toast.LENGTH_LONG;
import static cn.edu.bit.helong.siksok.utils.Utils.MEDIA_TYPE_IMAGE;
import static cn.edu.bit.helong.siksok.utils.Utils.MEDIA_TYPE_VIDEO;
import static cn.edu.bit.helong.siksok.utils.Utils.getOutputMediaFile;

public class CustomCameraActivity extends AppCompatActivity  implements SurfaceHolder.Callback{

    private SurfaceView mSurfaceView;
    private Camera mCamera;
    public AutoFocusCallback autoFocusCallback;
    public static Handler handler;
    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int RECORD_DURATION_TIME = 10000; //ms
    public float RED_DOT_START_ANGLE = (float)(3f/2*Math.PI);
    public float ARC_MID_X = 435 + 210 / 2, ARC_MID_Y = 1606 + 187 / 2,ARC_RADIUS ;
//    Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f, viewWidth, viewHeight);

    private boolean isRecording = false;

    private int rotationDegree = 0;

    private File myLatestVideo, myLatestImage;

    private int nowCameraFacing;

    public ImageView imgRecord;
    public ProgressBar barPosting;

    CountDownTimer cdt;

    float nowY = 0, lastY = 0;

    int SCROLL_UP = 0, SCROLL_STILL = 1, SCROLL_DOWN = 2;

    Camera.Parameters cameraParameters = null;
    ImageView redDot ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_custom_camera);

        mCamera = getCamera(CAMERA_TYPE);
        mSurfaceView = findViewById(R.id.img);
//        mCamera = getCamera(CAMERA_TYPE);
        nowCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
        barPosting = findViewById(R.id.bar_posting);
        redDot = findViewById(R.id.red_dot);


        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        SurfaceHolder.Callback callback  = CustomCameraActivity.this;
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try{
                    cameraParameters = mCamera.getParameters();
                    cameraParameters.setFocusMode(Camera.Parameters.ANTIBANDING_AUTO);
                    mCamera.setPreviewDisplay(surfaceHolder);
                    mCamera.startPreview();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                releaseMediaRecorder();
                releaseCameraAndPreview();
            }
        });


        imgRecord = findViewById(R.id.img_record);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) redDot.getLayoutParams();
        layoutParams.bottomMargin = (int)((imgRecord.getLayoutParams().height / 2f) * (1.3f - 1) - layoutParams.height / 2) ;
        redDot.setLayoutParams(layoutParams);
        imgRecord.setOnClickListener(v -> {

            if (isRecording) {
                releaseMediaRecorder();
                cdt.cancel();

                editVideo(myLatestVideo);

            } else {
                mMediaRecorder = new MediaRecorder();//如果显示'this' is not available 则可能是在外部类中不可达的意思

                mCamera.unlock();
                mMediaRecorder.setCamera(mCamera);

                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

                mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
                myLatestVideo = getOutputMediaFile(MEDIA_TYPE_VIDEO);
                mMediaRecorder.setOutputFile(myLatestVideo.toString());

                mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
                mMediaRecorder.setOrientationHint(rotationDegree);

                try{
                    mMediaRecorder.prepare();
                    mMediaRecorder.start();
                    isRecording = true;

                    //imgRecord的左上角坐标是(435.0,1606.0)
                    //imgRecord的宽高是(210,187)
                    redDot.bringToFront();
                    ARC_MID_X = imgRecord.getX() + imgRecord.getWidth() / 2;
                    ARC_MID_Y = imgRecord.getY() + imgRecord.getHeight() / 2;
                    ARC_RADIUS = (imgRecord.getHeight())/ 2f * 1.3f;
                    float RED_DOT_START_XY[] = calcRedDotStartPos(RED_DOT_START_ANGLE,ARC_MID_X,ARC_MID_Y,ARC_RADIUS);
                    float rRedDot = redDot.getHeight() / 2;
                    redDot.setX(RED_DOT_START_XY[0] - rRedDot);
                    redDot.setY(RED_DOT_START_XY[1] - rRedDot);


                    cdt = new CountDownTimer(RECORD_DURATION_TIME, 32) {//interval is 100ms
                        int times = 0;
                        float[] XY;
                        @Override
                        public void onTick(long millisUntilFinished) {
                            Log.i("perform click","perform click");
//                            times++;
                            //if(){
                                XY = drawCircle((float)millisUntilFinished,ARC_RADIUS, ARC_MID_X, ARC_MID_Y,RECORD_DURATION_TIME,RED_DOT_START_ANGLE);//RED_DOT_START_ANGLE
                                redDot.setX((float)XY[0] - rRedDot);
                                redDot.setY((float)XY[1] - rRedDot);
                                redDot.bringToFront();
                                Log.i("redotxy", "X " + ((float)XY[0] - rRedDot) + "," + "Y " + ((float)XY[1] - rRedDot));
//                                times = 0;
//                            }

                        }
                        @Override
                        public void onFinish() {
                            Log.i("total time", "total time is up");
                            if (isRecording) {
                                imgRecord.performClick();
                                Log.i("middlecoordinate", "中心坐标 " + imgRecord.getX() + "," + imgRecord.getY());
                            }
                        }
                    };

                    cdt.start();
                }catch (Exception e){
                    releaseMediaRecorder();
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.img_facing).setOnClickListener(v -> {
            if(CAMERA_TYPE == Camera.CameraInfo.CAMERA_FACING_BACK){
                mCamera = getCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
            }
            else{
                mCamera = getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
            try{
                mCamera.setPreviewDisplay(mSurfaceView.getHolder());
                mCamera.startPreview();
            }catch (Exception e) {
                e.printStackTrace();
            }
        });

        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String DEBUG_TAG = "mymotiondetected";

                int action = MotionEventCompat.getActionMasked(event);

                switch(action) {
                    case (MotionEvent.ACTION_DOWN) :
                        Log.d(DEBUG_TAG,"Action was DOWN");
                        lastY = event.getY();
                        focusOnTouch((int) event.getX(), (int) event.getY());
                        return true;
                    case (MotionEvent.ACTION_MOVE) :
                        nowY = event.getY();
                        if(judgeDirection(nowY,lastY) == SCROLL_UP )
                            zoom(SCROLL_UP);
                        else if(judgeDirection(nowY, lastY) == SCROLL_DOWN)
                            zoom(SCROLL_DOWN);
                        lastY = nowY;
                        //int x = (int )event.getX();
                        Log.d(DEBUG_TAG,"Action was MOVE");
                        return true;
                    case (MotionEvent.ACTION_UP) :
                        Log.d(DEBUG_TAG,"Action was UP");
                        return true;
                    case (MotionEvent.ACTION_CANCEL) :
                        Log.d(DEBUG_TAG,"Action was CANCEL");
                        return true;
                    case (MotionEvent.ACTION_OUTSIDE) :
                        Log.d(DEBUG_TAG,"Movement occurred outside bounds " +
                                "of current screen element");
                        return true;
                    default :
                        Log.d(DEBUG_TAG,"default motion");
                }

                return true;
            }
        });
    }


    private void focusOnTouch(int x, int y) {
        Rect rect = new Rect(x - 100, y - 100, x + 100, y + 100);
        int left = rect.left * 2000 / mSurfaceView.getWidth() - 1000;
        int top = rect.top * 2000 / mSurfaceView.getHeight() - 1000;
        int right = rect.right * 2000 / mSurfaceView.getWidth() - 1000;
        int bottom = rect.bottom * 2000 / mSurfaceView.getHeight() - 1000;
        // 如果超出了(-1000,1000)到(1000, 1000)的范围，则会导致相机崩溃
        left = left < -1000 ? -1000 : left;
        top = top < -1000 ? -1000 : top;
        right = right > 1000 ? 1000 : right;
        bottom = bottom > 1000 ? 1000 : bottom;
        focusOnRect(new Rect(left, top, right, bottom));
    }


    public void focusOnRect(Rect rect) {

        String TAG = "focusOnRect";
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters(); // 先获取当前相机的参数配置对象
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); // 设置聚焦模式
            Log.d(TAG, "parameters.getMaxNumFocusAreas() : " + parameters.getMaxNumFocusAreas());
            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                focusAreas.add(new Camera.Area(rect, 1000));
                parameters.setFocusAreas(focusAreas);
            }
            mCamera.cancelAutoFocus(); // 先要取消掉进程中所有的聚焦功能
            mCamera.setParameters(parameters); // 一定要记得把相应参数设置给相机
            mCamera.autoFocus(new AutoFocusCallback());
        }
    }


    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = null;
        try{
            cam = Camera.open(CAMERA_TYPE);
            rotationDegree = getCameraDisplayOrientation(position);
            cam.setDisplayOrientation(rotationDegree);
        }catch (Exception e) {
            e.printStackTrace();
        }

        return cam;
    }


    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
//            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }


    private void releaseCameraAndPreview() {
        mCamera.stopPreview();
        mCamera.release();
        mCamera=null;
    }

    Camera.Size size;

    private void startPreview(SurfaceHolder holder) {
    }


    private MediaRecorder mMediaRecorder;

    private boolean prepareVideoRecorder() {
        return true;
    }


    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();//stop recording.
            mMediaRecorder.reset();//Restarts the MediaRecorder to its idle state.
            mMediaRecorder.release();//Releases resources associated with this MediaRecorder object.
            mMediaRecorder = null;
        }
        mCamera.lock();//Re-locks the camera to prevent other processes from accessing it.
        isRecording = false;
    }

    private Camera.PictureCallback mPicture = (data, camera) -> {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        myLatestImage = pictureFile;
        if (pictureFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            Log.d("mPicture", "Error accessing file: " + e.getMessage());
        }

        try{
            MediaStore.Images.Media.insertImage(getContentResolver(),BitmapFactory.decodeFile(myLatestImage.getAbsolutePath().toString()),myLatestImage.getName(),null);
            Uri contentUri = Uri.fromFile(new File(myLatestImage.getAbsoluteFile().toString()));
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
        }catch (Exception e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    };

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }


    public int judgeDirection(float nowy, float lasty){
        if(nowy - lasty < 0)
            return SCROLL_UP;
        else if(nowy - lasty > 0)
            return SCROLL_DOWN;
        else
            return SCROLL_STILL;
    }

    public void zoom(int direction){
        if(mCamera!=null) {
            Camera.Parameters parameter = mCamera.getParameters();

            if (parameter.isZoomSupported()) {
                int MAX_ZOOM = parameter.getMaxZoom();
                int currnetZoom = parameter.getZoom();
                if(direction == SCROLL_UP) {
                    if (currnetZoom < MAX_ZOOM && currnetZoom >=0 ) {
                        parameter.setZoom(++currnetZoom);
                        mCamera.setParameters(parameter);
                    }
                }
                else if(direction == SCROLL_DOWN){
                    if (currnetZoom <= MAX_ZOOM && currnetZoom >0 ) {
                        parameter.setZoom(--currnetZoom);
                        mCamera.setParameters(parameter);
                    }
                }

            } else
                Toast.makeText(this, "Zoom Not Avaliable", Toast.LENGTH_LONG).show();
        }
    }


//    private static void handleFocus(MotionEvent event, Camera camera) {
//        int viewWidth = getWidth();
//        int viewHeight = getHeight();
//        Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f, viewWidth, viewHeight);
//
//        camera.cancelAutoFocus();
//        Camera.Parameters params = camera.getParameters();
//        if (params.getMaxNumFocusAreas() > 0) {
//            List<Camera.Area> focusAreas = new ArrayList<>();
//            focusAreas.add(new Camera.Area(focusRect, 800));
//            params.setFocusAreas(focusAreas);
//        } else {
//            Log.i(TAG, "focus areas not supported");
//        }
//        final String currentFocusMode = params.getFocusMode();
//        params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
//        camera.setParameters(params);
//
//        camera.autoFocus(new Camera.AutoFocusCallback() {
//            @Override
//            public void onAutoFocus(boolean success, Camera camera) {
//                Camera.Parameters params = camera.getParameters();
//                params.setFocusMode(currentFocusMode);
//                camera.setParameters(params);
//            }
//        });
//    }



    private static List<Camera.Area> buildMiddleArea(int areaPer1000) {
        return Collections.singletonList(
                new Camera.Area(new Rect(-areaPer1000, -areaPer1000, areaPer1000, areaPer1000), 1));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //todo 释放Camera和MediaRecorder资源
    }

    public void postVideo() {

        Bitmap bmp = extractFrame(0);
        Uri uriBmp = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bmp, null,null));

//        File tmpImage = new File("/sdcard/DCIM/Camera/IMG_20190127_183651.jpg");
//        File tmpImage = new File("/storage/emulated/0/DCIM/Camera/IMG_20190122_211543.jpg");
//        File tmpVideo = new File("/storage/emulated/0/DCIM/Camera/VID_20190127_172830.mp4");
        Retrofit retrofit = RetrofitManager.get("http://10.108.10.39:8080");

        retrofit.create(IMiniDouyinService.class).postVideo("1120151026", "何龙",
                CommonMethod.getMultipartFromUri("cover_image",uriBmp,CustomCameraActivity.this),
                CommonMethod.getMultipartFromUri("video",Uri.fromFile(myLatestVideo),CustomCameraActivity.this)).
                enqueue(new Callback<PostVideoResponse>() {
                    @Override
                    public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                        Toast.makeText(CustomCameraActivity.this,"Success ",Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        CustomCameraActivity.this.finish();
                    }

                    @Override
                    public void onFailure(Call<PostVideoResponse> call, Throwable throwable) {
                        Toast.makeText(CustomCameraActivity.this,throwable.getMessage(),LENGTH_LONG).show();
                        setResult(RESULT_CANCELED);
                        CustomCameraActivity.this.finish();
                    }
                });
        barPosting.bringToFront();
        barPosting.setVisibility(View.VISIBLE);
    }

    public Bitmap extractFrame(long timeMs) {
        //第一个参数是传入时间，只能是us(微秒)
        //OPTION_CLOSEST ,在给定的时间，检索最近一个帧,这个帧不一定是关键帧。
        //OPTION_CLOSEST_SYNC   在给定的时间，检索最近一个同步与数据源相关联的的帧（关键帧）
        //OPTION_NEXT_SYNC 在给定时间之后检索一个同步与数据源相关联的关键帧。
        //OPTION_PREVIOUS_SYNC 在给定时间之前检索一个同步与数据源相关联的关键帧。

// Bitmap bitmap = mMetadataRetriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        MediaMetadataRetriever mMetadataRetriever = new MediaMetadataRetriever();
        mMetadataRetriever.setDataSource(myLatestVideo.getAbsolutePath());
        String w = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String h = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        long fileLength = 3000;

        Bitmap bitmap = null;
        for (long i = timeMs; i < fileLength; i += 1000) {
            bitmap = mMetadataRetriever.getFrameAtTime(i * 1000, MediaMetadataRetriever.OPTION_CLOSEST);//OPTION_CLOSEST_SYNC
            if (bitmap != null) {
                break;
            }
        }
        mMetadataRetriever.release();
        return bitmap;
    }

    public void editVideo(File video) {
        postVideo();
    }

    public float[] drawCircle(float t, float R, float x, float y, float totalT, float startAngle){
        float twoPi = (float)Math.PI * 2;
        float angle =  t/totalT * twoPi + startAngle;
        float[] coordinate = new float[2];
        coordinate[0] = (float)(x + R * Math.cos(angle));
        coordinate[1] = (float)(y - R * Math.sin(twoPi - angle) );

        Log.i("angle", String.valueOf(angle));
        return coordinate;
    }

    public float[] calcRedDotStartPos(float startAngle, float midX, float midY, float R) {
        float[] XY = new float[2];
        XY[0] = midX + R * (float)Math.cos(startAngle);
        XY[1] = midY - R * (float)Math.sin(startAngle);
        return XY;
    }

}