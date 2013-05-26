package de.exitgames.api.loadbalancing;

import java.util.HashMap;

import de.exitgames.client.photon.EventData;
import de.exitgames.client.photon.IPhotonPeerListener;
import de.exitgames.client.photon.OperationResponse;
import de.exitgames.client.photon.StatusCode;
import de.exitgames.client.photon.TypedHashMap;
import de.exitgames.client.photon.enums.ConnectionProtocol;
import de.exitgames.client.photon.enums.DebugLevel;

/**
 * This class extends the pure LoadBalancingPeer to implement the Photon LoadBalancing workflow.
 * It keeps a state and will automatically execute transitions between the Master and Game Servers.
 *
 * This class (and the Player class) should be extended to implement your own game logic.
 * The State of this class is essential to know when a client is in a lobby (or just on the master)
 * and when in a game where the actual gameplay should take place.
 * Extension notes:
 * An extension of this class should the methods of the IPhotonPeerListener, as they 
 * are called when the state changes. Call super.method first, then pick the operation or state you
 * want to react to and put it in a switch-case.
 * We try to provide demo to each platform where this api can be used, so lookout for those.
 */
public abstract class LoadBalancingClient implements IPhotonPeerListener {

    public LoadBalancingPeer loadBalancingPeer;

    /**
     * The version of your client. A new version also creates a new "virtual app" to separate players from older client versions.
     */
	private String m_appVersion;

    /**
     * Client version getter
     * @return client version
     */
	public String getAppVersion()
	{
		return m_appVersion;
	}

    /**
     * Client version setter
     * @param version client version
     */
	public void setAppVersion(String version)
	{
		m_appVersion = version;
	}

    /**
     * The appName as assigned from the Photon Cloud or just the "regular" Photon Server Application Name ("LoadBalancing").
     */
    private String m_appId;

    /**
     * Application ID getter
     * @return [@link de.exitgames.api.loadbalancing.LoadBalancingClient#getAppId()] parameter
     */
    public String getAppId()
    {
    	return m_appId;
    }


    /**
     * Application ID setter
     * @param id application ID to set
     */
    public void setAppId(String id)
    {
    	m_appId = id;
    }

    /**
     * The master server's address. Defaults to "app.exitgamescloud.com:5055"
     */
    protected String m_masterServerAddress;
    
    public String getMasterServerAddress()
    {
    	return m_masterServerAddress;
    }
    
    protected void setMasterServerAddress(String address)
    {
    	m_masterServerAddress = address;
    }

    /**
     * The game server's address for a particular room. In use temporarily, as assigned by master.
     */
    private String m_gameServerAddress;
    
    public String getGameServerAddress()
    {
    	return m_gameServerAddress;
    }
    
    protected void setGameServerAddress(String address)
    {
    	m_gameServerAddress = address;
    }

    /**
     * Backing field for property.
     */
    private ClientState m_state = ClientState.Uninitialized;
    
    public ClientState getState()
    {
    	return m_state;
    }
    
    public void setState(ClientState state)
    {
    	m_state = state;
    }

    /**
     * Available server (types) for internally used field: server.
     */
    private enum ServerConnection
    {
        MasterServer,
        GameServer
    }

    /**
     * The server this client is currently connected or connecting to.
     */
    private ServerConnection m_server;

    /**
     * Backing field for property.
     */
    private boolean m_autoJoinLobby = true;
    
    public boolean getAutoJoinLobby()
    {
    	return m_autoJoinLobby;
    }
    
    public void setAutoJoinLobby(boolean value)
    {
    	m_autoJoinLobby = value;
    }

    /**
     * This "list" is populated while being in the lobby of the Master. It contains RoomInfo per roomName (keys).
     */
    private TypedHashMap<String, RoomInfo> m_roomInfoList = new TypedHashMap<String, RoomInfo>(String.class, RoomInfo.class);

    public TypedHashMap<String, RoomInfo> getRooms()
    {
    	return m_roomInfoList;
    }

