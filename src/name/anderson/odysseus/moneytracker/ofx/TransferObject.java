package name.anderson.odysseus.moneytracker.ofx;

import java.sql.Time;
import java.util.*;
import android.text.format.DateFormat;

/**
 * @author Erik Anderson
 *
 */
public class TransferObject
{
	public String name;
	public LinkedList<ObjValue> members;
	public Map<String, ObjValue> memberNames;
	
	static public class ObjValue
	{
		public String name;
		public String attrValue;
		public TransferObject child;
		public boolean secure;
		
		public ObjValue(String n, String v)
		{
			name = n;
			attrValue = v;
			secure = false;
		}

		public ObjValue(String n, TransferObject v)
		{
			name = n;
			child = v;
			secure = false;
		}
	}
	
	public TransferObject(String n)
	{
		this.name = n;
		this.members = new LinkedList<ObjValue>();
		this.memberNames = new TreeMap<String, ObjValue>();
	}
	
	public void put(String key, String value)
	{
		ObjValue val = new ObjValue(key, value);
		this.members.add(val);
		if(!this.memberNames.containsKey(key))
		{
			this.memberNames.put(key, val);
		}
	}
	
	public void putSecure(String key, String value)
	{
		ObjValue val = new ObjValue(key, value);
		val.secure = true;
		this.members.add(val);
		if(!this.memberNames.containsKey(key))
		{
			this.memberNames.put(key, val);
		}
	}
	
	public void put(String key, Date value)
	{
		put(key, (String) DateFormat.format("yyyyMMddkkmmss", value));
	}
	
	public void put(String key, boolean value)
	{
		put(key, value ? "Y" : "N");
	}
	
	public String getAttr(String key)
	{
		if(!this.memberNames.containsKey(key))
		{
			return null;
		} else {
			return this.memberNames.get(key).attrValue;
		}
	}
	
	public TransferObject getObj(String key)
	{
		if(!this.memberNames.containsKey(key))
		{
			return null;
		} else {
			return this.memberNames.get(key).child;
		}
	}
	
	public void put(TransferObject obj)
	{
		ObjValue val = new ObjValue(obj.name, obj);
		this.members.add(val);
		if(!this.memberNames.containsKey(obj.name))
		{
			this.memberNames.put(obj.name, val);
		}
	}
	
	public void putHead(TransferObject obj)
	{
		ObjValue val = new ObjValue(obj.name, obj);
		this.members.addFirst(val);
		if(!this.memberNames.containsKey(obj.name))
		{
			this.memberNames.put(obj.name, val);
		}
	}

	public String Format(float ver)
	{
		StringBuilder out = new StringBuilder();
		out.append("<" + this.name + ">\n");

		for(ObjValue attr : this.members)
		{
			if(attr.child != null)
			{
				out.append(attr.child.Format(ver));
			}
			else if(ver >= 2.0)
			{
				out.append("<" + attr.name + ">" + FormatString(ver, attr.attrValue) + "</" + attr.name + ">");
			} else {
				out.append("<" + attr.name + ">" + FormatString(ver, attr.attrValue) + "\n");
			}
		}

		out.append("</" + this.name + ">\n");
		return out.toString();
	}

	private static String FormatString(float ver, String value)
	{
		value.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;");
		if(ver >= 2.0)
		{
			value.replace("\"", "&quot;")
				.replace("'", "&apos;");
		}
		else if(ver >= 1.5)
		{
			if(value.startsWith(" "))
			{
				value = "&nbsp;" + value.substring(1);
			}
			if(value.endsWith(" "))
			{
				value = value.substring(0, value.length()-1) + "&nbsp;";
			}
		}
		return value;
	}
	
	public static Date parseDate(String in)
	{
		int limit = in.indexOf('[');
		if(limit < 0) limit = in.length();
		
		if(limit < 8) return null;
		int year = Integer.parseInt(in.substring(0, 4));
		int month = Integer.parseInt(in.substring(4, 6));
		int date = Integer.parseInt(in.substring(6, 8));

		int hrs = 0;
		int min = 0;
		int sec = 0;
		if(limit > 10)
		{
			hrs = Integer.parseInt(in.substring(8, 10));
			if(limit > 12)
			{
				min = Integer.parseInt(in.substring(10, 12));
				if(limit >= 14)
				{
					sec = Integer.parseInt(in.substring(12, 14));
				}
			}
		}

		int tz = 0;
		if(limit < in.length())
		{
			int endTZ = in.indexOf(':');
			if(endTZ < 0) endTZ = in.indexOf(']');
			if(endTZ < 0) return null;
			tz = Integer.parseInt(in.substring(limit, endTZ-1));
		}
		
		TimeZone tzObj;
		if(tz != 0)
		{
			int offset = tz * 60 * 60 * 1000;
			String[] tzIds = TimeZone.getAvailableIDs(offset);
			tzObj = new SimpleTimeZone(offset, tzIds[0]);
		} else {
			tzObj = TimeZone.getTimeZone("GMT");
		}
		Calendar baseDate = new GregorianCalendar(tzObj);
		baseDate.set(year, month, date, hrs, min, sec);
		return baseDate.getTime();
	}

	public static Time parseTime(String in)
	{
		int limit = in.indexOf('[');
		if(limit < 0) limit = in.length();
		
		if(limit < 6) return null;
		int hrs = Integer.parseInt(in.substring(0, 2));
		int min = Integer.parseInt(in.substring(2, 4));
		float sec = Float.parseFloat(in.substring(4, limit));

		int tz = 0;
		if(limit < in.length())
		{
			int endTZ = in.indexOf(':');
			if(endTZ < 0) endTZ = in.indexOf(']');
			if(endTZ < 0) return null;
			tz = Integer.parseInt(in.substring(limit, endTZ-1));
		}
		
		return new Time((long) (((hrs-tz)*60+min)*60+sec)*1000);
	}
}
