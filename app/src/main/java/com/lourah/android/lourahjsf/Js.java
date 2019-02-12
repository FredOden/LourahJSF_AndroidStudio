package com.lourah.android.lourahjsf;

import android.app.Activity;
import android.content.res.AssetManager;
import java.io.IOException;
import java.text.MessageFormat;
//import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import org.mozilla.javascript.*;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.ScriptRuntime;

public class Js implements ErrorReporter {
  private Context cx;
  private Scriptable scope;
  private static JsFramework activity;
    
  private static class AssetMessageProvider implements ScriptRuntime.MessageProvider {
    @Override
    public String getMessage(String messageId, Object[] arguments) {
      AssetManager assetManager = activity.getAssets();
      try {
      ResourceBundle resourceBundle =
        new PropertyResourceBundle(
          assetManager.open("Messages.properties")
        );
        
      String formatString;
      
      try {
       formatString = resourceBundle.getString(messageId);
      } catch(java.util.MissingResourceException mre) {
        throw new RuntimeException(
        "AssetMessageProvider::no message resource for property::"
        + messageId
        );
      }
      
      MessageFormat formatter = new MessageFormat(formatString);
      
      return formatter.format(arguments);
      } catch(IOException e) {
        return "failed to load bundle::" + e.getMessage();
      }
    }
  }

  public Context getContext() {
    return cx;
  }

  public class JsObject {
    public boolean ok;
    public String s;
    public Object o;
    public Js js;

    public JsObject(Js js) {
      this.js = js;
    }
  }

  public Activity getActivity() {
    return activity;
  }

  public Js(Activity activity) {
    ScriptRuntime.messageProvider = new AssetMessageProvider();
    // @issue: must use this.activity non statically ...
    this.activity = (JsFramework)activity;
    cx = Context.enter();
    cx.setOptimizationLevel(-1);
    System.err.println(
        "Rhino version:"
            + org.mozilla.javascript.Context.getCurrentContext().getImplementationVersion());
    scope = this.cx.initStandardObjects();
    ScriptableObject.putProperty(scope, "Activity", Context.javaToJS(activity, scope));
    Context.getCurrentContext().setErrorReporter(this);
  }

  public void finalize() {
    Context.exit();
  }

  public JsObject eval(String s, String filename) {
    JsObject obj = new JsObject(this);
    try {
      obj.o = cx.evaluateString(scope, s, filename, 1, null);
      obj.ok = true;
      obj.s = Context.toString(obj.o);
      return obj;  
    } catch (Exception e) {
      obj.ok = false;
      obj.s = e.getMessage();
      obj.o = e;
      return obj;
    }
    
  }
  
  @Override
  public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
    activity.reportError("error::" + message + "::at " + sourceName + "::(" + line + ":" + lineOffset + ")::" + lineSource);
  }
  
  @Override
  public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
    activity.reportWarning("warning::" + message + "::at " + sourceName + "::(" + line + ":" + lineOffset + ")::" + lineSource);
  }

  @Override
  public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
    activity.reportError("runtime error::" + message + "::at " + sourceName + "::(" + line + ":" + lineOffset + ")::" + lineSource);
    return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
  }

}
