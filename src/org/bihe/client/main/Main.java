package org.bihe.client.main;

import java.awt.EventQueue;


import org.bihe.client.gui.MainFrame;

public class Main {

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				new MainFrame();
			}
		});
	}
	

}
