package com.flyco.tablayoutsamples.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.SlidingTabLayoutEx;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.flyco.tablayout.widget.MsgView;
import com.flyco.tablayoutsamples.R;
import com.flyco.tablayoutsamples.utils.ViewFindUtils;

import java.util.ArrayList;

public class SlidingTabExActivity extends AppCompatActivity implements OnTabSelectListener {
    private Context mContext = this;
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private final String[] mTitles = {
            "热门", "iOS", "AndroidAndroidAndroid"
            , "前端", "后端", "设计", "工具资源"
    };
    private SlidingTabExActivity.MyPagerAdapter mAdapter;
    SlidingTabLayoutEx tabLayout_6;

    protected int getContentResId() {
        return R.layout.activity_sliding_tab_ex;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentResId());


        for (String title : mTitles) {
            mFragments.add(SimpleCardFragment.getInstance(title));
        }


        View decorView = getWindow().getDecorView();
        ViewPager vp = ViewFindUtils.find(decorView, R.id.vp);
        mAdapter = new SlidingTabExActivity.MyPagerAdapter(getSupportFragmentManager());
        vp.setAdapter(mAdapter);

        /** 默认 */
        SlidingTabLayoutEx tabLayout_1 = ViewFindUtils.find(decorView, R.id.tl_1);
        /**自定义部分属性*/
        SlidingTabLayoutEx tabLayout_2 = ViewFindUtils.find(decorView, R.id.tl_2);
        /** 字体加粗,大写 */
        SlidingTabLayoutEx tabLayout_3 = ViewFindUtils.find(decorView, R.id.tl_3);
        /** tab固定宽度 */
        SlidingTabLayoutEx tabLayout_4 = ViewFindUtils.find(decorView, R.id.tl_4);
        /** indicator固定宽度 */
        SlidingTabLayoutEx tabLayout_5 = ViewFindUtils.find(decorView, R.id.tl_5);
        /** indicator圆 */
        SlidingTabLayoutEx tabLayout_6 = ViewFindUtils.find(decorView, R.id.tl_6);
        this.tabLayout_6 = tabLayout_6;
        /** indicator矩形圆角 */
        final SlidingTabLayoutEx tabLayout_7 = ViewFindUtils.find(decorView, R.id.tl_7);
        /** indicator三角形 */
        SlidingTabLayoutEx tabLayout_8 = ViewFindUtils.find(decorView, R.id.tl_8);
        /** indicator圆角色块 */
        SlidingTabLayoutEx tabLayout_9 = ViewFindUtils.find(decorView, R.id.tl_9);
        /** indicator圆角色块 */
        SlidingTabLayoutEx tabLayout_10 = ViewFindUtils.find(decorView, R.id.tl_10);

        tabLayout_1.setViewPager(vp);
        tabLayout_2.setViewPager(vp);
        tabLayout_2.setOnTabSelectListener(this);
        tabLayout_3.setViewPager(vp);
        tabLayout_4.setViewPager(vp);
        tabLayout_5.setViewPager(vp);
        tabLayout_6.setViewPager(vp);
        tabLayout_7.setViewPager(vp, mTitles);
        tabLayout_8.setViewPager(vp, mTitles, this, mFragments);
        tabLayout_9.setViewPager(vp);
        tabLayout_10.setViewPager(vp);

        vp.setCurrentItem(4);

        tabLayout_1.showDot(4);
        tabLayout_3.showDot(4);
        tabLayout_2.showDot(4);

        tabLayout_2.showMsg(1, 255);
        tabLayout_2.showMsg(2, 55);
        tabLayout_2.showMsg(3, 5);
        tabLayout_2.setMsgMargin(3, 0, 10);
        MsgView rtv_2_3 = tabLayout_2.getMsgView(3);
        if (rtv_2_3 != null) {
            rtv_2_3.setBackgroundColor(Color.parseColor("#6D8FB0"));
        }

        tabLayout_2.showMsg(5, 5);
        tabLayout_2.setMsgMargin(5, 0, 10);

        tabLayout_6.showMsg(2, 23);
        tabLayout_6.showMsg2(3, 23);

        tabLayout_7.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                Toast.makeText(mContext, "onTabSelect&position--->" + position, Toast.LENGTH_SHORT).show();
                if (position == 4) {
                    if (num >= MAX_NUM) {
                        num = 0;
                    } else {
                        num = MAX_NUM;
                    }
                    scoreChangeAnimator.setView(tabLayout_6.getSubscript(4), null);
                    scoreChangeAnimator.startTo(num, num);
                }
            }

            @Override
            public void onTabReselect(int position) {
                mFragments.add(SimpleCardFragment.getInstance("后端"));
                mAdapter.notifyDataSetChanged();
                tabLayout_7.addNewTab("后端");
            }
        });

