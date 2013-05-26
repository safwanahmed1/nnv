package de.exitgames.demo.loadbalancing;

import android.app.ProgressDialog;

public class ProgressBar
{
	static ProgressDialog m_progressDialog = null;

	public static ProgressDialog createDialog(String text)
	{
		m_progressDialog = new ProgressDialog(
				ApplicationManager.getCurrentActivityContext());
		m_progressDialog.setOwnerActivity(ApplicationManager
				.getCurrentActivity());
		m_progressDialog.setMessage(text);
		m_progressDialog.setIndeterminate(true);
		m_progressDialog.setCancelable(false);
		return m_progressDialog;
	}

	public static void show(final String message)
	{
		ApplicationManager.getCurrentActivity().runOnUiThread(new Runnable() {

			@Override
			public void run()
			{
				m_progressDialog = new ProgressDialog(ApplicationManager
						.getCurrentActivityContext());
				m_progressDialog.setMessage(message);
				m_progressDialog.setIndeterminate(true);
				m_progressDialog.setCancelable(false);
				m_progressDialog.show();
			}
		});
	}

	public static void hide()
	{
		if (m_progressDialog != null)
			if (m_progressDialog.isShowing())
				m_progressDialog.dismiss();

		m_progressDialog = null;
	}
}
