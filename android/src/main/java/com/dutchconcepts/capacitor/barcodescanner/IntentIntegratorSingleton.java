package com.dutchconcepts.capacitor.barcodescanner;

import android.app.Activity;
import com.google.zxing.integration.android.IntentIntegrator;
import java.util.Collection;

public class IntentIntegratorSingleton {

    private static IntentIntegratorSingleton instance;
    private static boolean scannerOpen = false;

    private IntentIntegratorSingleton() {
        super();
    }

    public static IntentIntegratorSingleton getInstance() {
        if (IntentIntegratorSingleton.instance == null) {
            IntentIntegratorSingleton.instance = new IntentIntegratorSingleton();
        }
        return IntentIntegratorSingleton.instance;
    }

    public void showScanner(Activity activity, Collection<String> types) {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats(types);
        integrator.setBeepEnabled(false);
        scannerOpen = true;
        integrator.initiateScan();
    }

    public void setScanState(boolean open) {
        scannerOpen = open;
    }

    public boolean getScanState() {
        return scannerOpen;
    }
}
