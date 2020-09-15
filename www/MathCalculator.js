var exec = require('cordova/exec');

module.exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'MathCalculator', 'coolMethod', [arg0]);
};

module.exports.add = function (arg0, success, error) {
    exec(success, error, 'MathCalculator', 'add', [arg0]);
};

//This will give information about the list of device connected too
module.exports.findUsbDevices = function (success, error) {
    exec(success, error, 'MathCalculator', 'findUsbDevices', []);
};

//with the help of productId and VendorID , usb connection will be setup
module.exports.setUsbDevice = function (arg0, success, error) {
    exec(success, error, 'MathCalculator', 'setUsbDevice', [arg0]);
};

//This method will help us to close all the connection.
module.exports.openUsbConnection = function (success, error) {
    exec(success, error, 'MathCalculator', 'openUsbConnection', []);
};

//This method will help us to close all the connection.
module.exports.closeUsbConnection = function (success, error) {
    exec(success, error, 'MathCalculator', 'closeUsbConnection', []);
};


//with the help of productId and VendorID , send Remote command
module.exports.sendCommand = function (arg0, success, error) {
    exec(success, error, 'MathCalculator', 'sendCommand', [arg0]);
};

//with the help of productId and VendorID , send Remote command
module.exports.sendCommandAndWaitResponse = function (arg0, success, error) {
    exec(success, error, 'MathCalculator', 'sendCommandAndWaitResponse', [arg0]);
};

module.exports.getUsbDevice = function (success, error) {
    exec(success, error, 'MathCalculator', 'getUsbDevice', []);
};

//This will give information about whether device is attached or detached
module.exports.isUsbDeviceConnected = function (success, error) {
    exec(success, error, 'MathCalculator', 'isUsbDeviceConnected', []);
};


