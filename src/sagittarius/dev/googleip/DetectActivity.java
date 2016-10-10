package sagittarius.dev.googleip;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
	private Handler mHandler = new Handler(Looper.getMainLooper());

	private BroadcastReceiver mSuccessReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				Record record = (Record) intent.getSerializableExtra(KEY_IP);
				MemoryManager.getInstance(getApplicationContext()).addOrUpdate(
						record);
				if (record != null) {
					if (mListView.getLastVisiblePosition() >= mAdapter
							.getCount() - 1) {
						mHandler.post(new Runnable() {

							@Override
							public void run() {
								mListView.setSelection(mAdapter.getCount());
							}
						});
					}
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
		mListView.setEmptyView(findViewById(R.id.progressBar));
		mAdapter = new DetectAdapter();
		mListView.setAdapter(mAdapter);
		mFailText = (TextView) findViewById(R.id.failip);
		findViewById(R.id.stop).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					unregisterReceiver(mSuccessReceiver);
				} catch (Exception e) {
				}
				try {
					unregisterReceiver(mFailReceiver);
				} catch (Exception e) {
				}
				stopService(new Intent(DetectActivity.this,
						DetectServices1.class));
				stopService(new Intent(DetectActivity.this,
						DetectServices2.class));
				stopService(new Intent(DetectActivity.this,
						DetectServices3.class));
				Toast.makeText(getApplicationContext(),
						getApplicationContext().getText(R.string.stopdetect),
						Toast.LENGTH_SHORT).show();
			}
		});
		IntentFilter filter1 = new IntentFilter(SUCCESS_ACTION);
		registerReceiver(mSuccessReceiver, filter1);
		IntentFilter filter2 = new IntentFilter(FAIL_ACTION);
		registerReceiver(mFailReceiver, filter2);
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
			startService(list1, DetectServices1.class);
			startService(list2, DetectServices2.class);
			startService(list3, DetectServices3.class);
		}
	}

	private void startService(List<String> list, Class<?> cls) {
		if (list == null) {
			return;
		}
		ArrayList<String> slist = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			slist.add(list.get(i));
			if (i != 0 && (i % 2000 == 0 || i == list.size() - 1)) {
				Intent intent = new Intent(getApplicationContext(), cls);
				intent.putStringArrayListExtra(KEY_IP_LIST, slist);
				getApplicationContext().startService(intent);
				slist = new ArrayList<String>();
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		try {
			unregisterReceiver(mSuccessReceiver);
		} catch (Exception e) {
		}
		try {
			unregisterReceiver(mFailReceiver);
		} catch (Exception e) {
		}
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
		public Record getItem(int position) {
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
			final Record record = getItem(position);
			TextView text = (TextView) convertView.findViewById(R.id.text);
			Button copy = (Button) convertView.findViewById(R.id.copy);
			Button open = (Button) convertView.findViewById(R.id.open);
			text.setText(record.ip);
			copy.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ClipData clip = ClipData.newPlainText(record.ip + "",
							record.ip + "");
					ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					clipboard.setPrimaryClip(clip);
					Toast.makeText(
							getApplicationContext(),
							getApplicationContext().getText(
									R.string.copysuccess), Toast.LENGTH_SHORT)
							.show();
				}
			});
			open.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO
				}
			});
			return convertView;
		}
	}

}
