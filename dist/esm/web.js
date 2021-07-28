import { WebPlugin } from '@capacitor/core';
export class BarcodeScannerWeb extends WebPlugin {
    async prepare() {
        throw this.unimplemented('Not implemented on web.');
    }
    async hideBackground() {
        throw this.unimplemented('Not implemented on web.');
    }
    async showBackground() {
        throw this.unimplemented('Not implemented on web.');
    }
    async startScan(_options) {
        throw this.unimplemented('Not implemented on web.');
    }
    async stopScan() {
        throw this.unimplemented('Not implemented on web.');
    }
    async checkPermission(_options) {
        throw this.unimplemented('Not implemented on web.');
    }
    async openAppSettings() {
        throw this.unimplemented('Not implemented on web.');
    }
}
//# sourceMappingURL=web.js.map