declare module '@capacitor/core' {
  interface PluginRegistry {
    BarcodeScannerPlugin: BarcodeScannerPluginPlugin;
  }
}

export interface BarcodeScannerPluginPlugin {
  prepare(): Promise<void>;
  hideBackground(): Promise<void>;
  showBackground(): Promise<void>;
  startScan(): Promise<{ hasContent: boolean; content: string }>;
  stopScan(): Promise<void>;
}
