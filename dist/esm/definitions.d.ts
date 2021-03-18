declare module '@capacitor/core' {
    interface PluginRegistry {
        BarcodeScanner: BarcodeScannerPlugin;
    }
}
export interface BarcodeScannerPlugin {
    prepare(): Promise<void>;
    hideBackground(): Promise<void>;
    showBackground(): Promise<void>;
    startScan(options: ScanOptions): Promise<ScanResult>;
    stopScan(): Promise<void>;
    enableTorch(): Promise<void>;
    disableTorch(): Promise<void>;
    checkPermission(options: CheckPermissionOptions): Promise<CheckPermissionResult>;
    openAppSettings(): Promise<void>;
}
export declare enum SupportedFormat {
    UPC_A = "UPC_A",
    UPC_E = "UPC_E",
    UPC_EAN_EXTENSION = "UPC_EAN_EXTENSION",
    EAN_8 = "EAN_8",
    EAN_13 = "EAN_13",
    CODE_39 = "CODE_39",
    CODE_39_MOD_43 = "CODE_39_MOD_43",
    CODE_93 = "CODE_93",
    CODE_128 = "CODE_128",
    CODABAR = "CODABAR",
    ITF = "ITF",
    ITF_14 = "ITF_14",
    AZTEC = "AZTEC",
    DATA_MATRIX = "DATA_MATRIX",
    MAXICODE = "MAXICODE",
    PDF_417 = "PDF_417",
    QR_CODE = "QR_CODE",
    RSS_14 = "RSS_14",
    RSS_EXPANDED = "RSS_EXPANDED"
}
export interface ScanOptions {
    targetedFormats?: [SupportedFormat];
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
    restricted?: boolean;
    unknown?: boolean;
}
