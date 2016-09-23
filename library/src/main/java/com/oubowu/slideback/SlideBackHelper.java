package com.oubowu.slideback;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

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
     * @param curActivity    当前Activity
     * @param helper         Activity栈管理类
     * @param fixOrientation 屏幕是否旋转，针对是否旋转两种方案实现侧滑
     * @param config         参数配置
     * @param listener       滑动的监听
     * @return 处理侧滑的布局，提高方法动态设置滑动相关参数
     */
    public static SlideBackLayout attach(@NonNull final Activity curActivity, @NonNull ActivityHelper helper, @NonNull final boolean fixOrientation, @Nullable SlideConfig config, @Nullable final OnSlideListener listener) {
        final ViewGroup decorView = getDecorView(curActivity);
        final View contentView = decorView.getChildAt(0);
        decorView.removeViewAt(0);

        contentView.setBackground(decorView.getBackground());
        decorView.setBackground(null);

        final ActivityHelper[] helpers = {helper};

        final Activity preActivity = helpers[0].getPreActivity();
        final View preContentView = getContentView(preActivity);

        //        Log.e("TAG", "SlideBackHelper-53行-attach(): " + preContentView);

        final SlideBackLayout slideBackLayout;
        slideBackLayout = new SlideBackLayout(curActivity, contentView, preContentView, getDecorViewDrawable(preActivity), config, new OnSlideListener() {

            @Override
            public void onSlide(View view, float percent) {
                if (listener != null) {
                    listener.onSlide(view, percent);
                }

                view = (View) view.getParent();
                if (!fixOrientation && preContentView != null && preContentView.getParent() != view) {
                    // 上个页面的内容页与之解绑
                    ((ViewGroup) preContentView.getParent()).removeView(preContentView);
                    preContentView.setVisibility(View.INVISIBLE);

                    /*if (preContentView instanceof ViewGroup) {
                        ViewGroup group = (ViewGroup) preContentView.findViewById(android.R.id.content);
                        if (group instanceof ViewGroup) {
                            group = (ViewGroup) group.getChildAt(0);
                            for (int i = 0; i < group.getChildCount(); i++) {
                                if (group.getChildAt(i) instanceof Toolbar) {

                                    group.getChildAt(i).measure(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

                                } else if (group.getChildAt(i) instanceof AppBarLayout) {

//                                    final TypedArray styledAttributes = view.getContext().getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
//                                    int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
//                                    styledAttributes.recycle();
//                                    group.getChildAt(i).getLayoutParams().height = toolbarHeight;
                                    
                                    Log.e("TAG","SlideBackHelper-90行-onSlide(): ");

                                    final Toolbar toolbar = (Toolbar) ((AppBarLayout) group.getChildAt(i)).getChildAt(0);
                                    // .measure(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

                                    break;
                                }
                            }
                        }
                    }*/

                    //                    final ViewGroup ccc = (ViewGroup) view.findViewById(Window.ID_ANDROID_CONTENT);
                    //
                    //                    Log.e("TAG","SlideBackHelper-93行-onSlide(): "+"----------------------------------------");
                    //                    for (int i = 0; i < ccc.getChildCount(); i++) {
                    //                        final View at = ccc.getChildAt(i);
                    //                        Log.e("TAG","SlideBackHelper-96行-onSlide(): "+at);
                    //                    }
                    //                    Log.e("TAG","SlideBackHelper-98行-onSlide(): "+"----------------------------------------");

                    ((ViewGroup) view).addView(preContentView, 0);
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

                if (!fixOrientation) {
                    // 当前页面的内容页与之解绑
                    ((ViewGroup) contentView.getParent()).removeView(contentView);
                    ((ViewGroup) preContentView.getParent()).removeView(preContentView);
                    preContentView.setVisibility(View.VISIBLE);
                    getDecorView(preActivity).addView(preContentView);
                }

                // TODO: 2016/9/23 偶尔会黑屏一下，找不到原因很苦恼
                curActivity.finish();
                curActivity.overridePendingTransition(0, R.anim.anim_out_none);
            }
        });

        helpers[0].setOnActivityDestroyListener(new ActivityHelper.OnActivityDestroyListener() {
            @Override
            public void onDestroy(Activity activity) {
                Log.e("TAG", "SlideBackHelper-103行-onDestroy(): " + activity);
                if (activity == curActivity) {
                    if (preActivity != null && preContentView.getParent() != getDecorView(preActivity)) {
                        // 当前页面的内容页与之解绑
                        ((ViewGroup) contentView.getParent()).removeView(contentView);
                        ((ViewGroup) preContentView.getParent()).removeView(preContentView);
                        preContentView.setVisibility(View.VISIBLE);
                        getDecorView(preActivity).addView(preContentView);
                    }
                    helpers[0].setOnActivityDestroyListener(null);
                    helpers[0] = null;
                }
            }
        });
        decorView.addView(slideBackLayout);

        //        Log.e("TAG", "SlideBackHelper-119行-attach(): -----------------------");
        //        for (int i = 0; i < slideBackLayout.getChildCount(); i++) {
        //            final View view = slideBackLayout.getChildAt(i);
        //            Log.e("TAG", "SlideBackHelper-121行-attach(): " + view);
        //        }
        //        Log.e("TAG", "SlideBackHelper-124行-attach(): ------------------------");

        return slideBackLayout;
    }


}
