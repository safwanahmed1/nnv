package de.exitgames.demo.loadbalancing;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: Tim Edelmann
 * Date: 22.08.12
 */
public class TrafficStatsView extends Activity
{
    private Button m_BackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.traffic_stats);
        this.m_BackBtn = (Button) findViewById(R.id.statsBtnBack);

        //this.update();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {

                while(true)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            TrafficStatsUpdater tsu = new TrafficStatsUpdater();
                            tsu.update();

                        }
                    });

                    try
                    {
                        Thread.sleep(1000);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

        this.m_BackBtn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // return to last view
                finish();
            }
        });
    }

    // TODO: autoupdate
    public void update()
    {
        this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                TrafficStatsUpdater tsu = new TrafficStatsUpdater();
                tsu.update();
            }
        });
    }

//class MyTimerTask extends TimerTask
class TrafficStatsUpdater
{
    private TextView data;
//    private TextView m_packageHeaderSizeValue;
//    private TextView reliableCommandCountValue;
//    private TextView unreliableCommandCountValue;
//    private TextView fragmentCommandCountValue;
//    private TextView controlCommandCountValue;
//    private TextView totalPacketCountValue;
//    private TextView totalCommandsInPacketsValue;
//    private TextView reliableCommandBytesValue;
//    private TextView unreliableCommandBytesValue;
//    private TextView fragmentCommandBytesValue;
//    private TextView ControlCommandBytesValue;

    public TrafficStatsUpdater()
    {
        this.data = (TextView) findViewById(R.id.data);
//        this.m_packageHeaderSizeValue = (TextView) findViewById(R.id.packageHeaderSizeValue);
//        this.reliableCommandCountValue = (TextView) findViewById(R.id.reliableCommandCountValue);
//        this.unreliableCommandCountValue = (TextView) findViewById(R.id.unreliableCommandCountValue);
//        this.fragmentCommandCountValue = (TextView) findViewById(R.id.fragmentCommandCountValue);
//        this.controlCommandCountValue = (TextView) findViewById(R.id.controlCommandCountValue);
//        this.totalPacketCountValue = (TextView) findViewById(R.id.totalPacketCountValue);
//        this.totalCommandsInPacketsValue = (TextView) findViewById(R.id.totalCommandsInPacketsValue);
//        this.reliableCommandBytesValue = (TextView) findViewById(R.id.reliableCommandBytesValue);
//        this.unreliableCommandBytesValue = (TextView) findViewById(R.id.unreliableCommandBytesValue);
//        this.fragmentCommandBytesValue = (TextView) findViewById(R.id.fragmentCommandBytesValue);
//        this.ControlCommandBytesValue = (TextView) findViewById(R.id.ControlCommandBytesValue);
    }

    public void update()
    {
        // only incomming dat for now
        // TODO: possibly ad outgoing stats too
        this.data.setText("" + ApplicationManager.getClient().loadBalancingPeer.VitalStatsToString(true));
//        this.m_packageHeaderSizeValue.setText(ApplicationManager.getClient().loadBalancingPeer.TrafficStatsIncoming().getPackageHeaderSize() + "");
//        this.reliableCommandCountValue.setText(ApplicationManager.getClient().loadBalancingPeer.TrafficStatsIncoming().getReliableCommandCount() + "");
//        this.unreliableCommandCountValue.setText(ApplicationManager.getClient().loadBalancingPeer.TrafficStatsIncoming().getUnreliableCommandCount() + "");
//        this.fragmentCommandCountValue.setText(ApplicationManager.getClient().loadBalancingPeer.TrafficStatsIncoming().getFragmentCommandCount() + "");
//        this.controlCommandCountValue.setText(ApplicationManager.getClient().loadBalancingPeer.TrafficStatsIncoming().getControlCommandCount() + "");
//        this.totalPacketCountValue.setText(ApplicationManager.getClient().loadBalancingPeer.TrafficStatsIncoming().getTotalPacketCount() + "");
//        this.totalCommandsInPacketsValue.setText(ApplicationManager.getClient().loadBalancingPeer.TrafficStatsIncoming().getTotalCommandsInPackets() + "");
//        this.reliableCommandBytesValue.setText(ApplicationManager.getClient().loadBalancingPeer.TrafficStatsIncoming().getReliableCommandBytes() + "");
//        this.unreliableCommandBytesValue.setText(ApplicationManager.getClient().loadBalancingPeer.TrafficStatsIncoming().getUnreliableCommandBytes() + "");
//        this.fragmentCommandBytesValue.setText(ApplicationManager.getClient().loadBalancingPeer.TrafficStatsIncoming().getFragmentCommandBytes() + "");
//        this.ControlCommandBytesValue.setText(ApplicationManager.getClient().loadBalancingPeer.TrafficStatsIncoming().getControlCommandBytes() + "");
    }
}
}