    /**
     * The current room this client is connected to (null if none available).
     */
    private Room m_currentRoom;
    
    public Room getCurrentRoom()
    {
    	return m_currentRoom;
    }

    /**
     * The local player is never null but not valid unless the client is in a room, too. The ID will be -1 outside of rooms.
     */
    private Player m_localPlayer;
    
    public Player getPlayer()
    {
    	return m_localPlayer;
    }
    
    public void setPlayer(Player player)
    {
    	m_localPlayer = player;
    }
    
    public String getPlayerName()
    {
    	return m_localPlayer.getName();
    }
    
    public void setPlayerName(String name)
    {
    	if (m_localPlayer != null)
    	{
    		m_localPlayer.setName(name);
    	}
    }

    /**
     * Statistic value available on master server: Players on master (looking for games).
     */
    public int m_playersOnMasterCount;
    
    public int getPlayersOnMasterCount()
    {
    	return this.m_playersOnMasterCount;
    }
    
    public void setPlayersOnMasterCount(int value)
    {
    	this.m_playersOnMasterCount = value;
    }

    /**
     * Statistic value available on master server: Players in rooms (playing).
     */
    public int m_playersInRoomsCount;
    
    public int getPlayersInRoomsCount()
    {
    	return this.m_playersInRoomsCount;
    }
    
    public void setPlayersInRoomsCount(int value)
    {
    	this.m_playersInRoomsCount = value;
    }

    /**
     * Statistic value available on master server: Rooms currently created.
     */
    public int m_roomsCount;
    
    public int getRoomsCount()
    {
    	return m_roomsCount;
    }

    public void setRoomsCount(int value)
    {
    	m_roomsCount = value;
    }

    /**
     * Internally used to decide if a room must be created or joined on game server.
     */
    private JoinType m_lastJoinType;

    /**
     * Internally used field to make identification of (multiple) clients possible.
     */
    private static int m_clientCount;

    /**
     * Internally used identification of clients. Useful to prefix debug output.
     */
    private int m_clientId;

    public LoadBalancingClient()
    {
    	//super(ConnectionProtocol.Udp);
        this.loadBalancingPeer = new LoadBalancingPeer(this, ConnectionProtocol.Udp);
        this.setMasterServerAddress("app.exitgamescloud.com:5055");

        this.m_clientId = ++m_clientCount;  // only used for debugging
//        this.loadBalancingPeer.setListener(this);
        this.setPlayer(this.createPlayer("meAndroid", -1, true, null));
    }

    public LoadBalancingClient(String masterAddress, String appId, String gameVersion)
    {
    	this();
    	this.setMasterServerAddress(masterAddress);
    	this.setAppId(appId);
    	this.setAppVersion(gameVersion);
    }

    /**
     * Starts the "process" to connect to the master server (initial connect).
     * This includes connecting, establishing encryption, authentification and joining a lobby (if AutoJoinLobby is true).
     * @param appId         Your application's name or ID assigned by Photon Cloud (webpage).
     * @param appVersion    The client's version (clients with differing client appVersions are separated and players don't meet).
     * @param playerName    This player's name.
     * @return true if the operation could be send.
     */
    public boolean connectToMaster(String appId, String appVersion, String playerName)
    {
    	this.setAppId(appId);
    	this.setAppVersion(appVersion);
        this.setPlayerName(playerName);

        if (this.loadBalancingPeer.connect(this.m_masterServerAddress, this.m_appId))
        {
            this.setState(ClientState.ConnectingToMasterserver);
            return true;
        }

        return false;
    }

    /**
     * Starts the "process" to connect to the master server (initial connect).
     * This includes connecting, establishing encryption, authentification and joining a lobby (if AutoJoinLobby is true).
     */
    public boolean connect()
    {
        if (this.loadBalancingPeer.connect(this.getMasterServerAddress(), this.getAppId()))
        {
            this.setState(ClientState.ConnectingToMasterserver);
            return true;
        }

        return false;
    }

