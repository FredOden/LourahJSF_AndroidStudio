/**
 * JsFramework
 */

package com.lourah.android.lourahjsf;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
//import com.lourah.jsframework.JsFramework;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class JsFramework
    extends Activity /* implements ActivityCompat.OnRequestPermissionsResultCallback */ {

  private View contentView;
  private Js js;

  public View getContentView() {
    return contentView;
  }
  
  public TextView tv;
  public Activity a;

  private void createContentView() {
    ScrollView sv = new ScrollView(this);
    sv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    LinearLayout ll = new LinearLayout(this);
    ll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    ll.setOrientation(LinearLayout.VERTICAL);
    ll.setBackgroundColor(0x7f7fffff);
    sv.addView(ll);
    contentView = sv;
    tv = new TextView(this);
        tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    //ll.addView(b);
    
    
    File fJsFramework = new File(Environment.getExternalStorageDirectory().toString() + "/LourahJsFramework");
    File[] files = fJsFramework.listFiles();
    try {
      for(File file : files) {
        if (file.isDirectory()) {
           final File index = new File(file.getAbsolutePath()
                                 + "/index.js"
                                 );
           if (index.exists() && index.isFile()) {
             Button bIndex = new Button(this);
             bIndex.setText(file.getName());
             
               
             ll.addView(bIndex);
             bIndex.setOnClickListener(
                new View.OnClickListener() {
                  public void onClick(View view) {
                    String indexPath = Environment.getExternalStorageDirectory()
                            + "/LourahJsFramework"
                            + "/"
                            + ((Button) view).getText()
                            + "/"
                            + "index.js"
                    ;
                    try {
                      HashMap<String, String> starterMacros = new HashMap<>();
                      starterMacros.put("@@@JS_APP_NAME@@@", ((Button)view).getText().toString());
                      starterMacros.put("@@@EXTERNAL_STORAGE_DIRECTORY@@@", Environment.getExternalStorageDirectory().toString());
                      // jsFrameworkDirectory to be configurable in a future version ?
                      starterMacros.put("@@@JS_FRAMEWORK_DIRECTORY@@@", "LourahJsFramework");
                      String script = path2String(indexPath);
                      starterMacros.put("@@@SCRIPT@@@", script);
                      String starter = asset2String("Lourah/JsFramework/starter.js");
                      for(String k : starterMacros.keySet()) {
                        starter = starter.replace(k, starterMacros.get(k));
                      }
                      Js.JsObject o =
                              js.eval(starter, indexPath);
                      if(!o.ok) {
                        reportError("LourahJsFramework::Loading::"
                        + ((Button)view).getText()
                        + "::"
                        + o.s
                        );
                      }
                    } catch(Exception e) {
                      reportError("LourahJsFramework::starter::exception::"
                              + ((Button)view).getText()
                              + "::"
                              + e.getMessage()
                      );
                    }
                  }
                }
             );
           }
        }
      }
    } catch(Exception e) {
      reportError("cannot list JsFramework::" + e);
    }
    
    
    ll.addView(tv);
    
    a = this;
    
  }
  
  
  public interface ErrorReporter {
     void report(String m);
  }
  
  private JsFramework self = this;
  
  public ErrorReporter errorReporter = new ErrorReporter() {
    public void report(String m) {
      Toast.makeText(self, m, Toast.LENGTH_LONG)
    .show();
      android.util.Log.e("LourahJsFramework", m);
    }
  };
  
  public void reportError(String error) {
    final String m = error;
    runOnUiThread(new Runnable(){
      @Override
      public void run() {
        errorReporter.report("reportError::" + m);
      }
    });
  }
  
  public void reportWarning(String warning) {
    
  }


  static boolean first = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (first) {
      createContentView();
      setContentView(contentView);
      js = new Js(this);
      //first = false;
    } else {
      setContentView(getContentView());
    }
  }

  //public static Charset encoding = StandardCharsets.UTF_8;
  public static File root = null;
  
  public String inputStream2String(InputStream is) {
    StringBuffer sb = new StringBuffer();
    try {
       BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
       int c = 0;
       while((c = br.read()) != -1) {
         sb.append((char)c);
       }
       br.close();
       
    } catch (Exception ioe) {
     reportError("inputStream2string::" + ioe);
    } 
    return sb.toString();
  }

  public String asset2String(String asset) {
    String ret = "";
    try {
     ret = inputStream2String(getAssets().open(asset));
     } catch(IOException ioe) {
       reportError("asset2String::" +ioe);
     }
     return ret;
  }

  public String path2String(String path) {
    try {
      File f = new File(path);
      return inputStream2String(new FileInputStream(f));
    } catch (IOException ioe) {
      return ioe.getMessage();
    }
  }

  @Deprecated
  public String file2String_deprecated(String filename) {
    
    if (root == null) root = Environment.getExternalStorageDirectory();
    return path2String(root + filename);
  }

  public Js getJs() {
    return js;
  }


  public Js.JsObject importScript(String scriptName) {
    Js.JsObject o;
    o = js.eval(path2String(scriptName), scriptName);
    if (!o.ok) {
      reportError("importScript::" + o.s);
    }
    return o;
  }

  private static boolean checkPermission = true;
  private AlertDialog permissionDialog;

  @Override
  public void onResume() {
    super.onResume();
    if (checkPermission) {
      checkPermission = false;
      try {
        askForPermissions();
      } catch(Exception e) {
        tv.setText(e.getMessage() + stringifyStackTrace(e));
      }
    }
    androidHandler("onResume");
  }

  @Override
  public void onStop() {
    super.onStop();
    //reportError("ON STOP");
    androidHandler("onStop");
  }
  
  @Override
  public void onDestroy(){
    super.onDestroy();
    androidHandler("onDestroy");
  }
  
  @Override
  public void onPause() {
    super.onPause();
    androidHandler("onPause");
  }
  
  @Override
  public void onStart() {
    super.onStart();
    androidHandler("onStart");
  }
  
  @Override
  public void onRestart() {
    super.onRestart();
    androidHandler("onRestart");
  }
  
  @Override
  public void onBackPressed() {
    String script = "Lourah.jsFramework.onBackPressed();";
    Js.JsObject o =
           js.eval(script, "JsFramework.java");
           if(!o.ok) {
                reportError("LourahJsFramework::onBackPressed::"
                    + o.s
                   );
                return;
           }
    if (o.s.equals("false")) {
      super.onBackPressed();
    }
  }
  
  protected void androidHandler(String onEvent) {
    String script = "(function() {try {var handler = (Lourah !== undefined)?Lourah.jsFramework.getAndroidOnHandler('" + onEvent + "'):undefined;"
           + "if (handler !== undefined) { handler(); }} catch(e){}})()";
    Js.JsObject o =
           js.eval(script, "JsFramework.java");
           if(!o.ok) {
                reportError("LourahJsFramework::androidHandler::"
                    + onEvent + "::"
                    + o.s
                   );
                return;
           }
  }

  private String stringifyStackTrace(Exception e) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    return "{" + sw.toString() + "}";
  }

  protected void checkPermissions() {
    if (checkPermission && Build.VERSION.SDK_INT >= 23) {
      Activity activity = this; // /getParentActivity();
      if (activity != null) {
        checkPermission = false;
        if (activity.checkSelfPermission(Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED
            || activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
          if (activity.shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("LourahJsFramework");
            builder.setMessage("Grant Internet");
            builder.setPositiveButton("Grant", null);
            permissionDialog = builder.create();
            permissionDialog.show();
          } else if (activity.shouldShowRequestPermissionRationale(
              Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("LourahJsFramework");
            builder.setMessage("Grant writeExternalStorage");
            builder.setPositiveButton("Grant", null);
            permissionDialog = builder.create();
            permissionDialog.show();
          } else {
            askForPermissions();
          }
        }
      }
    }
  }

  // @TargetApi(Build.VERSION_CODES.M)
  @SuppressLint("NewApi")
  private void askForPermissions() {
    ArrayList<String> permissions = new ArrayList<>();
    //if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
      //  != PackageManager.PERMISSION_GRANTED) {
      permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
      permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
      permissions.add(Manifest.permission.INTERNET);
      permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
      permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
    //}
    String[] items = permissions.toArray(new String[permissions.size()]);
    requestPermissions(items, 100);
  }

  
  @Override
  public void onRequestPermissionsResult(
    int requestCode,
    String permissions[],
    int [] grantResults
  ) {
    String s = "";
    switch(requestCode) {
      case 100:
           for(int i = 0; i < grantResults.length; i++){
              s += permissions[i] +":" +
             ((grantResults[i] == PackageManager.PERMISSION_GRANTED)
             ?"ok":"ko") + "\n";
           } 
           tv.setText(s);
           
           break;
    }
    
  }
  
  /*
  @Override
  protected void onDialogDismiss(Dialog dialog) {
      super.onDialogDismiss(dialog);
      if (permissionDialog != null && dialog == permissionDialog && this != null) {
          askForPermissons();
      }
  }
  */

}
