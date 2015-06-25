package org.djkazic.RoomOS.modules;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import org.djkazic.RoomOS.Settings;
import org.djkazic.RoomOS.Utils;
import org.djkazic.RoomOS.modules.bases.Module;

import de.voidplus.soundcloud.SoundCloud;
import de.voidplus.soundcloud.Track;

public class SCModule extends Module {

	private Utils uc;
	private SoundCloud sc;
	private AdvancedPlayer mp3Player;
	private ArrayList<Track> favorites;
	//private Track lastKnownTrack;
	private int pausedOnFrame = 0;
	private File buffer;

	public SCModule() {
		super("cmd_music_gen");
		sc = new SoundCloud(Settings.getScClient(), Settings.getScSecret());
		uc = new Utils();
	}

	public void process() {
		//TODO: connectivity test, if fail -> local file playback
		//TODO: mood switch (if local, specify flat_file?playlist? for this -> likely music/mood)
		try {
			uc.speak("Connecting to SoundCloud.");
			uc.speak("Pulling your likes list.");
			favorites = sc.get("/users/114439318/favorites");
			uc.speak("After looking around, I found this.");
			findAndPlay();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void findAndPlay() {
		try {
			int rand = (int) (Math.random() * favorites.size());
			Track streaming = favorites.get(rand);
			//lastKnownTrack = streaming;
			String streamURLStr = streaming.getStreamUrl();
			while(streamURLStr == null) {
				rand = (int) (Math.random() * favorites.size());
				streaming = favorites.get(rand);
				streamURLStr = streaming.getStreamUrl();
				System.out.println("Track stream URL was null: " + clean(streaming.getTitle()));
			}
			URL streamURL = new URL(streamURLStr);
			HttpURLConnection hconn = (HttpURLConnection) streamURL.openConnection();
			preBuffer(hconn);
			
			//URLConnection mp3Con = streamURL.openConnection();
			//mp3Con.addRequestProperty("User-Agent",
			//"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
			//InputStream playStream = mp3Con.getInputStream();
			//BufferedInputStream bis = new BufferedInputStream(playStream);
			
			String title = clean(streaming.getTitle());
			
			System.out.println("Dirty title: " + streaming.getTitle());
			System.out.println("Clean title: " + clean(streaming.getTitle()));
			
			//Smart formatting of titling
			if(title.contains(" - ")) {
				String[] split = title.split(" - ");
				uc.speak("You're listening to: " + uc.firstCaps(split[1]) + " by " + uc.firstCaps(split[0]));
			} else {
				uc.speak("You're listening to: " + uc.firstCaps(title));
			}
			
			buffer = new File("audioBuffer.mp3");

			mp3Player = new AdvancedPlayer(new BufferedInputStream(new FileInputStream(buffer)));
			mp3Player.setPlayBackListener(new PlaybackListener() {
				@Override
				public void playbackFinished(PlaybackEvent event) {
					pausedOnFrame = event.getFrame();
				}
			});
			latch.countDown();
			
			try {
				mp3Player.play();
			} catch (Exception e) {
				uc.speak("Error: this stream has timed out.");
				e.printStackTrace();
				stop();
				uc.speak("Music controls disabled.");
			}
			favorites.remove(streaming);
			if(mp3Player != null && favorites.size() > 0) {
				uc.speak("Advancing song.");
				pausedOnFrame = 0;
				buffer.delete();
				findAndPlay();
			}
			buffer.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		if(mp3Player != null) {
			mp3Player.stop();
			mp3Player = null;
		}
		uc.speak("Stopping music.");
		buffer.delete();
	}

	public void replay() {
		if(mp3Player == null && pausedOnFrame != 0) {
			try {
				mp3Player.play(pausedOnFrame, Integer.MAX_VALUE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			uc.speak("Re entering music.");
		} else {
			uc.speak("No music to resume.");
			System.out.println(mp3Player == null);
			System.out.println(pausedOnFrame);
		}
	}
	
	private void preBuffer(final HttpURLConnection httpConn)
			throws IOException {
		(new Thread(new Runnable() {
			public void run() {
				try {

					int responseCode = httpConn.getResponseCode();

					// always check HTTP response code first
					if (responseCode == HttpURLConnection.HTTP_OK) {
						String fileName = "audioBuffer.mp3";

						InputStream inputStream = httpConn.getInputStream();
						String saveFilePath = fileName;

						FileOutputStream outputStream = new FileOutputStream(saveFilePath);

						int bytesRead = -1;
						byte[] buffer = new byte[4096];
						while ((bytesRead = inputStream.read(buffer)) != -1) {
							outputStream.write(buffer, 0, bytesRead);
						}
						outputStream.close();
						inputStream.close();
					} else {
						uc.speak("Prebuffer error. Server replied HTTP code: " + responseCode);
					}
					httpConn.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		})).start();
	}
	
	private String clean(String str) {
		String output = str;
		return output.toLowerCase()
				  .replace("&", "and")
				  .replace("feat", "featuring")
				  .replace("ft", "featuring")
				  .replaceAll("\\[|\\]", "");
	}
}
