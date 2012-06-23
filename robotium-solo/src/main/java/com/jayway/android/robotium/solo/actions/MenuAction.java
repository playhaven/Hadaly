package com.jayway.android.robotium.solo.actions;

import android.app.Activity;
import android.app.Instrumentation;
import android.view.View;

public class MenuAction implements Action {
	private String mTitle;
	
	public MenuAction(String title) {
		mTitle = title;
	}

	@Override
	public void setView(View view) {
		// pass
		
	}

	@Override
	public void doAction(Activity activity, Instrumentation inst, View view) {
		// TODO: show the menu then press the item matching the selector
		
	}
	
	
}
