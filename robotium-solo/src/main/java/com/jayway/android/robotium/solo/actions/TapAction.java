package com.jayway.android.robotium.solo.actions;

import junit.framework.Assert;
import android.app.Activity;
import android.app.Instrumentation;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

/** Standard tap action for a view*/
public class TapAction implements Action {
	private final int SHORT_SLEEP = 100;
	
	public TapAction() {
		
	}
	
	@Override
	public void doAction(Activity activity, Instrumentation inst, View view) {
		RectF viewPos = getViewFrameOnScreen(view);
		
		long dnTime  = SystemClock.uptimeMillis();
		
		MotionEvent eventDwn = MotionEvent.obtain(dnTime, 
											   SystemClock.uptimeMillis(),
											   MotionEvent.ACTION_DOWN, 
											   viewPos.centerX(), 
											   viewPos.centerY(), 
											   0);
		
		MotionEvent eventUp = MotionEvent.obtain(dnTime, 
											    SystemClock.uptimeMillis(),
											    MotionEvent.ACTION_UP, 
											    viewPos.centerX(), 
											    viewPos.centerY(), 
											    0);
		try {
			inst.sendPointerSync(eventDwn);
			inst.sendPointerSync(eventUp);
			
			Thread.sleep(SHORT_SLEEP);
			
		} catch(Exception e) {
			Assert.fail("Could not tap: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	protected RectF getViewFrameOnScreen(View view) {
		int[] xy = new int[2];

		view.getLocationOnScreen(xy);
		
		return new RectF(xy[0], xy[1], view.getWidth(), view.getHeight());
	}
	
	@Override
	public void setView(View view) {
		// pass
	}
}
