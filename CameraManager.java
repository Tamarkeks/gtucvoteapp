package com.example.godspower.gtucvote.qr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.godspower.gtucvote.util.Util;

import java.io.IOException;

public final class CameraManager {

	private static final String TAG = CameraManager.class.getSimpleName();
	private static final int MIN_FRAME_WIDTH = 240;
	private static final int MIN_FRAME_HEIGHT = 240;
	private static final int MAX_FRAME_WIDTH = 940;
	private static final int MAX_FRAME_HEIGHT = 540;
	private final Context context;
	private final CameraConfigurationManager configManager;
	private Camera camera;
	private Rect framingRect;
	private Rect framingRectInPreview;
	private boolean initialized;
	private boolean previewing;
	private boolean reverseImage;
	private int requestedFramingRectWidth;
	private int requestedFramingRectHeight;
	private final PreviewCallback previewCallback;
	private final AutoFocusCallback autoFocusCallback;

	public CameraManager(Context context) {

		this.context = context;
		this.configManager = new CameraConfigurationManager(context);
		previewCallback = new PreviewCallback(configManager);
		autoFocusCallback = new AutoFocusCallback();
	}

	public void openDriver(SurfaceHolder holder, boolean turnOnFlash)
			throws IOException {

		Camera theCamera = camera;
		if (theCamera == null) {
			theCamera = Camera.open();

			if (theCamera == null) {
				throw new IOException();
			}
			camera = theCamera;
		}
		theCamera.setPreviewDisplay(holder);

		if (!initialized) {

			initialized = true;
			configManager.initFromCameraParameters(theCamera);

			if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {

				setManualFramingRect(requestedFramingRectWidth,
						requestedFramingRectHeight);
				requestedFramingRectWidth = 0;
				requestedFramingRectHeight = 0;
			}
		}
		configManager.setDesiredCameraParameters(theCamera);

		if (turnOnFlash) {
			configManager.setTorch(theCamera, true);
		}

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		reverseImage = prefs.getBoolean(PreferencesActivity.KEY_REVERSE_IMAGE,
				false);
	}

	public void closeDriver() {

		if (camera != null) {
			camera.release();
			camera = null;
			framingRect = null;
			framingRectInPreview = null;
		}
	}

	public void startPreview() {

		Camera theCamera = camera;
		if (theCamera != null && !previewing) {
			theCamera.startPreview();
			previewing = true;
		}
	}

	public void stopPreview() {

		if (camera != null && previewing) {
			camera.stopPreview();
			previewCallback.setHandler(null, 0);
			autoFocusCallback.setHandler(null, 0);
			previewing = false;
		}
	}

	public void requestPreviewFrame(Handler handler, int message) {

		Camera theCamera = camera;
		if (theCamera != null && previewing) {
			previewCallback.setHandler(handler, message);
			theCamera.setOneShotPreviewCallback(previewCallback);
		}
	}

	public void requestAutoFocus(Handler handler, int message) {

		if (camera != null && previewing) {
			autoFocusCallback.setHandler(handler, message);
			try {
				camera.autoFocus(autoFocusCallback);
			} catch (RuntimeException re) {
				if (Util.DEBUGGABLE) {
					Log.w(TAG, "Unexpected exception while focusing", re);
				}
			}
		}
	}

