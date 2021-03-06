package com.tot.totcheck;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainFragmentActivity extends FragmentActivity {
	
	private ViewPager viewPager;
	private ActionBar actionBar;
	private MenuItem updateText;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_fragment);
//		
//new AsyncTask<Void, Void, Void>() {
//
//	@Override
//	protected Void doInBackground(Void... params) {
//		try {
//			System.out.println(Parser.parse(Request.request("SELECT * FROM sector")));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
//}.execute();
			
		

		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
		viewPager.setOffscreenPageLimit(3);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				actionBar.setSelectedNavigationItem(arg0);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
		
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		TabListener tabListener = new TabListener() {
			
			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				
			}
			
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				viewPager.setCurrentItem(tab.getPosition());
			}
			
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				
			}
		};
		
		ActionBar.Tab tabFilter = actionBar.newTab();
		tabFilter.setText("DOWN");
		tabFilter.setTabListener(tabListener);
		actionBar.addTab(tabFilter);
		
		ActionBar.Tab tabNotification = actionBar.newTab();
		tabNotification.setText("แจ้งเตือน");
		tabNotification.setTabListener(tabListener);
		actionBar.addTab(tabNotification);
		
		ActionBar.Tab tabSetting = actionBar.newTab();
		tabSetting.setText("ตั้งค่า");
		tabSetting.setTabListener(tabListener);
		actionBar.addTab(tabSetting);
		
		boolean notificationTab = getIntent().getBooleanExtra("notificationTab", false);
		if (notificationTab) {
			actionBar.setSelectedNavigationItem(1);
			viewPager.setCurrentItem(1);
		}
		
		File apk = new File(SharedValues.FILE_PATH, SharedValues.FILE_NAME);
		if (apk.exists())
			apk.delete();
		

		ServerUtilities.register(getApplicationContext());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity_actions, menu);
	    updateText = menu.findItem(R.id.updateText);
	    
	    CheckUpdateTask task = new CheckUpdateTask(true);
    	task.execute();

	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case R.id.logo:
        case R.id.updateText:
        	CheckUpdateTask task = new CheckUpdateTask(false);
        	task.execute();
            return true;
        default:
            return super.onOptionsItemSelected(item);
		}
	};
	
	private class CheckUpdateTask extends AsyncTask<String, Integer, String> {

		private String version;
		private boolean inBackground;
		
		private ProgressDialog loading;
		
		public CheckUpdateTask(boolean inBackground) {
			this.inBackground = inBackground;
			loading = new ProgressDialog(MainFragmentActivity.this);
			loading.setTitle("ตรวจสอบเวอร์ชันล่าสุด");
			loading.setMessage("กำลังโหลด...");
//			loading.setCancelable(false);
			loading.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
		}
		
		@Override
		protected String doInBackground(String[] params) {
			
			try {
				version = Request.getVersion();
			} catch (ConnectTimeoutException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟนานเกินไป");
				version = "";
			} catch (SocketTimeoutException e) {
				loading.setMessage("รอผลตอบกลับนานเกินไป");
				version = "";
			} catch (HttpHostConnectException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟเวอร์ไม่ได้");
				version = "";
				e.printStackTrace();
			} catch (IOException e) {
				loading.setMessage("มีปัญหาการเชื่อมต่อ");
				version = "";
				e.printStackTrace();
			}
			return "some message";
		}

		@Override
		protected void onPreExecute() {
			if (!inBackground) {
				loading.show();
			}
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String message) {
			if (!inBackground) {
				loading.dismiss();
				
	        	final Dialog dialog = new Dialog(MainFragmentActivity.this);
	            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	            dialog.setContentView(R.layout.about_dialog);
	            dialog.setCancelable(true);
	            
	            if (!version.equals("")) {
		            double currentVersionName = 0;
					try {
						currentVersionName = Double.parseDouble(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
		            double versionName = Double.parseDouble(version.split(",")[0]);
		            
		            if (currentVersionName == versionName) {
		            	TextView textViewCurrentVersionName = (TextView) dialog.findViewById(R.id.textViewCurrentVersionName);
		            	textViewCurrentVersionName.setText("เวอร์ชันปัจจุบัน " + currentVersionName);
		            	
		            	TextView textViewNewVersionName = (TextView) dialog.findViewById(R.id.textViewNewVersionName);
		            	textViewNewVersionName.setText("เวอร์ชันล่าสุดแล้ว");
		
		            	Button buttonUpdate = (Button) dialog.findViewById(R.id.buttonUpdate);
		            	buttonUpdate.setVisibility(View.GONE);
		            }
		            else {
		            	TextView textViewCurrentVersionName = (TextView) dialog.findViewById(R.id.textViewCurrentVersionName);
		            	textViewCurrentVersionName.setText("เวอร์ชันปัจจุบัน " + currentVersionName);
		            	
		            	TextView textViewNewVersionName = (TextView) dialog.findViewById(R.id.textViewNewVersionName);
		            	textViewNewVersionName.setText("เวอร์ชันล่าสุด " + versionName);
		
		                Button buttonUpdate = (Button) dialog.findViewById(R.id.buttonUpdate);
		                buttonUpdate.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								LoadAPKTask task = new LoadAPKTask();
								task.execute();
							}
						});
		                
		                if (currentVersionName > versionName)
		                	buttonUpdate.setVisibility(View.GONE);
		            }
	            }
	            else {
	            	TextView textViewCurrentVersionName = (TextView) dialog.findViewById(R.id.textViewCurrentVersionName);
	            	textViewCurrentVersionName.setText("เชื่อมต่อเซิร์ฟเวอร์ไม่ได้ 1");
	
	            	TextView textViewNewVersionName = (TextView) dialog.findViewById(R.id.textViewNewVersionName);
	            	textViewNewVersionName.setVisibility(View.GONE);
	
	            	Button buttonUpdate = (Button) dialog.findViewById(R.id.buttonUpdate);
	            	buttonUpdate.setVisibility(View.GONE);
	            }
	            
	            TextView textViewRegId = (TextView) dialog.findViewById(R.id.textViewRegId);
	            textViewRegId.setText(SharedValues.getStringPref(getApplicationContext(), SharedValues.KEY_REGID));
	            dialog.show();
			}
			else {
				if (!version.equals("")) {
		            double currentVersionName = 0;
					try {
						currentVersionName = Double.parseDouble(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
		            double versionName = Double.parseDouble(version.split(",")[0]);
		            
		            if (currentVersionName == versionName) {
		            	
		            }
		            else {
		            	if (!(currentVersionName > versionName))
		            		updateText.setVisible(true);
		            }
				}
			}
		}
		
		public AsyncTask<String, Integer, String> execute() {
			return execute("test");
		}
	}
	
	private class LoadAPKTask extends AsyncTask<String, Integer, String> {

		private ProgressDialog loading;
		
		public LoadAPKTask() {
			loading = new ProgressDialog(MainFragmentActivity.this);
			loading.setTitle("ดาวน์โหลดตัวติดตั้ง");
			loading.setMessage("กำลังโหลด...");
//			loading.setCancelable(false);
			loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}
		
		@Override
		protected String doInBackground(String[] params) {
			try {
				URL url = new URL(SharedValues.HOST_APK);
				URLConnection connection = url.openConnection();
				connection.connect();
				
				int fileLength = connection.getContentLength();
				
				File dir = new File(SharedValues.FILE_PATH);
				dir.mkdirs();
				
				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(SharedValues.FILE_PATH + SharedValues.FILE_NAME);
				
				byte data[] = new byte[1024];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress((int) (total * 100 / fileLength));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    return SharedValues.FILE_NAME;
		}
		
		@Override
		protected void onPreExecute() {
			loading.show();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String message) {
			loading.dismiss();
			
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			File apk = new File(SharedValues.FILE_PATH, SharedValues.FILE_NAME);
			if (apk.exists()) {
				intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    startActivity(intent);
			}
			else {
				final Dialog dialog = new Dialog(MainFragmentActivity.this);
	            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	            dialog.setContentView(R.layout.about_dialog);
	            dialog.setCancelable(true);
	            
	            TextView textViewCurrentVersionName = (TextView) dialog.findViewById(R.id.textViewCurrentVersionName);
            	textViewCurrentVersionName.setText("เชื่อมต่อเซิร์ฟเวอร์ไม่ได้ 2");
            	
            	TextView textViewNewVersionName = (TextView) dialog.findViewById(R.id.textViewNewVersionName);
            	textViewNewVersionName.setVisibility(View.GONE);

            	Button buttonUpdate = (Button) dialog.findViewById(R.id.buttonUpdate);
            	buttonUpdate.setVisibility(View.GONE);
            	
            	dialog.show();
			}
		}
		
		public AsyncTask<String, Integer, String> execute() {
			return execute("test");
		}
	}
}

class PagerAdapter extends FragmentPagerAdapter {

	public PagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int arg0) {
		Fragment fragment = null;
		
		if (arg0 == 0)
			fragment = new FilterFragment();
		else if (arg0 == 1)
			fragment = new NotificationFragment();
		else
			fragment = new SettingFragment();
		
		return fragment;
	}

	@Override
	public int getCount() {
		return 3;
	}
}
