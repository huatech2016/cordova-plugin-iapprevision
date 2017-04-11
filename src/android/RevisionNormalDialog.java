package com.kinggrid.plugin.iapprevisionplugin.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kinggrid.iapprevision.RevisionEntity;
import com.kinggrid.iapprevision.iAppRevisionView;
import com.kinggrid.plugin.iapprevisionplugin.IConstant;
import com.kinggrid.plugin.iapprevisionplugin.OnFinishListener;
import com.kinggrid.plugin.iapprevisionplugin.R;
import com.kinggrid.plugin.iapprevisionplugin.view.RevisionSettingDialog.OnSignChangedListener;
import com.kinggrid.plugin.iapprevisionplugin.view.RevisionSettingDialog.OnWordChangedListener;
/**
 * 普通手写签批窗口
 * com.kinggrid.iapprevisiondemo.view.RevisionNormalDialog
 * @author wmm
 * create at 2016年1月14日 下午4:48:18
 */
public class RevisionNormalDialog implements OnClickListener,IConstant{

	private static final String TAG = "RevisionWindowIntersected";
//	private PopupWindow revision_dialog;
	private Activity activity;
	private String copyRight;
	private LinearLayout manager_btn_layout;
	private RelativeLayout undo_layout,redo_layout;
	private Button pen_btn, clear_btn,undo_btn, redo_btn ,save_btn,close_btn;
	private Button mix_sign,mix_word,mix_img;
	private Button demo_save_revision,demo_cancel_revision;
	private TextView common_word;
	private iAppRevisionView demo_revision_view;
	private View dialog_view;
	private String userName;
	private String fieldName;
	private Dialog revision_dialog;
	private LinearLayout mix_btn_layout;
	/**
	 * 签批模式：1-手写  2-文字
	 */
	private int revision_mode;
	private DisplayMetrics dm = new DisplayMetrics();
	
