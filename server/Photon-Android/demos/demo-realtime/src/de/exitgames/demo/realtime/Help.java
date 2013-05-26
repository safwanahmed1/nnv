package de.exitgames.demo.realtime;
import de.exitgames.demo.realtime.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class Help extends Activity
{
	
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.help);
		
		TextView textView = (TextView)findViewById(R.id.textViewHelp);
		textView.setMovementMethod(new ScrollingMovementMethod());
  		
  		String helpText = "Control your marker with [UP], [DOWN], [LEFT] and [RIGHT]. Use [back] key to leave game and return to menu. \n\n\n";
  		helpText += "SETTINGS \n\n";
  		helpText += "Random move interval:\ninterval in milliseconds between automatic movement of the local player.\n\n";
  		helpText += "send interval:\nOur application will send outgoing data in regular intervals, not immediately upon interaction.";
  		helpText += "This allows npeer to cumulate more data in a single packet. \n\n";
  		helpText += "dispatch interval:\nIncoming messages will be stored in a queue, until the NPeer.dispatchIncomingMessages() method is called.";
  		helpText += "Dispatching empties the queue, and will fire the related callback of each incoming message.\n\n";
  		helpText += "Player name:\nName displayed to other players.\n\n";
  		helpText += "Server address:\nAddress of neutron realtime server. Can contain port.\n\n";
  		helpText += "Game name:\nUnique ID of the Game instance to join / create. \n\n";
  		helpText += "Player color:\nColor of your marker in web format ('#RRGGBB' or 'red' are ok).\n\n";
  		
  		textView.setText(helpText);

	}
}