package com.example.godspower.gtucvote.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.godspower.gtucvote.R;
import com.example.godspower.gtucvote.util.C;
import com.example.godspower.gtucvote.util.Util;


public class LoadingSpinner extends Dialog {
	private Context cx;
	private ImageView iv;
	private TextView tv;
	private boolean isWhite;

	public LoadingSpinner(Context context, boolean isWhite) {
		super(context, R.style.SpinnerDialog);
		this.cx = context;
		this.isWhite = isWhite;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isWhite) {
			setContentView(R.layout.dialog_loading);
		} else {
			setContentView(R.layout.dialog_loading_black);
		}
		setCancelable(false);
		iv = (ImageView) findViewById(R.id.spinner_img);
		iv.startAnimation(AnimationUtils.loadAnimation(cx, R.anim.spinner));

		tv = (TextView) findViewById(R.id.loading);
		if (C.typeFace != null) {
			tv.setTypeface(C.typeFace);
		}
		tv.setText(C.loading);
		if (isWhite) {
			tv.setTextColor(Util
					.generateHexColorValue(C.loadingWindowForeground));
		} else {
			tv.setTextColor(Color.BLACK);
		}
		LayoutParams params = getWindow().getAttributes();
		params.height = LayoutParams.FILL_PARENT;
		params.width = LayoutParams.FILL_PARENT;
		getWindow().setAttributes(
				(LayoutParams) params);
	}
}