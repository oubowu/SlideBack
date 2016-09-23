package com.oubowu.slideback.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.FloatRange;
import android.support.v4.view.ViewGroupCompat;
import android.support.v4.widget.ViewDragHelper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.oubowu.slideback.SlideConfig;
import com.oubowu.slideback.callbak.OnSlideListener;

/**
 * Created by Oubowu on 2016/9/22 0022 15:24.
 */
public class SlideBackLayout extends FrameLayout {

    private static final int MIN_FLING_VELOCITY = 400;
    private ViewDragHelper mDragHelper;
    private View mContentView;
    private CacheDrawView mCacheDrawView;
    private ShadowView mShadowView;
    private SlideLeftCallback mSlideLeftCallback;
    private View mPreContentView;
    private Drawable mPreDecorViewDrawable;
    private int mScreenWidth;

    private boolean mEdgeOnly = false;
    private boolean mLock = false;

    @FloatRange(from = 0.0,
            to = 1.0)
    private float mSlideOutRangePercent = 0.4f;

    @FloatRange(from = 0.0,
            to = 1.0)
    private float mEdgeRangePercent = 0.1f;

    private float mSlideOutRange;
    private float mEdgeRange;

    private float mSlideOutVelocity;

    private boolean mIsEdgeRangeInside;

    private OnSlideListener mOnSlideListener;

    private float mDownX;

    private float mSlidDistantX;

    public SlideBackLayout(Context context, View contentView, View preContentView, Drawable preDecorViewDrawable, SlideConfig config, OnSlideListener onSlideListener) {
        super(context);
        mContentView = contentView;
        mPreContentView = preContentView;
        mPreDecorViewDrawable = preDecorViewDrawable;
        mOnSlideListener = onSlideListener;

        init(config);
    }

    private void init(SlideConfig config) {

        if (config == null) {
            config = new SlideConfig.Builder().create();
        }

        mScreenWidth = getResources().getDisplayMetrics().widthPixels;

        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;

        ViewGroupCompat.setMotionEventSplittingEnabled(this, false);
        mSlideLeftCallback = new SlideLeftCallback();
        mDragHelper = ViewDragHelper.create(this, 1.0f, mSlideLeftCallback);
        // 最小拖动速度
        mDragHelper.setMinVelocity(minVel);
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);

        mCacheDrawView = new CacheDrawView(getContext());
        mCacheDrawView.setVisibility(INVISIBLE);

        mShadowView = new ShadowView(getContext());
        mShadowView.setVisibility(INVISIBLE);

        addView(mCacheDrawView);
        addView(mShadowView, mScreenWidth / 28, LayoutParams.MATCH_PARENT);

        addView(mContentView);

        mEdgeOnly = config.isEdgeOnly();
        mLock = config.isLock();

        mSlideOutRangePercent = config.getSlideOutPercent();
        mEdgeRangePercent = config.getEdgePercent();

        mSlideOutRange = mScreenWidth * mSlideOutRangePercent;
        mEdgeRange = mScreenWidth * mEdgeRangePercent;

        mSlideOutVelocity = config.getSlideOutVelocity();

        mSlidDistantX = mScreenWidth / 20.0f;

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                // 优化侧滑的逻辑，不要一有稍微的滑动就被ViewDragHelper拦截掉了
                if (event.getX() - mDownX < mSlidDistantX) {
                    return false;
                }
                break;
        }

        if (mLock) {
            return false;
        }

        if (mEdgeOnly) {
            float x = event.getX();
            mIsEdgeRangeInside = isEdgeRangeInside(x);
            return mIsEdgeRangeInside ? mDragHelper.shouldInterceptTouchEvent(event) : false;
        } else {
            return mDragHelper.shouldInterceptTouchEvent(event);
        }

    }

    private boolean isEdgeRangeInside(float x) {
        return x <= mEdgeRange;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mLock) {
            return super.onTouchEvent(event);
        }

        if ((mEdgeOnly && mIsEdgeRangeInside) || !mEdgeOnly) {
            mDragHelper.processTouchEvent(event);
        } else {
            return super.onTouchEvent(event);
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    class SlideLeftCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mContentView;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return Math.max(Math.min(mScreenWidth, left), 0);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mScreenWidth;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (releasedChild == mContentView) {

                if (xvel > mSlideOutVelocity) {
                    mDragHelper.settleCapturedViewAt(mScreenWidth, 0);
                    invalidate();
                    return;
                }

                if (mContentView.getLeft() < mSlideOutRange) {
                    mDragHelper.settleCapturedViewAt(0, 0);
                } else {
                    mDragHelper.settleCapturedViewAt(mScreenWidth, 0);
                }

                invalidate();
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            switch (state) {
                case ViewDragHelper.STATE_IDLE:
                    if (mContentView.getLeft() == 0) {
                        // 2016/9/22 0022 回到原处
                        if (mOnSlideListener != null) {
                            mOnSlideListener.onOpen();
                        }
                    } else if (mContentView.getLeft() == mScreenWidth) {
                        // 2016/9/22 0022 结束Activity
                        if (mOnSlideListener != null) {
                            mOnSlideListener.onClose();
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {

            if (mCacheDrawView.getVisibility() == INVISIBLE) {
                mCacheDrawView.setBackground(mPreDecorViewDrawable);
                mCacheDrawView.setVisibility(VISIBLE);
                mCacheDrawView.drawCacheView(mPreContentView);
                mShadowView.setVisibility(VISIBLE);
            }

            float percent = left * 1.0f / mScreenWidth;

            if (mOnSlideListener != null) {
                mOnSlideListener.onSlide(SlideBackLayout.this,percent);
            }

            mCacheDrawView.setX(-mScreenWidth / 2 + percent * (mScreenWidth / 2));
            mShadowView.setX(mContentView.getX() - mShadowView.getWidth());
            mShadowView.redraw(1 - percent);

        }
    }

    public void edgeOnly(boolean edgeOnly) {
        mEdgeOnly = edgeOnly;
    }

    public boolean isEdgeOnly() {
        return mEdgeOnly;
    }

    public void lock(boolean lock) {
        mLock = lock;
    }

    public boolean isLock() {
        return mLock;
    }

    public void setSlideOutRangePercent(float slideOutRangePercent) {
        mSlideOutRangePercent = slideOutRangePercent;
        mSlideOutRange = mScreenWidth * mSlideOutRangePercent;
    }

    public float getSlideOutRangePercent() {
        return mSlideOutRangePercent;
    }

    public void setEdgeRangePercent(float edgeRangePercent) {
        mEdgeRangePercent = edgeRangePercent;
        mEdgeRange = mScreenWidth * mEdgeRangePercent;
    }

    public float getEdgeRangePercent() {
        return mEdgeRangePercent;
    }

}
