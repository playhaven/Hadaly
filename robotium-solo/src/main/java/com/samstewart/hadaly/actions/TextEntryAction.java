package com.samstewart.hadaly.actions;

import junit.framework.Assert;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
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
	public void doAction(Activity activity, InstrumentationTestCase testCase, View view) {
		Assert.assertTrue(view instanceof EditText);
		
		// make final so we can access within closure
		final EditText editTextF = (EditText)view;
		
		// TODO: waitForIdleSync()?
		// TODO: startActivitySync()?
		// TODO: actually give edit text focus
		TouchUtils.clickView(testCase, editTextF);
		
		testCase.getInstrumentation().runOnMainSync(new Runnable() {
			public void run() {
				//TODO: editTextF.setInputType(InputType.TYPE_NULL);
				editTextF.setText(""); // clear the field
			}
		});
		testCase.getInstrumentation().waitForIdleSync();
		
		// should send the text as a series of key events?
		testCase.getInstrumentation().sendStringSync(mText);
		
		closeSoftKeyboard(editTextF, testCase.getInstrumentation());
		
	}
	
	@Override
	public void setView(View view) {
		// Pass
	}
	
	private void closeSoftKeyboard(EditText editText, Instrumentation inst) {
		InputMethodManager imm = (InputMethodManager)inst.getTargetContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
}
