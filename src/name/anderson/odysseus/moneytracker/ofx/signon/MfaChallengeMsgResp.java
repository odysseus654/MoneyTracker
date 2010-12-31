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
/*
	public static final String mfaPhrases[] =
	{ 
		"City of birth",
		"Date of birth MM/DD/YYYY",
		"Debit card number",
		"Father's middle name",
		"Favorite color",
		"First pet's name",
		"Five digit ZIP code",
		"Grandmother's maiden name on your father's side",
		"Grandmother's maiden name on your mother's side",
		"Last four digits of your cell phone number",
		"Last four digits of your daytime phone number",
		"Last four digits of your home phone number",
		"Last four digits of your social security number",
		"Last four digits of your Tax ID",
		"Month of birth of youngest sibling (do not abbreviate)",
		"Mother's maiden name",
		"Mother's middle name",
		"Name of the company where you had your first job",
		"Name of the manufacturer of your first car",
		"Name of your high school football team, do not include high school name, e.g. \"Beavers\" rather than \"Central High Beavers\"",
		"Recent deposit or recent withdrawal amount",
		"Year of birth, use YYYY"
	};
*/
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
		Iterator<TransferObject.ObjValue> iter = in.members.iterator();
		while(iter.hasNext())
		{
			TransferObject val = iter.next().child;
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
