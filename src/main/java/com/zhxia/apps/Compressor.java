package com.zhxia.apps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.Socket;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class Compressor implements Runnable {

	private Socket socket;

	public Compressor(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		try {
			try{
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						this.socket.getInputStream()));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
						this.socket.getOutputStream()));
				// 读取文件名
				String filename = reader.readLine();
				System.out.println(String.format("Thread:%d,%s", new Object[]{Long.valueOf(Thread.currentThread().getId()),filename}));
				String filetype="";
				if (filename.endsWith(".js")) {
					filetype = "js";
				} else if (filename.endsWith(".css")) {
					filetype = "css";
				} else {
					return;
				}
				// 读取内容部分
				StringBuilder sb = new StringBuilder();
				while (true) {
					String line = reader.readLine();
					if (line.equals("\0")) {
						break;
					}
					sb.append(line);
				}
				StringReader stringReader = new StringReader(sb.toString());
				// 分别处理js和css
				if("js".equals(filetype)){
					this.processJavaScript(stringReader, writer);
				}
				else if("css".equals(filetype)){
					this.processCss(stringReader, writer);
				}
				//需要先关闭socket输出流，再关socket闭输入流，顺序很重要
				writer.close(); 
				reader.close();
			}
			finally{
				this.socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 处理js文件的压缩
	 * @param in
	 * @param out
	 * @throws Exception
	 */
	private void processJavaScript(Reader in, Writer out) throws Exception {
		JavaScriptCompressor compressor = new JavaScriptCompressor(in,
				new ErrorReporter() {

					public void warning(String message, String sourceName,
							int line, String lineSource, int lineOffset) {
						if (line < 0) {
							System.err.println(String.format("\n[WARNING] %s",
									message));
						} else {
							System.err.println(String.format(
									"\n[WARNING] %d:%d:%s", line, lineOffset,
									message));
						}
					}

					public void error(String message, String sourceName,
							int line, String lineSource, int lineOffset) {
						if (line < 0) {
							System.err.println("\n[ERROR] " + message);
						} else {
							System.err.println("\n[ERROR] " + line + ':'
									+ lineOffset + ':' + message);
						}
					}

					public EvaluatorException runtimeError(String message,
							String sourceName, int line, String lineSource,
							int lineOffset) {
						error(message, sourceName, line, lineSource, lineOffset);
						return new EvaluatorException(message);
					}
				});
		boolean verbose = false;
		int linebreakpos = -1;
		boolean munge = true;
		boolean preserveAllSemiColons = false;
		boolean disableOptimizations = false;
		compressor.compress(out, linebreakpos, munge, verbose, preserveAllSemiColons, disableOptimizations);
	}
	
	/**
	 * 处理css文件的压缩
	 * @param in
	 * @param out
	 * @throws Exception
	 */
	private void processCss(Reader in,Writer out) throws Exception{
		CssCompressor compressor=new CssCompressor(in);
		int linebreakpos=-1;
		compressor.compress(out, linebreakpos);
	}
}
