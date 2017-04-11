package com.kinggrid.plugin.iapprevisionplugin.web;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.kinggrid.iapprevision_iwebrevision.iAppRevision;
import com.kinggrid.iapprevision_iwebrevision.iAppRevision_iWebRevisionEx;
import com.kinggrid.plugin.iapprevisionplugin.IConstant;
/**
 * 与网络版兼容公共类
 * com.kinggrid.iapprevisiondemo.web.WebCommonActivity
 * @author wmm
 * create at 2016年2月17日 上午9:54:16
 */
public class WebCommonActivity extends Activity implements IConstant {

	public static iAppRevision_iWebRevisionEx webRevisionEx;  //与iWebRevisionEx兼容操作实例
//	public static iAppRevision_iWebRevision webRevision;   //与iWebRevision兼容操作实例
	
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
			
		} /*else if(url.endsWith("iWebRevision/iWebServer.jsp")){ //与iWebRevision兼容
			
			if(webRevision == null){
				webRevision = new iAppRevision_iWebRevision();
				webRevision.setDebug(true);
				webRevision.setCopyRight(this, copyRight,userName);
			}
			return webRevision;
		} */
		return null;
	}
	
	public ProgressDialog showMyDialog(Context context,String title,String msg){
		ProgressDialog my_dialog = new ProgressDialog(context);
		my_dialog.setTitle(title);
		my_dialog.setMessage(msg);
		my_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		my_dialog.setIndeterminate(false);
		my_dialog.setCancelable(false);
		return my_dialog;
	}
	
}
