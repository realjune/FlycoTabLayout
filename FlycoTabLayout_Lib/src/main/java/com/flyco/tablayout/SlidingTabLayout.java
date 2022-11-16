package com.flyco.tablayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.R.drawable;
import com.flyco.tablayout.R.id;
import com.flyco.tablayout.R.styleable;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.flyco.tablayout.utils.UnreadMsgUtils;
import com.flyco.tablayout.widget.MsgView;

import java.util.ArrayList;
import java.util.Collections;

/** 滑动TabLayout,对于ViewPager的依赖性强 */
public class SlidingTabLayout extends HorizontalScrollView implements ViewPager.OnPageChangeListener {
    private Context mContext;
    private ViewPager mViewPager;
    private ArrayList<String> mTitles;
    private LinearLayout mTabsContainer;
    private int mCurrentTab;
    private float mCurrentPositionOffset;
    private int mTabCount;
    /** 用于绘制显示器 */
    private Rect mIndicatorRect = new Rect();
    /** 用于实现滚动居中 */
    private Rect mTabRect = new Rect();
    private GradientDrawable mIndicatorDrawable = new GradientDrawable();

    private Paint mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTrianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path mTrianglePath = new Path();
    private static final int STYLE_NORMAL = 0;
    private static final int STYLE_TRIANGLE = 1;
    private static final int STYLE_BLOCK = 2;
    private int mIndicatorStyle = STYLE_NORMAL;
    // show MsgTipView
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private SparseArray<Boolean> mInitSetMap = new SparseArray<>();
    // 上下标数据
    private SparseArray<ScriptInfo> mTitleWrapRight = new SparseArray<>();
    private float margin;
    private float marginRight;

    private float mTabPadding;
    private boolean mTabSpaceEqual;
    private float mTabWidth;

    /** indicator */
    private int mIndicatorColor;
    private float mIndicatorHeight;
    private float mIndicatorWidth;
    private float mIndicatorCornerRadius;
    private float mIndicatorMarginLeft;
    private float mIndicatorMarginTop;
    private float mIndicatorMarginRight;
    private float mIndicatorMarginBottom;
    private int mIndicatorGravity;
    private boolean mIndicatorWidthEqualTitle;
    private boolean mIndicatorGradient;
    private int mIndicatorColorOther;

    /** underline */
    private int mUnderlineColor;
    private float mUnderlineHeight;
    private int mUnderlineGravity;

    /** divider */
    private int mDividerColor;
    private float mDividerWidth;
    private float mDividerPadding;

    /** title */
    private static final int TEXT_BOLD_NONE = 0;
    private static final int TEXT_BOLD_WHEN_SELECT = 1;
    private static final int TEXT_BOLD_BOTH = 2;
    private float mTextsize;
    private int mTextSelectColor;
    private int mTextUnselectColor;
    private int mTextBold;
    private boolean mTextAllCaps;

    /** script */
    private float mScriptTextSize;
    private int mScriptTextSelectColor;
    private int mScriptTextUnselectColor;
    private int mScriptMarginLeft;
    private int mScriptTopOut;
    private int mScriptMarginRight;
    private int mScriptBottomOut;

    private int mLastScrollX;
    private int mHeight;
    private boolean mSnapOnTabClick;
    private OnTabSelectListener mListener;
    /** tabTitle容器的id */
    private int titleViewId = R.id.tv_tab_title;
    /** 是否走spport修改的逻辑 */
    private boolean isNewLogic = false;
    /** 禁用上下标的特殊处理 */
    private boolean isSubscriptAlignDisable = false;

