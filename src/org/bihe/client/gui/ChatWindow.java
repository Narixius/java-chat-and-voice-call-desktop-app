package org.bihe.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.apache.commons.lang3.SerializationUtils;
import org.bihe.client.io.IO;
import org.bihe.utils.Message;
import org.bihe.utils.MessageType;
import org.bihe.utils.User;

public class ChatWindow extends Frame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6288360306114864692L;
	private User sender;
	private User reciever;
	private ArrayList<Message> messages = new ArrayList<>();
	private JPanel chats = new JPanel();
	private ChatWindow c = this;
	private JScrollPane jp = new JScrollPane();
	private JTextArea textArea = new JTextArea();
	private JPanel header;
	private JPanel basePanel;
	private Avatar avatar;
	private JPanel profile;
	private JPanel names;
	private int height = 0;
	private JPanel state;
	private Icon file;
	private Icon send;
	private JButton call;
	private boolean userBlockedMe = false;
	private JButton blockUser;
	public boolean getUserBlockedMe() {
		return userBlockedMe;
	}
	public JButton getBlockUserButton() {
		return blockUser;
	}
	public void setUserBlockedMe(boolean userBlockedMe) {
		this.userBlockedMe = userBlockedMe;
	}
	private boolean refreshed = false;

	public ChatWindow(User sender, User reciever) {
		this.sender = SerializationUtils.clone(sender);
		this.reciever = SerializationUtils.clone(reciever);
		this.setSize(new Dimension(IO.WIDTH, IO.HEIGHT));
		this.setResizable(false);
		this.setVisible(true);
		this.setTitle("Chat with " + reciever.getName());
		avatar = reciever.getAvatar();
		header = header();
		initializePanel();
	}

	public ChatWindow(User sender, User reciever, boolean visibleWindow) {
		this.sender = SerializationUtils.clone(sender);
		this.reciever = SerializationUtils.clone(reciever);
		this.setSize(new Dimension(IO.WIDTH, IO.HEIGHT));
		this.setResizable(false);
		this.setVisible(visibleWindow);
		try {
			this.setIconImage(ImageIO.read(IO.RESOURCES.getResourceAsStream("icon.png")));
		} catch (IOException e1) {
		}
		this.setTitle("Chat with " + reciever.getName());
		avatar = reciever.getAvatar();
		header = header();
		initializePanel();
	}

	private void initializePanel() {
		chats.setLayout(new FlowLayout(0, 0, 0));
		basePanel = new JPanel();
		basePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		basePanel.setBackground(Color.WHITE);
		this.add(basePanel);
		basePanel.add(header);
		jp = new JScrollPane(chats, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jp.setPreferredSize(new Dimension(310, 420));
		jp.setBorder(new EmptyBorder(6, 6, 6, 6));
		basePanel.add(jp);
		textArea = new JTextArea();
		textArea.setFont(IO.getFont().deriveFont(Font.PLAIN, 14));
		basePanel.add(footer());
	}

	public void updateHeader(Avatar avatar) {
		//update user's new avatar
		avatar = SerializationUtils.clone(avatar);
		profile.remove(this.avatar);
		this.avatar = avatar;
		profile.add(this.avatar, BorderLayout.WEST);
		profile.revalidate();
		profile.repaint();

	}

	public void updateStatus(int status) {
		// update new status of user
		reciever.setState(status);
		names.remove(state);
		state = statePanel(reciever);
		names.add(state, BorderLayout.SOUTH);
		names.revalidate();
		names.repaint();
	}

	private JPanel footer() {
		JPanel footer = new JPanel();
		footer.setBackground(Color.white);
		file = new Icon(IO.RESOURCES.getResource("clip.png"));
		file.setToolTipText("Send file");
		file.setBackground(Color.WHITE);
		file.addActionListener((arg0) -> {
			JFileChooser fc = new JFileChooser();
			int res = fc.showOpenDialog(this);
			if (res == JFileChooser.APPROVE_OPTION) {
				sendFile(fc.getSelectedFile());
			}
		});
		footer.add(file);
		InputMap input = textArea.getInputMap();
		KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
		KeyStroke shiftEnter = KeyStroke.getKeyStroke("shift ENTER");
		input.put(shiftEnter, "insert-break"); // input.get(enter)) = "insert-break"
		input.put(enter, "text-submit");

		ActionMap actions = textArea.getActionMap();
		actions.put("text-submit", new AbstractAction() {
			private static final long serialVersionUID = 9009712373260803098L;

			@Override
			public void actionPerformed(ActionEvent e) {
				sendMessage();
			}
		});
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
		scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
		scrollPane.setBackground(Color.white);
		JPanel textAreaPanel = new JPanel();
		textAreaPanel.setLayout(new BorderLayout());
		textAreaPanel.add(scrollPane, BorderLayout.CENTER);
		textAreaPanel.setBackground(Color.RED);
		textAreaPanel.setPreferredSize(new Dimension(180, 50));
		scrollPane.setBorder(new EmptyBorder(3, 3, 3, 3));
		JPanel message = new JPanel();
		message.add(textAreaPanel);
		send = new Icon(IO.RESOURCES.getResource("send.png"));
		send.setToolTipText("Send");
		send.setBackground(Color.WHITE);
		send.setPreferredSize(new Dimension(50, 50));
		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				sendMessage();
			}
		});
		message.setLayout(new FlowLayout(0, 0, 0));
		message.add(send);
		message.setBorder(LineBorder.createBlackLineBorder());
		message.setBackground(Color.WHITE);
		footer.add(message);
		return footer;
	}

	private JPanel header() {
		JPanel x = new JPanel();
		x.setLayout(new BorderLayout());
		x.setAlignmentX(LEFT_ALIGNMENT);
		x.setAlignmentY(LEFT_ALIGNMENT);
		x.setSize(IO.WIDTH, 70);
		x.setBorder(new EmptyBorder(10, 25, 10, 30));
		profile = new JPanel(new BorderLayout());
		profile.setBackground(Color.white);
		profile.setLayout(new BorderLayout());
		profile.add(this.avatar, BorderLayout.WEST);
		names = new JPanel(new BorderLayout());
		names.setBackground(Color.WHITE);
		JLabel name = new JLabel(reciever.getName());
		name.setFont(IO.getFontBold().deriveFont(Font.PLAIN, 17));
		name.setPreferredSize(new Dimension(120, 45));
		names.add(name, BorderLayout.NORTH);
		state = statePanel(reciever);
		names.add(state, BorderLayout.SOUTH);
		names.setBorder(new EmptyBorder(-5, 5, 10, 0));
		profile.add(names, BorderLayout.EAST);
		x.add(profile, BorderLayout.WEST);
		JPanel buttons = new JPanel(new GridBagLayout());
		buttons.setBackground(Color.WHITE);
		x.add(buttons, BorderLayout.EAST);
		call = new Icon(IO.RESOURCES.getResource("phone-call.png"));
		call.setToolTipText("Call");
		blockUser = new Icon(IO.RESOURCES.getResource("block-user.png"));
		blockUser.setToolTipText("Block user");
		blockUser.addActionListener((ActionEvent arg0) -> {
			if (IO.getContactsPanel().getUsers().get(reciever.getId()).getstate() != -1)
				blockUser(reciever);
			else
				unBlockUser(reciever);
		});
		call.addActionListener((ActionEvent arg0) -> {
			reciever.call();
		});
		buttons.add(call);
		buttons.add(blockUser);
		x.setPreferredSize(new Dimension(IO.WIDTH, 70));
		x.setBackground(Color.WHITE);
		return x;
	}

	private JPanel statePanel(User u) {
		//generate the status of user gui properties
		JPanel state = new JPanel(new FlowLayout(FlowLayout.LEFT));
		state.add(new RoundedShape(10, u.getstate() == 1 ? Color.green : (u.getstate() == 0 ? Color.red : Color.gray)));
		JLabel stateText = new JLabel(u.stateString());
		stateText.setForeground(new Color(168, 173, 181));
		stateText.setFont(IO.getFont().deriveFont(Font.PLAIN, 10));
		state.add(stateText);
		state.setBorder(new EmptyBorder(-5, -5, -5, -5));
		state.setBackground(Color.white);
		return state;
	}

	private void blockUser(User user) {
		//block user
		try {
			IO.getSocketWriter().writeObject(new Message(IO.getMe(), null, user.getId(), MessageType.BlockedUser));
			IO.getContactsPanel().addToBlockedUsers(SerializationUtils.clone(user));
			setControllersEnabled(false);
			blockUser.setIcon(new ImageIcon(IO.RESOURCES.getResource("unblock-user.png")));
			blockUser.repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void unBlockUser(User user) {
		//unblock user
		IO.getContactsPanel().unblockUser(user);
		blockUser.setIcon(new ImageIcon(IO.RESOURCES.getResource("block-user.png")));
		blockUser.repaint();
	}

	public void setControllersEnabled(boolean bool) {
		// disable or enable buttons like send or call
		// it will call when user goes online or in block list or user get's unblocked
		if (IO.getContactsPanel().getUsers().get(reciever.getId()).getstate() != 0) {
			updateStatus(IO.getContactsPanel().getUsers().get(reciever.getId()).getstate());
			if (userBlockedMe || IO.getContactsPanel().getUsers().get(reciever.getId()).getstate() == -1)
				bool = false;
			System.out.println(bool);
			file.setEnabled(bool);
			send.setEnabled(bool);
			call.setEnabled(bool);
			textArea.setEnabled(bool);
			if (bool)
				textArea.setText("");
			else {
				if (!userBlockedMe)
					textArea.setText("You blocked this user.\nUnblock this user to chat.");
				else
					textArea.setText("This user blocked you.");
			}
		} else {
			bool = false;
			file.setEnabled(bool);
			send.setEnabled(bool);
			call.setEnabled(bool);
			textArea.setEnabled(bool);
			textArea.setText("User is offline");
		}
	}

	private void sendMessage() {
		//send message to user
		String text = textArea.getText().trim();
		final Pattern pattern = Pattern.compile("^ *$");
		if (text.trim().length() > 0 && pattern.matcher(text.trim()).find() == false) {
			textArea.setText("");
			Message message = new Message(sender, reciever, text.trim(), MessageType.ChatMessage);
			try {
				IO.getSocketWriter().writeObject(message);
				addMessage(text);
				textArea.requestFocus(true);
				IO.getContactsPanel().addToRecentChats(reciever);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Oops");
			textArea.setText("");
		}
	}

	private void sendFile(File file) {
		//send gile to user
		if (file.length() / 1024 <= 5 * 1024) {
			Object[] data = new Object[2];
			data[0] = file;

			byte[] bytes = null;
			try {
				bytes = Files.readAllBytes(file.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			data[1] = bytes;
			Message message = new Message(sender, reciever, data, MessageType.ChatFile);
			try {
				IO.getSocketWriter().writeObject(message);
				addFileMessage(file);
				textArea.requestFocus(true);
				IO.getContactsPanel().addToRecentChats(reciever);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(this, "The file size must be lower than 5MB", "File is too large",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	private void addMessage(String message) {
		// this user send a message
		messages.add(new Message(sender, reciever, message, MessageType.ChatMessage, false));
		repaintFrame();
	}

	public void addMessage(Message message) {
		// message from partner
		messages.add(message);
		repaintFrame();
	}

	private void addFileMessage(File file) {
		// this user sent a file
		messages.add(new Message(sender, reciever, file, MessageType.ChatMessage, false));
		repaintFrame();
	}

	public void addFileMessage(Message message) {
		// partner user send a file to this user
		messages.add(message);
		repaintFrame();
	}

	private void repaintFrame() {
		// add the last message received or added by the users
		// add them by their types, file or message
		if (IO.getWindows().get(reciever.getId()) == null) {
			IO.getWindows().put(reciever.getId(), c);
		}
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		if (messages.get(messages.size() - 1).getData() instanceof String) {
			String text = ((String) messages.get(messages.size() - 1).getData());
			if (messages.get(messages.size() - 1).getSender().getId() == IO.getMe().getId()) {
				JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
				ChatBubble.RightArrowBubble r = new ChatBubble.RightArrowBubble(text);
				right.setPreferredSize(new Dimension(300, (int) r.getPreferredSize().getHeight() + 5));
				right.add(r);
				if (chats.getPreferredSize().getHeight() > 420)
					right.setBorder(new EmptyBorder(0, 0, 0, 20));
				chats.add(right, gbc);
				height += right.getPreferredSize().height;
			} else {
				JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
				ChatBubble.LeftArrowBubble l = new ChatBubble.LeftArrowBubble(text);
				left.setPreferredSize(new Dimension(300, (int) l.getPreferredSize().getHeight() + 5));
				left.add(l);
				chats.add(left, gbc);
				height += left.getPreferredSize().height;
			}
		} else {

			if (messages.get(messages.size() - 1).getSender().getId() == IO.getMe().getId()) {
				File file = ((File) messages.get(messages.size() - 1).getData());
				JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
				ChatBubble.RightArrowBubble r = new ChatBubble.RightArrowBubble(file);
				right.setPreferredSize(new Dimension(300, (int) r.getPreferredSize().getHeight() + 5));
				right.add(r);
				chats.add(right, gbc);
				height += right.getPreferredSize().height;
			} else {
				Object[] data = ((Object[]) messages.get(messages.size() - 1).getData());
				JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
				ChatBubble.LeftArrowBubble l = new ChatBubble.LeftArrowBubble(data);
				left.setPreferredSize(new Dimension(300, (int) l.getPreferredSize().getHeight() + 5));
				left.add(l);
				chats.add(left, gbc);
				height += left.getPreferredSize().height;
			}
		}
		chats.setPreferredSize(new Dimension(300, height + 5));
		if (chats.getPreferredSize().getHeight() > 420 && refreshed == false) {
			for (Component c : chats.getComponents()) {
				if (((JPanel) c).getComponent(0) instanceof ChatBubble.RightArrowBubble)
					((JPanel) c).setBorder(new EmptyBorder(0, 0, 0, 20));
			}
		}
		this.revalidate();
		jp.getVerticalScrollBar().setValue(jp.getVerticalScrollBar().getMaximum());

	}

}
