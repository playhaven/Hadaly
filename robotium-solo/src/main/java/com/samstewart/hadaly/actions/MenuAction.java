package com.samstewart.hadaly.actions;

import android.app.Activity;
import android.test.InstrumentationTestCase;
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
	public void doAction(Activity activity, InstrumentationTestCase testCase, View view) {
		// TODO: show the menu then press the item matching the selector
		
	}
	
	
}
