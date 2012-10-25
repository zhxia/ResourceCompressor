package com.zhxia.apps;

import java.net.ServerSocket;
import java.net.Socket;
//import java.nio.charset.Charset;

import jargs.gnu.CmdLineParser;

public class Main {
	public static void main(String[] args) throws Exception{
		CmdLineParser parser=new CmdLineParser();
		CmdLineParser.Option portOpt=parser.addIntegerOption('p', "port");
		CmdLineParser.Option helpOpt=parser.addBooleanOption('h',"help");
//		CmdLineParser.Option charsetOpt=parser.addStringOption("charset");
		try{
			parser.parse(args);
			
			//判断是否需要help
			Boolean help=(Boolean)parser.getOptionValue(helpOpt);
			if(help!=null && help.booleanValue()){
				usage();
				System.exit(0);
			}
			//判断端口
//			String strPort=(String)parser.getOptionValue(portOpt);
			int port=9527;
			try{
				port=(Integer)parser.getOptionValue(portOpt);
			}
			catch (Exception e) {
				System.err.println(String.format("default listen port:%s", port));
			}
/*			String charset=(String)parser.getOptionValue(charsetOpt);
			if(charset==null || !Charset.isSupported(charset)){
				charset="UTF-8";
			}*/
			//开启socket
			ServerSocket serverSocket=new ServerSocket(port);
			while(true){
				Socket socket=serverSocket.accept();
				Thread thread=new Thread(new Compressor(socket));
				thread.start();
			}
		}
		catch (CmdLineParser.OptionException e) {
			usage();
			System.exit(1);
		}
	}
	
	private static void usage(){
		System.err.println("\n Usage: java -jar ResourceCompressor.jar [options] \n -h, --help");
	}
}
