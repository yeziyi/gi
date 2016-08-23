package sagittarius.dev.googleip;

import com.cwtlib.aesencript.SecurityUtil;

public class Constants {
	public static final String GOOGLEHK_REQUEST = SecurityUtil
			.deCrypto("AB0968AB6030A87099AB1628D2C5E58ABE2FAB9076BC5D7B8ED5CA8B6047B46BDDD08CD82A8CBABE6B451570DE0A31382C1BC03F001561E2722D9BDB444711F5");

	public static final String SUCCESS_MARK = SecurityUtil
			.deCrypto("F8EBEC3837E7B1E7477D409870396607");
}
