package com.jayway.android.robotium.solo.actions;

import android.app.Activity;
import android.app.Instrumentation;
import android.view.View;

/**
 * The interface for any action which acts on a view.
 * Examples might include tap actions, drag actions, etc.
 * The user can implement this class to define their own custom actions.
 * 
 * We never return anything from doAction since the asserts are done
 * within the class. While it might be better to have the enclosing 
 * class do the assertions, it's easier to do them in the actual action.
 * @author samstewart
 *
 */
public interface Action {
	public void setView(View view);
	public void doAction(Activity activity, Instrumentation inst, View view);
}
