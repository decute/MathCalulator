var exec = require('cordova/exec');

module.exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'MathCalculator', 'coolMethod', [arg0]);
};

module.exports.add = function (arg0, success, error) {
    exec(success, error, 'MathCalculator', 'add', [arg0]);
};

module.exports.findUsbDevices = function (success, error) {
    exec(success, error, 'MathCalculator', 'findUsbDevices', []);
};

module.exports.setUsbDevice = function (arg0, success, error) {
    exec(success, error, 'MathCalculator', 'setUsbDevice', [arg0]);
};

module.exports.openUsbConnection = function (success, error) {
    exec(success, error, 'MathCalculator', 'openUsbConnection', []);
};

module.exports.closeUsbConnection = function (success, error) {
    exec(success, error, 'MathCalculator', 'closeUsbConnection', []);
};


module.exports.sendCommand = function (arg0, success, error) {
    exec(success, error, 'MathCalculator', 'sendCommand', [arg0]);
};

module.exports.sendCommandAndWaitResponse = function (arg0, success, error) {
    exec(success, error, 'MathCalculator', 'sendCommandAndWaitResponse', [arg0]);
};

module.exports.getUsbDevice = function (success, error) {
    exec(success, error, 'MathCalculator', 'getUsbDevice', []);
};

module.exports.isUsbDeviceConnected = function (success, error) {
    exec(success, error, 'MathCalculator', 'isUsbDeviceConnected', []);
};

module.exports.openAscanUsbConnection = function (success, error) {
    exec(success, error, 'MathCalculator', 'openAscanUsbConnection', []);
};

module.exports.getAscan = function (success, error) {
    exec(success, error, 'MathCalculator', 'getAscan', []);
};

module.exports.testFunction = function (success, error) {
    exec(success, error, 'MathCalculator', 'testFunction', []);
};

module.exports.testThreadFunction = function (success, error) {
    exec(success, error, 'MathCalculator', 'testThreadFunction', []);
};



