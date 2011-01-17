/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx;

/**
 * @author Erik
 *
 */
public class StatusResponse
{
//	public enum StatusTypes { Info, Warn, Error };
	public static final int ST_INFO = 1;
	public static final int ST_WARN = 2;
	public static final int ST_ERROR = 3;

	public static final int STATUS_SUCCESS = 0;
	public static final int STATUS_UP_TO_DATE = 1;
	public static final int STATUS_ERROR = 2000;
	public static final int STATUS_ACCT_INVALID = 2001;
	public static final int STATUS_ACCT_ERROR = 2002;
	public static final int STATUS_ACCT_NOTFOUND = 2003;
	public static final int STATUS_ACCT_CLOSED = 2004;
	public static final int STATUS_ACCT_DENIED = 2005;
	public static final int STATUS_ACCT_SRC_NOTFOUND = 2006;
	public static final int STATUS_ACCT_SRC_CLOSED = 2007;
	public static final int STATUS_ACCT_SRC_DENIED = 2008;
	public static final int STATUS_ACCT_DEST_NOTFOUND = 2009;
	public static final int STATUS_ACCT_DEST_CLOSED = 2010;
	public static final int STATUS_ACCT_DEST_DENIED = 2011;
	public static final int STATUS_AMT_INVALID = 2012;
	public static final int STATUS_DATE_TOO_EARLY = 2014;
	public static final int STATUS_DATE_TOO_LATE = 2015;
	public static final int STATUS_XACT_COMMITTED = 2016;
	public static final int STATUS_XACT_CANCELLED = 2017;
	public static final int STATUS_SERVER_INVALID = 2018;
	public static final int STATUS_XACT_DUPLICATE = 2019;
	public static final int STATUS_DATE_INVALID = 2020;
	public static final int STATUS_VER_INVALID = 2021;
	public static final int STATUS_TAN_INVALID = 2022;
	public static final int STATUS_FITID_INVALID = 2023;
	public static final int STATUS_BRANCHID_MISSING = 2025;
	public static final int STATUS_BANKID_INCONSISTENT = 2026;
	public static final int STATUS_DATE_RANGE = 2027;
	public static final int STATUS_REQUEST_IGNORED = 2028;
	public static final int STATUS_MFA_REQUIRED = 3000;
	public static final int STATUS_MFA_INVALID = 3001;
	public static final int STATUS_TOKEN_MISSING = 6500;
	public static final int STATUS_EMBED_XACT_EXPIRED = 6501;
	public static final int STATUS_XACT_EXPIRED = 6502;
	public static final int STATUS_STOP_CHECK_IN_PROGRESS = 10000;
	public static final int STATUS_TOO_MANY_STOP_CHECK = 10500;
	public static final int STATUS_INVALID_PAYEE = 10501;
	public static final int STATUS_INVALID_PAYEE_ADDRESS = 10502;
	public static final int STATUS_INVALID_PAYEE_ACCT = 10503;
	public static final int STATUS_INSUFFICIENT_FUNDS = 10504;
	public static final int STATUS_CANT_MOD_ELEMENT = 10505;
	public static final int STATUS_CANT_MOD_SRC_ACCT = 10506;
	public static final int STATUS_CANT_MOD_DEST_ACCT = 10507;
	public static final int STATUS_FREQ_INVALID = 10508;
	public static final int STATUS_MODEL_CANCELLED = 10509;
	public static final int STATUS_PAYEE_ID_INVALID = 10510;
	public static final int STATUS_PAYEE_CITY_INVALID = 10511;
	public static final int STATUS_PAYEE_STATE_INVALID = 10512;
	public static final int STATUS_PAYEE_POSTAL_INVALID = 10513;
	public static final int STATUS_XACT_PROCESSED = 10514;
	public static final int STATUS_PAYEE_NOT_MODIFIABLE = 10515;
	public static final int STATUS_WIRE_BENEFICIARY_INVALID = 10516;
	public static final int STATUS_PAYEE_NAME_INVALID = 10517;
	public static final int STATUS_MODEL_UNKNOWN = 10518;
	public static final int STATUS_PAYEE_LISTID_INVALID = 10519;
	public static final int STATUS_TABLETYPE_NOTFOUND = 10600;
	public static final int STATUS_INVEST_XACT_DOWNLOAD_UNSUPPORT = 12250;
	public static final int STATUS_INVEST_POS_DOWNLOAD_UNSUPPORT = 12251;
	public static final int STATUS_INVEST_POS_DATE_UNAVAIL = 12252;
	public static final int STATUS_INVEST_OPEN_UNSUPPORT = 12253;
	public static final int STATUS_INVEST_BALANCE_DOWNLOAD_UNSUPPORT = 12254;
	public static final int STATUS_401K_UNAVAIL = 12255;
	public static final int STATUS_SECURITY_NOTFOUND = 12500;
	public static final int STATUS_LOGIN_OUT_OF_BAND = 13000;
	public static final int STATUS_ENROLL_FAILURE = 13500;
	public static final int STATUS_ENROLL_ALREADY = 13501;
	public static final int STATUS_SERVICE_INVALID = 13502;
	public static final int STATUS_CANT_MOD_USER = 13503;
	public static final int STATUS_FI_INVALID = 13504;
	public static final int STATUS_1099_UNAVAIL = 14500;
	public static final int STATUS_1099_UNAVAIL_USERID = 14501;
	public static final int STATUS_W2_UNAVAIL = 14600;
	public static final int STATUS_W2_UNAVAIL_USERID = 14601;
	public static final int STATUS_1098_UNAVAIL = 14700;
	public static final int STATUS_1098_UNAVAIL_USERID = 14701;
	public static final int STATUS_PINCH_NEEDED = 15000;
	public static final int STATUS_BAD_LOGIN = 15500;
	public static final int STATUS_ACCT_BUSY = 15501;
	public static final int STATUS_ACCT_LOCKED = 15502;
	public static final int STATUS_PINCH_FAILURE = 15503;
	public static final int STATUS_RANDOM_FAILURE = 15504;
	public static final int STATUS_COUNTRY_INVALID = 15505;
	public static final int STATUS_EMPTY_REQUEST = 15506;
	public static final int STATUS_PINCH_REQUIRED = 15507;
	public static final int STATUS_XACT_DENIED = 15508;
	public static final int STATUS_ONE_ACCT_ONLY = 15509;
	public static final int STATUS_CLIENTUID_REJECTED = 15510;
	public static final int STATUS_CALL_US = 15511;
	public static final int STATUS_AUTHTOKEN_REQUIRED = 15512;
	public static final int STATUS_AUTHTOKEN_INVALID = 15513;
	public static final int STATIC_NO_HTML_PERMITTED = 16500;
	public static final int STATIC_MAIL_DEST_UNKNOWN = 16501;
	public static final int STATIC_URL_INVALID = 16502;
	public static final int STATIC_URL_UNAVAIL = 16503;
	public static final int STATIC_IMAGE_UNAVAIL = 16510;
	public static final int STATIC_IMAGE_EXPIRED = 16511;
	public static final int STATIC_IMAGEREF_NOTFOUND = 16512;
	public static final int STATIC_IMAGE_SERVER_UNAVAIL = 16513;

	public int code;
	public int sev;
	public String msg;
	// currency
	
	public StatusResponse()
	{
	}
	
	public StatusResponse(TransferObject in)
	{
		this.code = Integer.parseInt(in.getAttr("CODE"));
		
		String statType = in.getAttr("SEVERITY");
		if(statType != null)
		{
			if(statType.equals("INFO"))
			{
				this.sev = ST_INFO; 
			}
			else if(statType.equals("WARN"))
			{
				this.sev = ST_WARN;
			}
			else if(statType.equals("ERROR"))
			{
				this.sev = ST_ERROR;
			}
		}
		
		this.msg = in.getAttr("MESSAGE");
		if(this.msg == null) in.getAttr("MESSAGE2");
	}
}
