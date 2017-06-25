module.exports = function (context) {
  var fs = require('fs'),
      path = require('path'),
      cordovaCommon = context.requireCordovaModule('cordova-common'),
      ConfigParser = cordovaCommon.ConfigParser;

  var appConfig = new ConfigParser(path.join(context.opts.projectRoot, 'config.xml'));

  var androidPlatform = path.join(context.opts.projectRoot, 'platforms/android/');

  var androidMainActivityFilePath = path.join(androidPlatform, 'src/' + appConfig.packageName().replace(/\./g, '/') + '/MainActivity.java');

  var contents = fs.readFileSync(androidMainActivityFilePath).toString();

  if (contents.indexOf('FirebaseApp.initializeApp(this);') != -1) {
    // Activity already contains firebase init code
    return;
  }

  contents = contents.replace(/;(\s+public class MainActivity)/, ';\nimport com.google.firebase.FirebaseApp;$1');

  contents = contents.replace(/(super\.onCreate\(savedInstanceState\);)/, '$1\n\n        FirebaseApp.initializeApp(this);');

  fs.writeFileSync(androidMainActivityFilePath, contents);
};
