package de.exitgames.demo.realtime;

import java.util.Hashtable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import de.exitgames.client.photon.*;
import de.exitgames.client.photon.enums.*;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class DemoService extends Service implements IPhotonPeerListener
{
	SettingsData settings;

	private long lastRandomMoveTime = 0xFFFFFFFF;
	private long lastSendTime = 0xFFFFFFFF;
	private long lastDispatchTime = 0xFFFFFFFF;
	
	private long startTime;
	private int moveSentCounter;
	
    // custom event codes
    public final static byte EV_MOVE = 1;
    public final static byte EV_PLAYER_INFO = 0;

    // Keys for PlayerStatus-Events
    public final static Byte STATUS_PLAYER_POS_X = 0;
    public final static Byte STATUS_PLAYER_POS_Y = 1;
    public final static Byte CONSECUTIVE_NUMBER = 50;	// optional part of event MOVE. increasing event id
    public final static Byte STATUS_PLAYER_NAME = 2;
    public final static Byte STATUS_PLAYER_COLOR= 3;

	private LitePeer peer;
	private Timer timer;
	private Random rand;
	private List<DemoServiceCallback> m_callbacks = new LinkedList<DemoServiceCallback>();

	private Player localPlayer;
	
	Hashtable<Integer, Player> players;

	// Handler required to process async events that are interfering with UI
	// (eventAction changes players array while UI reads it when redrawing. 
	// So eventAction should be executed in main loop to avoid ConcurrentModificationException)
	final Handler syncHandler = new Handler();

	public DemoService()
	{
		players = new Hashtable<Integer, Player>();
		rand = new Random(System.currentTimeMillis());
	}
	
	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder 
    {
    	DemoService getService() 
    	{
            return DemoService.this;
        }
    }
	
	
	@Override
	public void onCreate()
	{
		super.onCreate();
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		disconnect();
	}
	
	@Override
	public IBinder onBind(Intent arg0) 
	{
		return mBinder;
	}
	
	private final IBinder mBinder = new LocalBinder();

	private Integer	eventCounter = 0;

	public static int NonConsecutiveIssueCount;

	public void connect(SettingsData set)
	{
		settings = set;
		
		localPlayer = new Player(-1);
		localPlayer.name = settings.name;
		
		try
		{
			localPlayer.color = Color.parseColor(settings.color);
		} catch (IllegalArgumentException e)
		{
			localPlayer.color = 0xFFFFFFFF;
		}
		
		peer = new LitePeer(this, settings.useTcp);
		peer.connect(settings.server, "Lite");

		startTime = System.currentTimeMillis();
		moveSentCounter = 0;
		
		timer = new Timer("main loop");
		TimerTask timerTask = new TimerTask()
		{
			public void run()
			{
				if (null == peer)
				{
					cancel();
					timer.cancel();
					
					return;
				}
				
				if (peer.getPeerState() == PeerStateValue.Connected)
				{
					if ( (System.currentTimeMillis() - lastRandomMoveTime) > settings.randomMoveInterval)
					{
						lastRandomMoveTime = System.currentTimeMillis();
						switch (rand.nextInt(4))
						{
						case 0:
							moveLocalPlayer(0, 1); break;
						case 1:
							moveLocalPlayer(0, -1); break;
						case 2:
							moveLocalPlayer(1, 0); break;
						case 3:
							moveLocalPlayer(-1, 0); break;
						}
					}
				}
					
				// test if it's time to dispatch all incoming commands to the application. Dispatching
				// will empty the queue of incoming messages and will fire the related callbacks.
				if ( (System.currentTimeMillis() - lastDispatchTime) > settings.intervalDispatch)
				{
					lastDispatchTime = System.currentTimeMillis();
					
					// dispatch all incoming commands
					while (peer != null && peer.dispatchIncomingCommands()) {}
				}
				
				// to spare some overhead, we will send outgoing packets in certain intervals, as defined
				// in the settings menu. 				
				if ( (System.currentTimeMillis() - lastSendTime) > settings.intervalSend)
				{
					lastSendTime = System.currentTimeMillis();
					if (peer != null)
						peer.sendOutgoingCommands();					
				}
			}
		};
		
		timer.schedule(timerTask, 0, 5);
	}
		
	public void disconnect()
	{
		if (null != timer)
		{
			timer.cancel();
			timer = null;
		}
		
		if (null != peer)
		{
            // we don't need to leave a room or anything. disconnect() lets the server know we're gone. this will remove this peer from games, too
			peer.disconnect();
		}
		
		peer = null;
		players.clear();
		localPlayer = null;
	}
	
	public void moveLocalPlayer(int dx, int dy)
	{
		if (null == localPlayer || localPlayer.id <= 0 || peer == null)
			return;
		
		localPlayer.x = (byte)(localPlayer.x + dx);
		localPlayer.y = (byte)(localPlayer.y + dy);
		
		if (localPlayer.x < 0) localPlayer.x = 0;
		if (localPlayer.y < 0) localPlayer.y = 0;
		if (localPlayer.x >= GameField.BOARD_SIZE_X) localPlayer.x = GameField.BOARD_SIZE_X - 1;
		if (localPlayer.y >= GameField.BOARD_SIZE_Y) localPlayer.y = GameField.BOARD_SIZE_Y - 1;
		
		sendLocalPlayerPosition();
	}
	
	public Hashtable<Integer, Player> getPlayers()
	{
		return players;
	}
	
	public Stats getStats()
	{
		Stats ret = new Stats();
		ret.roundTripTime = (null != peer) ? peer.getRoundTripTime() : 0;
		ret.timeSec = (int) ((System.currentTimeMillis() - startTime) / 1000);
		ret.movesSent = moveSentCounter;
		ret.playerNum = (null != players) ? players.size() : 0;
		ret.eventCount = eventCounter;

		return ret;
	}
	
	public void addCallbackListener(DemoServiceCallback cb)
	{
		if (cb != null) 
		{
			m_callbacks.add(cb);
		}
	}

	public void removeCallbackListener(DemoServiceCallback cb) 
	{
		if (cb != null) 
		{
			m_callbacks.remove(cb);
		}				
	}
	
	 
	
	// Callback broadcasters
	 
	private void errorOccured(String message)
	{
		for (DemoServiceCallback h : m_callbacks)
			h.errorOccurred(message);
	}
	
	private void loginDone(boolean ok)
	{
		for (DemoServiceCallback h : m_callbacks)
			h.loginDone(ok);
	}

	
	Object sequenceNumberingLockObj = new Object();
	
	private void sendLocalPlayerPosition() 
	{
		synchronized (sequenceNumberingLockObj) 
		{
			// sync makes sure we're not sending/changing lastSentConsecutiveNumber by more than one thread 
			HashMap<Object, Object> ev = new HashMap<Object, Object>();
			ev.put(STATUS_PLAYER_POS_X, localPlayer.x);
			ev.put(STATUS_PLAYER_POS_Y, localPlayer.y);
			//ev.put(CONSECUTIVE_NUMBER, localPlayer.lastSentConsecutiveNumber++);
			
			peer.opRaiseEvent(EV_MOVE, ev, false);
			++moveSentCounter;
		}
	}
	
	private void sendPlayerInfo()
    {
		if (this.localPlayer == null || this.localPlayer.id <= 0 || this.peer == null)
        {
			return;
        }

        this.localPlayer.sendPlayerInfo(this.peer);
    }

	public void debugReturn(DebugLevel level, String message)
	{
		switch(level)
		{
			case OFF:
				Log.println(Log.ASSERT, "CLIENT", message);
				break;
			case ERROR:
				Log.e("CLIENT", message);
				break;
			case WARNING:
				Log.w("CLIENT", message) ;
				break;
			case INFO:
				Log.i("CLIENT", message);
				break;
			case ALL:
				Log.d("CLIENT", message);
				break;
			default:
				Log.e("CLIENT", message);
				break;
		}
	}
	
	public void onOperationResponse(OperationResponse operationResponse)
	{
        byte opCode = operationResponse.OperationCode;
        short returnCode = operationResponse.ReturnCode;

        if (opCode != (byte)LiteOpCode.RaiseEvent || returnCode != (short)0)
        {
            debugReturn(DebugLevel.INFO, "OnOperationResponse() " + opCode + "/" + returnCode);
        }

        // handle operation returns (aside from "join", this demo does not watch for returns)
        switch (opCode)
        {
            case (byte)LiteOpCode.Join:
            {
                debugReturn(DebugLevel.INFO, "Join request: " + operationResponse.Parameters.toString());

                // get the local player's numer from the returnvalues, get the player from the list and colorize it:
                if (operationResponse.Parameters.containsKey((byte)LiteOpKey.ActorNr))
                {
                    this.localPlayer.id = (Integer)operationResponse.Parameters.get((byte)LiteOpKey.ActorNr);
                }
                // LocalPlayer.generateColor();
                this.players.put(localPlayer.id, this.localPlayer);
                //sendPlayerInfoNull(false);
                debugReturn(DebugLevel.INFO, "Local Player ID: " + localPlayer.id);
                break;
            }
        }
	}
	
	public void onEvent (EventData ev)
	{
        //debug output is OK for all but the most rapidly sent events (in our case: EV_MOVE)
        if (ev.Code != Player.EV_MOVE)
        {
            this.debugReturn(DebugLevel.INFO, "OnEvent() " + ev.Code + " = " + ev.Parameters.toString());
        }

        //most events will contain the actorNumber of the player who sent the event, so check if the event origin is known
        int actorNr = 0;
        if (ev.Parameters.containsKey(LiteEventKey.ActorNr))
        {
            actorNr = (Integer)ev.Parameters.get(LiteEventKey.ActorNr);
        }

        // get the player that raised this event
        Player p = players.get(actorNr);

        switch (ev.Code)
        {
            case (byte)LiteEventCode.Join:
                // Event is defined by Lite. A peer entered the room. It could be this peer!
                // This event provides the current list of actors and a actorNumber of the player who is new.

                // get the list of current players and check it against local list - create any that's not yet there
                Integer[] actorsInGame = (Integer[])ev.Parameters.get(LiteEventKey.ActorList);
                for (int i : actorsInGame)
                {
                    if (i != localPlayer.id)
                        players.put(i, new Player(i));
                }

                peer.establishEncryption();
                this.sendPlayerInfo(); // the new peers does not have our info, so send it again
                break;

            case (byte)LiteEventCode.Leave:
                // Event is defined by Lite. Someone left the room.
                this.players.remove(actorNr);
                break;

            case (byte)Player.EV_PLAYER_INFO:
                // this is a custom event, which is defined by this application.
                // if player is known (and it should be known!), update info
                if (p != null)
                {
                    if (p == this.localPlayer)
                    {
                        this.debugReturn(DebugLevel.INFO, "info event's target is localplayer. ignoring it");
                    }
                    else
                    {
                        p.setInfo((HashMap<Byte, Object>)ev.Parameters.get((byte)LiteEventKey.Data));
                        players.put(p.id, p);
                        this.sendPlayerInfo();
                    }
                }
                else
                {
                    this.debugReturn(DebugLevel.WARNING, "did not find player to set info: " + actorNr);
                }

                //this.PrintPlayers();
                break;

            case (byte)Player.EV_MOVE:
                // this is a custom event, which is defined by this application.
                // if player is known (and it should be known) update position
                if (p != null)
                {
                    p.setPosition((HashMap<Byte, Object>)ev.Parameters.get((byte)LiteEventKey.Data));
                }
                else
                {
                    this.debugReturn(DebugLevel.WARNING, "did not find player to move: " + actorNr);
                }
                break;
        }
	}

	@Override
	public void onStatusChanged(StatusCode statusCode) 
	{
		String message;
		
		switch (statusCode)
		{
            case Connect:
                loginDone(true);
                peer.opJoin(settings.gameName);
                break;
            case Disconnect:
                localPlayer.id = 0;
                players.clear();

                break;
            case ExceptionOnConnect:
                message = "Exception while connecting. Check server address, network and server. Code: " + statusCode;
                debugReturn(DebugLevel.ERROR, message);
                errorOccured(message);
                break;
            case Exception:
                message = "Exception peer.state: " + peer.getPeerState();
                debugReturn(DebugLevel.ERROR, message);
                errorOccured(message);
                break;
            case SendError:
                message = "SendError! peer.state: " + peer.getPeerState();
                debugReturn(DebugLevel.ERROR, message);
                errorOccured(message);
                break;
            case TimeoutDisconnect:
                message = "TimeoutDisconnect! peer.state: " + peer.getPeerState();
                debugReturn(DebugLevel.ERROR, message);
                errorOccured(message);
                break;
            default:
                message = "PeerStatusCallback: " + statusCode;
                debugReturn(DebugLevel.ERROR, message);
                break;
		}
		
		
	}
}
