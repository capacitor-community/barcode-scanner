import { registerPlugin } from '@capacitor/core';
const BarcodeScanner = registerPlugin('BarcodeScanner', {
    web: () => import('./web').then(m => new m.BarcodeScannerWeb()),
});
export * from './definitions';
export { BarcodeScanner };
//# sourceMappingURL=index.js.map