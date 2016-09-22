package com.oubowu.slideback;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import com.oubowu.slideback.callbak.OnSlideListener;
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

    public static SlideBackLayout attach(final Activity curActivity, Activity preActivity, SlideConfig config, final OnSlideListener listener) {
        final ViewGroup decorView = getDecorView(curActivity);
        final View contentView = decorView.getChildAt(0);
        decorView.removeViewAt(0);

        contentView.setId(R.id.slide_content_view);
        contentView.setBackground(decorView.getBackground());
        decorView.setBackground(null);

        final SlideBackLayout slideBackLayout = new SlideBackLayout(curActivity, contentView, getContentView(preActivity), getDecorViewDrawable(preActivity), config,
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
                        //                        decorView.removeViewAt(0);
                        //                        contentView.setVisibility(View.INVISIBLE);
                        //                        decorView.addView(contentView);
                        //                        decorView.postDelayed(new Runnable() {
                        //                            @Override
                        //                            public void run() {
                        //                                Log.e("TAG", "SlideBackHelper-67行-run(): " + "关闭页面");
                        //                                curActivity.finish();
                        //                                curActivity.overridePendingTransition(0, R.anim.anim_out_none);
                        //                            }
                        //                        }, 50);
                        // TODO: 2016/9/23 偶尔会黑屏一下，找不到原因很苦恼
                        curActivity.finish();
                        curActivity.overridePendingTransition(0, R.anim.anim_out_none);
                    }
                });
        decorView.addView(slideBackLayout);
        return slideBackLayout;
    }


}
