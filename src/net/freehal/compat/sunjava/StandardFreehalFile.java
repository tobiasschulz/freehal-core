/*******************************************************************************
 * Copyright (c) 2006 - 2012 Tobias Schulz and Contributors.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/gpl.html>.
 ******************************************************************************/
package net.freehal.compat.sunjava;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import net.freehal.core.util.AbstractFreehalFile;
import net.freehal.core.util.FreehalFile;
import net.freehal.core.util.FreehalFileImpl;
import net.freehal.core.util.FreehalFiles;
import net.freehal.core.util.LogUtils;

public class StandardFreehalFile extends AbstractFreehalFile {

	private StandardFreehalFile(File file) {
		super(file);
	}

	public static FreehalFiles.Factory newFactory() {
		return new FreehalFiles.Factory() {
			@Override
			public FreehalFileImpl newInstance(String path) {
				return new StandardFreehalFile(new File(path));
			}
		};
	}

	@Override
	public FreehalFile getChild(String file) {
		return new FreehalFile(new StandardFreehalFile(new File(this.getAbsolutePath(), file)));
	}

	@Override
	public FreehalFile getChild(FreehalFileImpl file) {
		return getChild(file.getPath());
	}

	@Override
	public boolean isFile() {
		return file.isFile();
	}

	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	@Override
	public FreehalFile[] listFiles() {
		ArrayList<FreehalFile> files = new ArrayList<FreehalFile>();
		File[] realFiles = file.listFiles();
		if (realFiles != null) {
			for (File realFile : realFiles) {
				files.add(new FreehalFile(new StandardFreehalFile(new File(realFile.getPath()))));
			}
		}
		return files.toArray(new FreehalFile[realFiles.length]);
	}

	@Override
	public long length() {
		return file.length();
	}

	@Override
	public boolean mkdirs() {
		return file.mkdirs();
	}

	@Override
	public boolean delete() {
		if (file.isDirectory()) {
			for (FreehalFile c : this.listFiles())
				c.delete();
		}
		if (file.delete()) {
			return true;
		} else {
			LogUtils.e("Failed to delete file or directory: " + file);
			return false;
		}
	}

	@Override
	public String toString() {
		return "{" + super.toString() + "}";
	}

	@Override
	public Iterable<String> readLines() {
		LogUtils.d("reading line by line: " + this);
		Iterable<String> iterator = null;
		try {
			iterator = new BufferedReaderIterator(new BufferedReader(new FileReader(this.getFile()),
					128 * 1024));

		} catch (Exception e) {
			LogUtils.e(e.getMessage());
			if (iterator == null)
				iterator = new NullIterator<String>();
		}
		return iterator;
	}

	@Override
	public String read() {
		LogUtils.d("reading in one chunk: " + this);
		BufferedReader theReader = null;
		String returnString = null;

		try {
			theReader = new BufferedReader(new FileReader(this.getFile()));
			char[] charArray = null;

			if (this.length() > Integer.MAX_VALUE) {
				LogUtils.e("The file is larger than int max = " + Integer.MAX_VALUE);
			} else {
				charArray = new char[(int) this.length()];

				// Read the information into the buffer.
				theReader.read(charArray, 0, (int) this.length());
				returnString = new String(charArray);

			}
		} catch (FileNotFoundException e) {
			LogUtils.e(e.getMessage());
		} catch (IOException e) {
			LogUtils.e(e.getMessage());
		} finally {
			try {
				if (theReader != null)
					theReader.close();
			} catch (IOException e) {
				LogUtils.e(e.getMessage());
			}
		}

		return returnString;
	}

	@Override
	public void append(String s) {
		BufferedWriter bw = null;

		try {
			File parent = this.getFile().getParentFile();
			if (!parent.exists() || !parent.isDirectory()) {
				parent.mkdirs();
			}

			bw = new BufferedWriter(new FileWriter(this.getFile(), true));
			bw.write(s);
			bw.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally { // always close the file
			if (bw != null)
				try {
					bw.close();
				} catch (IOException ioe2) {
					// just ignore it
				}
		} // end try/catch/finally
	}

	@Override
	public void write(String s) {
		BufferedWriter bw = null;

		try {
			File parent = this.getFile().getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}

			bw = new BufferedWriter(new FileWriter(this.getFile(), false));
			bw.write(s);
			bw.flush();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
			e.printStackTrace();
		} finally { // always close the file
			if (bw != null)
				try {
					bw.close();
				} catch (IOException ioe2) {
					// just ignore it
				}
		} // end try/catch/finally
	}

	@Override
	public int countLines() {
		int countOfLines = 0;
		Iterable<String> lines = readLines();
		for (@SuppressWarnings("unused")
		String line : lines) {
			++countOfLines;
		}
		return countOfLines;
	}

	public static class NullIterator<A> implements Iterable<A> {

		@Override
		public Iterator<A> iterator() {
			return new Iterator<A>() {

				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public A next() {
					return null;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}

	public static class BufferedReaderIterator implements Iterable<String> {

		private BufferedReader r;

		public BufferedReaderIterator(BufferedReader r) {
			this.r = r;
		}

		@Override
		public Iterator<String> iterator() {
			return new Iterator<String>() {

				private boolean end = false;

				@Override
				public boolean hasNext() {
					return !end;
				}

				@Override
				public String next() {
					String next = null;
					try {
						next = r.readLine();
					} catch (IOException e) {
						LogUtils.e(e.getMessage());
					}
					if (next == null) {
						end = true;
						next = "";
					}
					return next;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}

	}

	@Override
	public void touch() {
		append("");
	}
}
