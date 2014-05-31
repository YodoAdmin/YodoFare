package co.yodo.pos.fare.helper;

import co.yodo.pos.fare.serverconnection.ServerResponse;

public interface YodoBase {
	public void setData(ServerResponse data, int queryType);
}
