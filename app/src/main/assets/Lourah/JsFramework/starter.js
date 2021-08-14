/* (c) 2018 - Lourah : jsFramework::launcher */
 var Lourah = {};
 (function(){
 var mainHandler = Packages.android.os.Handler(
 Packages.android.os.Looper
 .getMainLooper());
 Lourah.jsFramework = new (function(){
  this.getRhinoVersion = () => '@@@RHINO_VERSION@@@';
  this.getGenerated = () => '@@@GENERATED@@@';
  this.name = () => '@@@JS_APP_NAME@@@';
  this.root = () => '@@@EXTERNAL_STORAGE_DIRECTORY@@@';
  this.parentDir = () => (
     this.root() + '/@@@JS_FRAMEWORK_DIRECTORY@@@'
     );
  this.dir = () => (
     this.parentDir() + '/' + this.name()
     );
  this.mainThread = function(f){
    mainHandler.post(new java.lang.Runnable({
     run: f
     }));
 };
  this.uiThread = function(f){
    Activity.runOnUiThread(new java.lang.Runnable({
     run: f
     }));
 };
  this.createThread = function(f){
    return new java.lang.Thread(new java.lang.Runnable({
     run: f
     }));
 };
 var backButtonListener = null;
 this.setOnBackButtonListener = function(listener) {
  backButtonListener = listener;
 };
 this.onBackPressed = function() {
  if(backButtonListener) {
   return backButtonListener();
  }
  return true;
 };
 var androidOnHandler = {};
 this.getAndroidOnHandler = function(event) {
  if(!androidOnHandler) return {};
  return androidOnHandler[event];
 };
 this.setAndroidOnHandlers = function(handler) {
  androidOnHandler = handler
 };
 this.setAndroidOnHandler = function(onEvent, fHandler) {
  androidOnHandler[onEvent] = fHandler
 };
 })();
 @@@SCRIPT@@@
 })();