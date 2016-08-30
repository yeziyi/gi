package sagittarius.dev.googleip;

import java.io.Serializable;

public class Record implements Serializable {
	private static final long serialVersionUID = 55648789765L;
	public String ip;
	public long time;
	public long consuming;
}
