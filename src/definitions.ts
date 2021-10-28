export type CallbackID = string;

export interface BarcodeScannerPlugin {
  prepare(): Promise<void>;
  hideBackground(): Promise<void>;
  showBackground(): Promise<void>;
  startScan(options?: ScanOptions): Promise<ScanResult>;
  stopScan(options?: StopScanOptions): Promise<void>;
  checkPermission(
    options?: CheckPermissionOptions,
  ): Promise<CheckPermissionResult>;
  openAppSettings(): Promise<void>;
  startScanning(
    options?: ScanOptions,
    callback?: (result: ScanResult, err?: any) => void,
  ): Promise<CallbackID>;
  pauseScanning(): Promise<void>;
  resumeScanning(): Promise<void>;
}

export enum SupportedFormat {
  // BEGIN 1D Product
  /**
   * Android only, UPC_A is part of EAN_13 according to Apple docs
   */
  UPC_A = 'UPC_A',

  UPC_E = 'UPC_E',

  /**
   * Android only
   */
  UPC_EAN_EXTENSION = 'UPC_EAN_EXTENSION',

  EAN_8 = 'EAN_8',

  EAN_13 = 'EAN_13',
  // END 1D Product

  // BEGIN 1D Industrial
  CODE_39 = 'CODE_39',

  /**
   * iOS only
   */
  CODE_39_MOD_43 = 'CODE_39_MOD_43',

  CODE_93 = 'CODE_93',

  CODE_128 = 'CODE_128',

  /**
   * Android only
   */
  CODABAR = 'CODABAR',

  ITF = 'ITF',

  /**
   * iOS only
   */
  ITF_14 = 'ITF_14',
  // END 1D Industrial

  // BEGIN 2D
  AZTEC = 'AZTEC',

  DATA_MATRIX = 'DATA_MATRIX',

  /**
   * Android only
   */
  MAXICODE = 'MAXICODE',

  PDF_417 = 'PDF_417',

  QR_CODE = 'QR_CODE',

  /**
   * Android only
   */
  RSS_14 = 'RSS_14',

  /**
   * Android only
   */
  RSS_EXPANDED = 'RSS_EXPANDED',
  // END 2D
}

export interface ScanOptions {
  /**
   * This parameter can be used to make the scanner only recognize specific types of barcodes.
   *  If `targetedFormats` is _not specified_ or _left empty_, _all types_ of barcodes will be targeted.
   *
   * @since 1.2.0
   */
  targetedFormats?: SupportedFormat[];
}

export interface StopScanOptions {
  /**
   * If this is set to `true`, the `startScan` method will resolve.
   * Additionally `hasContent` will be `false`.
   * For more information see: https://github.com/capacitor-community/barcode-scanner/issues/17
   *
   * @default true
   * @since 2.1.0
   */
  resolveScan?: boolean;
}

export interface ScanResult {
  /**
   * This indicates whether or not the scan resulted in readable content.
   * When stopping the scan with `resolveScan` set to `true`, for example,
   * this parameter is set to `false`, because no actual content was scanned.
   *
   * @since 1.0.0
   */
  hasContent: boolean;

  /**
   * This holds the content of the barcode if available.
   *
   * @since 1.0.0
   */
  content?: string;
}

export interface CheckPermissionOptions {
  /**
   * If this is set to `true`, the user will be prompted for the permission.
   * The prompt will only show if the permission was not yet granted and also not denied completely yet.
   * For more information see: https://github.com/capacitor-community/barcode-scanner#permissions
   *
   * @default false
   * @since 1.0.0
   */
  force?: boolean;
}

export interface CheckPermissionResult {
  /**
   * When set to `true`, the ermission is granted.
   */
  granted?: boolean;

  /**
   * When set to `true`, the permission is denied and cannot be prompted for.
   * The `openAppSettings` method should be used to let the user grant the permission.
   *
   * @since 1.0.0
   */
  denied?: boolean;

  /**
   * When this is set to `true`, the user was just prompted the permission.
   * Ergo: a dialog, asking the user to grant the permission, was shown.
   *
   * @since 1.0.0
   */
  asked?: boolean;

  /**
   * When this is set to `true`, the user has never been prompted the permission.
   *
   * @since 1.0.0
   */
  neverAsked?: boolean;

  /**
   * iOS only
   * When this is set to `true`, the permission cannot be requested for some reason.
   *
   * @since 1.0.0
   */
  restricted?: boolean;

  /**
   * iOS only
   * When this is set to `true`, the permission status cannot be retrieved.
   *
   * @since 1.0.0
   */
  unknown?: boolean;
}
