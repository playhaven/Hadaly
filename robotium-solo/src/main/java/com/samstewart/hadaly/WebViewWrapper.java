package com.samstewart.hadaly;

import junit.framework.Assert;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;

import com.playhaven.src.publishersdk.content.PHContentView;
import com.samstewart.hadaly.actions.WebViewFetcher;

public class WebViewWrapper {

    private static final int WAIT_PAGE_RELOAD  = 2000;
    private static final int WAIT_PAGE_SCROLL  = 2000;
    private static final int WAIT_PROTOCOL_SET = 200;
    
    private static final int WAIT_BREAK = 1000;
    
    public static int DEFAULT_WEBVIEW_ELEMENT_WIDTH = 50;
    public static int DEFAULT_WEBVIEW_ELEMENT_HEIGHT = 50;
    
    private WebView mWebView;
    private InstrumentationTestCase mTestCase;
    private WebViewFetcher mFetcher;
    
    private RectF mElement;
    
    private class WebViewRegion extends WebView {

        private WebView mWebview;
        
        private RectF mSubRegion;
        
        public WebViewRegion(Context context) {
            super(context);
            
        }
        
        public WebViewRegion(WebView webview, RectF subRegion) {
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
    
    public WebViewWrapper(WebView webView, InstrumentationTestCase testCase) {
        mWebView = webView;
        mTestCase = testCase;
        
        mFetcher = new WebViewFetcher(mWebView);
        
        mWebView.addJavascriptInterface(mFetcher, WebViewFetcher.JS_FRAMEWORK_NAME);
        
        try {
            mWebView.loadUrl(mWebView.getOriginalUrl());
            Thread.sleep(WAIT_PAGE_RELOAD);

            PHContentView.setWebviewProtocolVersion(mWebView);
            Thread.sleep(WAIT_PROTOCOL_SET);
        } catch (Exception e) { // swallow all exceptions
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        
        mFetcher.attachTestFramework(); // install the JS test framework
    }
    
    public void scrollDown(final boolean bottom) {
        try {
            mTestCase.runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.pageDown(bottom);
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        
        try {
            Thread.sleep(WAIT_PAGE_SCROLL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void tapOnElement(final String selector) {
        Assert.assertNotNull(selector);
        
        mElement = getViewFrameOnScreen(mWebView, mFetcher, selector);
        
        // let app catch up..
        mTestCase.getInstrumentation().waitForIdleSync();
        
        Assert.assertNotNull(mElement);
        
        if (mElement.left == WebViewFetcher.ELEMENT_NOT_FOUND ||
                mElement.top  == WebViewFetcher.ELEMENT_NOT_FOUND   ) {
            Assert.fail("Could not find HTML element for selector '" + selector + "'");
        }
        
        // create a webview wrapper which "fakes" the given touch region so we can "click"
        // on it with TouchUtils.clickView
        // TODO: should be synchronized access to mElementRect?
        WebViewRegion region = new WebViewRegion(mWebView, mElement);
        
        try {
            TouchUtils.clickView(mTestCase, region); // touch on the wrapper (which has the underlying webview)
        } catch(Exception e) {
            Assert.fail("Could not tap: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            Thread.sleep(WAIT_BREAK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void inputText(String selector, String text) {
        tapOnElement(selector);
        
        mTestCase.getInstrumentation().sendStringSync(text);
        
        try {
            Thread.sleep(WAIT_BREAK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getText(String selector) {
        return mFetcher.getText(selector);
    }

    public void closeSoftKeyboard() {
        InputMethodManager imm = 
                (InputMethodManager) mTestCase
                                        .getInstrumentation()
                                        .getTargetContext()
                                        .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mWebView.getWindowToken(), 0);
    }
    
    private RectF getViewFrameOnScreen(View view, WebViewFetcher fetcher, String selector) {
        PointF elementLocation = fetcher.getElementLocation(selector);
        
        try {
            mTestCase.runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.computeScroll();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        
        // TODO: should we use the real device element width?
        RectF elementFrame = new RectF(elementLocation.x, 
                                       elementLocation.y, 
                                       elementLocation.x + DEFAULT_WEBVIEW_ELEMENT_WIDTH, 
                                       elementLocation.y + DEFAULT_WEBVIEW_ELEMENT_HEIGHT);
        return elementFrame;
        
    }
    
}
