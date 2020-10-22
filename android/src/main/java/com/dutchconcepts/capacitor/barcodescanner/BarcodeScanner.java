package com.dutchconcepts.capacitor.barcodescanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.camera.CameraSettings;
import java.util.ArrayList;
import java.util.List;

@NativePlugin
public class BarcodeScanner extends Plugin implements BarcodeCallback {

    private BarcodeView mBarcodeView;

    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isScanning = false;
    private boolean shouldRunScan = false;
    private boolean didRunCameraSetup = false;
    private boolean didRunCameraPrepare = false;
    private boolean isBackgroundHidden = false;

    private boolean hasCamera() {
        // @TODO(): check: https://stackoverflow.com/a/57974578/8634342
        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        } else {
            return false;
        }
    }

    private void setupCamera() {
        // @TODO(): add support for switching cameras
        // @TODO(): add support for toggling torch

        getActivity()
            .runOnUiThread(
                () -> {
                    // Create BarcodeView
                    mBarcodeView = new BarcodeView(getActivity());

                    // Configure the decoder
                    ArrayList<BarcodeFormat> formatList = new ArrayList<BarcodeFormat>();
                    formatList.add(BarcodeFormat.QR_CODE);
                    mBarcodeView.setDecoderFactory(new DefaultDecoderFactory(formatList));

                    // Configure the camera (front/back)
                    CameraSettings settings = new CameraSettings();
                    settings.setRequestedCameraId(currentCameraId);
                    mBarcodeView.setCameraSettings(settings);

                    FrameLayout.LayoutParams cameraPreviewParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    );

                    // Set BarcodeView as sibling View of WebView
                    ((ViewGroup) bridge.getWebView().getParent()).addView(mBarcodeView, cameraPreviewParams);

                    // Bring the WebView in front of the BarcodeView
                    // This allows us to completely style the BarcodeView in HTML/CSS
                    bridge.getWebView().bringToFront();

                    mBarcodeView.resume();
                }
            );

        didRunCameraSetup = true;
    }

    private void dismantleCamera() {
        // opposite of setupCamera

        getActivity()
            .runOnUiThread(
                () -> {
                    if (mBarcodeView != null) {
                        mBarcodeView.pause();
                        mBarcodeView.stopDecoding();
                        ((ViewGroup) bridge.getWebView().getParent()).removeView(mBarcodeView);
                    }
                }
            );

        isScanning = false;
        didRunCameraSetup = false;
        didRunCameraPrepare = false;

        // If a call is saved and a scan will not run, free the saved call
        if (getSavedCall() != null && !shouldRunScan) {
            freeSavedCall();
        }
    }

    private void prepare() {
        // undo previous setup
        // because it may be prepared with a different config
        dismantleCamera();

        // setup camera with new config
        setupCamera();

        // indicate this method was run
        didRunCameraPrepare = true;

        if (shouldRunScan) {
            scan();
        }
    }

    private void destroy() {
        showBackground();

        dismantleCamera();
    }

    private void scan() {
        if (!didRunCameraPrepare) {
            if (hasCamera()) {
                if (!hasPermission(Manifest.permission.CAMERA)) {
                    // @TODO()
                    // requestPermission()
                } else {
                    shouldRunScan = true;
                    prepare();
                }
            }
        } else {
            didRunCameraPrepare = false;

            shouldRunScan = false;

            final BarcodeCallback b = this;
            getActivity()
                .runOnUiThread(
                    () -> {
                        if (mBarcodeView != null) {
                            mBarcodeView.decodeSingle(b);
                        }
                    }
                );

            hideBackground();

            isScanning = true;
        }
    }

    private void hideBackground() {
        getActivity()
            .runOnUiThread(
                () -> {
                    bridge.getWebView().setBackgroundColor(Color.TRANSPARENT);
                    bridge.getWebView().loadUrl("javascript:document.documentElement.style.backgroundColor = 'transparent';void(0);");
                    isBackgroundHidden = true;
                }
            );
    }

    private void showBackground() {
        getActivity()
            .runOnUiThread(
                () -> {
                    bridge.getWebView().setBackgroundColor(Color.WHITE);
                    bridge.getWebView().loadUrl("javascript:document.documentElement.style.backgroundColor = '';void(0);");
                    isBackgroundHidden = false;
                }
            );
    }

    @Override
    public void barcodeResult(BarcodeResult barcodeResult) {
        JSObject jsObject = new JSObject();

        if (barcodeResult.getText() != null) {
            jsObject.put("hasContent", true);
            jsObject.put("content", barcodeResult.getText());
        } else {
            jsObject.put("hasContent", false);
        }

        if (getSavedCall() != null) {
            getSavedCall().resolve(jsObject);
        }

        destroy();
    }

    @Override
    public void possibleResultPoints(List<ResultPoint> resultPoints) {}

    @PluginMethod
    public void prepare(PluginCall call) {
        prepare();
        call.resolve();
    }

    @PluginMethod
    public void hideBackground(PluginCall call) {
        hideBackground();
        call.resolve();
    }

    @PluginMethod
    public void showBackground(PluginCall call) {
        showBackground();
        call.resolve();
    }

    @PluginMethod
    public void startScan(PluginCall call) {
        saveCall(call);
        scan();
    }

    @PluginMethod
    public void stopScan(PluginCall call) {
        destroy();
        call.resolve();
    }
}
