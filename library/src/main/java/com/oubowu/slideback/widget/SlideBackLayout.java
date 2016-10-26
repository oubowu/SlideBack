package com.oubowu.slideback.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.support.annotation.FloatRange;
import android.support.v4.view.ViewGroupCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.oubowu.slideback.SlideConfig;
import com.oubowu.slideback.callbak.OnInternalSlideListener;

/**
 * Created by Oubowu on 2016/9/22 0022 15:24.
 */
public class SlideBackLayout extends FrameLayout {

    private static final int MIN_FLING_VELOCITY = 400;
    private boolean mIsFirstAttachToWindow;
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

    private OnInternalSlideListener mOnInternalSlideListener;

    private float mDownX;

    private float mSlidDistantX;

    private boolean mRotateScreen;

    private boolean mIsClose;

    public SlideBackLayout(Context context, View contentView, View preContentView, Drawable preDecorViewDrawable, SlideConfig config, OnInternalSlideListener onInternalSlideListener) {
        super(context);
        mContentView = contentView;
        mPreContentView = preContentView;
        mPreDecorViewDrawable = preDecorViewDrawable;
        mOnInternalSlideListener = onInternalSlideListener;

        initConfig(config);

    }

    private void initConfig(SlideConfig config) {

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
        addView(mCacheDrawView);

        mShadowView = new ShadowView(getContext());
        mShadowView.setVisibility(INVISIBLE);
        addView(mShadowView, mScreenWidth / 28, LayoutParams.MATCH_PARENT);

        addView(mContentView);

        mEdgeOnly = config.isEdgeOnly();
        mLock = config.isLock();
        mRotateScreen = config.isRotateScreen();

        mSlideOutRangePercent = config.getSlideOutPercent();
        mEdgeRangePercent = config.getEdgePercent();

        mSlideOutRange = mScreenWidth * mSlideOutRangePercent;
        mEdgeRange = mScreenWidth * mEdgeRangePercent;
        mSlideOutVelocity = config.getSlideOutVelocity();

        mSlidDistantX = mScreenWidth / 20.0f;

        mContentView.setFitsSystemWindows(false);
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
            return mIsEdgeRangeInside && mDragHelper.shouldInterceptTouchEvent(event);
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

        if (!mEdgeOnly || mIsEdgeRangeInside) {
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

    public void isComingToFinish() {
        if (mOnInternalSlideListener != null && mRotateScreen) {
            // 旋转屏幕的时候必调此方法，这里掉onClose目的是把preContentView给回上个Activity
            mOnInternalSlideListener.onClose(false);
            mPreContentView.setX(0);
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
                        if (mOnInternalSlideListener != null) {
                            mOnInternalSlideListener.onOpen();
                        }
                    } else if (mContentView.getLeft() == mScreenWidth) {
                        // 2016/9/22 0022 结束Activity
                        if (mOnInternalSlideListener != null) {

                            // 这里再绘制一次是因为在屏幕旋转的模式下，remove了preContentView后布局会重新调整
                            if (mRotateScreen && mCacheDrawView.getVisibility() == INVISIBLE) {
                                mCacheDrawView.setBackground(mPreDecorViewDrawable);
                                mCacheDrawView.drawCacheView(mPreContentView);
                                mCacheDrawView.setVisibility(VISIBLE);
                                Log.e("TAG", "这里再绘制一次是因为在屏幕旋转的模式下，remove了preContentView后布局会重新调整");

                                mIsClose = true;
                                Log.e("TAG", "SlideBackLayout-245行-onDetachedFromWindow(): 通知移除");
                                mOnInternalSlideListener.onClose(true);
                                mPreContentView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e("TAG", "绘制绘制");
                                        mCacheDrawView.setBackground(mPreDecorViewDrawable);
                                        mCacheDrawView.drawCacheView(mPreContentView);
                                    }
                                }, 10);
                            } else if (!mRotateScreen) {
                                mIsClose = true;
                                mOnInternalSlideListener.onClose(true);
                            }

                        }
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {

            if (!mRotateScreen && mCacheDrawView.getVisibility() == INVISIBLE) {
                mCacheDrawView.setBackground(mPreDecorViewDrawable);
                mCacheDrawView.drawCacheView(mPreContentView);
                mCacheDrawView.setVisibility(VISIBLE);
                mShadowView.setVisibility(VISIBLE);
            } else if (mRotateScreen) {
                if (mPreContentView.getParent() != SlideBackLayout.this) {
                    // 上个页面的内容页与之解绑，添加到当前页面
                    ((ViewGroup) mPreContentView.getParent()).removeView(mPreContentView);
                    SlideBackLayout.this.addView(mPreContentView, 0);
                    mShadowView.setVisibility(VISIBLE);
                }
            }

            final float percent = left * 1.0f / mScreenWidth;

            if (mOnInternalSlideListener != null) {
                mOnInternalSlideListener.onSlide(percent);
            }

            if (mRotateScreen) {
                // Log.e("TAG", "滑动上个页面");
                mPreContentView.setX(-mScreenWidth / 2 + percent * (mScreenWidth / 2));
            } else {
                mCacheDrawView.setX(-mScreenWidth / 2 + percent * (mScreenWidth / 2));
            }
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mOnInternalSlideListener != null && mRotateScreen) {
            // 1.旋转屏幕的时候必调此方法，这里掉onClose目的是把preContentView给回上个Activity
            // 2.跳转到另外一个Activity，例如也是需要滑动的，这时候就需要取当前Activity的contentView，所以这里把preContentView给回上个Activity
            if (mIsClose) {
                mIsClose = false;
            } else {
                Log.e("TAG", "SlideBackLayout-344行-onDetachedFromWindow(): 通知移除");
                mOnInternalSlideListener.onClose(false);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mIsFirstAttachToWindow) {
            mIsFirstAttachToWindow = true;
        } else if (mRotateScreen && mPreContentView.getParent() != SlideBackLayout.this) {
            // 从其他Activity返回来的时候，把mPreContentView添加到当前Activity
            ((ViewGroup) mPreContentView.getParent()).removeView(mPreContentView);
            SlideBackLayout.this.addView(mPreContentView, 0);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        //        Log.e("TAG", "SlideBackLayout-338行-onConfigurationChanged(): " + mScreenWidth);
        ViewGroup.LayoutParams layoutParams = mShadowView.getLayoutParams();
        layoutParams.width = mScreenWidth / 28;
        layoutParams.height = LayoutParams.MATCH_PARENT;
    }

}
