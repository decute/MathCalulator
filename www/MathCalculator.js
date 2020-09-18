var MathCalculator = {

    coolMethod: function (arg0, success, error) {
        cordova.exec(
            success, 
            error, 
            'MathCalculator', 
            'coolMethod', 
            [arg0]);
    },
    add : function (arg0, success, error) {
        cordova.exec(success, error, 'MathCalculator', 'add', [arg0]);
    },
    findUsbDevices : function (success, error) {
        cordova.exec(success, error, 'MathCalculator', 'findUsbDevices', []);
    },
    setUsbDevice : function (arg0, success, error) {
        cordova.exec(success, error, 'MathCalculator', 'setUsbDevice', [arg0]);
    },
    openUsbConnection : function (success, error) {
        cordova.exec(success, error, 'MathCalculator', 'openUsbConnection', []);
    },  
    closeUsbConnection : function (success, error) {
        cordova.exec(success, error, 'MathCalculator', 'closeUsbConnection', []);
    },
    sendCommand : function (arg0, success, error) {
        cordova.exec(success, error, 'MathCalculator', 'sendCommand', [arg0]);
    },
    sendCommandAndWaitResponse : function (arg0, success, error) {
        cordova.exec(success, error, 'MathCalculator', 'sendCommandAndWaitResponse', [arg0]);
    },
    getUsbDevice : function (success, error) {
        cordova.exec(success, error, 'MathCalculator', 'getUsbDevice', []);
    },
    isUsbDeviceConnected : function (success, error) {
        cordova.exec(success, error, 'MathCalculator', 'isUsbDeviceConnected', []);
    },
    openAscanUsbConnection : function (success, error) {
        cordova.exec(success, error, 'MathCalculator', 'openAscanUsbConnection', []);
    },
    getAscan : function (success, error) {
        cordova.exec(success, error, 'MathCalculator', 'getAscan', []);
    },
    testFunction : function (success, error) {
        cordova.exec(success, error, 'MathCalculator', 'testFunction', []);
    },
    testThreadFunction : function (success, error) {
        cordova.exec(success, error, 'MathCalculator', 'testThreadFunction', []);
    }
  };
  
  MathCalculator.ERROR_CODES = {
    '0':'MATHCALCULATOR_NOT_INITIALIZED',
    '1':'MATHCALCULATOR_NOT_LISTENING'
  };
  
  module.exports = MathCalculator;