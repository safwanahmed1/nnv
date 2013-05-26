package de.exitgames.api.loadbalancing;

import java.util.HashMap;

import de.exitgames.client.photon.TypedHashMap;
import de.exitgames.client.photon.enums.DebugLevel;

/** 
 * This class is used for room listings mostly. It is able to:
 * - cache "standard" properties (byte keys)
 * - cache custom properties (string keys)
 */ 
public class Room extends RoomInfo
{
    /** 
     * A reference to the LoadbalancingClient which is currently keeping the connection and state.
     */ 
    protected LoadBalancingClient m_loadBalancingClient;
    
    public LoadBalancingClient getLoadBalancingClient()
    {
    	return this.m_loadBalancingClient;
    }
    
    public void setLoadBalancingClient(LoadBalancingClient loadBalancingClient)
    {
    	this.m_loadBalancingClient = loadBalancingClient;
    }

    /** The name of a room. Unique identifier (per Loadbalancing group) for a room/match.
     * Note:The name can't be changed - instead the setter might be used to apply a name created by the server.
     */
    public void setName(String name)
    {
    	m_name = name;
    }

    /** 
     * Defines if the room can be joined.
     * This does not affect listing in a lobby but joining the room will fail if not open.
     * If not open, the room is excluded from random matchmaking. 
     * Due to racing conditions, found matches might become closed before they are joined. 
     * Simply re-connect to master and find another.
     * Use property "IsVisible" to not list the room.
     * 
     * Note:
     * As part of RoomInfo this can't be set.
     * As part of a Room (which the player joined), the setter will update the server and all clients.
     */ 
    public void setOpen(boolean open)
    {
    	if (!this.isLocalClientInside())
    	{
    		this.m_loadBalancingClient.loadBalancingPeer.getListener().debugReturn(DebugLevel.WARNING, "Can't set room properties when not in that room.");
    	}
    		
        if (open != this.isOpen())
        {
        	HashMap<Object, Object> properties = new HashMap<Object, Object>();
        	properties.put(GameProperties.IsOpen, open);
        	m_loadBalancingClient.opSetPropertiesOfRoom(properties);
        }

        this.setOpen(open);
    }

     /**
     * Defines if the room is listed in its lobby.
     * Rooms can be created invisible, or changed to invisible.
     * To change if a room can be joined, use property: open.
     * 
     * Note:
     * As part of RoomInfo this can't be set.
     * As part of a Room (which the player joined), the setter will update the server and all clients.
     */
    public void setVisible(boolean value)
    {
    	if (!this.isLocalClientInside())
    	{
    		m_loadBalancingClient.loadBalancingPeer.getListener().debugReturn(DebugLevel.WARNING, "Can't set room properties when not in that room.");
    	}

    	if (value != this.isVisible())
    	{
    		HashMap<Object, Object> properties = new HashMap<Object, Object>();
    		properties.put(GameProperties.IsVisible, value);
    		m_loadBalancingClient.opSetPropertiesOfRoom(properties);
    	}

    	this.m_isVisible = value;
    }

     /**
     * Sets a limit of players to this room. This property is shown in lobby, too.
     * If the room is full (players count == maxplayers), joining this room will fail.
     * 
     * Note:
     * As part of RoomInfo this can't be set.
     * As part of a Room (which the player joined), the setter will update the server and all clients.
     */
    public void setMaxPlayers(byte value)
    {
    	if (!this.isLocalClientInside())
    	{
    		m_loadBalancingClient.loadBalancingPeer.getListener().debugReturn(DebugLevel.WARNING, "Can't set room properties when not in that room.");
    	}

    	if (value != this.m_maxPlayers)
    	{
    		HashMap<Object, Object> properties = new HashMap<Object, Object>();
    		properties.put(GameProperties.MaxPlayers, value);
    		m_loadBalancingClient.opSetPropertiesOfRoom(properties);
    	}

    	this.m_maxPlayers = value;
    }

    /**
     * Gets the count of players in this Room (using this.LoadBalancingClient.Players.Count).
     */
    public byte getPlayersCount()
    {
    	if (this.getPlayers() == null)
    	{
    		return 0;
    	}
            
    	return (byte)this.getPlayers().size();
    }

