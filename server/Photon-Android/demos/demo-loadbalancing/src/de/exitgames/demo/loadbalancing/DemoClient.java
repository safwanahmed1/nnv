package de.exitgames.demo.loadbalancing;

import java.util.HashMap;
import java.util.Random;

import android.util.Log;
import de.exitgames.api.loadbalancing.ClientState;
import de.exitgames.api.loadbalancing.EventCode;
import de.exitgames.api.loadbalancing.LoadBalancingClient;
import de.exitgames.api.loadbalancing.OperationCode;
import de.exitgames.client.photon.EventData;
import de.exitgames.client.photon.OperationResponse;
import de.exitgames.client.photon.StatusCode;
import de.exitgames.client.photon.enums.DebugLevel;

public class DemoClient extends LoadBalancingClient implements Runnable {

	Random	m_random = new Random();
	int		m_eventCount = 0;
    String appId = "ngonnhaovo";

	public DemoClient()
	{
		super();
	}
	
	@Override
	public void run()
    {

		this.connectToMaster(this.appId, "v1.0", "PlayerX");
		this.setPlayerName("Player_" + m_random.nextInt(1000));  // the name is set in connectToMaster, too. this is for demo usage here.
		this.getPlayer().m_customProperties.put("class", "tank" + m_random.nextInt(99));
		
		while (true)
		{
			this.loadBalancingPeer.service();
            ApplicationManager.updateTrafficStats();
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	public void setEventCount(int count)
	{
		m_eventCount = count;
	}

	public int getEventCount()
	{
		return m_eventCount;
	}
	
	/*****************************************************************************************************/
    /// <summary>
    /// Called by our demo form to join a particular room.
    /// Note: OpJoinRoom *could* fail and return false but we only use it in 
    /// places where it safely works, so we ignore the return value here.
    /// </summary>
    /// <param name="name">Name of the room</param>
    public boolean joinSelectedRoom(String name)
    {
        // you don't have to wrap OpJoinRoom like we do here! We just wanted all OP calls in this class...
        if (this.opJoinRoom(name, this.getPlayer().m_customProperties))
        {
        	ProgressBar.createDialog("Please wait...").show();
        	return true;
        }
        
        return false;
    }

    /// <summary>
    /// Called by our demo form to create a new room (and set a few properties for it).
    /// </summary>
    /// <param name="name">Name of the room</param>
    public void createNewRoom(String name)
    {
        // make up some custom properties (key is a string for those)
        HashMap<Object, Object> customGameProperties = new HashMap<Object, Object>();
        customGameProperties.put("map", "blue");
        customGameProperties.put("units", 2);

        ProgressBar.createDialog("Please wait...").show();
        // tells the master to create the room and pass on our locally set properties of "this" player
        this.opCreateRoom(name, true, true, (byte)2, customGameProperties, new String[] { "map" });
    }

    /// <summary>
    /// This method sends event 1 for testing purposes. Your game would send more useful events.
    /// </summary>
    public void sendSomeEvent()
    {
        // to send an event, "raise" it. apply any code (here 1) and set any content (or even null)
        HashMap<Object, Object> eventContent = new HashMap<Object, Object>();
        eventContent.put((byte)10, "my data");                 // using bytes as event keys is most efficient

        this.loadBalancingPeer.opRaiseEvent((byte)1, eventContent, false, (byte)0);       // this is received by OnEvent()
    }

    public void setRoomProperties()
    {
        HashMap<Object, Object> customRoomProperties = new HashMap<Object, Object>();
        if (m_random.nextInt(2) > 0)
        {
            customRoomProperties.put("map", "map" + m_random.nextInt(10));
        }
        else
        {
            customRoomProperties.put("units", m_random.nextInt(10));
        }

        this.loadBalancingPeer.opSetCustomPropertiesOfRoom(customRoomProperties);
        
        ApplicationManager.onPropertiesChanged();
    }

    public void setCustomPlayerProps()
    {
        HashMap<Object, Object> customPlayerProps = new HashMap<Object, Object>();
        if (m_random.nextInt(2) > 0)
        {
            customPlayerProps.put("class", "tank" + m_random.nextInt(10));
        }
        else
        {
            customPlayerProps.put("lvl", m_random.nextInt(10));
        }

        this.loadBalancingPeer.opSetCustomPropertiesOfActor(this.getPlayer().getID(), customPlayerProps);
        
        ApplicationManager.onPropertiesChanged();
    }

	/**
	 * Debug output of low level api (and this client).
	 */
	@Override
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
	
    
    /******************************************************************************************/
    /// <summary>
    /// Uses the connection's statusCodes to advance the internal state and call ops as needed.
    /// In this client, we also update the form, cause new data might be available to display.
    /// </summary>
    @Override
    public void onStatusChanged(StatusCode statusCode)
    {
        super.onStatusChanged(statusCode);
        
        ApplicationManager.onClientUpdateCallback();
        
        switch (statusCode)
        {
            case Connect:
                if (getState() == ClientState.ConnectedToMaster)
                    ApplicationManager.onJoinedToMaster();
                break;
            case Disconnect:
                if (getState() == ClientState.Disconnecting)
                    ApplicationManager.onDisconnected();
                break;
            case TimeoutDisconnect:
                ApplicationManager.getConsole().writeLine("Connection timed out");
                break;
            case DisconnectByServer:
                ApplicationManager.getConsole().writeLine("Disconnected by server");
                break;
            case DisconnectByServerLogic:
                ApplicationManager.getConsole().writeLine("Disconnected by server logic");
                break;
            default:
                break;
        }
    }

    /// <summary>
    /// Uses the photonEvent's provided by the server to advance the internal state and call ops as needed.
    /// In this demo client, we check for a particular event (1) and count these. After that, we update the view / gui.
    /// </summary>
    @Override
    public void onEvent(EventData eventData)
    {
        super.onEvent(eventData);

        switch (eventData.Code)
        {
            case (byte)1:
                this.m_eventCount++;
                ApplicationManager.onEventReceived();
                break;
            case EventCode.GameList:
            case EventCode.GameListUpdate:
                ApplicationManager.onGameListUpdated();
                break;
            case EventCode.PropertiesChanged:
                ApplicationManager.onPropertiesChanged();
                break;
            case EventCode.Join:
                ProgressBar.hide();
                ApplicationManager.onPlayerJoined();
                break;
            case EventCode.Leave:
                ApplicationManager.onAnotherPlayerLeave();
                break;
        }

        // update the form / gui
        ApplicationManager.onClientUpdateCallback();
    }

    /// <summary>
    /// Uses the operationResponse's provided by the server to advance the internal state and call ops as needed.
    /// In this client, we also update the form, cause new data might be available to display.
    /// </summary>
    @Override
    public void onOperationResponse(OperationResponse operationResponse)
    {
        super.onOperationResponse(operationResponse);
        switch (operationResponse.OperationCode)
        {
            case OperationCode.JoinLobby:
                ProgressBar.hide();
                ApplicationManager.onJoinedToLobby();
                this.loadBalancingPeer.setTrafficStatsEnabled(true);
                break;
        }
        ApplicationManager.onClientUpdateCallback();
    }

}
