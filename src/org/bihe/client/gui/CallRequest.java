package org.bihe.client.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.bihe.client.io.IO;
import org.bihe.utils.User;

public class CallRequest extends Frame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 490757662090704364L;
	private static int userID = -2;
	private static CallRequest instance = null;

	public CallRequest(User u) {
		// if another call request window was open, close that
		if (instance != null)
			instance.dispose();
		this.setAlwaysOnTop(true);
		userID = u.getId();
		//process that
		this.setSize(300, 140);
		this.setLocation(IO.getMainFrame().getLocation().x + IO.WIDTH/2 - this.getSize().width / 2,
				IO.getMainFrame().getLocation().y + IO.HEIGHT/2 - this.getSize().height / 2);
		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(10,10,10,10));
		JPanel text = new JPanel(new BorderLayout());
		text.add(new JLabel("Call request has sent to " + u.getName(), SwingConstants.CENTER),BorderLayout.NORTH);
		text.add(new JLabel("Please wait for user response.", SwingConstants.CENTER), BorderLayout.SOUTH);
		content.add(text,BorderLayout.NORTH);
		JButton cancel = new JButton("Cancel Request");
		content.add(cancel, BorderLayout.SOUTH);
		this.setVisible(true);
		cancel.addActionListener((ActionEvent arg0) -> {
			userID = -1;
			this.dispose();
		});
		instance = this;
		this.add(content);
	}
	public static int getUserID() {
		return userID;
	}
	public static void setUserID(int userID) {
		CallRequest.userID = userID;
	}
	public static CallRequest getInstance() {
		return instance;
	}
}
