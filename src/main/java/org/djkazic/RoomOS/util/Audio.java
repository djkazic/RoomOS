package org.djkazic.RoomOS.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import org.apache.commons.codec.digest.DigestUtils;

public class Audio {
        private static Audio audio;

        private Audio() {
        }

        public synchronized static Audio getInstance() {

                if (audio == null) {
                        audio = new Audio();
                }
                return audio;
        }

        public InputStream getAudio(String text) throws IOException {
        		//TODO: cache into cache folder
        		String hashTxt = DigestUtils.shaHex(text);
        		
        		File cacheFolder = new File("cache");
        		if(!cacheFolder.exists()) {
        			cacheFolder.mkdir();
        		}
        		for(File file : cacheFolder.listFiles()) {
        			if(file.getName().equals(hashTxt)) {
        				FileInputStream fi = new FileInputStream(file);
        				return new BufferedInputStream(fi);
        			}
        		}
        		
                URL url = new URL("http://api.voicerss.org" + "?key=" + Settings.getVoiceApi()
                				+ "&src=" + text.replace(" ", "%20") + "&hl=en-gb&f=24khz_16bit_stereo");
                URLConnection urlConn = url.openConnection();
                urlConn.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
                InputStream audioSrc = urlConn.getInputStream();
                
                File audioCache = new File("cache/" + hashTxt);
                if(!audioCache.exists()) {
                	audioCache.createNewFile();
                }
                FileOutputStream fo = new FileOutputStream(audioCache);
                byte[] buf = new byte[1024];
                int len;
                while((len = audioSrc.read(buf)) > 0){
                    fo.write(buf, 0, len);
                }
                fo.close();

                return new BufferedInputStream(new FileInputStream(audioCache));
        }

        public void play(InputStream sound) throws JavaLayerException {
                new Player(sound).play();
        }

}
