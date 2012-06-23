package com.jayway.android.robotium.solo;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Activity;
import android.graphics.PointF;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Main class for fetching views from the activity. 
 *
 * Searches via {@link Selector}
 * @author Renas Reda, renas.reda@jayway.com
 *
 */

class ViewFetcher {	
	
	private WeakReference<Activity> mActivity;
		
	public ViewFetcher(Activity activity) {
		mActivity = new WeakReference<Activity>(activity);
	}
	
	/**
	 * Performs breadth-first traversal to attempt to match the selector and return the appropriate view
	 * @param selector The css like selector for traversing the view hierarchy
	 * @return The view matching the selector or null
	 */
	public View getView(Selector selector) {
		if (mActivity == null || mActivity.get() == null) return null;
		
		// Tips and Tricks
		// isAssignableFrom
		// getDrawingTime
		// getLocationOnScreen
		// view.isShown()
		
		// breadth first search
		ConcurrentLinkedQueue<View> viewChildren = new ConcurrentLinkedQueue<View>();
		
		View curView = null;
		viewChildren.add(getRootView());
		
		Selector curSelector = selector; 
		
		while (viewChildren.size() > 0) {
			curView = viewChildren.poll(); // grab the current view we're examining
			
			// does the current view match the selector?
			if (matchViewSelector(curView, curSelector)) {
				
				if (curSelector.hasChild())
					curSelector = curSelector.getChild();
				else
					break; // we've found a match, stop the loop
			}
			
			if (curView instanceof ViewGroup) {
				// enqueue children
				ViewGroup curViewGroup = (ViewGroup)curView;
				
				for (int i = 0; i < curViewGroup.getChildCount(); i++)  {
					// we "look ahead" to see if any children match.
					// if a child matches we restrict our search to that particular
					// subtree.
					
					View child = curViewGroup.getChildAt(i);
					
					if (matchViewSelector(child, selector)) {
						viewChildren.clear(); // ignore all previous nodes at one level up (potentially)

						viewChildren.add(child);
						break; // skip adding the other nodes
					}
					
					viewChildren.add(child); // otherwise just add normally to breadth first
					
				}
			}
			
		}
		
		return curView;
	}
	
	
	private boolean matchViewSelector(View view, Selector selector) {
		// TODO: actually match each element against the view selector
		// TODO: check hint, check description, check tag, check text, check int id, check accessibility label
		return false;
	}
	
	public View getRootView() {
		if (mActivity 	  		 == null ||
			mActivity.get()      == null) return null;
		
		return mActivity.get().getWindow().getDecorView();
	}
	
	

}