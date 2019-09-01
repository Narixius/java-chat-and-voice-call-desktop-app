package org.bihe.server.bin;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.bihe.client.io.IO;

public class Server {
	private static ServerSocket server;
	private static ServerSocket voiceServer;
	// maps for save users with their id
	private static Map<Socket, ServerSocketHandler> clients = new HashMap<Socket, ServerSocketHandler>();
	private static Map<Integer, Socket> sockets = new HashMap<Integer, Socket>();
	private static int idCounter = 0;
	// -------------
	// Maps for identify user by socket id and call socket id
	private static Map<Integer, Integer> callRelationchip = new HashMap<Integer, Integer>();
	private static Map<Integer, ServerSocketHandler> voiceSocketsHandlers = new HashMap<Integer, ServerSocketHandler>();
	private static Map<Socket, ServerVoiceCallHandler> voiceHandlers = new HashMap<Socket, ServerVoiceCallHandler>();
	private static Map<Integer, Socket> voiceSockets = new HashMap<Integer, Socket>();

	public static int getIdCounter() {
		idCounter++;
		return idCounter - 1;
	}
	public static ServerSocket getServer() {
		return server;
	}
	public static ServerSocket getVoiceServer() {
		return voiceServer;
	}
	public static Map<Socket, ServerVoiceCallHandler> getVoiceHandlers() {
		return voiceHandlers;
	}
	public static Map<Integer, Integer> getCallRelationchip() {
		return callRelationchip;
	}
	public static Map<Socket, ServerSocketHandler> getClients() {
		return clients;
	}
	public static Map<Integer, Socket> getSockets() {
		return sockets;
	}
	public static Map<Integer, Socket> getVoiceSockets() {
		return voiceSockets;
	}
	public static Map<Integer, ServerSocketHandler> getVoiceSocketsHandlers() {
		return voiceSocketsHandlers;
	}
	public void start() {
		new Thread() {
			@Override
			public void run() {
				initializeSocketServer();
			}
		}.start();
		new Thread() {
			@Override
			public void run() {
				initializeVoiceCallSocketServer();
			}
		}.start();
	}

	private void initializeSocketServer() {
		try {
			server = new ServerSocket(IO.getPort());
			while (true) {
				Socket socket = server.accept();
				System.out.println("New client request received : " + socket);
				System.out.println("Creating a new handler for this client...");
				ServerSocketHandler mtch = new ServerSocketHandler(socket);
				clients.put(socket, mtch);
				mtch.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initializeVoiceCallSocketServer() {
		try {
			voiceServer = new ServerSocket(IO.VOICE_PORT);
			while (true) {
				Socket socket = voiceServer.accept();
				System.out.println("= Voice call request " + socket);
				ServerVoiceCallHandler mtch;
				try {
					mtch = new ServerVoiceCallHandler(socket);
					voiceHandlers.put(socket, mtch);
					mtch.start();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