	public RevisionNormalDialog(Activity context,String lic,String userName,String fieldName){
		this.activity = context;
		copyRight = lic;
		this.userName = userName;
		this.fieldName = fieldName;
		
		revision_dialog = new Dialog(activity,android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
//		revision_dialog = new Dialog(activity,android.R.style.);
//		dialog_view = LayoutInflater.from(activity).inflate(R.layout.bottom_revision_dialog, null);
		dialog_view = LayoutInflater.from(activity).inflate(R.layout.revision_dialog, null);
		initDialogView(dialog_view);
		initDialogListener();
		
//		dialog.setContentView(R.layout.bottom_revision_dialog);
		revision_dialog.setContentView(dialog_view);
		WindowManager.LayoutParams lp = revision_dialog.getWindow().getAttributes();
	    activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
	    lp.height = (int) (dm.heightPixels / 2.5); // 高度
	    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
	    revision_dialog.getWindow().setGravity(Gravity.BOTTOM);
	    revision_dialog.getWindow().setAttributes(lp);
	    revision_dialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
			}
		});
	    revision_dialog.setCancelable(false);
	}
	/**
	 * 显示签批窗口
	 * @param mode 1-手写  2-文字
	 */
	public void showRevisionWindow(int mode){
		if(revision_dialog != null && revision_dialog.isShowing()){
			return;
		}
		this.revision_mode = mode;
		if(mode == REVISION_MODE_SIGN){  //手写签批
			
			setSignView();
		} else if(mode == REVISION_MODE_WORD){ //文字签批
			
			setWordView();
		} else if(mode == REVISION_MODE_MIX){ //混合签批
			
			Toast.makeText(activity, "当前处于手写签批模式", Toast.LENGTH_SHORT).show();
			mix_btn_layout.setVisibility(View.VISIBLE);
			setSignView();
			mix_sign.setVisibility(View.GONE);
//			mix_img.setVisibility(View.VISIBLE); //TODO 
			mix_img.setVisibility(View.GONE); 
		}
		revision_dialog.show();
	}
	/**
	 * 设置手写签批显示界面
	 */
	private void setSignView(){
		if(revision_mode == REVISION_MODE_MIX || revision_mode == REVISION_MODE_MIX_WORD){
			revision_mode = REVISION_MODE_MIX_SIGN;
		}
		manager_btn_layout.setVisibility(View.VISIBLE);
		common_word.setVisibility(View.GONE);
		undo_layout.setVisibility(View.VISIBLE);
		redo_layout.setVisibility(View.VISIBLE);
		demo_revision_view.useWriteSign();
	}
	/**
	 * 设置文字签批显示界面
	 */
	private void setWordView(){
		if(revision_mode == REVISION_MODE_MIX || revision_mode == REVISION_MODE_MIX_SIGN){
			revision_mode = REVISION_MODE_MIX_WORD;
		}
		manager_btn_layout.setVisibility(View.VISIBLE);
		undo_layout.setVisibility(View.GONE);
		redo_layout.setVisibility(View.GONE);
		common_word.setVisibility(View.VISIBLE);
		demo_revision_view.useWordSign();
	}
	/**
	 * 设置图片显示界面
	 */
	private void setImageView(){
		manager_btn_layout.setVisibility(View.GONE);
		common_word.setVisibility(View.GONE);
	}
	
	private void initDialogView(View view) {
		//控制按钮
		manager_btn_layout = (LinearLayout) view.findViewById(R.id.manager_btn_layout);
		undo_layout = (RelativeLayout) manager_btn_layout.findViewById(R.id.undo_layout);
		redo_layout = (RelativeLayout) manager_btn_layout.findViewById(R.id.redo_layout);
		pen_btn = (Button) manager_btn_layout.findViewById(R.id.pen_btn);
		clear_btn = (Button) manager_btn_layout.findViewById(R.id.clear_btn);
		undo_btn = (Button) manager_btn_layout.findViewById(R.id.undo_btn);
		redo_btn = (Button) manager_btn_layout.findViewById(R.id.redo_btn);
		save_btn = (Button) manager_btn_layout.findViewById(R.id.save_btn);
		close_btn = (Button) manager_btn_layout.findViewById(R.id.close_btn);
		//常用批示语
		common_word = (TextView) view.findViewById(R.id.common_word);
		common_word.setOnClickListener(this);
		//混合签批
		mix_btn_layout = (LinearLayout) view.findViewById(R.id.mix_btn_layout);
		mix_sign = (Button) mix_btn_layout.findViewById(R.id.mix_sign);
		mix_word = (Button) mix_btn_layout.findViewById(R.id.mix_word);
		mix_img = (Button) mix_btn_layout.findViewById(R.id.mix_img);
		//确定保存或取消
		demo_save_revision = (Button) view.findViewById(R.id.demo_save_revision);
		demo_cancel_revision = (Button) view.findViewById(R.id.demo_cancel_revision);
		//金格控件
		demo_revision_view = (iAppRevisionView) view.findViewById(R.id.demo_revision_view);
	}
	private void initDialogListener() {
		demo_revision_view.setCopyRight(activity,copyRight);
		demo_revision_view.configSign(Color.BLACK, 10, iAppRevisionView.TYPE_BALLPEN);
		demo_revision_view.setFieldName(fieldName);
		
		pen_btn.setOnClickListener(this);
		clear_btn.setOnClickListener(this);
		undo_btn.setOnClickListener(this);
		redo_btn.setOnClickListener(this);
		save_btn.setOnClickListener(this);
		close_btn.setOnClickListener(this);
		
		mix_sign.setOnClickListener(this);
		mix_word.setOnClickListener(this);
		mix_img.setOnClickListener(this);
		
		demo_save_revision.setOnClickListener(this);
		demo_cancel_revision.setOnClickListener(this);
	}
	/**
	 * 生成签批结果
	 */
	private void create(){
		Bitmap valid_sign_bmp = null;
		if(revision_mode == REVISION_MODE_SIGN){
//			demo_revision_view.setTimeTextInfo(demo_revision_view.getTimeTextWidth(),
//					demo_revision_view.getTimeTextHeight(), 10, 20, Color.RED, demo_revision_view.getTime_textAlign());
			valid_sign_bmp = demo_revision_view.saveValidSign();
		} else if(revision_mode == REVISION_MODE_WORD){
			valid_sign_bmp = demo_revision_view.saveValidWord();
		} else if (revision_mode == REVISION_MODE_MIX
				|| revision_mode == REVISION_MODE_MIX_SIGN
				|| revision_mode == REVISION_MODE_MIX_WORD) {
			
			Bitmap sign_bitmap = demo_revision_view.saveSign();
			demo_revision_view.clearSign();
			if (sign_bitmap != null) {
				demo_revision_view.saveFieldSignBitmap(fieldName,
						sign_bitmap);
			}

			Bitmap word_bitmap = demo_revision_view.saveWord();
			demo_revision_view.clearWord();
			if (word_bitmap != null) {
				demo_revision_view.saveFieldWordBitmap(fieldName,
						word_bitmap);
			}
			
			valid_sign_bmp = demo_revision_view.getFieldBitmapByName(fieldName);
		}
		if (valid_sign_bmp != null) {
			demo_revision_view.showRevisionImage(valid_sign_bmp,false);
			manager_btn_layout.setVisibility(View.GONE);// 手写控制按钮
			mix_btn_layout.setVisibility(View.GONE);
			common_word.setVisibility(View.GONE);
			demo_save_revision.setVisibility(View.VISIBLE);
			demo_cancel_revision.setVisibility(View.VISIBLE);
		} else {
			Log.e(TAG, "签批内容为空");
		}
	}
	/**
	 * 保存最终签批结果
	 */
	private void save(){
		Bitmap bitmap = demo_revision_view.saveRevisionValidImage(userName,true);
//		 Bitmap bitmap = demo_revision_view.saveRevisionImage(userName,false);

		if (bitmap != null) {
			if (revision_dialog != null) {
				revision_dialog.dismiss();
			}
			if (finishListener != null) {
				if(revision_mode == REVISION_MODE_SIGN){
					finishListener.setOnFinish(demo_revision_view,bitmap, RevisionEntity.SIGN_FLAG);
				} else if(revision_mode == REVISION_MODE_WORD){
					finishListener.setOnFinish(demo_revision_view,bitmap, RevisionEntity.WORD_FLAG);
				} else{
					finishListener.setOnFinish(demo_revision_view,bitmap, RevisionEntity.SIGN_FLAG);
				}
			}
		} else {
			Log.e(TAG, "签批内容为空");
		}
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.demo_save_revision){// 完成签批
			save();
		} else if(v.getId() == R.id.pen_btn){
			if(revision_mode == REVISION_MODE_SIGN || revision_mode == REVISION_MODE_MIX_SIGN){
//				RevisionSignConfig config = new RevisionSignConfig(activity,
//						demo_revision_view, "penMaxSize", "penColor",
//						"penType", 50, activity.getResources()
//								.getDisplayMetrics());
//				config.showSettingWindow(dialog_view,
//						RelativeLayout.LayoutParams.WRAP_CONTENT,
//						RelativeLayout.LayoutParams.WRAP_CONTENT);
				RevisionSettingDialog config = new RevisionSettingDialog(activity, new OnSignChangedListener() {
					
					@Override
					public void changed(int color, int size, int type) {
						demo_revision_view.configSign(color, size, type);
					}
				});
				config.setProgress(30);
				config.setKeyName(fieldName + "sign_color", fieldName + "sign_type", fieldName + "sign_size");
				config.show();
//				ColorPickerDialog dialog = new ColorPickerDialog(activity, new OnColorChangedListener() {
//					
//					@Override
//					public void colorChanged(int color) {
//						demo_revision_view.configSign(color, 12, iAppRevisionView.TYPE_BALLPEN);
//					}
//				}, Color.BLACK);
//				dialog.show();
			} else if(revision_mode == REVISION_MODE_WORD || revision_mode == REVISION_MODE_MIX_WORD){
				
				RevisionSettingDialog config = new RevisionSettingDialog(activity,new OnWordChangedListener() {
					
					@Override
					public void changed(int color, int size) {
						demo_revision_view.configWord(color, size, Typeface.DEFAULT);
					}
				});
				config.setProgress(50);
				config.setKeyName(fieldName + "word_color", fieldName + "word_type", fieldName + "word_size");
				config.show();
			}
		} else if(v.getId() == R.id.clear_btn){
			if(revision_mode == REVISION_MODE_SIGN || revision_mode == REVISION_MODE_MIX_SIGN){
				demo_revision_view.clearSign();
			} else if(revision_mode == REVISION_MODE_WORD || revision_mode == REVISION_MODE_MIX_WORD){
				demo_revision_view.clearWord();
			}
		} else if(v.getId() == R.id.undo_btn){
			demo_revision_view.undoSign();
		} else if(v.getId() == R.id.redo_btn){
			demo_revision_view.redoSign();
		} else if(v.getId() == R.id.save_btn){
			create();
		} else if(v.getId() == R.id.close_btn){
			if (revision_dialog != null) {
				revision_dialog.dismiss();
			}
		} else if(v.getId() == R.id.demo_cancel_revision){
			if (revision_dialog != null) {
				revision_dialog.dismiss();
			}
		} else if(v.getId() == R.id.common_word){
			showCommonWordMenu(common_word);
		} else if (v.getId() == R.id.mix_sign){
			switchMix_SignView();
		} else if(v.getId() == R.id.mix_word){
			switchMix_WordView();
		} else if(v.getId() == R.id.mix_img){
			switchMix_ImgView();
		}
		
	}
	/**
	 * 切换至手写签批
	 */
	private void switchMix_SignView(){
		Toast.makeText(activity, "已切换至手写签批模式", Toast.LENGTH_SHORT).show();
		
		//保存文字签批
		Bitmap word_bitmap = demo_revision_view.saveWord();
		if(word_bitmap != null){
			demo_revision_view.clearWord();
			demo_revision_view.saveFieldWordBitmap(fieldName, word_bitmap);
			showTotalBitmapToView(demo_revision_view, fieldName);
		}
		
		setSignView();
		mix_sign.setVisibility(View.GONE);
		mix_word.setVisibility(View.VISIBLE);
//		mix_img.setVisibility(View.VISIBLE); //TODO 
		mix_img.setVisibility(View.GONE);
	}
	/**
	 * 切换至文字签批
	 */
	private void switchMix_WordView(){
		Toast.makeText(activity, "已切换至文字签批模式", Toast.LENGTH_SHORT).show();

		//保存手写签批
		Bitmap sign_bitmap = demo_revision_view.saveSign();
		if(sign_bitmap != null){
			demo_revision_view.clearSign();
			demo_revision_view.saveFieldSignBitmap(fieldName, sign_bitmap);
			showTotalBitmapToView(demo_revision_view, fieldName);
		}
		
		setWordView();
		mix_word.setVisibility(View.GONE);
		mix_sign.setVisibility(View.VISIBLE);
//		mix_img.setVisibility(View.VISIBLE); //TODO 
		mix_img.setVisibility(View.GONE);
	}
	/**
	 * 切换至图片签批
	 */
	private void switchMix_ImgView(){
		Toast.makeText(activity, "已切换至图片签批模式", Toast.LENGTH_SHORT).show();
		// 保存文字签批
		Bitmap word_bitmap = demo_revision_view.saveWord();
		if (word_bitmap != null) {
			demo_revision_view.clearWord();
			demo_revision_view.saveFieldWordBitmap(fieldName, word_bitmap);
		}
		// 保存手写签批
		Bitmap sign_bitmap = demo_revision_view.saveSign();
		if (sign_bitmap != null) {
			demo_revision_view.clearSign();
			demo_revision_view.saveFieldSignBitmap(fieldName, sign_bitmap);
		}
		
		setImageView();
		mix_img.setVisibility(View.GONE);
		mix_sign.setVisibility(View.VISIBLE);
		mix_word.setVisibility(View.VISIBLE);

		Bitmap stamp_file_bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.kinggrid_demo);
		if(stamp_file_bitmap != null){
			demo_revision_view.setImageScaleType(ScaleType.FIT_CENTER);
			demo_revision_view.showRevisionImage(stamp_file_bitmap, false);
		}
