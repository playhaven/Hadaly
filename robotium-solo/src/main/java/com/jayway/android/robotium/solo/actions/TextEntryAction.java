package com.jayway.android.robotium.solo.actions;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Simple action to enter text into a textview.
 * @author samstewart
 *
 */
public class TextEntryAction implements Action {
	
	private String mText;
	
	public TextEntryAction(String text) {
		mText = text;
	}
	
	@Override
	public void doAction(Activity activity, Instrumentation inst, View view) {

		// make final so we can access within closure
		final EditText editTextF = editText;
		final String   textF 	 = text;
		inst.runOnMainSync(new Runnable() {
			public void run()
			{
				editTextF.setInputType(InputType.TYPE_NULL); 
				editTextF.performClick();
				

//				editTextF.setText(textF);
				inst.sendStringSync(textF);
				
				closeSoftKeyboard(editTextF);
			}
		}); 
	}
	
	@Override
	public void setView(View view) {
		// Pass
	}
	
	@SuppressWarnings("static-access")
	private void closeSoftKeyboard(EditText editText) {
		InputMethodManager imm = (InputMethodManager)mInstr.getTargetContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
}
