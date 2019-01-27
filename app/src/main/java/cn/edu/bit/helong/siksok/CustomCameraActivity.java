package cn.edu.bit.helong.siksok;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.camera2.*;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Policy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.edu.bit.helong.siksok.utils.AutoFocusCallback;

import static cn.edu.bit.helong.siksok.utils.Utils.MEDIA_TYPE_IMAGE;
import static cn.edu.bit.helong.siksok.utils.Utils.MEDIA_TYPE_VIDEO;
import static cn.edu.bit.helong.siksok.utils.Utils.getOutputMediaFile;

public class CustomCameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;
    private Camera mCamera;
    public AutoFocusCallback autoFocusCallback;
    public static Handler handler;
    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int RECORD_DURATION_TIME = 10000; //ms
//    Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f, viewWidth, viewHeight);

    private boolean isRecording = false;

    private int rotationDegree = 0;

    private File myLatestVideo, myLatestImage;

    private int nowCameraFacing;

    public Button btnRecord;

    CountDownTimer cdt;

    float nowY = 0, lastY = 0;

    int SCROLL_UP = 0, SCROLL_STILL = 1, SCROLL_DOWN = 2;
    private static final int MSG_AUTOFUCS = 1001;


    Camera.Parameters cameraParameters = null;

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

        findViewById(R.id.btn_picture).setOnClickListener(v -> {

            mCamera.takePicture(null,null,mPicture);
        });

        btnRecord = findViewById(R.id.btn_record);
        btnRecord.setOnClickListener(v -> {

            if (isRecording) {
                releaseMediaRecorder();

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

//                    cdt = new CountDownTimer(RECORD_DURATION_TIME, RECORD_DURATION_TIME) {
//                        @Override
//                        public void onTick(long millisUntilFinished) {
//                            btnRecord.performClick();
//                            Log.i("perform click","perform click");
//                        }
//                        @Override
//                        public void onFinish() {
//                            Log.i("total time", "total time is up");
//                        }
//                    };
//
//                    cdt.start();
                }catch (Exception e){
                    releaseMediaRecorder();
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.btn_facing).setOnClickListener(v -> {
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

        findViewById(R.id.btn_zoom).setOnClickListener(v -> {
            if(mCamera!=null) {
                Camera.Parameters parameter = mCamera.getParameters();

                if (parameter.isZoomSupported()) {
                    int MAX_ZOOM = parameter.getMaxZoom();
                    int currnetZoom = parameter.getZoom();
                    if (currnetZoom <= MAX_ZOOM) {
                        parameter.setZoom(++currnetZoom);
                        mCamera.setParameters(parameter);
                    }
                } else
                    Toast.makeText(this, "Zoom Not Avaliable", Toast.LENGTH_LONG).show();
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



    public void StoreToAlbum() {
        //scanFile(CustomCameraActivity.this, filePath);
        try{
            MediaStore.Images.Media.insertImage(getContentResolver(),BitmapFactory.decodeFile(myLatestImage.getAbsolutePath().toString()),myLatestImage.getName(),null);
            Uri contentUri = Uri.fromFile(new File(myLatestImage.getAbsoluteFile().toString()));

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
        }catch (Exception e) {
            e.printStackTrace();
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
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
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
        mMediaRecorder.stop();//stop recording.
        mMediaRecorder.reset();//Restarts the MediaRecorder to its idle state.
        mMediaRecorder.release();//Releases resources associated with this MediaRecorder object.
        mMediaRecorder = null;
        mCamera.lock();//Re-locks the camera to prevent other processes from accessing it.
        isRecording = false;
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

    private static Rect calculateTapArea(float x, float y, float coefficient, int width, int height) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / width * 2000 - 1000);
        int centerY = (int) (y / height * 2000 - 1000);

        int halfAreaSize = areaSize / 2;
        RectF rectF = new RectF(clamp(centerX - halfAreaSize, -1000, 1000)
                , clamp(centerY - halfAreaSize, -1000, 1000)
                , clamp(centerX + halfAreaSize, -1000, 1000)
                , clamp(centerY + halfAreaSize, -1000, 1000));
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
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

    //sean owen
    private static final int AREA_PER_1000 = 400;
    public static void setFocusArea(Camera.Parameters parameters) {
        String TAG = "owen's setfocusarea";
        if (parameters.getMaxNumFocusAreas() > 0) {
            Log.i(TAG, "Old focus areas: " + (parameters.getFocusAreas()));
            List<Camera.Area> middleArea = buildMiddleArea(AREA_PER_1000);
            Log.i(TAG, "Setting focus area to : " + (middleArea));
            parameters.setFocusAreas(middleArea);
        } else {
            Log.i(TAG, "Device does not support focus areas");
        }
    }

    private static List<Camera.Area> buildMiddleArea(int areaPer1000) {
        return Collections.singletonList(
                new Camera.Area(new Rect(-areaPer1000, -areaPer1000, areaPer1000, areaPer1000), 1));
    }
}


