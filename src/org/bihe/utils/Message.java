package org.bihe.utils;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bihe.client.bin.Encryption;

public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8089483497351726936L;
	private User sender;
	private User reciever;
	private Object data;
	private MessageType messageType;

	public Message(User sender, User reciever, Object data, MessageType messageType) {
		super();
		this.sender = sender;
		this.reciever = reciever;
		this.data = data;
		this.messageType = messageType;
		if (messageType == MessageType.ChatMessage || messageType == MessageType.ChatFile) {
		encryptData();
		}
	}

	public Message(User sender, User reciever, Object data, MessageType messageType, boolean encrypt) {
		super();
		this.sender = sender;
		this.reciever = reciever;
		this.data = data;
		this.messageType = messageType;
		if (messageType == MessageType.ChatMessage && encrypt) {
			encryptData();
		}
	}

	private void encryptData() {
		try {
			this.data = Encryption.encrypt(this.data, reciever.getPublicKey());
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException
				| IllegalBlockSizeException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public User getSender() {
		return sender;
	}

	public User getReciever() {
		return reciever;
	}

	public Object getData() {
		return data;
	}

	public MessageType getMessageType() {
		return messageType;
	}

}
