import { registerPlugin } from '@capacitor/core';

import type { BarcodeScannerPlugin } from './definitions';

const BarcodeScanner = registerPlugin<BarcodeScannerPlugin>('CapacitorCommunityBarcodeScanner', {
  web: () => import('./web').then((m) => new m.BarcodeScannerWeb()),
});

export * from './definitions';
export { BarcodeScanner };
