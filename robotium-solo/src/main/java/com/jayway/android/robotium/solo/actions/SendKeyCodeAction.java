package com.jayway.android.robotium.solo.actions;

import android.app.Activity;
import android.app.Instrumentation;
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
	public void doAction(Activity activity, Instrumentation inst, View view) {
		// inst.sendCharacterSync(keycode);
		
	}

}
