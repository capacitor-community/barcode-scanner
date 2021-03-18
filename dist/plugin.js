var capacitorPlugin = (function (exports, core) {
    'use strict';

    (function (SupportedFormat) {
        // 1D Product
        SupportedFormat["UPC_A"] = "UPC_A";
        SupportedFormat["UPC_E"] = "UPC_E";
        SupportedFormat["UPC_EAN_EXTENSION"] = "UPC_EAN_EXTENSION";
        SupportedFormat["EAN_8"] = "EAN_8";
        SupportedFormat["EAN_13"] = "EAN_13";
        // 1D Industrial
        SupportedFormat["CODE_39"] = "CODE_39";
        SupportedFormat["CODE_39_MOD_43"] = "CODE_39_MOD_43";
        SupportedFormat["CODE_93"] = "CODE_93";
        SupportedFormat["CODE_128"] = "CODE_128";
        SupportedFormat["CODABAR"] = "CODABAR";
        SupportedFormat["ITF"] = "ITF";
        SupportedFormat["ITF_14"] = "ITF_14";
        // 2D
        SupportedFormat["AZTEC"] = "AZTEC";
        SupportedFormat["DATA_MATRIX"] = "DATA_MATRIX";
        SupportedFormat["MAXICODE"] = "MAXICODE";
        SupportedFormat["PDF_417"] = "PDF_417";
        SupportedFormat["QR_CODE"] = "QR_CODE";
        SupportedFormat["RSS_14"] = "RSS_14";
        SupportedFormat["RSS_EXPANDED"] = "RSS_EXPANDED";
    })(exports.SupportedFormat || (exports.SupportedFormat = {}));

    var __awaiter = (undefined && undefined.__awaiter) || function (thisArg, _arguments, P, generator) {
        function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
        return new (P || (P = Promise))(function (resolve, reject) {
            function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
            function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
            function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
            step((generator = generator.apply(thisArg, _arguments || [])).next());
        });
    };
    class BarcodeScannerWeb extends core.WebPlugin {
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
    core.registerWebPlugin(BarcodeScanner);

    exports.BarcodeScanner = BarcodeScanner;
    exports.BarcodeScannerWeb = BarcodeScannerWeb;

    Object.defineProperty(exports, '__esModule', { value: true });

    return exports;

}({}, capacitorExports));
//# sourceMappingURL=plugin.js.map
