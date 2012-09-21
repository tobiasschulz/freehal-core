package net.freehal.core.util;

import java.io.File;
import java.util.List;

public class FileUtils {

	private static FileUtilsImpl instance = null;

	public static void set(FileUtilsImpl instance) {
		FileUtils.instance = instance;
	}

	public static List<String> readLines(File f) {
		return instance.readLines(f);
	}

	public static List<String> readLines(File d, File f) {
		return instance.readLines(new File(d.getAbsolutePath(), f.getPath()));
	}

	public static String read(File f) {
		return instance.read(f);
	}

	public static String read(File d, File f) {
		return instance.read(new File(d.getAbsolutePath(), f.getPath()));
	}
}
