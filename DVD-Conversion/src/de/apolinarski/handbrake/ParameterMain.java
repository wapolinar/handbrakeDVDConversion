package de.apolinarski.handbrake;

import java.io.File;
import java.io.IOException;

public class ParameterMain {

	private static boolean DEBUG=false;
	private static boolean MKV=false;
	private static String filename;
	
	public static void main(String[] args) throws IOException {
		if(args.length==0)
		{
			System.out.println("No parameter file given.\nFirst parameter should be an ISO file.\nAs second paramter, you can use\"debug\".");
		}
		if(args.length>=2)
		{
			for(int i=1;i<args.length;i++)
			{
				if(args[i].equals("debug"))
				{
					DEBUG=true;
				}
				else if(args[i].equals("mkv"))
				{
					MKV=true;
				}
			}
		}
		filename=args[0];
		int lastSeparator=filename.lastIndexOf(File.separatorChar);
		if(lastSeparator!=-1)
		{
			filename=filename.substring(lastSeparator+1);
		}
		int extension=filename.lastIndexOf('.');
		if(extension!=-1)
		{
			filename=filename.substring(0,extension);
		}
		System.out.println("Last part of file name is: "+filename);
		HandbrakeCmd tasks=HandbrakeCmd.handbrakeCheckISO(args[0]);
		//Tasks are executed automatically, at the moment...
	}

	public static boolean isDEBUG() {
		return DEBUG;
	}

	public static boolean isMKV() {
		return MKV;
	}

	public static String getFilename() {
		return filename;
	}

}
