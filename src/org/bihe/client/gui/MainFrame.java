package org.bihe.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;
import org.bihe.client.bin.ClientSocketHandler;
import org.bihe.client.bin.Encryption;
import org.bihe.client.io.IO;
import org.bihe.resources.Resources;
import org.bihe.utils.Message;
import org.bihe.utils.MessageType;
import org.bihe.utils.User;

public class MainFrame extends Frame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9119534887535708967L;
	public static JFrame x;
	public static String name = "Nariman";
	private static File profilePicture;
	private static Avatar avatar;

	public MainFrame() {
		IO.setMainFrame(this);
		intializeFrame();
	}

	private void intializeFrame() {
		// check server
		connectoToServer();
		// initialize frame
		IO.getMainFrame().setTitle("Voice Chat Client");
		IO.getMainFrame().setSize(IO.WIDTH, IO.HEIGHT);
		IO.getMainFrame().setResizable(false);
		IO.getMainFrame().setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - IO.WIDTH) / 2,
				(Toolkit.getDefaultToolkit().getScreenSize().height - IO.HEIGHT) / 2);
		IO.setMainPanel(new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)));
		IO.getMainFrame().add(IO.getMainPanel());
		IO.setStatusPanel(new StatusBar());
		IO.setItemsPanel(new ItemsPane());
		IO.getMainPanel().add(IO.getStatusPanel());
		IO.getMainPanel().add(IO.getItemsPanel());
		IO.getMainPanel().setBackground(new Color(239, 239, 239));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(false);
	}

	public static void connectoToServer() {
		//crate a connecting frame until sockets connected to server
		x = new Frame("Connectiong...");
		x.setSize(250, 150);
		JPanel z = new JPanel();
		x.add(z);
		JLabel text = new JLabel("Connection to server...", SwingConstants.CENTER);
		text.setPreferredSize(new Dimension((int) x.getSize().getWidth(), 50));
		z.add(text);
		JButton close = new JButton("Cancel");
		z.add(close);
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		x.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		x.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - 250) / 2,
				(Toolkit.getDefaultToolkit().getScreenSize().height - 150) / 2);
		x.setVisible(true);
		new Thread() {
			public void run() {
				try {
					Socket socket = new Socket(IO.getServerAddress(), IO.getPort());
					IO.setSocket(socket);
					ObjectInputStream sInput = null;
					inputField();
					IO.getMainFrame().setTitle("Telephant");
					sInput = new ObjectInputStream(socket.getInputStream());
					IO.setSocketWriter(new ObjectOutputStream(socket.getOutputStream()));
					IO.getStatusPanel().updateName();
					IO.getSocketWriter().writeObject(new Message(IO.getMe(), null, IO.getName(), MessageType.Greeting));
					new ClientSocketHandler(sInput).start();
					IO.getMainFrame().addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosed(WindowEvent arg0) {
							try {
								socket.close();
							} catch (IOException e) {}
						}
					});
				} catch (Exception e) {
					serverNotResponding();
				}
			};
		}.start();

	}
	private static void serverNotResponding() {
		// default server is not responding
		// get a new server address and port from user
		String res = JOptionPane.showInputDialog(x,
				"Server is not responding.\nPlease enter the server address if you have\nPattern: IP:PORT",
				"Server is down", JOptionPane.ERROR_MESSAGE);
		if (res != null) {
			String[] data = res.split(":");
			if (data.length == 2) {
				IO.setServerAddress(data[0]);
				try {
					IO.setPort(Integer.parseInt(data[1]));
					x.dispose();
					connectoToServer();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(x, "Invalid port","Error",JOptionPane.ERROR_MESSAGE);
					serverNotResponding();
				}
			} else {
				JOptionPane.showMessageDialog(x, "The address is invalid","Invalid Arguments",JOptionPane.ERROR_MESSAGE);
				serverNotResponding();
			}
		} else {
			System.exit(0);
		}
	}
	private static void inputField() {
		// create a JOptionPane to get user's name and avatar 
		String m = "";
		JTextField firstName = new JTextField();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG file", "jpg", "jpeg"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG file", "png"));
		profilePicture = new File(IO.RESOURCES.getResource("user.jpg").getPath());
		avatar = new Avatar(new Resources().getClass().getResource("user.jpg"));
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") +"/Desktop"));
		while (m.trim().length() <= 0 || StringUtils.isAllBlank(m)) {
			firstName.setText("");
			firstName.setFont(IO.getFont().deriveFont(Font.PLAIN, 14));
			JPanel profilePic = new JPanel();
			JButton btn = new JButton("Choose picture");
			profilePic.add(avatar);
			profilePic.add(btn);
			btn.addActionListener((ActionEvent arg0) -> {
				int f = fileChooser.showOpenDialog(null);
				if (f == JFileChooser.APPROVE_OPTION) {
					profilePicture = fileChooser.getSelectedFile();
					try {
						if (ImageIO.read(profilePicture) != null) {
							profilePic.removeAll();
							avatar = new Avatar(profilePicture.getPath());
							profilePic.add(avatar);
							profilePic.add(btn);
							SwingUtilities.getWindowAncestor((Component) arg0.getSource()).pack();
						} else {
							profilePicture = new File(IO.RESOURCES.getResource("user.jpg").getPath());
							JOptionPane.showMessageDialog(x, "Selected File is not a valid image", "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					} catch (IOException e) {
						JOptionPane.showMessageDialog(x, "Selected File is not a valid image", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			final JComponent[] inputs = new JComponent[] { new JLabel("Name"), firstName, new JLabel("Profile picture"),
					profilePic };
			int result = JOptionPane.showConfirmDialog(x, inputs, "Please enter informations",
					JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE);
			if (result != JOptionPane.OK_OPTION) {
				System.exit(0);
			} else {
				m = firstName.getText();
			}
		}
		IO.setName(firstName.getText());
		IO.setMe(new User(IO.getName(), Encryption.getPublicKey()));
		IO.getMe().setAvatar(avatar);
	}
}
