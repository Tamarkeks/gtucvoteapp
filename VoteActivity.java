package com.example.godspower.gtucvote;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.godspower.gtucvote.model.Vote;
import com.example.godspower.gtucvote.util.C;
import com.example.godspower.gtucvote.util.Util;

/**
 * Vote activity.
 */
public class VoteActivity extends Activity {

	private Button btnVoteVerify;

	private static final String TAG = VoteActivity.class.getSimpleName();

	private String qrCode;
	private String webResult;

	private String versionNumber;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.vote_activity);

		Intent intent = getIntent();
		qrCode = intent.getStringExtra(Util.QR_CODE);
		webResult = intent.getStringExtra(Util.WEB_RESULT);
		versionNumber = intent.getStringExtra(Util.VERSION_NUMBER);

		TextView lblVote = (TextView) findViewById(R.id.vote_label);
		lblVote.setText(C.lblVote);
		lblVote.setTypeface(C.typeFace);
		lblVote.setTextColor(Util.generateHexColorValue(C.lblForeground));
		lblVote.setBackgroundColor(Util.generateHexColorValue(C.lblBackground));

		View lblShadow = (View) findViewById(R.id.vote_label_shadow);
		lblShadow.setBackgroundColor(Util.generateHexColorValue(C.lblShadow));

		TextView lblVoteTxt = (TextView) findViewById(R.id.vote_txt_label);
		lblVoteTxt.setTypeface(C.typeFace);
		lblVoteTxt.setText(C.lblVoteTxt);
		lblVoteTxt.setTextColor(Util
				.generateHexColorValue(C.lblOuterContainerForeground));

		btnVoteVerify = (Button) findViewById(R.id.vote_btn_verify);
		btnVoteVerify.setTypeface(C.typeFace);
		btnVoteVerify.setText(C.btnVerify);
		btnVoteVerify.setTextColor(Util
				.generateHexColorValue(C.btnVerifyForeground));
		int colors[] = new int[3];
		colors[0] = Util.generateHexColorValue(C.btnVerifyBackgroundStart);
		colors[1] = Util.generateHexColorValue(C.btnVerifyBackgroundCenter);
		colors[2] = Util.generateHexColorValue(C.btnVerifyBackgroundEnd);

		GradientDrawable bgVoteVerifyShape = new GradientDrawable(
				GradientDrawable.Orientation.TOP_BOTTOM, colors);
		bgVoteVerifyShape.setCornerRadius(5);
		btnVoteVerify.setBackgroundDrawable(bgVoteVerifyShape);
		btnVoteVerify.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clickVerifyButton(v);
			}
		});
		Vote vxml = new Vote();
		try {
			vxml.parseHeader(webResult);

			btnVoteVerify.setClickable(true);
			btnVoteVerify.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			if (Util.DEBUGGABLE) {
				Log.d(TAG, "Parser exception. Vote could not be parsed.");
			}
			Util.startErrorIntent(VoteActivity.this,
					C.badServerResponseMessage, true);
		}
	}

	public void clickVerifyButton(View view) {
		Intent next_intent = new Intent(this, BruteForceActivity.class);
		next_intent.putExtra(Util.QR_CODE, qrCode);
		next_intent.putExtra(Util.WEB_RESULT, webResult);
		next_intent.putExtra(Util.VERSION_NUMBER, versionNumber);
		startActivity(next_intent);
		finish();
	}
}