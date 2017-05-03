package com.son.subsidecode.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * 适用于动态标签布局
 */
public class TagViewGroup extends ViewGroup {
    private int LINE_SPACE = 15;
    private int COLUMN_SPACE = 10;

    private int mLineSpace;
    private int mColumnSpace;

    private ArrayList<View> views = new ArrayList<>();

    private int gravity = Gravity.CENTER_HORIZONTAL;

    public TagViewGroup(Context context) {
        this(context, null);
    }

    public TagViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, ATTRS);
        a.recycle();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public TagViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, ATTRS, defStyleAttr, defStyleAttr);
        a.recycle();
    }

    private static final int[] ATTRS = {android.R.attr.checkedButton};
    {
        mLineSpace = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                LINE_SPACE, getResources().getDisplayMetrics());
        mColumnSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                COLUMN_SPACE, getResources().getDisplayMetrics());
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        if(childCount == 0){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int contentWidth = width - getPaddingLeft() - getPaddingRight();
        int lineWidth = 0;
        int lines = 0;
        int lineHeight = 0;
        int column = 0;
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if(childAt.getVisibility() == GONE)continue;
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            measureChild(childAt, widthMeasureSpec, heightMeasureSpec);
            lineWidth += childAt.getMeasuredWidth();
            lineHeight = childAt.getMeasuredHeight();
            if(lineWidth > contentWidth) {
                lines++;
                lineWidth = childAt.getMeasuredWidth();
                column = 0;
            } else {
                lineWidth += mColumnSpace;
                column ++;
            }
            layoutParams.columnInLine = column;
            layoutParams.lineIndex = lines;
        }
        int height = getPaddingTop() + getPaddingBottom() + (lines + 1) * lineHeight + mLineSpace * lines;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(gravity == Gravity.LEFT) {
            onLayoutLeftAlign();
        } else if(gravity == Gravity.CENTER_HORIZONTAL) {
            onLayoutCenterAlign();
        }
    }

    private void onLayoutCenterAlign() {
        int childCount = getChildCount();

        int lineWidth = 0;
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if(childAt.getVisibility() == GONE){
                continue;
            }
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            views.add(childAt);
            lineWidth += childAt.getMeasuredWidth();
            if(i+ 1 < childCount){
                View nextChild = getChildAt(i + 1);
                if(nextChild.getVisibility() == GONE)continue;
                LayoutParams lp = (LayoutParams) nextChild.getLayoutParams();
                if(layoutParams.lineIndex < lp.lineIndex){
                    layoutLine(lineWidth, childAt.getMeasuredHeight(), layoutParams);
                    lineWidth = 0;
                }
            } else {
                layoutLine(lineWidth, childAt.getMeasuredHeight(), layoutParams);
                lineWidth = 0;
            }

        }
    }

    private void layoutLine(int lineWidth, int itemHeight,  LayoutParams layoutParams) {
        int contentWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int left = getPaddingLeft() +(contentWidth - lineWidth- (views.size() - 1) * mColumnSpace) / 2;
        int right = 0;
        int top = getPaddingTop() + (layoutParams.lineIndex * (mLineSpace + itemHeight));
        for (int j = 0; j < views.size(); j++) {
            View view = views.get(j);
            right = left+ view.getMeasuredWidth();
            view.layout(left, top, right, top + view.getMeasuredHeight());
            left  = right + mColumnSpace;
        }
        views.clear();
    }

    private void onLayoutLeftAlign() {
        int childCount = getChildCount();
        int lineLeft = 0;
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if(childAt.getVisibility() == GONE){
                continue;
            }
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            if(layoutParams.columnInLine == 0) {
                lineLeft = getPaddingLeft();
            }
            int left = lineLeft;
            lineLeft += childAt.getMeasuredWidth();
            int top = (layoutParams.lineIndex * (mLineSpace + childAt.getMeasuredHeight()));
            childAt.layout(left, top, lineLeft, top + childAt.getMeasuredHeight());
            lineLeft += mLineSpace;
        }
    }

    class LayoutParams extends ViewGroup.LayoutParams{

        int lineIndex = 0;
        int columnInLine = 0;
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}
