package org.bihe.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.bihe.client.io.IO;

public class ItemsPane extends JPanel{
	/**
	 * 
	 */
	// the parent of contacts panel
	private static final long serialVersionUID = -6147409020268344155L;
	public static JScrollPane jp;
	public ItemsPane() {
		this.setPreferredSize(new Dimension(IO.WIDTH-7, 480));
		IO.setContactsPanel(new ContactsPanel());
		IO.setSettingsPanel(new JPanel());
		setLayout(new GridLayout());
		IO.getMainPanel().setBorder(new EmptyBorder(0,0,0,0));
		jp = new JScrollPane(IO.getContactsPanel(),JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jp.setBorder(BorderFactory.createEmptyBorder());
        jp.setBorder(new EmptyBorder(0,0,0,0));
        add(BorderLayout.NORTH, jp);
	}

}

