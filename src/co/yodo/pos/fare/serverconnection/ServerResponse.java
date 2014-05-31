package co.yodo.pos.fare.serverconnection;

public class ServerResponse {
	/**
	 * XML root element
	 */
	public static final String ROOT_ELEMENT = "Yodoresponses";
	
	/**
	 * XML sub root element
	 */
	public static final String SUB_ROOT_ELEMENT = "Yodoresponse";
	
	public static final String CODE_ELEM = "code";
	
	public static final String AUTH_NUM_ELEM = "authNumber";
	
	public static final String MESSAGE_ELEM = "message";
	
	public static final String PARAMS_ELEM = "params";
	
	public static final String DEBIT_ELEM = "MerchantDebitWTCost";
	
	public static final String CREDIT_ELEM = "MerchantCreditWTCost";
	
	public static final String SETTLEMENT_ELEM = "Settlement";
	
	public static final String EQUIPMENTS_ELEM = "Equipments";
	
	public static final String LEASE_ELEM = "Lease";
	
	public static final String TOTAL_LEASE_ELEM = "TotalLease";
	
	public static final String BALANCE_ELEM = "balance";
	
	public static final String VALUE_SEPARATOR = ":";
	
	public static final String ENTRY_SEPARATOR = "-";
	
	/**
	 * Response code number
	 */
	public String code;
	
	/**
	 * Response authentication number
	 */
	public String authNumber;
	
	/**
	 * Response message
	 */
	public String message;
	
	/**
	 * Response parameters
	 */
	public String params;
	
	/**
	 * Response extraData
	 */
	public String extraData;
	
	public String getRootElement() {
		return ROOT_ELEMENT;
	}
	
	public String getSubRootElement() {
		return SUB_ROOT_ELEMENT;
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getAuthNumber() {
		return authNumber;
	}
	public void setAuthNumber(String authNumber) {
		this.authNumber = authNumber;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getParams() {
		return params;
	}
	
	public void setParams(String params) {
		this.params = params;
	}
	public void setExtraData(String extraData) {
		this.extraData = extraData;
	}

	/**
	 * @return the balanceElem
	 */
	public static String getBalanceElem() {
		return BALANCE_ELEM;
	}
	
	@Override
	public String toString() {
		return code + " " + authNumber + " " + message + " " + params;
		
	}
}
