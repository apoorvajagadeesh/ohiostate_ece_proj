package com.osu.sensoranalytics.widgets;

import org.mapsforge.map.android.view.MapView;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class MapViewPager extends ViewPager {

	public MapViewPager(Context context) {
		super(context);
	}

	public MapViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	

	@Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof MapView) {
            return true;
        }
        return super.canScroll(v, checkV, dx, x, y);
    }

}