    /**
     * Starts the "process" to connect to the master server (initial connect).
     * This includes connecting, establishing encryption, authentification and joining a lobby (if AutoJoinLobby is true).
     */
    public boolean Connect(String serverAddress, String applicationName)
    {
        this.setMasterServerAddress(serverAddress);
        this.setAppId(applicationName);
        return this.connect();
    }

    /**
     * Starts the "process" to connect to the master server (initial connect).
     * This includes connecting, establishing encryption, authentification and joining a lobby (if AutoJoinLobby is true).
     */
    public boolean connect(String serverAddress, String applicationName, byte node)
    {
        this.setMasterServerAddress(serverAddress);
        this.setAppId(applicationName);
        return this.connect();
    }

    /**
     * Disconnects this client from any server.
     */
    public void disconnect()
    {
        this.setState(ClientState.Disconnecting);
        this.loadBalancingPeer.disconnect();
    }

    /**
     * Leaves the CurrentRoom and returns to the Master server (back to the lobby).
     * This method actually is not an operation per se. It sets a state and calls Disconnect().
     * Note: This is is quicker than calling OpLeave and then disconnect (which also triggers a leave).
     * @return true if the current room could be left (impossible while not in a room).
     */
    public boolean opLeaveRoom()
    {
        if (this.m_currentRoom == null || !this.m_currentRoom.isLocalClientInside())
        {
            return false;
        }

        this.setState(ClientState.DisconnectingFromGameserver);
        this.loadBalancingPeer.disconnect();

        return true;
    }

    /**
     * Internally used only.
     */
    private void disconnectToReconnect()
    {
        this.setState((this.m_server == ServerConnection.MasterServer)
                         ? ClientState.DisconnectingFromMasterserver
                         : ClientState.DisconnectingFromGameserver);
        this.loadBalancingPeer.disconnect();
    }

    /**
     * Internally used only.
     * Starts the "process" to connect to the game server (connect before a game is joined).
     */
    private boolean connectToGameServer()
    {
        if (this.loadBalancingPeer.connect(this.getGameServerAddress(), this.getAppId()))
        {
            this.setState(ClientState.ConnectingToGameserver);
            return true;
        }

        // TODO: handle error "cant connect to GS"
        return false;
    }

    /**
     * Operation to join a random, available room.
     * This operation fails if all rooms are closed or full.
     * If successful, the result contains a gameserver address and the name of some room.
     *
     * Note: This sets the state of the client.
     * @param expectedCustomRoomProperties Optional. A room will only be joined, if it matches these custom properties (with String keys).
     * @param expectedMaxPlayers for a particular maxplayer setting. Use 0 to accept any maxPlayer value.
     * If the operation could be sent currently (requires connection).
     */
    public boolean opJoinRandomRoom(HashMap<Object, Object> expectedCustomRoomProperties, byte expectedMaxPlayers)
    {
        this.setState(ClientState.Joining);
        this.m_lastJoinType = JoinType.JoinRandomRoom;
        this.m_currentRoom = new Room();

        return this.loadBalancingPeer.opJoinRandomRoom(expectedCustomRoomProperties, expectedMaxPlayers);
    }

    /**
     * Joins a room by name and sets this player's properties.
     *
     * Note: This sets the state of the client.
     * @param roomName The name of the room to join. Must be existing already, open and non-full or can't be joined.
     * @param playerProperties Custom properties of "this player"  (use String-typed keys but short ones)
     * @return true if the operation could be sent (has to be connected).
     */
    public boolean opJoinRoom(String roomName, HashMap<Object, Object> playerProperties)
    {
        this.setState(ClientState.Joining);
        this.m_lastJoinType = JoinType.JoinRoom;
        this.m_currentRoom = new Room(roomName, null);
        this.getPlayer().cacheProperties(playerProperties);

        HashMap<Object, Object> playerPropsToSend = null;
        if (this.m_server == ServerConnection.GameServer)
        {
            playerPropsToSend = this.getPlayer().getAllProperties();
        }

        return this.loadBalancingPeer.opJoinRoom(roomName, playerPropsToSend);
    }

