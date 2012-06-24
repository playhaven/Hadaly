package com.jayway.android.robotium.solo.actions;

import android.app.Activity;
import android.graphics.PointF;
import android.test.InstrumentationTestCase;
import android.view.View;

/**
 * Simple scroll action on a scrollview
 */
public class ScrollAction implements Action {

	public ScrollAction(PointF scrollStart, PointF scrollEnd) {
		
	}
	
	@Override
	public void setView(View view) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doAction(Activity activity, InstrumentationTestCase testCase, View view) {
		// TODO Auto-generated method stub
		// view.setSelection(lineToMoveTo);
		// .scrollBy, .smoothScrollBy
	}

}
