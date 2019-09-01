package org.bihe.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.SerializationUtils;
import org.bihe.client.io.IO;
import org.bihe.utils.Message;
import org.bihe.utils.MessageType;
import org.bihe.utils.User;

public class ContactsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2115395905019957849L;
	private TreeMap<Integer, User> users = new TreeMap<>();
	private HashSet<Integer> onlineUsers = new HashSet<>();
	private HashSet<Integer> recentChats = new HashSet<>();
	private HashSet<Integer> blockedUsers = new HashSet<>();
	private HashSet<Integer> offlineUsers = new HashSet<>();
	private Color bg = new Color(255, 255, 255);

	public TreeMap<Integer, User> getUsers() {
		return users;
	}

	public HashSet<Integer> getOnlineUsers() {
		return onlineUsers;
	}

	public HashSet<Integer> getRecentChats() {
		return recentChats;
	}

	public HashSet<Integer> getBlockedUsers() {
		return blockedUsers;
	}

	public HashSet<Integer> getOfflineUsers() {
		return offlineUsers;
	}

	ContactsPanel() {
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setAlignmentY(LEFT_ALIGNMENT);
		this.setLayout(new FlowLayout(1, 30, 1));
		paintContacts();
	}

	public void setContactList(ArrayList<User> users) {
		// add all users
		for (User u : users) {
			if (this.users.get(u.getId()) == null) {
				this.users.put(u.getId(), u);
				addToOnlineUsers(u);
			}
		}
		paintContacts();
	}

	public void addToOnlineUsers(User user) {
		//if user was not in recent chats add it to onine users
		users.get(user.getId()).setState(1);
		if (!recentChats.contains(user.getId()))
			onlineUsers.add(user.getId());
		paintContacts();
	}

	public void addToRecentChats(User user) {
		// if user was in online users, remove it
		recentChats.add(user.getId());
		if (onlineUsers.contains(user.getId()))
			onlineUsers.remove(user.getId());
		paintContacts();
	}

	public void addToBlockedUsers(User user) {
		// remove user from online users and if user was in recent chats, leave it
		// update icon
		IO.getWindows().get(user.getId()).getBlockUserButton()
				.setIcon(new ImageIcon(IO.RESOURCES.getResource("unblock-user.png")));
		IO.getWindows().get(user.getId()).getBlockUserButton().repaint();
		users.get(user.getId()).setState(-1);
		if (onlineUsers.contains(user.getId()))
			onlineUsers.remove(user.getId());
		if (!recentChats.contains(user.getId()))
			blockedUsers.add(user.getId());
		paintContacts();
	}

	public void addToOfflineUsers(User user) {
		// if user was in recent chat users, leave it
		// if was in online users, remove it from online users
		// add it to offline users
		if (users.get(user.getId()) != null) {
			users.get(user.getId()).setState(0);
			if (IO.getWindows().get(user.getId()) != null) {
				IO.getWindows().get(user.getId()).getBlockUserButton().setEnabled(false);
				IO.getWindows().get(user.getId()).setControllersEnabled(false);
			}
			if (!recentChats.contains(user.getId()) && !blockedUsers.contains(user.getId()))
				offlineUsers.add(user.getId());
			if (onlineUsers.contains(user.getId()))
				onlineUsers.remove(user.getId());
			paintContacts();
		}
	}

	public void paintContacts() {
		//paint all sets
		this.removeAll();
		int printed = 0;
		if (recentChats.size() > 0) {
			JLabel title = new JLabel("Recent Chats");
			title.setFont(IO.getFont().deriveFont(Font.PLAIN, 15));
			title.setPreferredSize(new Dimension(IO.WIDTH, 20));
			title.setBorder(new EmptyBorder(5, 5, 5, 5));
			title.setForeground(new Color(137, 137, 137));
			this.add(title);
			for (Integer i : recentChats) {
				this.add(userDialog((User) SerializationUtils.clone(users.get(i))));
				printed++;
			}
		}
		if (onlineUsers.size() > 0) {
			JLabel title = new JLabel("Online Users");
			title.setFont(IO.getFont().deriveFont(Font.PLAIN, 15));
			title.setPreferredSize(new Dimension(IO.WIDTH, 20));
			title.setBorder(new EmptyBorder(5, 5, 5, 5));
			title.setForeground(new Color(137, 137, 137));
			this.add(title);
			for (Integer i : onlineUsers) {
				this.add(userDialog(users.get(i)));
				printed++;
			}
		}
		if (blockedUsers.size() > 0) {
			JLabel title = new JLabel("Blocked Users");
			title.setFont(IO.getFont().deriveFont(Font.PLAIN, 15));
			title.setPreferredSize(new Dimension(IO.WIDTH, 20));
			title.setBorder(new EmptyBorder(5, 5, 5, 5));
			title.setForeground(new Color(137, 137, 137));
			this.add(title);
			for (Integer i : blockedUsers) {
				this.add(userDialog(users.get(i)));
				printed++;
			}
		}
		if (offlineUsers.size() > 0) {
			JLabel title = new JLabel("Offline Users");
			title.setFont(IO.getFont().deriveFont(Font.PLAIN, 15));
			title.setPreferredSize(new Dimension(IO.WIDTH, 20));
			title.setBorder(new EmptyBorder(5, 5, 5, 5));
			title.setForeground(new Color(137, 137, 137));
			this.add(title);
			for (Integer i : offlineUsers) {
				this.add(userDialog(users.get(i)));
				printed++;
			}
		}
		if (printed == 0) {
			JLabel label = new JLabel("No online user...");
			label.setFont(IO.getFont().deriveFont(Font.PLAIN, 14));
			label.setForeground(new Color(137, 137, 137));
			this.add(label);
		} else {
			this.setPreferredSize(new Dimension(IO.WIDTH, (printed * 75) + 60));
		}

		this.revalidate();
		if (ItemsPane.jp != null)
			ItemsPane.jp.repaint();

	}

	private JPanel userDialog(User u) {
		//create a user for each user
		JPanel x = new JPanel();
		x.setLayout(new BorderLayout());
		x.setAlignmentX(LEFT_ALIGNMENT);
		x.setAlignmentY(LEFT_ALIGNMENT);
		x.setSize(IO.WIDTH, 70);
		x.setBorder(new EmptyBorder(10, 25, 10, 30));
		JPanel profile = new JPanel(new GridBagLayout());
		profile.setBackground(Color.white);
		profile.setLayout(new BorderLayout());
		profile.add(u.getAvatar(), BorderLayout.WEST);
		JPanel names = new JPanel(new BorderLayout());
		names.setBackground(Color.WHITE);
		JLabel name = new JLabel(u.getName());
		name.setFont(IO.getFontBold().deriveFont(Font.PLAIN, 14));
		name.setPreferredSize(new Dimension(120, 50));
		name.setBorder(new EmptyBorder(0, 0, 10, 0));
		names.add(name, BorderLayout.NORTH);
		JPanel state = new JPanel(new FlowLayout(FlowLayout.LEFT));
		state.add(new RoundedShape(10, u.getstate() == 1 ? Color.green : (u.getstate() == 0 ? Color.red : Color.gray)));
		JLabel stateText = new JLabel(u.stateString());
		stateText.setForeground(new Color(168, 173, 181));
		stateText.setFont(IO.getFont().deriveFont(Font.PLAIN, 10));
		state.add(stateText);
		state.setBorder(new EmptyBorder(-5, -5, -5, -5));
		state.setBackground(Color.white);
		names.add(state, BorderLayout.SOUTH);
		names.setBorder(new EmptyBorder(-5, 5, 10, 0));
		profile.add(names, BorderLayout.EAST);
		x.add(profile, BorderLayout.WEST);
		JPanel buttons = new JPanel(new GridBagLayout());
		buttons.setBackground(bg);
		x.add(buttons, BorderLayout.EAST);
		if (u.getstate() == 1) {
			JButton call = new Icon(IO.RESOURCES.getResource("phone-call.png"));
			call.setToolTipText("Call");
			call.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					u.call();
				}
			});
			buttons.add(call);
		}
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		if (u.getstate() != -1) {
			JButton chat = new Icon(IO.RESOURCES.getResource("chat.png"));
			chat.setToolTipText("Chat");
			chat.addActionListener((ActionEvent arg0) -> {
				if (IO.getWindows().get(u.getId()) == null) {
					ChatWindow cw = new ChatWindow(IO.getMe(), u);
					IO.setLocation(cw);
					IO.getWindows().put(u.getId(), cw);
				} else {
					IO.getWindows().get(u.getId()).setVisible(true);
				}
			});
			buttons.add(chat);
		}
		if (u.getstate() == -1) {
			JButton unblockUser = new Icon(IO.RESOURCES.getResource("unblock-user.png"));
			unblockUser.setToolTipText("Unblock user");
			unblockUser.addActionListener((ActionEvent arg0) -> {
				unblockUser(u);
			});
			unblockUser.setToolTipText("Unblock user");
			buttons.add(unblockUser);
		}
		x.setPreferredSize(new Dimension(IO.WIDTH, 70));
		x.setBackground(bg);
		return x;
	}

	public void unblockUser(User u) {
		// unblock user
		// remove from blocked users
		if (IO.getWindows().get(u.getId()) != null) {
			try {
				IO.getSocketWriter().writeObject(new Message(IO.getMe(), null, u.getId(), MessageType.UnBlockedUser));
				blockedUsers.remove(u.getId());
				IO.getWindows().get(u.getId()).getBlockUserButton()
						.setIcon(new ImageIcon(IO.RESOURCES.getResource("block-user.png")));
				IO.getWindows().get(u.getId()).getBlockUserButton().repaint();
				if (users.get(u.getId()).getstate() == 0) {
					addToOfflineUsers(u);
				} else {
					addToOnlineUsers(u);
				}
				IO.getWindows().get(u.getId()).setControllersEnabled(true);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

}
