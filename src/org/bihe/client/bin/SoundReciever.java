package org.bihe.client.bin;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import org.bihe.client.io.IO;

public class SoundReciever extends Thread {
	private DataInputStream soundIn = null;
	private SourceDataLine inSpeaker = null;
	private boolean flag;

	public SoundReciever(Socket conn, DataInputStream is) throws Exception {
		flag = true;
		soundIn = is;
		// generate audio format
		AudioFormat af = new AudioFormat(16000, 16, 2, true, false);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
		inSpeaker = (SourceDataLine) AudioSystem.getLine(info);
		inSpeaker.open(af);
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
		if (!flag) {
			// stop speaker
			inSpeaker.stop();
			inSpeaker.close();
		}
	}

	@Override
	public void run() {
		//read bytes and transfere that to partner
		//partner id is in callRelation map
		int bytesRead = 0;
		byte[] inSound;
		if (IO.ENCRYPT_CALL)
			inSound = new byte[IO.ENCRYPTED_BYTE_SIZE];
		else
			inSound = new byte[IO.BYTE_SIZE];
		inSpeaker.start();
		while (bytesRead != -1 && flag) {
			try {
				bytesRead = soundIn.read(inSound, 0, inSound.length);
				byte[] data;
				if (IO.ENCRYPT_CALL) {
					ByteArrayInputStream bos = new ByteArrayInputStream(inSound);
					ObjectInput out = new ObjectInputStream(bos);
					byte[][] encryptedData = (byte[][]) out.readObject();
					data = (byte[]) Encryption.decrypt(encryptedData);
				}else {
					data = inSound;
				}

				if (bytesRead >= 0) {
					inSpeaker.write(data, 0, data.length);
				}
			} catch (Exception e) {
				setFlag(false);
				e.printStackTrace();
			}
		}
	}

}
