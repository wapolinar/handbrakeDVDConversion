package de.apolinarski.handbrake;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HandbrakeCmd {
	
	private static final String HANDBRAKE="HandBrakeCLI";
	private static final int MAX_WAIT_TIME=30000; //30 seconds
	private static final int WAIT_PERIOD_TIME=1000; //1 second
	private List<ProcessBuilder> handbrakeCmdChain;

	private HandbrakeCmd()
	{

	}
	
	public static HandbrakeCmd handbrakeCheckISO(String filename) throws IOException
	{
		System.err.println("Filename is: "+filename);
		HandbrakeCmd result=new HandbrakeCmd();
		ProcessBuilder pb=new ProcessBuilder(HANDBRAKE,"-i",filename,"-t", "0");
		pb.redirectErrorStream(true);
		ParseHandbrakeDVDCheck check=new ParseHandbrakeDVDCheck(pb.start());
		int aggregatedWaitTime=0;
		while(!check.isFinished() && aggregatedWaitTime<MAX_WAIT_TIME)
		{
			try
			{
				Thread.sleep(WAIT_PERIOD_TIME);
				aggregatedWaitTime+=WAIT_PERIOD_TIME;
			}
			catch (InterruptedException e)
			{
				break;
			}
		}
		if(check!=null && check.getTitles()!=null)
		{
			//Do something - create CmdChain
		}
		String dvdName=check.getDvdName();
		if(dvdName==null)
		{
			Random random=new Random();
			dvdName=String.valueOf(random.nextInt());
		}
		output(check.getTitles(),filename,dvdName);
		return result;
	}
	
	private static void output(List<DVDTitle> titles, String filename, String dvdName)
	{
		if(titles==null)
		{
			System.out.println("Title is null!");
			return;
		}
		for(DVDTitle title : titles)
		{
			System.out.println(title.getParameters());
			try {
				List<String> argumentList=new ArrayList<String>();
				argumentList.add(HANDBRAKE);
				argumentList.add("-i");
				argumentList.add(filename);
				argumentList.add("-o");
				argumentList.add(buildName(ParameterMain.isMKV(),ParameterMain.getFilename(),dvdName,title.getId()));
//				if(ParameterMain.isMKV())
//				{
//					argumentList.add(dvdName+"_"+title.getId()+".mkv");
//				}
//				else
//				{
//					argumentList.add(dvdName+"_"+title.getId()+".mp4");
//				}
				argumentList.addAll(title.getParameters());
				Process p=new ProcessBuilder(argumentList).redirectErrorStream(true).start();
				ReadIt inputStream=new ReadIt(p.getInputStream());
				inputStream.join();
				p.waitFor();
			} catch (IOException | InterruptedException e) {
				System.err.println("Error while executing the command: "+e.getLocalizedMessage());
			}
		}
	}
	
	private static String buildName(boolean mkv, String filename,
			String dvdName, int id) {
		StringBuilder sb=new StringBuilder();
		sb.append(dvdName);
		sb.append('_');
		sb.append(id);
		String testName=sb.toString();
		if(mkv)
		{
			testName+=".mkv";
		}
		else
		{
			testName+=".mp4";
		}
		File testFile=new File(testName);
		if(!testFile.exists())
		{
			return testName;
		}
		sb.append('_');
		sb.append(filename);
		testName=sb.toString();
		if(mkv)
		{
			testName+=".mkv";
		}
		else
		{
			testName+=".mp4";
		}
		testFile=new File(testName);
		if(!testFile.exists())
		{
			return testName;
		}
		//Now add random number
		Random random=new Random();
		sb.append('_');
		final String fixedName=sb.toString();
		String currentTestName=testName;
		while(testFile.exists())
		{
			currentTestName=fixedName+String.valueOf(random.nextInt());
			if(mkv)
			{
				currentTestName+=".mkv";
			}
			else
			{
				currentTestName+=".mp4";
			}
			testFile=new File(currentTestName);
		}
		return currentTestName;
	}

	private static class ReadIt implements Runnable {
		
		private final Thread myThread;
		private final InputStream is;
		
		public ReadIt(InputStream is)
		{
			this.is=is;
			myThread=new Thread(this);
			myThread.start();
		}

		@Override
		public void run() {
			try
			{
				byte[] buffer=new byte[1024*1024];
				int bytesRead;
				while((bytesRead=is.read(buffer))!=-1)
				{
					//do nothing, just read it
					if(ParameterMain.isDEBUG())
					{
						System.err.println(new String(buffer));
					}
				}
			}
			catch(IOException e)
			{
				System.err.println("Could not read from thread: "+e.getLocalizedMessage());
			}
			
		}
		
		public void join()
		{
			try {
				myThread.join();
			} catch (InterruptedException e) {
				System.err.println("Could not wait for end of thread: "+e.getLocalizedMessage());
			}
		}
	}
}
