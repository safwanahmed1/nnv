package de.exitgames.api.loadbalancing;

import java.util.HashMap;

import de.exitgames.client.photon.IPhotonPeerListener;
import de.exitgames.client.photon.PhotonPeer;
import de.exitgames.client.photon.TypedHashMap;
import de.exitgames.client.photon.enums.ConnectionProtocol;
import de.exitgames.client.photon.enums.DebugLevel;
import de.exitgames.client.photon.enums.EventCaching;
import de.exitgames.client.photon.enums.ReceiverGroup;

/**
 * A LoadbalancingPeer provides the operations and enum definitions needed to use the loadbalancing server app(s).
 * The LoadBalancingPeer does not keep a state, instead this is done by a LoadBalancingClient.
 */
public class LoadBalancingPeer extends PhotonPeer {

	public LoadBalancingPeer(ConnectionProtocol protocolType) {
		super(protocolType);
	}

    /// <summary>
    /// Creates a Peer with the given connection protocol.
    /// </summary>
    public LoadBalancingPeer(IPhotonPeerListener listener, ConnectionProtocol protocolType)
    {
        super(listener, protocolType);
    }

    /**
     * Joins the lobby on the Master Server, where you get a list of RoomInfos of currently open rooms.
     * This is an async request which triggers a OnOperationResponse() call.
     * @return
     * If the operation could be sent (has to be connected).</returns>
     */
    public boolean opJoinLobby()
    {
        if (this.getDebugOut().atLeast(DebugLevel.INFO))
        {
            this.getListener().debugReturn(DebugLevel.INFO, "OpJoinLobby()");
        }

        return this.opCustom(OperationCode.JoinLobby, null, true);
    }
    
    /**
     * Leaves the lobby on the Master Server.
     * This is an async request which triggers a OnOperationResponse() call.
     * @return
     * If the operation could be sent (has to be connected).
     */
    public boolean opLeaveLobby()
    {
        if (this.getDebugOut().atLeast(DebugLevel.INFO))
        {
            this.getListener().debugReturn(DebugLevel.INFO, "OpLeaveLobby()");
        }

        return this.opCustom(OperationCode.LeaveLobby, null, true);
    }

    /// <summary>
    /// Creates a room (on either Master or Game Server).
    /// Internal, used by LoadBalancingClient, which caches the player properties.
    ///
    /// The response depends on the server the peer is connected to: 
    /// Master will return a Game Server to connect to.
    /// Game Server will return the Room's data.
    /// This is an async request which triggers a OnOperationResponse() call.
    /// </summary>
    /// <param name="roomName"></param>
    /// <param name="isVisible"></param>
    /// <param name="isOpen"></param>
    /// <param name="maxPlayers"></param>
    /// <param name="customGameProperties"></param>
    /// <param name="playerProperties"></param>
    /// <param name="propsListedInLobby">A list of the room properties to pass to the RoomInfo list in a lobby. This is used in CreateRoom, which defines this list once per room.</param>
    /// <returns>If the operation could be sent (has to be connected).</returns>
    public boolean opCreateRoom(String roomName, boolean isVisible, boolean isOpen, byte maxPlayers, HashMap<Object, Object> customGameProperties, HashMap<Object, Object> playerProperties, String[] propsListedInLobby)
    {
        if (this.getDebugOut().atLeast(DebugLevel.INFO))
        {
            this.getListener().debugReturn(DebugLevel.INFO, "OpCreateRoom()");
        }

        // well-known game properties
        HashMap<Byte, Object> gameProperties = new HashMap<Byte, Object>();
        gameProperties.put(GameProperties.IsOpen, isOpen);
        gameProperties.put(GameProperties.IsVisible, isVisible);
        if (maxPlayers > 0)
        {
            gameProperties.put(GameProperties.MaxPlayers, maxPlayers);
        }

        // custom properties: define which should be listed in the lobby and merge those that are set now
        propsListedInLobby = (propsListedInLobby == null) ? new String[0] : propsListedInLobby;
        gameProperties.put(GameProperties.PropsListedInLobby, propsListedInLobby);
        Extensions.mergeStringKeys(gameProperties, customGameProperties);

        TypedHashMap<Byte, Object> op = new TypedHashMap<Byte, Object>(Byte.class, Object.class);
        op.put(ParameterCode.GameProperties, gameProperties);
        op.put(ParameterCode.Broadcast, true);
        if (playerProperties != null)
        {
        	op.put(ParameterCode.PlayerProperties, playerProperties);
        }

        if (roomName != null && roomName.length() > 0)
        {
        	op.put(ParameterCode.RoomName, roomName);
        }

        return this.opCustom(OperationCode.CreateGame, op, true);
    }

