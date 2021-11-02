package com.dutchconcepts.capacitor.barcodescanner;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.activity.result.ActivityResult;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.camera.CameraSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;

@CapacitorPlugin(permissions = { @Permission(strings = { Manifest.permission.CAMERA }, alias = BarcodeScanner.PERMISSION_ALIAS_CAMERA) })
public class BarcodeScanner extends Plugin implements BarcodeCallback {

    public static final String PERMISSION_ALIAS_CAMERA = "camera";

    private BarcodeView mBarcodeView;

    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isScanning = false;
    private boolean shouldRunScan = false;
    private boolean didRunCameraSetup = false;
    private boolean didRunCameraPrepare = false;
    private boolean isBackgroundHidden = false;
    private boolean scanningPaused = false;
    private String lastScanResult = null;

    // declare a map constant for allowed barcode formats
    private static final Map<String, BarcodeFormat> supportedFormats = supportedFormats();

    private static Map<String, BarcodeFormat> supportedFormats() {
        Map<String, BarcodeFormat> map = new HashMap<>();
        // 1D Product
        map.put("UPC_A", BarcodeFormat.UPC_A);
        map.put("UPC_E", BarcodeFormat.UPC_E);
        map.put("UPC_EAN_EXTENSION", BarcodeFormat.UPC_EAN_EXTENSION);
        map.put("EAN_8", BarcodeFormat.EAN_8);
        map.put("EAN_13", BarcodeFormat.EAN_13);
        // 1D Industrial
        map.put("CODE_39", BarcodeFormat.CODE_39);
        map.put("CODE_93", BarcodeFormat.CODE_93);
        map.put("CODE_128", BarcodeFormat.CODE_128);
        map.put("CODABAR", BarcodeFormat.CODABAR);
        map.put("ITF", BarcodeFormat.ITF);
        // 2D
        map.put("AZTEC", BarcodeFormat.AZTEC);
        map.put("DATA_MATRIX", BarcodeFormat.DATA_MATRIX);
        map.put("MAXICODE", BarcodeFormat.MAXICODE);
        map.put("PDF_417", BarcodeFormat.PDF_417);
        map.put("QR_CODE", BarcodeFormat.QR_CODE);
        map.put("RSS_14", BarcodeFormat.RSS_14);
        map.put("RSS_EXPANDED", BarcodeFormat.RSS_EXPANDED);
        return Collections.unmodifiableMap(map);
    }

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

                    // Configure the camera (front/back)
                    CameraSettings settings = new CameraSettings();
                    settings.setRequestedCameraId(currentCameraId);
                    settings.setContinuousFocusEnabled(true);
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
                        mBarcodeView = null;
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

