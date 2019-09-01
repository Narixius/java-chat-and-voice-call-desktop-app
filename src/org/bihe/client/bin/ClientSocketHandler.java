package org.bihe.client.bin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.SerializationUtils;
import org.bihe.client.gui.Avatar;
import org.bihe.client.gui.CallRequest;
import org.bihe.client.gui.CallWindow;
import org.bihe.client.gui.ChatWindow;
import org.bihe.client.gui.MainFrame;
import org.bihe.client.io.IO;
import org.bihe.utils.Message;
import org.bihe.utils.MessageType;
import org.bihe.utils.User;

public class ClientSocketHandler extends Thread {
	ObjectInputStream sInput;

	public ClientSocketHandler(ObjectInputStream sInput) {
		this.sInput = sInput;
	}

	public void run() {
		while (true) {
			try {
				Message message = (Message) sInput.readObject();
				switch (message.getMessageType()) {
				// Check messages type and process them by their type
				case Greeting:
					this.setupUser(message);
					break;
				case ContactsUpdate:
					repaintUsers(message);
					break;
				case ChatMessage:
					try {
						processChatWindow(message);
						IO.getWindows().get(message.getSender().getId())
								.addMessage(new Message(message.getSender(), message.getReciever(),
										Encryption.decrypt((byte[][]) message.getData()), MessageType.ChatMessage,
										false));
					} catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
						e.printStackTrace();
					}
					break;
				case ChatFile:
					try {
						processChatWindow(message);
						IO.getWindows().get(message.getSender().getId())
								.addFileMessage(new Message(message.getSender(), message.getReciever(),
										Encryption.decrypt((byte[][]) message.getData()), MessageType.ChatFile, false));
					} catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
						e.printStackTrace();
					}
					break;
				case AvatarUpdate:
					updateContactProfilePic(message);
					break;
				case OfflineUser:
					IO.getContactsPanel().addToOfflineUsers((User) message.getData());
					if (IO.getWindows().get(((User) message.getData()).getId()) != null)
						IO.getWindows().get(((User) message.getData()).getId()).updateStatus(0);
					break;
				case BlockedUser:
					userBlockedThisUser(message);
					break;
				case UnBlockedUser:
					userUnBlockedThisUser(message);
					break;
				case CallRequest:
					callRequest(message);
					break;
				case CallRequestResponse:
					callRequestResponse(message);
					break;
				case Call:
					initializeCall(message);
					break;
				case VoiceCallEnded:
					voiceCallEnded();
					break;
				case VoiceCall:
					break;
				default:
					break;
				}
			} catch (SocketException e) {
				IO.getMainFrame().dispose();
				MainFrame.connectoToServer();
				break;
			} catch (IOException e) {
			} catch (ClassNotFoundException e2) {
				e2.printStackTrace();
			}
		}
	}

	private void voiceCallEnded() {
		//stop call sockets
		System.out.println("call ended");
		JOptionPane.showMessageDialog(CallWindow.getInstance(), "User left the call", "Call ended",
				JOptionPane.ERROR_MESSAGE);
		CallWindow.getInstance().stopCall();
		CallWindow.getInstance().dispose();
	}

	private void initializeCall(Message message) {
		//request users to create call
		new CallWindow(SerializationUtils.clone(IO.getContactsPanel().getUsers().get((int) message.getData())));
	}

	private void callRequestResponse(Message message) {
		//transfere call response
		if (message.getData() instanceof Object[]) {
			Object[] data = (Object[]) message.getData();
			if (((boolean) data[0])) {
				if (CallRequest.getUserID() == message.getReciever().getId() && CallRequest.getInstance().isVisible()) {
					try {
						IO.getSocketWriter()
								.writeObject(new Message(IO.getMe(), null, message.getReciever().getId(), MessageType.Call));
						CallRequest.setUserID(-1);
						CallRequest.getInstance().dispose();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				if (CallRequest.getUserID() == message.getSender().getId()) {
					JOptionPane.showMessageDialog(CallRequest.getInstance(), "User ignored you request",
							"Request ignored", JOptionPane.ERROR_MESSAGE);
					CallRequest.setUserID(-1);
					CallRequest.getInstance().dispose();
				}
			}
		} else {
			boolean bool = (boolean) message.getData();
			if (!bool) {
				JOptionPane.showMessageDialog(CallRequest.getInstance(), "User is in another call", "Request ignored",
						JOptionPane.ERROR_MESSAGE);
				CallRequest.setUserID(-1);
				CallRequest.getInstance().dispose();
			}
		}
	}

	private void callRequest(Message message) {
		//transfer call request
		int res = JOptionPane.showConfirmDialog(IO.getMainFrame(),
				"Call request from " + IO.getContactsPanel().getUsers().get((int) message.getData()).getName(),
				"Call Request", JOptionPane.YES_NO_OPTION);
		boolean response = false;
		if (res == 0) {
			response = true;
		}
		try {
			IO.getSocketWriter().writeObject(new Message(IO.getMe(), null, new Object[] { response, (int) message.getData() },
					MessageType.CallRequestResponse));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processChatWindow(Message message) {
		//create chat window and show it if there is no chat window for the user or frame is not visible
		if (IO.getWindows().get(message.getSender().getId()) == null) {
			IO.getContactsPanel().addToRecentChats(message.getSender());
			ChatWindow cw = new ChatWindow(IO.getMe(), message.getSender());
			IO.setLocation(cw);
			IO.getWindows().put(message.getSender().getId(), cw);
		}
		if (!IO.getWindows().get(message.getSender().getId()).isVisible()) {
			IO.setLocation(IO.getWindows().get(message.getSender().getId()));
			IO.getWindows().get(message.getSender().getId()).setVisible(true);
			IO.getWindows().get(message.getSender().getId()).setAlwaysOnTop(true);
			IO.getWindows().get(message.getSender().getId()).setAlwaysOnTop(false);
		} else {
			IO.getWindows().get(message.getSender().getId()).setAlwaysOnTop(true);
			IO.getWindows().get(message.getSender().getId()).setAlwaysOnTop(false);
		}

	}

	private void updateContactProfilePic(Message message) {
		// process avatar update
		if (IO.getWindows().get(message.getSender().getId()) != null) {
			IO.getWindows().get(message.getSender().getId()).updateHeader(((Avatar) message.getData()));
		}
		IO.getContactsPanel().getUsers().get(message.getSender().getId()).setAvatar(((Avatar) message.getData()));
		IO.getContactsPanel().paintContacts();
		if (CallWindow.getUser() != null && CallWindow.getUser().getId() == message.getSender().getId()) {
			CallWindow.setAvatar((Avatar) message.getData());
		}

	}

	@SuppressWarnings("unchecked")
	private void repaintUsers(Message message) {
		//repaint contacts list
		IO.getContactsPanel().setContactList(((ArrayList<User>) message.getData()));
	}

	private void setupUser(Message message) {
		// setup this user
		IO.setMe((User) message.getData());
		IO.getMainFrame().setVisible(true);
		MainFrame.x.setVisible(false);
	}

	private void userBlockedThisUser(Message message) {
		// the user blocked me
		if (IO.getWindows().get((int) message.getData()) == null) {
			ChatWindow cw = new ChatWindow(IO.getMe(), IO.getContactsPanel().getUsers().get((int) message.getData()), false);
			IO.setLocation(cw);
			IO.getWindows().put((int) message.getData(), cw);
		}
		IO.getWindows().get((int) message.getData()).setUserBlockedMe(true);
		IO.getWindows().get((int) message.getData()).setControllersEnabled(false);
	}

	private void userUnBlockedThisUser(Message message) {
		//user unblocked me
		IO.getWindows().get((int) message.getData()).setUserBlockedMe(false);
		IO.getWindows().get((int) message.getData()).setControllersEnabled(true);
	}
}
