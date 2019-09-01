package org.bihe.client.gui;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.bihe.client.io.IO;

public class Frame extends JFrame {
	/**
	 * 
	 */
	// a class for all frames
	private static final long serialVersionUID = -480865720624365546L;
	public Frame(String title) {
		setTitle(title);
		doStuff();
	}
	public Frame() {
		doStuff();
	}
	private void doStuff() {
		try {
			// set a icon for all frames
			setIconImage(ImageIO.read(IO.RESOURCES.getResourceAsStream("icon.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