    /**
     * Creates a room (on either Master or Game Server).
     * The response depends on the server the peer is connected to:
     * Master will return a Game Server to connect to.
     * Game Server will return the Room's data.
     * Creating a room, will automatically send this local player's properties (name and custom ones).
     * This is an async request which triggers a OnOperationResponse() call.
     *
     * Note:  This sets the state of the client.</remarks>
     * @param roomName">The name to create a room with. Must be unique and not in use or can't be created. 
     * @param isVisible">Shows the room in the lobby's room list. 
     * @param isOpen">Keeps players from joining the room (or opens it to everyone). 
     * @param maxPlayers">Max players before room is considered full (but still listed). 
     * @param customGameProperties">Custom properties to apply to the room on creation (use String-typed keys but short ones). 
     * @param customPropsToListInLobby">Custom properties that should be listed in the lobby. Use null or string[0] to list no custom props. MaxPlayers, etc will be in lobby in any case.
     * @return true if the operation could be sent (has to be connected).
     */
    public boolean opCreateRoom(String roomName, boolean isVisible, boolean isOpen, byte maxPlayers, HashMap<Object, Object> customGameProperties, String[] customPropsToListInLobby)
    {
        this.setState(ClientState.Joining);
        this.m_lastJoinType = JoinType.CreateRoom;
        this.m_currentRoom = new Room(roomName, customGameProperties, isVisible, isOpen, maxPlayers, customPropsToListInLobby);

        HashMap<Object, Object> playerPropsToSend = null;
        if (this.m_server == ServerConnection.GameServer)
        {
            playerPropsToSend = this.getPlayer().getAllProperties();
        }

        return this.loadBalancingPeer.opCreateRoom(roomName, isVisible, isOpen, maxPlayers, customGameProperties, playerPropsToSend, customPropsToListInLobby);
    }

    /**
     * This updates the local cache of a player's properties before sending them to the server.
     * Use this only when in state Joined.
     *
     * @param actorNr">ID of player to update/set properties for. 
     * @param actorProperties">The properties to set for target actor. 
     * @return If sending the properties to the server worked (not if the operation was executed successfully).
     */
    protected boolean opSetPropertiesOfActor(int actorNr, HashMap<Object, Object> actorProperties)
    {
        Player target = this.m_currentRoom.getPlayer(actorNr);
        if (target != null)
        {
            target.cacheProperties(actorProperties);
        }

        return this.loadBalancingPeer.opSetPropertiesOfActor(actorNr, actorProperties);
    }

    /**
     * This updates the current room's properties before sending them to the server.
     * Use this only when in state Joined.
     *
     * @param gameProperties">The roomProperties to udpate or set. 
     * @return If sending the properties to the server worked (not if the operation was executed successfully).
     */
    protected boolean opSetPropertiesOfRoom(HashMap<Object, Object> gameProperties)
    {
        this.m_currentRoom.cacheProperties(gameProperties);
        return this.loadBalancingPeer.opSetPropertiesOfRoom(gameProperties);
    }


    
    /**
     * Internally used only.
     * Reads out properties coming from the server in events and operation responses (which might be a bit tricky).
     */
    private void readoutProperties(HashMap<Object, Object> gameProperties, HashMap<Object, Object> actorProperties, int targetActorNr)
    {
        // Debug.LogWarning("ReadoutProperties game=" + gameProperties + " actors(" + actorProperties + ")=" + actorProperties + " " + targetActorNr);
        // read game properties and cache them locally
        if (this.m_currentRoom != null && gameProperties != null)
        {
            this.m_currentRoom.cacheProperties(gameProperties);
        }

        if (actorProperties != null && actorProperties.size() > 0)
        {
            if (targetActorNr > 0)
            {
                // we have a single entry in the actorProperties with one user's name
                // targets MUST exist before you set properties
                Player target = this.m_currentRoom.getPlayer(targetActorNr);
                if (target != null)
                {
                    target.cacheProperties(this.readoutPropertiesForActorNr(actorProperties, targetActorNr));
                }
            }
            else
            {
                // in this case, we've got a key-value pair per actor (each
                // value is a hashtable with the actor's properties then)
                int actorNr;
                HashMap<Object, Object> props;
                String newName;
                Player target;

                for (Object key : actorProperties.keySet())
                {
                    actorNr = (Integer)key;
                    props = (HashMap)actorProperties.get(key);
                    newName = (String)props.get(ActorProperties.PlayerName);
                    
                    target = this.m_currentRoom.getPlayer(actorNr);
                    if (target == null)
                    {
                        target = this.createPlayer(newName, actorNr, false, props);
                        this.m_currentRoom.storePlayer(target);
                    }
                    else
                    {
                        target.cacheProperties(props);
                    }
                }
            }
        }
    }

