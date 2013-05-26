package de.exitgames.demo.loadbalancing;

import java.util.ArrayList;
import java.util.Map.Entry;

import de.exitgames.api.loadbalancing.Player;
import de.exitgames.api.loadbalancing.RoomInfo;
import de.exitgames.client.photon.TypedHashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.KeyEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenu extends Activity {

	ArrayList<String>	m_currentRoomListName = new ArrayList<String>();
	
	ListView m_buttonsListView, m_roomsListView;
	TextView m_roomNameTextView, m_roomsCountTextView, m_playersCountTextView, m_eventsTextView, m_TrafficStatsTextView;
	
	final String[] m_disconnectedButtons = {"Show Log", "Exit"};
	final String[] m_lobbyButtons = {"Show log", "Create game", "Exit"};
	final String[] m_gameButtons = {"Show log", "Send event", "Send room properties", "Send player properties", "Leave room", "Show Stats", "Exit"};
	
	final OnItemClickListener m_disconnectedClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			switch (position)
			{
			case 0:
				ApplicationManager.pushActivity(new Intent(ApplicationManager.getCurrentActivityContext(), LogMenu.class));
				break;
			case 1:
				ApplicationManager.getClient().disconnect();
				ApplicationManager.setState(ApplicationManager.STATE_DISCONNECTED);
                ApplicationManager.getClient().setEventCount(0);
				ApplicationManager.closeAllActivities();
				break;
			}
		}
	};
	
	final OnItemClickListener m_lobbyClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            switch (position) {
                case 0:
                    ApplicationManager.pushActivity(new Intent(ApplicationManager.getCurrentActivityContext(), LogMenu.class));
                    break;
                case 1:
                    LayoutInflater inflater = LayoutInflater.from(ApplicationManager.getCurrentActivityContext());
                    View root = inflater.inflate(R.layout.enter_room_name, null);
                    final AutoCompleteTextView edit = (AutoCompleteTextView) root.findViewById(R.id.roomName);

                    AlertDialog.Builder b = new AlertDialog.Builder(ApplicationManager.getCurrentActivityContext());
                    b.setView(root);
                    b.setTitle("Enter room name");
                    b.setPositiveButton(R.string.btnCreate,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
//								if (edit.getText().length() > 0)
                                    {
                                        ApplicationManager.getClient().createNewRoom(edit.getText().toString());
                                    }
                                }
                            });
                    b.setNegativeButton(R.string.btnCancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            });
                    b.show();
                    break;
                case 2:
                    ApplicationManager.getClient().disconnect();
                    ApplicationManager.setState(ApplicationManager.STATE_DISCONNECTED);
                    ApplicationManager.getClient().setEventCount(0);
                    ApplicationManager.closeAllActivities();
                    break;
            }
        }
    };

	final OnItemClickListener m_gameClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			switch (position)
			{
			case 0:
				ApplicationManager.pushActivity(new Intent(ApplicationManager.getCurrentActivityContext(), LogMenu.class));
				break;
			case 1:
				ApplicationManager.getClient().sendSomeEvent();
				break;
			case 2:
				ApplicationManager.getClient().setRoomProperties();
				break;
			case 3:
				ApplicationManager.getClient().setCustomPlayerProps();
				break;
			case 4:
				ApplicationManager.setState(ApplicationManager.STATE_LOBBY);
				ProgressBar.createDialog("Please wait...").show();
				ApplicationManager.getClient().opLeaveRoom();
				break;
            case 5:
                // show stats
                ApplicationManager.pushActivity(new Intent(ApplicationManager.getCurrentActivityContext(), TrafficStatsView.class));
                break;
			case 6:
				ApplicationManager.getClient().disconnect();
				ApplicationManager.setState(ApplicationManager.STATE_DISCONNECTED);
                ApplicationManager.getClient().setEventCount(0);
				ApplicationManager.closeAllActivities();
				break;
			}
		}
	};

	final OnItemClickListener m_roomClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			synchronized(ApplicationManager.getClient())
			{
				if (!ApplicationManager.getClient().joinSelectedRoom(m_currentRoomListName.get(position)))
					Toast.makeText(ApplicationManager.getCurrentActivityContext(), "Join room failed. See LogCat.", Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	Console	m_console;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.main, null);
        
        ApplicationManager.registerActivity(this);
        
        setContentView(v);
        
        m_buttonsListView = (ListView)findViewById(R.id.buttons);
        m_roomsListView = (ListView)findViewById(R.id.rooms);
        
        m_roomNameTextView = (TextView)findViewById(R.id.room_name);
        m_roomsCountTextView = (TextView)findViewById(R.id.rooms_count);
        m_playersCountTextView = (TextView)findViewById(R.id.players_count);
        m_eventsTextView = (TextView)findViewById(R.id.events_count);
        m_TrafficStatsTextView = (TextView)findViewById(R.id.TrafficStats);

        updateMenu();
      
        new Thread(ApplicationManager.getClient()).start();
	}
	
	public void updateRoomList()
	{
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run()
			{
				switch (ApplicationManager.getState())
				{
				case ApplicationManager.STATE_DISCONNECTED:
					m_currentRoomListName.clear();
					m_roomsListView.setAdapter(new ArrayAdapter<String>(
							ApplicationManager.getCurrentActivityContext(), R.layout.rooms_list, m_currentRoomListName.toArray(new String[0])));
					m_roomsListView.setClickable(false);
					break;
				case ApplicationManager.STATE_LOBBY:
					TypedHashMap<String, RoomInfo> rooms = ApplicationManager.getClient().getRooms();
					if (rooms != null)
					{
						m_currentRoomListName.clear();
						ArrayList<String> roomsStrings = new ArrayList<String>();
						for (Entry<String, RoomInfo> room : rooms.entrySet())
						{
							roomsStrings.add(new String(room.getKey() + ": " + room.getValue().toString()));
							m_currentRoomListName.add(room.getKey());
						}
						
						m_roomsListView.setAdapter(new ArrayAdapter<String>(
								ApplicationManager.getCurrentActivityContext(), R.layout.rooms_list, roomsStrings.toArray(new String[0])));
						m_roomsListView.setClickable(true);
						m_roomsListView.setOnItemClickListener(m_roomClickListener);
					}
					break;
				case ApplicationManager.STATE_GAME:
					TypedHashMap<Integer, Player> players = ApplicationManager.getClient().getCurrentRoom().getPlayers();
					if (players != null)
					{
						ArrayList<String> playersStrings = new ArrayList<String>();
						for (Entry<Integer, Player> player : players.entrySet())
						{
							playersStrings.add(player.toString());
						}
						
						ArrayAdapter<String> adapter = new ArrayAdapter<String>(
								ApplicationManager.getCurrentActivityContext(), R.layout.rooms_list, playersStrings.toArray(new String[0])) {
							@Override
							public boolean isEnabled(int position)
							{
								return false;
							}
						};
						m_roomsListView.setAdapter(adapter);
					}
					break;
				}
				
			}
		});
	}

    /*
    * Simply show usage of TrafficStats if enabled
    * */
    public void updateTrafficStats()
    {
        this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (ApplicationManager.getClient().loadBalancingPeer.getTrafficStatsEnabled())
                {
                    m_TrafficStatsTextView.setText("Packages out: " + ApplicationManager.getClient().loadBalancingPeer.TrafficStatsOutgoing().getTotalPacketCount());
                }
            }
        });
    }

    public void updateStatistics() {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                switch (ApplicationManager.getState()) {
                    default:
                    case ApplicationManager.STATE_DISCONNECTED:
                        m_roomsCountTextView.setText("Not connected");
                        m_roomNameTextView.setText("");
                        m_playersCountTextView.setText("");
                        m_eventsTextView.setText("");
                        break;
                    case ApplicationManager.STATE_LOBBY:
                        m_roomsCountTextView.setText("Rooms count: " + ApplicationManager.getClient().getRoomsCount());
                        m_roomNameTextView.setText("");
                        m_playersCountTextView.setText("Players on Master: " + ApplicationManager.getClient().getPlayersOnMasterCount());
                        m_eventsTextView.setText("");
                        break;
                    case ApplicationManager.STATE_GAME:
                        m_roomsCountTextView.setText("Room properties: " + ApplicationManager.getClient().getCurrentRoom().getCustomProperties().toString());
                        m_roomNameTextView.setText("Room name: " + ApplicationManager.getClient().getCurrentRoom().getName());
                        m_playersCountTextView.setText("Players in all rooms: " + ApplicationManager.getClient().getPlayersInRoomsCount());
                        m_eventsTextView.setText("Events received: " + ApplicationManager.getClient().getEventCount());
                        break;
                }
            }
        });
    }

    public void updateButtons() {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                switch (ApplicationManager.getState()) {
                    default:
                    case ApplicationManager.STATE_DISCONNECTED:
                        m_buttonsListView.setAdapter(new ArrayAdapter<String>(ApplicationManager.getCurrentActivityContext(), R.layout.buttons_list, m_disconnectedButtons));
                        m_buttonsListView.setOnItemClickListener(m_disconnectedClickListener);
                        break;
                    case ApplicationManager.STATE_LOBBY:
                        m_buttonsListView.setAdapter(new ArrayAdapter<String>(ApplicationManager.getCurrentActivityContext(), R.layout.buttons_list, m_lobbyButtons));
                        m_buttonsListView.setOnItemClickListener(m_lobbyClickListener);
                        break;
                    case ApplicationManager.STATE_GAME:
                        m_buttonsListView.setAdapter(new ArrayAdapter<String>(ApplicationManager.getCurrentActivityContext(), R.layout.buttons_list, m_gameButtons));
                        m_buttonsListView.setOnItemClickListener(m_gameClickListener);
                        break;
                }
            }
        });
    }
	
	public void updateMenu()
	{
		updateButtons();
		updateStatistics();
		updateRoomList();
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_HOME)
        {
            ApplicationManager.getClient().disconnect();
            ApplicationManager.setState(ApplicationManager.STATE_DISCONNECTED);
            ApplicationManager.getClient().setEventCount(0);
            ApplicationManager.closeAllActivities();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
