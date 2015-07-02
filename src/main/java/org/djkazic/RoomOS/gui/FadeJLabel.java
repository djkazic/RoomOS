package org.djkazic.RoomOS.gui;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class FadeJLabel extends JLabel {

	private static final long serialVersionUID = 5719300898366882494L;
	private float alpha;
	private ImageIcon background;

	public FadeJLabel() {
		super();
		setAlpha(1f);
	}

	public ImageIcon getGif() {
		return background;
	}
	
	public void setGif(ImageIcon gif) {
		background = gif;
	}
	
	public void setAlpha(float value) {
		float old = alpha;
		alpha = value;
		firePropertyChange("alpha", old, alpha);
		repaint();
	}

	public float getAlpha() {
		return alpha;
	}

	@Override
	public Dimension getPreferredSize() {
		return background == null ? super.getPreferredSize() : new Dimension(500, 500);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(background != null) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getAlpha()));
			g2d.drawImage(background.getImage(), 0, -12, null);
			g2d.dispose();
		}
	}
}