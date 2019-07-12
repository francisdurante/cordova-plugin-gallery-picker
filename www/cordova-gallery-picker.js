var exec = require('cordova/exec');

exports.showImagePicker = function (arg0,success, error) {
    exec(success, error, 'callImagePicker', 'coolMethod', [arg0]); // arg0- if comment or post
};
