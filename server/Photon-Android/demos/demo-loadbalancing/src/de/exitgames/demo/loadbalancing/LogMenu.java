package de.exitgames.demo.loadbalancing;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Iterator;

public class LogMenu extends Activity{
	
	Button	m_backButton;
	ScrollView m_scrollView;
	TextView m_textView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log);
        
        m_textView = (TextView)findViewById(R.id.textView1);
        m_scrollView = (ScrollView)findViewById(R.id.scrollView1);

        m_backButton = (Button)findViewById(R.id.btnBack);
        m_backButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ApplicationManager.getConsole().setScrollViewHandler(null);
				ApplicationManager.getConsole().setTextViewHandler(null);
				finish();
			}
		});

        
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	synchronized (ApplicationManager.getConsole())
    	{
            Iterator<String> messIter = ApplicationManager.getConsole().getMessageQueue().iterator();
            while (messIter.hasNext())
                m_textView.append(messIter.next());
	    	
	    	m_scrollView.smoothScrollTo(0, m_textView.getLineHeight()*m_textView.getLineCount());
	    	ApplicationManager.getConsole().setScrollViewHandler(m_scrollViewHandler);
	    	ApplicationManager.getConsole().setTextViewHandler(m_textViewHandler);
    	}
    }
    
    public void onDestroy()
    {
    	super.onDestroy();
    }
    
    final Handler m_textViewHandler = new Handler() {
        public void handleMessage(Message msg) {
            String message = (String) msg.obj;
            m_textView.append(message);
        }
    };

    final Handler m_scrollViewHandler = new Handler() {
        public void handleMessage(Message msg) {
        	m_scrollView.smoothScrollTo(0, m_textView.getLineHeight()*m_textView.getLineCount());
        }
    };
}
