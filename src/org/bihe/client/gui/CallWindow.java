package org.bihe.client.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.bihe.client.bin.VoiceCall;
import org.bihe.client.io.IO;
import org.bihe.utils.User;

public class CallWindow extends Frame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7858869952239978857L;
	private static User user;
	private VoiceCall voiceCall;
	private static CallWindow instance;
	private static Avatar userAvatar;
	public static CallWindow getInstance() {
		return instance;
	}
	public static void setAvatar(Avatar avatar) {
		userAvatar.setIcon(avatar.getIcon());
		userAvatar.repaint();
	}
	public CallWindow(User reciever) {
		instance = this;
		user = reciever;
		initializeCallSocket();
		initializeUI();
	}
	public static User getUser() {
		return user;
	}
	private void initializeUI() {
		setTitle("Call with "+user.getName());
		setResizable(false);
		this.setFont(IO.getFont().deriveFont(Font.PLAIN,15));
		setSize(IO.WIDTH-100, 190);
		JPanel mainPanel = new JPanel();
		JPanel avatar = new JPanel();
		userAvatar = user.getAvatar();
		setAlwaysOnTop(true);
		JPanel txt = new JPanel();
		txt.setPreferredSize(new Dimension(IO.WIDTH, 20));
		txt.add(new JLabel("You are in call with "+user.getName()));
		mainPanel.add(txt);
		this.setLocation(IO.getMainFrame().getLocation().x + IO.WIDTH/2 - this.getSize().width / 2,
				IO.getMainFrame().getLocation().y + IO.HEIGHT/2 - this.getSize().height / 2);
		avatar.setPreferredSize(new Dimension(IO.WIDTH, 60));
		avatar.add(userAvatar);
		mainPanel.add(avatar);
		mainPanel.add(new JLabel(user.getName()));
		JPanel buttons = new JPanel();
		buttons.setPreferredSize(new Dimension(IO.WIDTH, 60));
		JButton endCall = new JButton("End Call");
		buttons.add(endCall);
		mainPanel.add(buttons);
		endCall.addActionListener((ActionEvent e) -> {
			try {
				voiceCall.stopRunning();
			}catch(Exception ee) {
				
			}
			this.dispose();
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent arg0) {
				System.out.println("asdas");
				voiceCall.stopRunning();
			}
		});
		setDefaultCloseOperation(Frame.DISPOSE_ON_CLOSE);
		add(mainPanel);
		this.setVisible(true);

	}

	private void initializeCallSocket() {
		System.out.println("intializing call");
		voiceCall = new VoiceCall();
		voiceCall.start();
	}
	public void stopCall() {
		try {
			voiceCall.stopRunning();
		}catch(Exception ee) {
			
		}
	}
}
