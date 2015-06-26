package org.djkazic.RoomOS.modules;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.djkazic.RoomOS.basemodules.Module;

import de.voidplus.soundcloud.SoundCloud;
import de.voidplus.soundcloud.Track;

public class SCModule extends Module {

	private Utils uc;
	private SoundCloud sc;
	private AdvancedPlayer mp3Player;
	private ArrayList<Track> favorites;
	//private Track lastKnownTrack;
	private int pausedOnFrame = 0;
	private boolean stopping;
	private boolean resume;
	private File buffer;
	private BufferedInputStream bis;

	public SCModule() {
		super("cmd_music_gen");
		sc = new SoundCloud(Settings.getScClient(), Settings.getScSecret());
		uc = new Utils();
		stopping = false;
		resume = false;
	}

	public void process() {
		//TODO: connectivity test, if fail -> local file playback
		//TODO: mood switch (if local, specify flat_file?playlist? for this -> likely music/mood)
		if(resume) {
			resume = false;
			if(mp3Player != null && pausedOnFrame != 0) {
				try {
					bis = new BufferedInputStream(new FileInputStream(buffer));
					mp3Player = new AdvancedPlayer(bis);
					mp3Player.setPlayBackListener(new PlaybackListener() {
						@Override
						public void playbackFinished(PlaybackEvent event) {
							pausedOnFrame += event.getFrame() / 27;
						}
					});
					uc.speak("Re entering music.");
					latch.countDown();
					mp3Player.play(pausedOnFrame, Integer.MAX_VALUE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				uc.speak("No music to resume.");
				System.out.println(mp3Player == null);
				System.out.println(pausedOnFrame);
			}
		} else {
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
	}

	private void findAndPlay() {
		try {
			int rand = (int) (Math.random() * favorites.size());
			Track streaming = favorites.get(rand);
			//lastKnownTrack = streaming;
			streaming = favorites.get(rand);
			String streamURLStr = streaming.getStreamUrl();
			while(streaming == null || streamURLStr == null) {
				rand = (int) (Math.random() * favorites.size());
				streaming = favorites.get(rand);
				streamURLStr = streaming.getStreamUrl();
			}
			URL streamURL = new URL(streamURLStr);
			HttpURLConnection hconn = (HttpURLConnection) streamURL.openConnection();
			buffer = new File("audioBuffer.mp3");
			
			if(buffer.exists()) {
				buffer.delete();
			}
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
			
			bis = new BufferedInputStream(new FileInputStream(buffer));

			mp3Player = new AdvancedPlayer(bis);
			mp3Player.setPlayBackListener(new PlaybackListener() {
				@Override
				public void playbackFinished(PlaybackEvent event) {
					pausedOnFrame += event.getFrame() / 27;
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
			if(!stopping && favorites.size() > 0) {
				uc.speak("Advancing song.");
				pausedOnFrame = 0;
				buffer.delete();
				findAndPlay();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if(e instanceof FileNotFoundException) {
				uc.speak("Audio buffer could not be found. Check your network connection.");
			}
		}
	}

	public void stop() {
		if(mp3Player != null) {
			stopping = true;
			try {
				mp3Player.stop();
			} catch (NullPointerException e) {}
		}
		uc.speak("Stopping music.");
		stopping = false;
	}

	public void resume() {
		resume = true;
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
				  .replace("free download", "")
				  .replace("out now", "")
				  .replaceAll("\\[|\\]", "");
	}
}
