package de.exitgames.api.loadbalancing;

public class ErrorCode {
    /**
     * (0) is always "OK", anything else an error or specific situation.
     */
    public static final int Ok = 0;

    /**
     * server - Photon low(er) level: <= 0
     */
    public static final int OperationNotAllowedInCurrentState = -3;
    /**
     *
     */
    public static final int InvalidOperationCode = -2;
    /**
     *
     */
    public static final int InternalServerError = -1;

    /**
     * server - PhotonNetwork: 0x7FFF and down
     * logic-level error codes start with short.max
     */

    /**
     * Authentication failed. Possible cause: AppId is unknown to Photon (in cloud service).</summary>
     */
    public static final int InvalidAuthentication = 0x7FFF;
    /**
     *
     */
    public static final int GameIdAlreadyExists = 0x7FFF - 1;
    /**
     *
     */
    public static final int GameFull = 0x7FFF - 2;
    /**
     *
     */
    public static final int GameClosed = 0x7FFF - 3;
    /**
     *
     */
    public static final int AlreadyMatched = 0x7FFF - 4;
    /**
     *
     */
    public static final int ServerFull = 0x7FFF - 5;
    /**
     *
     */
    public static final int UserBlocked = 0x7FFF - 6;
    /**
     *
     */
    public static final int NoRandomMatchFound = 0x7FFF - 7;
    /**
     *
     */
    public static final int GameDoesNotExist = 0x7FFF - 9;
}