    /**
     * Internally used only to read properties for a distinct actor (which might be the hashtable OR a key-pair value IN the actorProperties).
     */
    private HashMap<Object, Object> readoutPropertiesForActorNr(HashMap<Object, Object> actorProperties, int actorNr)
    {
        if (actorProperties.containsKey(actorNr))
        {
            return (HashMap)actorProperties.get(actorNr);
        }

        return actorProperties;
    }

    /**
     * Internally used to set the LocalPlayer's ID.
     *
     * @param newID">New actor ID (a.k.a actorNr) assigned when joining a room.
     */
    protected void changeLocalID(int newID)
    {
        if (this.getPlayer() == null)
        {
            this.debugReturn(DebugLevel.WARNING, String.format("Local actor is null or not in mActors! mLocalActor: {0} mActors==null: {1} newID: {2}", 
            		this.getPlayer(), this.m_currentRoom.getPlayers() == null, newID));
        }

        if (this.m_currentRoom == null)
        {
            // change to new actor/player ID
            this.getPlayer().changeLocalID(newID);
        }
        else
        {
            // remove old actorId from actor list
            this.m_currentRoom.removePlayer(this.getPlayer());

            // change to new actor/player ID
            this.getPlayer().changeLocalID(newID);

            // update the room's list with the new reference
            this.m_currentRoom.storePlayer(this.getPlayer());

            // make this client known to the local player (used to get state and to sync values from within Player)
            this.getPlayer().m_loadBalancingClient = this;
        }
    }

    /**
     * Internally used to clean up local instances of players and room.
     */
    private void cleanCachedValues()
    {
        this.changeLocalID(-1);

        // if this is called on the gameserver, we clean the room we were in. on the master, we keep the room to get into it
        if (this.m_server == ServerConnection.GameServer)
        {
            this.m_currentRoom = null;    // players get cleaned up inside this, too, except LocalPlayer (which we keep)
        }

        // when we leave the master, we clean up the rooms list (which might be updated by the lobby when we join again)
        if (this.m_server == ServerConnection.MasterServer)
        {
            this.m_roomInfoList.clear();
        }
    }

