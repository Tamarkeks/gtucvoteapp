package com.example.godspower.gtucvote.qr;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.godspower.gtucvote.QRScannerActivity;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.example.godspower.gtucvote.R;
import com.example.godspower.gtucvote.util.Util;

import java.util.Map;

final class DecodeHandler extends Handler {

	private static final String TAG = DecodeHandler.class.getSimpleName();

	private final QRScannerActivity activity;
	private final MultiFormatReader multiFormatReader;
	private boolean running = true;

	DecodeHandler(QRScannerActivity activity, Map<DecodeHintType, Object> hints) {
		multiFormatReader = new MultiFormatReader();
		multiFormatReader.setHints(hints);
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message message) {
		if (!running) {
			return;
		}

		if (message.what == R.id.decode) {
			decode((byte[]) message.obj, message.arg1, message.arg2);
		} else if (message.what == R.id.quit) {
			running = false;
			Looper.myLooper().quit();
		}
	}

	private void decode(byte[] data, int width, int height) {
		long start = System.currentTimeMillis();
		Result rawResult = null;
		PlanarYUVLuminanceSource source;

		if (! Util.SpecialModels.contains(Util.getDeviceName())){
			byte[] rotatedData = new byte[data.length];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++)
					rotatedData[x * height + height - y - 1] = data[x + y * width];
			}
			data = rotatedData;
			int tmp = width;
			width = height;
			height = tmp;

		}
		source = activity.getCameraManager().buildLuminanceSource(data,
				width, height);
		if (source != null) {
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			try {
				rawResult = multiFormatReader.decodeWithState(bitmap);
			} catch (ReaderException re) {
			} finally {
				multiFormatReader.reset();
			}
		}

		Handler handler;
		handler = activity.getHandler();
		if (rawResult != null) {
			long end = System.currentTimeMillis();
			if (Util.DEBUGGABLE) {
				Log.d(TAG, "Found barcode in " + (end - start) + " ms");
			}
			if (handler != null) {
				Message message = Message.obtain(handler,
						R.id.decode_succeeded, rawResult);
				Bundle bundle = new Bundle();
				bundle.putParcelable(DecodeThread.BARCODE_BITMAP,
						source.renderCroppedGreyscaleBitmap());
				message.setData(bundle);
				message.sendToTarget();
			}
		} else {
			if (handler != null) {
				Message message = Message.obtain(handler, R.id.decode_failed);
				message.sendToTarget();
			}
		}
	}
}
