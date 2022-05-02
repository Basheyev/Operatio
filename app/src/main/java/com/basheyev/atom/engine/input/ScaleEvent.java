package com.basheyev.atom.engine.input;

import android.view.ScaleGestureDetector;

public class ScaleEvent {

    public boolean isScalingInProgress;
    public float scaleFactor;
    public float focusX;
    public float focusY;

    public ScaleEvent(ScaleGestureDetector detector, boolean inProgress) {
        scaleFactor = detector.getScaleFactor();
        focusX = detector.getFocusX();
        focusY = detector.getFocusY();
        isScalingInProgress = inProgress;
    }

}
