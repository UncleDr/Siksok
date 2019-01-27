package cn.edu.bit.helong.siksok.views;


import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class DoubleClickImageView extends AppCompatImageView {

    GestureDetector gestureDetector;
    public DoubleClickListener mListener;


    public DoubleClickImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // creating new gesture detector
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public DoubleClickImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        gestureDetector = new GestureDetector(context, new GestureListener());

    }

    // skipping measure calculation and drawing

    // delegate the event to the gesture detector
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //Single Tap
        return gestureDetector.onTouchEvent(e);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            float x = e.getX();
//            float y = e.getY();
//
//            Log.d("Double Tap", "Tapped at: (" + x + "," + y + ")");
//
//            return true;
            mListener.onDoubleClick(DoubleClickImageView.this, (int)e.getX(), (int)e.getY());
            return true;

        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mListener.onSingleClick(DoubleClickImageView.this);
            return true;
        }
    }

    public interface DoubleClickListener {
        void onDoubleClick(View view, int x, int y);
        void onSingleClick(View view);

    }

    public void setOnDoubleClickListener( DoubleClickListener listener) {
        this.mListener = listener ;
    }

}