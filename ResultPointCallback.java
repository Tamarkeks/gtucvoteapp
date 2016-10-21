package com.example.godspower.gvote.qr;

import com.google.zxing.ResultPoint;

/**
 * Created by Godspower on 5/3/2016.
 */
public interface ResultPointCallback {
    void foundPossibleResultPoint(ResultPoint point);
}
