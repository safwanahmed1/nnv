package de.exitgames.api.loadbalancing;

public enum ClientState {
    /// <summary>Peer is created but not used yet.</summary>
    Uninitialized,
    /// <summary>Not used currently.</summary>
    PeerCreated,
    /// <summary>Connecting to master (includes connect, authenticate and joining the lobby)</summary>
    ConnectingToMasterserver,
    /// <summary>Connected to master server.</summary>
    ConnectedToMaster,
    /// <summary>Currently not used.</summary>
    Queued,
    /// <summary>Usually when Authenticated, the client will join a game or the lobby (if AutoJoinLobby is true).</summary>
    Authenticated,
    /// <summary>Connected to master and joined lobby. Display room list and join/create rooms at will.</summary>
    JoinedLobby,
    /// <summary>Transition from master to game server.</summary>
    DisconnectingFromMasterserver,
    /// <summary>Transition to gameserver (client will authenticate and join/create game).</summary>
    ConnectingToGameserver,
    /// <summary>Connected to gameserver (going to auth and join game).</summary>
    ConnectedToGameserver,
    /// <summary>Joining game on gameserver.</summary>
    Joining,
    /// <summary>The client arrived inside a room. CurrentRoom and Players are known. Send events with OpRaiseEvent.</summary>
    Joined,
    /// <summary>Currently not used. Instead of OpLeave, the client disconnects from a server (which also triggers a leave immediately).</summary>
    Leaving,
    /// <summary>Currently not used.</summary>
    Left,
    /// <summary>Transition from gameserver to master (after leaving a room/game).</summary>
    DisconnectingFromGameserver,
    /// <summary>Currently not used.</summary>
    QueuedComingFromGameserver,
    /// <summary>The client disconnects (from any server).</summary>
    Disconnecting,
    /// <summary>The client is no longer connected (to any server). Connect to master to go on.</summary>
    Disconnected
}
