/**
 * 
 */
package name.anderson.odysseus.moneytracker.prof;

import name.anderson.odysseus.moneytracker.R;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

/**
 * @author Erik
 *
 */
public class AddProfile extends ListActivity
{
	private static final int SELECT_PROFILE = 1001;

    @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setListAdapter(ArrayAdapter.createFromResource(this, R.array.add_profile_options, android.R.layout.simple_list_item_1));

		ListView lv = getListView();
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				switch(arg2)
				{
				case 0:
					{
						Intent browseProf = new Intent(AddProfile.this, SelectProfile.class);
						startActivityForResult(browseProf, SELECT_PROFILE);
					}
				case 1:
					{
						Intent enterProf = new Intent(AddProfile.this, EnterProfile.class);
						startActivityForResult(enterProf, SELECT_PROFILE);
					}
				}
			}
		});
	}
}
