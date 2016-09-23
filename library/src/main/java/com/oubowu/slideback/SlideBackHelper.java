package com.oubowu.slideback;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import com.oubowu.slideback.callbak.OnSlideListener;
import com.oubowu.slideback.callbak.OnViewChangeListener;
import com.oubowu.slideback.widget.SlideBackLayout;

/**
 * Created by Oubowu on 2016/9/22 0022 14:31.
 */
public class SlideBackHelper {

    public static ViewGroup getDecorView(Activity activity) {
        return (ViewGroup) activity.getWindow().getDecorView();
    }

    public static Drawable getDecorViewDrawable(Activity activity) {
        return getDecorView(activity).getBackground();
    }

    public static View getContentView(Activity activity) {
        final View view = getDecorView(activity).getChildAt(0);
        final View slideContentView = view.findViewById(R.id.slide_content_view);
        return slideContentView != null ? slideContentView : view;
    }

    /**
     * 附着Activity，实现侧滑
     *
     * @param curActivity    当前Activity
     * @param preActivity    上个Activity
     * @param fixOrientation 屏幕是否旋转，针对是否旋转两种方案实现侧滑
     * @param config         参数配置
     * @param listener       滑动的监听
     * @return 处理侧滑的布局，提高方法动态设置滑动相关参数
     */
    public static SlideBackLayout attach(final Activity curActivity, final Activity preActivity, final boolean fixOrientation, SlideConfig config, final OnSlideListener listener) {
        final ViewGroup decorView = getDecorView(curActivity);
        final View contentView = decorView.getChildAt(0);
        decorView.removeViewAt(0);

        contentView.setId(R.id.slide_content_view);
        contentView.setBackground(decorView.getBackground());
        decorView.setBackground(null);

        final View preContentView = getContentView(preActivity);

        final SlideBackLayout slideBackLayout = new SlideBackLayout(curActivity, contentView, preContentView, getDecorViewDrawable(preActivity), config,
                new OnSlideListener() {

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
                    public void onClose() {
                        if (listener != null) {
                            listener.onClose();
                        }
                        // TODO: 2016/9/23 偶尔会黑屏一下，找不到原因很苦恼
                        curActivity.finish();
                        curActivity.overridePendingTransition(0, R.anim.anim_out_none);
                    }
                });

        slideBackLayout.setOnViewChangeListener(new OnViewChangeListener() {
            @Override
            public void onStart() {
                if (!fixOrientation) {
                    // 上个页面的内容页与之解绑
                    getDecorView(preActivity).removeView(preContentView);
                }
            }

            @Override
            public void onEnd() {
                if (!fixOrientation) {
                    // 当前页面的内容页与之解绑
                    slideBackLayout.removeView(preContentView);
                }
            }
        });
        slideBackLayout.addView(preContentView, 0);
        decorView.addView(slideBackLayout);
        return slideBackLayout;
    }


}
