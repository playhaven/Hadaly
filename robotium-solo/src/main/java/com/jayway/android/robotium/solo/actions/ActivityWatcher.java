package com.jayway.android.robotium.solo.actions;

import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import junit.framework.Assert;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.IntentFilter;

import com.jayway.android.robotium.solo.Hadaly;

/**
 * Monitors the current activity and history stack.
 * Used by {@link Hadaly}.
 * TODO: add some assertions to avoid nulls, etc.?
 * @author samstewart
 *
 */
public class ActivityWatcher {
	
	private Stack<Activity> mActivityHistory;
	
	private ActivityMonitor mActivityMonitor;
	
	private Timer 			mActivityWatcherPump;
	
	private final int		ACTIVITY_REFRESH_INTERVAL = 50;
	
	private TimerTask		mActivityRefresh = new TimerTask() {
		@Override
		public void run() {
			Activity lastActivity = mActivityMonitor.getLastActivity();
			
			if (lastActivity == null) return;
			
			if (lastActivity.equals(mActivityHistory.peek())) return; // same as current activity
			
			// if the activity is closing, get rid of it
			if (lastActivity.isFinishing()) {
				mActivityHistory.remove(lastActivity);
				return;
			}
			
			// if none of the others, it must be a brand new activity, add it.
			mActivityHistory.push(lastActivity);
			
		}
	};
	
	public ActivityWatcher(Instrumentation instr, Activity rootActivity) {
		mActivityHistory = new Stack<Activity>();
		mActivityHistory.push(rootActivity);
		
		// Note: you can filter by whatever you wish
		IntentFilter filter = null;
		mActivityMonitor = instr.addMonitor(filter, null, false);
		
		// start listening for new activities
		mActivityWatcherPump = new Timer();
		mActivityWatcherPump.schedule(mActivityRefresh, 0, ACTIVITY_REFRESH_INTERVAL);
	}
	
	
	public Activity getCurrentActivity() {
		return mActivityHistory.peek();
	}
	
	public Activity getInitialActivity() {
		if (mActivityHistory.size() > 0)
			return mActivityHistory.get(0);
		
		return null;
	}
	
	public Activity getRootActivity() {
		return getInitialActivity();
	}
	
	public Activity getActivity(int index) {
		return mActivityHistory.get(index);
	}
	
	public int getTotalActivities() {
		return mActivityHistory.size();
	}
	
	public void waitForNewActivity() {
		// TODO: wait for the next activity properly (we just sleep now)
		// TODO: also make assertions
		sleep(2000);
	}
	
	public void sleep(int time) {
		try {
			Thread.sleep(time);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	public void closeCurrentActivity() {
		if (mActivityHistory.peek() == null) return;
		
		mActivityHistory.pop().finish();
	}
	
	public void closeAllActivities() {
		while (mActivityHistory.peek() != null) {
			mActivityHistory.pop().finish();
		}
	}
	
	public void assertActivityShowing(Class<? extends Activity> activityClass) {
		Assert.assertEquals(getCurrentActivity().getClass(), activityClass);
	}
}
