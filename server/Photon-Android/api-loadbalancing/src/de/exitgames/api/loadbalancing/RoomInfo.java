package de.exitgames.api.loadbalancing;

import java.util.HashMap;

import de.exitgames.client.photon.SupportClass;

/** 
* Used as long as a Room is just listed but not (yet) joined. A simplified room with just the required info.
* 
* Note:
* This class resembles info about available rooms, as sent by the Master server's lobby. 
* Consider all values as readonly. None are synced (only updated by events by server).
*/
public abstract class RoomInfo
{

    /**
     * Used internally in lobby, to mark rooms that are no longer listed.
     */
    boolean m_removedFromList;

    /**
     * Backing field for property.
     */
    HashMap<Object, Object> m_customProperties = new HashMap<Object, Object>();

    /**
     * Backing field for property.
     */
    protected byte m_maxPlayers = 0;

    /**
     * Backing field for property.
     */
    protected boolean m_isOpen = true;

    /**
     * Backing field for property.
     */
    protected boolean m_isVisible = true;

    /**
     * Backing field for property.
     */
    protected String m_name;

    /**
     * Custom properties of a room. All keys are string-typed and the values depend on the game/application.
     *
     * @return
     */
    public HashMap<Object, Object> getCustomProperties()
    {
    	return this.m_customProperties;
    }

    /**
     * The name of a room. Unique identifier (per Loadbalancing group) for a room/match.
     *
     * @return
     */
    public String getName()
    {
    	return m_name;
    }

    /**
     * Only used internally in lobby, to display number of players in room (while you're not in).
     */
    private int m_playerCount;
    
    public int getPlayerCount()
    {
    	return m_playerCount;
    }
    
    public void setPlayerCount(int count)
    {
    	this.m_playerCount = count;
    }

    /**
     * State if the local client is already in the game or still going to join it on gameserver (in lobby always false).
     */
    private boolean m_localClientInside;
    
    public boolean isLocalClientInside()
    {
    	return m_localClientInside;
    }
    
    public void setLocalClientInside(boolean value)
    {
    	m_localClientInside = value;
    }

    /**
     * Sets a limit of players to this room. This property is shown in lobby, too.
     * If the room is full (players count == maxplayers), joining this room will fail.
     *
     * Note:
     * As part of RoomInfo this can't be set.
     * As part of a Room (which the player joined), the setter will update the server and all clients.
     */
    public byte getMaxPlayers()
    {
    	return this.m_maxPlayers;
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
    public boolean isOpen()
    {
    	return this.m_isOpen;
    }

    /*
     * Defines if the room is listed in its lobby.
     * Rooms can be created invisible, or changed to invisible.
     * To change if a room can be joined, use property: open.
     *
     * Note:
     * As part of RoomInfo this can't be set.
     * As part of a Room (which the player joined), the setter will update the server and all clients.
     */
    public boolean isVisible()
    {
    	return this.m_isVisible;
    }

    /**
     * Constructs a RoomInfo to be used in room listings in lobby.
     *
     * @param roomName
     * @param properties
     */
    protected RoomInfo(String roomName, HashMap<Object,Object> properties)
    {
        this.cacheProperties(properties);

        this.m_name = roomName;
    }

    /**
     * Makes RoomInfo comparable (by name).
     */
    public boolean equals(Object p)
    {
        Room pp = (Room)p;
        return (pp != null && this.m_name.equals(pp.m_name));
    }

    /**
     * Accompanies Equals, using the name's HashCode as return.
     *
     * @return
     */
    public int getHashCode()
    {
        return this.m_name.hashCode();
    }

    /* Simple printingin method.
     * @return String showing the RoomInfo.
     */
    public String toString()
    {
        return String.format("Room: '%1s' visible: %2b open: %3b max: %4d count: %5d\ncustomProps: %6s", 
        		this.m_name, this.m_isVisible, this.m_isOpen, this.m_maxPlayers, this.m_playerCount, 
        		SupportClass.HashMapToString(m_customProperties));
    }

    /** Copies "well known" properties to fields (isVisible, etc) and caches the custom properties (string-keys only) in a local hashtable.
     * @param propertiesToCache New or updated properties to store in this RoomInfo.
     */
    protected void cacheProperties(HashMap<Object, Object> propertiesToCache)
    {
        if (propertiesToCache == null || propertiesToCache.size() == 0 || this.m_customProperties.equals(propertiesToCache))
        {
            return;
        }

        // check of this game was removed from the list. in that case, we don't
        // need to read any further properties
        // list updates will remove this game from the game listing
        if (propertiesToCache.containsKey(GameProperties.Removed))
        {
            this.m_removedFromList = (Boolean)propertiesToCache.get(GameProperties.Removed);
            if (this.m_removedFromList)
            {
                return;
            }
        }

        // fetch the "well known" properties of the room, if available
        if (propertiesToCache.containsKey(GameProperties.MaxPlayers))
        {
            this.m_maxPlayers = (Byte)propertiesToCache.get(GameProperties.MaxPlayers);
        }

        if (propertiesToCache.containsKey(GameProperties.IsOpen))
        {
            this.m_isOpen = (Boolean)propertiesToCache.get(GameProperties.IsOpen);
        }

        if (propertiesToCache.containsKey(GameProperties.IsVisible))
        {
            this.m_isVisible = (Boolean)propertiesToCache.get(GameProperties.IsVisible);
        }

        if (propertiesToCache.containsKey(GameProperties.PlayerCount))
        {
            this.m_playerCount = (int)((Byte)propertiesToCache.get(GameProperties.PlayerCount));
        }

        // merge the custom properties (from your application) to the cache (only string-typed keys will be kept)
        Extensions.mergeStringKeys(this.m_customProperties, propertiesToCache);
    }
}