    /**
     * Called internally, when a game was joined or created on the game server.
     * This reads the response, finds out the local player's actorNumber (a.k.a. Player.ID) and applies properties of the room and players.
     *
     * @param operationResponse">
     */
    private void gameEnteredOnGameServer(OperationResponse operationResponse)
    {
        if (operationResponse.ReturnCode != 0)
        {
            switch (operationResponse.OperationCode)
            {
                case OperationCode.CreateGame:
                    this.debugReturn(DebugLevel.ERROR, "Create failed on GameServer. Changing back to MasterServer.");
                    break;
                case OperationCode.JoinGame:
                case OperationCode.JoinRandomGame:
                    this.debugReturn(DebugLevel.ERROR, "Join failed on GameServer. Changing back to MasterServer.");

                    if (operationResponse.ReturnCode == ErrorCode.GameDoesNotExist)
                    {
                        this.debugReturn(DebugLevel.INFO, "Most likely the game became empty during the switch to GameServer.");
                    }

                    // TODO: add callback to join failed
                    break;
            }

            this.disconnectToReconnect();
            return;
        }

        this.setState(ClientState.Joined);
        this.m_currentRoom.m_loadBalancingClient = this;
        this.m_currentRoom.setLocalClientInside(true);

        // the local player's actor-properties are not returned in join-result. add this player to the list
        int localActorNr = (Integer)operationResponse.get(ParameterCode.ActorNr);
        this.changeLocalID(localActorNr);

        HashMap<Object, Object> actorProperties = (HashMap<Object, Object>)operationResponse.get(ParameterCode.PlayerProperties);
        HashMap<Object, Object> gameProperties = (HashMap<Object, Object>)operationResponse.get(ParameterCode.GameProperties);
        this.readoutProperties(gameProperties, actorProperties, 0);
        
        switch (operationResponse.OperationCode)
        {
            case OperationCode.CreateGame:
                // TODO: add callback "game created"
                break;
            case OperationCode.JoinGame:
            case OperationCode.JoinRandomGame:
                // TODO: add callback "game joined"
                break;
        }
    }

    /**
     * Internally used "factory" method to create a player.
     * Override this method to replace Player with some extended class (and provide your own, game-specific values in there).
     *
     * @param actorName"> 
     * @param actorNumber"> 
     * @param isLocal"> 
     * @param actorProperties"> 
     * @return created Player object
     */
    protected Player createPlayer(String actorName, int actorNumber, boolean isLocal, HashMap<Object, Object> actorProperties)
    {
        Player newPlayer = new Player(actorName, actorNumber, isLocal);
        newPlayer.cacheProperties(actorProperties);
        return newPlayer;
    }

    /**
     * Debug output of low level api (and this client).
     */
    @Override
    public void debugReturn(DebugLevel level, String message)
    {
        System.out.println(message);
    }

    /**
     * Uses the operationResponse's provided by the server to advance the internal state and call ops as needed.
     */
    @Override
    public void onOperationResponse(OperationResponse operationResponse)
    {
        switch (operationResponse.OperationCode)
        {
            case OperationCode.Authenticate:
                {
                    if (operationResponse.ReturnCode != 0)
                    {
                        this.setState(ClientState.Disconnecting);
                        this.disconnect();
                        break;
                    }

                    if (this.getState() == ClientState.ConnectedToMaster)
                    {
                        this.setState(ClientState.Authenticated);
                        if (this.getAutoJoinLobby()) 
                        {
                            this.loadBalancingPeer.opJoinLobby();
                        }
                    }
                    else if (this.getState() == ClientState.ConnectedToGameserver)
                    {
                        this.setState(ClientState.Joining);
                        if (this.m_lastJoinType == JoinType.JoinRoom || this.m_lastJoinType == JoinType.JoinRandomRoom)
                        {
                            // if we just "join" the game, do so
                            this.opJoinRoom(this.m_currentRoom.getName(), this.getPlayer().m_customProperties);
                        }
                        else if (this.m_lastJoinType == JoinType.CreateRoom)
                        {
                            this.loadBalancingPeer.opCreateRoom(
                                this.m_currentRoom.getName(),
                                this.m_currentRoom.isVisible(),
                                this.m_currentRoom.isOpen(),
                                this.m_currentRoom.getMaxPlayers(),
                                this.m_currentRoom.getCustomProperties(),
                                this.getPlayer().m_customProperties,
                                this.m_currentRoom.getPropsListedInLobby()
                            );
                        }
                        break;
                    }
                    break;
                }

            case OperationCode.Leave:
                this.cleanCachedValues();
                break;

            case OperationCode.JoinLobby:
                this.setState(ClientState.JoinedLobby);
                break;

            case OperationCode.JoinRandomGame:  // this happens only on the master server. on gameserver this is a "regular" join
            case OperationCode.CreateGame:
            case OperationCode.JoinGame:
                {
                    if (this.m_server == ServerConnection.GameServer)
                    {
                        this.gameEnteredOnGameServer(operationResponse);
                    }
                    else
                    {
                        if (operationResponse.ReturnCode == ErrorCode.NoRandomMatchFound)
                        {
                            // this happens only for JoinRandomRoom
                            // TODO: implement callback/reaction when no random game could be found (this is no bug and can simply happen if no games are open)
                            this.setState(ClientState.JoinedLobby); // TODO: maybe we have to return to another state here (if we didn't join a lobby)
                            break;
                        }

                        // TODO: handle more error cases

                        if (operationResponse.ReturnCode != 0)
                        {
                            if (this.loadBalancingPeer.getDebugOut().atLeast(DebugLevel.ERROR))
                            {
                                this.debugReturn(DebugLevel.ERROR, String.format(
                                		"Getting into game failed, client stays on masterserver: {0}.", 
                                		operationResponse.ToStringFull()));
                            }

                            this.setState(ClientState.JoinedLobby); // TODO: maybe we have to return to another state here (if we didn't join a lobby)
                            break;
                        }

                        this.setGameServerAddress((String)operationResponse.get(ParameterCode.Address));
                        String gameId = (String)(operationResponse.get(ParameterCode.RoomName));
                        if (gameId != null && gameId.length() > 0)
                        {
                            // is only sent by the server's response, if it has not been sent with the client's request before!
                            this.m_currentRoom.setName(gameId);
                        }
                        
                        this.disconnectToReconnect();
                    }

                    break;
                }
        }
    }

