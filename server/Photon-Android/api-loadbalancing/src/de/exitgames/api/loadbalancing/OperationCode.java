package de.exitgames.api.loadbalancing;

import de.exitgames.client.photon.enums.LiteOpCode;

public class OperationCode {
    /**
     * (230) Authenticates this peer and connects to a virtual application
     */
    public static final byte Authenticate = (byte)230;

    /**
     * (229) Joins lobby (on master)
     */
    public static final byte JoinLobby = (byte)229;

    /**
     * (228) Leaves lobby (on master)
     */
    public static final byte LeaveLobby = (byte)228;

    /**
     * (227) Creates a game (or fails if name exists)
     */
    public static final byte CreateGame = (byte)227;

    /**
     * (226) Join game (by name)
     */
    public static final byte JoinGame = (byte)226;

    /**
     * (225) Joins random game (on master)
     */
    public static final byte JoinRandomGame = (byte)225;
    
    // public static final byte CancelJoinRandom = 224; // obsolete, cause JoinRandom no longer is a "process". now provides result immediately
    
    public static final byte Leave = (byte)LiteOpCode.Leave;

    /**
     * (253) Raise event (in a room, for other actors/players)
     */
    public static final byte RaiseEvent = (byte)LiteOpCode.RaiseEvent;

    /**
     * (252) Set Properties (of room or actor/player)
     */
    public static final byte SetProperties = (byte)LiteOpCode.SetProperties;

    /**
     * (251) Get Properties
     */
    public static final byte GetProperties = (byte)LiteOpCode.GetProperties;
}
