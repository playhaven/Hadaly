package com.samstewart.hadaly.actions;

import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import junit.framework.Assert;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.IntentFilter;

import com.samstewart.hadaly.Hadaly;

/**
 * Monitors the current activity and history stack.
 * Used by {@link Hadaly}.
 * We have to perform quite a bit of synchronization since the Timer runs
 * on a background thread.
 * TODO: add some assertions to avoid nulls, etc.?
 * @author samstewart
 *
 */
public class ActivityWatcher {
	
	private Stack<Activity> mActivityHistory; // TODO: might be potential memory leak?
	
	private ActivityMonitor mActivityMonitor;
	
	private Timer 			mActivityWatcherPump;
	
	private final int		ACTIVITY_REFRESH_INTERVAL = 50;
	
	private final int 		ACTIVITY_WAIT_TIMEOUT 	  = 3000;
	
	private TimerTask		mActivityRefresh = new TimerTask() {
		@Override
		public void run() {
			synchronized (mActivityMonitor) {
				
				synchronized (mActivityHistory) {					
					// what if any of our activities are finishing? Kill em'
					for (Activity activity : mActivityHistory) {
						if (activity.isFinishing()) {
							mActivityHistory.remove(activity);
							return;
						}
							
					}
					
					// running on a background thread remember!
					Activity lastActivity = mActivityMonitor.getLastActivity();
					
					if (lastActivity == null) return;
					
					if (activityAlreadyAdded(lastActivity)) return; // same as current activity

					// if none of the others, it must be a brand new activity, add it.
					mActivityHistory.push(lastActivity);
				}
			}
			

		}
	};
	
	public void tearDown() {
		mActivityHistory.clear();
		mActivityRefresh.cancel(); 
		mActivityRefresh = null;
		
		mActivityWatcherPump.cancel();
		mActivityWatcherPump = null;
		
				
	}
	private boolean activityAlreadyAdded(Activity activity) {
		return mActivityHistory.contains(activity); // check the entire stack
	}
	
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
	
	
	public synchronized Activity getCurrentActivity() {
		synchronized (mActivityHistory) {
			return (mActivityHistory.empty() ? null : mActivityHistory.peek());
		}
	}
	
	public synchronized Activity getInitialActivity() {
		if (mActivityHistory.size() > 0)
			return mActivityHistory.get(0);
		
		return null;
	}
	
	public synchronized Activity getRootActivity() {
		return getInitialActivity();
	}
	
	public synchronized Activity getActivity(int index) {
		return mActivityHistory.get(index);
	}
	
	public synchronized  int getTotalActivities() {
		return mActivityHistory.size();
	}
	
	public void waitForNewActivity() {
		// TODO: check to make sure we actually got a new activity (check hit count)
		Activity newActivity = mActivityMonitor.waitForActivityWithTimeout(ACTIVITY_WAIT_TIMEOUT);
		
		Assert.assertNotNull(newActivity);
		
		if (activityAlreadyAdded(newActivity)) return;
		
		synchronized (mActivityHistory) {
			mActivityHistory.push(newActivity);
		}
		
	}
	
	public void sleep(int time) {
		try {
			Thread.sleep(time);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	public synchronized void closeCurrentActivity() {
		synchronized (mActivityHistory) {
			if (mActivityHistory.empty()) return;;
			
			mActivityHistory.pop().finish();
			
			// sleep for a few seconds to let the system catch up
			sleep(600);
		}
	}
	
	public synchronized void closeAllActivities() {
		while ( ! mActivityHistory.empty()) {
			closeCurrentActivity();
		}
	}
	
	public synchronized void assertActivityShowing(Class<? extends Activity> activityClass) {
		synchronized (mActivityHistory) {
			Assert.assertEquals(activityClass, getCurrentActivity().getClass());
		}
		
	}
}
