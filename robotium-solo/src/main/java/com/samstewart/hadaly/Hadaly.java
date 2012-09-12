package com.samstewart.hadaly;

import junit.framework.Assert;
import android.app.Activity;
import android.content.Context;
import android.test.InstrumentationTestCase;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;

import com.samstewart.hadaly.actions.Actions;
import com.samstewart.hadaly.actions.ActivityWatcher;

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
 * 4. Add to documentation ViewAsserts and MoreAsserts
 * 5. Add support for Activity monitoring
 * 6. Add more support for TouchUtils
 * 7. Waiting for views (startActivitySync, idleSync)
 * 8. Testing across activities (startActivitySync, ActivityMonitor)[http://stackoverflow.com/questions/1759626/how-do-you-test-an-android-application-across-multiple-activities]
 * 9. Some pitfals (http://codetrips.blogspot.com/2010/06/unit-testing-android-activity.html)
 * 10. Some general testing guidelines: http://stackoverflow.com/questions/522312/best-practices-for-unit-testing-android-apps
 * 11. Use more ViewAsserts
 * 12. Far more assertions *within* the testing framework
 * 13. Use activity monitor to close all activities (multiple levels)
 * 14. Use 'startInstrumentation' to avoid test project overhead
 * @author Sam Stewart
 *
 */

public class Hadaly {

	private ViewFetcher mViewSearcher;
	
	private Screenshotter   mScreenshotter;
	
	private InstrumentationTestCase mTestCase;
	
	private ActivityWatcher mActivityWatcher;
	
	private static int mWaitTime = 500; // standard time to wait in between actions, etc.
	
	protected final int TIMEOUT = 20000;
	
	protected final int SMALLTIMEOUT = 10000;
	
	protected final int ACTIVITY_WAIT_TIMEOUT = 1000;
	
	public final static String LOGGING_TAG = "Hadaly";
	
	public Hadaly(InstrumentationTestCase testCase, Activity activity) {
        mTestCase = testCase;
        mViewSearcher 	 = new ViewFetcher(activity);
        mScreenshotter	 = new Screenshotter();
        mActivityWatcher = new ActivityWatcher(testCase.getInstrumentation(), activity);
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
		Assert.assertNotNull(mTestCase);
		
		try {
			Actions.sendKeycodeAction(KeyEvent.KEYCODE_BACK).doAction(null, mTestCase, null);
			
			sleep(mWaitTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void closeAllActivities() {
		mActivityWatcher.closeAllActivities();
	}
	
	public void closeCurrentActivity() {
		mActivityWatcher.closeCurrentActivity();
	}

	
	/**
	 * Attempts to tap an HTML element in the specified webview
	 * @param webviewSelector The selector for the WebView in the view hierarchy
	 * @param htmlSelector The selector *within* the WebView's HTML content
	 */
	public void tapInWebview(String webviewSelector, String htmlSelector) {
		// get the webview first
		View webview = attemptGetView(webviewSelector);
		if (webview != null) {
			// now try to tap an html element
			Actions.tapWebviewAction(htmlSelector).doAction(mActivityWatcher.getCurrentActivity(), 
															mTestCase, 
															webview);
		}
	}
	
	public WebViewWrapper getWebViewWrapper(String webviewSelector) {
        View webview = attemptGetView(webviewSelector);
        if (webview != null) {
            WebViewWrapper wrapper = new WebViewWrapper((WebView) webview, mTestCase);
            return wrapper;
        }
        
        return null;
	}
	
	public void tap(String selector) {
		View view = attemptGetView(selector);
		
		if (view != null)
			Actions.tapAction().doAction(mActivityWatcher.getCurrentActivity(), mTestCase, view);
	}
	
	private View attemptGetView(String selector) {
		// TODO: should I be running on the activity thread instead of this thread?
		Assert.assertNotNull(mTestCase);
		
		//TODO: needs to timeout if it can't find the element
		Assert.assertNotNull(selector);
		
		Selector select = new Selector(selector);
		
		Assert.assertNotNull(select);
		
		mViewSearcher.setActivity(mActivityWatcher.getCurrentActivity());
		
		View view = mViewSearcher.getView(select);
		
		Assert.assertNotNull(view);
		
		Assert.assertTrue(view.isShown());
		
		return view;
	}
	
	public void enterText(String selector, String text) {
		View view = attemptGetView(selector);
		if (view != null)
			Actions.enterTextAction(text).doAction(mActivityWatcher.getCurrentActivity(), mTestCase, view);
		
		
	}
	
	public void waitForNewActivityToShow() {
		mActivityWatcher.waitForNewActivity();
	}
	
	public void sleep(int time) {
		try {
			Thread.sleep(time);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	public void assertActivityShowing(Class<? extends Activity> activityClass) {
		mActivityWatcher.assertActivityShowing(activityClass);
	}
	
	public void setOrientation(int orientation) {
		mActivityWatcher.getCurrentActivity().setRequestedOrientation(orientation);
	}
	
	public void takeScreenshot() {
		Assert.assertNotNull(mScreenshotter);
		
		mScreenshotter.takeScreenshot(mActivityWatcher.getCurrentActivity());
	}
	
	public void tearDown() {
		mActivityWatcher.tearDown(); // ensure no memory leaks
	}
	
}
