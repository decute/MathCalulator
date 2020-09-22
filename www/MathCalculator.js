var MathCalculator = {

    findUsbDevices : function (success, error) {
        cordova.exec(success, error, 'MathCalculator', 'findUsbDevices', []);
    },
    isUsbDeviceConnected : function (success, error) {
        cordova.exec(success, error, 'MathCalculator', 'isUsbDeviceConnected', []);
    },
    setUsbDevice : function (arg0, success, error) {
        cordova.exec(success, error, 'MathCalculator', 'setUsbDevice', [arg0]);
    },
    getUsbDevice : function (success, error) {
        cordova.exec(success, error, 'MathCalculator', 'getUsbDevice', []);
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
    getAscan : function (success, error) {
        cordova.exec(success, error, 'MathCalculator', 'getAscan', []);
    },
    getContiniousAscan : function (success, error) {
        cordova.exec(success, error, 'MathCalculator', 'getContiniousAscan', []);
    } 
  };

  MathCalculator.ERROR_CODES = {
    '0':'MATHCALCULATOR_NOT_INITIALIZED',
    '1':'MATHCALCULATOR_NOT_LISTENING'
  };

  module.exports = MathCalculator; 