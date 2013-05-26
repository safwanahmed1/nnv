package de.exitgames.demo.realtime;
import java.util.Hashtable;
import java.util.Enumeration;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;

public class GameField extends Activity implements DemoServiceCallback
{
	public static final int BOARD_SIZE_X = 16;
	public static final int BOARD_SIZE_Y = 16;
	
	public static final int BOARD_POS_X = 30;
	public static final int BOARD_POS_Y = 30;
	
	public static final int BOARD_CELL_SIZE = 16;
	
	DemoService serviceInstance;
	GameFieldView view;
	
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		view = new GameFieldView(this);
		setContentView(view); 
		
		bindService(new Intent(GameField.this, DemoService.class), serviceConnection, Context.BIND_AUTO_CREATE);		
	}
	
	
	public void onDestroy()
	{
		super.onDestroy();
		
		serviceInstance.removeCallbackListener(GameField.this);
		serviceInstance = null;
		unbindService(serviceConnection);
	}
	
	
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		boolean ret = false;
		if (null != view)
			ret = view.onKeyDown(keyCode, event);
		
		if (ret)
			return true;
		return super.onKeyDown(keyCode, event);
	}
	
	
	private static class GameFieldView extends View 
	{ 
		private Paint paintGrid;
		private Paint paintPlayer;
		private Paint paintPlayerFlare;
		private Paint paintText;
		
		DemoService serviceInstance;
		
		public GameFieldView(Context context) 
		{
			super(context);
			
			paintGrid = new Paint();
			paintGrid.setAntiAlias(true); 
			paintGrid.setStyle(Paint.Style.FILL); 
			paintGrid.setColor(0xFFAAAAFF);
			paintGrid.setStrokeWidth(1);
			
			paintPlayer = new Paint();
			paintPlayer.setAntiAlias(true); 
			paintPlayer.setStyle(Paint.Style.FILL); 
			
			paintText = new Paint();
			paintText.setAntiAlias(true); 
			paintText.setColor(0xFFFFFFFF);
			
			paintPlayerFlare = new Paint();
			paintPlayerFlare.setAntiAlias(true);
			paintPlayerFlare.setColor(0xBBFFFFFF);
		}

		
		public boolean onKeyDown(int keyCode, KeyEvent event)
		{
			if (null == serviceInstance)
				return true;
			
			switch (keyCode)
			{
			case KeyEvent.KEYCODE_DPAD_UP:
				serviceInstance.moveLocalPlayer(0, -1);
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				serviceInstance.moveLocalPlayer(0, 1);
				return true;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				serviceInstance.moveLocalPlayer(-1, 0);
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				serviceInstance.moveLocalPlayer(1, 0);
				return true;
			}
			
			return false;
		}
		
		 
		protected void onDraw(Canvas canvas) 
		{
			canvas.drawColor(Color.BLACK);
			
			drawGrid(canvas);
			
			if (null != serviceInstance)
			{
				Hashtable<Integer, Player> players = serviceInstance.getPlayers();
				if (null != players)
					synchronized (players) 
					{
						for (Enumeration<Integer> e = players.keys(); e.hasMoreElements();)
						{
							Integer id = e.nextElement();
							drawPlayer(canvas, players.get(id));
						}
					}
				
				Stats stats = serviceInstance.getStats();
				if (null != stats)
					drawStats(canvas, stats);
			}

			handler.sendMessageDelayed(Message.obtain(), 100);
		}
		
		private Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				invalidate();
			}
		};
		
		private void drawStats(Canvas c, Stats stats) 
		{
			int x = BOARD_POS_X;
			int y = BOARD_POS_Y + BOARD_SIZE_Y * BOARD_CELL_SIZE + 30;
			
			int textLineHeight = 20;
			int valuesOffset = 160;
			
			c.drawText("Events out of order:", x, y, paintText);
			c.drawText(((Integer)DemoService.NonConsecutiveIssueCount).toString(), x + valuesOffset, y, paintText);
			
			//c.drawText("Number of Players:", x, y, paintText);
			//c.drawText(((Integer)stats.playerNum).toString(), x + valuesOffset, y, paintText);
			
			c.drawText("Events:", x, y + (1 * textLineHeight), paintText);
			c.drawText(((Integer)stats.eventCount).toString(), x + valuesOffset, y + (1 * textLineHeight), paintText);

			//c.drawText("Game time (sec):", x, y + (1 * textLineHeight), paintText);
			//c.drawText(((Integer)stats.timeSec).toString(), x + valuesOffset, y + (1 * textLineHeight), paintText);
			
			c.drawText("Moves sent:", x, y + (2 * textLineHeight), paintText);
			c.drawText(((Integer)stats.movesSent).toString(), x + valuesOffset, y + (2 * textLineHeight), paintText);
			
			c.drawText("Round trip time (msec):", x, y + (3 * textLineHeight), paintText);
			c.drawText(((Integer)stats.roundTripTime).toString(), x + valuesOffset, y + (3 * textLineHeight), paintText);
		}

		private void drawPlayer(Canvas c, Player p) 
		{
			// Select player color with alpha channel adjusted
			paintPlayer.setColor(p.color | 0xFF000000);
			
			c.drawCircle(
					BOARD_POS_X + p.x * BOARD_CELL_SIZE + BOARD_CELL_SIZE / 2, 
					BOARD_POS_Y + p.y * BOARD_CELL_SIZE + BOARD_CELL_SIZE / 2, 
					(int)(BOARD_CELL_SIZE / 2.2), paintPlayer);
			
			c.drawCircle(
					BOARD_POS_X + p.x * BOARD_CELL_SIZE + BOARD_CELL_SIZE / 2 + 2, 
					BOARD_POS_Y + p.y * BOARD_CELL_SIZE + BOARD_CELL_SIZE / 2 - 2, 
					3, paintPlayerFlare);
			
			final String label = p.name + " " + p.id;
			c.drawText(
					label, 
					BOARD_POS_X + p.x * BOARD_CELL_SIZE, 
					BOARD_POS_Y + p.y * BOARD_CELL_SIZE, 
					GameFieldView.this.paintText);
		}

		protected void drawGrid(Canvas c)
		{
			for (int i = 0; i < BOARD_SIZE_X + 1; i++)
				c.drawLine(BOARD_POS_X + i * BOARD_CELL_SIZE, BOARD_POS_Y, BOARD_POS_X + i * BOARD_CELL_SIZE, BOARD_POS_Y + BOARD_SIZE_Y * BOARD_CELL_SIZE, paintGrid);

			for (int i = 0; i < BOARD_SIZE_Y + 1; i++)
				c.drawLine(BOARD_POS_X, BOARD_POS_Y + i * BOARD_CELL_SIZE, BOARD_POS_X + BOARD_SIZE_X * BOARD_CELL_SIZE, BOARD_POS_Y + i * BOARD_CELL_SIZE, paintGrid);
		
		}

		public void setServiceInstance(DemoService instance) 
		{
			serviceInstance = instance;
		}
	}
	

	// This is our anonymous inner class, that is needed for binding our activity to the DemoService().
	// It provides two callback methods that will fire as soon as the service has connected / disconnected.
	private ServiceConnection serviceConnection = new ServiceConnection()
	{
		// As soon as the connection to the Service has been established, the GameField registers itself
		// as a callback listener implementing the DemoServiceCallback Interface. This makes sure our
		// GameField will be notified whenever certain messages are received by the Neutron layer. 
		public void onServiceConnected(ComponentName className, IBinder service) 
		{
			serviceInstance = ((DemoService.LocalBinder)service).getService();
			serviceInstance.addCallbackListener(GameField.this);
			view.setServiceInstance(serviceInstance);
		}
	 
		public void onServiceDisconnected(ComponentName className) 
		{
			serviceInstance = null;
		}
	};

	
	public void errorOccurred(final String message) 
	{
		_handler.post(new Runnable() 
		{
			@Override
			public void run()
			{
				AlertDialog alert = new AlertDialog(GameField.this) {};
				alert.setMessage(message);
				DialogInterface.OnClickListener onOkListener = new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface alert, int arg1) 
					{
						alert.dismiss();
					}
				};
			
				alert.setButton("OK", onOkListener);
				alert.show();
			}
		});
	}


	
	public void loginDone(boolean ok) {}
	
	final Handler _handler = new Handler();
	
}