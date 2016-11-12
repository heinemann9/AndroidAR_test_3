/*
 * Copyright (C) 2010- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare;

/**
 * This class is the main application which uses the other classes for different
 * functionalities.
 * It sets up the camera screen and the augmented screen which is in front of the
 * camera screen.
 * It also handles the main sensor events, touch events and location events.
 */

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.mixare.R.drawable;
import org.mixare.data.DataHandler;
import org.mixare.data.DataSourceList;
import org.mixare.data.DataSourceStorage;
import org.mixare.lib.MixContextInterface;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.render.Matrix;
import org.mixare.mgr.downloader.DownloadManager;
import org.mixare.mgr.downloader.DownloadResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;

public class MixView extends Activity implements SensorEventListener, OnTouchListener {

	private CameraSurface camScreen;
	private AugmentedView augScreen;

	// 메뉴 인플레이터와 뷰
	LayoutInflater vi;
	View v;
	MenuInflater mInflater;

	private boolean isInited;
	private static PaintScreen dWindow;
	private static DataView dataView;
	private boolean fError;

	//----------
    private MixViewDataHolder mixViewData  ;
	
	// TAG for logging
	public static final String TAG = "Mixare";

	ImageButton img1,img2;
	ImageButton menu_img1,menu_img2;
	ImageButton edit_img1,edit_img2;
	RelativeLayout menu_relative1,menu_relative2;
	RelativeLayout edit_relative, main_relative;
	EditText search;
	TextView textAll,textSchool,textFood,textBook,textETC,textOption;

	boolean menu_flag,edit_flag;

	boolean multiChoice;
	boolean choicedAll;
	boolean choicedSchool;
	boolean choicedFood;
	boolean choicedBook;
	boolean choicedETC;

	// why use Memory to save a state? MixContext? activity lifecycle?
	//private static MixView CONTEXT;

	/* string to name & access the preference file in the internal storage */
	public static final String PREFS_NAME = "MyPrefsFileForMenuItems";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//MixView.CONTEXT = this;
		try {

			//handleIntent(getIntent());

			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			getMixViewData().setmWakeLock(pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag"));

			killOnError();
			requestWindowFeature(Window.FEATURE_NO_TITLE);

			maintainCamera();
			maintainMenu();
			maintainAugmentR();
			maintainZoomBar();

			if (!isInited) {
				//getMixViewData().setMixContext(new MixContext(this));
				//getMixViewData().getMixContext().setDownloadManager(new DownloadManager(mixViewData.getMixContext()));
				setdWindow(new PaintScreen());
				setDataView(new DataView(getMixViewData().getMixContext()));

				/* set the radius in data view to the last selected by the user */
				setZoomLevel();
				isInited = true;
			}

			/*Get the preference file PREFS_NAME stored in the internal memory of the phone*/
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

			/*check if the application is launched for the first time*/
			if(settings.getBoolean("firstAccess",false)==false){
				firstAccess(settings);
			}
		} catch (Exception ex) {
			doError(ex);
		}

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		mInflater = this.getMenuInflater();
		mInflater.inflate(R.menu.option, menu);
		if(multiChoice == true)
			menu.findItem(R.id.option_onMulti).setChecked(true);
		else
			menu.findItem(R.id.option_onMulti).setChecked(false);
		menu.setHeaderTitle("설정");
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);

		if(item.getItemId() == R.id.option_onMulti){
			if(multiChoice == false)
				multiChoice = true;
			else {
				multiChoice = false;
				textAll.setTextColor(Color.WHITE);
				textSchool.setTextColor(Color.rgb(255, 187, 0));
				textFood.setTextColor(Color.WHITE);
				textBook.setTextColor(Color.WHITE);
				textETC.setTextColor(Color.WHITE);
				choicedAll = false;
				choicedSchool = true;
				choicedFood = false;
				choicedBook = false;
				choicedETC = false;
			}
				return true;
		}else if(item.getItemId() == R.id.option_onMap){
			Intent intent = new Intent(MixView.this, MapActivity.class);
			startActivity(intent);

		}else if(item.getItemId() == R.id.option_markerInit){
			return true;
		}else if(item.getItemId() == R.id.option_info){
			return true;
		}

