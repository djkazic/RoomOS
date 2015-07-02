package org.djkazic.RoomOS.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = -4284511493505984854L;
	private Timer timer;
	private float direction;
	private JPanel contentPane;
	private FadeJLabel gifHolder;
	private ImageIcon defaultIcon;
	private CountDownLatch blacked;
	private boolean alwaysOn = true;
    private boolean fadeIn = false;
    private long lastSetGifTime = 0L;

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 500, 500);
		
		direction = 0.050f;
		
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		contentPane.setBackground(Color.BLACK);
		setContentPane(contentPane);

		gifHolder = new FadeJLabel();
		gifHolder.setAlignmentX(CENTER_ALIGNMENT);
		gifHolder.setAlignmentY(CENTER_ALIGNMENT);
		defaultIcon = new ImageIcon("assets/imgres/gifs/boot.gif");
		gifHolder.setGif(defaultIcon);
		contentPane.add(gifHolder, BorderLayout.CENTER);

		timer = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				float alpha = gifHolder.getAlpha();
				alpha += direction;
				if(alpha < 0.09f) {
					alpha = 0.09f;
				} else if(alpha > 1) {
					alpha = 1;
				}
				if(alwaysOn) {
					alpha = 1;
				}
				if(fadeIn) {
					direction = 0.050f;
					if(alpha == 1) {
						alwaysOn = true;
						fadeIn = false;
					}
				} else {
					direction = -0.070f;
					if(alpha == 0.09f) {
						blacked.countDown();
					}
				}
				gifHolder.setAlpha(alpha);
			}
		});
		timer.start();
		setVisible(true);
	}

	private void changeGif(ImageIcon replacement) {
		try {
			alwaysOn = false;                //Disables static light and starts fade out
			if(blacked == null) { blacked = new CountDownLatch(1); }
			blacked.await();                 //Waits until fully blacked out
			blacked = new CountDownLatch(1); //Resets latch for blacked out state
			gifHolder.getGif().getImage().flush();
			gifHolder.setGif(replacement);   //Set actual image
			fadeIn = true;                   //Enables fade in (automatic disable)
			timer.restart();                 //Reset timer
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setLoop(String imageName) {
		String path = null;
		if(!imageName.contains(".")) {
			path = "assets/imgres/gifs/" + imageName + ".gif";
		} else {
			path = "assets/imgres/" + imageName;
		}
		File testImage = new File(path);
		swap(path, testImage);
	}
	
	public void setAlbumArt() {
		System.out.println("triggered attempt at AA");
		String path = "picBuffer.jpg";
		File testImage = new File(path);
		swap(path, testImage);
	}
	
	private void swap(String path, File testImage) {
		if(testImage.exists()) {
			if(lastSetGifTime + 1000L > System.currentTimeMillis()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			ImageIcon testII = new ImageIcon(path);
			changeGif(testII);
			lastSetGifTime = System.currentTimeMillis();
		}
	}
	
	public void reset() {
		setLoop("idle");
	}
}