package org.csvjoiner.main;

import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVRecord;

public class ParsedFile {

	private final Set<String> columns;
	private final List<CSVRecord> records;
	private final String filename;

	public ParsedFile(final String filename, List<CSVRecord> records, Set<String> columns) {

		this.records = records;
		this.columns = columns;
		this.filename = filename;
	}

	public List<CSVRecord> getRecords() {
		return records;
	}

	public Set<String> getColumns() {
		return columns;
	}

	public String getFileName() {
		return filename;
	}

}
