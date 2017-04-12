package com.kinggrid.plugin.iapprevisionplugin;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.kinggrid.iapprevision.iAppRevisionView;
//import com.kinggrid.plugin.iapprevisionplugin.R;
import huatech.gov.slt.R;
/**
 * 设置类
 * com.kinggrid.iapprevisiondemo.view.RevisionSettingDialog
 * @author wmm
 * create at 2016年1月18日 下午2:04:37
 */
public class RevisionSettingDialog {

	private SharedPreferences sharedPreferences;
	private Editor editor;
	private Context context;
	
	/**
	 * Pen Settings
	 */
//	private PopupWindow mSettingWindow;
	private Dialog mSettingWindow;
	/**
	 * 笔的颜色、宽度、类型
	 */
	private int changeColor,changeWidth,changeType;
	/**
	 * 笔宽
	 */
	private int DEFAULT_PENSIZE;
	private final int BRUSH_DEFAULT_PENSIZE = 70;
	private final int BALL_DEFAULT_PENSIZE = 2;
	private final int PENCIL_DEFAULT_PENSIZE = 5;
	private final int WATER_DEFAULT_PENSIZE = 30;
	private String penColorName = "color_name",penSizeName = "size_name",penTypeName = "type_name";
	private boolean isShowPenType;
    /**
     * 笔宽设置的跨度
     */
    private int progress = 30;   
	
	public RevisionSettingDialog(Context mContext, OnSignChangedListener l) {
		context = mContext;
		this.signChangedListener = l;
		isShowPenType = true;
		init();
	}
	
	public RevisionSettingDialog(Context mContext, OnWordChangedListener l) {
		context = mContext;
		this.wordChangedListener = l;
		isShowPenType = false;
		init();
	}
	
