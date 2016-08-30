package sagittarius.dev.googleip;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DetectActivity extends Activity {

	public static final String SUCCESS_ACTION = "SUCCESS_ACTION";
	public static final String FAIL_ACTION = "FAIL_ACTION";
	public static final String KEY_IP = "KEY_IP";
	public static final String KEY_IP_LIST = "KEY_IP_LIST";

	private ListView mListView;
	private DetectAdapter mAdapter;
	private List<Record> mSuccessList = new ArrayList<Record>();
	private LayoutInflater mInflater;
	private TextView mFailText;

	private BroadcastReceiver mSuccessReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				Record record = (Record) intent.getSerializableExtra(KEY_IP);
				if (record != null) {
					mSuccessList.add(record);
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	};

	private BroadcastReceiver mFailReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (intent != null) {
					Record record = (Record) intent
							.getSerializableExtra(KEY_IP);
					if (record != null && !TextUtils.isEmpty(record.ip)) {
						mFailText.setText(record.ip + ""
								+ context.getString(R.string.detectfail));
					}
				}
			} catch (Exception e) {
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detect_layout);
		mInflater = LayoutInflater.from(this);
		mListView = (ListView) findViewById(R.id.listview);
		mAdapter = new DetectAdapter();
		mListView.setAdapter(mAdapter);
		mFailText = (TextView) findViewById(R.id.failip);
		findViewById(R.id.stop).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});
		IntentFilter filter1 = new IntentFilter(SUCCESS_ACTION);
		registerReceiver(mSuccessReceiver, filter1);
		IntentFilter filter2 = new IntentFilter(FAIL_ACTION);
		registerReceiver(mSuccessReceiver, filter2);
		initData();
	}

	private void initData() {
		List<String> allIp = Util.getDetectIP(getApplicationContext());
		if (allIp != null) {
			ArrayList<String> list1 = new ArrayList<String>();
			ArrayList<String> list2 = new ArrayList<String>();
			ArrayList<String> list3 = new ArrayList<String>();
			for (int i = 0; i < allIp.size(); i++) {
				int yu = i % 3;
				if (yu == 0) {
					list1.add(allIp.get(i));
				} else if (yu == 1) {
					list2.add(allIp.get(i));
				} else {
					list3.add(allIp.get(i));
				}
			}
			Intent intent1 = new Intent(getApplicationContext(),
					DetectServices1.class);
			intent1.putStringArrayListExtra(KEY_IP_LIST, list1);
			getApplicationContext().startService(intent1);
			Intent intent2 = new Intent(getApplicationContext(),
					DetectServices2.class);
			intent1.putStringArrayListExtra(KEY_IP_LIST, list2);
			getApplicationContext().startService(intent2);
			Intent intent3 = new Intent(getApplicationContext(),
					DetectServices3.class);
			intent1.putStringArrayListExtra(KEY_IP_LIST, list3);
			getApplicationContext().startService(intent3);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mSuccessReceiver);
		unregisterReceiver(mFailReceiver);
		stopService(new Intent(this, DetectServices1.class));
		stopService(new Intent(this, DetectServices2.class));
		stopService(new Intent(this, DetectServices3.class));
		super.onDestroy();
	}

	private class DetectAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mSuccessList.size();
		}

		@Override
		public Object getItem(int position) {
			return mSuccessList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.detect_item, null);
			}
			return convertView;
		}
	}

}
