package com.jayway.android.robotium.solo.actions;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;
import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.ConditionVariable;
import android.test.InstrumentationTestCase;
import android.view.View;
import android.webkit.WebView;

import com.playhaven.src.utils.PHStringUtil;

/**
 * Simple action which "taps" an element *within* a webview.
 * @author samstewart
 *
 */
public class TapWebviewAction extends TapAction implements Action {
	String mSelector;
	
	private WebViewFetcher fetcher;
	
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
		
		fetcher = new WebViewFetcher((WebView)view);
		
		//TODO: actually send a tap event to the x/y coordinate since we are tapping on elements *within* the view
		
	}
	
	protected RectF getViewFrameOnScreen(View view) {
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
	
	/**
	 * Simple javascript bridge which allows us to fetch the location
	 * of an element onscreen so we can send the appropriate touch event.
	 * 
	 * @author samstewart
	 *
	 */
	private static class WebViewFetcher {
		
		private static final int JS_TIMEOUT = 5000;
		
		// atomic state variables for the current element
		private AtomicInteger x_pos = new AtomicInteger();
		
		private AtomicInteger y_pos = new AtomicInteger();
		
		private AtomicBoolean isTappable = new AtomicBoolean();
		
		private AtomicBoolean testFrameworkExists = new AtomicBoolean();
		
		private static final String UI_FRAMEWORK = 
				"(function(Zepto){\n" + 
				"    if (Zepto.fn.viewportPosition !== undefined) Hadaly.initTestFramework(true);\n" + 
				"    Zepto.fn.viewportPosition = function(selector){\n" + 
				"        if ($(selector).length === 0) return Hadaly.viewportPosition(null, null);\n" + 
				" \n" + 
				"        var offset = $(selector).offset();\n" + 
				"        var tapOffsetX = offset.width /2;\n" + 
				"        var tapOffsetY = offset.height /2;\n" + 
				" \n" + 
				"        Hadaly.viewportPosition(offset.left - window.scrollX + tapOffsetX, offset.top - window.scrollY + tapOffsetY);\n" + 
				"    }\n" + 
				"\n" + 
				"    if (Zepto.fn.isTappable !== undefined) Hadaly.initTestFramework(true);\n" + 
				"    Zepto.fn.isTappable = function(selector){\n" + 
				"        if ($(selector).length === 0) return Hadaly.isTappable(false);\n" + 
				"        \n" + 
				"        var visible = $(selector).css(\"display\") != \"none\";\n" + 
				"        var offset = $(selector).offset();\n" + 
				"        Hadaly.isTappable(visible && offset.left >= 0 && offset.top >= 0 && offset.width > 0 && offset.height > 0)\n" + 
				"    }\n" +
				"    Hadaly.initTestFramework(false);\n" +
				"})($);";
		
		private final ConditionVariable blocker = new ConditionVariable();
		
		private WebView mWebView;
		
		public WebViewFetcher(WebView webview) {
			mWebView = webview;
			
			mWebView.addJavascriptInterface(this, "Hadaly");
			
			attachTestFramework();
		}

		////////////////////////////////////////////
		//////////// Interface to WebViewFetcher.js ////////
		
		private void attachTestFramework() {
			mWebView.loadUrl("javascript: " + UI_FRAMEWORK);
		}
		
		private void executeJS(String javascript) {
			if (mWebView == null) return;
			
			attachTestFramework();
			
			blocker.block();
			
			mWebView.loadUrl("javascript: " + javascript);
			
			blocker.block();
		}
		
		///////////////////////////////////////////
		/////// Action Methods ////////////////////
		
		public void enterText(String selector, String text) {
			//TODO: actually enter text in the webview
			executeJS("Hadaly.log('test')");
		}
		
		public PointF getElementLocation(String selector) {
			// executeJS("Zepto.fn.isTappable('" + selector + "');");
			executeJS("Zepto.fn.viewportPosition('" + selector + "');");
			
			Assert.assertTrue (isTappable.get());
			Assert.assertFalse(x_pos.get() >= Integer.MAX_VALUE);
			Assert.assertFalse(x_pos.get() <= 0);
			Assert.assertFalse(y_pos.get() >= Integer.MAX_VALUE);
			Assert.assertFalse(y_pos.get() <= 0);
			
			return new PointF(Float.intBitsToFloat(x_pos.get()), 
							  Float.intBitsToFloat(y_pos.get()));
		}
		
		////////////////////////////////////////////
		/////////// Callbacks from JS /////////////
		/////////// Purposely not used locally ////
		
		public void viewportPosition(String x, String y) {
			if (x != null && y != null) {
				Float xf = Float.parseFloat(x);
				Float yf = Float.parseFloat(y);
				PHStringUtil.log("viewport position: " + xf + ", " + yf);
				
				x_pos.set(Float.floatToIntBits(xf));
				y_pos.set(Float.floatToIntBits(yf));
			} else {
				x_pos.set(Integer.MAX_VALUE);
				y_pos.set(Integer.MAX_VALUE);
			}
			
			
			
			blocker.open();
		}
		
		public void isTappable(String tappable) {
			isTappable.set(Boolean.parseBoolean(tappable));
			
			blocker.open();
		}
		
		public void initTestFramework(String alreadyExists) {
			testFrameworkExists.set(Boolean.parseBoolean(alreadyExists));
			
			blocker.open();
		}
	}

}
