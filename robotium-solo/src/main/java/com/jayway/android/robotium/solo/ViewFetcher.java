package com.jayway.android.robotium.solo;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
	
	
	public void setActivity(Activity activity) {
		mActivity = new WeakReference<Activity>(activity);
	}
	
	public Activity getActivity() {
		return mActivity.get();
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
		// TODO: should we check to make sure it's shown?
		
		// breadth first search
		ConcurrentLinkedQueue<View> viewChildren = new ConcurrentLinkedQueue<View>();
		
		View curView = null;
		View matchedView = null;
		View rootView = getRootView();
		viewChildren.add(rootView);
		
		Selector curSelector = selector; 
		
		while (viewChildren.size() > 0) {
			curView = viewChildren.poll(); // grab the current view we're examining
			
			// does the current view match the selector?
			if (matchViewSelector(curView, curSelector)) {
				
				if (curSelector.hasChild())
					curSelector = curSelector.getChild();
				else {
					matchedView = curView; // we've found a match
					break;
				}
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
		
		return matchedView;
	}
	
	public boolean testMatchViewSelector(View view, Selector selector) {
		return matchViewSelector(view, selector);
	}
	
	/**
	 * Matches a single element selector against a view. We always make sure to check
	 * via a "fail first" policy so that we can exit quickly.
	 * TODO: should we be using "findViewById"?
	 * @param view The view to match against
	 * @param selector The selector we are checking for a match
	 * @return
	 */
	private boolean matchViewSelector(View view, Selector selector) {
		// check the following:
		// class
		// text (if it exists)
		// tag
		// integer ID
		// accessibility description
		
		if (selector.getClasses().size() != 0)
			if ( ! selector.getClasses().contains(view.getClass().getSimpleName()))
				return false;
			
		if (selector.getIntegerID() != -1) // if we've specified an ID, check to make sure view matches
			
			if (view.getId() != selector.getIntegerID()) 
				return false;
		
		if (selector.hasAttribute(Selector.Attribute.Tag.toString())) // if we've specified a tag, check to make sure view matches
			
			if ( ! selector.getAttribute(Selector.Attribute.Tag.toString())
								.equals(view.getTag())) 
				return false;
		
		if (selector.hasAttribute(Selector.Attribute.ContentDescription.toString())) // if selector filters by "contentDescription", check view matches
			
			if ( ! selector.getAttribute(Selector.Attribute.ContentDescription.toString())
										.equalsIgnoreCase(
													(view.getContentDescription() != null    ?
													 view.getContentDescription().toString() :
													 null)
													)) 
				return false;
		
		if (selector.hasAttribute(Selector.Attribute.Text.toString())) // if we filter by text, make sure the view matches
			
			if (view instanceof TextView)
				if ( ! selector.getAttribute(Selector.Attribute.Text.toString())
									.equalsIgnoreCase(
												((TextView)view).getText().toString()))
					return false;
		
		// TODO: check for *all* attributes via reflection
		
		return true;
	}
	
	public View getRootView() {
		if (mActivity 	  		 == null ||
			mActivity.get()      == null) return null;
		
		return mActivity.get().getWindow().getDecorView();
	}
	
	

}