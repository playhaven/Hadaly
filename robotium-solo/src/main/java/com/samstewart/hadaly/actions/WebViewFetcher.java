package com.samstewart.hadaly.actions;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Assert;
import android.graphics.PointF;
import android.os.ConditionVariable;
import android.webkit.WebView;

import com.playhaven.src.utils.PHStringUtil;


/**
 * Simple javascript bridge which allows us to fetch the location
 * of an element onscreen so we can send the appropriate touch event.
 * 
 * Used only by TapWebviewAction (we had to break it out into a separate class)
 * 
 * TODO: the HTML template must have the appropriate viewport tag to appropriately
 * scale the content so that we get the proper screen coordinates. Currently we perform the scaling
 * manually but this might cause problems if the user decides to set a viewport <meta> tag.
 * Thoughts?
 * 
 * Helpful Links:
 * http://www.flynsarmy.com/2011/08/how-to-fix-strange-web-page-widths-on-android-browser/
 * http://stackoverflow.com/questions/2796814/how-do-i-get-the-wvga-android-browser-to-stop-scaling-my-images/2799580
 * http://stackoverflow.com/questions/8628860/android-webview-javascript-screen-dimensions-vs-actual-screen-dimensions
 * @author samstewart
 *
 */
public class WebViewFetcher {
	
	private static final int JS_TIMEOUT = 5000;
	
	public static final String JS_FRAMEWORK_NAME = "Hadaly";
	
	// a flag indicating we got a javascript callback over the native bridge
	private AtomicBoolean didReceiveJSCallback = new AtomicBoolean();
	
	// atomic state variables for the current element
	private AtomicInteger x_pos = new AtomicInteger();
	
	private AtomicInteger y_pos = new AtomicInteger();
	
	private AtomicBoolean isTappable = new AtomicBoolean();
	
	private AtomicBoolean testFrameworkExists = new AtomicBoolean();
	
	private AtomicReference<String> text = new AtomicReference<String>();
	
	public static final int ELEMENT_NOT_FOUND = -1;
	
	// TODO: use JS_FRAMEWORK_NAME instead of Hadaly inline
	private static final String UI_FRAMEWORK = 
			"(function(Zepto) {\n" + 
			"    if (Zepto.fn.viewportPosition !== undefined) { " +
			"		Hadaly.initTestFramework(true);\n" + 
			"		return;\n" +
			"	 }\n" +
			"    Zepto.fn.viewportPosition = function(selector){\n" + 
			"        if ($(selector).length === 0) return Hadaly.viewportPosition(null, null);\n" + 
			" \n" + 
			"        var offset = $(selector).offset();\n" + 
			"        Hadaly.viewportPosition((offset.left - window.scrollX) * window.devicePixelRatio, (offset.top - window.scrollY) * window.devicePixelRatio);\n" +		
			"    }\n" + 
			"\n" + 
			"    if (Zepto.fn.isTappable !== undefined) {\n" +
			"		Hadaly.initTestFramework(true);\n" +
			"		return;\n" +
			"	 }\n" +
            "\n" +
			"    Zepto.fn.getText = function(selector){\n" +
			"        return Hadaly.getTextCallback($(selector).text());\n" +
			"    }\n" +
            "\n" +
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
	}

	////////////////////////////////////////////
	//////////// Interface to WebViewFetcher.js ////////
	
	public void attachTestFramework() {
		// install test framework
		executeJS(UI_FRAMEWORK);
	}
	
	private void executeJS(String javascript) {
		if (mWebView == null) return;
		
		mWebView.loadUrl("javascript: " + javascript);
		
		blocker.close(); // reset
		
		// wait until we get a JS callback
		blocker.block(JS_TIMEOUT);
		
		// make sure we actually received a callback across the native bridge
		Assert.assertTrue(didReceiveJSCallback.get());
		didReceiveJSCallback.set(false); // reset
	}
	
	///////////////////////////////////////////
	/////// Action Methods ////////////////////
	
	public void enterText(String selector, String text) {
		//TODO: actually enter text in the webview
		executeJS(JS_FRAMEWORK_NAME + ".log('test')");
	}
	
	public PointF getElementLocation(String selector) {
		executeJS("$.fn.viewportPosition('" + selector + "');");
		
		// will block for a bit
		
		return new PointF(Float.intBitsToFloat(x_pos.get()), 
						  Float.intBitsToFloat(y_pos.get()));
	}
	
	public String getText(String selector) {
	    executeJS("$.fn.getText('" + selector + "');");
	    
	    return text.get();
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
			x_pos.set(Float.floatToIntBits(ELEMENT_NOT_FOUND));
			y_pos.set(Float.floatToIntBits(ELEMENT_NOT_FOUND));
		}
		
		didReceiveJSCallback.set(true);
		
		blocker.open();
	}
	
	public void getTextCallback(String result) {
        text.set(result);
        
        didReceiveJSCallback.set(true);
        blocker.open();
    }
	
	public void isTappable(String tappable) {
		isTappable.set(Boolean.parseBoolean(tappable));
		
		didReceiveJSCallback.set(true);
		blocker.open();
	}
	
	public void initTestFramework(String alreadyExists) {
		testFrameworkExists.set(Boolean.parseBoolean(alreadyExists));
		
		didReceiveJSCallback.set(true);
		blocker.open();
	}
}
