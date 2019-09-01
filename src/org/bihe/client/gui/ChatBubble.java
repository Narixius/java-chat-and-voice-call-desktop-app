package org.bihe.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.DecimalFormat;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.bihe.client.io.IO;

public class ChatBubble {
	private static String calculateSize(File f) {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		if (f.length() / 1024 > 1024) {
			return df.format(((float) ((float) f.length() / (float) 1024) / (float) 1024)) + "MB";
		} else {
			return df.format(((float) ((float) f.length() / (float) 1024))) + "KB";
		}
	}

	private static String minifyName(String name) {
		if (name.length() > 30) {
			return name.substring(0, 10) + " ... " + name.substring(name.length() - 5, name.length());
		}
		return name;
	}

	private static void openFile(byte[] b, File f) {
		JFileChooser fc = new JFileChooser();
		fc.setSelectedFile(new File(f.getName()));
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
				OutputStream os = new FileOutputStream(file);
				os.write(b);
				os.close();
				try {
					// open file with explorer
					Process proc = Runtime.getRuntime().exec("explorer.exe /select, " + file.getPath());
					proc.waitFor();
				} catch (Exception e) {
					try {
						// open with desktop property
						Desktop.getDesktop().open(file.getParentFile());
					} catch (Exception ex) {
					}
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Error in saving file", "Problem", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private static MouseAdapter mouseAdapter(byte[] b, File f) {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				openFile(b, f);
				super.mouseClicked(arg0);
			}
		};
	}

	private static MouseAdapter mouseAdapterForSender(byte[] b, File f) {
		//if the senders sent the file, must use this mouse adapter
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (f.exists()) {
					try {
						//if file exists, open it with explorer
						Process proc = Runtime.getRuntime().exec("explorer.exe /select, " + f.getPath());
						proc.waitFor();
					} catch (Exception e) {
						try {
							//or with desktop property
							Desktop.getDesktop().open(f.getParentFile());
						} catch (Exception ex) {
						}
					}
				} else {
					// so create file with it's byte
					openFile(b, f);
				}
				super.mouseClicked(arg0);
			}
		};
	}

	private static JTextArea generateTextBubble(String data, Color c) {
		JTextArea label = new JTextArea(data);
		label.setBorder(new EmptyBorder(5, 5, 5, 5));
		label.setForeground(Color.white);
		label.setBackground(c);
		label.setEditable(false);
		JTextArea tmp = new JTextArea(label.getText());
		tmp.setFont(IO.getFont().deriveFont(Font.PLAIN, 14));
		label.setSize((tmp.getPreferredSize().width + 10 <= 260 ? (tmp.getPreferredSize().width + 10) : 260), 20);
		label.setLineWrap(true);
		return label;
	}

	private static JPanel generateFileBubble(Object response, Color c, boolean isSender) {
		byte[] bytes = null;
		MouseAdapter mouseAdapter;
		File f;
		if (isSender) {
			// if is sender, generate bytes from file
			f = (File) response;
			try {
				bytes = Files.readAllBytes(f.toPath());

			} catch (IOException e) {
				e.printStackTrace();
			}
			mouseAdapter = mouseAdapterForSender(bytes, f);
		} else {
			// if is not sender, get bytes and file objects from response
			Object[] res = (Object[]) response;
			f = ((File) res[0]);

			bytes = (byte[]) res[1];
			mouseAdapter = mouseAdapter(bytes, f);
		}

		JPanel container = new JPanel(new BorderLayout());
		JPanel textFields = new JPanel(new BorderLayout());
		JLabel name = new JLabel(minifyName(f.getName()));
		name.setBorder(new EmptyBorder(0, 0, 0, 5));
		name.setForeground(Color.WHITE);
		textFields.add(name, BorderLayout.NORTH);
		JLabel size = new JLabel(calculateSize(f));
		size.setForeground(Color.WHITE);
		size.setBorder(new EmptyBorder(0, 0, 0, 5));
		textFields.add(size, BorderLayout.SOUTH);
		Icon icon = new Icon(IO.RESOURCES.getResource("clip.png"));
		icon.setBackground(c);
		container.setBackground(c);
		textFields.setBackground(c);
		container.add(icon, BorderLayout.WEST);
		container.add(textFields, BorderLayout.EAST);
		container.addMouseListener(mouseAdapter);
		for (Component component : container.getComponents()) {
			component.addMouseListener(mouseAdapter);
			component.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
		return container;
	}

	public static class LeftArrowBubble extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7610567787514365793L;
		private Color bg = new Color(76, 175, 80);

		LeftArrowBubble(Object data) {
			if (data instanceof String) {
				this.add(generateTextBubble((String) data, bg));
			} else {
				this.setBorder(new EmptyBorder(0, 5, 0, 0));
				this.add(generateFileBubble((Object[]) data, bg, false));
			}
		}

		@Override
		protected void paintComponent(final Graphics g) {
			// paint left bubble
			final Graphics2D graphics2D = (Graphics2D) g;
			RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics2D.setRenderingHints(qualityHints);
			graphics2D.setPaint(bg);
			int width = getWidth();
			int height = getHeight();
			GeneralPath path = new GeneralPath();
			path.moveTo(5, 10);
			path.curveTo(5, 10, 7, 5, 0, 0);
			path.curveTo(50, 0, 12, 0, 12, 5);
			path.curveTo(12, 5, 12, 0, 20, 0);
			path.lineTo(width - 10, 0);
			path.curveTo(width - 10, 0, width, 0, width, 10);
			path.lineTo(width, height - 10);
			path.curveTo(width, height - 10, width, height, width - 10, height);
			path.lineTo(15, height);
			path.curveTo(15, height, 5, height, 5, height - 10);
			path.lineTo(5, 15);
			path.closePath();
			graphics2D.fill(path);
		}
	}

	public static class RightArrowBubble extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4058404505283977313L;
		private Color bg = new Color(1, 121, 247);

		RightArrowBubble(Object data) {
			if (data instanceof String) {
				this.add(generateTextBubble((String) data, bg));
			} else {
				this.setBorder(new EmptyBorder(0, 5, 0, 0));
				this.add(generateFileBubble(data, bg, true));
			}
		}

		@Override
		protected void paintComponent(final Graphics g) {
			//paint right bubble
			final Graphics2D graphics2D = (Graphics2D) g;
			RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics2D.setRenderingHints(qualityHints);
			graphics2D.setPaint(bg);
			int width = getWidth();
			int height = getHeight();
			GeneralPath path = new GeneralPath();
			path.moveTo(width - 5, 10);
			path.curveTo(width - 5, 10, width - 7, 5, width - 0, 0);
			path.curveTo(width - 50, 0, width - 12, 0, width - 12, 5);
			path.curveTo(width - 12, 5, width - 12, 0, width - 20, 0);
			path.lineTo(10, 0);
			path.curveTo(10, 0, 0, 0, 0, 10);
			path.lineTo(0, height - 10);
			path.curveTo(0, height - 10, 0, height, 10, height);
			path.lineTo(width - 15, height);
			path.curveTo(width - 15, height, width - 5, height, width - 5, height - 10);
			path.lineTo(width - 5, 15);
			path.closePath();
			graphics2D.fill(path);
		}

	}
}
