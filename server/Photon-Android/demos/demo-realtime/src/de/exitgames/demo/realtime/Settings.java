package de.exitgames.demo.realtime;
import de.exitgames.demo.realtime.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class Settings extends Activity
{
	private EditText edtRandomInterval;
	private EditText edtIntervalSend;
	private EditText edtIntervalDispatch;
	private EditText edtName;
	private EditText edtServer;
	private CheckBox useTCP;
	private EditText edtGameName;
	private EditText edtColor;
	
	private Button btnOk;
	
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.settings);
      
		setResult(RESULT_CANCELED);
		
		edtRandomInterval = (EditText)findViewById(R.id.edtRandomInterval);
		edtIntervalSend = (EditText)findViewById(R.id.edtIntervalSend);
		edtIntervalDispatch = (EditText)findViewById(R.id.edtIntervalDispatch);
		edtName = (EditText)findViewById(R.id.edtName);
		edtServer = (EditText)findViewById(R.id.edtServer);
		useTCP = (CheckBox) findViewById(R.id.useTcp);
		edtGameName = (EditText)findViewById(R.id.edtGameName);
		edtColor = (EditText)findViewById(R.id.edtColor);
		btnOk = (Button)findViewById(R.id.btnOk);
		
		Intent i = getIntent();
		
		edtRandomInterval.setText( ((Integer)i.getIntExtra(SettingsData.RANDOM_MOVE_INTERVAL, SettingsData.DEFAULTVALUE_INTERVAL_RANDOMMOVE)).toString() );
		edtIntervalSend.setText( ((Integer)i.getIntExtra(SettingsData.INTERVAL_SEND, SettingsData.DEFAULTVALUE_INTERVAL_SEND)).toString() );
		edtIntervalDispatch.setText( ((Integer)i.getIntExtra(SettingsData.INTERVAL_DISPATCH, SettingsData.DEFAULTVALUE_INTERVAL_DISPATCH)).toString() );
		edtName.setText(i.getStringExtra(SettingsData.PLAYER_NAME));
		edtServer.setText(i.getStringExtra(SettingsData.SERVER_URL));
		edtGameName.setText(i.getStringExtra(SettingsData.GAME_NAME));
		edtColor.setText(i.getStringExtra(SettingsData.PLAYER_COLOR));
		useTCP.setChecked(i.getBooleanExtra(SettingsData.USE_TCP, false));
		
		btnOk.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View arg0) 
			{
				Intent result = new Intent();
				
				int randomInterval = SettingsData.DEFAULTVALUE_INTERVAL_RANDOMMOVE;
				int intervalSend = SettingsData.DEFAULTVALUE_INTERVAL_SEND;
				int intervalDispatch = SettingsData.DEFAULTVALUE_INTERVAL_DISPATCH;
				
				try
				{
					randomInterval = Integer.parseInt(edtRandomInterval.getText().toString());
					intervalSend = Integer.parseInt(edtIntervalSend.getText().toString());
					intervalDispatch = Integer.parseInt(edtIntervalDispatch.getText().toString());
				} catch (NumberFormatException e)
				{}
				 
				result.putExtra(SettingsData.RANDOM_MOVE_INTERVAL, randomInterval);
				result.putExtra(SettingsData.INTERVAL_SEND, intervalSend);
				result.putExtra(SettingsData.INTERVAL_DISPATCH, intervalDispatch);
				result.putExtra(SettingsData.PLAYER_NAME, edtName.getText().toString());
				result.putExtra(SettingsData.SERVER_URL, edtServer.getText().toString());
				result.putExtra(SettingsData.GAME_NAME, edtGameName.getText().toString());
				result.putExtra(SettingsData.PLAYER_COLOR, edtColor.getText().toString());
				result.putExtra(SettingsData.USE_TCP, useTCP.isChecked());
				 
				setResult(RESULT_OK, result);
				finish();
			}
		});        
	}
}