package de.exitgames.demo.loadbalancing;

import android.os.Handler;
import android.os.Message;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Console {

	Handler	m_textViewHandler;
	Handler	m_scrollViewHandler;
    ConcurrentLinkedQueue<String>	m_messageQueue;
	
	Console()
	{
		this(null, null);
	}
	
	Console(Handler textViewHandler, Handler scrollViewHandler)
	{
		this.m_textViewHandler = textViewHandler;
		this.m_scrollViewHandler = scrollViewHandler;
		m_messageQueue = new ConcurrentLinkedQueue<String>();
	}
	
	public void setTextViewHandler(Handler h)
	{
		this.m_textViewHandler = h;
	}
	
	public void setScrollViewHandler(Handler h)
	{
		this.m_scrollViewHandler = h;
	}
	
	public ConcurrentLinkedQueue<String> getMessageQueue()
	{
		return m_messageQueue;
	}
	
	public void writeLine(String text)
	{
		writeLine(text, true);
	}
	
	public void writeLine(String text, boolean scroll)
	{
		write(text + "\n", scroll);
	}
	
	public void write(String text)
	{
		write(text, true);
	}
	
	public void write(String text, boolean scroll)
	{
		while (m_messageQueue.size() >= 1000)
            m_messageQueue.poll();

		m_messageQueue.offer(text);

		if (m_textViewHandler != null)
		{	
			Message msg = m_textViewHandler.obtainMessage();
			msg.obj = text;
			m_textViewHandler.sendMessage(msg);
			if (scroll)
				m_scrollViewHandler.sendEmptyMessage(0);
		}
	}
}
