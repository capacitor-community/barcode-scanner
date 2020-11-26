package com.dutchconcepts.capacitor.barcodescanner;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;
import java.util.List;

@NativePlugin(permissionRequestCode = BarcodeScanner.REQUEST_CODE)
public class BarcodeScanner extends Plugin implements BarcodeCallback {

    private BarcodeView mBarcodeView;

    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isScanning = false;
    private boolean shouldRunScan = false;
    private boolean didRunCameraSetup = false;
    private boolean didRunCameraPrepare = false;
    private boolean isBackgroundHidden = false;
    private String actionOnResume = null;

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

    private void dismantleCamera(Boolean freeSavedCallFlag) {
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
        if (freeSavedCallFlag && getSavedCall() != null && !shouldRunScan) {
            freeSavedCall();
        }
    }

    private void prepare() {
        // undo previous setup
        // because it may be prepared with a different config
        dismantleCamera(true);

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

        dismantleCamera(true);
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
    public void handleOnPause() {
        if (isScanning) {
            actionOnResume = "scan";
        } else {
            actionOnResume = null;
        }
        dismantleCamera(false);
    }

    @Override
    public void handleOnResume() {
        if (actionOnResume != null) {
            if (actionOnResume.equals("scan")) {
                scan();
            }
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

    @PluginMethod
    public void stopScan(PluginCall call) {
        destroy();
        call.resolve();
    }

    static final int REQUEST_CODE = 57351;

    private static final String TAG_PERMISSION = "permission";

    private static final String GRANTED = "granted";
    private static final String DENIED = "denied";
    private static final String ASKED = "asked";
    private static final String NEVER_ASKED = "neverAsked";

    private static final String PERMISSION_NAME = Manifest.permission.CAMERA;

    private PluginCall savedCall;
    private JSObject savedReturnObject;

    void _checkPermission(PluginCall call, boolean force) {
        this.savedReturnObject = new JSObject();

        if (hasPermission(PERMISSION_NAME)) {
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
                        // also set this.savedCall = call
                        // so a callback can be made from the handleRequestPermissionsResult
                        pluginRequestPermission(PERMISSION_NAME, REQUEST_CODE);
                        this.savedCall = call;
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

    private static String PREFS_PERMISSION_FIRST_TIME_ASKING = "PREFS_PERMISSION_FIRST_TIME_ASKING";

    private void setPermissionFirstTimeAsking(String permission, boolean isFirstTime) {
        SharedPreferences sharedPreference = getActivity().getSharedPreferences(PREFS_PERMISSION_FIRST_TIME_ASKING, MODE_PRIVATE);
        sharedPreference.edit().putBoolean(permission, isFirstTime).apply();
    }

    private boolean isPermissionFirstTimeAsking(String permission) {
        return getActivity().getSharedPreferences(PREFS_PERMISSION_FIRST_TIME_ASKING, MODE_PRIVATE).getBoolean(permission, true);
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);

        if (this.savedCall == null || this.savedReturnObject == null || permissions.length <= 0) {
            // No stored plugin call for permissions request result
            return;
        }
        String permission = permissions[0];

        // the user was apparently requested this permission
        // update the preferences to reflect this
        setPermissionFirstTimeAsking(permission, false);

        boolean granted = false;
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                if (getActivity().shouldShowRequestPermissionRationale(permission)) {
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
        this.savedCall.resolve(this.savedReturnObject);
        // release saved vars
        this.savedCall = null;
        this.savedReturnObject = null;
    }

    @PluginMethod
    public void checkPermission(PluginCall call) {
        Boolean force = call.getBoolean("force", false);

        _checkPermission(call, force);
    }
}
