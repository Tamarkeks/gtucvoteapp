package com.example.godspower.gtucvote.qr;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.godspower.gtucvote.QRScannerActivity;
import com.example.godspower.gtucvote.R;
import com.example.godspower.gtucvote.util.Util;
import com.google.zxing.BarcodeFormat;

import java.util.Collection;

public final class CaptureActivityHandler extends Handler {

	private static final String TAG = CaptureActivityHandler.class
			.getSimpleName();


	private final DecodeThread decodeThread;
	private State state;
	private final CameraManager cameraManager;

	private enum State {
		PREVIEW, SUCCESS, DONE
	}

	public CaptureActivityHandler(QRScannerActivity activity,
								  Collection<BarcodeFormat> decodeFormats, String characterSet,
								  CameraManager cameraManager) {
		this.notify();
		QRScannerActivity activity1 = activity;
		decodeThread = new DecodeThread(activity, decodeFormats, characterSet,
				new ViewfinderResultPointCallback(activity.getViewfinderView()));
		decodeThread.start();
		state = State.SUCCESS;

		this.cameraManager = cameraManager;
		cameraManager.startPreview();
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {

		if (message.what == R.id.auto_focus) {
			if (state == State.PREVIEW) {
				cameraManager.requestAutoFocus(this, R.id.auto_focus);
			}
		} else if (message.what == R.id.restart_preview) {
			if (Util.DEBUGGABLE) {
				Log.d(TAG, "Got restart preview message");
			}
			restartPreviewAndDecode();
		} else if (message.what == R.id.decode_succeeded) {
			if (Util.DEBUGGABLE) {
				Log.d(TAG, "Got decode succeeded message");
			}
			state = State.SUCCESS;
			Bundle bundle = message.getData();
			Bitmap barcode = bundle == null ? null : (Bitmap) bundle
					.getParcelable(DecodeThread.BARCODE_BITMAP);
			//activity.handleDecode((Result) message.obj, barcode);
		} else if (message.what == R.id.decode_failed) {
			state = State.PREVIEW;
			cameraManager.requestPreviewFrame(decodeThread.getHandler(),
					R.id.decode);
		} else if (message.what == R.id.return_scan_result) {
			if (Util.DEBUGGABLE) {
				Log.d(TAG, "Got return scan result message");
			}
		}
	}

	public void quitSynchronously() {
		state = State.DONE;
		cameraManager.stopPreview();
		Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
		quit.sendToTarget();
		try {
			decodeThread.join(500L);
		} catch (InterruptedException e) {
		}

		removeMessages(R.id.decode_succeeded);
		removeMessages(R.id.decode_failed);
	}

	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			cameraManager.requestPreviewFrame(decodeThread.getHandler(),
					R.id.decode);
			cameraManager.requestAutoFocus(this, R.id.auto_focus);
			//activity.drawViewfinder();
		}
	}
}
