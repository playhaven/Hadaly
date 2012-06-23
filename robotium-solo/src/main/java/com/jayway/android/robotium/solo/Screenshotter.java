package com.jayway.android.robotium.solo;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.View;

/**
 * Takes a screenshot of the current device state. 
 * 
 * Status: In progress...
 * @author samstewart
 *
 */
public class Screenshotter {
	
	/**
	 * Grabs a screenshot and saves it in "/sdcard/Robotium-Screenshots/". 
	 * Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.
	 * @author Sam Stewart
	 */

	public void takeScreenshot(Activity activity) {
		final Activity staticAct = activity; // won't work with inner class otherwise
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				View rootView = staticAct.getWindow().getDecorView();
				
				if(rootView != null){
					rootView.destroyDrawingCache();
					rootView.buildDrawingCache(false);
					
					Bitmap b = rootView.getDrawingCache();
					
					FileOutputStream fos = null;
					SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy-hhmmss");
					
					String fileName = sdf.format( new Date()) + ".jpg";
					
					File directory = new File(Environment.getExternalStorageDirectory() + "/Robotium-Screenshots/");
					
					directory.mkdir();

					File fileToSave = new File(directory,fileName);
					try {
						fos = new FileOutputStream(fileToSave);
						
						if (b.compress(Bitmap.CompressFormat.JPEG, 100, fos) == false)
							Log.e(Hadaly.LOGGING_TAG, "Compress/Write failed");
						
						fos.flush();
						fos.close();
					} catch (Exception e) {
						Log.d(Hadaly.LOGGING_TAG, "Can't save the screenshot! Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.");
						e.printStackTrace();
						
					} finally {
						rootView.destroyDrawingCache();
					}
					
				}
			}

		});
	}
}