    /// <summary>
    /// Joins a room by name and sets this player's properties.
    /// </summary>
    /// <param name="roomName"></param>
    /// <param name="playerProperties"></param>
    /// <returns>If the operation could be sent (has to be connected).</returns>
    public boolean opJoinRoom(String roomName, HashMap<Object,Object> playerProperties)
    {
        if (this.getDebugOut().atLeast(DebugLevel.INFO))
        {
            this.getListener().debugReturn(DebugLevel.INFO, "OpJoinRoom()");
        }

        if (roomName == null || roomName.length() == 0)
        {
            this.getListener().debugReturn(DebugLevel.ERROR, "OpJoinRoom() failed. Please specify a roomname.");
            return false;
        }

        TypedHashMap<Byte, Object> op = new TypedHashMap<Byte, Object>(Byte.class, Object.class);
        op.put(ParameterCode.RoomName, roomName);
        op.put(ParameterCode.Broadcast, true);
        if (playerProperties != null)
        {
        	op.put(ParameterCode.PlayerProperties, playerProperties);
        }

        //Listener.DebugReturn(DebugLevel.INFO, OperationCode.JoinGame + ": " + SupportClass.DictionaryToString(op));
        return this.opCustom(OperationCode.JoinGame, op, true);
    }

    /// <summary>
    /// Operation to join a random, available room. 
    /// This operation fails if all rooms are closed or full.
    /// If successful, the result contains a gameserver address and the name of some room.
    /// </summary>
    /// <param name="expectedCustomRoomProperties">Optional. A room will only be joined, if it matches these custom properties (with string keys).</param>
    /// <param name="expectedMaxPlayers">Filters for a particular maxplayer setting. Use 0 to accept any maxPlayer value.</param>
    /// <returns>If the operation could be sent currently (requires connection).</returns>
    public boolean opJoinRandomRoom(HashMap<Object, Object> expectedCustomRoomProperties, byte expectedMaxPlayers)
    {
        if (this.getDebugOut().atLeast(DebugLevel.INFO))
        {
            this.getListener().debugReturn(DebugLevel.INFO, "OpJoinRandomRoom()");
        }
        
        HashMap<Object,Object> expectedRoomProperties = new HashMap<Object,Object>();
        Extensions.mergeStringKeys(expectedRoomProperties, expectedCustomRoomProperties);
        if (expectedMaxPlayers > 0)
        {
            expectedRoomProperties.put(GameProperties.MaxPlayers, expectedMaxPlayers);
        }

        TypedHashMap<Byte, Object> opParameters = new TypedHashMap<Byte, Object>(Byte.class, Object.class);
        if (expectedRoomProperties != null && expectedRoomProperties.size() > 0)
        {
            opParameters.put(ParameterCode.GameProperties, expectedRoomProperties);
        }

        this.getListener().debugReturn(DebugLevel.INFO, OperationCode.JoinRandomGame + ": " + opParameters.toString());
        return this.opCustom(OperationCode.JoinRandomGame, opParameters, true);
    }

    /// <summary>
    /// Sets custom properties of a player / actor (only passing on the string-typed custom properties).
    /// Internally this uses OpSetProperties, which can be used to either set room or player properties.
    /// </summary>
    /// <param name="actorNr">The payer ID (a.k.a. actorNumber) of the player to attach these properties to.</param>
    /// <param name="actorProperties">The custom properties to add or update.</param>
    /// <returns>If the operation could be sent (has to be connected).</returns>
    public boolean opSetCustomPropertiesOfActor(int actorNr, HashMap<Object,Object> actorProperties)
    {
        return this.opSetPropertiesOfActor(actorNr, Extensions.stripToStringKeys(actorProperties));
    }

    /// <summary>
    /// Sets properties of a player / actor.
    /// Internally this uses OpSetProperties, which can be used to either set room or player properties.
    /// </summary>
    /// <param name="actorNr">The payer ID (a.k.a. actorNumber) of the player to attach these properties to.</param>
    /// <param name="actorProperties">The properties to add or update.</param>
    /// <returns>If the operation could be sent (has to be connected).</returns>
    protected boolean opSetPropertiesOfActor(int actorNr, HashMap<Object,Object> actorProperties)
    {
        if (this.getDebugOut().atLeast(DebugLevel.INFO))
        {
            this.getListener().debugReturn(DebugLevel.INFO, "OpSetPropertiesOfActor()");
        }

        TypedHashMap<Byte,Object> opParameters = new TypedHashMap<Byte,Object>(Byte.class, Object.class);
        opParameters.put(ParameterCode.Properties, actorProperties);
        opParameters.put(ParameterCode.ActorNr, actorNr);
        opParameters.put(ParameterCode.Broadcast, true);
        
        return this.opCustom((byte)OperationCode.SetProperties, opParameters, true, (byte)0, false);
    }

