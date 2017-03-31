package com.sscience.stopapp.widget;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/3/29
 */

public class MoveFloatingActionButton extends FloatingActionButton implements View.OnTouchListener {

    private Context mContext;
    private float mPosY;
    private float mCurPosY;

    public MoveFloatingActionButton(Context context) {
        super(context);
        mContext = context;
        setOnTouchListener(this);
    }

    public MoveFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPosY = event.getRawY();
                mCurPosY = mPosY;
                break;
            case MotionEvent.ACTION_MOVE:
                mCurPosY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                int touchSlop = ViewConfiguration.get(mContext).getScaledPagingTouchSlop();
                if (mCurPosY - mPosY > 0 && (Math.abs(mCurPosY - mPosY) > touchSlop)) {
                    //向下滑動
                    if (mOnMoveListener != null) {
                        mOnMoveListener.onMove(false);
                    }
                } else if (mCurPosY - mPosY < 0 &&
                        (Math.abs(mCurPosY - mPosY) > touchSlop)) {
                    //向上滑动
                    if (mOnMoveListener != null) {
                        mOnMoveListener.onMove(true);
                    }
                }
                break;
        }
        return false;
    }

    public interface OnMoveListener {
        void onMove(boolean isMoveUp);
    }

    private OnMoveListener mOnMoveListener;

    public void setOnMoveListener(OnMoveListener onMoveListener) {
        mOnMoveListener = onMoveListener;
    }
}
