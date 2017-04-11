package com.kinggrid.plugin.iapprevisionplugin.web;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.kinggrid.iapprevision.iAppRevisionUtil;
import com.kinggrid.iapprevision.iAppRevisionView;
import com.kinggrid.plugin.iapprevisionplugin.OnFinishListener;
import com.kinggrid.plugin.iapprevisionplugin.R;
import com.kinggrid.plugin.iapprevisionplugin.view.RevisionGridDialog;
import com.kinggrid.plugin.iapprevisionplugin.view.RevisionNormalDialog;
/**
 * 与iWebRevisionEx兼容
 * com.kinggrid.iapprevisiondemo.web.WebRevisionExActivity
 * @author wmm
 * create at 2015年12月18日 下午5:01:35
 */
public class WebRevisionExActivity extends WebCommonActivity implements OnClickListener {

	private static final String TAG = "MainActivity";
	private WebView webView;
	private Button submit_btn,back_btn,refresh_btn;
	private MyHandler handler;
	/**
	 * 用户名、服务器址、文档ID
	 */
	private String user_name,url,recordId;
	/**
	 * 保存签批dialog
	 */
	private ProgressDialog save_revision_dialog;
	private String fieldName = "Consult"; //与pc保持一致
	private DisplayMetrics dm = new DisplayMetrics();
	private Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_revision_ex);
		
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		user_name = getIntent().getStringExtra("user_name");
		recordId = getIntent().getStringExtra("record_id");
		url = getIntent().getStringExtra("url");
		initRevisionInstance(url,user_name);
		
		handler = new MyHandler();
		webView = (WebView) findViewById(R.id.webview);
		submit_btn = (Button) findViewById(R.id.submit);
		submit_btn.setOnClickListener(this);
		back_btn = (Button) findViewById(R.id.back);
		back_btn.setOnClickListener(this);
		refresh_btn = (Button) findViewById(R.id.refresh);
		refresh_btn.setOnClickListener(this);
		
		initWebView(webView);
		context = this;
		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		
		super.onConfigurationChanged(newConfig);
		webView.reload();
	}
	
	/**
	 * 初始化WebView
	 * @param webView
	 */
	private void initWebView(WebView webView){
		

		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new JavaScriptObject(this),"iAppRevision");
		webView.loadUrl("file:///android_asset/www/iWebRevision_ex.html");
		//加载网络url
