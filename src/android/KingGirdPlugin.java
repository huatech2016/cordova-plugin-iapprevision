package cn.jpush.phonegap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.jpush.android.api.BasicPushNotificationBuilder;

import cn.jpush.android.api.TagAliasCallback;
import cn.jpush.android.data.JPushLocalNotification;

public final String copyRight = "SxD/phFsuhBWZSmMVtSjKZmm/c/3zSMrkV2Bbj5tznSkEVZmTwJv0wwMmH/+p6wLiUHbjadYueX9v51H9GgnjUhmNW1xPkB++KQqSv/VKLDsR8V6RvNmv0xyTLOrQoGzAT81iKFYb1SZ/Zera1cjGwQSq79AcI/N/6DgBIfpnlwiEiP2am/4w4+38lfUELaNFry8HbpbpTqV4sqXN1WpeJ7CHHwcDBnMVj8djMthFaapMFm/i6swvGEQ2JoygFU368sLBQG57FhM8Bkq7aPAVn7PKydRw7TZH+uYuUQlXrXx6aD2yiupr6ji7hzsE6/QRx89Izb7etgW5cXVl5PwIXUfHGed2lVzLEUESbPiW9qVWEbHWAH22+t7LdPt+jENSQiA7dkr82tbPKBUosdwVg5HQMh2MCeMcObMffaVFDaSgCNRP4FpYjl8hG/IVrYXNDSPCFphBt45dwskoiXK6OW8fXpxdRHfEuWC1PB9ruQ=";
public final String url = "http://10.15.51.90:9001/slt/iWebRevisionServer.jsp";
public final int REVISION_MODE_SIGN = 1; //手写签批
public final int REVISION_MODE_WORD = 2; //文字签批
public final int REVISION_MODE_MIX = 3;  //混合签批
public final int REVISION_MODE_MIX_SIGN = 4;  //混合签批
public final int REVISION_MODE_MIX_WORD = 5;  //混合签批

public class KingGirdPlugin extends CordovaPlugin {
    private final static List<String> methodList =
            Arrays.asList(
                    "addLocalNotification",

            );
	public static iAppRevision_iWebRevisionEx webRevisionEx;  //与iWebRevisionEx兼容操作实例

    private ExecutorService threadPool = Executors.newFixedThreadPool(1);
    private static KingGirdPlugin instance;
    private static Activity cordovaActivity;
    private static String TAG = "KingGirdPlugin";
    public KingGirdPlugin() {
        instance = this;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        Log.i(TAG, "JPush initialize.");
        super.initialize(cordova, webView);
        cordovaActivity = cordova.getActivity();
        if(webRevisionEx == null){
            webRevisionEx = new iAppRevision_iWebRevisionEx();

        }


    }


/**
	 * 获取iAppRevision操作实例
	 * @param url
	 * @param userName
	 * @return
	 */
	protected iAppRevision initRevisionInstance(String url,String userName){

		if(url.endsWith("iWebRevisionEx/iWebServer.jsp")){ //与iWebRevisionEx兼容
			webRevisionEx.isDebug = true;
			if(webRevisionEx == null){
				webRevisionEx = new iAppRevision_iWebRevisionEx();
				webRevisionEx.setCopyRight(this, copyRight,userName);
			}
			return webRevisionEx;

		}
		return null;
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        cordovaActivity = null;
        instance = null;
    }



    @Override
    public boolean execute(final String action, final JSONArray data,
                           final CallbackContext callbackContext) throws JSONException {
        if (!methodList.contains(action)) {
            return false;
        }
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Method method = KingGirdPlugin.class.getDeclaredMethod(action,
                            JSONArray.class, CallbackContext.class);
                    method.invoke(KingGirdPlugin.this, data, callbackContext);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
        return true;
    }
    void init(JSONArray data, CallbackContext callbackContext) {
        JPushInterface.init(this.cordova.getActivity().getApplicationContext());
    }





    /**
     * 用于 Android 6.0 以上系统申请权限，具体可参考：
     * http://docs.Push.io/client/android_api/#android-60
     */
    void requestPermission(JSONArray data, CallbackContext callbackContext) {
        JPushInterface.requestPermission(this.cordova.getActivity());
    }

    private final TagAliasCallback mTagWithAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            if (instance == null) {
                return;
            }
            JSONObject data = new JSONObject();
            try {
                data.put("resultCode", code);
                data.put("tags", tags);
                data.put("alias", alias);
                final String jsEvent = String.format(
                        "cordova.fireDocumentEvent('jpush.setTagsWithAlias',%s)",
                        data.toString());
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        instance.webView.loadUrl("javascript:" + jsEvent);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean hasPermission(String appOpsServiceId) {
        Context context = cordova.getActivity().getApplicationContext();
        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();

        String pkg = context.getPackageName();
        int uid = appInfo.uid;
        Class appOpsClazz = null;

        try {
            appOpsClazz = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClazz.getMethod("checkOpNoThrow",
                    Integer.TYPE, Integer.TYPE, String.class);
            Field opValue = appOpsClazz.getDeclaredField(appOpsServiceId);
            int value = opValue.getInt(Integer.class);
            Object result = checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg);

            return Integer.parseInt(result.toString()) == AppOpsManager.MODE_ALLOWED;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

}
