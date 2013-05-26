package de.exitgames.api.loadbalancing;

import java.util.HashMap;

import de.exitgames.client.photon.SupportClass;

/**
 * Summarizes a "player" within a room, identified (in that room) by actorID.
 *
 * Note: Each player has a actorID, valid for that room. It's -1 until assigned by server.
 */
public class Player
{
    /// Backing field for property.
    private int m_actorID = -1;

    public int getID()
    {
    	return m_actorID;
    }
    
    /// Only one player is controlled by each client. Others are not local.
    public final boolean m_isLocal;
    
    public boolean isLocal()
    {
    	return m_isLocal;
    }

    /** 
     * A reference to the LoadbalancingClient which is currently keeping the connection and state.
     */ 
    protected LoadBalancingClient m_loadBalancingClient;

    /// Background field for Name.
    private String m_name;

    /** Nickname of this player. Also in Properties.
     * Note:
     *A player might change his own playername in a room (it's only a property).
     * Setting this value updates the server and other players (using an operation).
     */  
    public String getName()
    {
    	return this.m_name;
    }
    
    public void setName(String name)
    {
    	if (this.m_name != null && this.m_name.length() != 0 && this.m_name.equals(name))
    	{
    		return;
    	}

    	this.m_name = name;

    	//update a room, if we changed our name (locally, while being in a room)
    	if (this.m_isLocal && this.m_loadBalancingClient != null && this.m_loadBalancingClient.getState() == ClientState.Joined)
    	{
    		this.setPlayerNameProperty();
    	}
    }

    /// Cache for custom properties of player.
    public HashMap<Object, Object> m_customProperties;

    /* Creates a HashMap with all properties (custom and "well known" ones).
     * Note:If used more often, this should be cached.
     */
    public HashMap<Object, Object> getAllProperties()
    {
    	HashMap<Object, Object> allProps = new HashMap<Object, Object>();
    	Extensions.merge(allProps, this.m_customProperties);
    	allProps.put(ActorProperties.PlayerName, this.m_name);
    	return allProps;
    }

    /** 
     * Creates a player instance.
     * To extend and replace this Player, override LoadBalancingPeer.CreatePlayer().
     * 
     * @param name">Name of the player (a "well known property"). 
     * @param actorID">ID or ActorNumber of this player in the current room (a shortcut to identify each player in room) 
     * @param isLocal">If this is the local peer's player (or a remote one).
     */
    protected Player(String name, int actorID, boolean isLocal)
    {
        this.m_customProperties = new HashMap<Object, Object>();
        this.m_isLocal = isLocal;
        this.m_actorID = actorID;
        this.m_name = name;
    }

    /**
     * Caches custom properties for this player.
     */
    protected void cacheProperties(HashMap<Object, Object> properties)
    {
        if (properties == null || properties.size() == 0 || this.m_customProperties.equals(properties))
        {
            return;
        }

        if (properties.containsKey(ActorProperties.PlayerName))
        {
            String nameInServersProperties = (String)properties.get(ActorProperties.PlayerName);
            if (this.m_isLocal)
            {
                // the local playername is different than in the properties coming from the server
                // so the local name was changed and the server is outdated -> update server
                // update property instead of using the outdated name coming from server
                if (!nameInServersProperties.equals(this.m_name))
                {
                    this.setPlayerNameProperty();
                }
            }
            else
            {
                this.m_name = nameInServersProperties;
            }
        }

        Extensions.merge(this.m_customProperties, properties);
    }

    /**
     * Returns name and custom properties.
     */
    public String toString()
    {
        return this.m_name + " " + SupportClass.HashMapToString(this.m_customProperties);
    }

    /**
     * Makes Player comparable
     */
    public boolean equals(Object p)
    {
        Player pp = (Player)p;
        return (pp != null && this.getHashCode() == pp.getHashCode());
    }

    /// 
    public int getHashCode()
    {
        return this.m_actorID;
    }

    /**
     * The player with the lowest actorID is the master and could be used for special tasks.
     */
    public boolean isMasterClient()
    {
        throw new UnsupportedOperationException("implement after playerlist is moved from client to room");
    }

    /**
     * Used internally, to update this client's playerID when assigned.
     */
    protected void changeLocalID(int newID)
    {
        if (!this.m_isLocal)
        {
            //Debug.LogError("ERROR You should never change Player IDs!");
            return;
        }

        this.m_actorID = newID;
    }

    /**
     * Updates the custom properties of this Room with propertiesToSet.
     * Only string-typed keys are applied, new properties (string keys) are added, existing are updated
     * and if a value is set to null, this will remove the custom property.
     *
     * Note: Local cache is updated immediately, other players are updated through Photon with a fitting operation.
     *
     * @param propertiesToSet
     */
    @SuppressWarnings("unchecked")
	public void setCustomProperties(HashMap<Object, Object> propertiesToSet)
    {
        HashMap<Object, Object> customProps = (HashMap<Object, Object>)Extensions.stripToStringKeys(propertiesToSet);

        // merge (delete null-values)
        Extensions.merge(this.m_customProperties, customProps);
        Extensions.stripKeysWithNullValues(this.m_customProperties);

        // send (sync) these new values
        this.m_loadBalancingClient.loadBalancingPeer.opSetCustomPropertiesOfActor(this.m_actorID, customProps);
    }

    /**
     * Uses OpSetPropertiesOfActor to set this player's name.
     */

    private void setPlayerNameProperty()
    {
        HashMap<Object, Object> properties = new HashMap<Object, Object>();
        properties.put(ActorProperties.PlayerName, this.m_name);
        this.m_loadBalancingClient.opSetPropertiesOfActor(this.getID(), properties);
    }
}
