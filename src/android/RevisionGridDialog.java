package com.kinggrid.plugin.iapprevisionplugin.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.kinggrid.iapprevision.RevisionEntity;
import com.kinggrid.iapprevision.iAppRevisionEditView;
import com.kinggrid.iapprevision.iAppRevisionView;
import com.kinggrid.plugin.iapprevisionplugin.OnFinishListener;
import com.kinggrid.plugin.iapprevisionplugin.R;
import com.kinggrid.plugin.iapprevisionplugin.view.RevisionSettingDialog.OnSignChangedListener;
/**
 * 米字格手写签批
 * com.kinggrid.iapprevisiondemo.view.RevisionGridDialog
 * @author wmm
 * create at 2016年1月18日 下午2:03:21
 */
public class RevisionGridDialog implements OnClickListener{
	private AlertDialog revision_dialog;
	private static final String TAG = "RevisionGridDialog";
	private Activity activity;
	private String copyRight;
	private iAppRevisionView revisionView;
	private iAppRevisionEditView editText;
	private Button setting_btn,save_btn,cancel_btn,clear_btn,undo_btn,space_btn,enter_btn;
	private DisplayMetrics dm;
	private String userName;
	private View dialog_view;
	private String fieldName;
	/**
	 * 
	 * @param context
	 * @param lic
	 * @param dm
	 * @param userName
	 */
	public RevisionGridDialog(Activity context,String lic,String userName,String fieldName){
		this.activity = context;
		copyRight = lic;
		this.userName = userName;
		dm = context.getResources().getDisplayMetrics();  
		this.fieldName = fieldName;
	}
	/**
	 * 显示窗口
	 */
	public void showRevisionWindow(){
		if(revision_dialog != null && revision_dialog.isShowing()){
			return;
		}
		dialog_view = LayoutInflater.from(activity).inflate(R.layout.intersected_dialog, null);
		initDialogView(dialog_view);
		initDialogListener();
		
		revision_dialog = new  AlertDialog.Builder(activity).create();
		revision_dialog.setView(dialog_view);
		revision_dialog.show();
		revision_dialog.setCancelable(false);
        
	}
	
	private void initDialogView(View view) {
		revisionView = (iAppRevisionView) view.findViewById(R.id.demo_get_revision);
		editText = (iAppRevisionEditView) view.findViewById(R.id.revisionEditView);
		setting_btn = (Button) view.findViewById(R.id.set_btn);
		save_btn = (Button) view.findViewById(R.id.save_btn);
		save_btn.setEnabled(false);
		cancel_btn = (Button) view.findViewById(R.id.cancel_btn);
		clear_btn = (Button) view.findViewById(R.id.clear_btn);
		undo_btn = (Button) view.findViewById(R.id.undo_btn);
		space_btn = (Button) view.findViewById(R.id.space_btn);
		enter_btn = (Button) view.findViewById(R.id.enter_btn);
	}
	private void initDialogListener() {
		revisionView.setCopyRight(activity, copyRight);
		revisionView.setShowSize((int)(dm.widthPixels / 1.5));
		revisionView.setRevisionHandler(new MyHandler());
		revisionView.configSign(Color.BLACK, 30, iAppRevisionView.TYPE_BALLPEN);
		revisionView.setGridStyle(true);
		revisionView.setGridStyleAutoSaveTime(500);
		
		save_btn.setOnClickListener(this);
		cancel_btn.setOnClickListener(this);
		clear_btn.setOnClickListener(this);
		undo_btn.setOnClickListener(this);
		space_btn.setOnClickListener(this);
		enter_btn.setOnClickListener(this);
		setting_btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.save_btn){			
			Bitmap area_bitmap = editText.saveRevisionValidImage(userName, true);
	//		Bitmap bitmap = editText.saveRevisionImage(userName, 20);
			if(area_bitmap == null){
				return;
			}
			if(revision_dialog != null){
				revision_dialog.dismiss();
			}
			if(finishListener != null){
				finishListener.setOnFinish(revisionView,area_bitmap,RevisionEntity.SIGN_FLAG);
			}
		} else if(v.getId() == R.id.cancel_btn){
			revisionView.cancelRevisionHandler();
			if(revision_dialog != null){
				revision_dialog.dismiss();
			}
		} else if(v.getId() == R.id.clear_btn){
			editText.clearEditText();
		} else if(v.getId() == R.id.undo_btn){
			editText.undoEditText();
		} else if(v.getId() == R.id.space_btn){
			editText.setSpaceSize(10);//设置空格大小
			editText.spaceEditText();
		} else if(v.getId() == R.id.enter_btn){
			editText.enterEditText();
		} else if(v.getId() == R.id.set_btn){
			// 设置画笔相关属性
	//		RevisionSignConfig config = new RevisionSignConfig(activity, revisionView,"penMaxSize","penColor","penType",50,dm);
	//		config.showSettingWindow(dialog_view, RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
			
			RevisionSettingDialog config = new RevisionSettingDialog(activity, new OnSignChangedListener() {
				
				@Override
				public void changed(int color, int size, int type) {
					revisionView.configSign(color, size, type);
				}
			});
			config.setProgress(30);
			config.setKeyName(fieldName + "sign_color", fieldName + "sign_type", fieldName + "sign_size");
			config.show();
		}		
	}
	
	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case iAppRevisionView.MSG_WHAT_AUTOSAVECONTENT:
				save_btn.setEnabled(true);
				Bundle bundle = new Bundle();
				bundle = msg.getData();
				Bitmap tempBitmap = bundle.getParcelable("bitmap");
				if (tempBitmap != null) {
					
					Bitmap scale_Bitmap = null;
					float scale = getFitScale(tempBitmap,editText.getLineHeight() - 8);
					if(scale != 1){
						scale_Bitmap =  editText.scaleBitmap(tempBitmap, scale); //超过指定大小，将缩小图片
					} else {
						scale_Bitmap = tempBitmap; //如果想要更美观，可以把此图片高度增加至指定大小
					}
					editText.insertScaleBitmap(scale_Bitmap);
				}
				break;
			}
		}
	}
	/**
	 * 获取图片压缩的比例
	 * @param bitmap 原图
	 * @param size 最终图片的大小
	 * @return
	 */
	private float getFitScale(Bitmap bitmap,float size){
		
		float scale = 1;
		float height_scale = 1;
		float width_scale = 1;
		//按高度压缩
		if(bitmap.getHeight() > size){
			height_scale = size / bitmap.getHeight();
		}
		if(scale == 1){
			//按宽度压缩
			if(bitmap.getWidth() > size){
				width_scale = size / bitmap.getWidth();
			}
		} 
		scale = height_scale > width_scale ? width_scale : height_scale; 
		return scale;
	}

	private OnFinishListener finishListener;
	
	public void setOnFinishListener(OnFinishListener listener){
		finishListener = listener;
	}
}

