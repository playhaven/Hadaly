package com.jayway.android.robotium.solo.actions;

import android.app.Activity;
import android.test.InstrumentationTestCase;
import android.view.View;

public class SendKeyCodeAction implements Action {

	private int mKeycode;
	
	public SendKeyCodeAction(int keycode) {
		mKeycode = keycode;
	}
	
	@Override
	public void setView(View view) {
		// pass
		
	}

	@Override
	public void doAction(Activity activity, InstrumentationTestCase testCase, View view) {
		
		testCase.getInstrumentation().sendKeyDownUpSync(mKeycode);
	}

}
