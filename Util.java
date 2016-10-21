package com.example.godspower.gtucvote.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.godspower.gtucvote.ErrorActivity;
import com.example.godspower.gtucvote.R;
import com.example.godspower.gtucvote.dialog.LoadingSpinner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilities.
 * 
 * @version 16.05.2013
 */
public class Util {

	public final static String QR_CODE = "ee.vvk.ivotingverification.QR_CODE";
	public final static String WEB_RESULT = "ee.vvk.ivotingverification.WEB_RESULT";
	public final static String VERSION_NUMBER = "ee.vvk.ivotingverification.VERSION_NUMBER";
	public final static String ERROR_MESSAGE = "ee.vvk.ivotingverification.ERROR_MESSAGE";
	public final static String NETWORK_STATUS = "ee.vvk.ivotingverification.NETWORK_STATUS";
	public final static String EXTRA_MESSAGE = "ee.vvk.ivotingverification.MESSAGE";
	public final static String EXIT = "ee.vvk.ivotingverification.EXIT";
	public final static String RESULT = "ee.vvk.ivotingverification.RESULT";
	public final static String POST_REQUEST_METHOD = "POST";
	public final static String GET_REQUEST_METHOD = "GET";
	public final static String TLS_PROTOCOL = "TLS";
	public final static String ENCODING = "UTF-8";
	public final static String VERIFY_PARAMETER = "verify";

	public final static int TIMEOUT = 10 * 1000;
	public final static long VIBRATE_DURATION = 350L;

	public static boolean DEBUGGABLE = false;
	public static boolean CONFIGURABLE = false;

	// Models where camera can't be rotated to portrait
	public static Set<String> SpecialModels = new HashSet<String>(Arrays.asList("Samsung GT-S6102", "Samsung GT-S5360",
			"Samsung GT-S5660", "Samsung YP-G1", "Samsung YP-G70"));

	public static KeyStore loadTrustStore(final Activity currentActivity) {

		try {
			KeyStore localTrustStore = KeyStore.getInstance("BKS");
			InputStream in;
			if(C.fromPro){
				in = new FileInputStream(new File(C.trustStoreURL + "/mytruststoresConfig.bks"));
			}else{
				in = currentActivity.getResources().openRawResource(
						R.raw.mytruststore);
			}
			try {
				localTrustStore.load(in, C.trustStorePass.toCharArray());
			} catch (NoSuchAlgorithmException e) {
				Util.startErrorIntent((Activity) currentActivity,
						C.badServerResponseMessage, true);
			} catch (IOException e) {
				Util.startErrorIntent((Activity) currentActivity,
						C.badServerResponseMessage, true);
			} catch (CertificateException e) {
				Util.startErrorIntent((Activity) currentActivity,
						C.badServerResponseMessage, true);
			} finally {
				in.close();
			}
			return localTrustStore;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String readLines(InputStream in, String encoding)
			throws IOException {
		try {
			StringBuffer buff = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					in, encoding != null ? encoding : "UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				buff.append(line + "\n");
			}
			return buff.toString();
		} finally {
			in.close();
		}
	}

	public static LoadingSpinner startSpinner(Activity currentActivity,
											  boolean isWhite) {
		LoadingSpinner mLoadingSpinner = new LoadingSpinner(currentActivity,
				isWhite);
		if (!mLoadingSpinner.isShowing()) {
			mLoadingSpinner.show();
		}

		return mLoadingSpinner;
	}

	public static void stopSpinner(LoadingSpinner mLoadingSpinner) {
		if (mLoadingSpinner != null && mLoadingSpinner.isShowing()) {
			mLoadingSpinner.dismiss();
		}
	}

	public static void startErrorIntent(Activity currentActivity,
			String error_msg, boolean networkStatus) {
		Intent error_intent = new Intent(currentActivity, ErrorActivity.class);
		error_intent.putExtra(Util.ERROR_MESSAGE, error_msg);
		error_intent.putExtra(Util.NETWORK_STATUS, networkStatus);
		currentActivity.startActivity(error_intent);
		currentActivity.finish();

		if (Util.DEBUGGABLE) {
			Log.e("Error intent", currentActivity.getClass().getSimpleName());
		}
	}

	public static int generateHexColorValue(String color) {
		int hexColor;
		try {
			hexColor = Color.parseColor(color);
		} catch (Exception e) {
			if (Util.DEBUGGABLE) {
				Log.d("Util", "Color wrong format");
			}
			hexColor = Color.parseColor("#FFFFFF");
		}
		return hexColor;
	}


	public static float convertPixelsToDp(float px, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px * (metrics.densityDpi / 160f);
		return dp;
	}

	public static String readRawTextFile(Context context, int fileName) {
		InputStream inputStream = context.getResources().openRawResource(
				fileName);

		InputStreamReader inputReader = new InputStreamReader(inputStream);
		BufferedReader buffReader = new BufferedReader(inputReader);
		String line;
		StringBuilder text = new StringBuilder();

		try {
			while ((line = buffReader.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
		} catch (IOException e) {
			return null;
		}
		return text.toString();
	}

	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	public static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

}