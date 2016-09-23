package com.oubowu.slideback.demo;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.oubowu.slideback.SlideBackHelper;
import com.oubowu.slideback.SlideConfig;
import com.oubowu.slideback.widget.SlideBackLayout;

import java.util.ArrayList;
import java.util.List;

public class ScrollActivity extends AppCompatActivity {

    private RecyclerView mRv;
    private NestedScrollView mNsv;
    private ViewPager mVp;
    private SlideBackLayout mSlideBackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll);

        mSlideBackLayout = SlideBackHelper.attach(
                // 当前Activity
                this,
                // 上个Activity
                MyApplication.getActivityHelper().getPreActivity(),
                // 参数的配置
                new SlideConfig.Builder()
                        // 是否侧滑
                        .edgeOnly(false)
                        // 是否禁止侧滑
                        .lock(false)
                        // 侧滑的响应阈值，0~1，对应屏幕宽度*percent
                        .edgePercent(0.1f)
                        // 关闭页面的阈值，0~1，对应屏幕宽度*percent
                        .slideOutPercent(0.5f).create(),
                // 滑动的监听
                null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRv = (RecyclerView) findViewById(R.id.rv);
        mNsv = (NestedScrollView) findViewById(R.id.nsv);
        mVp = (ViewPager) findViewById(R.id.vp);

        mRv.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            list.add(i);
        }
        mRv.setAdapter(new RvAdapter(R.layout.item_rv, list));

        mVp.setAdapter(new VpAdapter<>(list));
        mVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mSlideBackLayout.edgeOnly(false);
                } else {
                    mSlideBackLayout.edgeOnly(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    class RvAdapter extends BaseQuickAdapter<Integer> {

        public RvAdapter(int layoutResId, List<Integer> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder baseViewHolder, Integer integer) {

        }
    }

    class VpAdapter<T> extends PagerAdapter {

        private List<T> mData;

        public VpAdapter(List<T> data) {
            this.mData = data;
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            final View view = LayoutInflater.from(container.getContext()).inflate(R.layout.item_vp, container, false);

            TextView tvPage = (TextView) view.findViewById(R.id.tv_page);

            tvPage.setText("页面" + (position + 1));

            container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scroll, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        item.setChecked(true);

        switch (item.getItemId()) {
            case R.id.action_rv:
                mRv.setVisibility(View.VISIBLE);
                mNsv.setVisibility(View.INVISIBLE);
                mVp.setVisibility(View.INVISIBLE);
                break;
            case R.id.action_nsv:
                mRv.setVisibility(View.INVISIBLE);
                mNsv.setVisibility(View.VISIBLE);
                mVp.setVisibility(View.INVISIBLE);
                break;
            case R.id.action_vp:
                mRv.setVisibility(View.INVISIBLE);
                mNsv.setVisibility(View.INVISIBLE);
                mVp.setVisibility(View.VISIBLE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