//		ImportSignatureDialog signatureDialog = new ImportSignatureDialog(webRevision, url,WebRevisionActivity.this, revisionView1.getWidth(), revisionView2.getHeight(), user_name);
//		signatureDialog.setSignatureListener(new OnSignatureListener() {
//			
//			@Override
//			public void onCloseWidndow(Bitmap signature_bmp) {
//				demo_revision_view.saveFieldSignatureBitmap(fieldName, signature_bmp);
//				Bitmap revisonView1_total = demo_revision_view.getFieldBitmapByName(fieldName);
//				if(revisonView1_total != null){
//					demo_revision_view.saveFieldSignatureBitmap(fieldName, revisonView1_total);
//					showTotalBitmapToView(demo_revision_view, fieldName);
//				}
//				
//			}
//		});
//		signatureDialog.showSignatureWindow();
	}
	/**
	 * 显示混合签批图片
	 * @param revisionView
	 * @param field_name
	 */
	private void showTotalBitmapToView(iAppRevisionView revisionView,String field_name){
		Bitmap field_bmp = revisionView.getFieldBitmapByName(field_name);
		if(field_bmp != null){
			revisionView.setImageScaleType(ScaleType.FIT_CENTER);
			revisionView.showRevisionImage(field_bmp,false);
		}
	}
	/**
	 * 显示常用批示语
	 */
	@SuppressLint("NewApi") 
	private void showCommonWordMenu(View view){
		PopupMenu popup = new PopupMenu(activity, view);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
            .inflate(R.menu.popup_menu_common_word, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
            	demo_revision_view.setCommonText(item.toString());
                return true;
            }
        });

        popup.show(); //showing popup menu 
	}
	
	private OnFinishListener finishListener;
	
	public void setOnFinishListener(OnFinishListener listener){
		finishListener = listener;
	}
}

