package com.example.godspower.gtucvote.qr;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.example.godspower.gtucvote.util.Util;

import java.util.Collection;


final class CameraConfigurationManager {

	private static final String TAG = "CameraConfiguration";
	private static final int MIN_PREVIEW_PIXELS = 320 * 240; // small screen
	private final Context context;
	private Point screenResolution;
	private Point cameraResolution;

	CameraConfigurationManager(Context context) {
		this.context = context;
	}

	void initFromCameraParameters(Camera camera) {

		Camera.Parameters parameters = camera.getParameters();
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		int width = 0;
		int height = 0;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			Point size = new Point();
			manager.getDefaultDisplay().getSize(size);
			width = size.x;
			height = size.y;
		} else {
			Display d = manager.getDefaultDisplay();
			width = d.getWidth();
			height = d.getHeight();
		}

		if (width < height) {
			int temp = width;
			width = height;
			height = temp;
		}

		screenResolution = new Point(width, height);

		if (Util.DEBUGGABLE) {
			Log.i(TAG, "Screen resolution: " + screenResolution);
		}

		cameraResolution = findBestPreviewSizeValue(parameters,
				screenResolution, false);

		if (Util.DEBUGGABLE) {
			Log.i(TAG, "Camera resolution: " + cameraResolution);
		}
	}

	void setDesiredCameraParameters(Camera camera) {

		Camera.Parameters parameters = camera.getParameters();
		if (! Util.SpecialModels.contains(Util.getDeviceName())) {
			camera.setDisplayOrientation(90);
		}

		if (parameters == null) {
			if (Util.DEBUGGABLE) {
				Log.w(TAG,
						"Device error: no camera parameters are available. Proceeding without configuration.");
			}
			return;
		}

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		initializeTorch(parameters, prefs);

		String focusMode = findSettableValue(
				parameters.getSupportedFocusModes(),
				Camera.Parameters.FOCUS_MODE_AUTO,
				Camera.Parameters.FOCUS_MODE_MACRO);

		if (focusMode != null) {
			parameters.setFocusMode(focusMode);
		}
		parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
		camera.setParameters(parameters);

	}

	Point getCameraResolution() {
		return cameraResolution;
	}

	Point getScreenResolution() {
		return screenResolution;
	}

	void setTorch(Camera camera, boolean newSetting) {
		Camera.Parameters parameters = camera.getParameters();

		doSetTorch(parameters, newSetting);
		camera.setParameters(parameters);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		boolean currentSetting = prefs.getBoolean(
				PreferencesActivity.KEY_FRONT_LIGHT, false);

		if (currentSetting != newSetting) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(PreferencesActivity.KEY_FRONT_LIGHT, newSetting);
			editor.commit();
		}
	}

	private static void initializeTorch(Camera.Parameters parameters,
			SharedPreferences prefs) {
		doSetTorch(parameters, false);
	}

	private static void doSetTorch(Camera.Parameters parameters,
			boolean newSetting) {
		String flashMode;

		if (newSetting) {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(),
					Camera.Parameters.FLASH_MODE_TORCH,
					Camera.Parameters.FLASH_MODE_ON);
		} else {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(),
					Camera.Parameters.FLASH_MODE_OFF);
		}
		if (flashMode != null) {
			parameters.setFlashMode(flashMode);
		}
	}

	private static Point findBestPreviewSizeValue(Camera.Parameters parameters,
			Point screenResolution, boolean portrait) {

		Point bestSize = null;

		double diff = Double.MAX_VALUE;

		for (Camera.Size supportedPreviewSize : parameters
				.getSupportedPreviewSizes()) {
			int pixels = supportedPreviewSize.height
					* supportedPreviewSize.width;

			if (pixels < MIN_PREVIEW_PIXELS || pixels > screenResolution.x * screenResolution.y * 1.20) {
				continue;
			}

			double supportedWidth = portrait ? supportedPreviewSize.height
					: supportedPreviewSize.width;
			double supportedHeight = portrait ? supportedPreviewSize.width
					: supportedPreviewSize.height;
			double newDiff = Math.abs(screenResolution.y / supportedHeight
					- screenResolution.x / supportedWidth);

			if (newDiff == 0) {
				bestSize = new Point((int)supportedWidth,(int) supportedHeight);
				break;
			}

			if (newDiff < diff) {
				bestSize = new Point((int)supportedWidth, (int)supportedHeight);
				diff = newDiff;
			}
		}

		if (bestSize == null) {
			Camera.Size defaultSize = parameters.getPreviewSize();
			bestSize = new Point(defaultSize.width, defaultSize.height);
		}
		return bestSize;
	}

	private static String findSettableValue(Collection<String> supportedValues,
			String... desiredValues) {

		if (Util.DEBUGGABLE) {
			Log.i(TAG, "Supported values: " + supportedValues);
		}
		String result = null;
		if (supportedValues != null) {
			for (String desiredValue : desiredValues) {
				if (supportedValues.contains(desiredValue)) {
					result = desiredValue;
					break;
				}
			}
		}

		if (Util.DEBUGGABLE) {
			Log.i(TAG, "Settable value: " + result);
		}
		return result;
	}
}