	private Dialog getDialog(boolean showPenType){
		View view = initSettingView(showPenType);

//		mSettingWindow = new PopupWindow(view, RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
//		mSettingWindow.setFocusable(true);
//		// 设置不允许在外点击消失
//		mSettingWindow.setOutsideTouchable(false);
//		mSettingWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
		
		Dialog dialog = new Dialog(context,android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
//		dialog_view = LayoutInflater.from(activity).inflate(R.layout.bottom_revision_dialog, null);
		
//		dialog.setContentView(R.layout.bottom_revision_dialog);
		dialog.setContentView(view);
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
	    lp.height = WindowManager.LayoutParams.WRAP_CONTENT; // 高度
	    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
	    dialog.getWindow().setGravity(Gravity.BOTTOM);
	    dialog.getWindow().setAttributes(lp);
	    dialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
			}
		});
	    dialog.setCancelable(false);
	    
	    return dialog;
	}
	/**
	 * 以PopopWindow的方式显示签名设置窗口
	 * @param width 窗口显示的宽度
	 * @param height 窗口显示的高度
	 */
	public void show() {
		mSettingWindow = getDialog(isShowPenType);
	    mSettingWindow.show();
	}
	/**
	 * 初始化
	 */
	private void init(){
		sharedPreferences = context.getSharedPreferences("pen_info",3);
		editor = sharedPreferences.edit();
	}
	/**
	 * 
	 * @param type
	 * @return
	 */
	private String getPenTypeString(int type,boolean isShowPenType){
		String name = "";
		switch (type) {
		case iAppRevisionView.TYPE_BRUSHPEN:
			DEFAULT_PENSIZE = BRUSH_DEFAULT_PENSIZE;
			name = "毛笔";
			break;
		case iAppRevisionView.TYPE_PENCIL:
			DEFAULT_PENSIZE = PENCIL_DEFAULT_PENSIZE;
			name = "铅笔";
			break;
		case iAppRevisionView.TYPE_WATERPEN:
			DEFAULT_PENSIZE = WATER_DEFAULT_PENSIZE;
			name = "水彩笔";
			break;
		case iAppRevisionView.TYPE_BALLPEN:
		default:
			DEFAULT_PENSIZE = BALL_DEFAULT_PENSIZE;
			break;
		}
		if(isShowPenType){
			return name;
		} else {
			DEFAULT_PENSIZE = 15;
			return "";
		}
	}
	/**
	 * 初始化界面
	 * @param mWidth
	 * @param mHeight
	 * @return
	 */
	private View initSettingView(final boolean showPenType) {
		View settingView = LayoutInflater.from(context).inflate(R.layout.pensetting, null);
		final TextView width_textView = (TextView) settingView.findViewById(R.id.width);
		final SeekBar seekBar = (SeekBar) settingView.findViewById(R.id.seekset);
		final TextView lineshow = (TextView) settingView.findViewById(R.id.textshow);
		Button closeWindowButton = (Button) settingView.findViewById(R.id.btn_close_setting);
		seekBar.setMax(progress); 
		
		int color = getPenColorFromXML(this.penColorName); 
		
		int penSize = (int) getPenSizeFromXML(this.penSizeName);
		changeType = getPenTypeFromXML(this.penTypeName);
		String keyName = getPenTypeString(changeType,showPenType);
		penSize = (penSize < DEFAULT_PENSIZE) ? DEFAULT_PENSIZE :penSize;
		penSize = (penSize > DEFAULT_PENSIZE + progress) ? DEFAULT_PENSIZE + progress :penSize;
		width_textView.setText(keyName+"宽度:" +penSize);
		seekBar.setProgress(penSize - DEFAULT_PENSIZE); 
		lineshow.setBackgroundColor(color);
		closeWindowButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mSettingWindow != null) {
					if(signChangedListener != null ){
						signChangedListener.changed(changeColor, changeWidth, changeType);
					}
					if(wordChangedListener != null){
						wordChangedListener.changed(changeColor, changeWidth);
					}
					setPenTypeToXML(changeType);
					setPenColorToXML(changeColor);
					setPenSizeToXML(changeWidth);
					mSettingWindow.dismiss();
				}
			}
		});
		final LayoutParams linearParams = (LayoutParams) lineshow
				.getLayoutParams(); // 取控件textView当前的布局参数
		linearParams.height = (int) (penSize/1.5);
		lineshow.setLayoutParams(linearParams);
		changeColor = color;
		changeWidth = penSize;

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				String penName = getPenTypeString(changeType,showPenType);
				linearParams.height = (int) ((progress + DEFAULT_PENSIZE)/1.5);
				lineshow.setLayoutParams(linearParams); // 使设置好的布局参数应用到控件
				changeWidth = progress+DEFAULT_PENSIZE;
				width_textView.setText(penName+"宽度：" +(progress+DEFAULT_PENSIZE));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

		});
		GridView penColor_gridView = (GridView) settingView.findViewById(R.id.pen_color_selector);
		penColor_gridView.setAdapter(new ColorAdapter(context));
		penColor_gridView.requestFocus();
		penColor_gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				changeColor = m_penColors[position];
				lineshow.setBackgroundColor(changeColor);
			}
		});
		
		GridView penType_gridView = (GridView) settingView.findViewById(R.id.pen_type_selector);
		if(showPenType){
			penType_gridView.setVisibility(View.VISIBLE);
			penType_gridView.setAdapter(new ImageAdapter(context));
			penType_gridView.requestFocus();
			penType_gridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					int penSize = 0;
					changeType = position;
					
					String penName = getPenTypeString(changeType,showPenType);
					penSize = (penSize < DEFAULT_PENSIZE) ? DEFAULT_PENSIZE :penSize;
					penSize = (penSize > DEFAULT_PENSIZE + progress) ? DEFAULT_PENSIZE + progress :penSize;
					width_textView.setText(penName+"宽度：" +penSize);
					linearParams.height = (int) (penSize/1.5);
					lineshow.setLayoutParams(linearParams);
					seekBar.setProgress(penSize - DEFAULT_PENSIZE);
					changeWidth = penSize;
				}
			});
		}else {
			penType_gridView.setVisibility(View.GONE);
		}
		return settingView;
	}
	/**
	 * 保存颜色至XML
	 * @param color
	 */
	private void setPenColorToXML(int color) {
		editor.putInt(penColorName, color);
		editor.commit();
	}
	/**
	 * 保存笔宽至XML
	 * @param penMaxSize
	 */
	private void setPenSizeToXML(float penSize) {
		editor.putFloat(penSizeName, penSize);
		editor.commit();
	}
	/**
	 * 保存笔型至XML 
	 * @param penType
	 */
	private void setPenTypeToXML(int penType){
		editor.putInt(penTypeName, penType);
		editor.commit();
	}
	/**
	 * 获取手写笔的颜色
	 * @param penColorName
	 * @return
	 */
	private int getPenColorFromXML(String penColorName) {
		return sharedPreferences.getInt(penColorName, Color.BLACK);
	}
	/**
	 * 获取笔宽最大值
	 * @param penMaxSizeName
	 * @return
	 */
	private float getPenSizeFromXML(String penSizeName) {
		return sharedPreferences.getFloat(penSizeName, 2);
	}
	/**
	 * 获取笔型
	 * @param penTypeName
	 * @return
	 */
	private int getPenTypeFromXML(String penTypeName){
		return sharedPreferences.getInt(penTypeName, iAppRevisionView.TYPE_BALLPEN);
	}
	/**
	 * 
	 * @param progress
	 */
    public void setProgress(int progress){
    	this.progress = progress;
    }
    /**
     * 设置颜色、类型、大小保存Key值 
     * @param colorName
     * @param typeName
     * @param sizeName
     */
    public void setKeyName(String colorName,String typeName,String sizeName){
    	this.penColorName = colorName;
    	this.penSizeName = sizeName;
    	this.penTypeName = typeName;
    }
	
	/**
	 * 适配器
	 * com.kinggrid.iapprevision.ImageAdapter
	 * @author wmm
	 * create at 2014-4-16 下午03:19:17
	 */
	public class ImageAdapter extends BaseAdapter {
		
		private Context mContext;

		private Integer[] mThumbIds = {
				R.drawable.ballpen, R.drawable.brushpen,R.drawable.pencil,R.drawable.waterpen};
        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
        	
        	final ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                imageView.setAdjustViewBounds(false);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageResource(mThumbIds[position]);

            return imageView;
        }

	}
	
	private static final int[] m_penColors = {
			Color.argb(255, 44, 152, 140), Color.argb(255, 48, 115, 170),
			Color.argb(255, 139, 26, 99), Color.argb(255, 112, 101, 89),
			Color.argb(255, 40, 36, 37), Color.argb(255, 226, 226, 226),
			Color.argb(255, 219, 88, 50), Color.argb(255, 129, 184, 69),
			Color.argb(255, 255, 0, 0), Color.argb(255, 0, 255, 0) };
	
	public class ColorAdapter extends BaseAdapter {
		private Context context;

		public ColorAdapter(Context mContext) {
			this.context = mContext;
		}

		@Override
		public int getCount() {
			return m_penColors.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null){
				convertView = LayoutInflater.from(context).inflate(R.layout.pencolor, null);
				
				holder = new ViewHolder();
				holder.color_imageView = (ImageView) convertView.findViewById(R.id.pen_color);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.color_imageView.setBackgroundColor(m_penColors[position]);
			
			return convertView;

		}
	}
	
	class ViewHolder{
		ImageView color_imageView;
	}
	
	private OnSignChangedListener signChangedListener;
	public interface OnSignChangedListener {
		void changed(int color, int size, int type);
	}
	private OnWordChangedListener wordChangedListener;
	public interface OnWordChangedListener {
		void changed(int color, int size);
	}
}
