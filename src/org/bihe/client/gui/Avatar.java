package org.bihe.client.gui;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;

public class Avatar extends JLabel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6654125396318928758L;

	public Avatar(String path) {
		try {
			BufferedImage master = ImageIO.read(new File(path));
			processImage(master);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Avatar(URL path) {
		try {
			BufferedImage master = ImageIO.read(path);
			processImage(master);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Avatar(InputStream path) {
		try {
			BufferedImage master = ImageIO.read(path);
			processImage(master);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void applyQualityRenderingHints(Graphics2D g2d) {
		// render graphic with a high quality
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

	}

	private void processImage(BufferedImage master) {
		// round image and crop it from center
		int diameter = 48;
		int min = (master.getHeight() > master.getWidth()) ? master.getWidth() : master.getHeight();
		master = Scalr.crop(master, (master.getWidth() - min) / 2, (master.getHeight() - min) / 2,
				(min + ((master.getWidth() - min) / 2)), (min + ((master.getHeight() - min) / 2)));

		if (master.getHeight() < master.getWidth())
			master = Scalr.resize(master, Mode.FIT_TO_HEIGHT, diameter, diameter);
		else
			master = Scalr.resize(master, Mode.FIT_TO_WIDTH, diameter, diameter);
		BufferedImage mask = new BufferedImage(master.getWidth(), master.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = mask.createGraphics();
		applyQualityRenderingHints(g2d);
		g2d.fillOval(0, 0, diameter - 1, diameter - 1);
		g2d.dispose();
		BufferedImage masked = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
		g2d = masked.createGraphics();
		applyQualityRenderingHints(g2d);
		g2d.drawImage(master, 0, 0, null);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
		g2d.drawImage(mask, 0, 0, null);
		g2d.dispose();
		setIcon(new ImageIcon(masked));

	}
}
