package org.bihe.client.io;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.bihe.client.bin.Encryption;
import org.bihe.client.gui.ChatWindow;
import org.bihe.client.gui.ContactsPanel;
import org.bihe.client.gui.ItemsPane;
import org.bihe.client.gui.MainFrame;
import org.bihe.client.gui.StatusBar;
import org.bihe.resources.Resources;
import org.bihe.utils.User;

public class IO {
	public static final Class<? extends Resources> RESOURCES = new Resources().getClass();
	private static MainFrame mainFrame;
	private static JPanel mainPanel;
	private static StatusBar statusPanel;
	private static ItemsPane itemsPanel;
	private static JLabel status;
	public static final int WIDTH = 320;
	public static final int HEIGHT = 600;
	private static ContactsPanel contactsPanel;
	private static JPanel settingsPanel;
	private static String serverAddress = "";
	public static int port = 8080;
	public static final int VOICE_PORT = 6214;
	private static Socket socket;
	private static String name = "Nariman";
	private static User me = new User("something", Encryption.getPublicKey());
	private static ObjectOutputStream socketWriter;
	private static HashMap<Integer, ChatWindow> windows = new HashMap<>();
	private static Font fontBold;
	private static Font font;
	public static final int BYTE_SIZE = 64;
	public static final int ENCRYPTED_BYTE_SIZE = 285;
	public static final boolean ENCRYPT_CALL = false;
	private static InputStream defaultProfilePicture = RESOURCES.getResourceAsStream("user.jpg");
	static {
		try {
			fontBold = Font.createFont(Font.PLAIN, new BufferedInputStream(
					new FileInputStream(RESOURCES.getResource("Fonts/NotoSans-Bold.ttf").getPath())));
		} catch (FontFormatException | IOException e) {
			fontBold = new JLabel().getFont();
		}
		try {
			font = Font.createFont(Font.PLAIN, new BufferedInputStream(
					new FileInputStream(RESOURCES.getResource("Fonts/NotoSans-Regular.ttf").getPath())));
		} catch (FontFormatException | IOException e) {
			font = new JLabel().getFont();
		}
	}

	public static int getPort() {
		return port;
	}

	public static void setName(String name) {
		IO.name = name;
	}

	public static void setPort(int port) {
		IO.port = port;
	}

	public static void setContactsPanel(ContactsPanel contactsPanel) {
		IO.contactsPanel = contactsPanel;
	}

	public static void setSettingsPanel(JPanel settingsPanel) {
		IO.settingsPanel = settingsPanel;
	}

	public static void setMainFrame(MainFrame mainFrame) {
		IO.mainFrame = mainFrame;
	}

	public static void setMainPanel(JPanel mainPanel) {
		IO.mainPanel = mainPanel;
	}

	public static void setStatusPanel(StatusBar statusPanel) {
		IO.statusPanel = statusPanel;
	}

	public static void setItemsPanel(ItemsPane itemsPanel) {
		IO.itemsPanel = itemsPanel;
	}

	public static void setSocketWriter(ObjectOutputStream socketWriter) {
		IO.socketWriter = socketWriter;
	}

	public static void setSocket(Socket socket) {
		IO.socket = socket;
	}

	public static void setServerAddress(String serverAddress) {
		IO.serverAddress = serverAddress;
	}

	public static void setLocation(JFrame f) {
		int x = 0;
		int y = 0;
		boolean hided = false;
		if (f.isVisible()) {
			f.setVisible(false);
			hided = true;
		}

		x = (int) IO.mainFrame.getLocation().getX() + 20;
		y = (int) IO.mainFrame.getLocation().getY() + 20;
		for (ChatWindow cw : IO.windows.values()) {
			Point l = cw.getLocation();
			if (x == l.getX() || y == l.getY()) {
				if ((x + 30) > Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 20)
					x = (int) l.getX() - 30;
				else
					x += 30;
				if ((x + 30) > Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 20)
					y = (int) l.getY() - 30;
				else
					y += 30;
			}
		}
		f.setLocation(x, y);
		if (hided)
			f.setVisible(true);
	}

	public static ContactsPanel getContactsPanel() {
		return contactsPanel;
	}

	public static InputStream getDefaultProfilePicture() {
		return defaultProfilePicture;
	}

	public static Font getFont() {
		return font;
	}

	public static Font getFontBold() {
		return fontBold;
	}

	public static ItemsPane getItemsPanel() {
		return itemsPanel;
	}

	public static MainFrame getMainFrame() {
		return mainFrame;
	}

	public static JPanel getMainPanel() {
		return mainPanel;
	}

	public static User getMe() {
		return me;
	}

	public static String getName() {
		return name;
	}

	public static String getServerAddress() {
		return serverAddress;
	}

	public static JPanel getSettingsPanel() {
		return settingsPanel;
	}
	public static void setStatus(JLabel status) {
		IO.status = status;
	}
	public static Socket getSocket() {
		return socket;
	}

	public static ObjectOutputStream getSocketWriter() {
		return socketWriter;
	}

	public static JLabel getStatus() {
		return status;
	}

	public static StatusBar getStatusPanel() {
		return statusPanel;
	}

	public static HashMap<Integer, ChatWindow> getWindows() {
		return windows;
	}

	public static void setMe(User me) {
		IO.me = me;
	}
}