    /// <summary>
    /// Set a "well known" property of a room.
    /// Internally this uses OpSetProperties, which can be used to either set room or player properties.
    /// </summary>
    /// <param name="propCode"></param>
    /// <returns>If the operation could be sent (has to be connected).</returns>
    protected void opSetPropertyOfRoom(byte propCode, Object value)
    {
        HashMap<Object,Object> properties = new HashMap<Object,Object>();
        properties.put(propCode, value);
        this.opSetPropertiesOfRoom(properties);
    }

    /// <summary>
    /// Sets custom properties of a room (only passing string-typed keys in the HashMap).
    /// Internally this uses OpSetProperties, which can be used to either set room or player properties.
    /// </summary>
    /// <param name="gameProperties"></param>
    /// <returns>If the operation could be sent (has to be connected).</returns>
    public boolean opSetCustomPropertiesOfRoom(HashMap<Object, Object> gameProperties)
    {
        return this.opSetPropertiesOfRoom(Extensions.stripToStringKeys(gameProperties));
    }

    /// <summary>
    /// Sets properties of a room.
    /// Internally this uses OpSetProperties, which can be used to either set room or player properties.
    /// </summary>
    /// <param name="gameProperties"></param>
    /// <returns>If the operation could be sent (has to be connected).</returns>
    protected boolean opSetPropertiesOfRoom(HashMap<Object, Object> gameProperties)
    {
        if (this.getDebugOut().atLeast(DebugLevel.INFO))
        {
            this.getListener().debugReturn(DebugLevel.INFO, "OpSetPropertiesOfRoom()");
        }

        TypedHashMap<Byte, Object> opParameters = new TypedHashMap<Byte, Object>(Byte.class, Object.class);
        opParameters.put(ParameterCode.Properties, gameProperties);
        opParameters.put(ParameterCode.Broadcast, true);

        return this.opCustom((byte)OperationCode.SetProperties, opParameters, true, (byte)0, false);
    }

    /// <summary>
    /// Sends this app's appId and appVersion to identify this application server side.
    /// </summary>
    /// <remarks>
    /// This operation makes use of encryption, if it's established beforehand.
    /// See: EstablishEncryption(). Check encryption with IsEncryptionAvailable.
    /// </remarks>
    /// <param name="appId"></param>
    /// <param name="appVersion"></param>
    /// <returns>If the operation could be sent (has to be connected).</returns>
    public boolean opAuthenticate(String appId, String appVersion)
    {
        if (this.getDebugOut().atLeast(DebugLevel.INFO))
        {
            this.getListener().debugReturn(DebugLevel.INFO, "OpAuthenticate()");
        }

        TypedHashMap<Byte, Object> opParameters = new TypedHashMap<Byte, Object>(Byte.class, Object.class);
        opParameters.put(ParameterCode.AppVersion, appVersion);
        opParameters.put(ParameterCode.ApplicationId, appId);

        return this.opCustom(OperationCode.Authenticate, opParameters, true, (byte)0, this.isEncryptionAvailable());
    }

    /// <summary>
    /// Used in a room to raise (send) an event to the other players. 
    /// Multiple overloads expose different parameters to this frequently used operation.
    /// </summary>
    /// <param name="eventCode">Code for this "type" of event (use a code per "meaning" or content).</param>
    /// <param name="evData">Data to send. HashMap that contains key-values of Photon serializable datatypes.</param>
    /// <param name="sendReliable">Use false if the event is replaced by a newer rapidly. Reliable events add overhead and add lag when repeated.</param>
    /// <param name="channelId">The "channel" to which this event should belong. Per channel, the sequence is kept in order.</param>
    /// <returns>If the operation could be sent (has to be connected).</returns>
    public boolean opRaiseEvent(byte eventCode, HashMap<Object, Object> evData, boolean sendReliable, byte channelId)
    {
        return this.opRaiseEvent(eventCode, evData, sendReliable, channelId, EventCaching.DoNotCache, ReceiverGroup.Others);
    }

