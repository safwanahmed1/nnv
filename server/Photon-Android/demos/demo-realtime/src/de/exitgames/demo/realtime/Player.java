package de.exitgames.demo.realtime;

import java.util.HashMap;

import de.exitgames.client.photon.LitePeer;
import de.exitgames.client.photon.enums.EventCaching;
import de.exitgames.client.photon.enums.ReceiverGroup;


public class Player 
{
	Player(int id)
	{
		x = 5;
		y = 5;
		color = 0xFFFFFFFF;
		this.id = id;
	}
	
    static final byte EV_PLAYER_INFO = 0;
    static final byte EV_MOVE = 1;
    static final byte STATUS_PLAYER_POS_X = 0;
    static final byte STATUS_PLAYER_POS_Y = 1;
    static final byte STATUS_PLAYER_NAME = 2;
    static final byte STATUS_PLAYER_COLOR = 3;
	
	public int id;
	public Byte x;
	public Byte y;
	public Integer lastReceivedConsecutiveNumber = 0;
	public Integer lastSentConsecutiveNumber = 0;
	public String name;
	public int color;
	
    public void setPosition(HashMap<Byte, Object> evData)
    {
        this.x = (Byte)evData.get(STATUS_PLAYER_POS_X);
        this.y = (Byte)evData.get(STATUS_PLAYER_POS_Y);
    }
	
    public void setInfo(HashMap<Byte, Object> neutronEvent)
    {
        if (neutronEvent == null)
        {
            System.out.println("Empty Event! Clearing name.");
            name = null;
            return;
        }

        this.name = new String(neutronEvent.get(STATUS_PLAYER_NAME).toString());
        this.color = ((Integer)neutronEvent.get(STATUS_PLAYER_COLOR)).intValue();
    }
    
    public void sendPlayerInfo(LitePeer peer)
    {
        if (peer == null)
        {
            return;
        }

        HashMap<Byte, Object> playerInfo = new HashMap<Byte, Object>();
        playerInfo.put(STATUS_PLAYER_NAME, this.name);
        playerInfo.put(STATUS_PLAYER_COLOR, this.color);

        peer.opRaiseEvent(EV_PLAYER_INFO, playerInfo, true, (byte)0, EventCaching.ReplaceCache, ReceiverGroup.All);
    }
}
