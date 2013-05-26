package de.exitgames.demo.loadbalancing;

import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

public class ApplicationManager extends Application {

	private static Context applicationContext;
	
	static DemoClient	m_client = new DemoClient();
	
	public static final int	STATE_DISCONNECTED = 1;
	public static final int	STATE_LOBBY = 2;
	public static final int	STATE_GAME = 3;
	
	static int 			m_currentState = STATE_DISCONNECTED;
	
	static Console		m_console = new Console();

	private static Stack<Activity> m_activityStack = new Stack<Activity>();

	@Override
	public void onCreate() {
		applicationContext = this;

// this code is needed to make the emulator check some restrictions that android 4 has
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                .detectNetwork()   // or .detectAll() for all detectable problems
//                .penaltyLog()
//                .build());

        super.onCreate();
	}
	
	public static int getState()
	{
		return m_currentState;
	}
	
	public static void setState(int value)
	{
		m_currentState = value;
	}
	
	public static DemoClient getClient()
	{
		return m_client;
	}

	public static Console getConsole()
	{
		return m_console;
	}
	
	public static void makeToast(String str) {
		Toast.makeText(applicationContext, str, Toast.LENGTH_SHORT).show();
	}

	public static Context getContext() {
		return applicationContext;
	}

	public static Context getCurrentActivityContext() {
		return (Context) (m_activityStack.isEmpty() ? null : m_activityStack.lastElement());
	}

	public static Activity getCurrentActivity() {
		return m_activityStack.isEmpty() ? null : m_activityStack.lastElement();
	}

	public static void registerActivity(Activity activity) {
		m_activityStack.push(activity);
	}

	public static void pushActivity(Intent intent) {
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		m_activityStack.lastElement().startActivity(intent);
	}

	public static void popActivity() {
		m_activityStack.pop().finish();
	}

	public static void switchActivity(Intent intent) {
		closeAllActivities();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		applicationContext.startActivity(intent);
//		pushActivity(intent);
	}

	public static void closeAllActivities() {
		synchronized (m_client) {
			while (!m_activityStack.isEmpty()) {
				m_activityStack.pop().finish();
			}
		}
	}
	
	public static synchronized void onClientUpdateCallback()
	{
		if (m_client.getAppId().equals("<insert your appid here>") && m_client.getMasterServerAddress().contains("exitgamescloud"))
        {
			m_client.setAppId("");
			m_console.writeLine("The appId is not set. Customize your appId in DemoClient.java. Find help in readme.txt");
			getCurrentActivity().runOnUiThread(new Runnable() {
				public void run()
				{
					AlertDialog alert = new AlertDialog(ApplicationManager.getCurrentActivityContext()) {};
					alert.setMessage("Error: default appId in use. Customize your appId in DemoClient.java");
					alert.setButton(ApplicationManager.getCurrentActivityContext().getString(R.string.btnOk), 
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface alert, int arg1)
								{
									alert.dismiss();
//									closeAllActivities();
								}
							});
					alert.show();
				}
			});
        }
	}
	
	public static synchronized void onEventReceived()
	{
		m_console.writeLine("Event number " + m_client.getEventCount() + " received!");
		Activity currentActivity = getCurrentActivity(); 
		if (currentActivity instanceof MainMenu)
		{
			((MainMenu)currentActivity).updateStatistics();
		}
	}
	
	public static synchronized void onPropertiesChanged()
	{
		Activity currentActivity = getCurrentActivity(); 
		if (currentActivity instanceof MainMenu)
		{
			((MainMenu)currentActivity).updateStatistics();
			((MainMenu)currentActivity).updateRoomList();
		}
	}
	
	public static synchronized void onGameListUpdated()
	{
		Activity currentActivity = getCurrentActivity(); 
		if (currentActivity instanceof MainMenu)
		{
			((MainMenu)currentActivity).updateRoomList();
		}
	}
	
	public static synchronized void onJoinedToMaster()
	{
	}
	
	public static synchronized void onDisconnected()
	{
		m_currentState = STATE_DISCONNECTED;
		
		Activity currentActivity = getCurrentActivity(); 
		if (currentActivity instanceof MainMenu)
		{
			((MainMenu)currentActivity).updateMenu();
		}
	}
	
	public static synchronized void onJoinedToLobby()
	{
		m_console.writeLine("Joined to lobby");
		m_currentState = STATE_LOBBY;
		
		Activity currentActivity = getCurrentActivity(); 
		if (currentActivity instanceof MainMenu)
		{
			((MainMenu)currentActivity).updateMenu();
		}
	}
	
	public static synchronized void onPlayerJoined()
	{
		if (m_currentState == STATE_LOBBY)
		{
			m_console.writeLine("Entered to the room");
			m_currentState = STATE_GAME;
		
			Activity currentActivity = getCurrentActivity(); 
			if (currentActivity instanceof MainMenu)
			{
				((MainMenu)currentActivity).updateMenu();
			}
		}
		else
			onAnotherPlayerJoined();
	}
	
	public static synchronized void onAnotherPlayerJoined()
	{
		m_console.writeLine("New player entered game");
		Activity currentActivity = getCurrentActivity(); 
		if (currentActivity instanceof MainMenu)
		{
			((MainMenu)currentActivity).updateRoomList();
			((MainMenu)currentActivity).updateStatistics();
		}
	}
	
	public static synchronized void onAnotherPlayerLeave()
	{
		m_console.writeLine("Player leave game. Bye, player!");
		Activity currentActivity = getCurrentActivity(); 
		if (currentActivity instanceof MainMenu)
		{
			((MainMenu)currentActivity).updateRoomList();
			((MainMenu)currentActivity).updateStatistics();
		}
	}

    public static synchronized void updateTrafficStats()
    {
//        m_console.writeLine("updating TrafficStats for view");
        Activity currentActivity = getCurrentActivity();
        if (currentActivity instanceof MainMenu)
        {
            ((MainMenu)currentActivity).updateTrafficStats();
        }
    }
}