    /**
     * Uses the connection's statusCodes to advance the internal state and call ops as needed.
     */
    @Override
    public void onStatusChanged(StatusCode statusCode)
    {
        switch (statusCode)
        {
            case Connect:
                if (this.getState() == ClientState.ConnectingToGameserver)
                {
                    if (this.loadBalancingPeer.getDebugOut().atLeast(DebugLevel.ALL))
                    {
                        this.debugReturn(DebugLevel.ALL, "Connected to gameserver.");
                    }

                    this.setState(ClientState.ConnectedToGameserver);
                    this.m_server = ServerConnection.GameServer;
                }

                if (this.getState() == ClientState.ConnectingToMasterserver)
                {
                    if (this.loadBalancingPeer.getDebugOut().atLeast(DebugLevel.ALL))
                    {
                        this.debugReturn(DebugLevel.ALL, "Connected to masterserver.");
                    }

                    this.setState(ClientState.ConnectedToMaster);
                    this.m_server = ServerConnection.MasterServer;
                }

                this.loadBalancingPeer.establishEncryption();
                break;

            case Disconnect:
                // disconnect due to connection exception is handled below (don't connect to GS or master in that case)

                this.cleanCachedValues();

                if (this.getState() == ClientState.Disconnecting)
                {
                    this.setState(ClientState.Disconnected);
                }
                else if (this.getState() == ClientState.Uninitialized)
                {
                    // when client never connects. avoids "connect to gameserver" (would be done below)
                    this.setState(ClientState.Disconnected);
                }
                else if (this.getState() != ClientState.Disconnected)
                {
                    if (this.m_server == ServerConnection.GameServer)
                    {
                        this.connectToMaster(this.getAppId(), this.getAppVersion(), this.getPlayerName());
                    }
                    else if (this.m_server == ServerConnection.MasterServer)
                    {
                        this.connectToGameServer();
                    }
                }
                break;

            case EncryptionEstablished:
                if (!this.loadBalancingPeer.opAuthenticate(this.getAppId(), this.getAppVersion()))
                {
                    this.debugReturn(DebugLevel.ERROR, "Error Authenticating! Did not work.");
                }
                break;

            case ExceptionOnConnect:
            case DisconnectByServer:
            case TimeoutDisconnect:
            case Exception:
                this.setState(ClientState.Disconnected);  // each could/should have it's own reaction
                break;
        }
    }

