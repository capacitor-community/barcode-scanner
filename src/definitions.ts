declare module '@capacitor/core' {
  interface PluginRegistry {
    CodeScannerPlugin: CodeScannerPluginPlugin;
  }
}

export interface CodeScannerPluginPlugin {
  prepare(): Promise<void>;
  hideBackground(): Promise<void>;
  showBackground(): Promise<void>;
  startScan(): Promise<{ hasContent: boolean; content: string }>;
  stopScan(): Promise<void>;
}