    /// <summary>
    /// Used in a room to raise (send) an event to the other players. 
    /// Multiple overloads expose different parameters to this frequently used operation.
    /// </summary>
    /// <param name="eventCode">Code for this "type" of event (use a code per "meaning" or content).</param>
    /// <param name="evData">Data to send. HashMap that contains key-values of Photon serializable datatypes.</param>
    /// <param name="sendReliable">Use false if the event is replaced by a newer rapidly. Reliable events add overhead and add lag when repeated.</param>
    /// <param name="channelId">The "channel" to which this event should belong. Per channel, the sequence is kept in order.</param>
    /// <param name="targetActors">Defines the target players who should receive the event (use only for small target groups).</param>
    /// <returns>If the operation could be sent (has to be connected).</returns>
    public boolean opRaiseEvent(byte eventCode, HashMap<Object, Object> evData, boolean sendReliable, byte channelId, int[] targetActors)
    {
        return this.opRaiseEvent(eventCode, evData, sendReliable, channelId, targetActors, EventCaching.DoNotCache);
    }

    /// <summary>
    /// Used in a room to raise (send) an event to the other players. 
    /// Multiple overloads expose different parameters to this frequently used operation.
    /// </summary>
    /// <param name="eventCode">Code for this "type" of event (use a code per "meaning" or content).</param>
    /// <param name="evData">Data to send. HashMap that contains key-values of Photon serializable datatypes.</param>
    /// <param name="sendReliable">Use false if the event is replaced by a newer rapidly. Reliable events add overhead and add lag when repeated.</param>
    /// <param name="channelId">The "channel" to which this event should belong. Per channel, the sequence is kept in order.</param>
    /// <param name="targetActors">Defines the target players who should receive the event (use only for small target groups).</param>
    /// <param name="cache">Use EventCaching options to store this event for players who join.</param>
    /// <returns>If the operation could be sent (has to be connected).</returns>
    public boolean opRaiseEvent(byte eventCode, HashMap<Object, Object> evData, boolean sendReliable, byte channelId, int[] targetActors, EventCaching cache)
    {
        if (this.getDebugOut().atLeast(DebugLevel.INFO))
        {
            this.getListener().debugReturn(DebugLevel.INFO, "OpRaiseEvent()");
        }

        TypedHashMap<Byte, Object> opParameters = new TypedHashMap<Byte, Object>(Byte.class, Object.class);
        opParameters.put(ParameterCode.Data, evData);
        opParameters.put(ParameterCode.Code, (byte)eventCode);

        if (cache != EventCaching.DoNotCache)
        {
        	opParameters.put(ParameterCode.Cache, (byte)cache.value());
        }

        if (targetActors != null)
        {
        	opParameters.put(ParameterCode.ActorList, targetActors);
        }

        return this.opCustom(OperationCode.RaiseEvent, opParameters, sendReliable, channelId);
    }

    /// <summary>
    /// Used in a room to raise (send) an event to the other players. 
    /// Multiple overloads expose different parameters to this frequently used operation.
    /// </summary>
    /// <param name="eventCode">Code for this "type" of event (use a code per "meaning" or content).</param>
    /// <param name="evData">Data to send. HashMap that contains key-values of Photon serializable datatypes.</param>
    /// <param name="sendReliable">Use false if the event is replaced by a newer rapidly. Reliable events add overhead and add lag when repeated.</param>
    /// <param name="channelId">The "channel" to which this event should belong. Per channel, the sequence is kept in order.</param>
    /// <param name="cache">Use EventCaching options to store this event for players who join.</param>
    /// <param name="receivers">ReceiverGroup defines to which group of players the event is passed on.</param>
    /// <returns>If the operation could be sent (has to be connected).</returns>
    public boolean opRaiseEvent(byte eventCode, HashMap<Object, Object> evData, boolean sendReliable, byte channelId, EventCaching cache, ReceiverGroup receivers)
    {
        if (this.getDebugOut().atLeast(DebugLevel.INFO))
        {
            this.getListener().debugReturn(DebugLevel.INFO, "OpRaiseEvent()");
        }

        TypedHashMap<Byte, Object> opParameters = new TypedHashMap<Byte, Object>(Byte.class, Object.class);
        opParameters.put(ParameterCode.Data, evData);
        opParameters.put(ParameterCode.Code, (byte)eventCode);

        if (receivers != ReceiverGroup.Others)
        {
        	opParameters.put(ParameterCode.ReceiverGroup, (byte)receivers.value());
        }

        if (cache != EventCaching.DoNotCache)
        {
        	opParameters.put(ParameterCode.Cache, (byte)cache.value());
        }

        return this.opCustom((byte)OperationCode.RaiseEvent, opParameters, sendReliable, channelId);
    }
	
}
