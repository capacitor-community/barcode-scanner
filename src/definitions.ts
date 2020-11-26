declare module '@capacitor/core' {
  interface PluginRegistry {
    BarcodeScanner: BarcodeScannerPlugin;
  }
}

export interface BarcodeScannerPlugin {
  prepare(): Promise<void>;
  hideBackground(): Promise<void>;
  showBackground(): Promise<void>;
  startScan(): Promise<{ hasContent: boolean; content: string }>;
  stopScan(): Promise<void>;
}
