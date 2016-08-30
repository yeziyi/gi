package sagittarius.dev.googleip;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DetectServices3 extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
}
