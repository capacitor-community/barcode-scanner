package com.dutchconcepts.capacitor.barcodescanner;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BarcodeActivity extends AppCompatActivity {

    private static final String LOG_TAG = BarcodeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        ArrayList<String> targetedFormats = intent.getStringArrayListExtra("targetedFormats");

        if (targetedFormats == null || targetedFormats.size() == 0) {
            targetedFormats = (ArrayList<String>) IntentIntegrator.ALL_CODE_TYPES;
        }

        try {
            if (!IntentIntegratorSingleton.getInstance().getScanState()) {
                IntentIntegratorSingleton.getInstance().showScanner(this, targetedFormats);
            }
        } catch (ActivityNotFoundException lEx) {
            IntentIntegratorSingleton.getInstance().setScanState(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanResult != null) {
            // handle scan result
            Log.d(LOG_TAG, scanResult.toString());
            IntentIntegratorSingleton.getInstance().setScanState(false);

            Intent result = getIntent();
            result.putExtra("Barcode", scanResult.getContents());
            setResult(Activity.RESULT_OK, result);
            finish();
        }
    }
}
