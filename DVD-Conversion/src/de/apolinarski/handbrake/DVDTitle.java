package de.apolinarski.handbrake;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DVDTitle {

	private final int id;
	private List<Audio> audio=new ArrayList<Audio>();
	private List<Integer> subtitles=new ArrayList<Integer>();
	
	public DVDTitle(int id)
	{
		this.id=id;
	}
	
	public void addAudio(int id, String copyParameters, String channelChanges)
	{
//		if(audio.size()==0)
//		{
//			//Add a faac track for the beginning
//			audio.add(new Audio(id,"faac","dpl2"));
//		}
		audio.add(new Audio(id, copyParameters, channelChanges));
	}
	
	public void addAudio(int id)
	{
//		if(audio.size()==0)
//		{
//			//Add a faac track for the beginning
//			audio.add(new Audio(id,"faac","dpl2"));
//		}
		audio.add(new Audio(id, "copy","none"));
	}
	
	public void addSubtitles(int id)
	{
		subtitles.add(id);
	}
	
	public boolean isOk()
	{
		return ((audio.size()>0) && (subtitles.size()>0));
	}
	
	public List<String> getParameters()
	{
		List<String> parameters=new ArrayList<String>();
		boolean audioIncluded=false;
		boolean subtitlesIncluded=false;
		parameters.add("-t");
		parameters.add(String.valueOf(id));
		parameters.add("-e");
		parameters.add("x264");
		parameters.add("-q");
		parameters.add("20.0");
		StringBuilder sb=new StringBuilder();
		Iterator<Audio> audioIterator=audio.iterator();
		if(audioIterator.hasNext())
		{
			parameters.add("-a");
			audioIncluded=true;
		}
		while(audioIterator.hasNext())
		{
			Audio a=audioIterator.next();
			sb.append(a.getId());
			if(audioIterator.hasNext())
			{
				sb.append(',');
			}
		}
		if(audioIncluded)
		{
			parameters.add(sb.toString());
			parameters.add("-E");
			sb=new StringBuilder();
			audioIterator=audio.iterator();
			while(audioIterator.hasNext())
			{
				Audio a=audioIterator.next();
				sb.append(a.getCopyParameters());
				if(audioIterator.hasNext())
				{
					sb.append(',');
				}
			}
			parameters.add(sb.toString());
			
			parameters.add("-6");
			sb=new StringBuilder();
			audioIterator=audio.iterator();
			while(audioIterator.hasNext())
			{
				Audio a=audioIterator.next();
				sb.append(a.getChannelParameters());
				if(audioIterator.hasNext())
				{
					sb.append(',');
				}
			}
			parameters.add(sb.toString());
		}
		parameters.add("--audio-copy-mask");
		parameters.add("aac,ac3,dtshd,dts,mp3");
		parameters.add("--audio-fallback");
		parameters.add("ffac3");
		parameters.add("-f");
		if(ParameterMain.isMKV())
		{
			parameters.add("mkv"); //Choose one
		}
		else
		{
			parameters.add("mp4"); //Choose one
		}
		parameters.add("-4");
		parameters.add("--decomb");
		parameters.add("--loose-anamorphic");
		parameters.add("--modulus");
		parameters.add("2");
		parameters.add("-m");
		parameters.add("--x264-preset");
		parameters.add("medium");
		parameters.add("--h264-profile");
		parameters.add("high");
		parameters.add("--h264-level");
		parameters.add("4.1");
		sb=new StringBuilder();
		Iterator<Integer> integerIterator=subtitles.iterator();
		if(integerIterator.hasNext())
		{
			subtitlesIncluded=true;
			parameters.add("-s");
		}
		while(integerIterator.hasNext())
		{
			int subtitle=integerIterator.next();
			sb.append(subtitle);
			if(integerIterator.hasNext())
			{
				sb.append(',');
			}
		}
		if(subtitlesIncluded)
		{
			parameters.add(sb.toString());
		}
		return parameters;
	}
	
	private static class Audio
	{
		private int id;
		private String copyParameters;
		private String channelParameters;
		
		public Audio(int id, String copyParameters, String channelParameters)
		{
			this.id=id;
			this.copyParameters=copyParameters;
			this.channelParameters=channelParameters;
		}

		public int getId() {
			return id;
		}

		public String getCopyParameters() {
			return copyParameters;
		}

		public String getChannelParameters() {
			return channelParameters;
		}
	}

	public int getId() {
		return id;
	}
}
