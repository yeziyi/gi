package sagittarius.dev.googleip;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DetectServices3 extends Service {

	private ThreadPoolExecutor mExecutor;

	@Override
	public void onCreate() {
		super.onCreate();
		// 初始化线程池
		int max = 200;
		while (true) {
			try {
				mExecutor = new ThreadPoolExecutor(max, max, 0L,
						TimeUnit.MILLISECONDS,
						new LinkedBlockingQueue<Runnable>());
				Log.e("", "max = " + max);
				break;
			} catch (Throwable e) {
				e.printStackTrace();
				System.gc();
				max -= 25;
			}
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 开始检测
		ArrayList<String> list = intent
				.getStringArrayListExtra(DetectActivity.KEY_IP_LIST);
		if (list != null) {
			for (String ip : list) {
				Task task = new Task(getApplicationContext(), ip);
				mExecutor.execute(task);
			}
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// 停掉所有线程
		if (mExecutor != null) {
			mExecutor.shutdown();
		}
		super.onDestroy();
	}

}
