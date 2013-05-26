package de.exitgames.demo.realtime;

public class SettingsData 
{
	public static final int DEFAULTVALUE_INTERVAL_RANDOMMOVE = 500;
	public static final int DEFAULTVALUE_INTERVAL_SEND = 50;
	public static final int DEFAULTVALUE_INTERVAL_DISPATCH = 10;
	
	public static final String RANDOM_MOVE_INTERVAL = "RANDOM_MOVE_INTERVAL"; 
	public int randomMoveInterval = DEFAULTVALUE_INTERVAL_RANDOMMOVE;
	
	public static final String INTERVAL_SEND = "INTERVAL_SEND"; 
	public int intervalSend = DEFAULTVALUE_INTERVAL_SEND;
	
	public static final String INTERVAL_DISPATCH = "INTERVAL_DISPATCH"; 
	public int intervalDispatch = DEFAULTVALUE_INTERVAL_DISPATCH;
	
	public static final String PLAYER_NAME = "PLAYER_NAME"; 
	public String name = "Android";
	
	public static final String PLAYER_COLOR = "PLAYER_COLOR"; 
	public String color = "#2222FF";
	
	public static final String SERVER_URL = "SERVER_URL"; 
//	public String server = "127.0.0.1:5055";
	public String server = "192.168.42.43:5055";

	public static final String USE_TCP = "USE_TCP"; 
    public boolean useTcp = false;
	
	public static final String GAME_NAME = "GAME_NAME"; 
	public String gameName = "realtimeDemoGame000"; 
}
