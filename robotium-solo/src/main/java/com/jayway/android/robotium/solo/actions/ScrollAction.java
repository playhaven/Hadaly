package com.jayway.android.robotium.solo.actions;

import android.app.Activity;
import android.app.Instrumentation;
import android.graphics.PointF;
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
	public void doAction(Activity activity, Instrumentation inst, View view) {
		// TODO Auto-generated method stub
		// view.setSelection(lineToMoveTo);
		// .scrollBy, .smoothScrollBy
	}

}
