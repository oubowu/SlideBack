package com.oubowu.slideback;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.oubowu.slideback.callbak.OnInternalSlideListener;
import com.oubowu.slideback.callbak.OnSlideListener;
import com.oubowu.slideback.widget.SlideBackLayout;

/**
 * Created by Oubowu on 2016/9/22 0022 14:31.
 */
// TODO: 2016/9/24 添加了上个页面的布局，如果页面有Toolbar的话其不随屏幕选择而大小变化，永远维持进入时的宽高比
public class SlideBackHelper {

    public static ViewGroup getDecorView(Activity activity) {
        return (ViewGroup) activity.getWindow().getDecorView();
    }

    public static Drawable getDecorViewDrawable(Activity activity) {
        return getDecorView(activity).getBackground();
    }

    public static View getContentView(Activity activity) {
        return getDecorView(activity).getChildAt(0);
    }

    /**
     * 附着Activity，实现侧滑
     *
     * @param curActivity 当前Activity
     * @param helper      Activity栈管理类
     * @param config      参数配置
     * @param listener    滑动的监听
     * @return 处理侧滑的布局，提高方法动态设置滑动相关参数
     */
    public static SlideBackLayout attach(@NonNull final Activity curActivity, @NonNull final ActivityHelper helper, @Nullable final SlideConfig config, @Nullable final OnSlideListener listener) {
        final ViewGroup decorView = getDecorView(curActivity);
        final View contentView = decorView.getChildAt(0);
        decorView.removeViewAt(0);

        View content = contentView.findViewById(android.R.id.content);
        if (content.getBackground() == null) {
            content.setBackground(decorView.getBackground());
        }

        final ActivityHelper[] helpers = {helper};

        final Activity preActivity = helpers[0].getPreActivity();
        final View preContentView = getContentView(preActivity);

        content = preContentView.findViewById(android.R.id.content);
        Drawable preDecorViewDrawable = getDecorViewDrawable(preActivity);
        if (content.getBackground() == null) {
            content.setBackground(preDecorViewDrawable);
        }

        final SlideBackLayout slideBackLayout;
        slideBackLayout = new SlideBackLayout(curActivity, contentView, preContentView, preDecorViewDrawable, config, new OnInternalSlideListener() {

            @Override
            public void onSlide(float percent) {
                if (listener != null) {
                    listener.onSlide(percent);
                }
            }

            @Override
            public void onOpen() {
                if (listener != null) {
                    listener.onOpen();
                }
            }

            @Override
            public void onClose(boolean finishActivity) {
                if (!finishActivity && listener != null) {
                    listener.onClose();
                }

                if (config != null && config.isRotateScreen()) {

                    if (finishActivity) {
                        // remove了preContentView后布局会重新调整，这时候contentView回到原处，所以要设不可见
                        contentView.setVisibility(View.INVISIBLE);
                    }

                    ((ViewGroup) preContentView.getParent()).removeView(preContentView);
                    Log.e("TAG", "这里把原先布局放回到上个Activity");
                    getDecorView(preActivity).addView(preContentView);
                    // preContentView.setVisibility(View.VISIBLE);
                }

                if (finishActivity) {
                    curActivity.finish();
                    curActivity.overridePendingTransition(0, R.anim.anim_out_none);
                }
            }
        });

        if (config != null && config.isRotateScreen()) {
            helpers[0].setOnActivityDestroyListener(new ActivityHelper.OnActivityDestroyListener() {
                @Override
                public void onDestroy(Activity activity) {
                    if (activity == curActivity) {
                        if (preActivity != null && preContentView.getParent() != getDecorView(preActivity)) {
                            // 当前页面的内容页与之解绑
                            preContentView.setX(0);
                            preContentView.post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ViewGroup) preContentView.getParent()).removeView(preContentView);
                                    // preContentView.setVisibility(View.VISIBLE);
                                    getDecorView(preActivity).addView(preContentView);
                                }
                            });
                        }
                        helpers[0].setOnActivityDestroyListener(null);
                        helpers[0] = null;
                    }
                }
            });
        }
        decorView.addView(slideBackLayout);

        return slideBackLayout;
    }


}
