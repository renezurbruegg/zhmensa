package com.mensa.zhmensa.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;

/**
 * Viewpager implemnetation that does change tabs on left / right swipe
 */
public class InterceptAllVerticalSwipesViewPager extends ViewPager {


    private static final int MIN_DRAG_VALUE = 20;
    private Float lastY = null;
    private Float lastX = null;

    public InterceptAllVerticalSwipesViewPager(Context context) {
        super(context);
        setMyScroller();
    }

    public InterceptAllVerticalSwipesViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMyScroller();
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // really ugly fix to let viewpager swipe not get intercepted by recycler view.
        // Returns always true if motion event swipes vertically
        // Thus NO CHILD VIEW WILL EVER GET TRIGGERED ON VERTICAL SWIPE EVENTS!
        // this might cause some bugs

        if(lastX == null) {
            lastX = event.getX();
        }
        if(lastY == null) {
            lastY = event.getY();
        }


        float xDiff = Math.abs(lastX - event.getX());
        float yDiff = Math.abs(lastY - event.getY());

        lastX = event.getX();
        lastY = event.getY();

        if(event.getAction() == MotionEvent.ACTION_MOVE  && xDiff != 0 && xDiff > yDiff && xDiff > MIN_DRAG_VALUE) {
                return true;
        }

        return super.onInterceptTouchEvent(event);

    }
    //down one is added for smooth scrolling

    private void setMyScroller() {
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this, new MyScroller(getContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MyScroller extends Scroller {
        public MyScroller(Context context) {
            super(context, new DecelerateInterpolator());
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, 350 /*1 secs*/);
        }
    }
}