package de.apolinarski.handbrake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ParseHandbrakeDVDCheck {
	
	private final HandbrakeWaiter waiter;
	private List<DVDTitle> titles=new ArrayList<DVDTitle>();
	private boolean finished=false;
	private static final String[] CHOSEN_LANGUAGES=new String[]{"eng","deu"};
	private String dvdName=null;
	private static final String DVD_TITLE="libdvdnav: DVD Title:";

	public ParseHandbrakeDVDCheck(Process handbrakeProcess)
	{
		waiter=new HandbrakeWaiter(handbrakeProcess);
		new Thread(waiter).start();
	}
	
	public boolean isFinished()
	{
		return finished;
	}
	
	private void parseOutput(String output)
	{
		final String TITLE="+ title";
		final String AUDIO="+ audio tracks";
		final String SUBTITLE="+ subtitle tracks";
		final String LANGUAGE="(iso639-2: ";
		ArrayList<String> possibleLanguages=new ArrayList<String>();
		for(int i=0;i<CHOSEN_LANGUAGES.length;i++)
		{
			possibleLanguages.add(LANGUAGE+CHOSEN_LANGUAGES[i]+")");
		}
		BufferedReader reader=new BufferedReader(new StringReader(output));
		try
		{
			String currentLine;
			DVDTitle currentTitle=null;
			boolean statusAudioTracks=false;
			boolean statusSubtitleTracks=false;
			while((currentLine=reader.readLine())!=null)
			{
				if((currentLine.startsWith(DVD_TITLE)) && (dvdName==null))
				{
					dvdName=currentLine.substring(DVD_TITLE.length()+1);
				}
				if(currentLine.startsWith(TITLE))
				{
					//Found new title
					if(ParameterMain.isDEBUG())
					{
						System.err.println("Found new title!");
					}
					if((currentTitle!=null) && (currentTitle.isOk()))
					{
						titles.add(currentTitle);
					}
					int id=Integer.parseInt(currentLine.substring(TITLE.length()+1, currentLine.length()-1));
					currentTitle=new DVDTitle(id);
					statusAudioTracks=false;
					statusSubtitleTracks=false;
					continue;
				}
				if(currentLine.startsWith(AUDIO))
				{
					if(ParameterMain.isDEBUG())
					{
						System.err.println("Found AUDIO Start");
					}
					statusAudioTracks=true;
					statusSubtitleTracks=false;
					continue;
				}
				if(currentLine.startsWith(SUBTITLE))
				{
					if(ParameterMain.isDEBUG())
					{
						System.err.println("Found SUBTITLE Start");
					}
					statusSubtitleTracks=true;
					statusAudioTracks=false;
					continue;
				}
				if(statusAudioTracks)
				{
					if(currentLine.contains(LANGUAGE))
					{
						for(int i=0;i<possibleLanguages.size();i++)
						{
							if(currentLine.contains(possibleLanguages.get(i)))
							{
								//Get id
								currentLine=currentLine.substring(2);
								int index=currentLine.indexOf(',');
								if(index==-1)
								{
									continue;
								}
								currentLine=currentLine.substring(0,index);
								currentTitle.addAudio(Integer.parseInt(currentLine));
								if(ParameterMain.isDEBUG())
								{
									System.err.println("Found and added audio");
								}
								break;
							}
						}
					}
				}
				if(statusSubtitleTracks)
				{
					if(currentLine.contains(LANGUAGE))
					{
						for(int i=0;i<possibleLanguages.size();i++)
						{
							if(currentLine.contains(possibleLanguages.get(i)))
							{
								//Get id
								currentLine=currentLine.substring(2);
								int index=currentLine.indexOf(',');
								if(index==-1)
								{
									continue;
								}
								currentLine=currentLine.substring(0,index);
								currentTitle.addSubtitles(Integer.parseInt(currentLine));
								if(ParameterMain.isDEBUG())
								{
									System.err.println("Found and added subtitle");
								}
								break;
							}
						}
					}
				}
			}
			//Add last title
			if((currentTitle!=null) && (currentTitle.isOk()))
			{
				titles.add(currentTitle);
			}
		}
		catch (IOException e)
		{
			System.err.println("Could not parse in titles: "+e.getLocalizedMessage());
		}
		finished=true;
	}
	
	private class HandbrakeWaiter implements Runnable
	{
		private Process handbrakeProcess;
		
		public HandbrakeWaiter(Process handbrakeProcess)
		{
			this.handbrakeProcess=handbrakeProcess;
		}

		@Override
		public void run() {
			BufferedReader reader=new BufferedReader(new InputStreamReader(handbrakeProcess.getInputStream()));
			StringBuilder sb=new StringBuilder();
			try
			{
				String currentLine;
				while((currentLine=reader.readLine())!=null)
				{
					currentLine=currentLine.trim();
					if(currentLine.startsWith("+") || currentLine.startsWith(DVD_TITLE))
					{
						sb.append(currentLine);
						sb.append('\n');
					}
				}
				parseOutput(sb.toString());
			}
			catch(IOException e)
			{
				System.err.println("Error while reading Handbrake output: "+e.getLocalizedMessage());
			}
		}
	}

	public List<DVDTitle> getTitles() {
		return titles;
	}

	public String getDvdName() {
		return dvdName;
	}
}
