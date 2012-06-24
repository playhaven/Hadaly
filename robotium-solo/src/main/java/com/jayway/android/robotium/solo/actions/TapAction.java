package com.jayway.android.robotium.solo.actions;

import junit.framework.Assert;
import android.app.Activity;
import android.graphics.RectF;
import android.os.SystemClock;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/** Standard tap action for a view.
 * 
 * For more information on the underlying technique, 
 * please examine the TouchUtils source code in the Android source code.
 */
public class TapAction implements Action {
	
	public TapAction() {
		
	}
	
	@Override
	public void doAction(Activity activity, InstrumentationTestCase testCase, View view) {	
		try {
			TouchUtils.clickView(testCase, view);
		} catch(Exception e) {
			Assert.fail("Could not tap: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override
	public void setView(View view) {
		// pass
	}
}
