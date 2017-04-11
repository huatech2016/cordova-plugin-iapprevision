package com.kinggrid.plugin.iapprevisionplugin;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.kinggrid.iapprevision.iAppRevisionUtil;
import com.kinggrid.iapprevision.iAppRevisionView;
import com.kinggrid.plugin.iapprevisionplugin.view.RevisionGridDialog;
import com.kinggrid.plugin.iapprevisionplugin.OnFinishListener;
import com.kinggrid.plugin.iapprevisionplugin.view.RevisionNormalDialog;
//private final int MSG_WHAT_OPEN_DOC = 1; //打开DOC文档
private final int MSG_WHAT_OPEN_REVISION = 2; //手写签批
private final int MSG_WHAT_OPEN_WORD_REVISION = 3; //文字签批
private final int MSG_WHAT_OPEN_INTERSECT_REVISION = 4; //米字格签批
private final int MSG_WHAT_SAVEREVISIONSUCCESS = 5; //保存签批成功
private final int MSG_WHAT_SAVEREVISIONERROR = 6; //保存签批失败
public final int MSG_WHAT_GETREVISIONSUCCESS = 7; //获取签批成功
public final int MSG_WHAT_TOASTMESSAGE = 8;

public static iAppRevision_iWebRevisionEx webRevisionEx;  //与iWebRevisionEx兼容操作实例

