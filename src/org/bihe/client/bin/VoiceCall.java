package org.bihe.client.bin;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import org.bihe.client.bin.SoundReciever;
import org.bihe.client.gui.CallWindow;
import org.bihe.client.io.IO;

public class VoiceCall extends Thread {
	private Socket socket;
	private SoundReciever inThread;
	private boolean flag;
	private TargetDataLine microphone;

	public VoiceCall() {
		flag = true;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	@Override
	public void run() {
		// connect to socket server
		// start transferring microphone data
		try {
			System.out.println(IO.getServerAddress());
			this.socket = new Socket(IO.getServerAddress(), 6214);
			ObjectOutputStream oos = new ObjectOutputStream(this.socket.getOutputStream());
			oos.writeObject(IO.getMe().getId());
			flag = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			AudioFormat format = new AudioFormat(16000, 16, 2, true, false);
			DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
			microphone = (TargetDataLine) AudioSystem.getLine(targetInfo);
			microphone.open(format);
			microphone.start();
			byte[] targetData = new byte[IO.BYTE_SIZE];
			System.out.println(targetData.length);
			DataOutputStream dos = new DataOutputStream(this.socket.getOutputStream());
			int bytesRead = 0;
			inThread = new SoundReciever(this.socket, new DataInputStream(this.socket.getInputStream()));
			inThread.start();
			while (bytesRead != -1 && flag == true) {
				bytesRead = microphone.read(targetData, 0, targetData.length);
				if (bytesRead >= 0) {
					byte[] transfereData;
					if(IO.ENCRYPT_CALL)
						transfereData = encryptData(targetData);
					else
						transfereData = targetData;
					dos.write(transfereData, 0, transfereData.length);
				}
			}
			System.out.println("IT IS DONE.");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private byte[] encryptData(byte[] targetData) {
		// encrypt sound data
		byte[][] data;
		ByteArrayOutputStream bos = null;
		try {
			data = Encryption.encrypt(targetData, CallWindow.getUser().getPublicKey());
			bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bos.toByteArray();
	}

	public void stopRunning() {
		//stop streaming data
		setFlag(false);
		if (inThread != null)
			inThread.setFlag(false);
		microphone.close();
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
