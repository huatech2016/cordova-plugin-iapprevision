var IAppRevisionPlugin = function () {}

IAppRevisionPlugin.prototype.isPlatformIOS = function () {
    var isPlatformIOS = device.platform == 'iPhone'
        || device.platform == 'iPad'
        || device.platform == 'iPod touch'
        || device.platform == 'iOS'
    return isPlatformIOS
}

IAppRevisionPlugin.prototype.error_callback = function (msg) {
    console.log('Javascript Callback Error: ' + msg)
}

IAppRevisionPlugin.prototype.call_native = function (success,error,name, args ) {
    ret = cordova.exec(success, this.error_callback, 'IAppRevisionPlugin', name, args)
    return ret
}

// public methods
// IAppRevisionPlugin.prototype.init = function () {
//   if (this.isPlatformIOS()) {
//     var data = []
//     this.call_native('initial', data, null)
//   } else {
//     data = []
//     this.call_native('init', data, null)
//   }
// }

IAppRevisionPlugin.prototype.getRevisionInfoFromNet = function (success,recordId,user_name) {
    if (device.platform == 'Android') {
        this.call_native(success,this.error_callback,'getRevisionInfoFromNet', [recordId, user_name])
    }
}

if (!window.plugins) {
    window.plugins = {}
}

if (!window.plugins.jPushPlugin) {
    window.plugins.iAppRevisionPlugin = new IAppRevisionPlugin()
}

module.exports = new IAppRevisionPlugin()
