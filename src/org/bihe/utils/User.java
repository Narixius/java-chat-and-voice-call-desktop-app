package org.bihe.utils;

import java.io.IOException;
import java.io.Serializable;
import java.security.PublicKey;

import javax.swing.JOptionPane;

import org.bihe.client.gui.Avatar;
import org.bihe.client.gui.CallRequest;
import org.bihe.client.io.IO;

public class User implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6968280220771155626L;
	private String name;
	private int id;
	private PublicKey publicKey;
	private Avatar avatar;
	private int state;

	public User(String name, PublicKey publicKey) {
		this.name = name;
		this.publicKey = publicKey;
		this.state = 1;
	};

	public User(String name, int id, PublicKey publicKey) {
		this.id = id;
		this.name = name;
		this.publicKey = publicKey;
		this.state = 1;
	}

	public void call() {
		try {
			if (CallRequest.getInstance() == null || !CallRequest.getInstance().isVisible()) {
				if (CallRequest.getUserID() != this.getId()) {
					new CallRequest(this);
					System.out.println("sending call request");
					IO.getSocketWriter().writeObject(new Message(IO.getMe(), null, this.id, MessageType.CallRequest));
				} else {
					CallRequest.getInstance().requestFocus();
				}
			} else {
				JOptionPane.showMessageDialog(CallRequest.getInstance(), "Already another request is sent to a user",
						"Request not sent", JOptionPane.ERROR_MESSAGE);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setAvatar(Avatar avatar) {
		this.avatar = avatar;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public Avatar getAvatar() {
		return avatar;
	}

	public String stateString() {
		return state == 1 ? "Online" : (state == 0 ? "Offline" : "Blocked");
	}

	public int getstate() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}
