/*
    Where globals are set for all applications
*/
var gLourah = {};
(function () {
    // TO BE SPECIFIED
    gLourah.packageName = Activity.getPackageName();
    gLourah.packageInfo = Activity.getPackageManager().getPackageInfo(gLourah.packageName, 0);
    gLourah.applications = function() {
       var jsFrameworkDirectory = '@@@JS_FRAMEWORK_DIRECTORY@@@';

    }
})();