public class IAppRevisionPlugin extends CordovaPlugin {
    class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case MSG_WHAT_OPEN_REVISION://手写签批
                    showRevisionDialog(REVISION_MODE_SIGN);
                    break;
                case MSG_WHAT_OPEN_WORD_REVISION: //文字签批
                    showRevisionDialog(REVISION_MODE_WORD);
                    break;
                case MSG_WHAT_OPEN_INTERSECT_REVISION:
                    RevisionGridDialog revisionWindowIntersected = new RevisionGridDialog(WebRevisionExActivity.this, copyRight,user_name,fieldName);
                    revisionWindowIntersected.showRevisionWindow();
                    revisionWindowIntersected.setOnFinishListener(new OnFinishListener() {

                        @Override
                        public void setOnFinish(iAppRevisionView revisionView,Bitmap bitmap, String sign_flag) {
                            if(bitmap != null){
                                saveRevisionToNet(url, recordId,user_name, fieldName, bitmap,sign_flag, "" + dm.density);
                            }
                        }
                    });
                    break;
                case MSG_WHAT_SAVEREVISIONSUCCESS:
                    if(save_revision_dialog != null){
                        save_revision_dialog.dismiss();
                    }
                    getRevisionInfoFromNet(recordId,user_name);
                    break;
                case MSG_WHAT_SAVEREVISIONERROR:
                    if(save_revision_dialog != null){
                        save_revision_dialog.dismiss();
                    }
                    Toast.makeText(WebRevisionExActivity.this, "保存签批失败，可能网络异常", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_WHAT_GETREVISIONSUCCESS:
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
//                    Bitmap bitmap = (Bitmap) msg.obj;
//                    if(bitmap != null){
//                        showImageToWebView(bitmap);
//                    } else {
//                        Log.e(TAG, "解析签批图片异常或者没有签批！");
//
//                    }
                    break;
                case MSG_WHAT_TOASTMESSAGE:
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
//				Toast.makeText(CommonActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
            }

        }

    }
    private MyHandler handler;
    private CallbackContext callbackContext;
    private String userName = "admin";
    private String copyRight = "SxD/phFsuhBWZSmMVtSjKZmm/c/3zSMrkV2Bbj5tznSkEVZmTwJv0wwMmH/+p6wLiUHbjadYueX9v51H9GgnjUhmNW1xPkB++KQqSv/VKLDsR8V6RvNmv0xyTLOrQoGzAT81iKFYb1SZ/Zera1cjGwQSq79AcI/N/6DgBIfpnlwiEiP2am/4w4+38lfUELaNFry8HbpbpTqV4sqXN1WpeJ7CHHwcDBnMVj8djMthFaapMFm/i6swvGEQ2JoygFU3CQHU1ScyOebPLnpsDlQDzBNGKNvuSThffnXLLiePTKbx6aD2yiupr6ji7hzsE6/QqGcC+eseQV1yrWJ/1FwxLLcsoeNlNayMAK+IVTbLtq2VWEbHWAH22+t7LdPt+jENjC3poF7Q7OPhmoi1OhPnmiioXy5JbKDR0oJ/cQ5Mp+SSgCNRP4FpYjl8hG/IVrYXd7gi5F+QXf6B2EiCJq+fuOW8fXpxdRHfEuWC1PB9ruQ=";


    private String fieldName = "Consult";
    public final int REVISION_MODE_SIGN = 1; //手写签批
    public final int REVISION_MODE_WORD = 2; //文字签批
    private boolean isCover = false; //是否为覆盖模式，还是叠加模式
    static final int mMyActivityRequestCode = 10000;
    private Bitmap bmp;
    private int mHeight;
    private int mWidth;


    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

        this.callbackContext = callbackContext;

        if (action.equals("dorevision")) {
            int width = args.getInt(0);
            int height = args.getInt(1);
            doRevision(width, height);
            return true;
        } else if (action.equals("dowordrevision")) {
            int width = args.getInt(0);
            int height = args.getInt(1);
            doWordRevision(width, height);
            return true;
        } else if (action.equals("dointersectedrevision")) {
            int width = args.getInt(0);
            int height = args.getInt(1);
            doIntersectedRevision(width, height);
            return true;
        } else if (action.equals("getfilelist")) {
            String url = args.getString(0);
            String username = args.getString(1);
            getFileList(url, username);
            return true;
        } else if (action.equals("dofullsignrevision")) {
            doFullSignRevision();
            return true;
        } else if (action.equals("getRevisionInfoFromNet")) {
            int recordId = args.getString(0);
            int userName = args.getString(1);
            getRevisionInfoFromNet(recordId,userName);
            return true;
        } else if (action.equals("saveRevisionToNet")) {
            saveRevisionToNet();
            return true;
        }


        return false;
    }


    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        /** 删除cache文件 */
        File[] files = cordova.getActivity().getFilesDir().listFiles();
        for (File f : files) {
            f.delete();
        }
        handler = new MyHandler(Looper.getMainLooper());
    }

    private ProgressDialog progressDialog;
    private boolean cancelRequest = false; //取消网络请求

    /**
     * 从网络获取签批数据进行展示
     */
    public ProgressDialog showMyDialog(Context context, String title, String msg) {
        ProgressDialog my_dialog = new ProgressDialog(context);
        my_dialog.setTitle(title);
        my_dialog.setMessage(msg);
        my_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        my_dialog.setIndeterminate(false);
        my_dialog.setCancelable(false);
        return my_dialog;
    }


    public void getRevisionInfoFromNet(String record_id, String userName,Callback) {

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        cancelRequest = false;
        progressDialog = showMyDialog(cordova.getActivity(), "提示", "正在获取签批数据");
        progressDialog.show();
        progressDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    cancelRequest = true;
                    progressDialog.dismiss();
                    Toast.makeText(context, "您已取消网络数据的数据，可以选择【刷新】按钮再次获取", Toast.LENGTH_SHORT).show();
                }
                return false;

            }
        });

        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    Bitmap bitmap = webRevisionEx.loadRevision(WebRevisionExActivity.this, url, record_id, userName, fieldName);
                    if (cancelRequest == false) {
                        Message msg = new Message();
                        if (bitmap != null) {
                            callbackContext.success(iAppRevisionUtil.getBitmapString(bitmap));//把图片Stirng 返回去
//                            msg.obj = bitmap;
                         msg.what = MSG_WHAT_GETREVISIONSUCCESS;
                        } else {
                            //提示：解析签批图片异常
                            msg.what = MSG_WHAT_TOASTMESSAGE;
                            msg.obj = "生成签批图片失败";
                        }
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {

                    Log.e(TAG, "异常" + e.toString());

                }
            }
        }).start();
    }

    private void saveRevisionToNet() {

    }


    private void doFullSignRevision() {
        // TODO Auto-generated method stub

        Intent intent = new Intent(cordova.getActivity(), com.kinggrid.plugin.iapprevisionplugin.view.RevisionFullSignActivity.class);
        cordova.startActivityForResult(IAppRevisionPlugin.this, intent, mMyActivityRequestCode);


    }

    private void getFileList(String url, String username) {
        Log.v("zxg", "getFileList enter!");
//		String url = "http://oa.goldgrid.com:88/iWebRevisionEx/iWebServer.jsp";
//		String user_name = "admin";
        Intent intent = new Intent(cordova.getActivity(), com.kinggrid.plugin.iapprevisionplugin.web.DocListActivity.class);
        intent.putExtra("login_url", url);
        intent.putExtra("login_name", username);
        cordova.getActivity().startActivity(intent);
    }

    private void doWordRevision(int width, int height) {
        // TODO Auto-generated method stub
        Log.v("zxg", "doWordRevision");
        showRevisionDialog(REVISION_MODE_WORD);
    }

    private void doIntersectedRevision(int width, int height) {
        // TODO Auto-generated method stub
        Log.v("zxg", "doIntersectedRevision");
        showGridSignDialog();
    }


    private void doRevision(int width, int height) {
        // TODO Auto-generated method stub
        Log.v("zxg", "doRevision");
        showRevisionDialog(REVISION_MODE_SIGN);
    }

    public void showRevisionDialog(int revision_mode) {
        RevisionNormalDialog revisionNormalDialog = new RevisionNormalDialog(
                cordova.getActivity(), copyRight, userName, fieldName);
        revisionNormalDialog.showRevisionWindow(revision_mode);
        revisionNormalDialog.setOnFinishListener(new OnFinishListener() {

            @Override
            public void setOnFinish(iAppRevisionView revisionView,
                                    Bitmap bitmap, String sign_flag) {
                if (bitmap != null) {
                    if (isCover) {
                        showImageToWebView(bitmap);
                    } else { //叠加图片
                        showImageToWebView(iAppRevisionUtil.overlapBitmapToFile(cordova.getActivity().getFilesDir() + "/iAppRevisionDemo_" + fieldName + ".png", bitmap));
                    }
                    //Log.v("zxg", "bitmap != null");
                }
            }
        });
    }

    /**
     * 米字格签批
     */
    private void showGridSignDialog() {
        RevisionGridDialog revisionWindowIntersected = new RevisionGridDialog(cordova.getActivity(), copyRight, userName, fieldName);
        revisionWindowIntersected.showRevisionWindow();
        revisionWindowIntersected.setOnFinishListener(new OnFinishListener() {

            @Override
            public void setOnFinish(iAppRevisionView revisionView, Bitmap bitmap, String sign_flag) {
                if (bitmap != null) {
                    if (isCover) {
                        showImageToWebView(bitmap);
                    } else { //叠加图片
                        showImageToWebView(iAppRevisionUtil.overlapBitmapToFile(cordova.getActivity().getFilesDir() + "/iAppRevisionDemo_" + fieldName + ".png", bitmap));
                    }
                }
            }
        });
    }

    protected void showImageToWebView(Bitmap bitmap) {
        // TODO Auto-generated method stub
        Log.v("zxg", "showImageToWebView");
        bmp = bitmap;
        handler.post(new Runnable() {

            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                // TODO Auto-generated method stub
                webView.clearCache(true);
                DisplayMetrics dm = new DisplayMetrics();
                cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
                webView.loadUrl("javascript:doRevisionResult('data:image/png;base64," + iAppRevisionUtil.getBitmapString(bmp) + "','" + bmp.getWidth() / dm.density + "','" + bmp.getHeight() / dm.density + "','" + fieldName + "')");
            }

        });
    }

    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // TODO Auto-generated method stub
        if (requestCode == mMyActivityRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                byte[] sign_bmp_byte = intent.getByteArrayExtra("singbmp");
                Bitmap sign_bmp = Bytes2Bimap(sign_bmp_byte);
                Bitmap webview_bmp = iAppRevisionUtil.getViewBitmap(webView.getView());
                if (webview_bmp != null && sign_bmp != null) {
                    showFullImageToWebView(iAppRevisionUtil.groupBitmap(webview_bmp, sign_bmp, false), 200, 200);
                }
                webView.getView().setDrawingCacheEnabled(false);
            }
        }
    }

    /**
     * 调用JS方法，显示图片
     *
     * @param filePath
     * @param bitmap
     */
    private void showFullImageToWebView(Bitmap bitmap, int width, int height) {
        bmp = bitmap;
        mWidth = width;
        mHeight = height;
        handler.post(new Runnable() {

            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                webView.clearCache(true);
                webView.loadUrl("javascript:doRevisionResult('data:image/png;base64," + iAppRevisionUtil.getBitmapString(bmp) + "','" + mWidth + "','" + mHeight + "','" + fieldName + "')");
            }

        });

    }


}
