export var SupportedFormat;
(function (SupportedFormat) {
    // BEGIN 1D Product
    /**
     * Android only, UPC_A is part of EAN_13 according to Apple docs
     */
    SupportedFormat["UPC_A"] = "UPC_A";
    SupportedFormat["UPC_E"] = "UPC_E";
    /**
     * Android only
     */
    SupportedFormat["UPC_EAN_EXTENSION"] = "UPC_EAN_EXTENSION";
    SupportedFormat["EAN_8"] = "EAN_8";
    SupportedFormat["EAN_13"] = "EAN_13";
    // END 1D Product
    // BEGIN 1D Industrial
    SupportedFormat["CODE_39"] = "CODE_39";
    /**
     * iOS only
     */
    SupportedFormat["CODE_39_MOD_43"] = "CODE_39_MOD_43";
    SupportedFormat["CODE_93"] = "CODE_93";
    SupportedFormat["CODE_128"] = "CODE_128";
    /**
     * Android only
     */
    SupportedFormat["CODABAR"] = "CODABAR";
    SupportedFormat["ITF"] = "ITF";
    /**
     * iOS only
     */
    SupportedFormat["ITF_14"] = "ITF_14";
    // END 1D Industrial
    // BEGIN 2D
    SupportedFormat["AZTEC"] = "AZTEC";
    SupportedFormat["DATA_MATRIX"] = "DATA_MATRIX";
    /**
     * Android only
     */
    SupportedFormat["MAXICODE"] = "MAXICODE";
    SupportedFormat["PDF_417"] = "PDF_417";
    SupportedFormat["QR_CODE"] = "QR_CODE";
    /**
     * Android only
     */
    SupportedFormat["RSS_14"] = "RSS_14";
    /**
     * Android only
     */
    SupportedFormat["RSS_EXPANDED"] = "RSS_EXPANDED";
    // END 2D
})(SupportedFormat || (SupportedFormat = {}));
//# sourceMappingURL=definitions.js.map