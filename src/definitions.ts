declare module '@capacitor/core' {
  interface PluginRegistry {
    BarcodeScanner: BarcodeScannerPlugin;
  }
}

export interface BarcodeScannerPlugin {
  prepare(): Promise<void>;
  hideBackground(): Promise<void>;
  showBackground(): Promise<void>;
  startScan(): Promise<ScanResult>;
  stopScan(): Promise<void>;
}

export interface ScanResult {
  hasContent: boolean;
  content?: string;
}
