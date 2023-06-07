package com.getcapacitor.community.barcodescanner;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.json.JSONException;

@CapacitorPlugin(
    permissions = {
        @Permission(strings = { Manifest.permission.CAMERA }, alias = CapacitorCommunityBarcodeScanner.PERMISSION_ALIAS_CAMERA)
    }
)
public class CapacitorCommunityBarcodeScanner extends Plugin implements ImageAnalysis.Analyzer {

    public static final String PERMISSION_ALIAS_CAMERA = "camera";

    private PreviewView mPreviewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ProcessCameraProvider mCameraProvider;
    private GraphicOverlay mGraphicOverlay;
    private Canvas mCanvas;

    private boolean shouldRunScan = false;
    private boolean didRunCameraPrepare = false;
    private boolean isTorchOn = false;
    private boolean isBackgroundHidden = false;
    private boolean scanningPaused = false;
    private float zoomLevel = 1;

    private static final String MLKIT_TAG = "MLKIT";

    private ArrayList<String> scannedResult = new ArrayList<String>();
    private Camera mCamera = null;
    Vibrator mVibrator;

    // initilize with Barcode.FORMAT_QR_CODE
    // this will be overwritten and called with last options coming by supportedFormat()
    private BarcodeScannerOptions mOptions;

    // can't use BarcodeScanner as a object name since it overlap with BarcodeScanner Class name
    com.google.mlkit.vision.barcode.BarcodeScanner mScanner;

    // declare a map constant for allowed barcode formats
    private static final Map<String, Integer> supportedFormats = supportedFormats();

    private static Map<String, Integer> supportedFormats() {
        Map<String, Integer> map = new HashMap<>();
        // 1D Product
        map.put("UPC_A", Barcode.FORMAT_UPC_A);
        map.put("UPC_E", Barcode.FORMAT_UPC_E);
        //        map.put("UPC_EAN_EXTENSION", Barcode.FORMAT_UPC_EAN_EXTENSION); // not supported by MLKit
        map.put("EAN_8", Barcode.FORMAT_EAN_8);
        map.put("EAN_13", Barcode.FORMAT_EAN_13);
        // 1D Industrial
        map.put("CODE_39", Barcode.FORMAT_CODE_39);
        map.put("CODE_93", Barcode.FORMAT_CODE_93);
        map.put("CODE_128", Barcode.FORMAT_CODE_128);
        map.put("CODABAR", Barcode.FORMAT_CODABAR);
        map.put("ITF", Barcode.FORMAT_ITF);
        // 2D
        map.put("AZTEC", Barcode.FORMAT_AZTEC);
        map.put("DATA_MATRIX", Barcode.FORMAT_DATA_MATRIX);
        //        map.put("MAXICODE", Barcode.FORMAT_MAXICODE); // not supported by MLKIT
        map.put("PDF_417", Barcode.FORMAT_PDF417);
        map.put("QR_CODE", Barcode.FORMAT_QR_CODE);
        //        map.put("RSS_14", Barcode.FORMAT_RSS_14); // not supported by MLKIT
        //        map.put("RSS_EXPANDED", Barcode.FORMAT_RSS_EXPANDED); // not supported by MLKIT

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

    private void setupCamera(String cameraDirection) {
        // @TODO(): add support for switching cameras while scanning is running

        getActivity()
            .runOnUiThread(() -> {
                // Create the mPreviewView
                mPreviewView = getActivity().findViewById(R.id.preview_view);
                mGraphicOverlay = getActivity().findViewById(R.id.graphic_overlay);

                cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
                cameraProviderFuture.addListener(
                    () -> {
                        try {
                            mCameraProvider = cameraProviderFuture.get();
                            bindPreview(mCameraProvider, cameraDirection.equals("front") ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK);
                        } catch (InterruptedException | ExecutionException e) {
                            // No errors need to be handled for this Future.
                            // This should never be reached.
                        }
                    },
                    ContextCompat.getMainExecutor(getContext())
                );
            });
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider, int cameraDirection) {
        getActivity()
            .runOnUiThread(() -> {
                Preview preview = new Preview.Builder().build();

                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(cameraDirection).build();

                preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(new Size(1280, 720))
                    .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(getContext()), this);

                mCamera = cameraProvider.bindToLifecycle((LifecycleOwner) getContext(), cameraSelector, preview, imageAnalysis);
            });
    }