//		webView.loadUrl("http://oa.goldgrid.com:88/iWebRevisionEx/test_WebRevision.html");
//		webView.loadUrl(url.substring(0, url.lastIndexOf("/"))+"/test_WebRevision.html");
		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				
				super.onPageFinished(view, url);
				getRevisionInfoFromNet(recordId,user_name);
			}
			
		});
		
	}
	/**
	 * 和JS交互的接口类
	 * com.example.iapprevision.JavaScriptObject
	 * @author wmm
	 * create at 2015年12月16日 上午11:32:02
	 */
	public class JavaScriptObject {
		Context context;

		public JavaScriptObject(Context mContext) {
			this.context = mContext;
		}

		@JavascriptInterface
		public void doRevision(int view_width,int view_height) { //手写签批
			Message msg = new Message();
			msg.arg1 = view_width;
			msg.arg2 = view_height;
			msg.what = MSG_WHAT_OPEN_REVISION;
			handler.sendMessage(msg);
		}
		@JavascriptInterface
		public void doWordRevision(int view_width,int view_height) { //文字签批
			Message msg = new Message();
			msg.arg1 = view_width;
			msg.arg2 = view_height;
			msg.what = MSG_WHAT_OPEN_WORD_REVISION;
			handler.sendMessage(msg);
		}
		@JavascriptInterface
		public void doIntersectedRevision(int view_width,int view_height) { //米字格签批
			Message msg = new Message();
			msg.arg1 = view_width;
			msg.arg2 = view_height;
			msg.what = MSG_WHAT_OPEN_INTERSECT_REVISION;
			handler.sendMessage(msg);
		}
		@JavascriptInterface
		public void doOpenDoc() { //打开Doc
			handler.sendEmptyMessage(MSG_WHAT_OPEN_DOC); 
		}
	}
	/**
	 * 调用JS方法，显示图片
	 * @param filePath
	 * @param bitmap
	 */
	private void showImageToWebView(Bitmap bitmap){
		webView.clearCache(true);
//		webView.loadUrl("javascript:doRevisionResult('data:image/png;base64," + iAppRevisionUtil.getBitmapString(bitmap)  + "','"+bitmap.getWidth() / dm.density+"','"+bitmap.getHeight() / dm.density+"','"+fieldName+"')");
		String temp  = "javascript:doRevisionResult('data:image/png;base64," + iAppRevisionUtil.getBitmapString(bitmap)  + "','"+bitmap.getWidth()+"','"+bitmap.getHeight()+"','"+fieldName+"')";
		Log.d("kingGird", temp);
		webView.loadUrl("javascript:doRevisionResult('data:image/png;base64," + iAppRevisionUtil.getBitmapString(bitmap)  + "','"+bitmap.getWidth()+"','"+bitmap.getHeight()+"','"+fieldName+"')");
	}
	/**
	 * 保存签批
	 * @param url
	 * @param recrodid
	 * @param userName
	 * @param fieldName
	 * @param bitmap
	 * @param signatureType
	 * @param scale
	 */
	private void saveRevisionToNet(final String url,final String recrodid,final String userName,final String fieldName,final Bitmap bitmap,final String signatureType,final String scale){
		if(bitmap != null){
			if(save_revision_dialog != null){
				save_revision_dialog.dismiss();
			}
//			iAppRevisionUtil.saveBitmap2File(Environment.getExternalStorageDirectory() + File.separator + "iapprevision_test.png", bitmap, CompressFormat.PNG, true);
			save_revision_dialog = showMyDialog(this,"提示","正在保存签批数据");
			save_revision_dialog.show();
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					boolean result = webRevisionEx.saveRevision(url,recrodid, userName, fieldName, bitmap, signatureType, scale);
					if(result){
						webRevisionEx.updateDoc(url, userName, recrodid);
						handler.sendEmptyMessage(MSG_WHAT_SAVEREVISIONSUCCESS);
					} else {
						handler.sendEmptyMessage(MSG_WHAT_SAVEREVISIONERROR);
					}
				}
			}).start();
		}
	}
	
	private final int MSG_WHAT_OPEN_DOC = 1; //打开DOC文档
	private final int MSG_WHAT_OPEN_REVISION = 2; //手写签批
	private final int MSG_WHAT_OPEN_WORD_REVISION = 3; //文字签批
	private final int MSG_WHAT_OPEN_INTERSECT_REVISION = 4; //米字格签批
	private final int MSG_WHAT_SAVEREVISIONSUCCESS = 5; //保存签批成功
	private final int MSG_WHAT_SAVEREVISIONERROR = 6; //保存签批失败
	public final int MSG_WHAT_GETREVISIONSUCCESS = 7; //获取签批成功 
	public final int MSG_WHAT_TOASTMESSAGE = 8;
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
				Bitmap bitmap = (Bitmap) msg.obj;
				if(bitmap != null){
					showImageToWebView(bitmap);
				} else {
					
					Log.e(TAG, "解析签批图片异常或者没有签批！");
					
				}
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
	/**
	 * 手写签批
	 */
	private void showRevisionDialog(int revision_mode) {
		
		RevisionNormalDialog revisionNormalDialog = new RevisionNormalDialog(
				this, copyRight, user_name, fieldName);
		revisionNormalDialog.showRevisionWindow(revision_mode);
		revisionNormalDialog.setOnFinishListener(new OnFinishListener() {

			@Override
			public void setOnFinish(iAppRevisionView revisionView,
					Bitmap bitmap, String sign_flag) {
				if (bitmap != null) {
					saveRevisionToNet(url, recordId,user_name, fieldName, bitmap,sign_flag, "" + dm.density);
				}
			}
		});
	}
	private ProgressDialog progressDialog;
	private boolean cancelRequest = false; //取消网络请求
	/**
	 * 从网络获取签批数据进行展示
	 */
	public void getRevisionInfoFromNet(final String record_id,final String userName) {

		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		cancelRequest = false;
		progressDialog = showMyDialog(this,"提示","正在获取签批数据");  
		progressDialog.show();
		progressDialog.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_BACK){
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
					Bitmap bitmap = webRevisionEx.loadRevision(WebRevisionExActivity.this,url,record_id,userName, fieldName);
					if(cancelRequest == false){
						Message msg = new Message();
						if(bitmap != null){
							msg.obj = bitmap;
							msg.what = MSG_WHAT_GETREVISIONSUCCESS;
						} else {
							//提示：解析签批图片异常
							msg.what = MSG_WHAT_TOASTMESSAGE;
							msg.obj = "生成签批图片失败";
						}
						handler.sendMessage(msg);
					} 
				} catch (Exception e) {

					Log.e(TAG, "异常"+e.toString());

				}
			}
		}).start();
	}
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.submit) {
			finish();
		} else if(v.getId() == R.id.back){
			finish();
		} else if(v.getId() == R.id.refresh){
			getRevisionInfoFromNet(recordId,user_name);
		}		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_revision_ex, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		return super.onOptionsItemSelected(item);
	}
}
