import { WebPlugin } from '@capacitor/core';
import { BarcodeScannerPluginPlugin } from './definitions';

export class BarcodeScannerPluginWeb
  extends WebPlugin
  implements BarcodeScannerPluginPlugin {
  constructor() {
    super({
      name: 'BarcodeScannerPlugin',
      platforms: ['web'],
    });
  }
}

const BarcodeScannerPlugin = new BarcodeScannerPluginWeb();

export { BarcodeScannerPlugin };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(BarcodeScannerPlugin);