// 上下标设置
        tabLayout_1.setScriptText(5, "默认为下标123");

        tabLayout_2.setScriptText(4, "很上标123");
        tabLayout_2.setSuperscriptModel(4, true);
        tabLayout_2.setScriptText(5, "很下标123");

        tabLayout_3.setScriptText(5, "静态左间距123");

        tabLayout_4.setScriptText(5, "静态右间距abc");
        tabLayout_4.setSuperscriptModel(5, false);

        tabLayout_5.setScriptText(5, "这个也是下标123");
        tabLayout_5.setSuperscriptModel(5, false);

        tabLayout_6.setScriptText(4, String.valueOf(num));
        tabLayout_6.setSuperscriptModel(4, true);

        tabLayout_6.setScriptText(5, "2间隔上标123");
        tabLayout_6.setSuperscriptModel(5, true);
        tabLayout_6.setTitleWrapRight(5, tabLayout_10.dp2px(2), 0);

        tabLayout_7.setScriptText(4, "3");
        tabLayout_7.setSuperscriptModel(4, true);

        TextView scriptTv = tabLayout_7.getSubscript(5);
        scriptTv.setTextColor(0xFF44aa00);
        tabLayout_7.setScriptText(5, "下标123");
        tabLayout_7.setSuperscriptModel(5, false);

        tabLayout_7.setScriptText(6, "6间隔默认为下标");
        tabLayout_7.setTitleWrapRight(6, tabLayout_10.dp2px(6), 0);

        tabLayout_8.setScriptText(5, "0间隔默认为下标123");

        tabLayout_9.setScriptText(5, "0间隔默认为下标123");
        tabLayout_9.setTitleWrapRight(5, 0, tabLayout_10.dp2px(3));

        scriptTv = tabLayout_10.getSubscript(5);
        scriptTv.setTextColor(0xFFFFFFFF);
        tabLayout_10.setScriptText(5, "默认为下标123");
        tabLayout_10.setTitleWrapRight(5, 0, tabLayout_10.dp2px(2));


    }

    int MAX_NUM = 1111111;
    int num = MAX_NUM;
    SlidingTabExActivity.ScoreChangeAnimator scoreChangeAnimator = new SlidingTabExActivity.ScoreChangeAnimator();

    /**
     * pk分数变化动效
     */
    private class ScoreChangeAnimator extends AnimatorListenerAdapter
            implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

        private float homeNumFrom;
        private float homeNumTo;
        private float guestNumFrom;
        private float guestNumTo;
        private TextView homeScoreView;
        private TextView guestScoreView;
        private ValueAnimator valueAnimator = null;

        public void setView(TextView homeScoreView, TextView guestScoreView) {
            this.homeScoreView = homeScoreView;
            this.guestScoreView = guestScoreView;
        }

        public void setFrom(float homeNumFrom, float guestNumFrom) {
            this.homeNumFrom = homeNumFrom;
            this.guestNumFrom = guestNumFrom;
        }

        public void startTo(float homeNumTo, float guestNumTo) {
            if (valueAnimator == null) {
                valueAnimator = ValueAnimator.ofFloat(0f, 1f);
                valueAnimator.setDuration(5000);
                valueAnimator.addUpdateListener(this);
                valueAnimator.addListener(this);
            } else {
                valueAnimator.cancel();
            }

            this.homeNumTo = homeNumTo;
            this.guestNumTo = guestNumTo;

            valueAnimator.setFloatValues(0F, 1F);
            valueAnimator.start();
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float v = (float) animation.getAnimatedValue();

            if (homeScoreView != null && homeNumFrom != homeNumTo) {
                String homeString = String.valueOf((int) (homeNumFrom + (homeNumTo - homeNumFrom) * v));
                homeScoreView.setText(homeString);
//                tabLayout_6.setScriptText(4, homeString);
            }

            if (guestScoreView != null && guestNumFrom != guestNumTo) {
                String guestString = String.valueOf(guestNumFrom + (guestNumTo - guestNumFrom) * v);
                guestScoreView.setText(guestString);
            }
        }

        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            TextView tv = tabLayout_6.getSubscript(4);
            Paint paint = tv.getPaint();
            float charWidth = paint.measureText("4");
            int destWidth = Math.round(charWidth * String.valueOf(homeNumTo).length());
            int curWidth = Math.round(charWidth * tv.getText().length());
            int maxWidth = Math.max(curWidth, destWidth);
            SlidingTabLayout.setViewWidth(tv, maxWidth);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            homeNumFrom = homeNumTo;
            guestNumFrom = guestNumTo;

            TextView tv = tabLayout_6.getSubscript(4);
            SlidingTabLayout.setViewWidth(tv, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        public String getInfo() {
            StringBuilder sb = new StringBuilder();
            sb.append("homeW: ").append(homeNumFrom).append(" -> ").append(homeNumTo);
            sb.append("\tguestW: ").append(guestNumFrom).append(" -> ").append(guestNumTo);
            return sb.toString();
        }
    }

    @Override
    public void onTabSelect(int position) {
        Toast.makeText(mContext, "onTabSelect&position--->" + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTabReselect(int position) {
        Toast.makeText(mContext, "onTabReselect&position--->" + position, Toast.LENGTH_SHORT).show();
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }
}