	public Rect getFramingRect() {

		if (framingRect == null) {
			if (camera == null) {
				return null;
			}
			Point screenResolution = configManager.getScreenResolution();
			int width = MIN_FRAME_WIDTH; int height = MIN_FRAME_HEIGHT;
			if (! Util.SpecialModels.contains(Util.getDeviceName())) {
				int tmp = 7 * screenResolution.x / 8;
				width = (tmp) < MIN_FRAME_WIDTH ? MIN_FRAME_WIDTH : (tmp);
				tmp = 1 * screenResolution.y / 3;
				height = (tmp) < MIN_FRAME_WIDTH ? MIN_FRAME_WIDTH : ((tmp) > MAX_FRAME_HEIGHT ?  MAX_FRAME_HEIGHT : (tmp));
			}else {

				width = findDesiredDimensionInRange(screenResolution.x,
						MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);

				height = findDesiredDimensionInRange(screenResolution.y,
						MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);
			}
			int leftOffset = (screenResolution.x - width) / 2;
			int topOffset = (screenResolution.y - height) / 2;

			framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
					topOffset + height);
			if (Util.DEBUGGABLE) {
				Log.d(TAG, "Calculated framing rect: " + framingRect);
			}
		}
		return framingRect;
	}

	private static int findDesiredDimensionInRange(int resolution, int hardMin,
			int hardMax) {
		int dim = resolution / 2; // Target 50% of each dimension
		if (dim < hardMin) {
			return hardMin;
		}
		if (dim > hardMax) {
			return hardMax;
		}
		return dim;

	}

	public Rect getFramingRectInPreview() {
		if (framingRectInPreview == null) {

			Rect framingRect = getFramingRect();
			if (framingRect == null) {
				return null;
			}

			Rect rect = new Rect(framingRect);
			Point cameraResolution = configManager.getCameraResolution();
			Point screenResolution = configManager.getScreenResolution();

			if (! Util.SpecialModels.contains(Util.getDeviceName())) {
				rect.left = rect.left * cameraResolution.y / screenResolution.x;
				rect.right = rect.right * cameraResolution.y / screenResolution.x;
				rect.top = rect.top * cameraResolution.x / screenResolution.y;
				rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;
			}else{
				rect.left = rect.left * cameraResolution.x / screenResolution.x;
				rect.right = rect.right * cameraResolution.x / screenResolution.x;
				rect.top = rect.top * cameraResolution.y / screenResolution.y;
				rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
			}

			int beforeL = rect.left;
			int beforeR = rect.right;
			int diffLR = rect.right - rect.left;
			int diffTB = rect.bottom - rect.top;
			int difference = diffTB - diffLR;

			if (difference > 80) {
				int extraSpace = difference / 2 - 50;
				rect.left = rect.left - extraSpace;
				rect.right = rect.right + extraSpace;
				if (rect.left < 0)
					rect.left = beforeL;
				if (rect.right > screenResolution.x)
					rect.right = beforeR;
			}

			framingRectInPreview = rect;
		}
		return framingRectInPreview;
	}

	public void setManualFramingRect(int width, int height) {
		if (initialized) {

			Point screenResolution = configManager.getScreenResolution();
			if (width > screenResolution.x) {
				width = screenResolution.x;
			}
			if (height > screenResolution.y) {
				height = screenResolution.y;
			}
			int leftOffset = (screenResolution.x - width) / 2;
			int topOffset = (screenResolution.y - height) / 2;
			framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
					topOffset + height);
			if (Util.DEBUGGABLE) {
				Log.d(TAG, "Calculated manual framing rect: " + framingRect);
			}
			framingRectInPreview = null;
		} else {
			requestedFramingRectWidth = width;
			requestedFramingRectHeight = height;
		}
	}

	public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data,
			int width, int height) {

		Rect rect = getFramingRectInPreview();
		if (rect == null) {
			return null;
		}
		return new PlanarYUVLuminanceSource(data, width, height, rect.left,
				rect.top, rect.width(), rect.height(), reverseImage);
	}

	@SuppressLint("LongLogTag")
	public void turnOnFlash(boolean turnOn) {

		if (camera != null) {
			try {
				if (turnOn) {
					Parameters p = camera.getParameters();
					p.setFlashMode(Parameters.FLASH_MODE_TORCH);
					camera.setParameters(p);
					stopPreview();
					startPreview();
				} else {
					camera.stopPreview();
					Parameters p = camera.getParameters();
					p.setFlashMode(Parameters.FLASH_MODE_OFF);
					camera.setParameters(p);
					stopPreview();
					startPreview();
				}
			} catch (Exception e) {
				if (Util.DEBUGGABLE) {
					Log.e("Error initiating or closing camera flash",
							"" + e.getMessage());
				}
			}
		}
	}
}