package org.bihe.server.bin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

import org.bihe.client.io.IO;
import org.bihe.utils.Message;
import org.bihe.utils.MessageType;

public class ServerVoiceCallHandler extends Thread {
	private Socket connection;
	private DataInputStream dataIn;
	private DataOutputStream dataOut;
	private int socketID;
	private boolean connectionIsAlive = true;
	public ServerVoiceCallHandler(Socket con) throws Exception {
		connectionIsAlive = true;
		connection = con;
		ObjectInputStream ois = new ObjectInputStream(con.getInputStream());
		Object x = ois.readObject();
		System.out.println();
		socketID = (int) x;
		Server.getVoiceSockets().put(socketID,con);
		dataIn = new DataInputStream(con.getInputStream());
		dataOut = new DataOutputStream(con.getOutputStream());
	}

	public void run() {
		int bytesRead = 0;
		byte[] inBytes;
		if(IO.ENCRYPT_CALL)
			inBytes = new byte[IO.ENCRYPTED_BYTE_SIZE];
		else
			inBytes = new byte[IO.BYTE_SIZE];
		while (bytesRead != -1 && connectionIsAlive) {
			try {
				bytesRead = dataIn.read(inBytes, 0, inBytes.length);
			} catch (Exception e) {
				e.printStackTrace();
				try {
					dataIn.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				endCall(this);
				break;
			}
			if (bytesRead >= 0) {
				sendToAll(inBytes, bytesRead);
			}
		}
	}

	private void endCall(ServerVoiceCallHandler socket) {
		System.out.println(Server.getCallRelationchip().keySet());
		System.out.println(Server.getCallRelationchip().values());
		int partner = Server.getCallRelationchip().get(socket.socketID);
		System.out.println("socked ID : " + socket.socketID + "    partner : " + partner);
		try {
			Server.getClients().get(Server.getSockets().get(partner)).send(new Message(null, null, null, MessageType.VoiceCallEnded));
			if(partner == socketID)
				partner = Server.getCallRelationchip().get(partner);
			
			socket.connectionIsAlive = false;
			connectionIsAlive = false;
			Server.getCallRelationchip().remove(socketID);
			Server.getCallRelationchip().remove(partner);
			
			Server.getVoiceHandlers().remove(connection);
			Server.getVoiceHandlers().remove(Server.getVoiceSockets().get(partner));
			
			Server.getVoiceSockets().remove(socketID);
			Server.getVoiceSockets().remove(partner);
			
			Server.getVoiceSocketsHandlers().remove(socketID);
			Server.getVoiceSocketsHandlers().remove(partner);
			
			System.out.println(Server.getCallRelationchip().size());
			System.out.println(Server.getVoiceHandlers().size());
			System.out.println(Server.getVoiceSockets().size());
			System.out.println(Server.getVoiceSocketsHandlers().size());
			
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void sendToAll(byte[] byteArray, int q) {
		ArrayList<ServerVoiceCallHandler> sockets = new ArrayList<>(Server.getVoiceHandlers().values());
		for (ServerVoiceCallHandler s : sockets) {
			if (!s.equals(this)) {
				try {
					s.dataOut.write(byteArray, 0, q);
				} catch (Exception e) {
					e.printStackTrace();
					endCall(s);
				}
			}
		}
	}
}
