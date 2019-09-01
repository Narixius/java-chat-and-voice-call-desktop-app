package org.bihe.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;

public class RoundedShape extends JLabel {
	/**
	 * 
	 */
	// a class for create a rounded JLabel for status of users
	private static final long serialVersionUID = 2989726232781756104L;
	private Color backgroundColor;
	private int cornerRadius = 15;
	public RoundedShape(int radius, Color bgColor) {
        super();
        cornerRadius = radius;
        backgroundColor = bgColor;
        this.setPreferredSize(new Dimension(10,10));
    }

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Dimension arcs = new Dimension(cornerRadius, cornerRadius);
		int width = 10;
		int height = 10;
		Graphics2D graphics = (Graphics2D) g;
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (backgroundColor != null) {
			graphics.setColor(backgroundColor);
		} else {
			graphics.setColor(getBackground());
		}
		graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height); // paint background
		graphics.setColor(getForeground());
	}
}
