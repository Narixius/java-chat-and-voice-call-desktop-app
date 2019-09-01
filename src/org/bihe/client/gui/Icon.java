package org.bihe.client.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public class Icon extends JButton {

	/**
	 * 
	 */
	// an Icon generator
	private static final long serialVersionUID = -2773218798363993593L;
	private ImageIcon icon;
	public Icon(String pathToIcon) {
		icon = new ImageIcon(pathToIcon);
		process();
	}
	public Icon(URL pathToIcon) {
		icon = new ImageIcon(pathToIcon);
		process();
	}
	private void process() {
		this.setIcon(icon);
		this.setFocusable(false);
		this.setBackground(Color.white);
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}
}
