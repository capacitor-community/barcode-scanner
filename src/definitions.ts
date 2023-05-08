import type { PermissionState } from '@capacitor/core';

export type CameraPermissionState = PermissionState | 'limited';

export type CameraPermissionType = 'camera' | 'photos';

export interface PermissionStates {
  camera: CameraPermissionState;
  // preparation for a future version of the plugin
  // photos: CameraPermissionState;
}

export interface CameraPluginPermissions {
  permissions: CameraPermissionType[];
}

export interface BarcodeScannerPlugin {
  // related to scanning
  /**
   * Start scanning for barcodes
   *
   * @since 4.0.0
   */
  start(options?: ScanOptions, callback?: (result: ScanResult, err?: any) => void): Promise<CallbackID>;

  /**
   * Pause scanning for barcodes
   *
   * @since 4.0.0
   */
  pause(): Promise<void>;

  /**
   * Resume paused scanning for barcodes
   *
   * @since 4.0.0
   */
  resume(): Promise<void>;

  /**
   * Stop scanning for barcodes
   *
   * @since 4.0.0
   */
  stop(): Promise<void>;

  // related to permissions
  /**
   * Check camera permissions
   *
   * @since 3.0.0
   */
  checkPermissions(): Promise<PermissionStates>;

  /**
   * Request camera permissions
   *
   * @since 3.0.0
   */
  requestPermissions(permissions?: CameraPluginPermissions): Promise<PermissionStates>;

  // TODO: readd this
  // openAppSettings(): Promise<void>;

  // related to torch
  /**
   * Enables torch
   *
   * @since 3.0.0
   */
  enableTorch(): Promise<void>;

  /**
   * Disables torch
   *
   * @since 3.0.0
   */
  disableTorch(): Promise<void>;

  /**
   * Toggles torch
   *
   * @since 3.0.0
   */
  toggleTorch(): Promise<void>;

  /**
   * Get current torch state
   *
   * @since 3.0.0
   */
  getTorchState(): Promise<TorchStateResult>;

  /**
   * Vibrates the device to indicate successful scan
   *
   * @since 4.0.0
   */
  vibrate(): Promise<void>;

  /**
   * Get current zoom, min and max zoom,
   * and if available the factors that trigger switching to another camera
   *
   * @since 4.0.0
   */
  getZoomState(): Promise<ZoomStateResult>;

  /**
   * Set the zoom
   *
   * @since 4.0.0
   */
  setZoom(zoomOptions: ZoomOptions): Promise<ZoomStateResult>;
}

export type CallbackID = string;

export enum BarcodeFormat {
  ALL = 'ALL',
  CODE_128 = 'CODE_128',
  CODE_39 = 'CODE_39',
  CODE_93 = 'CODE_93',
  CODA_BAR = 'CODA_BAR',
  DATA_MATRIX = 'DATA_MATRIX',
  EAN_13 = 'EAN_13',
  EAN_8 = 'EAN_8',
  ITF = 'ITF',
  QR_CODE = 'QR_CODE',
  UPC_A = 'UPC_A',
  UPC_E = 'UPC_E',
  PDF_417 = 'PDF_417',
  AZTEC = 'AZTEC',
}

// TODO: Maybe it would make sense to rename this to CameraType and allow additional settings like "WIDE_ANGLE_CAMERA"?
export enum CameraDirection {
  FRONT = 'front',
  BACK = 'back',
}

export interface ScanOptions {
  /**
   * This parameter can be used to make the scanner only recognize a specific type of barcode.
   *  If `format` is not specified or left empty, all types of barcodes will be targeted.
   *
   * @since 4.0.0
   */
  formats?: BarcodeFormat[];
  /**
   * This parameter can be used to set the camera direction.
   *
   * @since 4.0.0
   */
  cameraDirection?: CameraDirection;

  zoom?: number;
}

export interface ScanResult {
  /**
   * Content of the barcode
   *
   * @since 4.0.0
   */
  content: string;

  /**
   * Format of the scanned barcode
   *
   * @since 4.0.0
   */
  format: BarcodeFormat;

  /**
   * Type of the returned barcodes content
   *
   * @since 4.0.0
   */
  contentType: string;

  /**
   * Corner points of the barcode in the image, array of x and y coordinates in pixels
   *
   * @since 4.0.0
   */
  cornerPoints: [[number, number], [number, number], [number, number], [number, number]];
}

export interface TorchStateResult {
  /**
   * Whether or not the torch is currently enabled.
   *
   * @since 3.0.0
   */
  isEnabled: boolean;
}

export interface ZoomStateResult {
  /**
   * Current zoom
   */
  zoom: number;

  minimum: number;
  maximum: number;
  switchOver?: number[];
}

export interface ZoomOptions {
  zoom: number;
}
