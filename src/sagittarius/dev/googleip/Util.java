package sagittarius.dev.googleip;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class Util {

	public static boolean detectIpHttps(String ip) {
		SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory
				.getDefault();
		SSLSocket socket = null;
		try {
			socket = initSSLSocket(factory, ip, 5000, 5000);

			OutputStream socketOut = socket.getOutputStream();
			socketOut.write(Constants.GOOGLEHK_REQUEST.getBytes());

			InputStream socketIn = socket.getInputStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			int len = -1;
			while ((len = socketIn.read(buff)) != -1) {
				buffer.write(buff, 0, len);
			}
			String result = new String(buffer.toByteArray());
			Log.e("", result + "");
			if (result.toLowerCase().contains(
					Constants.SUCCESS_MARK.toLowerCase())
					&& result.toLowerCase().contains("google")) {
				return true;
			}
		} catch (Exception e) {
		} finally {
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (Exception e) {
			}
		}
		return false;
	}

	private static SSLSocket initSSLSocket(SSLSocketFactory factory,
			final String host, final int connectTimeOut, final int soTimeOut)
			throws UnknownHostException, IOException {
		SSLSocket socket = (SSLSocket) factory.createSocket();
		socket.connect(new InetSocketAddress(host, 443), connectTimeOut);
		String[] sup = socket.getSupportedCipherSuites();
		socket.setEnabledCipherSuites(sup);
		socket.setSoTimeout(soTimeOut);
		return socket;
	}

	public static List<String> getDetectIP(Context context) {
		// 历史记录上成功的IP，固定写死的500个IP，成功IP的兄弟IP，TODO 通过解析域名得到的IP，TODO 数据统计得到的IP，泛IP
		List<String> ret = new ArrayList<String>();
		List<String> mlist = getMemoryIP(context);
		ret.addAll(mlist);
		ret.addAll(getProvenIP(context));
		ret.addAll(getMemoryBrotherIP(context, mlist));
		ret.addAll(getGenIP(context));
		removeDuplicateWithOrder(ret);
		return ret;
	}

	public static List<String> getMemoryIP(Context context) {
		List<String> ret = new ArrayList<String>();
		List<Record> list = MemoryManager.getInstance(context).querySuccess();
		for (Record record : list) {
			ret.add(record.ip);
		}
		// 去掉重复项并保持顺序
		removeDuplicateWithOrder(ret);
		return ret;
	}

	public static List<String> getProvenIP(Context context) {
		List<String> list = new ArrayList<String>();
		// 读取配置好的IP，要trim()
		try {
			InputStreamReader inputReader = new InputStreamReader(context
					.getResources().getAssets().open("megtrx"));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			while ((line = bufReader.readLine()) != null) {
				line = line.trim();
				if (TextUtils.isEmpty(line)) {
					list.add(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Collections.shuffle(list);
		return list;
	}

	public static List<String> getMemoryBrotherIP(Context context,
			List<String> list) {
		// 过滤掉相同的IP段
		List<String> tags = new ArrayList<String>();
		for (String ip : list) {
			String tag = getTag(ip);
			if (!TextUtils.isEmpty(tag)) {
				tags.add(tag);
			}
		}
		removeDuplicateWithOrder(tags);
		List<String> result = new ArrayList<String>();
		for (String tag : tags) {
			for (int i = 0; i <= 255; i++) {
				result.add(tag + i);
			}
		}
		removeDuplicateWithOrder(result);
		return result;
	}

	public static List<String> getGenIP(Context context) {
		List<String> list = new ArrayList<String>();
		try {
			InputStreamReader inputReader = new InputStreamReader(context
					.getResources().getAssets().open("triple"));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			while ((line = bufReader.readLine()) != null) {
				line = line.trim();
				if (TextUtils.isEmpty(line)) {
					list.add(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] tags = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			tags[i] = list.get(i);
		}
		list = new ArrayList<String>();
		for (String tag : tags) {
			tag = tag.trim();
			for (int i = 0; i <= 255; i++) {
				list.add((tag + i).trim());
			}
		}
		// 随机排序
		Collections.shuffle(list);
		return list;
	}

	public static String getTag(String ip) {
		if (TextUtils.isEmpty(ip)) {
			return "";
		}
		ip = ip.trim();
		String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
		if (ip.matches(regex)) {
			int index = ip.lastIndexOf(".");
			return ip.substring(0, index + 1).trim();
		} else {
			Log.e("test", "illegalip = " + ip);
		}
		return "";
	}

	public static void removeDuplicateWithOrder(List<String> list) {
		Set<String> set = new HashSet<String>();
		List<String> newList = new ArrayList<String>();
		for (Iterator<String> iter = list.iterator(); iter.hasNext();) {
			String element = iter.next().trim();
			if (set.add(element)) {
				newList.add(element);
			}
		}
		list.clear();
		list.addAll(newList);
	}

}
