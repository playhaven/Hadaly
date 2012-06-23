package com.jayway.android.robotium.solo;

import junit.framework.Assert;
import android.app.Activity;
import android.app.Instrumentation;
import android.view.KeyEvent;
import android.view.View;

/**
 * Main utility class for running UI tests.
 * 
 * Made up of three components:
 * 1. View fetcher which searches the view hierarchy
 * 2. View clicker which actually clicks on the view
 * 3. Text enterer which actually enters the text
 * 4. Waiter which waits on various conditions
 * 
 * TODO:
 * 1. Add support for clicking on menu items
 * 2. Add support for scrolling list views
 * 3. Add support for spinners, and other abnormal UI elements
 * @author Sam Stewart
 *
 */

public class Hadaly {

	private ViewFetcher mViewSearcher;
		
	private Screenshotter   mScreenshotter;
	
	private Instrumentation mInstrumentation;
	
	private Activity 		mActivity;
	
	private static int mWaitTime = 500; // standard time to wait in between actions, etc.
	
	protected final int TIMEOUT = 20000;
	
	protected final int SMALLTIMEOUT = 10000;
	
	public final static String LOGGING_TAG = "[Hadaly]";


	public Hadaly(Instrumentation instrumentation, Activity activity) {
        mInstrumentation = instrumentation;
        mActivity = activity;
        mViewSearcher = new ViewFetcher(activity);
        mScreenshotter = new Screenshotter();
	}


	
	/** Sets the time to wait when waiting for ui
	 * elements, etc.
	 * @param time Standard time to wait (in milliseconds) when pausing, etc.
	 */
	public static void setWaitTime(int time) {
		mWaitTime = time;
	}
	
	public static int getWaitTime() {
		return mWaitTime;
	}
	
	
	public void goBack() {
		
		try {
			Thread.sleep(mWaitTime);
			mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
			
			Thread.sleep(mWaitTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void tap(String selector) {
		Assert.assertNotNull(selector);
		
		Selector select = new Selector(selector);
		
		Assert.assertNotNull(select);
		
		View view = mViewSearcher.getView(select);
		
		Assert.assertNotNull(view);
		
		//TODO: actually tap the view
	}
	
	public void enterText(String selector, String text) {
		Assert.assertNotNull(selector);
		
		Selector select = new Selector(selector);
		
		Assert.assertNotNull(select);
		
		View view = mViewSearcher.getView(select);
		
		Assert.assertNotNull(view);
		
	}

	
	public void takeScreenshot() {
		if (mActivity == null) return;
		
		mScreenshotter.takeScreenshot(mActivity);
		
	}
	
}