		return false;
	}


	private void setPlusMenu(boolean mflag){
		if(mflag == false) {
			img1.setVisibility(View.GONE);
			menu_relative1.setVisibility(View.VISIBLE);
			menu_relative2.setVisibility(View.VISIBLE);
			menu_flag = true;
		}else if(mflag == true){
			img1.setVisibility(View.VISIBLE);
			menu_relative1.setVisibility(View.GONE);
			menu_relative2.setVisibility(View.GONE);
			menu_flag = false;
		}
	}

	private void setSearchMenu(boolean sflag){
		if(sflag == false) {
			img1.setVisibility(View.GONE);
			edit_relative.setVisibility(View.VISIBLE);
			main_relative.setVisibility(View.GONE);

			edit_flag = true;
		}else if(sflag == true){
			img1.setVisibility(View.VISIBLE);
			edit_relative.setVisibility(View.GONE);
			main_relative.setVisibility(View.VISIBLE);

			edit_flag = false;
		}
	}

	public MixViewDataHolder getMixViewData() {
		if (mixViewData==null){
			// TODO: VERY inportant, only one!
			mixViewData = new MixViewDataHolder(new MixContext(this));
		}
		return mixViewData;
	}

	@Override
	protected void onPause() {
		super.onPause();

		try {
			this.getMixViewData().getmWakeLock().release();

			try {
				getMixViewData().getSensorMgr().unregisterListener(this,
						getMixViewData().getSensorGrav());
				getMixViewData().getSensorMgr().unregisterListener(this,
						getMixViewData().getSensorMag());
				getMixViewData().setSensorMgr(null);
				
				getMixViewData().getMixContext().getLocationFinder().switchOff();
				getMixViewData().getMixContext().getDownloadManager().switchOff();

				if (getDataView() != null) {
					getDataView().cancelRefreshTimer();
				}
			} catch (Exception ignore) {
			}

			if (fError) {
				finish();
			}
		} catch (Exception ex) {
			doError(ex);
		}
	}

	/**
	 * {@inheritDoc}
	 * Mixare - Receives results from other launched activities
	 * Base on the result returned, it either refreshes screen or not.
	 * Default value for refreshing is false
	 */
	protected void onActivityResult(final int requestCode,
			final int resultCode, Intent data) {
		Log.d(TAG + " WorkFlow", "MixView - onActivityResult Called");
		// check if the returned is request to refresh screen (setting might be
		// changed)
		try {
			if (data.getBooleanExtra("RefreshScreen", false)) {
				Log.d(TAG + " WorkFlow",
						"MixView - Received Refresh Screen Request .. about to refresh");
				//repaint();
				refresh();
				//refreshDownload();
			}

		} catch (Exception ex) {
			// do nothing do to mix of return results.
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		try {
			this.getMixViewData().getmWakeLock().acquire();

			killOnError();
			getMixViewData().getMixContext().doResume(this);

			//maintainMenu();

			//repaint();
			getDataView().doStart();
			//getDataView().clearEvents();

			getMixViewData().getMixContext().getDataSourceManager().refreshDataSources();

			float angleX, angleY;
			int marker_orientation = -90;
			int rotation = Compatibility.getRotation(this);

			// display text from left to right and keep it horizontal
			angleX = (float) Math.toRadians(marker_orientation);
			getMixViewData().getM1().set(1f, 0f, 0f, 0f,
					(float) Math.cos(angleX),
					(float) -Math.sin(angleX), 0f,
					(float) Math.sin(angleX),
					(float) Math.cos(angleX));
			angleX = (float) Math.toRadians(marker_orientation);
			angleY = (float) Math.toRadians(marker_orientation);
			if (rotation == 1) {
				getMixViewData().getM2().set(1f, 0f, 0f, 0f,
						(float) Math.cos(angleX),
						(float) -Math.sin(angleX), 0f,
						(float) Math.sin(angleX),
						(float) Math.cos(angleX));
				getMixViewData().getM3().set((float) Math.cos(angleY), 0f,
						(float) Math.sin(angleY), 0f, 1f, 0f,
						(float) -Math.sin(angleY), 0f,
						(float) Math.cos(angleY));
			} else {
				getMixViewData().getM2().set((float) Math.cos(angleX), 0f,
						(float) Math.sin(angleX), 0f, 1f, 0f,
						(float) -Math.sin(angleX), 0f,
						(float) Math.cos(angleX));
				getMixViewData().getM3().set(1f, 0f, 0f, 0f,
						(float) Math.cos(angleY),
						(float) -Math.sin(angleY), 0f,
						(float) Math.sin(angleY),
						(float) Math.cos(angleY));

			}

			getMixViewData().getM4().toIdentity();

			for (int i = 0; i < getMixViewData().getHistR().length; i++) {
				getMixViewData().getHistR()[i] = new Matrix();
			}

			getMixViewData()
					.setSensorMgr((SensorManager) getSystemService(SENSOR_SERVICE));

			getMixViewData().setSensors(getMixViewData().getSensorMgr().getSensorList(
					Sensor.TYPE_ACCELEROMETER));
			if (getMixViewData().getSensors().size() > 0) {
				getMixViewData().setSensorGrav(getMixViewData().getSensors().get(0));
			}

			getMixViewData().setSensors(getMixViewData().getSensorMgr().getSensorList(
					Sensor.TYPE_MAGNETIC_FIELD));
			if (getMixViewData().getSensors().size() > 0) {
				getMixViewData().setSensorMag(getMixViewData().getSensors().get(0));
			}

			getMixViewData().getSensorMgr().registerListener(this,
					getMixViewData().getSensorGrav(), SENSOR_DELAY_GAME);
			getMixViewData().getSensorMgr().registerListener(this,
					getMixViewData().getSensorMag(), SENSOR_DELAY_GAME);

			try {
				GeomagneticField gmf = getMixViewData().getMixContext().getLocationFinder().getGeomagneticField(); 
				angleY = (float) Math.toRadians(-gmf.getDeclination());
				getMixViewData().getM4().set((float) Math.cos(angleY), 0f,
						(float) Math.sin(angleY), 0f, 1f, 0f,
						(float) -Math.sin(angleY), 0f,
						(float) Math.cos(angleY));
			} catch (Exception ex) {
				Log.d("mixare", "GPS Initialize Error", ex);
			}

			getMixViewData().getMixContext().getDownloadManager().switchOn();
			getMixViewData().getMixContext().getLocationFinder().switchOn();
		} catch (Exception ex) {
			doError(ex);
			try {
				if (getMixViewData().getSensorMgr() != null) {
					getMixViewData().getSensorMgr().unregisterListener(this,
							getMixViewData().getSensorGrav());
					getMixViewData().getSensorMgr().unregisterListener(this,
							getMixViewData().getSensorMag());
					getMixViewData().setSensorMgr(null);
				}

				if (getMixViewData().getMixContext() != null) {
					getMixViewData().getMixContext().getLocationFinder().switchOff();
					getMixViewData().getMixContext().getDownloadManager().switchOff();
				}
			} catch (Exception ignore) {
			}
		}

		//Log.d("-------------------------------------------", "resume");
		if (getDataView().isFrozen() && getMixViewData().getSearchNotificationTxt() == null) {
			getMixViewData().setSearchNotificationTxt(new TextView(this));
			getMixViewData().getSearchNotificationTxt().setWidth(
					getdWindow().getWidth());
			getMixViewData().getSearchNotificationTxt().setPadding(10, 2, 0, 0);
			getMixViewData().getSearchNotificationTxt().setText(
					getString(R.string.search_active_1) + " "
							+ DataSourceList.getDataSourcesStringList()
							+ getString(R.string.search_active_2));
			;
			getMixViewData().getSearchNotificationTxt().setBackgroundColor(
					Color.DKGRAY);
			getMixViewData().getSearchNotificationTxt().setTextColor(Color.WHITE);

			getMixViewData().getSearchNotificationTxt().setOnTouchListener(this);
			addContentView(getMixViewData().getSearchNotificationTxt(),
					new LayoutParams(LayoutParams.FILL_PARENT,
							LayoutParams.WRAP_CONTENT));
		} else if (!getDataView().isFrozen()
				&& getMixViewData().getSearchNotificationTxt() != null) {
			getMixViewData().getSearchNotificationTxt().setVisibility(View.GONE);
			getMixViewData().setSearchNotificationTxt(null);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * Customize Activity after switching back to it.
	 * Currently it maintain and ensures view creation.
	 */
	protected void onRestart (){
		super.onRestart();
		maintainCamera();
		maintainMenu();
		//maintainAugmentR();
		maintainZoomBar();
	}
	

	public void repaint() {
		//clear stored data
		getDataView().clearEvents();
		setDataView(null); //It's smelly code, but enforce garbage collector 
							//to release data.
		setdWindow(new PaintScreen());
		setDataView(new DataView(mixViewData.getMixContext()));

		setZoomLevel(); //@TODO Caller has to set the zoom. This function repaints only.
	}
	
	/**
	 *  Checks camScreen, if it does not exist, it creates one.
	 */
	private void maintainCamera() {
		if (camScreen == null){
		camScreen = new CameraSurface(this);
		}
		setContentView(camScreen);
	}
	
	/**
	 * Checks augScreen, if it does not exist, it creates one.
	 */
	void maintainAugmentR() {
		if (augScreen == null ){
		augScreen = new AugmentedView(this);
		}
		addContentView(augScreen, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

	}

	void refreshmaintainAugmentR() {
		((ViewGroup)augScreen.getParent()).removeView(augScreen);
		if (augScreen == null ){
			augScreen = new AugmentedView(this);
		}

		addContentView(augScreen, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

	}
	/**
	 * Creates a zoom bar and adds it to view.
	 */
	private void maintainZoomBar() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		FrameLayout frameLayout = createZoomBar(settings);
		addContentView(frameLayout, new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT,
				Gravity.BOTTOM));
	}

	private void maintainMenu(){
		vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v  = vi.inflate(R.layout.activity_main, null);
		addContentView(v, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		// 메뉴
		img1 = (ImageButton)findViewById(R.id.img1);
		img2 = (ImageButton)findViewById(R.id.img2);

		menu_img1 = (ImageButton)findViewById(R.id.menu_img1);
		menu_img2 = (ImageButton)findViewById(R.id.menu_img2);

		menu_relative1 = (RelativeLayout)findViewById(R.id.menu_relative1);
		menu_relative2 = (RelativeLayout)findViewById(R.id.menu_relative2);

		edit_img1 = (ImageButton)findViewById(R.id.edit_img1);  // search
		edit_img2 = (ImageButton)findViewById(R.id.edit_img2);

		main_relative = (RelativeLayout)findViewById(R.id.main_relative);
		edit_relative = (RelativeLayout)findViewById(R.id.edit_relative);

		search = (EditText)findViewById(R.id.edit_search);      // edittext search

		menu_flag = false;
		edit_flag = false;
		multiChoice = false;
		choicedAll = false;
		choicedSchool = false;
		choicedFood = false;
		choicedBook = false;
		choicedETC = false;

		textAll = (TextView) findViewById(R.id.textAll);
		textSchool = (TextView) findViewById(R.id.textSchool);
		textFood = (TextView) findViewById(R.id.textFood);
		textBook = (TextView) findViewById(R.id.textBook);
		textETC = (TextView) findViewById(R.id.textETC);
		textOption = (TextView) findViewById(R.id.textOption);

		registerForContextMenu(textOption);


		// 메뉴 클릭시 ..
		img1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setPlusMenu(menu_flag);
			}
		});

		menu_img1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setPlusMenu(menu_flag);
			}
		});

		// 검색 클릭시 ..
		img2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setSearchMenu(edit_flag);
			}
		});

		menu_img2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setPlusMenu(edit_flag);
			}
		});

		edit_img1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setSearchMenu(edit_flag);
                // search 수정 중 (?)
                if(search.getText().toString() != null){
                    doMixSearch(search.getText().toString());
                    // 혹시 작동?

                }

			}
		});

		textOption.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openContextMenu(textOption);
			}
		});

		// 메뉴 전체
		textAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (choicedAll == false) {
					textAll.setTextColor(Color.rgb(255, 187, 0));
					textSchool.setTextColor(Color.rgb(255, 187, 0));
					textFood.setTextColor(Color.rgb(255, 187, 0));
					textBook.setTextColor(Color.rgb(255, 187, 0));
					textETC.setTextColor(Color.rgb(255, 187, 0));
					choicedAll = true;
					choicedSchool = true;
					choicedFood = true;
					choicedBook = true;
					choicedETC = true;
				} else {
					textAll.setTextColor(Color.WHITE);
					textSchool.setTextColor(Color.rgb(255, 187, 0));
					textFood.setTextColor(Color.WHITE);
					textBook.setTextColor(Color.WHITE);
					textETC.setTextColor(Color.WHITE);
					choicedAll = false;
					choicedSchool = true;
					choicedFood = false;
					choicedBook = false;
					choicedETC = false;
				}

				getDataView().clearEvents();
				setDataView(null); //It's smelly code, but enforce garbage collector
				//to release data.

				setdWindow(new PaintScreen());
				setDataView(new DataView(mixViewData.getMixContext()));

			}
		});

		// 메뉴 학교건물
		textSchool.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if(multiChoice == true){
					if (choicedSchool == false) {
						textSchool.setTextColor(Color.rgb(255, 187, 0));
						choicedSchool = true;

						if(choicedSchool == true && choicedFood == true && choicedBook == true && choicedETC == true)
							textAll.setTextColor(Color.rgb(255, 187, 0));
					}
					else {
						textSchool.setTextColor(Color.WHITE);
						choicedSchool = false;
						textAll.setTextColor(Color.WHITE);
						choicedAll = false;
					}
				}
				else {
					//to do
                    textAll.setTextColor(Color.WHITE);
                    textSchool.setTextColor(Color.rgb(255, 187, 0));
                    textFood.setTextColor(Color.WHITE);
                    textBook.setTextColor(Color.WHITE);
                    textETC.setTextColor(Color.WHITE);
                    choicedAll = false;
                    choicedSchool = true;
                    choicedFood = false;
                    choicedBook = false;
                    choicedETC = false;

				}
			}
		});

		// 메뉴 음식
		textFood.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if(multiChoice == true){
					if (choicedFood == false) {
						textFood.setTextColor(Color.rgb(255, 187, 0));
						choicedFood = true;

						if(choicedSchool == true && choicedFood == true && choicedBook == true && choicedETC == true)
							textAll.setTextColor(Color.rgb(255, 187, 0));
					}
					else {
						textFood.setTextColor(Color.WHITE);
						choicedFood = false;
						textAll.setTextColor(Color.WHITE);
						choicedAll = false;
					}
				}
				else {
					//to do
                    textAll.setTextColor(Color.WHITE);
                    textSchool.setTextColor(Color.WHITE);
                    textFood.setTextColor(Color.rgb(255, 187, 0));
                    textBook.setTextColor(Color.WHITE);
                    textETC.setTextColor(Color.WHITE);
                    choicedAll = false;
                    choicedSchool = false;
                    choicedFood = true;
                    choicedBook = false;
                    choicedETC = false;


				}
			}
		});

		// 메뉴 문구, 서적
		textBook.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if(multiChoice == true){
					if (choicedBook == false) {
						textBook.setTextColor(Color.rgb(255, 187, 0));
						choicedBook = true;

						if(choicedSchool == true && choicedFood == true && choicedBook == true && choicedETC == true)
							textAll.setTextColor(Color.rgb(255, 187, 0));
					}
					else {
						textBook.setTextColor(Color.WHITE);
						choicedBook = false;
						textAll.setTextColor(Color.WHITE);
						choicedAll = false;
					}
				}
				else {
					//to do
						textAll.setTextColor(Color.WHITE);
						textSchool.setTextColor(Color.WHITE);
						textFood.setTextColor(Color.WHITE);
						textBook.setTextColor(Color.rgb(255, 187, 0));
						textETC.setTextColor(Color.WHITE);
						choicedAll = false;
						choicedSchool = false;
						choicedFood = false;
						choicedBook = true;
						choicedETC = false;
				}
			}
		});

		// 메뉴 기타
		textETC.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if(multiChoice == true){
					if (choicedETC == false) {
						textETC.setTextColor(Color.rgb(255, 187, 0));
						choicedETC = true;

						if(choicedSchool == true && choicedFood == true && choicedBook == true && choicedETC == true)
							textAll.setTextColor(Color.rgb(255, 187, 0));
					}
					else {
						textETC.setTextColor(Color.WHITE);
						choicedETC = false;
						textAll.setTextColor(Color.WHITE);
						choicedAll = false;
					}
				}
				else {
					//to do
						textAll.setTextColor(Color.WHITE);
						textSchool.setTextColor(Color.WHITE);
						textFood.setTextColor(Color.WHITE);
						textBook.setTextColor(Color.WHITE);
						textETC.setTextColor(Color.rgb(255, 187, 0));
						choicedAll = false;
						choicedSchool = false;
						choicedFood = false;
						choicedBook = false;
						choicedETC = true;
				}
			}
		});
	}
	
	/**
	 * Refreshes Download 
	 * TODO refresh downloads
	 */
	private void refreshDownload(){
//		try {
//			if (getMixViewData().getDownloadThread() != null){
//				if (!getMixViewData().getDownloadThread().isInterrupted()){
//					getMixViewData().getDownloadThread().interrupt();
//					getMixViewData().getMixContext().getDownloadManager().restart();
//				}
//			}else { //if no download thread found
//				getMixViewData().setDownloadThread(new Thread(getMixViewData()
//						.getMixContext().getDownloadManager()));
//				//@TODO Syncronize DownloadManager, call Start instead of run.
//				mixViewData.getMixContext().getDownloadManager().run();
//			}
//		}catch (Exception ex){
//		}
	}
	
	public void refresh(){
		dataView.refresh();
	}

	public void setErrorDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.connection_error_dialog));
		builder.setCancelable(false);

		/*Retry*/
		builder.setPositiveButton(R.string.connection_error_dialog_button1, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				fError=false;
				//TODO improve
				try {
					maintainCamera();
					maintainMenu();
					maintainAugmentR();
					repaint();
					setZoomLevel();
				}
				catch(Exception ex){
					//Don't call doError, it will be a recursive call.
					//doError(ex);
				}
			}
		});
		/*Open settings*/
		builder.setNeutralButton(R.string.connection_error_dialog_button2, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent intent1 = new Intent(Settings.ACTION_WIRELESS_SETTINGS); 
				startActivityForResult(intent1, 42);
			}
		});
		/*Close application*/
		builder.setNegativeButton(R.string.connection_error_dialog_button3, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				System.exit(0); //wouldn't be better to use finish (to stop the app normally?)
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	
	public float calcZoomLevel(){

		int myZoomLevel = getMixViewData().getMyZoomBar().getProgress();
		float myout = 5;

		if (myZoomLevel <= 26) {
			myout = myZoomLevel / 25f;
		} else if (25 < myZoomLevel && myZoomLevel < 50) {
			myout = (1 + (myZoomLevel - 25)) * 0.38f;
		} else if (25 == myZoomLevel) {
			myout = 1;
		} else if (50 == myZoomLevel) {
			myout = 10;
		} else if (50 < myZoomLevel && myZoomLevel < 75) {
			myout = (10 + (myZoomLevel - 50)) * 0.83f;
		} else {
			myout = (30 + (myZoomLevel - 75) * 2f);
		}


		return myout;
	}

	/**
	 * Handle First time users. It display license agreement and store user's
	 * acceptance.
	 * 
	 * @param settings
	 */
	private void firstAccess(SharedPreferences settings) {
		SharedPreferences.Editor editor = settings.edit();
		AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
		builder1.setMessage(getString(R.string.license));
		builder1.setNegativeButton(getString(R.string.close_button),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		AlertDialog alert1 = builder1.create();
		alert1.setTitle(getString(R.string.license_title));
		alert1.show();
		editor.putBoolean("firstAccess", true);

		// value for maximum POI for each selected OSM URL to be active by
		// default is 5
		editor.putInt("osmMaxObject", 5);
		editor.commit();

		// add the default datasources to the preferences file
		DataSourceStorage.getInstance().fillDefaultDataSources();
	}

	/*
	 * Create zoom bar and returns FrameLayout. FrameLayout is created to be
	 * hidden and not added to view, Caller needs to add the frameLayout to
	 * view, and enable visibility when needed.
	 * 
	 * @param SharedOreference settings where setting is stored
	 * @return FrameLayout Hidden Zoom Bar
	 */
	private FrameLayout createZoomBar(SharedPreferences settings) {
		getMixViewData().setMyZoomBar(new SeekBar(this));
		getMixViewData().getMyZoomBar().setMax(100);
		getMixViewData().getMyZoomBar()
				.setProgress(settings.getInt("zoomLevel", 65));
		getMixViewData().getMyZoomBar().setOnSeekBarChangeListener(myZoomBarOnSeekBarChangeListener);
		getMixViewData().getMyZoomBar().setVisibility(View.INVISIBLE);

		FrameLayout frameLayout = new FrameLayout(this);

		frameLayout.setMinimumWidth(3000);
		frameLayout.addView(getMixViewData().getMyZoomBar());
		frameLayout.setPadding(10, 0, 10, 10);
		return frameLayout;
	}
	
	/* ********* Operator - Menu ******/
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int base = Menu.FIRST;
		/* define the first */
		MenuItem item1 = menu.add(base, base, base,
				getString(R.string.menu_item_1));
		MenuItem item2 = menu.add(base, base + 1, base + 1,
				getString(R.string.menu_item_2));
		MenuItem item3 = menu.add(base, base + 2, base + 2,
				getString(R.string.menu_item_3));
		MenuItem item4 = menu.add(base, base + 3, base + 3,
				getString(R.string.menu_item_4));
		MenuItem item5 = menu.add(base, base + 4, base + 4,
				getString(R.string.menu_item_5));
		MenuItem item6 = menu.add(base, base + 5, base + 5,
				getString(R.string.menu_item_6));
		MenuItem item7 = menu.add(base, base + 6, base + 6,
				getString(R.string.menu_item_7));

		/* assign icons to the menu items */
		item1.setIcon(drawable.icon_datasource);
		item2.setIcon(android.R.drawable.ic_menu_view);
		item3.setIcon(android.R.drawable.ic_menu_mapmode);
		item4.setIcon(android.R.drawable.ic_menu_zoom);
		item5.setIcon(android.R.drawable.ic_menu_search);
		item6.setIcon(android.R.drawable.ic_menu_info_details);
		item7.setIcon(android.R.drawable.ic_menu_share);

		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		/* Data sources */
		case 1:
			if (!getDataView().isLauncherStarted()) {
				Intent intent = new Intent(MixView.this, DataSourceList.class);
				startActivityForResult(intent, 40);
			} else {
				Toast.makeText(this, getString(R.string.no_website_available),
						Toast.LENGTH_LONG).show();
			}
			break;
		/* List view */
		case 2:
			/*
			 * if the list of titles to show in alternative list view is not
			 * empty
			 */
			if (getDataView().getDataHandler().getMarkerCount() > 0) {
				Intent intent1 = new Intent(MixView.this, MixListView.class); 
				startActivityForResult(intent1, 42);
			}
			/* if the list is empty */
			else {
				Toast.makeText(this, R.string.empty_list, Toast.LENGTH_LONG)
						.show();
			}
			break;
		/* Map View */
		case 3:
			Intent intent2 = new Intent(MixView.this, MixMap.class);
			startActivityForResult(intent2, 20);
			break;
		/* zoom level */
		case 4:
			getMixViewData().getMyZoomBar().setVisibility(View.VISIBLE);
			getMixViewData().setZoomProgress(getMixViewData().getMyZoomBar()
					.getProgress());
			break;
		/* Search */
		case 5:
			onSearchRequested();
			break;
		/* GPS Information */
		case 6:
			Location currentGPSInfo = getMixViewData().getMixContext().getLocationFinder().getCurrentLocation();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.general_info_text) + "\n\n"
					+ getString(R.string.longitude)
					+ currentGPSInfo.getLongitude() + "\n"
					+ getString(R.string.latitude)
					+ currentGPSInfo.getLatitude() + "\n"
					+ getString(R.string.altitude)
					+ currentGPSInfo.getAltitude() + "m\n"
					+ getString(R.string.speed) + currentGPSInfo.getSpeed()
					+ "km/h\n" + getString(R.string.accuracy)
					+ currentGPSInfo.getAccuracy() + "m\n"
					+ getString(R.string.gps_last_fix)
					+ new Date(currentGPSInfo.getTime()).toString() + "\n");
			builder.setNegativeButton(getString(R.string.close_button),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			AlertDialog alert = builder.create();
			alert.setTitle(getString(R.string.general_info_title));
			alert.show();
			break;
		/* Case 6: license agreements */
		case 7:
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			builder1.setMessage(getString(R.string.license));
			/* Retry */
			builder1.setNegativeButton(getString(R.string.close_button),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			AlertDialog alert1 = builder1.create();
			alert1.setTitle(getString(R.string.license_title));
			alert1.show();
			break;

		}
		return true;
	}

	/* ******** Operators - Sensors ****** */

	private SeekBar.OnSeekBarChangeListener myZoomBarOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		Toast t;

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			float myout = calcZoomLevel();

			getMixViewData().setZoomLevel(String.valueOf(myout));
			getMixViewData().setZoomProgress(getMixViewData().getMyZoomBar()
					.getProgress());

			t.setText("Radius: " + String.valueOf(myout));
			t.show();
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			Context ctx = seekBar.getContext();
			t = Toast.makeText(ctx, "Radius: ", Toast.LENGTH_LONG);
			// zoomChanging= true;
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			/* store the zoom range of the zoom bar selected by the user */
			editor.putInt("zoomLevel", getMixViewData().getMyZoomBar().getProgress());
			editor.commit();
			getMixViewData().getMyZoomBar().setVisibility(View.INVISIBLE);
			// zoomChanging= false;

			getMixViewData().getMyZoomBar().getProgress();

			t.cancel();
			//repaint after zoom level changed.
			repaint();
			setZoomLevel();
		}

	};


	public void onSensorChanged(SensorEvent evt) {
		try {

			if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				getMixViewData().getGrav()[0] = evt.values[0];
				getMixViewData().getGrav()[1] = evt.values[1];
				getMixViewData().getGrav()[2] = evt.values[2];

				augScreen.postInvalidate();
			} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				getMixViewData().getMag()[0] = evt.values[0];
				getMixViewData().getMag()[1] = evt.values[1];
				getMixViewData().getMag()[2] = evt.values[2];

				augScreen.postInvalidate();
			}

			SensorManager.getRotationMatrix(getMixViewData().getRTmp(),
					getMixViewData().getI(), getMixViewData().getGrav(),
					getMixViewData().getMag());

			int rotation = Compatibility.getRotation(this);

			if (rotation == 1) {
				SensorManager.remapCoordinateSystem(getMixViewData().getRTmp(),
						SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z,
						getMixViewData().getRot());
			} else {
				SensorManager.remapCoordinateSystem(getMixViewData().getRTmp(),
						SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z,
						getMixViewData().getRot());
			}
			getMixViewData().getTempR().set(getMixViewData().getRot()[0],
					getMixViewData().getRot()[1], getMixViewData().getRot()[2],
					getMixViewData().getRot()[3], getMixViewData().getRot()[4],
					getMixViewData().getRot()[5], getMixViewData().getRot()[6],
					getMixViewData().getRot()[7], getMixViewData().getRot()[8]);

			getMixViewData().getFinalR().toIdentity();
			getMixViewData().getFinalR().prod(getMixViewData().getM4());
			getMixViewData().getFinalR().prod(getMixViewData().getM1());
			getMixViewData().getFinalR().prod(getMixViewData().getTempR());
			getMixViewData().getFinalR().prod(getMixViewData().getM3());
			getMixViewData().getFinalR().prod(getMixViewData().getM2());
			getMixViewData().getFinalR().invert();

			getMixViewData().getHistR()[getMixViewData().getrHistIdx()].set(getMixViewData()
					.getFinalR());
			getMixViewData().setrHistIdx(getMixViewData().getrHistIdx() + 1);
			if (getMixViewData().getrHistIdx() >= getMixViewData().getHistR().length)
				getMixViewData().setrHistIdx(0);

			getMixViewData().getSmoothR().set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
			for (int i = 0; i < getMixViewData().getHistR().length; i++) {
				getMixViewData().getSmoothR().add(getMixViewData().getHistR()[i]);
			}
			getMixViewData().getSmoothR().mult(
					1 / (float) getMixViewData().getHistR().length);

			getMixViewData().getMixContext().updateSmoothRotation(getMixViewData().getSmoothR());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		try {
			killOnError();

			float xPress = me.getX();
			float yPress = me.getY();
			if (me.getAction() == MotionEvent.ACTION_UP) {
				getDataView().clickEvent(xPress, yPress);
			}//TODO add gesture events (low)

			return true;
		} catch (Exception ex) {
			// doError(ex);
			ex.printStackTrace();
			return super.onTouchEvent(me);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			killOnError();

			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (getDataView().isDetailsView()) {
					getDataView().keyEvent(keyCode);
					getDataView().setDetailsView(false);
					return true;
				} else {
					//TODO handle keyback to finish app correctly
					return super.onKeyDown(keyCode, event);
				}
			} else if (keyCode == KeyEvent.KEYCODE_MENU) {
				return super.onKeyDown(keyCode, event);
			} else {
				getDataView().keyEvent(keyCode);
				return false;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return super.onKeyDown(keyCode, event);
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
				&& accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
				&& getMixViewData().getCompassErrorDisplayed() == 0) {
			for (int i = 0; i < 2; i++) {
				Toast.makeText(getMixViewData().getMixContext(),
						"Compass data unreliable. Please recalibrate compass.",
						Toast.LENGTH_LONG).show();
			}
			getMixViewData().setCompassErrorDisplayed(getMixViewData()
					.getCompassErrorDisplayed() + 1);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		getDataView().setFrozen(false);
		if (getMixViewData().getSearchNotificationTxt() != null) {
			getMixViewData().getSearchNotificationTxt().setVisibility(View.GONE);
			getMixViewData().setSearchNotificationTxt(null);
		}
		return false;
	}


	/* ************ Handlers *************/

	public void doError(Exception ex1) {
		if (!fError) {
			fError = true;

			setErrorDialog();

			ex1.printStackTrace();
			try {
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
		}

		try {
			augScreen.invalidate();
		} catch (Exception ignore) {
		}
	}

	public void killOnError() throws Exception {
		if (fError)
			throw new Exception();
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			//doMixSearch(query);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

    // search 수정 중
	// 만약 마커로 못띄우는 경우 액티비티로 넘길예정
	private void doMixSearch(String query) {
/*
		setdWindow(new PaintScreen());
		getMixViewData().getMixContext().getActualMixView().refreshmaintainAugmentR();
		if (!getDataView().isInited()) {
			getDataView().init(getdWindow().getWidth(),
					getdWindow().getHeight());
		}
		MixView.getdWindow().setCanvas(augScreen.canvas);
*/
		// Download로부터 수정할 예정

		getDataView().setDataHandler(new DataHandler());
		getDataView().getDataHandler().addMarkers(getDataView().getMarkers());
		getDataView().getDataHandler().onLocationChanged(getDataView().getCurFix());

		DataHandler jLayer = getDataView().getDataHandler();

		ArrayList<Marker> searchResults = new ArrayList<Marker>();
		//Log.d("SEARCH-------------------0", "" + query);
		if (jLayer.getMarkerCount() > 0) {
			for (int i = 0; i < jLayer.getMarkerCount(); i++) {
				Marker ma = jLayer.getMarker(i);
                if(ma.getTitle().contains(query)){
                    // 이 단어를 포함하면 add
					searchResults.add(ma);
					/* the website for the corresponding title */
				}
			}
		}

		//getDataView().dRes.setMarkers(searchResults);
/*
		if (searchResults.size() > 0) {
            //getDataView().setFrozen(true);
			getDataView().dRes.setMarkers(searchResults);
            //jLayer.setMarkerList(searchResults);
			getDataView().setDataHandler(jLayer);
			//getDataView().refreshForSearch(getdWindow());
            // 이후에 dataview에서 뭘 해주면 해당 마커만 보일 것으로 판단

            Toast.makeText(this, "setMarkerList:" + searchResults.get(0), Toast.LENGTH_LONG).show();
		} else
			Toast.makeText(this,
					getString(R.string.search_failed_notification),
					Toast.LENGTH_LONG).show();
*/
	}

	/* ******* Getter and Setters ********** */

	public boolean isZoombarVisible() {
		return getMixViewData().getMyZoomBar() != null
				&& getMixViewData().getMyZoomBar().getVisibility() == View.VISIBLE;
	}
	
	public String getZoomLevel() {
		return getMixViewData().getZoomLevel();
	}
	
	/**
	 * @return the dWindow
	 */
	static PaintScreen getdWindow() {
		return dWindow;
	}


	/**
	 * @param dWindow
	 *            the dWindow to set
	 */
	static void setdWindow(PaintScreen dWindow) {
		MixView.dWindow = dWindow;
	}


	/**
	 * @return the dataView
	 */
	static DataView getDataView() {
		return dataView;
	}

	/**
	 * @param dataView
	 *            the dataView to set
	 */
	static void setDataView(DataView dataView) {
		MixView.dataView = dataView;
	}


	public int getZoomProgress() {
		return getMixViewData().getZoomProgress();
	}

	private void setZoomLevel() {
		float myout = calcZoomLevel();

		getDataView().setRadius(myout);
		//caller has the to control of zoombar visibility, not setzoom
		//mixViewData.getMyZoomBar().setVisibility(View.INVISIBLE);
		mixViewData.setZoomLevel(String.valueOf(myout));
		//setZoomLevel, caller has to call refreash download if needed.
//		mixViewData.setDownloadThread(new Thread(mixViewData.getMixContext().getDownloadManager()));
//		mixViewData.getDownloadThread().start();


		getMixViewData().getMixContext().getDownloadManager().switchOn();

	};

}


