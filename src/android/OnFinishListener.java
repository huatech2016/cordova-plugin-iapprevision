
package com.kinggrid.plugin.iapprevisionplugin;

import android.graphics.Bitmap;
import com.kinggrid.iapprevision.iAppRevisionView;

/**
 * 完成签批监听器
 * com.kinggrid.iapprevisiondemo.OnFinishListener
 * @author wmm
 * create at 2016年2月17日 上午9:55:41
 */
public interface OnFinishListener {
	/**
	 * 保存签批完成
	 * @param bitmap
	 * @param sign_flag RevisionEntity.SIGN_FLAG - 手写 
	 *                  RevisionEntity.WORD_FLAG - 文字
	 */
	public void setOnFinish(iAppRevisionView revisionView, Bitmap bitmap, String sign_flag);
}

