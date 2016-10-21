package com.example.godspower.gtucvote;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.example.godspower.gtucvote.dialog.LoadingSpinner;
import com.example.godspower.gtucvote.util.C;
import com.example.godspower.gtucvote.util.HttpRequest;
import com.example.godspower.gtucvote.util.RegexMatcher;
import com.example.godspower.gtucvote.util.Util;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTPS connection with a web server. TLS authentication with own trust store.
 * 
 * @version 16.05.2013
 */
public class VoteDownloadActivity extends Activity {

	private static final String TAG = VoteDownloadActivity.class
			.getSimpleName();

	private LoadingSpinner mLoadingSpinner;
	private String qrCode;
	private String webResult;
	private String versionNumber;
	private String controlContainer;
	private int controlState;
	private HttpResponse response;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.vote_download_activity);

		Intent intent = getIntent();
		qrCode = intent.getStringExtra(Util.EXTRA_MESSAGE);

		LinearLayout frameBg = (LinearLayout) findViewById(R.id.vote_download_frame_bg);
		frameBg.setBackgroundColor(Util
				.generateHexColorValue(C.frameBackground));

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.target_window_load);
		GradientDrawable bgShape = (GradientDrawable) linearLayout
				.getBackground();
		bgShape.setColor(Util.generateHexColorValue(C.loadingWindow));

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			urlConnConnect();
		} else {
			Util.startErrorIntent(VoteDownloadActivity.this,
					C.noNetworkMessage, false);
		}
	}

	abstract class GetHtmlTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			mLoadingSpinner = Util
					.startSpinner(VoteDownloadActivity.this, true);
		}

		@Override
		protected void onPostExecute(String result) {
			Util.stopSpinner(mLoadingSpinner);
			if (result != null) {
				if (Util.DEBUGGABLE) {
					Log.d(TAG, "Response from the server: " + result);
				}
				try {
					versionNumber = result.split("\n")[0];

					if (!RegexMatcher.IsOneOrTwoDigits(versionNumber)) {
						Util.startErrorIntent(VoteDownloadActivity.this,
								C.badServerResponseMessage, true);
						
					}
					controlState = Integer.parseInt(result.split("\n")[1]);

					if (!RegexMatcher.IsOneDigit(result.split("\n")[1])) {
						Util.startErrorIntent(VoteDownloadActivity.this,
								C.badServerResponseMessage, true);
					}
					controlContainer = result.split("\n" + controlState + "\n")[1];

					if (Util.DEBUGGABLE) {
						Log.e(TAG, "Control state: " + controlState);
					}
					if (controlState == 0) {
						if (Util.DEBUGGABLE) {
							Log.d(TAG, "Control container: " + controlContainer);
						}
						webResult = controlContainer;

						startNextIntent();
					} else {
						Util.startErrorIntent(VoteDownloadActivity.this,
								controlContainer, true);
					}
				} catch (Exception e) {
					Util.startErrorIntent(VoteDownloadActivity.this,
							C.noNetworkMessage, false);

					if (Util.DEBUGGABLE) {
						Log.e(TAG, e.getMessage());
					}
				}

			} else {
				Util.startErrorIntent(VoteDownloadActivity.this,
						C.badServerResponseMessage, true);
				if (Util.DEBUGGABLE) {
					Log.e(TAG, "Server output result is null");
				}
			}
		}
	}

	private void urlConnConnect() {
		new GetHtmlTask() {

			@Override
			protected String doInBackground(Void... arg0) {
				try {
					List<NameValuePair> entryNameValuePairs = new ArrayList<NameValuePair>();
					entryNameValuePairs.add(new BasicNameValuePair(
							Util.VERIFY_PARAMETER, qrCode.substring(0, 40)));
					response = new HttpRequest(VoteDownloadActivity.this).post(
							C.appURL, null, entryNameValuePairs);
				} catch (Exception e) {
					if (Util.DEBUGGABLE) {
						Log.e(TAG, "Tehniline viga: " + e.getMessage(), e);
					}
					return null;
				}
				try {
					if (response != null)
						return Util.readLines(
								response.getEntity().getContent(),
								Util.ENCODING);
				} catch (IllegalStateException e) {
					if (Util.DEBUGGABLE) {
						Log.e(TAG, "Tehniline viga: " + e.getMessage(), e);
					}
					Util.startErrorIntent(VoteDownloadActivity.this,
							C.badServerResponseMessage, true);

				} catch (IOException e) {
					if (Util.DEBUGGABLE) {
						Log.e(TAG, "Tehniline viga: " + e.getMessage(), e);
					}
					Util.startErrorIntent(VoteDownloadActivity.this,
							C.badServerResponseMessage, true);
				}

				return null;
			}
		}.execute();
	}

	public void startNextIntent() {
		Intent next_intent = new Intent(this, VoteActivity.class);
		next_intent.putExtra(Util.QR_CODE, qrCode);
		next_intent.putExtra(Util.WEB_RESULT, webResult);
		next_intent.putExtra(Util.VERSION_NUMBER, versionNumber);
		startActivity(next_intent);
		finish();
	}
}