    /**
     * Uses the photonEvent's provided by the server to advance the internal state and call ops as needed.
     */
    @Override
    public void onEvent(EventData photonEvent)
    {
        if (photonEvent.Code == EventCode.GameList || photonEvent.Code == EventCode.GameListUpdate)
        {
        	if (photonEvent.Code == EventCode.GameList)
        	{
        		this.m_roomInfoList = new TypedHashMap<String, RoomInfo>(String.class, RoomInfo.class);
        	}

        	HashMap<Object, Object> games = (HashMap<Object, Object>)photonEvent.get(ParameterCode.GameList);
        	for (Object gameName : games.keySet())
        	{
        		RoomInfo game = new Room((String)gameName, (HashMap<Object, Object>)games.get((String)gameName));
        		if (game.m_removedFromList)
        		{
        			this.m_roomInfoList.remove(gameName);
        		}
        		else
        		{
        			this.m_roomInfoList.put((String)gameName, game);
        		}
        	}
        }
        else if (photonEvent.Code == EventCode.Join)
        {
            String actorName = null;
            int actorNr = (Integer)photonEvent.get(ParameterCode.ActorNr);  // actorNr (a.k.a. playerNumber / ID) of sending player
            boolean isLocal = this.getPlayer().getID() == actorNr;

            HashMap<Object, Object> actorProperties = (HashMap<Object, Object>)photonEvent.get(ParameterCode.PlayerProperties);
            if (actorProperties != null) {
                actorName = (String)actorProperties.get(ActorProperties.PlayerName);
            }

        	if (!isLocal) {
        		Player newPlayer = this.createPlayer(actorName, actorNr, isLocal, actorProperties);
        		this.m_currentRoom.storePlayer(newPlayer);
        	}

            Integer[] playerList = (Integer[])photonEvent.get(ParameterCode.ActorList);
            if (playerList != null) {
                for (int i=0; i<playerList.length; i++) {
                    if (this.m_currentRoom.getPlayer(playerList[i]) == null) {
                        Player newPlayer = this.createPlayer(null, playerList[i], false, null);
                        this.m_currentRoom.storePlayer(newPlayer);
                    }
                }
            }
        }
        else if (photonEvent.Code == EventCode.Leave)
        {
        	int actorID = (Integer)photonEvent.get(ParameterCode.ActorNr);
        	this.m_currentRoom.removePlayer(actorID);
        }
        else if (photonEvent.Code == EventCode.PropertiesChanged)
        {
        	// whenever properties are sent in-room, they can be broadcasted as event (which we handle here)
        	// we get PLAYERproperties if actorNr > 0 or ROOMproperties if actorNumber is not set or 0
        	int targetActorNr = 0;
        	if (photonEvent.Parameters.containsKey(ParameterCode.TargetActorNr))
        	{
        		targetActorNr = (Integer)photonEvent.get(ParameterCode.TargetActorNr);
        	}
        	HashMap<Object, Object> props = (HashMap<Object, Object>)photonEvent.get(ParameterCode.Properties);

        	if (targetActorNr > 0)
        	{
        		this.readoutProperties(null, props, targetActorNr);
        	}
        	else 
        	{
        		this.readoutProperties(props, null, 0);
        	}
                
        }
        else if (photonEvent.Code == EventCode.AppStats)
        {
        	// only the master server sends these in (1 minute) intervals
        	this.setPlayersInRoomsCount((Integer)photonEvent.get(ParameterCode.PeerCount));
        	this.setRoomsCount((Integer)photonEvent.get(ParameterCode.GameCount));
        	this.setPlayersOnMasterCount((Integer)photonEvent.get(ParameterCode.MasterPeerCount));
        }
    }

}
