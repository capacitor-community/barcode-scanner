export default {
  input: 'dist/esm/index.js',
  output: [
    {
      file: 'dist/plugin.js',
      format: 'iife',
      name: 'capacitorExample',
      globals: {
        '@capacitor/core': 'capacitorExports',
        '@zxing/browser': 'zxingBrowser',
        '@zxing/library': 'zxingLibrary',
      },
      sourcemap: true,
      inlineDynamicImports: true,
    },
    {
      file: 'dist/plugin.cjs.js',
      format: 'cjs',
      sourcemap: true,
      inlineDynamicImports: true,
    },
  ],
  external: ['@capacitor/core', '@zxing/browser', '@zxing/library'],
};
