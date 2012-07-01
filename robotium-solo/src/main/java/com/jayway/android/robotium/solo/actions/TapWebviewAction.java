package com.jayway.android.robotium.solo.actions;

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
 * @author samstewart
 *
 */
public class TapWebviewAction extends TapAction implements Action {
	
	private static final int WAIT_PAGE_RELOAD = 5000;
	
	private String mSelector;
	
	
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
		}
		
		@Override
		public void getLocationOnScreen(int[] xy) {
			int[] totalXY = new int[2];
			
			super.getLocationOnScreen(totalXY);
			
			// add the sub region offset
			totalXY[0] += mSubRegion.left;
			totalXY[1] += mSubRegion.top;
		}
		
		@Override
		public void onMeasure(int widthMeasure, int heightMeasure) {
			// we need to pretend the view's real size is the subregion
			
			int totalWidth = super.getWidth();
			// shrink down to subregion (both sides of subregion)
			totalWidth = totalWidth - (int)mSubRegion.left - (int)mSubRegion.width();
			
			int totalHeight = super.getHeight();
			// shrink down to subregion (both top and bottom of subregion)
			totalHeight = totalHeight - (int)mSubRegion.top - (int)mSubRegion.height();
			
			
			setMeasuredDimension(totalWidth, totalHeight);
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
		RectF elementRect;
		final WebViewFetcher fetcher = new WebViewFetcher((WebView)webviewF);
		
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
		
		testCase.runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				fetcher.attachTestFramework(); // install the JS test framework
				
				Assert.assertNotNull(mSelector);
				
				RectF elementRect = getViewFrameOnScreen(webviewF, fetcher);
				 
				Assert.assertFalse(elementRect.left == Float.MAX_VALUE);
				Assert.assertFalse(elementRect.top  == Float.MAX_VALUE);
				
			}
		});
				
				
		} catch (Throwable t) {
			t.printStackTrace(); // swallow all problems
			Assert.fail(t.getMessage());
		}
		
		// let app catch up..
		testCase.getInstrumentation().waitForIdleSync();
		
		// create a webview wrapper which "fakes" the given touch region
		WebViewWrapper wrapper = new WebViewWrapper((WebView)view, new RectF());
		
		try {
			TouchUtils.clickView(testCase, wrapper); // touch on the wrapper
		} catch(Exception e) {
			Assert.fail("Could not tap: " + e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	protected RectF getViewFrameOnScreen(View view, WebViewFetcher fetcher) {
		RectF elementFrame = new RectF(Float.MAX_VALUE, 
				  					   Float.MAX_VALUE, 
				  					   Float.MAX_VALUE, 
				  					   Float.MAX_VALUE);
		
		if (fetcher == null) return elementFrame;
		
		PointF elementLocation = fetcher.getElementLocation(mSelector);
		
		Assert.assertFalse(elementLocation.x == Float.MAX_VALUE);
		Assert.assertFalse(elementLocation.y == Float.MAX_VALUE);
		
		elementFrame = new RectF(elementLocation.x, 
								 elementLocation.y, 
								 DEFAULT_WEBVIEW_ELEMENT_WIDTH, 
								 DEFAULT_WEBVIEW_ELEMENT_HEIGHT);
		return elementFrame;
		
	}


}
