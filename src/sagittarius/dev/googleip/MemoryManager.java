package sagittarius.dev.googleip;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MemoryManager {

	private volatile static MemoryManager sInstance = null;
	private DBOpenHelper helper;
	private SQLiteDatabase db;

	public static MemoryManager getInstance(Context context) {
		if (sInstance == null) {
			synchronized (MemoryManager.class) {
				if (sInstance == null) {
					sInstance = new MemoryManager(context);
				}
			}
		}
		return sInstance;
	}

	private MemoryManager(Context context) {
		helper = new DBOpenHelper(context);
		db = helper.getWritableDatabase();
	}

	public synchronized void addOrUpdate(Record record) {
		if (record == null) {
			return;
		}
		// 先检查有没有这个IP，有的话更新时间和consuming
		ContentValues cv = new ContentValues();
		cv.put("ip", record.ip);
		cv.put("time", record.time);
		cv.put("consuming", record.consuming);
		int updateCount = db.update("detect", cv, "ip = ?", new String[] { ""
				+ record.ip });
		if (updateCount > 0) {
			return;
		}
		// 没有就插入
		try {
			db.insert("detect", null, cv);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 超过500个就删除超出范围的
		int count = getCount();
		List<Long> ids = new ArrayList<Long>();
		if (count > 500) {
			int deleteCount = count - 500;
			Cursor cursor = null;
			try {
				cursor = db.query("detect", null, null, null, null, null,
						"time ASC", "" + deleteCount);
				if (cursor != null && cursor.moveToFirst()) {
					while (!cursor.isAfterLast()) {
						long id = cursor.getLong(cursor.getColumnIndex("_id"));
						ids.add(id);
						cursor.moveToNext();
					}
				}
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		if (ids.size() > 0) {
			String sss = "";
			String[] xxx = new String[ids.size()];
			for (int i = 0; i < ids.size(); i++) {
				if (i > 0) {
					sss += " || ";
				}
				sss += " _id = ? ";
				xxx[i] = ids.get(i) + "";
			}
			db.delete("detect", sss, xxx);
		}
	}

	private int getCount() {
		Cursor cursor = null;
		try {
			cursor = db.query("detect", new String[] { "COUNT(*)" }, null,
					null, null, null, null);
			if (cursor.moveToFirst()) {
				while (!cursor.isAfterLast()) {
					int count = cursor.getInt(0);
					return count;
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return 0;
	}

	public synchronized List<Record> querySuccess() {
		Cursor cursor = db.query("detect", null, null, null, null, null,
				"time ASC");
		List<Record> list = new ArrayList<Record>();
		while (cursor.moveToNext()) {
			Record record = new Record();
			record.ip = cursor.getString(cursor.getColumnIndex("ip"));
			record.time = cursor.getLong(cursor.getColumnIndex("time"));
			record.consuming = cursor.getLong(cursor
					.getColumnIndex("consuming"));
			list.add(record);
		}
		cursor.close();
		return list;
	}

}
