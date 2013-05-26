package de.exitgames.api.loadbalancing;

import de.exitgames.client.photon.enums.LiteOpKey;

public class ParameterCode {
    /// <summary>(230) Address of a (game) server to use.</summary>
    public static final byte Address = (byte)230;
    /// <summary>(229) Count of players in this application in a rooms (used in stats event)</summary>
    public static final byte PeerCount = (byte)229;
    /// <summary>(228) Count of games in this application (used in stats event)</summary>
    public static final byte GameCount = (byte)228;
    /// <summary>(227) Count of players on the master server (in this app, looking for rooms)</summary>
    public static final byte MasterPeerCount = (byte)227;
    /// <summary>(225) User's ID</summary>
    public static final byte UserId = (byte)225;
    /// <summary>(224) Your application's ID: a name on your own Photon or a GUID on the Photon Cloud</summary>
    public static final byte ApplicationId = (byte)224;
    /// <summary>(223) Not used currently. If you get queued before connect, this is your position</summary>
    public static final byte Position = (byte)223;
    /// <summary>(222) List of RoomInfos about open / listed rooms</summary>
    public static final byte GameList = (byte)222;
    /// <summary>(221) Internally used to establish encryption</summary>
    public static final byte Secret = (byte)221;
    /// <summary>(220) Version of your application</summary>
    public static final byte AppVersion = (byte)220;
    /// <summary>(210) Internally used in case of hosting by Azure</summary>
    public static final byte AzureNodeInfo = (byte)210;	// only used within events, so use: EventCode.AzureNodeInfo
    /// <summary>(209) Internally used in case of hosting by Azure</summary>
    public static final byte AzureLocalNodeId = (byte)209;
    /// <summary>(208) Internally used in case of hosting by Azure</summary>
    public static final byte AzureMasterNodeId = (byte)208;

    /// <summary>(255) Code fro the gameId/roomName (a unique name per room). Used in OpJoin and similar.</summary>
    public static final byte RoomName = (byte)LiteOpKey.GameId;
    /// <summary>(250) Code for broadcast parameter of OpSetProperties method.</summary>
    public static final byte Broadcast = (byte)LiteOpKey.Broadcast;
    /// <summary>(252) Code for list of players in a room. Currently not used.</summary>
    public static final byte ActorList = (byte)LiteOpKey.ActorList;
    /// <summary>(254) Code of the Actor of an operation. Used for property get and set.</summary>
    public static final byte ActorNr = (byte)LiteOpKey.ActorNr;
    /// <summary>(249) Code for property set (Hashtable).</summary>
    public static final byte PlayerProperties = (byte)LiteOpKey.ActorProperties;
    /// <summary>(245) Code of data/custom content of an event. Used in OpRaiseEvent.</summary>
    public static final byte CustomEventContent = (byte)LiteOpKey.Data;
    /// <summary>(245) Code of data of an event. Used in OpRaiseEvent.</summary>
    public static final byte Data = (byte)LiteOpKey.Data;
    /// <summary>(244) Code used when sending some code-related parameter, like OpRaiseEvent's event-code.</summary>
    /// <remarks>This is not the same as the Operation's code, which is no longer sent as part of the parameter Dictionary in Photon 3.</remarks>
    public static final byte Code = (byte)LiteOpKey.Code;
    /// <summary>(248) Code for property set (Hashtable).</summary>
    public static final byte GameProperties = (byte)LiteOpKey.GameProperties;
    /// <summary>
    /// (251) Code for property-set (Hashtable). This key is used when sending only one set of properties.
    /// If either ActorProperties or GameProperties are used (or both), check those keys.
    /// </summary>
    public static final byte Properties = (byte)LiteOpKey.Properties;
    /// <summary>(253) Code of the target Actor of an operation. Used for property set. Is 0 for game</summary>
    public static final byte TargetActorNr = (byte)LiteOpKey.TargetActorNr;
    /// <summary>(246) Code to select the receivers of events (used in Lite, Operation RaiseEvent).</summary>
    public static final byte ReceiverGroup = (byte)LiteOpKey.ReceiverGroup;
    /// <summary>(247) Code for caching events while raising them.</summary>
    public static final byte Cache = (byte)LiteOpKey.Cache;
}
