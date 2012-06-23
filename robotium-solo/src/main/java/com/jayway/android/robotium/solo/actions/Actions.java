package com.jayway.android.robotium.solo.actions;

/**
 * Simple utility class for creating various actions
 * @author samstewart
 *
 */
public class Actions {
	public static TapAction tapAction() {
		return new TapAction();
	}
	
	public static TextEntryAction enterTextAction(String text) {
		return new TextEntryAction(text);
	}
	
	public static SendKeyCodeAction sendKeycodeAction(int keycode) {
		return new SendKeyCodeAction(keycode);
	}
}