    public SlidingTabLayout(Context context) {
        this(context, null, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFillViewport(true);//设置滚动视图是否可以伸缩其内容以填充视口
        setWillNotDraw(false);//重写onDraw方法,需要调用这个方法来清除flag
        setClipChildren(false);
        setClipToPadding(false);

        this.mContext = context;
        mTabsContainer = new LinearLayout(context);
        addView(mTabsContainer);

        obtainAttributes(context, attrs);

        //get layout_height
        String height = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_height");

        if (height.equals(ViewGroup.LayoutParams.MATCH_PARENT + "")) {
        } else if (height.equals(ViewGroup.LayoutParams.WRAP_CONTENT + "")) {
        } else {
            int[] systemAttrs = {android.R.attr.layout_height};
            @SuppressLint("ResourceType") TypedArray a = context.obtainStyledAttributes(attrs, systemAttrs);
            mHeight = a.getDimensionPixelSize(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            a.recycle();
        }
    }

    private void obtainAttributes(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabLayout);

        mIndicatorStyle = ta.getInt(R.styleable.SlidingTabLayout_tl_indicator_style, STYLE_NORMAL);
        mIndicatorColor = ta.getColor(R.styleable.SlidingTabLayout_tl_indicator_color, Color.parseColor(mIndicatorStyle == STYLE_BLOCK ? "#4B6A87" : "#ffffff"));
        mIndicatorHeight = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_height,
                dp2px(mIndicatorStyle == STYLE_TRIANGLE ? 4 : (mIndicatorStyle == STYLE_BLOCK ? -1 : 2)));
        mIndicatorWidth = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_width, dp2px(mIndicatorStyle == STYLE_TRIANGLE ? 10 : -1));
        mIndicatorCornerRadius = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_corner_radius, dp2px(mIndicatorStyle == STYLE_BLOCK ? -1 : 0));
        mIndicatorMarginLeft = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_left, dp2px(0));
        mIndicatorMarginTop = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_top, dp2px(mIndicatorStyle == STYLE_BLOCK ? 7 : 0));
        mIndicatorMarginRight = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_right, dp2px(0));
        mIndicatorMarginBottom = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_bottom, dp2px(mIndicatorStyle == STYLE_BLOCK ? 7 : 0));
        mIndicatorGravity = ta.getInt(R.styleable.SlidingTabLayout_tl_indicator_gravity, Gravity.BOTTOM);
        mIndicatorWidthEqualTitle = ta.getBoolean(R.styleable.SlidingTabLayout_tl_indicator_width_equal_title, false);

        mUnderlineColor = ta.getColor(R.styleable.SlidingTabLayout_tl_underline_color, Color.parseColor("#ffffff"));
        mUnderlineHeight = ta.getDimension(R.styleable.SlidingTabLayout_tl_underline_height, dp2px(0));
        mUnderlineGravity = ta.getInt(R.styleable.SlidingTabLayout_tl_underline_gravity, Gravity.BOTTOM);

        mDividerColor = ta.getColor(R.styleable.SlidingTabLayout_tl_divider_color, Color.parseColor("#ffffff"));
        mDividerWidth = ta.getDimension(R.styleable.SlidingTabLayout_tl_divider_width, dp2px(0));
        mDividerPadding = ta.getDimension(R.styleable.SlidingTabLayout_tl_divider_padding, dp2px(12));

        mTextsize = ta.getDimension(R.styleable.SlidingTabLayout_tl_textsize, sp2px(14));
        mTextSelectColor = ta.getColor(R.styleable.SlidingTabLayout_tl_textSelectColor, Color.parseColor("#ffffff"));
        mTextUnselectColor = ta.getColor(R.styleable.SlidingTabLayout_tl_textUnselectColor, Color.parseColor("#AAffffff"));
        mTextBold = ta.getInt(R.styleable.SlidingTabLayout_tl_textBold, TEXT_BOLD_NONE);
        mTextAllCaps = ta.getBoolean(R.styleable.SlidingTabLayout_tl_textAllCaps, false);

        mTabSpaceEqual = ta.getBoolean(R.styleable.SlidingTabLayout_tl_tab_space_equal, false);
        mTabWidth = ta.getDimension(R.styleable.SlidingTabLayout_tl_tab_width, dp2px(-1));
        mTabPadding = ta.getDimension(R.styleable.SlidingTabLayout_tl_tab_padding, mTabSpaceEqual || mTabWidth > 0 ? dp2px(0) : dp2px(20));

        // spports 新增
        mIndicatorGradient = ta.getBoolean(styleable.SlidingTabLayout_tl_indicator_gradient, false);
        mIndicatorColorOther = ta.getColor(styleable.SlidingTabLayout_tl_indicator_color_other, mIndicatorColor);

        // <!-- scriptText-上下标 -->
        // <!-- 设置上下标字体大小 -->
        mScriptTextSize = ta.getDimension(styleable.SlidingTabLayout_tl_script_textsize, sp2px(11));
        // <!-- 设置上下标字体选中颜色 -->
        mScriptTextSelectColor = ta.getColor(styleable.SlidingTabLayout_tl_script_textSelectColor, mTextSelectColor);
        // <!-- 设置上下标字体未选中颜色 -->
        mScriptTextUnselectColor = ta.getColor(styleable.SlidingTabLayout_tl_script_textUnselectColor,
                mTextUnselectColor);
        // <!-- 设置上下标左间距 -->
        mScriptMarginLeft = ta.getDimensionPixelSize(styleable.SlidingTabLayout_tl_script_marginLeft, 0);
        // <!-- 设置上下标左间距 -->
        mScriptTopOut = ta.getDimensionPixelSize(styleable.SlidingTabLayout_tl_script_topOut, 0);
        // <!-- 设置上下标上间距 -->
        mScriptMarginRight = ta.getDimensionPixelSize(styleable.SlidingTabLayout_tl_script_marginRight, 0);
        // <!-- 设置上下标下间距 -->
        mScriptBottomOut = ta.getDimensionPixelSize(styleable.SlidingTabLayout_tl_script_bottomOut, 0);

        ta.recycle();
    }

    /** 关联ViewPager */
    public void setViewPager(ViewPager vp) {
        if (vp == null || vp.getAdapter() == null) {
            throw new IllegalStateException("ViewPager or ViewPager adapter can not be NULL !");
        }

        this.mViewPager = vp;

        this.mViewPager.removeOnPageChangeListener(this);
        this.mViewPager.addOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    /** 关联ViewPager,用于不想在ViewPager适配器中设置titles数据的情况 */
    public void setViewPager(ViewPager vp, String[] titles) {
        if (vp == null || vp.getAdapter() == null) {
            throw new IllegalStateException("ViewPager or ViewPager adapter can not be NULL !");
        }

        if (titles == null || titles.length == 0) {
            throw new IllegalStateException("Titles can not be EMPTY !");
        }

        if (titles.length != vp.getAdapter().getCount()) {
            throw new IllegalStateException("Titles length must be the same as the page count !");
        }

        this.mViewPager = vp;
        mTitles = new ArrayList<>();
        Collections.addAll(mTitles, titles);

        this.mViewPager.removeOnPageChangeListener(this);
        this.mViewPager.addOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    /** 关联ViewPager,用于连适配器都不想自己实例化的情况 */
    public void setViewPager(ViewPager vp, String[] titles, FragmentActivity fa, ArrayList<Fragment> fragments) {
        if (vp == null) {
            throw new IllegalStateException("ViewPager can not be NULL !");
        }

        if (titles == null || titles.length == 0) {
            throw new IllegalStateException("Titles can not be EMPTY !");
        }

        this.mViewPager = vp;
        this.mViewPager.setAdapter(new InnerPagerAdapter(fa.getSupportFragmentManager(), fragments, titles));

        this.mViewPager.removeOnPageChangeListener(this);
        this.mViewPager.addOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    /** 更新数据 */
    public void notifyDataSetChanged() {
        mTabsContainer.removeAllViews();
        this.mTabCount = mTitles == null ? mViewPager.getAdapter().getCount() : mTitles.size();
        View tabView;
        for (int i = 0; i < mTabCount; i++) {
            tabView = View.inflate(mContext, R.layout.layout_tab, null);
            CharSequence pageTitle = mTitles == null ? mViewPager.getAdapter().getPageTitle(i) : mTitles.get(i);
            addTab(i, pageTitle.toString(), tabView);
        }

        updateTabStyles();
    }

    public void addNewTab(String title) {
        View tabView = View.inflate(mContext, R.layout.layout_tab, null);
        if (mTitles != null) {
            mTitles.add(title);
        }

        CharSequence pageTitle = mTitles == null ? mViewPager.getAdapter().getPageTitle(mTabCount) : mTitles.get(mTabCount);
        addTab(mTabCount, pageTitle.toString(), tabView);
        this.mTabCount = mTitles == null ? mViewPager.getAdapter().getCount() : mTitles.size();

        updateTabStyles();
    }

    /** 创建并添加tab */
    private void addTab(final int position, String title, View tabView) {
        TextView tv_tab_title = (TextView) tabView.findViewById(R.id.tv_tab_title);
        if (tv_tab_title != null) {
            if (title != null) tv_tab_title.setText(title);
        }

        tabView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mTabsContainer.indexOfChild(v);
                if (position != -1) {
                    if (mViewPager.getCurrentItem() != position) {
                        if (mSnapOnTabClick) {
                            mViewPager.setCurrentItem(position, false);
                        } else {
                            mViewPager.setCurrentItem(position);
                        }

                        if (mListener != null) {
                            mListener.onTabSelect(position);
                        }
                    } else {
                        if (mListener != null) {
                            mListener.onTabReselect(position);
                        }
                    }
                }
            }
        });

        /** 每一个Tab的布局参数 */
        LinearLayout.LayoutParams lp_tab = mTabSpaceEqual ?
                new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f) :
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        if (mTabWidth > 0) {
            lp_tab = new LinearLayout.LayoutParams((int) mTabWidth, LayoutParams.MATCH_PARENT);
        }

        mTabsContainer.addView(tabView, position, lp_tab);
    }

    private void updateTabStyles() {
        for (int i = 0; i < mTabCount; i++) {
            View v = mTabsContainer.getChildAt(i);
//            v.setPadding((int) mTabPadding, v.getPaddingTop(), (int) mTabPadding, v.getPaddingBottom());
            TextView tv_tab_title = (TextView) v.findViewById(R.id.tv_tab_title);
            if (tv_tab_title != null) {
                tv_tab_title.setTextColor(i == mCurrentTab ? mTextSelectColor : mTextUnselectColor);
                tv_tab_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextsize);
                tv_tab_title.setPadding((int) mTabPadding, 0, (int) mTabPadding, 0);
                if (mTextAllCaps) {
                    tv_tab_title.setText(tv_tab_title.getText().toString().toUpperCase());
                }

                if (mTextBold == TEXT_BOLD_BOTH) {
                    tv_tab_title.getPaint().setFakeBoldText(true);
                } else if (mTextBold == TEXT_BOLD_NONE) {
                    tv_tab_title.getPaint().setFakeBoldText(false);
                }
            }
            TextView scriptTv = v.findViewById(id.tv_tab_subscript);
            if (scriptTv != null) {
                scriptTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mScriptTextSize);
                scriptTv.setTextColor(i == mCurrentTab ? mScriptTextSelectColor : mScriptTextUnselectColor);
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        /**
         * position:当前View的位置
         * mCurrentPositionOffset:当前View的偏移量比例.[0,1)
         */
        this.mCurrentTab = position;
        this.mCurrentPositionOffset = positionOffset;
        scrollToCurrentTab();
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
        updateTabSelection(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /** HorizontalScrollView滚到当前tab,并且居中显示 */
    private void scrollToCurrentTab() {
        if (mTabCount <= 0) {
            return;
        }

        int offset = (int) (mCurrentPositionOffset * mTabsContainer.getChildAt(mCurrentTab).getWidth());
        /**当前Tab的left+当前Tab的Width乘以positionOffset*/
        int newScrollX = mTabsContainer.getChildAt(mCurrentTab).getLeft() + offset;

        if (mCurrentTab > 0 || offset > 0) {
            /**HorizontalScrollView移动到当前tab,并居中*/
            newScrollX -= getWidth() / 2 - getPaddingLeft();
            calcIndicatorRect();
            newScrollX += ((mTabRect.right - mTabRect.left) / 2);
        }

        if (newScrollX != mLastScrollX) {
            mLastScrollX = newScrollX;
            /** scrollTo（int x,int y）:x,y代表的不是坐标点,而是偏移量
             *  x:表示离起始位置的x水平方向的偏移量
             *  y:表示离起始位置的y垂直方向的偏移量
             */
            scrollTo(newScrollX, 0);
        }
    }

    private void updateTabSelection(int position) {
        for (int i = 0; i < mTabCount; ++i) {
            View tabView = mTabsContainer.getChildAt(i);
            final boolean isSelect = i == position;
            TextView tab_title = (TextView) tabView.findViewById(R.id.tv_tab_title);

            if (tab_title != null) {
                tab_title.setTextColor(isSelect ? mTextSelectColor : mTextUnselectColor);
                if (mTextBold == TEXT_BOLD_WHEN_SELECT) {
                    tab_title.getPaint().setFakeBoldText(isSelect);
                }
            }

            TextView scriptTv = tabView.findViewById(id.tv_tab_subscript);
            if (scriptTv != null) {
                scriptTv.setTextColor(isSelect ? mScriptTextSelectColor : mScriptTextUnselectColor);
            }
        }
    }

    private void calcIndicatorRect() {
        View curTabView = mTabsContainer.getChildAt(this.mCurrentTab);
        float left = curTabView.getLeft();
        float right = curTabView.getRight();

        View curTv = curTabView.findViewById(titleViewId);

        //for mIndicatorWidthEqualTitle
        if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
            float textWidth = getTextWidth(curTabView);
            margin = (right - left - textWidth) / 2;
            marginRight = margin;
        }

        if (this.mCurrentTab < mTabCount - 1) {
            View nextTabView = mTabsContainer.getChildAt(this.mCurrentTab + 1);
            float nextTabLeft = nextTabView.getLeft();
            float nextTabRight = nextTabView.getRight();

            left = left + mCurrentPositionOffset * (nextTabLeft - left);
            right = right + mCurrentPositionOffset * (nextTabRight - right);

            //for mIndicatorWidthEqualTitle
            if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
                float nextTextWidth = getTextWidth(nextTabView);
                float nextMargin = (nextTabRight - nextTabLeft - nextTextWidth) / 2;
                margin = margin + mCurrentPositionOffset * (nextMargin - margin);
                marginRight = margin;
            }
        }

        mIndicatorRect.left = (int) left;
        mIndicatorRect.right = (int) right;
        //for mIndicatorWidthEqualTitle
        if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
            mIndicatorRect.left = (int) (left + margin - 1);
            mIndicatorRect.right = (int) (right - marginRight - 1);
        }

        mTabRect.left = (int) left;
        mTabRect.right = (int) right;

        if (mIndicatorWidth < 0) {   //indicatorWidth小于0时,原jpardogo's PagerSlidingTabStrip

        } else {//indicatorWidth大于0时,圆角矩形以及三角形
            float curentTabWidth = curTabView.getWidth();
            float indicatorLeft = curTabView.getLeft() + (curentTabWidth - mIndicatorWidth) / 2;
            float curentTabRight = curTabView.getRight() - indicatorLeft - mIndicatorWidth / 2;
            if (hasSubscript(curTabView)) {
                float contentWidth = curTv.getWidth() - curTv.getPaddingLeft() - curTv.getPaddingRight();
                indicatorLeft = curTabView.getLeft() + curTv.getPaddingLeft() + (contentWidth - mIndicatorWidth) / 2;
                curentTabRight = curTabView.getRight() - indicatorLeft - mIndicatorWidth / 2;
            }

            if (this.mCurrentTab < mTabCount - 1) {
                View nextTab = mTabsContainer.getChildAt(this.mCurrentTab + 1);
                float nextTabWidth = nextTab.getWidth();
                float nextLeft = (nextTabWidth) / 2;

                if (hasSubscript(nextTab)) {
                    View nextTv = nextTab.findViewById(titleViewId);

                    nextLeft = nextTv.getPaddingLeft() +
                            (nextTv.getWidth() - nextTv.getPaddingLeft() - nextTv.getPaddingRight()) / 2;
                }
                indicatorLeft = indicatorLeft + mCurrentPositionOffset * (curentTabRight + nextLeft);
            }

            mIndicatorRect.left = (int) indicatorLeft;
            mIndicatorRect.right = (int) (mIndicatorRect.left + mIndicatorWidth);
        }
    }

    /**
     * 获取Tab标题内容的宽度
     *
     * @param currentTabView tab的itemView
     * @return 内容宽度
     */
    private float getTextWidth(View currentTabView) {
        TextView tab_title = currentTabView.findViewById(R.id.tv_tab_title);
        float textWidth = 0;
        if (tab_title != null && tab_title.getVisibility() == View.VISIBLE) {
//            TextPaint paint = tab_title.getPaint();
//            textWidth = paint.measureText(tab_title.getText().toString());
            textWidth = tab_title.getWidth() - tab_title.getPaddingLeft() - tab_title.getPaddingRight();
        }
        View tabIcon = currentTabView.findViewById(R.id.iv_tab_icon);
        if (tabIcon != null && tabIcon.getVisibility() == View.VISIBLE) {
            textWidth = Math.max(textWidth, tabIcon.getWidth());
        }
        return textWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode() || mTabCount <= 0) {
            return;
        }

        int height = getHeight();
        int paddingLeft = getPaddingLeft();
        // draw divider
        if (mDividerWidth > 0) {
            mDividerPaint.setStrokeWidth(mDividerWidth);
            mDividerPaint.setColor(mDividerColor);
            for (int i = 0; i < mTabCount - 1; i++) {
                View tab = mTabsContainer.getChildAt(i);
                canvas.drawLine(paddingLeft + tab.getRight(), mDividerPadding, paddingLeft + tab.getRight(), height - mDividerPadding, mDividerPaint);
            }
        }

        // draw underline
        if (mUnderlineHeight > 0) {
            mRectPaint.setColor(mUnderlineColor);
            if (mUnderlineGravity == Gravity.BOTTOM) {
                canvas.drawRect(paddingLeft, height - mUnderlineHeight, mTabsContainer.getWidth() + paddingLeft, height, mRectPaint);
            } else {
                canvas.drawRect(paddingLeft, 0, mTabsContainer.getWidth() + paddingLeft, mUnderlineHeight, mRectPaint);
            }
        }

        //draw indicator line

        calcIndicatorRect();
        if (mIndicatorStyle == STYLE_TRIANGLE) {
            if (mIndicatorHeight > 0) {
                mTrianglePaint.setColor(mIndicatorColor);
                mTrianglePath.reset();
                mTrianglePath.moveTo(paddingLeft + mIndicatorRect.left, height);
                mTrianglePath.lineTo(paddingLeft + mIndicatorRect.left / 2 + mIndicatorRect.right / 2, height - mIndicatorHeight);
                mTrianglePath.lineTo(paddingLeft + mIndicatorRect.right, height);
                mTrianglePath.close();
                canvas.drawPath(mTrianglePath, mTrianglePaint);
            }
        } else if (mIndicatorStyle == STYLE_BLOCK) {
            if (mIndicatorHeight < 0) {
                mIndicatorHeight = height - mIndicatorMarginTop - mIndicatorMarginBottom;
            } else {

            }

            if (mIndicatorHeight > 0) {
                if (mIndicatorCornerRadius < 0 || mIndicatorCornerRadius > mIndicatorHeight / 2) {
                    mIndicatorCornerRadius = mIndicatorHeight / 2;
                }

                if (mIndicatorGradient) {
                    int[] colors = new int[]{mIndicatorColor, mIndicatorColorOther};
                    mIndicatorDrawable.setShape(GradientDrawable.RECTANGLE);
                    mIndicatorDrawable.setColors(colors);
                    mIndicatorDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                    mIndicatorDrawable.setOrientation(Orientation.RIGHT_LEFT);
                } else {
                    mIndicatorDrawable.setColor(mIndicatorColor);
                }
                mIndicatorDrawable.setBounds(paddingLeft + (int) mIndicatorMarginLeft + mIndicatorRect.left,
                        (int) mIndicatorMarginTop, (int) (paddingLeft + mIndicatorRect.right - mIndicatorMarginRight),
                        (int) (mIndicatorMarginTop + mIndicatorHeight));
                mIndicatorDrawable.setCornerRadius(mIndicatorCornerRadius);
                mIndicatorDrawable.draw(canvas);
            }
        } else {
               /* mRectPaint.setColor(mIndicatorColor);
                calcIndicatorRect();
                canvas.drawRect(getPaddingLeft() + mIndicatorRect.left, getHeight() - mIndicatorHeight,
                        mIndicatorRect.right + getPaddingLeft(), getHeight(), mRectPaint);*/

            if (mIndicatorHeight > 0) {
                mIndicatorDrawable.setColor(mIndicatorColor);

                if (mIndicatorGravity == Gravity.BOTTOM) {
                    mIndicatorDrawable.setBounds(paddingLeft + (int) mIndicatorMarginLeft + mIndicatorRect.left,
                            height - (int) mIndicatorHeight - (int) mIndicatorMarginBottom,
                            paddingLeft + mIndicatorRect.right - (int) mIndicatorMarginRight,
                            height - (int) mIndicatorMarginBottom);
                } else {
                    mIndicatorDrawable.setBounds(paddingLeft + (int) mIndicatorMarginLeft + mIndicatorRect.left,
                            (int) mIndicatorMarginTop,
                            paddingLeft + mIndicatorRect.right - (int) mIndicatorMarginRight,
                            (int) mIndicatorHeight + (int) mIndicatorMarginTop);
                }
                mIndicatorDrawable.setCornerRadius(mIndicatorCornerRadius);
                mIndicatorDrawable.draw(canvas);
            }
        }
    }

    //setter and getter
    public void setCurrentTab(int currentTab) {
        this.mCurrentTab = currentTab;
        mViewPager.setCurrentItem(currentTab);
    }

    public void setCurrentTab(int currentTab, boolean smoothScroll) {
        this.mCurrentTab = currentTab;
        mViewPager.setCurrentItem(currentTab, smoothScroll);
    }

    public void setIndicatorStyle(int indicatorStyle) {
        this.mIndicatorStyle = indicatorStyle;
        invalidate();
    }

    public void setTabPadding(float tabPadding) {
        this.mTabPadding = dp2px(tabPadding);
        updateTabStyles();
    }

    public void setTabSpaceEqual(boolean tabSpaceEqual) {
        this.mTabSpaceEqual = tabSpaceEqual;
        updateTabStyles();
    }

    public void setTabWidth(float tabWidth) {
        this.mTabWidth = dp2px(tabWidth);
        updateTabStyles();
    }

    public void setIndicatorColor(int indicatorColor) {
        this.mIndicatorColor = indicatorColor;
        invalidate();
    }

    public void setIndicatorHeight(float indicatorHeight) {
        this.mIndicatorHeight = dp2px(indicatorHeight);
        invalidate();
    }

    public void setIndicatorWidth(float indicatorWidth) {
        this.mIndicatorWidth = dp2px(indicatorWidth);
        invalidate();
    }

    public void setIndicatorCornerRadius(float indicatorCornerRadius) {
        this.mIndicatorCornerRadius = dp2px(indicatorCornerRadius);
        invalidate();
    }

    public void setIndicatorGravity(int indicatorGravity) {
        this.mIndicatorGravity = indicatorGravity;
        invalidate();
    }

    public void setIndicatorMargin(float indicatorMarginLeft, float indicatorMarginTop,
                                   float indicatorMarginRight, float indicatorMarginBottom) {
        this.mIndicatorMarginLeft = dp2px(indicatorMarginLeft);
        this.mIndicatorMarginTop = dp2px(indicatorMarginTop);
        this.mIndicatorMarginRight = dp2px(indicatorMarginRight);
        this.mIndicatorMarginBottom = dp2px(indicatorMarginBottom);
        invalidate();
    }

    public void setIndicatorWidthEqualTitle(boolean indicatorWidthEqualTitle) {
        this.mIndicatorWidthEqualTitle = indicatorWidthEqualTitle;
        invalidate();
    }

    public void setUnderlineColor(int underlineColor) {
        this.mUnderlineColor = underlineColor;
        invalidate();
    }

    public void setUnderlineHeight(float underlineHeight) {
        this.mUnderlineHeight = dp2px(underlineHeight);
        invalidate();
    }

    public void setUnderlineGravity(int underlineGravity) {
        this.mUnderlineGravity = underlineGravity;
        invalidate();
    }

    public void setDividerColor(int dividerColor) {
        this.mDividerColor = dividerColor;
        invalidate();
    }

    public void setDividerWidth(float dividerWidth) {
        this.mDividerWidth = dp2px(dividerWidth);
        invalidate();
    }

    public void setDividerPadding(float dividerPadding) {
        this.mDividerPadding = dp2px(dividerPadding);
        invalidate();
    }

    public void setScriptTextSize(float textSize) {
        this.mScriptTextSize = textSize;
    }

    public void setTextsize(float textsize) {
        setTextsizePx(sp2px(textsize));
    }

    public void setTextsizePx(float textsize) {
        this.mTextsize = textsize;
        updateTabStyles();
    }

    public void setTextSelectColor(int textSelectColor) {
        this.mTextSelectColor = textSelectColor;
        updateTabStyles();
    }

    public void setTextUnselectColor(int textUnselectColor) {
        this.mTextUnselectColor = textUnselectColor;
        updateTabStyles();
    }

    public void setTextBold(int textBold) {
        this.mTextBold = textBold;
        updateTabStyles();
    }

    public void setTextAllCaps(boolean textAllCaps) {
        this.mTextAllCaps = textAllCaps;
        updateTabStyles();
    }

    public void setSnapOnTabClick(boolean snapOnTabClick) {
        mSnapOnTabClick = snapOnTabClick;
    }

    public int getTabCount() {
        return mTabCount;
    }

    public int getCurrentTab() {
        return mCurrentTab;
    }

    public int getIndicatorStyle() {
        return mIndicatorStyle;
    }

    public float getTabPadding() {
        return mTabPadding;
    }

    public boolean isTabSpaceEqual() {
        return mTabSpaceEqual;
    }

    public float getTabWidth() {
        return mTabWidth;
    }

    public int getIndicatorColor() {
        return mIndicatorColor;
    }

    public float getIndicatorHeight() {
        return mIndicatorHeight;
    }

    public float getIndicatorWidth() {
        return mIndicatorWidth;
    }

    public float getIndicatorCornerRadius() {
        return mIndicatorCornerRadius;
    }

    public float getIndicatorMarginLeft() {
        return mIndicatorMarginLeft;
    }

    public float getIndicatorMarginTop() {
        return mIndicatorMarginTop;
    }

    public float getIndicatorMarginRight() {
        return mIndicatorMarginRight;
    }

    public float getIndicatorMarginBottom() {
        return mIndicatorMarginBottom;
    }

    public int getUnderlineColor() {
        return mUnderlineColor;
    }

    public float getUnderlineHeight() {
        return mUnderlineHeight;
    }

    public int getDividerColor() {
        return mDividerColor;
    }

    public float getDividerWidth() {
        return mDividerWidth;
    }

    public float getDividerPadding() {
        return mDividerPadding;
    }

    public float getTextsize() {
        return mTextsize;
    }

    public int getTextSelectColor() {
        return mTextSelectColor;
    }

    public int getTextUnselectColor() {
        return mTextUnselectColor;
    }

    public int getTextBold() {
        return mTextBold;
    }

    public boolean isTextAllCaps() {
        return mTextAllCaps;
    }

    public TextView getTitleView(int tab) {
        View tabView = mTabsContainer.getChildAt(tab);
        TextView tv_tab_title = (TextView) tabView.findViewById(R.id.tv_tab_title);
        return tv_tab_title;
    }

    /**
     * 显示未读消息
     *
     * @param position 显示tab位置
     * @param num      num小于等于0显示红点,num大于0显示数字
     */
    public void showMsg(int position, int num) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }

        View tabView = mTabsContainer.getChildAt(position);
        MsgView tipView = (MsgView) tabView.findViewById(R.id.rtv_msg_tip);
        if (tipView != null) {
            if (isNewLogic) {
                show(tipView, num);
            } else {
                UnreadMsgUtils.show(tipView, num);
            }
            if (mInitSetMap.get(position) != null && mInitSetMap.get(position)) {
                return;
            }

            if (!isNewLogic) {
                setMsgMargin(position, 4, 2);
            }
            mInitSetMap.put(position, true);
        }
    }

    private void show(MsgView tipView, int num) {
        DisplayMetrics dm = tipView.getResources().getDisplayMetrics();
        android.widget.RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) tipView.getLayoutParams();
        if (num < 10) {
            lp.setMargins((int) (-5.0F * dm.density), 0, 0, (int) (-5.0F * dm.density));
            lp.width = (int) (14.0F * dm.density);
            lp.height = (int) (12.0F * dm.density);
            tipView.setLayoutParams(lp);
            tipView.setBackgroundResource(drawable.one_red);
        } else {
            lp.setMargins(0, 0, 0, (int) (-5.0F * dm.density));
            lp.width = (int) (20.0F * dm.density);
            lp.height = (int) (12.0F * dm.density);
            tipView.setLayoutParams(lp);
            tipView.setBackgroundResource(drawable.two_red);
        }

        if (num < 100) {
            tipView.setText(num + "");
        } else {
            tipView.setText("99+");
        }

        tipView.setVisibility(View.VISIBLE);
    }

    /**
     * 设置上标或下标的内容
     *
     * @param position tab position
     * @param script   内容
     */
    public void setScriptText(int position, String script) {
        if (script == null) {
            script = "";
        }
        ScriptInfo info = optScripInfo(position);
        info.scriptText = script;
        TextView tipView = getSubscript(position);
        tipView.setText(script);
        setTitleWrapRight(position, mScriptMarginLeft, mScriptMarginRight);
        updateScriptTopBottomOut(tipView, info.isSuperScript);
    }

    /**
     * 设置为上标或下标模式， 默认为下标
     */
    public void setSuperscriptModel(int position, boolean superscript) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }

        ScriptInfo info = optScripInfo(position);
        info.isSuperScript = superscript;

        View tabView = mTabsContainer.getChildAt(position);
        TextView tipView = tabView.findViewById(R.id.tv_tab_subscript);
        if (tipView != null) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tipView.getLayoutParams();
            if (superscript) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    lp.removeRule(RelativeLayout.ALIGN_BASELINE);
                }
                lp.addRule(RelativeLayout.ALIGN_TOP, titleViewId);
                if ((tipView.getGravity() & Gravity.TOP) != Gravity.TOP) {
                    tipView.setGravity(Gravity.TOP);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    lp.removeRule(RelativeLayout.ALIGN_TOP);
                }
                lp.addRule(RelativeLayout.ALIGN_BOTTOM, titleViewId);
                if ((tipView.getGravity() & Gravity.BOTTOM) != Gravity.BOTTOM) {
                    tipView.setGravity(Gravity.BOTTOM);
                }
            }
            tipView.setLayoutParams(lp);
            updateScriptTopBottomOut(tipView, info.isSuperScript);
        }
    }

    /**
     * 更新上下标位置
     */
    private void updateScreiptTopBottomOut() {
        ScriptInfo info = null;
        int k;
        TextView tv;
        for (int i = 0; i < mTitleWrapRight.size(); i++) {
            k = mTitleWrapRight.keyAt(i);
            tv = getSubscript(k);
            if (tv != null && tv.getVisibility() == VISIBLE && tv.getText().length() > 0) {
                info = mTitleWrapRight.valueAt(i);
                updateScriptTopBottomOut(tv, info.isSuperScript);
            }
        }
    }

    /**
     * 更新上下标位置
     *
     * @param scriptView
     * @param isSuperScript
     */
    private void updateScriptTopBottomOut(View scriptView, boolean isSuperScript) {
        if (isSuperScript) {
            scriptView.setTranslationY(-mScriptTopOut);
        } else {
            scriptView.setTranslationY(mScriptBottomOut);
        }
    }

    /**
     * 获取上下标的TextView
     *
     * @param position tab position
     * @return TextView
     */
    public TextView getSubscript(int position) {
        View tabView = mTabsContainer.getChildAt(position);
        TextView tipView = tabView.findViewById(R.id.tv_tab_subscript);
        return tipView;
    }

    /**
     * 是否设置了下标
     *
     * @param tabView
     * @return
     */
    private boolean hasSubscript(View tabView) {
        if (isSubscriptAlignDisable) {
            // 禁用上下标特殊处理
            return false;
        }
        if (tabView == null) {
            return false;
        }
        TextView tipView = tabView.findViewById(R.id.tv_tab_subscript);
        return tipView != null && tipView.getVisibility() != View.GONE && tipView.getWidth() > 0;
    }

    private ScriptInfo optScripInfo(int position) {
        ScriptInfo last = mTitleWrapRight.get(position);
        if (last == null) {
            last = new ScriptInfo();
            mTitleWrapRight.put(position, last);
        }
        return last;
    }

    public void setScriptTopOut(int topOut) {
        this.mScriptTopOut = topOut;
        updateScreiptTopBottomOut();
    }

    public void setScriptBottomOut(int bottomOut) {
        this.mScriptBottomOut = bottomOut;
        updateScreiptTopBottomOut();
    }

    /**
     * 设置个别title的左右边距
     *
     * @param marginLeft  左边距，单位px
     * @param marginRight 右边距，单位px
     */
    public void setTitleWrapRight(int position, int marginLeft, int marginRight) {
        ScriptInfo last = mTitleWrapRight.get(position);
        if (last == null) {
            last = new ScriptInfo();
            mTitleWrapRight.put(position, last);
        }
        last.marginLeft = marginLeft;
        last.marginRight = marginRight;

        View tabV = mTabsContainer.getChildAt(position);
        if (tabV == null) {
            return;
        }
        if (last.marginRight != tabV.getPaddingRight()) {
            tabV.setPadding(tabV.getPaddingLeft(), tabV.getPaddingTop(), last.marginRight, tabV.getPaddingBottom());
        }
        View tv_tab_title = tabV.findViewById(titleViewId);
        if (tv_tab_title == null) {
            return;
        }
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tv_tab_title.getLayoutParams();
        if (last.marginLeft >= 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                lp.removeRule(RelativeLayout.CENTER_IN_PARENT);
                lp.addRule(RelativeLayout.CENTER_VERTICAL);
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                tv_tab_title.setPadding(tv_tab_title.getPaddingLeft(), 0, last.marginLeft, 0);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                tv_tab_title.setPadding(tv_tab_title.getPaddingLeft(), 0, tv_tab_title.getPaddingLeft(), 0);
                lp.removeRule(RelativeLayout.CENTER_VERTICAL);
            }
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        }
        tv_tab_title.setLayoutParams(lp);
    }

    public static void setViewWidth(View v, int width) {
        if (v == null) {
            return;
        }
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp.width != width) {
            lp.width = width;
            v.setLayoutParams(lp);
        }
    }

    /**
     * 显示未读红点
     *
     * @param position 显示tab位置
     */
    public void showDot(int position) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }
        showMsg(position, 0);
    }

    /** 隐藏未读消息 */
    public void hideMsg(int position) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }

        View tabView = mTabsContainer.getChildAt(position);
        MsgView tipView = (MsgView) tabView.findViewById(R.id.rtv_msg_tip);
        if (tipView != null) {
            tipView.setVisibility(View.GONE);
        }
    }

    public void showMsg2(int position, int num) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }

        View tabView = mTabsContainer.getChildAt(position);
        FrameLayout tipView = (FrameLayout) tabView.findViewById(id.rtv_msg_tip_fl);
        TextView tipViewTv = (TextView) tabView.findViewById(id.msg_tv);
        if (tipView != null) {
            DisplayMetrics dm = tipView.getResources().getDisplayMetrics();
            android.widget.RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) tipView.getLayoutParams();
            if (num < 10) {
                lp.setMargins((int) (-7.0F * dm.density), 0, 0, (int) (-7.0F * dm.density));
                lp.width = (int) (14.0F * dm.density);
                lp.height = (int) (12.0F * dm.density);
                tipView.setLayoutParams(lp);
                tipView.setBackground(getResources().getDrawable(drawable.drawable_dot_shap));
            } else {
                lp.setMargins(0, 0, 0, (int) (-7.0F * dm.density));
                lp.width = (int) (20.0F * dm.density);
                lp.height = (int) (12.0F * dm.density);
                tipView.setLayoutParams(lp);
                tipView.setBackground(getResources().getDrawable(drawable.drawable_dot_shap));
            }

            if (num < 100) {
                tipViewTv.setText(num + "");
            } else {
                tipViewTv.setText("99+");
            }

            tipView.setVisibility(View.VISIBLE);
            if (mInitSetMap.get(position) != null && (Boolean) mInitSetMap.get(position)) {
                return;
            }

            mInitSetMap.put(position, true);
        }

    }

    public void hideMsg2(int position) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }

        View tabView = mTabsContainer.getChildAt(position);
        FrameLayout tipView = (FrameLayout) tabView.findViewById(id.rtv_msg_tip_fl);
        if (tipView != null) {
            tipView.setVisibility(View.GONE);
        }

    }

    /** 设置未读消息偏移,原点为文字的右上角.当控件高度固定,消息提示位置易控制,显示效果佳 */
    public void setMsgMargin(int position, float leftPadding, float bottomPadding) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }
        View tabView = mTabsContainer.getChildAt(position);
        MsgView tipView = (MsgView) tabView.findViewById(R.id.rtv_msg_tip);
        if (tipView != null) {
            TextView tv_tab_title = (TextView) tabView.findViewById(R.id.tv_tab_title);
            mTextPaint.setTextSize(mTextsize);
            float textWidth = mTextPaint.measureText(tv_tab_title.getText().toString());
            float textHeight = mTextPaint.descent() - mTextPaint.ascent();
            MarginLayoutParams lp = (MarginLayoutParams) tipView.getLayoutParams();
            lp.leftMargin = mTabWidth >= 0 ? (int) (mTabWidth / 2 + textWidth / 2 + dp2px(leftPadding)) : (int) (mTabPadding + textWidth + dp2px(leftPadding));
            lp.topMargin = mHeight > 0 ? (int) (mHeight - textHeight) / 2 - dp2px(bottomPadding) : 0;
            tipView.setLayoutParams(lp);
        }
    }

    /** 当前类只提供了少许设置未读消息属性的方法,可以通过该方法获取MsgView对象从而各种设置 */
    public MsgView getMsgView(int position) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }
        View tabView = mTabsContainer.getChildAt(position);
        MsgView tipView = (MsgView) tabView.findViewById(R.id.rtv_msg_tip);
        return tipView;
    }

    public void setOnTabSelectListener(OnTabSelectListener listener) {
        this.mListener = listener;
    }

    class InnerPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments = new ArrayList<>();
        private String[] titles;

        public InnerPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments, String[] titles) {
            super(fm);
            this.fragments = fragments;
            this.titles = titles;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // 覆写destroyItem并且空实现,这样每个Fragment中的视图就不会被销毁
            // super.destroyItem(container, position, object);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putInt("mCurrentTab", mCurrentTab);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mCurrentTab = bundle.getInt("mCurrentTab");
            state = bundle.getParcelable("instanceState");
            if (mCurrentTab != 0 && mTabsContainer.getChildCount() > 0) {
                updateTabSelection(mCurrentTab);
                scrollToCurrentTab();
            }
        }
        super.onRestoreInstanceState(state);
    }

    public int dp2px(float dp) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public int sp2px(float sp) {
        final float scale = this.mContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * scale + 0.5f);
    }

    private void d(String msg) {
        android.util.Log.d("slidingTab", msg);
    }
}
