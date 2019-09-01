package org.bihe.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons .lang3.StringUtils;
import org.bihe.client.io.IO;
import org.bihe.utils.Message;
import org.bihe.utils.MessageType;

public class StatusBar extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7721961689174131877L;
	private Avatar avatar = new Avatar(IO.getDefaultProfilePicture());

	public StatusBar() {
		IO.setStatus(new JLabel(IO.getMe().getName()));
		IO.getStatus().setFont(IO.getFont().deriveFont(Font.PLAIN, 25));
		Icon settings = new Icon(IO.RESOURCES.getResource("more.png"));
		settings.setToolTipText("Settings");
		settings.setBackground(new Color(239, 239, 239));
		this.add(this.avatar);
		this.add(IO.getStatus());
		IO.getStatus().setPreferredSize(new Dimension(IO.WIDTH - 155, 30));
		this.add(settings);
		settings.addActionListener(settingsClick());
		this.setBorder(new EmptyBorder(20, 20, 10, 20));
	}

	public void updateName() {
		// remove all of status bar and create that with new avatar
		this.removeAll();
		this.avatar = IO.getMe().getAvatar();
		IO.getStatus().setText(IO.getMe().getName());
		Icon settings = new Icon(IO.RESOURCES.getResource("more.png"));
		settings.setToolTipText("Settings");
		settings.setBackground(new Color(239, 239, 239));
		this.add(this.avatar);
		this.add(IO.getStatus());
		IO.getStatus().setPreferredSize(new Dimension(IO.WIDTH - 155, 30));
		settings.addActionListener(settingsClick());
		this.add(settings);
		this.repaint();
		IO.getMainFrame().revalidate();
	}

	private ActionListener settingsClick() {
		return new ActionListener() {
			// open dialog for settings
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String m = "";
				JTextField firstName = new JTextField();
				firstName.setText(IO.getMe().getName());
				firstName.setEnabled(false);
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG file", "jpg", "jpeg"));
				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG file", "png"));
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") +"/Desktop"));
				while (m.trim().length() <= 0 || StringUtils.isAllBlank(m)) {
					JPanel profilePic = new JPanel();
					JButton btn = new JButton("Choose picture");
					profilePic.add(avatar);
					profilePic.add(btn);
					btn.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							int f = fileChooser.showOpenDialog(null);
							if (f == JFileChooser.APPROVE_OPTION) {
								try {
									if (ImageIO.read(fileChooser.getSelectedFile()) != null) {
										profilePic.removeAll();
										avatar = new Avatar(fileChooser.getSelectedFile().getPath());
										profilePic.add(avatar);
										profilePic.add(btn);
										SwingUtilities.getWindowAncestor((Component) arg0.getSource()).pack();

									} else {
										JOptionPane.showMessageDialog(IO.getMainFrame(), "Selected File is not a valid image",
												"Error", JOptionPane.ERROR_MESSAGE);
									}
								} catch (IOException e) {
									JOptionPane.showMessageDialog(IO.getMainFrame(), "Selected File is not a valid image", "Error",
											JOptionPane.ERROR_MESSAGE);
								}

							}
						}
					});
					final JComponent[] inputs = new JComponent[] { new JLabel("Name"), firstName,
							new JLabel("Profile picture"), profilePic };
					int result = JOptionPane.showConfirmDialog(IO.getMainFrame(), inputs, "Please enter informations",
							JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE);
					if (result == JOptionPane.OK_OPTION) {
						IO.getMe().setAvatar(avatar);
						try {
							//send new avatar to socket server
							IO.getSocketWriter()
									.writeObject(new Message(IO.getMe(), null, IO.getMe().getAvatar(), MessageType.AvatarUpdate));
						} catch (IOException e) {
						}
						updateName();
						break;
					} else {
						break;
					}
				}

			}
		};
	}
}