    /**
     * While inside a Room, this is the list of players who are also in that room.
     */
    private TypedHashMap<Integer, Player> m_players = new TypedHashMap<Integer, Player>(Integer.class, Player.class);

    private String[] m_propsListedInLobby;

    /**
     * While inside a Room, this is the list of players who are also in that room.
     *
     * @return
     */
    public TypedHashMap<Integer, Player> getPlayers()
    {
    	return m_players;
    }
    
    private void setPlayers(TypedHashMap<Integer, Player> value)
    {
    	m_players = value;
    }

    /**
     * Creates a Room with null for name and no properties.
     */
    protected Room()
    {
        super(null, null);
    }

    /**
     * Creates a Room with given name and properties.
     *
     * @param roomName
     * @param properties
     */
    protected Room(String roomName, HashMap<Object, Object> properties)
    {
        // base sets name and (custom)properties. here we set "well known" properties
    	super(roomName, properties);
    }

    /**
     * Creates a Room with given name and properties and the "listing options" as provided by parameters.
     *
     * @param roomName
     * @param properties
     * @param isVisible
     * @param isOpen
     * @param maxPlayers
     * @param propsListedInLobby
     */
    protected Room(String roomName, HashMap<Object, Object> properties, boolean isVisible, boolean isOpen, byte maxPlayers, String[] propsListedInLobby)
    {
    	super(roomName, properties);
        // base sets name and (custom)properties. here we set "well known" properties
        this.m_isVisible = isVisible;
        this.m_isOpen = isOpen;
        this.m_maxPlayers = maxPlayers;
        this.m_propsListedInLobby = propsListedInLobby;
    }

     /**
     * Updates the custom properties of this Room with propertiesToSet.
     * Only string-typed keys are applied, new properties (string keys) are added, existing are updated
     * and if a value is set to null, this will remove the custom property.
     * 
     * Note:
     * Local cache is updated immediately, other players are updated through Photon with a fitting operation.
     * 
     * @param propertiesToSet
     */
    public void setCustomProperties(HashMap<Object, Object> propertiesToSet)
    {
    	HashMap<Object, Object> customProps = (HashMap<Object, Object>)Extensions.stripToStringKeys(propertiesToSet);

        // merge (delete null-values)
        Extensions.merge(this.m_customProperties, customProps);
        Extensions.stripKeysWithNullValues(this.m_customProperties);

        // send (sync) these new values
        this.m_loadBalancingClient.loadBalancingPeer.opSetCustomPropertiesOfRoom(customProps);
    }

     /**
     * Removes a player from this room's Players Dictionary.
     */
    public void removePlayer(Player player)
    {
        this.m_players.remove(player.getID());
    }
    
     /**
     * Removes a player from this room's Players Dictionary.
     */
    public void removePlayer(int id)
    {
        this.m_players.remove(id);
    }

     /**
     * Adds a player to the list if it doesn't exist already.
     * 
     * @param player
     * @return False if the player could not be added (cause it was in the list already).
     */
    public boolean addPlayer(Player player)
    {
        if (!this.m_players.containsKey(player.getID()))
        {
            this.m_players.put(player.getID(), player);
            return true;
        }

        return false;
    }

     /**
     * Updates a player reference in the Players dictionary (no matter if it existed before or not).
     * 
     * @param player
     */
    public void storePlayer(Player player)
    {
        this.m_players.put(player.getID(), player);
    }

     /**
     * Tries to find the player with given actorNumber (a.k.a. ID).
     * Only useful when in a Room, as IDs are only valid per Room.
     * 
     * @param id ID to look for.
     * @return The player with the ID or null.
     */
    protected Player getPlayer(int id)
    {
        Player result = this.m_players.get(id);
        
        return result;
    }

    public String[] getPropsListedInLobby() {
        return this.m_propsListedInLobby;
    }

    public void setPropsListedInLobby(String[] propsToListInLobby) {
        this.m_propsListedInLobby = propsToListInLobby;
    }
}
