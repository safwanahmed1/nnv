package de.exitgames.api.loadbalancing;

public class GameProperties {
    /**
     * (255) Max number of players that "fit" into this room. 0 is for "unlimited".
     */
    public static final byte MaxPlayers = (byte)255;

    /**
     * (254) Makes this room listed or not in the lobby on master.
     */
    public static final byte IsVisible = (byte)254;

    /**
     * (253) Allows more players to join a room (or not).
     */
    public static final byte IsOpen = (byte)253;

    /**
     * (252) Current count od players in the room. Used only in the lobby on master.
     */
    public static final byte PlayerCount = (byte)252;

    /**
     * (251) True if the room is to be removed from room listing (used in update to room list in lobby on master)
     */
    public static final byte Removed = (byte)251;

    /**
     * (250) A list of the room properties to pass to the RoomInfo list in a lobby. This is used in CreateRoom, which defines this list once per room.
     */
    public static final byte PropsListedInLobby = (byte)250;

    /**
     * (249) Equivalent of Operation Join parameter CleanupCacheOnLeave
     */
    public static final byte CleanupCacheOnLeave = (byte)249;
}
