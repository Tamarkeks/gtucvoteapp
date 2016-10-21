package com.example.godspower.gtucvote.qr;

import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;

final class ViewfinderResultPointCallback implements ResultPointCallback {

	private final ViewfinderView viewfinderView;

	ViewfinderResultPointCallback(ViewfinderView viewfinderView) {
		this.viewfinderView = viewfinderView;
	}

	public ViewfinderResultPointCallback(Object viewfinderView) {
		this.foundPossibleResultPoint((ResultPoint) viewfinderView);
		this.viewfinderView = null;
	}


	@Override
	public void foundPossibleResultPoint(ResultPoint point) {
		viewfinderView.addPossibleResultPoint(point);
	}
}