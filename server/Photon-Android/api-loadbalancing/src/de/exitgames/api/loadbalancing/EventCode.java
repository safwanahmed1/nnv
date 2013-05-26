package de.exitgames.api.loadbalancing;

import de.exitgames.client.photon.enums.LiteEventCode;

public class EventCode {
    /**
     * (230) Initial list of RoomInfos (in lobby on Master)
     */
    public static final byte GameList = (byte)230;

    /**
     * (229) Update of RoomInfos to be merged into "initial" list (in lobby on Master)
     */
    public static final byte GameListUpdate = (byte)229;

    /**
     * (228) Currently not used. State of queueing in case of server-full
     */
    public static final byte QueueState = (byte)228;

    /**
     * (227) Currently not used. Event for matchmaking
     */
    public static final byte Match = (byte)227;

    /**
     * (226) Event with stats about this application (players, rooms, etc)
     */
    public static final byte AppStats = (byte)226;

    /**
     * (210) Internally used in case of hosting by Azure
     */
    public static final byte AzureNodeInfo = (byte)210;

    /**
     * (255) Event Join: someone joined the game. The new actorNumber is provided as well as the properties of that actor (if set in OpJoin).
     */
    public static final byte Join = (byte)LiteEventCode.Join;

    /**
     * (254) Event Leave: The player who left the game can be identified by the actorNumber.
     */
    public static final byte Leave = (byte)LiteEventCode.Leave;

    /**
     * (253) When you call OpSetProperties with the broadcast option "on", this event is fired. It contains the properties being set.
     */
    public static final byte PropertiesChanged = (byte)LiteEventCode.PropertiesChanged;
}
