package com.kinggrid.plugin.iapprevisionplugin.view;

import java.io.ByteArrayOutputStream;

import com.kinggrid.iapprevision.iAppRevisionView;
import com.kinggrid.plugin.iapprevisionplugin.IConstant;
import com.kinggrid.plugin.iapprevisionplugin.view.RevisionSettingDialog.OnSignChangedListener;
import com.kinggrid.plugin.iapprevisionplugin.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class RevisionFullSignActivity extends Activity implements IConstant,OnClickListener{
	private String fieldName = "Consult";
	private Context context;
	private View full_sign_layout;
	private iAppRevisionView full_sign_view;
	private Button pen_btn, clear_btn,undo_btn, redo_btn ,save_btn,close_btn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.full_sign_layout);
		context = getApplicationContext();
		initFullSignView();
	}

	private void initFullSignView() {
		// TODO Auto-generated method stub
		full_sign_view = (iAppRevisionView)findViewById(R.id.full_sign_view);
		pen_btn = (Button) findViewById(R.id.pen_btn);
		clear_btn = (Button) findViewById(R.id.clear_btn);
		undo_btn = (Button) findViewById(R.id.undo_btn);
		redo_btn = (Button) findViewById(R.id.redo_btn);
		save_btn = (Button) findViewById(R.id.save_btn);
		close_btn = (Button) findViewById(R.id.close_btn);
		
		full_sign_view.setCopyRight(this, copyRight);
		full_sign_view.configSign(Color.BLACK, 15, iAppRevisionView.TYPE_BALLPEN);
		full_sign_view.setSignSupportEbenMode(true);
		full_sign_view.setSignEnabled(true);
		pen_btn.setOnClickListener(this);
		clear_btn.setOnClickListener(this);
		undo_btn.setOnClickListener(this);
		redo_btn.setOnClickListener(this);
		save_btn.setOnClickListener(this);
		close_btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.pen_btn){
			RevisionSettingDialog config = new RevisionSettingDialog(this, new OnSignChangedListener() {
				
				@Override
				public void changed(int color, int size, int type) {
					// TODO Auto-generated method stub
					full_sign_view.configSign(color, size, type);
				}
			});
			config.setProgress(30);
			config.setKeyName(fieldName + "full_sign_color", fieldName + "full_sign_type", fieldName + "full_sign_size");
			config.show();
		} else if(v.getId() == R.id.clear_btn){
			full_sign_view.clearSign();
		} else if(v.getId() == R.id.undo_btn){
			full_sign_view.undoSign();			
		} else if(v.getId() == R.id.redo_btn){
			full_sign_view.redoSign();
		} else if(v.getId() == R.id.save_btn){
			Bitmap sign_bmp = full_sign_view.saveSign();
			if(sign_bmp == null){
				Toast.makeText(this, "签批内容为空", Toast.LENGTH_SHORT).show();
				return;
			}
			byte[] sign_bmp_byte = Bitmap2Bytes(sign_bmp);
			Intent in = new Intent(getIntent().getAction());
			in.putExtra("singbmp", sign_bmp_byte);
			setResult(RESULT_OK, in);
			full_sign_view.setDrawingCacheEnabled(false);
			closeFullSignView();			
		} else if(v.getId() == R.id.close_btn){
			closeFullSignView();			
		}
	}
	
	private void closeFullSignView(){
		if(full_sign_view != null){
			full_sign_view.clearSign();
		}
		finish();
	}
	
	public Bitmap Bytes2Bimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
		    return null;
		}
	}
	
	public byte[] Bitmap2Bytes(Bitmap bm) {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
}
