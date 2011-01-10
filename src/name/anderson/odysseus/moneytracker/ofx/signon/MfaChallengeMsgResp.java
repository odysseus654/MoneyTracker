/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.signon;

import java.util.*;
import name.anderson.odysseus.moneytracker.ofx.*;

/**
 * @author Erik
 *
 */
public class MfaChallengeMsgResp extends OfxMessageResp
{
	public List<Challenge> challenges;
	
	public static class Challenge
	{
		public String phraseID;
		public String label;
	}
/*
	public MfaChallengeMsgResp()
	{
	}
*/
	public MfaChallengeMsgResp(TransferObject tran, TransferObject in)
	{
		if(tran != null) this.trn = new TransactionResp(tran);
		this.challenges = new LinkedList<Challenge>();
		for(TransferObject.ObjValue obj : in.members)
		{
			TransferObject val = obj.child;
			if(val != null)
			{
				Challenge chal = new Challenge();
				chal.phraseID = val.getAttr("MFAPHRASEID");
				chal.label = val.getAttr("MFAPHRASELABEL");
				this.challenges.add(chal);
			}
		}
	}
}
