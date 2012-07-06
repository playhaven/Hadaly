package com.samstewart.hadaly.actions;

import com.playhaven.src.publishersdk.content.PHContentView;

import junit.framework.Assert;
import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.view.View;
import android.webkit.WebView;

/**
 * Simple action which "taps" an element *within* a webview.
 * TODO: a number of interdependencies we should clean up (fetcher)
 * @author samstewart
 *
 */
public class TapWebviewAction extends TapAction implements Action {
	
	private static final int WAIT_PAGE_RELOAD = 2000;
	
	private static final int WAIT_PROTOCOL_SET = 200;
	
	private String mSelector;
	
	private RectF mElementRect;
	
	public static int DEFAULT_WEBVIEW_ELEMENT_WIDTH = 100;
	
	public static int DEFAULT_WEBVIEW_ELEMENT_HEIGHT = 100;
	
	public TapWebviewAction(String selector) {
		mSelector = selector;
	}
	
	/**
	 * Simple wrapper class which allows us to fake
	 * the width/height/offset so that the TouchUtils class
	 * taps on the appropriate *part* of the webview (right over
	 * HTML element we want to tap). Bit tricky and hacky but better
	 * than rewriting all of our own .sendPointerSync(event) code...
	 * @author samstewart
	 *
	 */
	private class WebViewWrapper extends WebView {

		private WebView mWebview;
		
		private RectF mSubRegion;
		
		public WebViewWrapper(Context context) {
			super(context);
			
		}
		
		public WebViewWrapper(WebView webview, RectF subRegion) {
			super(webview.getContext());
			
			mWebview = webview;
			mSubRegion = subRegion;
			
			// no padding because we want measuredWidth/Height to match
			// width/height
			this.setPadding(0, 0, 0, 0); 
			
			// we need to pretend the view's real size is the subregion
			//TODO: what if the webview has padding?
			
			// force the size (don't care about x/y)
			// We're assuming screen coordinates since we don't have a parent view
			// we just want the width and height to be equal to the mSubRegion (we get the xy on getLocationOnScreen)
			// We know that width = right - left = subRegionWidth - 0 = subRegionWidth
			// and we know height = bottom - top = subRegionHeight - 0 = subRegionHeight
			layout(0, 0, 
				   (int)mSubRegion.width(), (int)mSubRegion.height());
		}
		
		@Override
		public void getLocationOnScreen(int[] xy) {
			int[] totalXY = new int[2];
			
			mWebview.getLocationOnScreen(totalXY);
			
			// add the sub region offset
			totalXY[0] += mSubRegion.left;
			totalXY[1] += mSubRegion.top;
			
			xy[0] = totalXY[0];
			xy[1] = totalXY[1];
		}
		
		
	}
	@Override
	public void setView(View view) {
		// pass
		
	}

	@Override
	public void doAction(Activity activity, InstrumentationTestCase testCase, View view) {
		Assert.assertEquals(WebView.class, view.getClass());
		
		final WebView webviewF = (WebView)view;
		mElementRect = null; // reset
		final WebViewFetcher fetcher = new WebViewFetcher((WebView)webviewF);
		
		// Note: [!] DO NOT MAKE ANY ASSERTIONS WHILE ON THE UI THREAD! 
		// THEY WILL BE CAUGHT AS EXCEPTIONS AND LITTLE INFORMATION WILL BE PASSED UP
		
		// Note: this code is slightly unsettling.
		// Basically, there are three steps:
		// [On Main Thread]
		// 1. Bind the native js interface
		// 2. Reload url to enable this interface
		// [On instrumentation thread]
		// 1. Wait a few seconds for all callbacks to complete
		// [On Main Thread]
		// 1. Run WebViewFetcher framework (block until we get result)
		// [On Instrumentation thread]
		// 1. Call method passing in the coordinates
		//
		// If death results from staring at this code, Playhaven is absolved from all responsibility
		try {
		testCase.runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				// we have to add the JS interface explicitly (doesn't seem to work WebViewFetcher)
				// TODO: Note: it appears you can only bind one interface at a time? 
				webviewF.addJavascriptInterface(fetcher, WebViewFetcher.JS_FRAMEWORK_NAME);
				
				// reload page to ensure the native bindings are available (KEY)
				// TODO: annoying since it might trigger bad side effects. Not many options though..
				// TODO: Wish we could intercept *before* webview loads
				webviewF.loadUrl(webviewF.getOriginalUrl()); 
				
			}
		});
		
		// wait for reload on instrumentation thread
		// (empirically tuned)
		try {
			Thread.sleep(WAIT_PAGE_RELOAD);
		} catch (Exception e) { // swallow all exceptions
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
			
		// just in case
		testCase.getInstrumentation().waitForIdleSync();
		
		// now set the protocol version again
		testCase.runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				PHContentView.setWebviewProtocolVersion(webviewF);
			}
		});
		
		// just in case
		testCase.getInstrumentation().waitForIdleSync();
				
		// wait for the protocol to set
		try {
			Thread.sleep(WAIT_PROTOCOL_SET);
		} catch (Exception e) { // swallow all exceptions
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		
		Assert.assertNotNull(mSelector);
		
		testCase.runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				fetcher.attachTestFramework(); // install the JS test framework
				
				RectF elementRect = getViewFrameOnScreen(webviewF, fetcher);
				
				setElementRect(elementRect);
			}
		});
		} catch (Throwable t) {
			t.printStackTrace(); // swallow all problems
			Assert.fail(t.getMessage());
		}
		
		// let app catch up..
		testCase.getInstrumentation().waitForIdleSync();
		
		Assert.assertNotNull(mElementRect);
		
		if (mElementRect.left == WebViewFetcher.ELEMENT_NOT_FOUND ||
			mElementRect.top  == WebViewFetcher.ELEMENT_NOT_FOUND   ) {
			Assert.fail("Could not find HTML element for selector '" + mSelector + "'");
		}
		
		
		// create a webview wrapper which "fakes" the given touch region so we can "click"
		// on it with TouchUtils.clickView
		// TODO: should be synchronized access to mElementRect?
		WebViewWrapper wrapper = new WebViewWrapper(webviewF, mElementRect);
		
		try {		
			TouchUtils.clickView(testCase, wrapper); // touch on the wrapper (which has the underlying webview)
		} catch(Exception e) {
			Assert.fail("Could not tap: " + e.getMessage());
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	protected void setElementRect(RectF rect) {
		// TODO: should we be synchronizing this?
		mElementRect = rect;
	}
	
	protected RectF getViewFrameOnScreen(View view, WebViewFetcher fetcher) {
		PointF elementLocation = fetcher.getElementLocation(mSelector);
		
		// TODO: should we use the real device element width?
		RectF elementFrame = new RectF(elementLocation.x, 
									   elementLocation.y, 
									   elementLocation.x + DEFAULT_WEBVIEW_ELEMENT_WIDTH, 
									   elementLocation.y + DEFAULT_WEBVIEW_ELEMENT_HEIGHT);
		return elementFrame;
		
	}


}
