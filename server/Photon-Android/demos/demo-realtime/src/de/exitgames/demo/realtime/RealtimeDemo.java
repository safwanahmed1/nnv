package de.exitgames.demo.realtime;

import de.exitgames.demo.realtime.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class RealtimeDemo extends Activity implements DemoServiceCallback
{
	public final static int CHILD_ACTIVITY_SETTINGS = 1; 
	public final static int CHILD_ACTIVITY_GAMEFIELD = 2; 
	
	private DemoService serviceInstance;
	
	private Button btnStartGame;
	private Button btnSettings;
	private Button btnHelp;
	private SettingsData settings;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setMainScreen();
    }

    private void setMainScreen() {
        setContentView(R.layout.main);

        settings = new SettingsData();
        bindService(new Intent(RealtimeDemo.this, DemoService.class), serviceConnection, Context.BIND_AUTO_CREATE);

        btnStartGame = (Button) findViewById(R.id.btnStartGame);
        btnStartGame.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View arg0)
            {
                btnStartGame.setEnabled(false);
                btnSettings.setEnabled(false);
                btnHelp.setEnabled(false);

                serviceInstance.connect(settings);
            }
        });

        btnSettings = (Button) findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View arg0)
            {
                Intent i = new Intent(RealtimeDemo.this, Settings.class);

                i.putExtra(SettingsData.RANDOM_MOVE_INTERVAL, settings.randomMoveInterval);
                i.putExtra(SettingsData.INTERVAL_SEND, settings.intervalSend);
                i.putExtra(SettingsData.INTERVAL_DISPATCH, settings.intervalDispatch);
                i.putExtra(SettingsData.PLAYER_NAME, settings.name);
                i.putExtra(SettingsData.PLAYER_COLOR, settings.color);
                i.putExtra(SettingsData.SERVER_URL, settings.server);
                i.putExtra(SettingsData.GAME_NAME, settings.gameName);
                i.putExtra(SettingsData.USE_TCP, settings.useTcp);

                startActivityForResult(i, CHILD_ACTIVITY_SETTINGS);
            }
        });

        btnHelp = (Button) findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View arg0)
            {
                Intent i = new Intent(RealtimeDemo.this, Help.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	unbindService(serviceConnection);
    }
    
    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder service) 
        {
        	serviceInstance = ((DemoService.LocalBinder)service).getService();
        	serviceInstance.addCallbackListener(RealtimeDemo.this);
        }
     
        public void onServiceDisconnected(ComponentName className) 
        {
        	serviceInstance = null;
        }
    };


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		Log.i("onActivityResult",requestCode+", "+resultCode+", ");
		switch (requestCode) 
		{
			case CHILD_ACTIVITY_SETTINGS:
				Log.i("RealtimeDemo", "CHILD_ACTIVITY_SETTINGS returned");
				if (RESULT_OK == resultCode)
				{
					settings.randomMoveInterval = data.getIntExtra(SettingsData.RANDOM_MOVE_INTERVAL, SettingsData.DEFAULTVALUE_INTERVAL_RANDOMMOVE);
					settings.intervalSend = data.getIntExtra(SettingsData.INTERVAL_SEND, SettingsData.DEFAULTVALUE_INTERVAL_SEND);
					settings.intervalDispatch = data.getIntExtra(SettingsData.INTERVAL_DISPATCH, SettingsData.DEFAULTVALUE_INTERVAL_DISPATCH);
					settings.name = data.getStringExtra(SettingsData.PLAYER_NAME);
					settings.server = data.getStringExtra(SettingsData.SERVER_URL);
					settings.gameName = data.getStringExtra(SettingsData.GAME_NAME);
					settings.color = data.getStringExtra(SettingsData.PLAYER_COLOR);
					settings.useTcp = data.getBooleanExtra(SettingsData.USE_TCP, false);
				}
				break;
			case CHILD_ACTIVITY_GAMEFIELD:
				Log.i("RealtimeDemo", "CHILD_ACTIVITY_GAMEFIELD returned");
				
				// Restore callbacks connection to DemoService
				serviceInstance.addCallbackListener(RealtimeDemo.this);
				serviceInstance.disconnect();
				
		    	btnStartGame.setEnabled(true);
		    	btnSettings.setEnabled(true);
		    	btnHelp.setEnabled(true);
				
				break;
		}
	}

	
	// DemoServiceCallback implementation
    
	@Override
	public void loginDone(boolean ok)
	{
		if (ok)
		{
			serviceInstance.removeCallbackListener(RealtimeDemo.this);
	  	   	Intent i = new Intent(RealtimeDemo.this, GameField.class);
	  	   	startActivityForResult(i, CHILD_ACTIVITY_GAMEFIELD);
		} else
		{
	    	btnStartGame.setEnabled(true);
	    	btnSettings.setEnabled(true);
	    	btnHelp.setEnabled(true);
			
			errorOccurred("Login failed");
		}
	}
    
	@Override
	public void errorOccurred(final String message) 
	{
		_handler.post(new Runnable() 
		{
			@Override
			public void run()
			{
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(RealtimeDemo.this);
				alertBuilder.setMessage(message);
				DialogInterface.OnClickListener onOkListener = new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface alert, int arg1)
					{
						alert.dismiss();
                        setMainScreen();
					}
				};

				alertBuilder.setPositiveButton("OK", onOkListener);
				AlertDialog alert = alertBuilder.create();
                alert.show();
			}
		});

	}
	
    final Handler _handler = new Handler();

    
}