package com.tot.totcheck;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.app.ProgressDialog;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class FilterFragment extends Fragment {
	
	private SwipeRefreshLayout swipeRefreshLayout;
	
	private Spinner spinnerProvinces;
	private ListView listView;
	
	public FilterFragment() {
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_filter, container, false);
		
		spinnerProvinces = (Spinner) view.findViewById(R.id.spinnerProvinces);
		GetProvincesTask getProvinces = new GetProvincesTask();
		getProvinces.execute();
		
		spinnerProvinces.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String province = ((TextView) view.findViewById(R.id.textViewProvince)).getText().toString();
				GetListTask job = new GetListTask(province);
				job.execute();
				SharedValues.setStringPref(getActivity().getApplicationContext(), SharedValues.KEY_LAST_USED_PROVINCE, province);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});

		swipeRefreshLayout = new SwipeRefreshLayout(getActivity().getApplicationContext());
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				GetProvincesTask getProvinces = new GetProvincesTask();
				getProvinces.execute();
				
				swipeRefreshLayout.setRefreshing(false);
			}
		});
		swipeRefreshLayout.addView(view);
		
		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();
				swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
			}
		});
		
		return swipeRefreshLayout;
	}
	
	private class GetProvincesTask extends AsyncTask<String, Integer, String> {

		private List<SpinnerRowItem> list;
		
		private ProgressDialog loading;
		
		public GetProvincesTask() {
			loading = new ProgressDialog(getActivity());
			loading.setTitle("รายชื่อจังหวัด");
			loading.setMessage("กำลังโหลด...");
//			loading.setCancelable(false);
			loading.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
		}
		
		@Override
		protected String doInBackground(String[] params) {
			list = new ArrayList<SpinnerRowItem>();
			
			try {
				String parsed = Parser.parse(Request.requestProvincesAndAmount());
				JSONArray js = new JSONArray(parsed);
				for (int i = 0; i < js.length(); i++) {
					JSONObject jo = js.getJSONObject(i);
					SpinnerRowItem item = new SpinnerRowItem(jo.getString("province"), jo.getInt("amount"));
					list.add(item);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (ConnectTimeoutException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟนานเกินไป");
			} catch (SocketTimeoutException e) {
				loading.setMessage("รอผลตอบกลับนานเกินไป");
			} catch (HttpHostConnectException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟเวอร์ไม่ได้");
				e.printStackTrace();
			} catch (IOException e) {
				loading.setMessage("มีปัญหาการเชื่อมต่อ");
				e.printStackTrace();
			}
			return "some message";
		}

		@Override
		protected void onPreExecute() {
			loading.show();
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(String message) {
			loading.dismiss();
			SpinnerRowAdapter adapter = new SpinnerRowAdapter(getActivity().getApplicationContext(), list);
			spinnerProvinces.setAdapter(adapter);

			String x = SharedValues.getStringPref(getActivity().getApplicationContext(), SharedValues.KEY_LAST_USED_PROVINCE);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getProvince().equals(x))
					spinnerProvinces.setSelection(i);
			}
		}
		
		public AsyncTask<String, Integer, String> execute() {
			return execute("test");
		}
	}
	
	private class GetListTask extends AsyncTask<String, Integer, String> {

		private String province;
		private List<ListViewRowItem> list;
		
		private ProgressDialog loading;
		
		public GetListTask(String province) {
			this.province = province;
			loading = new ProgressDialog(getActivity());
			loading.setTitle("รายการอุปกรณ์");
			loading.setMessage("กำลังโหลด...");
//			loading.setCancelable(false);
			loading.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
		}
		
		@Override
		protected String doInBackground(String[] params) {
			list = new ArrayList<ListViewRowItem>();
			
			try {
				String parsed = Parser.parse(Request.requestDownList(province));
				JSONArray js = new JSONArray(parsed);
				for (int i = 0; i < js.length(); i++) {
					JSONObject jo = js.getJSONObject(i);
					ListViewRowItem item = new ListViewRowItem(jo.getString("id_nu"), jo.getString("node_id"), jo.getString("node_ip"), jo.getString("node_time_down"), jo.getString("node_name"), jo.getString("temp"), province);
					list.add(item);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
				list.add(new ListViewRowItem());
			} catch (ConnectTimeoutException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟนานเกินไป");
			} catch (SocketTimeoutException e) {
				loading.setMessage("รอผลตอบกลับนานเกินไป");
			} catch (HttpHostConnectException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟเวอร์ไม่ได้");
				e.printStackTrace();
			} catch (IOException e) {
				loading.setMessage("มีปัญหาการเชื่อมต่อ");
				e.printStackTrace();
			}
			
			if (list.size() == 0)
				list.add(new ListViewRowItem());
			
			return "some message";
		}

		@Override
		protected void onPreExecute() {
			loading.show();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String message) {
			loading.dismiss();
			listView.setAdapter(null);
			ListViewRowAdapter adapter = new ListViewRowAdapter(getActivity().getApplicationContext(), list, false);
			listView.setAdapter(adapter);
		}
		
		public AsyncTask<String, Integer, String> execute() {
			return execute("test");
		}
	}
}