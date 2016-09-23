## 仿微信侧滑返回库
#### 实现思路参考自「[Slidr](https://github.com/r0adkll/Slidr)」和「[and_swipeback](https://github.com/XBeats/and_swipeback)」

### 功能
- 无需设置Activity主题透明
- 支持动态切换全局或边缘侧滑，亦可动态禁止或恢复侧滑
- 支持动态设置边缘响应和滑动关闭距离的阈值
- 页面边缘附有阴影并随滑动距离而渐变
- 优化了与RecyclerView、ViewPager等滑动控件手势冲突

### 效果图
![Demo](/pic/demo.gif) 

### 使用方法
#### 1. 继承Application实现Activity生命周期的监听，ActivityHelper用于保存Activity栈供侧滑库使用
    public class MyApplication extends Application {

        private ActivityHelper mActivityHelper;

        private static MyApplication sMyApplication;

        @Override
        public void onCreate() {
            super.onCreate();

            mActivityHelper = new ActivityHelper();
            registerActivityLifecycleCallbacks(mActivityHelper);

            sMyApplication = this;

        }

        public static ActivityHelper getActivityHelper(){
            return sMyApplication.mActivityHelper;
        }
        
    }
#### 2.在需要侧滑的Activity使用SlideBackHelper去attach当前Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        mSlideBackLayout = SlideBackHelper.attach(
                // 当前Activity
                this,
                // 上个Activity
                MyApplication.getActivityHelper().getPreActivity(),
                // 参数的配置
                new SlideConfig.Builder()
                        // 是否启用边缘侧滑
                        .edgeOnly(false)
                        // 是否禁止侧滑
                        .lock(false)
                        // 边缘侧滑的响应阈值，0~1，对应屏幕宽度*percent
                        .edgePercent(0.1f)
                        // 关闭页面的阈值，0~1，对应屏幕宽度*percent
                        .slideOutPercent(0.5f)
                        .create(),
                // 滑动的监听
                new OnSlideListenerAdapter() {
                    @Override
                    public void onSlide(@FloatRange(from = 0.0,
                            to = 1.0) float percent) {
                        super.onSlide(percent);
                    }
                });
               
        // 其它初始化
    }
#### 3.SlideBackHelper.attach会返回处理侧滑的SlideBackLayout，可在适当时候动态控制侧滑几个参数
```  
  // 是否启用边缘侧滑
  mSlideBackLayout.edgeOnly(boolean);
  // 是否禁止侧滑
  mSlideBackLayout.lock(boolean);
  // 设置边缘侧滑的响应阈值
  mSlideBackLayout.setEdgeRangePercent(float);
  // 设置关闭页面的阈值
  mSlideBackLayout.setSlideOutRangePercent(float);
``` 

### 存在问题
##### 偶尔侧滑回来会有一瞬间闪屏，原因不详，希望有大神告知>_<

#### License
```
Copyright 2016 oubowu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```


