package com.example.magnus.livingthing;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by magnus on 16-03-27.
 */
public class ViewPagerWithException extends ViewPager  {



    public ViewPagerWithException(Context context) {
        super(context);
    }

    public ViewPagerWithException(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}

