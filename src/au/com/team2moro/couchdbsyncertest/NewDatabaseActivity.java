package au.com.team2moro.couchdbsyncertest;

import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import au.com.team2moro.couchdbsyncer.DatabaseStore;

public class NewDatabaseActivity extends Activity implements OnClickListener {
	public static final String TAG = "NewDatabaseActivity";
	
	private Button buttonSave;
	private EditText editName, editURL, editUser, editPass;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.new_database_activity);

		buttonSave = (Button) findViewById(R.id.button1);
		editName = (EditText) findViewById(R.id.editText1);
		editURL = (EditText) findViewById(R.id.editText2);
		editUser = (EditText) findViewById(R.id.editText3);
		editPass = (EditText) findViewById(R.id.editText4);
		
		buttonSave.setOnClickListener(this);
	}

	public void onClick(View v) {
		// add database
		try {
			String name = editName.getText().toString();
			String url = editURL.getText().toString();
			String username = editUser.getText().toString();
			String password = editPass.getText().toString();
			
			DatabaseStore dbstore = ((TestApplication) getApplication()).getDatabaseStore();
			dbstore.addDatabase(name, new URL(url), username, password);
			
			// go back to previous activity
			finish();  
		}
		catch(Exception e) {
			Log.d(TAG, e.toString());
		}
	}

}
