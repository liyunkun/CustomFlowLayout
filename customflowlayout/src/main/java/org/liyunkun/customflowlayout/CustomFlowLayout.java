package org.liyunkun.customflowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyunkun on 2016/10/29 0029.
 */
public class CustomFlowLayout extends ViewGroup{
    //定义一个集合用于存储容器中所有的控件（List<View>用于存放每一行的控件）
    private List<List<View>> mAllViews = new ArrayList<>();
    //定义一个集合用于存储每一行的最大高度
    private List<Integer> lineMaxHeight = new ArrayList<>();

    public CustomFlowLayout(Context context) {
        this(context,null);
    }

    public CustomFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 注意：一定记得要重写这三个方法；
     * 如果不重写，则 MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();这时，
     * 会报类型转换异常
     * generateDefaultLayoutParams
     * generateLayoutParams
     * generateLayoutParams
     * @return
     */
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }


    //测量大小（容器的高）
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取容器的宽度
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        //获取容器宽度的模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        //获取容器的高度
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //获取容器高度的模式
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //记录容器内控件的总宽度
        int totalWidth = 0;
        //记录容器内控件的总高度
        int totalHeight = 0;
        //记录容器内每一行中控件的最大高度
        int perLineMaxHeight = 0;
        //获取容器中控件的个数
        int childCount = getChildCount();
        //循环遍历容器内的控件
        for (int i = 0; i < childCount; i++) {
            //获取容器内某一个控件
            View childView = getChildAt(i);
            //测量子控件的大小
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
            //获取该控件对应的LayoutParams
            MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();
            //获取容器中某一控件在容器中所占的宽度（等于控件的宽度+左外边距+右外边距）
            int childWidth = childView.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            //获取容器中某一控件在容器中所占的高度（等于控件的高度+上外边距+下外边距）
            int childHeight = childView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            //判读将要放进容器的控件宽度+之前控件的总宽度是否大于容器的总宽度，如果大于，则换行；否则不换行。
            if (totalWidth + childWidth > widthSize) {//换行
                //将第一个控件的宽度赋给总宽度
                totalWidth = childWidth;
                //将第一行中最大高度赋值与总宽度相加
                totalHeight += perLineMaxHeight;
                //将该行的最大高度设置为第一个控件的高度
                perLineMaxHeight = childHeight;
            } else {//不换行
                //将子控件的宽度赋给总宽度
                totalWidth += childWidth;
                //用前一个控件的高度与后一个控件的高度进行比较，将最大值赋值行最大高度
                perLineMaxHeight = Math.max(perLineMaxHeight, childHeight);
            }
            //当最后一行时，设置总高度
            if (i == childCount - 1) {
                totalHeight += perLineMaxHeight;
            }
        }
        switch (heightMode) {
            case MeasureSpec.AT_MOST://wrap_content
                heightSize = totalHeight;
                break;
            case MeasureSpec.UNSPECIFIED://不确定大小
                heightSize = totalHeight;
                break;
            case MeasureSpec.EXACTLY://确定的值或match_parent
                break;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    /**
     * 控件的摆放
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //注意：使用之前记得要先将集合清空一下，因为这个方法会执行多次
        mAllViews.clear();
        lineMaxHeight.clear();

        /************************填充集合的数据*****************************/
        int childCount = getChildCount();
        int totalWidth = 0;
        int perLineMaxHeight = 0;
        List<View> viewList = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
            int childWidth = view.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = view.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            if (totalWidth + childWidth > getMeasuredWidth()) {
                mAllViews.add(viewList);
                lineMaxHeight.add(perLineMaxHeight);
                viewList = new ArrayList<>();
                totalWidth = 0;
                perLineMaxHeight = 0;
            }
            perLineMaxHeight = Math.max(perLineMaxHeight, childHeight);
            totalWidth += childWidth;
            viewList.add(view);
        }
        lineMaxHeight.add(perLineMaxHeight);
        mAllViews.add(viewList);

        /*************************根据集合来摆放子控件的位置***********************************/
        int mLeft = 0;
        int mTop = 0;
        for (int i = 0; i < mAllViews.size(); i++) {
            List<View> views = mAllViews.get(i);
            for (int j = 0; j < views.size(); j++) {
                View view = views.get(j);
                MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
                int left = mLeft + lp.leftMargin;
                int top = mTop + lp.topMargin;
                int right = left + view.getMeasuredWidth();
                int bottom = top + view.getMeasuredHeight();
                view.layout(left, top, right, bottom);
                mLeft += view.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            }
            mLeft = 0;
            mTop += lineMaxHeight.get(i);
        }
    }
}
