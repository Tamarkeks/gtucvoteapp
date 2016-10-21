package com.example.godspower.gtucvote;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.godspower.gtucvote.dialog.LoadingSpinner;
import com.example.godspower.gtucvote.qr.CameraManager;
import com.example.godspower.gtucvote.qr.CaptureActivityHandler;
import com.example.godspower.gtucvote.qr.InactivityTimer;
import com.example.godspower.gtucvote.util.C;
import com.example.godspower.gtucvote.util.HttpRequest;
import com.example.godspower.gtucvote.util.JSONParser;
import com.example.godspower.gtucvote.util.RegexMatcher;
import com.example.godspower.gtucvote.util.Util;

import org.apache.http.HttpResponse;

import java.io.File;
import java.io.IOException;

/**
 * Main activity. The first screen.
 */
public class MainActivity extends Activity implements SurfaceHolder.Callback {

	private static final String TAG = MainActivity.class.getSimpleName();

	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private boolean hasSurface;
	private InactivityTimer inactivityTimer;
	private SurfaceView surfaceView;
	private LoadingSpinner mLoadingSpinner;

	private Button buttonMore;
	private Button buttonNext;

	private HttpResponse response;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null && Util.CONFIGURABLE) {
			Bundle extras = getIntent().getExtras();
			if (extras != null && extras.size() == 2) {
				C.configURL = extras.getString("configURL");
				C.trustStoreURL = extras.getString("trustStoreURL");
				C.fromPro = true;
			}
		}
		if (!C.fromPro) {
			C.configURL = Util.readRawTextFile(this.getApplicationContext(),
					R.raw.config).trim();
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Util.DEBUGGABLE = (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));

		if (getIntent().getBooleanExtra(Util.EXIT, false)) {
			finish();
		}

		hasSurface = false;
		if (this != null)
			inactivityTimer = new InactivityTimer(this);

		setContentView(R.layout.activity_main);

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.target_window);
		GradientDrawable bgShape = (GradientDrawable) linearLayout
				.getBackground();
		bgShape.setColor(Color.GRAY);

		linearLayout = (LinearLayout) findViewById(R.id.target_window_error_shadow);
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			urlConnect();
		} else {
			Util.startErrorIntent(MainActivity.this, C.noNetworkMessage, false);
		}

		LinearLayout frameBg = (LinearLayout) findViewById(R.id.frame_bg);
		frameBg.setBackgroundColor(Util
				.generateHexColorValue(C.frameBackground));

		buttonNext = (Button) findViewById(R.id.btn_next);
		buttonNext.setText(C.btnNext);
		buttonNext.setTextColor(Util.generateHexColorValue(C.btnForeground));
		GradientDrawable bgNextShape = (GradientDrawable) buttonNext
				.getBackground();
		bgNextShape.setColor(Util.generateHexColorValue(C.btnBackground));

		buttonNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clickNextButton(v);
			}
		});

		buttonMore = (Button) findViewById(R.id.btn_more);
		buttonMore.setText(C.btnMore);
		buttonMore.setTextColor(Util.generateHexColorValue(C.btnForeground));
		GradientDrawable bgMoreShape = (GradientDrawable) buttonMore
				.getBackground();
		bgMoreShape.setColor(Util.generateHexColorValue(C.btnBackground));

		buttonMore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clickMoreButton(v);
			}
		});
	}

	public Handler getHandler() {
		return handler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	@Override
	public void onResume() {
		super.onResume();
		cameraManager = new CameraManager(this);

		if (cameraManager == null) {
			Intent i = this.getIntent();
			this.finish();
			startActivity(i);
			return;
		}

		handler = null;

		surfaceView = (SurfaceView) this.findViewById(R.id.surface);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder, false);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		inactivityTimer.onResume();
	}

	public void clickNextButton(View view) {
		Intent QRCodeDecoder = new Intent(this, VoteActivity.class);
		startActivityForResult(QRCodeDecoder, 1);
	}

	public void clickMoreButton(View view) {
		Intent helpActivity = new Intent(this, HelpActivity.class);
		startActivity(helpActivity);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra(Util.RESULT);
				if (Util.DEBUGGABLE) {
					Log.d(TAG, contents);
				}
				try {
					if (contents != null && RegexMatcher.isCorrectQR(contents)) {
						startVoteDownloadActivity(contents);
					} else {
						Util.startErrorIntent(MainActivity.this,
								C.problemQrCodeMessage, true);
					}
				} catch (Exception e) {
					Util.startErrorIntent(MainActivity.this,
							C.problemQrCodeMessage, true);
				}
			}
			if (resultCode == RESULT_CANCELED) {
				finish();
				Intent intentMain = new Intent(getApplicationContext(),
						MainActivity.class);
				startActivity(intentMain);
			}
		}
	}

	private void startVoteDownloadActivity(String contents) {
		Intent next_intent = new Intent(this, VoteDownloadActivity.class);
		next_intent.putExtra(Util.EXTRA_MESSAGE, contents);
		startActivity(next_intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		C.fromPro = false;
		Util.stopSpinner(mLoadingSpinner);
		try {
			trimCache(this);
		} catch (Exception e) {
		}
	}

	public static void trimCache(Context context) {
		try {
			File dir = context.getCacheDir();
			if (dir != null && dir.isDirectory()) {
				deleteDir(dir);
			}
		} catch (Exception e) {
		}
	}

	public static boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	@Override
	public void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) this
					.findViewById(R.id.surface);
			if (surfaceView != null) {
				SurfaceHolder surfaceHolder = surfaceView.getHolder();
				surfaceHolder.removeCallback(this);
			}
		}
		super.onPause();
	}

	private void initCamera(SurfaceHolder surfaceHolder, boolean flashlight) {
		try {
			cameraManager.openDriver(surfaceHolder, flashlight);
			if (handler == null) {
				cameraManager.startPreview();
			}
		} catch (IOException ioe) {
			if (Util.DEBUGGABLE) {
				Log.e(TAG, "Error:" + ioe);
			}
		} catch (RuntimeException e) {
			if (Util.DEBUGGABLE) {
				Log.e(TAG, "Unexpected error initializing camera", e);
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			if (Util.DEBUGGABLE) {
				Log.e(TAG,
						"*** WARNING *** surfaceCreated() gave us a null surface!");
			}
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder, false);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	private void initMainWindow() {

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.target_window);
		GradientDrawable bgShape = (GradientDrawable) linearLayout
				.getBackground();
		bgShape.setColor(Util.generateHexColorValue(C.mainWindow));

		linearLayout = (LinearLayout) findViewById(R.id.target_window_shadow);
		GradientDrawable bgShapeShadow = (GradientDrawable) linearLayout
				.getBackground();
		bgShapeShadow.setColor(Util.generateHexColorValue(C.mainWindowShadow));

		LinearLayout linearLayoutShadow = (LinearLayout) findViewById(R.id.window_shadow);
		linearLayoutShadow.setVisibility(View.VISIBLE);

		TextView textView = (TextView) findViewById(R.id.text_message);
		textView.setTypeface(C.typeFace);
		textView.setText(C.welcomeMessage);
		textView.setVisibility(View.VISIBLE);
		textView.setTextColor(Util
				.generateHexColorValue(C.mainWindowForeground));

		buttonMore.setText(C.btnMore);
		buttonMore.setVisibility(View.VISIBLE);
		buttonMore.setTypeface(C.typeFace);
		if (C.appURL.length() > 0) {
			buttonNext.setText(C.btnNext);
			buttonNext.setVisibility(View.VISIBLE);
			buttonNext.setTypeface(C.typeFace);
		}
		ImageView frameImage = (ImageView) findViewById(R.id.frame_image);
		frameImage.setVisibility(View.GONE);
	}

	abstract class GetHtmlTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			mLoadingSpinner = Util.startSpinner(MainActivity.this, true);
		}

		@Override
		protected void onPostExecute(String result) {

			try {
				try {
					if (result == null && Util.DEBUGGABLE)
						Log.d(TAG, "result is null");
					new JSONParser(result);
				} catch (Exception e) {
					onDestroy();
					Util.startErrorIntent(MainActivity.this,
							C.badServerResponseMessage, true);
					if (Util.DEBUGGABLE) {
						Log.e(TAG, e.getMessage());
					}
				}
				String locale = java.util.Locale.getDefault().toString()
						.substring(0, 2);
				if (!C.languages.isEmpty() && C.languages.contains(locale)){
					C.forLanguages = true;
					C.langURL = C.configURL.replace("MultiLang.json", locale.toUpperCase() + ".json");
					urlConnectLang();
				}else{
					Util.stopSpinner(mLoadingSpinner);
					initMainWindow();
				}
			} catch (Exception e) {
				if (Util.DEBUGGABLE) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
	}

	private void urlConnect() {
		new GetHtmlTask() {

			@Override
			protected String doInBackground(Void... arg0) {
				try {
					response = new HttpRequest(MainActivity.this).get(
							C.configURL, null);
					System.setProperty("http.keepAlive", "false");
				} catch (Exception e) {
					if (Util.DEBUGGABLE) {
						Log.e(TAG, "Tehniline viga: " + e.getMessage(), e);
					}
					return null;
				}
				try {
					if (response == null) {
						return null;
					} else {
						return Util.readLines(
								response.getEntity().getContent(),
								Util.ENCODING);
					}
				} catch (IllegalStateException e) {
					if (Util.DEBUGGABLE) {
						Log.e(TAG, "Tehniline viga: " + e.getMessage(), e);
					}
					Util.startErrorIntent(MainActivity.this,
							C.badServerResponseMessage, true);

				} catch (IOException e) {
					if (Util.DEBUGGABLE) {
						Log.e(TAG, "Tehniline viga: " + e.getMessage(), e);
					}
					Util.startErrorIntent(MainActivity.this,
							C.badServerResponseMessage, true);
				}
				;
				return null;
			}
		}.execute();
	}
	
	abstract class GetHtmlTaskLang extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			
		}

		@Override
		protected void onPostExecute(String result) {

			try {
				try {
					if (result == null && Util.DEBUGGABLE)
						Log.d(TAG, "result is null");
					new JSONParser(result);
				} catch (Exception e) {
					onDestroy();
					Util.startErrorIntent(MainActivity.this,
							C.badServerResponseMessage, true);
					if (Util.DEBUGGABLE) {
						Log.e(TAG, e.getMessage());
					}
				}
				Util.stopSpinner(mLoadingSpinner);
				C.forLanguages = false;
				initMainWindow();
			} catch (Exception e) {
				if (Util.DEBUGGABLE) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
	}
	private void urlConnectLang() {
		new GetHtmlTaskLang() {

			@Override
			protected String doInBackground(Void... arg0) {
				try {
					response = new HttpRequest(MainActivity.this).get(
							C.langURL, null);
					System.setProperty("http.keepAlive", "false");
				} catch (Exception e) {
					if (Util.DEBUGGABLE) {
						Log.e(TAG, "Tehniline viga: " + e.getMessage(), e);
					}
					return null;
				}
				try {
					if (response == null) {
						return null;
					} else {
						return Util.readLines(
								response.getEntity().getContent(),
								Util.ENCODING);
					}
				} catch (IllegalStateException e) {
					if (Util.DEBUGGABLE) {
						Log.e(TAG, "Tehniline viga: " + e.getMessage(), e);
					}
					Util.startErrorIntent(MainActivity.this,
							C.badServerResponseMessage, true);

				} catch (IOException e) {
					if (Util.DEBUGGABLE) {
						Log.e(TAG, "Tehniline viga: " + e.getMessage(), e);
					}
					Util.startErrorIntent(MainActivity.this,
							C.badServerResponseMessage, true);
				}
				;
				return null;
			}
		}.execute();
	}
}