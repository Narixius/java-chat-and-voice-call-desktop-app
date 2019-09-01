package org.bihe.server.bin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import org.bihe.client.gui.Avatar;
import org.bihe.utils.Message;
import org.bihe.utils.MessageType;
import org.bihe.utils.User;

public class ServerSocketHandler extends Thread {
	private Socket socket;
	private ObjectInputStream sInput;
	private ObjectOutputStream sOutput;
	private int id;
	private User user;
	private ArrayList<Integer> blockedUsers = new ArrayList<>();
	private String username;

	public ServerSocketHandler(Socket socket) {
		id = Server.getIdCounter();
		this.socket = socket;

	}

	public void run() {
		System.out.println("Thread trying to create Object Input/Output Streams");
		try {
			sOutput = new ObjectOutputStream(socket.getOutputStream());
			sInput = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("Exception creating new Input/output Streams: " + e);
			return;
		}
		boolean keepGoing = true;
		while (keepGoing) {
			try {
				Message message = (Message) sInput.readObject();
				switch (message.getMessageType()) {
				case Greeting:
					System.out.println("User " + (String) message.getData() + " ID: " + id + " Joined");
					username = (String) message.getData();
					this.user = new User(message.getSender().getName(), id, message.getSender().getPublicKey());
					this.user.setAvatar(message.getSender().getAvatar());
					send(new Message(null, null, this.user, MessageType.Greeting));
					Server.getSockets().put(id, socket);
					broadcastOnlineUsers();
					break;
				case ChatFile:
				case ChatMessage:
					sendMessageToReciever(message);
					break;
				case AvatarUpdate:
					updateContact(message);
					break;
				case BlockedUser:
					blockUser(message);
					break;
				case UnBlockedUser:
					UnBlockUser(message);
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
				default:
					break;
				}

			} catch (IOException e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				removeUser();
				System.out.println(username + " Exception reading Streams: " + e);
				keepGoing = false;
				break;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				keepGoing = false;
				break;
			} catch (NullPointerException e) {
				e.printStackTrace();
				Server.getClients().remove(socket);
				keepGoing = false;
				break;
			}

		}
		close();
	}

	private void initializeCall(Message message) {
		Server.getCallRelationchip().put(id, (int) message.getData());
		Server.getCallRelationchip().put((int) message.getData(), id);
		System.out.println(id + " " + (int) message.getData());

		Server.getVoiceSocketsHandlers().put(id,
				((ServerSocketHandler) Server.getClients().get(Server.getSockets().get((int) message.getData()))));
		Server.getVoiceSocketsHandlers().put((int) message.getData(), this);

		try {
			((ServerSocketHandler) Server.getClients().get(Server.getSockets().get((int) message.getData())))
					.send(new Message(null,
							((ServerSocketHandler) Server.getClients()
									.get(Server.getSockets().get((int) message.getData()))).user,
							id, MessageType.Call));

			send(new Message(null, user, (int) message.getData(), MessageType.Call));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void send(Object o) {
		try {
			sOutput.writeObject(o);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void callRequestResponse(Message message) {
		Object[] data = (Object[]) message.getData();
		if (((boolean) data[0])) {
			try {
				((ServerSocketHandler) Server.getClients().get(Server.getSockets().get((int) data[1])))
						.send(new Message(
								((ServerSocketHandler) Server.getClients()
										.get(Server.getSockets().get((int) data[1]))).user,
								user, data, MessageType.CallRequestResponse));

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				((ServerSocketHandler) Server.getClients().get(Server.getSockets().get((int) data[1])))
						.send(new Message(
								((ServerSocketHandler) Server.getClients()
										.get(Server.getSockets().get((int) data[1]))).user,
								user, data, MessageType.CallRequestResponse));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void blockUser(Message message) {
		this.blockedUsers.add((int) message.getData());
		try {
			((ServerSocketHandler) Server.getClients().get(Server.getSockets().get((int) message.getData())))
					.send(new Message(null,
							((ServerSocketHandler) Server.getClients()
									.get(Server.getSockets().get((int) message.getData()))).user,
							id, MessageType.BlockedUser));
		} catch (Exception e) {
		}
	}

	private void callRequest(Message message) {
		int id = (int) message.getData();
		if (Server.getCallRelationchip().get(id) == null) {
			try {
				System.out.println("sending request");
				((ServerSocketHandler) Server.getClients().get(Server.getSockets().get(id))).send(new Message(null,
						((ServerSocketHandler) Server.getClients().get(Server.getSockets().get(id))).user, this.id,
						MessageType.CallRequest));

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			send(new Message(null, user, false, MessageType.CallRequestResponse));

		}
	}

	private void UnBlockUser(Message message) {
		if (this.blockedUsers.contains((int) message.getData())) {
			int c = 0;
			for (int i : blockedUsers) {
				if (i == (int) message.getData()) {
					this.blockedUsers.remove(c);
					break;
				}
				c++;
			}
		}
		try {
			((ServerSocketHandler) Server.getClients().get(Server.getSockets().get((int) message.getData())))
					.send(new Message(null,
							((ServerSocketHandler) Server.getClients()
									.get(Server.getSockets().get((int) message.getData()))).user,
							id, MessageType.UnBlockedUser));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateContact(Message message) {
		user.setAvatar((Avatar) message.getData());
		broadcastContactUpdate(message);
	}

	private void sendMessageToReciever(Message message) {
		try {
			if (!this.blockedUsers.contains(message.getReciever().getId())
					&& !((ServerSocketHandler) Server.getClients()
							.get(Server.getSockets().get(message.getReciever().getId()))).blockedUsers.contains(id)) {
				((ServerSocketHandler) Server.getClients().get(Server.getSockets().get(message.getReciever().getId())))
						.send(message);
			} else {
				// user is blocked
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private synchronized void removeUser() {
		Message m = new Message(null, null, user, MessageType.OfflineUser);
		try {
			Server.getSockets().remove(user.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Server.getClients().remove(socket);
		} catch (Exception e) {
			e.printStackTrace();
		}
		broadCastMessage(m);
		// broadcastOnlineUsers();
	}

	private void broadCastMessage(Message m) {
		for (ServerSocketHandler s : Server.getClients().values()) {
			try {
				s.send(new Message(null, s.user, m.getData(), m.getMessageType()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void broadcastContactUpdate(Message message) {
		for (Iterator<Socket> iterator = Server.getClients().keySet().iterator(); iterator.hasNext();) {
			Socket socket = (Socket) iterator.next();
			try {
				if (Server.getClients().get(socket).user.getId() != this.user.getId()) {
					((ServerSocketHandler) Server.getClients().get(socket)).send(new Message(this.user,
							Server.getClients().get(socket).user, message.getData(), MessageType.AvatarUpdate));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void broadcastOnlineUsers() {

		for (Iterator<Socket> iterator = Server.getClients().keySet().iterator(); iterator.hasNext();) {
			Socket socket = (Socket) iterator.next();
			try {
				ArrayList<User> users = new ArrayList<>();
				for (Iterator<ServerSocketHandler> iterator2 = Server.getClients().values().iterator(); iterator2
						.hasNext();) {
					try {
						ServerSocketHandler s = (ServerSocketHandler) iterator2.next();
						try {
							if (Server.getClients().get(socket).user.getId() != s.user.getId())
								users.add(s.user);
						} catch (Exception e) {
							Server.getClients().remove(socket);
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				((ServerSocketHandler) Server.getClients().get(socket)).send(
						new Message(null, Server.getClients().get(socket).user, users, MessageType.ContactsUpdate));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void close() {
		try {
			sInput.close();
			sOutput.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
