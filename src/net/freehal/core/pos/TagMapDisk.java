package net.freehal.core.pos;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.freehal.core.util.FileUtils;
import net.freehal.core.util.FreehalConfig;
import net.freehal.core.util.RegexUtils;

public class TagMapDisk implements TagContainer {

	@SuppressWarnings("unused")
	private String name;
	private Set<File> files;

	public TagMapDisk(String name) {
		this.name = name;
		files = new HashSet<File>();
	}

	@Override
	public Iterator<Entry<String, Tags>> iterator() {
		return new HashMap<String, Tags>().entrySet().iterator();
	}

	@Override
	public void add(String word, Tags tags) {
		// ignore (we don't have a memory cache)
	}

	@Override
	public boolean containsKey(String word) {
		return get(word) != null;
	}

	@Override
	public Tags get(String word) {
		final String search = word + ":";
		for (File filename : files) {
			Tags tags = null;
			List<String> lines = FileUtils.readLines(
					FreehalConfig.getLanguageDirectory(), filename);
			lines.add(":");
			for (String line : lines) {
				line = RegexUtils.trimRight(line, "\\s");

				if (line.equals(search)) {
					tags = new Tags((Tags) null, null, null);

				} else if (tags != null) {
					if (line.startsWith(" ")) {
						line = RegexUtils.trim(line, ":,;\\s");
						if (line.startsWith("type")) {
							line = line.substring(4);
							line = RegexUtils.trimLeft(line, ":\\s");
							line = Tags.getUniqueType(line);
							tags = new Tags(tags, line, null, word);
						} else if (line.startsWith("genus")) {
							line = line.substring(5);
							line = RegexUtils.trimLeft(line, ":\\s");
							tags = new Tags(tags, null, line, word);
						}
					} else if (line.endsWith(":")) {
						return tags;
					}
				}
			}
		}
		return null;
	}

	@Override
	public void add(File filename) {
		files.add(filename);
	}

}