    private void dismantleCamera() {
        // opposite of setupCamera
        getActivity()
            .runOnUiThread(() -> {
                if (mCameraProvider != null) {
                    mCameraProvider.unbindAll();
                    mCamera = null;
                    mPreviewView = null;
                }
            });

        didRunCameraPrepare = false;

        // If a call is saved and a scan will not run, free the saved call
        if (getSavedCall() != null && !shouldRunScan) {
            freeSavedCall();
        }
    }

    private void _prepare(PluginCall call) {
        // undo previous setup
        // because it may be prepared with a different config
        dismantleCamera();

        // setup camera with new config
        setupCamera(call.getString("cameraDirection", "back"));

        // indicate this method was run
        didRunCameraPrepare = true;

        if (shouldRunScan) {
            scan();
        }
    }

    private void destroy() {
        scannedResult.clear();
        showBackground();
        dismantleCamera();
        this.setTorch(false);
    }

    private void configureCamera() {
        getActivity()
            .runOnUiThread(() -> {
                PluginCall call = getSavedCall();
                mVibrator = (Vibrator) getActivity().getSystemService(getContext().VIBRATOR_SERVICE);



                if (call == null || mPreviewView == null) {
                    Log.d("scanner", "Something went wrong with configuring the BarcodeScanner.");
                    return;
                }

                if (call.hasOption("targetedFormats")) {
                    JSArray targetedFormats = call.getArray("targetedFormats");

                    ArrayList<Integer> formatList = new ArrayList<>();

                    if (targetedFormats != null && targetedFormats.length() > 0) {
                        for (int i = 0; i < targetedFormats.length(); i++) {
                            try {
                                String targetedFormat = targetedFormats.getString(i);
                                int targetedBarcodeFormat = supportedFormats.get(targetedFormat);
                                if (targetedBarcodeFormat != 0) {
                                    formatList.add(targetedBarcodeFormat);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (formatList.size() > 0) {
                        int[] formats = convertIntegers(formatList);
                        mOptions = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE, formats).build();
                        mScanner = BarcodeScanning.getClient(mOptions);
                    } else {
                        Log.d("scanner", "The property targetedFormats was not set correctly.");
                    }
                }
                if (mScanner == null) {
                    mOptions = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build();
                    mScanner = BarcodeScanning.getClient(mOptions);
                }
            });
    }

    public static int[] convertIntegers(List<Integer> integers) {
        int[] ret = new int[integers.size()];
        for (int i = 0; i < ret.length; i++) {
            if (integers.get(i).intValue() != Barcode.FORMAT_QR_CODE) ret[i] = integers.get(i).intValue();
        }
        return ret;
    }

    private void scan() {
        if (!didRunCameraPrepare) {
            if (hasCamera()) {
                if (!hasPermission(Manifest.permission.CAMERA)) {
                    Log.d("scanner", "No permission to use camera. Did you request it yet?");
                } else {
                    shouldRunScan = true;
                    _prepare(getSavedCall());
                }
            }
        } else {
            didRunCameraPrepare = false;

            shouldRunScan = false;

            configureCamera();
            hideBackground();
        }
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        @SuppressLint("UnsafeOptInUsageError")
        Image mediaImage = image.getImage();

        if (mediaImage != null) {
            InputImage inputImage = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());

            Task<List<Barcode>> result = mScanner
                .process(inputImage)
                .addOnSuccessListener(
                    new OnSuccessListener<List<Barcode>>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            if (scanningPaused) {
                                return;
                            }
                            for (Barcode barcode : barcodes) {
                                PluginCall call = getSavedCall();

                                Rect bounds = barcode.getBoundingBox();
                                Point[] corners = barcode.getCornerPoints();
                                String rawValue = barcode.getRawValue();

                                // add vibration logic here

                                String s = bounds.flattenToString();
                                Log.e(MLKIT_TAG, "content : " + rawValue);
                                //                                                    Log.e(MLKIT_TAG,"corners : " + corners.toString());
                                Log.e(MLKIT_TAG, "bounds : " + bounds.flattenToString());

                                if (!scannedResult.contains(rawValue)) {
                                    Log.e(MLKIT_TAG, "Added Into ArrayList : " + rawValue);

                                    scannedResult.add(rawValue);

                                    JSObject jsObject = new JSObject();
                                    int[] boundArr = { bounds.top, bounds.bottom, bounds.right, bounds.left };
                                    Log.e(MLKIT_TAG, "onSuccess: boundArr");
                                    jsObject.put("hasContent", true);
                                    jsObject.put("content", rawValue);
                                    jsObject.put("format", null);
                                    //                                                        jsObject.put("corners",corners);
                                    jsObject.put("bounds", s);

                                    if (call != null && !call.isKeptAlive()) {
                                        destroy();
                                    }
                                    call.resolve(jsObject);
                                }
                            }
                        }
                    }
                )
                .addOnFailureListener(
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(MLKIT_TAG, e.toString());
                        }
                    }
                )
                .addOnCompleteListener(
                    new OnCompleteListener<List<Barcode>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Barcode>> task) {
                            image.close();
                            mediaImage.close();
                        }
                    }
                );
        }
    }

    @PluginMethod
    public void vibrate(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mVibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    @PluginMethod
    public void prepare(PluginCall call) {
        _prepare(call);
        call.resolve();
    }

    private void hideBackground() {
        getActivity()
            .runOnUiThread(() -> {
                bridge.getWebView().setBackgroundColor(Color.TRANSPARENT);
                bridge.getWebView().loadUrl("javascript:document.documentElement.style.backgroundColor = 'transparent';void(0);");
                isBackgroundHidden = true;
            });
    }

    @PluginMethod
    public void hideBackground(PluginCall call) {
        hideBackground();
        call.resolve();
    }

    private void showBackground() {
        getActivity()
            .runOnUiThread(() -> {
                bridge.getWebView().setBackgroundColor(Color.WHITE);
                bridge.getWebView().loadUrl("javascript:document.documentElement.style.backgroundColor = '';void(0);");
                isBackgroundHidden = false;
            });
    }

    @PluginMethod
    public void showBackground(PluginCall call) {
        showBackground();
        call.resolve();
    }

    // @PluginMethod
    // public void startScan(PluginCall call) {
    //     saveCall(call);
    //     scan();
    // }

    @PluginMethod
    public void stop(PluginCall call) {
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

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void start(PluginCall call) {
        call.setKeepAlive(true);
        saveCall(call);
        scanningPaused = false;
        scan();
    }

    @PluginMethod
    public void pause(PluginCall call) {
        scanningPaused = true;
        call.resolve();
    }

    @PluginMethod
    public void resume(PluginCall call) {
        // lastScanResult = null; // reset when scanning again
        scanningPaused = false;
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

    private void setTorch(boolean on) {
        if (on != isTorchOn) {
            isTorchOn = on;
            getActivity()
                .runOnUiThread(() -> {
                    if (mCamera != null && mCamera.getCameraInfo().hasFlashUnit()) {
                        mCamera.getCameraControl().enableTorch(on);
                    }
                });
        }
    }

    private void setZoomLevel(float zoom) {
        if (zoom != zoomLevel) {
          float clampedZoomScale = Math.max(1.0f, Math.min(zoom, mCamera.cameraControl.getMaxZoomRatio()));
          zoomLevel = clampedZoomScale;
          getActivity()
            .runOnUiThread(() -> {
              if (mCamera != null) {
                mCamera.cameraControl.setZoomRatio(zoomLevel);
              }
            });
        }
    }

    @PluginMethod
    public void setZoom(PluginCall call) {
      float zoom = call.getFloat("zoom");
      this.setZoomLevel(zoom);
      call.resolve();
    }

    @PluginMethod
    public void enableTorch(PluginCall call) {
        this.setTorch(true);
        call.resolve();
    }

    @PluginMethod
    public void disableTorch(PluginCall call) {
        this.setTorch(false);
        call.resolve();
    }

    @PluginMethod
    public void toggleTorch(PluginCall call) {
        this.setTorch(!this.isTorchOn);
        call.resolve();
    }

    @PluginMethod
    public void getTorchState(PluginCall call) {
        JSObject result = new JSObject();

        result.put("isEnabled", this.isTorchOn);

        call.resolve(result);
    }
}
