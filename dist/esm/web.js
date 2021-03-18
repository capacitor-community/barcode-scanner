var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
import { registerWebPlugin, WebPlugin } from '@capacitor/core';
export class BarcodeScannerWeb extends WebPlugin {
    constructor() {
        super({
            name: 'BarcodeScanner',
            platforms: ['web'],
        });
    }
    prepare() {
        return __awaiter(this, void 0, void 0, function* () {
            throw new Error('method not available in web');
        });
    }
    hideBackground() {
        return __awaiter(this, void 0, void 0, function* () {
            throw new Error('method not available in web');
        });
    }
    showBackground() {
        return __awaiter(this, void 0, void 0, function* () {
            throw new Error('method not available in web');
        });
    }
    enableTorch() {
        return __awaiter(this, void 0, void 0, function* () {
            throw new Error('method not available in web');
        });
    }
    disableTorch() {
        return __awaiter(this, void 0, void 0, function* () {
            throw new Error('method not available in web');
        });
    }
    startScan(_options) {
        return __awaiter(this, void 0, void 0, function* () {
            throw new Error('method not available in web');
        });
    }
    stopScan() {
        return __awaiter(this, void 0, void 0, function* () {
            throw new Error('method not available in web');
        });
    }
    checkPermission(_options) {
        return __awaiter(this, void 0, void 0, function* () {
            throw new Error('method not available in web');
        });
    }
    openAppSettings() {
        return __awaiter(this, void 0, void 0, function* () {
            throw new Error('method not available in web');
        });
    }
}
const BarcodeScanner = new BarcodeScannerWeb();
export { BarcodeScanner };
registerWebPlugin(BarcodeScanner);
//# sourceMappingURL=web.js.map