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
  checkPermission(
    options: CheckPermissionOptions,
  ): Promise<CheckPermissionResult>;
  openAppSettings(): Promise<void>;
}

export interface ScanResult {
  hasContent: boolean;
  content?: string;
}

export interface CheckPermissionOptions {
  force?: boolean;
}

export interface CheckPermissionResult {
  granted?: boolean;
  denied?: boolean;
  asked?: boolean;
  neverAsked?: boolean;
}
