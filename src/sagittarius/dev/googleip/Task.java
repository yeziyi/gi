package sagittarius.dev.googleip;

import android.content.Context;
import android.content.Intent;

public class Task implements Runnable {

	private String mIP;
	private Context mContext;

	public Task(Context context, String ip) {
		mContext = context;
		mIP = ip;
	}

	@Override
	public void run() {
		long t1 = System.currentTimeMillis();
		boolean good = Util.detectIpHttps(mIP);
		long consuming = System.currentTimeMillis() - t1;

		// 保存到MemorySystem
		Record record = new Record();
		record.ip = mIP;
		record.consuming = consuming;
		record.time = System.currentTimeMillis();

		if (good) {
			MemoryManager.getInstance(mContext).addOrUpdate(record);
			Intent intent = new Intent(DetectActivity.SUCCESS_ACTION);
			intent.putExtra(DetectActivity.KEY_IP, record);
			mContext.sendBroadcast(intent);
		} else {
			Intent intent = new Intent(DetectActivity.FAIL_ACTION);
			intent.putExtra(DetectActivity.KEY_IP, record);
			mContext.sendBroadcast(intent);
		}

	}
}
