package de.exitgames.demo.realtime;

public interface DemoServiceCallback
{
	void loginDone(boolean ok);
	void errorOccurred(String message);
}
