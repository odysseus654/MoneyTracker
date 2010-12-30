package name.anderson.odysseus.moneytracker.ofx.signon;

import java.util.*;
import name.anderson.odysseus.moneytracker.ofx.*;

public class MfaChallengeMsgReq extends OfxMessageReq
{
	public MfaChallengeMsgReq()
	{
		super(MessageSet.SIGNON, RequestType.Default, "MFACHALLENGE");
	}

	protected void populateRequest(TransferObject obj, float msgsetVer)
	{
		obj.put("DTCLIENT", new Date());
	}

	@Override
	public OfxMessageResp processResponse(TransferObject tran, TransferObject obj)
	{
		return new MfaChallengeMsgResp(tran, obj);
	}
}
