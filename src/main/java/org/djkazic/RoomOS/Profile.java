package org.djkazic.RoomOS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Profile implements Serializable {

	private static final long serialVersionUID = 3023409294303024485L;

	private String name;
	private String callCard;
	private String newsGeneric = "http://news.google.com/?output=rss";
	private String newsFinance = "http://feeds.reuters.com/news/wealth";
	private String newsSports  = "http://feeds.reuters.com/reuters/sportsNews";
	private String newsTechnology = "http://feeds.reuters.com/reuters/technologyNews";

	public static void main(String[] args) {
		Profile kevin = new Profile("Kevin", "Ava", null, null, null, null);
		try {
			kevin.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Profile(String name, String callCard, String newsGeneric, String newsFinance, String newsSports, String newsTechnology) {
		this.name = name;
		this.callCard = callCard;
		if(newsGeneric != null) {
			this.newsGeneric = newsGeneric;
		}
		if(newsFinance != null) {
			this.newsFinance = newsFinance;
		}
		if(newsSports != null) {
			this.newsSports = newsSports;
		}
		if(newsTechnology != null) {
			this.newsTechnology = newsTechnology;
		}
	}

	public String getName() {
		return name;
	}

	public String getCallCard() {
		return callCard;
	}

	public String getNewsGeneric() {
		return newsGeneric;
	}

	public String getNewsFinance() {
		return newsFinance;
	}

	public String getNewsSports() {
		return newsSports;
	}

	public String getNewsTechnology() {
		return newsTechnology;
	}

	/**
	 * Pulls all known profiles from DB and creates instances of this class for each
	 */
	public static void loadProfiles() {
		Utils uc = new Utils();
		File profileDir = new File("profiles/");
		if(!profileDir.exists()) {
			profileDir.mkdir();
		}
		File[] files = profileDir.listFiles();
		if(files.length > 0) {
			for(File file : files) {
				try {
					Profile iterProf = load(file);
					RTCore.getProfileList().add(iterProf);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		uc.speak(files.length + " profiles loaded.");
	}

	public static Profile load(File profile) throws ClassNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(profile);
		ObjectInputStream os = new ObjectInputStream(fis);
		Object obj = os.readObject();
		os.close();
		if(obj instanceof Profile) {
			return (Profile) obj;
		}
		return null;
	}
	
	public void save() throws IOException {
		FileOutputStream fout = new FileOutputStream("profiles/" + name.toLowerCase() + ".profile");
		ObjectOutputStream os = new ObjectOutputStream(fout);
		os.writeObject(this);
		os.close();
	}
}