    private void configureCamera() {
        getActivity()
            .runOnUiThread(
                () -> {
                    PluginCall call = getSavedCall();

                    if (call == null || mBarcodeView == null) {
                        Log.d("scanner", "Something went wrong with configuring the BarcodeScanner.");
                        return;
                    }

                    DefaultDecoderFactory defaultDecoderFactory = new DefaultDecoderFactory(null, null, null, Intents.Scan.MIXED_SCAN);

                    if (call.hasOption("targetedFormats")) {
                        JSArray targetedFormats = call.getArray("targetedFormats");
                        ArrayList<BarcodeFormat> formatList = new ArrayList<>();

                        if (targetedFormats != null && targetedFormats.length() > 0) {
                            for (int i = 0; i < targetedFormats.length(); i++) {
                                try {
                                    String targetedFormat = targetedFormats.getString(i);
                                    BarcodeFormat targetedBarcodeFormat = supportedFormats.get(targetedFormat);
                                    if (targetedBarcodeFormat != null) {
                                        formatList.add(targetedBarcodeFormat);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (formatList.size() > 0) {
                            defaultDecoderFactory = new DefaultDecoderFactory(formatList, null, null, Intents.Scan.MIXED_SCAN);
                        } else {
                            Log.d("scanner", "The property targetedFormats was not set correctly.");
                        }
                    }

                    mBarcodeView.setDecoderFactory(defaultDecoderFactory);
                }
            );
    }

    private void scan() {
        if (!didRunCameraPrepare) {
            if (hasCamera()) {
                if (!hasPermission(Manifest.permission.CAMERA)) {
                    Log.d("scanner", "No permission to use camera. Did you request it yet?");
                } else {
                    shouldRunScan = true;
                    prepare();
                }
            }
        } else {
            didRunCameraPrepare = false;

            shouldRunScan = false;

            configureCamera();

            final BarcodeCallback b = this;
            getActivity()
                .runOnUiThread(
                    () -> {
                        if (mBarcodeView != null) {
                            PluginCall call = getSavedCall();
                            if (call != null && call.isKeptAlive()) {
                                mBarcodeView.decodeContinuous(b);
                            } else {
                                mBarcodeView.decodeSingle(b);
                            }
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

        PluginCall call = getSavedCall();

        if (call != null) {
            if (call.isKeptAlive()) {
                if (!scanningPaused && barcodeResult.getText() != null && !barcodeResult.getText().equals(lastScanResult)) {
                    lastScanResult = barcodeResult.getText();
                    call.resolve(jsObject);
                }
            } else {
                call.resolve(jsObject);
                destroy();
            }
        } else {
            destroy();
        }
    }

    @Override
    public void handleOnPause() {
        if (mBarcodeView != null) {
            mBarcodeView.pause();
        }
    }

    @Override
    public void handleOnResume() {
        if (mBarcodeView != null) {
            mBarcodeView.resume();
        }
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

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void startScanning(PluginCall call) {
        call.setKeepAlive(true);
        lastScanResult = null; // reset when scanning again
        saveCall(call);
        scanningPaused = false;
        scan();
    }

    @PluginMethod
    public void pauseScanning(PluginCall call) {
        scanningPaused = true;
        call.resolve();
    }

    @PluginMethod
    public void resumeScanning(PluginCall call) {
        scanningPaused = false;
        call.resolve();
    }

    @PluginMethod
    public void stopScan(PluginCall call) {
        if (call.hasOption("resolveScan") && getSavedCall() != null) {
            Boolean resolveScan = call.getBoolean("resolveScan", false);
            if (resolveScan != null && resolveScan) {
                JSObject jsObject = new JSObject();
                jsObject.put("hasContent", false);

                getSavedCall().resolve(jsObject);
            }
        }

        destroy();
        call.resolve();
    }

    private static final String TAG_PERMISSION = "permission";

    private static final String GRANTED = "granted";
    private static final String DENIED = "denied";
    private static final String ASKED = "asked";
    private static final String NEVER_ASKED = "neverAsked";

    private static final String PERMISSION_NAME = Manifest.permission.CAMERA;

    private JSObject savedReturnObject;

    void _checkPermission(PluginCall call, boolean force) {
        this.savedReturnObject = new JSObject();

        if (getPermissionState(PERMISSION_ALIAS_CAMERA) == PermissionState.GRANTED) {
            // permission GRANTED
            this.savedReturnObject.put(GRANTED, true);
        } else {
            // permission NOT YET GRANTED

            // check if asked before
            boolean neverAsked = isPermissionFirstTimeAsking(PERMISSION_NAME);
            if (neverAsked) {
                this.savedReturnObject.put(NEVER_ASKED, true);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // from version Android M on,
                // on runtime,
                // each permission can be temporarily denied,
                // or be denied forever
                if (neverAsked || getActivity().shouldShowRequestPermissionRationale(PERMISSION_NAME)) {
                    // permission never asked before
                    // OR
                    // permission DENIED, BUT not for always
                    // So
                    // can be asked (again)
                    if (force) {
                        // request permission
                        // so a callback can be made from the handleRequestPermissionsResult
                        requestPermissionForAlias(PERMISSION_ALIAS_CAMERA, call, "cameraPermsCallback");
                        return;
                    }
                } else {
                    // permission DENIED
                    // user ALSO checked "NEVER ASK AGAIN"
                    this.savedReturnObject.put(DENIED, true);
                }
            } else {
                // below android M
                // no runtime permissions exist
                // so always
                // permission GRANTED
                this.savedReturnObject.put(GRANTED, true);
            }
        }
        call.resolve(this.savedReturnObject);
    }

    private static final String PREFS_PERMISSION_FIRST_TIME_ASKING = "PREFS_PERMISSION_FIRST_TIME_ASKING";

    private void setPermissionFirstTimeAsking(String permission, boolean isFirstTime) {
        SharedPreferences sharedPreference = getActivity().getSharedPreferences(PREFS_PERMISSION_FIRST_TIME_ASKING, MODE_PRIVATE);
        sharedPreference.edit().putBoolean(permission, isFirstTime).apply();
    }

    private boolean isPermissionFirstTimeAsking(String permission) {
        return getActivity().getSharedPreferences(PREFS_PERMISSION_FIRST_TIME_ASKING, MODE_PRIVATE).getBoolean(permission, true);
    }

    @PermissionCallback
    private void cameraPermsCallback(PluginCall call) {
        if (this.savedReturnObject == null) {
            // No stored plugin call for permissions request result
            return;
        }

        // the user was apparently requested this permission
        // update the preferences to reflect this
        setPermissionFirstTimeAsking(PERMISSION_NAME, false);

        boolean granted = false;
        if (getPermissionState(PERMISSION_ALIAS_CAMERA) == PermissionState.GRANTED) {
            granted = true;
        }

        // indicate that the user has been asked to accept this permission
        this.savedReturnObject.put(ASKED, true);

        if (granted) {
            // permission GRANTED
            Log.d(TAG_PERMISSION, "Asked. Granted");
            this.savedReturnObject.put(GRANTED, true);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (getActivity().shouldShowRequestPermissionRationale(PERMISSION_NAME)) {
                    // permission DENIED
                    // BUT not for always
                    Log.d(TAG_PERMISSION, "Asked. Denied For Now");
                } else {
                    // permission DENIED
                    // user ALSO checked "NEVER ASK AGAIN"
                    Log.d(TAG_PERMISSION, "Asked. Denied");
                    this.savedReturnObject.put(DENIED, true);
                }
            } else {
                // below android M
                // no runtime permissions exist
                // so always
                // permission GRANTED
                Log.d(TAG_PERMISSION, "Asked. Granted");
                this.savedReturnObject.put(GRANTED, true);
            }
        }
        // resolve saved call
        call.resolve(this.savedReturnObject);
        // release saved vars
        this.savedReturnObject = null;
    }

    @PluginMethod
    public void checkPermission(PluginCall call) {
        Boolean force = call.getBoolean("force", false);

        if (force != null && force) {
            _checkPermission(call, true);
        } else {
            _checkPermission(call, false);
        }
    }

    @PluginMethod
    public void openAppSettings(PluginCall call) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getAppId(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(call, intent, "openSettingsResult");
    }

    @ActivityCallback
    private void openSettingsResult(PluginCall call, ActivityResult result) {
        call.resolve();
    }
}
