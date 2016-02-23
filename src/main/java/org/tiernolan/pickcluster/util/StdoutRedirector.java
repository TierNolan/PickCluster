package org.tiernolan.pickcluster.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class StdoutRedirector {

	private static final AtomicBoolean redirectActive = new AtomicBoolean(false);

	public static boolean setRedirect(File dir, String prefix) {
		if (redirectActive.compareAndSet(false, true)) {
			try {
				new StdoutRedirector(dir, prefix);
			} catch (IOException e) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	private final LogOutputStream logOut;
	
	public StdoutRedirector(File dir, String prefix) throws IOException {
		this.logOut = new LogOutputStream(dir, prefix);
		System.setErr(new PrintStreamPassthrough(System.err, logOut));
		System.setOut(new PrintStreamPassthrough(System.out, logOut));
	}
	
	private static class PrintStreamPassthrough extends PrintStream {
		
		private final LogOutputStream logOut;
		private final PrintStream ps;
		
		public PrintStreamPassthrough(PrintStream ps, LogOutputStream logOut) {
			super(logOut, true);
			this.logOut = logOut;
			this.ps = ps;
		}
		
		@Override
		public void println() {
			synchronized (this) {
				synchronized (logOut) {
					super.println();
				}
			}
		}
		
		@Override
		public void println(char x) {
			synchronized (this) {
				synchronized (logOut) {
					super.println(x);
				}
			}
		}
		
		@Override
		public void println(float x) {
			synchronized (this) {
				synchronized (logOut) {
					super.println(x);
				}
			}
		}
		
		@Override
		public void println(long x) {
			synchronized (this) {
				synchronized (logOut) {
					super.println(x);
				}
			}
		}
		
		@Override
		public void println(int x) {
			synchronized (this) {
				synchronized (logOut) {
					super.println(x);
				}
			}
		}
		
		@Override
		public void println(boolean x) {
			synchronized (this) {
				synchronized (logOut) {
					super.println(x);
				}
			}
		}
		
		@Override
		public void println(double x) {
			synchronized (this) {
				synchronized (logOut) {
					super.println(x);
				}
			}
		}
		
		@Override
		public void println(String x) {
			synchronized (this) {
				synchronized (logOut) {
					super.println(x);
				}
			}
		}
		
		@Override
		public void println(Object x) {
			synchronized (this) {
				synchronized (logOut) {
					super.println(x);
				}
			}
		}
		
		@Override
		public void println(char[] x) {
			synchronized (this) {
				synchronized (logOut) {
					super.println(x);
				}
			}
		}
		
		@Override
		public void print(char x) {
			synchronized (this) {
				synchronized (logOut) {
					super.print(x);
				}
			}
		}
		
		@Override
		public void print(float x) {
			synchronized (this) {
				synchronized (logOut) {
					super.print(x);
				}
			}
		}
		
		@Override
		public void print(long x) {
			synchronized (this) {
				synchronized (logOut) {
					super.print(x);
				}
			}
		}
		
		@Override
		public void print(int x) {
			synchronized (this) {
				synchronized (logOut) {
					super.print(x);
				}
			}
		}
		
		@Override
		public void print(boolean x) {
			synchronized (this) {
				synchronized (logOut) {
					super.print(x);
				}
			}
		}
		
		@Override
		public void print(double x) {
			synchronized (this) {
				synchronized (logOut) {
					super.print(x);
				}
			}
		}
		
		@Override
		public void print(String x) {
			synchronized (this) {
				synchronized (logOut) {
					super.print(x);
				}
			}
		}
		
		@Override
		public void print(Object x) {
			synchronized (this) {
				synchronized (logOut) {
					super.print(x);
				}
			}
		}
		
		@Override
		public void print(char[] x) {
			synchronized (this) {
				synchronized (logOut) {
					super.print(x);
				}
			}
		}
		
		@Override
		public void flush() {
			synchronized (this) {
				synchronized (logOut) {
					super.flush();
				}
			}
		}
		
		@Override
		public void write(int b) {
			synchronized (this) {
				synchronized (logOut) {
					super.write(b);
					ps.write(b);
				}
			}
		}
		
		@Override
		public void write(byte[] b) {
			synchronized (this) {
				synchronized (logOut) {
					try {
						super.write(b);
						ps.write(b);
					} catch (IOException e) {
						super.setError();
					}
				}
			}
			
		}
		
		@Override
		public void write(byte[] b, int off, int len) {
			synchronized (this) {
				synchronized (logOut) {
					super.write(b, off, len);
					ps.write(b, off, len);
				}
			}
		}
		
	}
	
	private static class LogOutputStream extends OutputStream {

		private final File dir;
		private final String prefix;
		private OutputStream out;
		private String filename;
		private long lastCheck;
		
		public LogOutputStream(File dir, String prefix) throws IOException {
			if (!dir.isDirectory() && !dir.mkdir()) {
				throw new IOException("Unable to create log directory");
			}
			this.dir = dir;
			this.prefix = prefix;
			this.lastCheck = Long.MIN_VALUE;
			checkFilename();
		}
		
		@Override
		public synchronized void write(int b) throws IOException {
			out.write(b);
		}
		
		@Override
		public synchronized void write(byte[] b) throws IOException {
			out.write(b);
		}
		
		@Override
		public synchronized void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
		}
		
		@Override
		public synchronized void flush() throws IOException {
			out.flush();
			checkFilename();
		}
		
		@Override
		public synchronized void close() throws IOException {
			try {
				writeMessage(out, "File closed " + new Date().toString());
			} finally {
				out.close();
			}
		}
		
		public synchronized boolean checkFilename() throws IOException {
			if (lastCheck + 60000 < System.currentTimeMillis()) {
				lastCheck = System.currentTimeMillis();
				String newFilename = getFilename();
				if (out == null || !newFilename.equals(filename)) {
					if (out != null) {
						close();
					}
					out = new FileOutputStream(new File(dir, newFilename), true);
					writeMessage(out, "File opened " + new Date().toString());
					return true;
				}
			}
			return false;
		}
		
		private synchronized String getFilename() {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String date = format.format(new Date());
			return prefix + "_" + date;
		}
		
		private static void writeMessage(OutputStream out, String message) throws IOException {
			out.write("\n".getBytes(StandardCharsets.UTF_8));
			for (int i = 0; i < message.length() + 6; i++) {
				out.write((int) '#');
			}
			out.write("\n".getBytes(StandardCharsets.UTF_8));
			out.write("## ".getBytes(StandardCharsets.UTF_8));
			out.write(message.getBytes(StandardCharsets.UTF_8));
			out.write(" ##\n".getBytes(StandardCharsets.UTF_8));
			for (int i = 0; i < message.length() + 6; i++) {
				out.write((int) '#');
			}
			out.write("\n\n".getBytes(StandardCharsets.UTF_8));
		}
	}

}
