package org.csvjoiner.main;

public class CSVHeader {

	private final String originalHeader;
	private final String originalFilePath;
	private final String newHeader;

	public CSVHeader(String originalHeader, String originalFilePath, TAGINPUTFILE tag) {

		this.originalFilePath = originalFilePath;
		this.originalHeader = originalHeader;
		this.newHeader = tag.toString() + " " + originalHeader;

	}

	public String getOriginalHeader() {
		return originalHeader;
	}

	public String getOriginalFilePath() {
		return originalFilePath;
	}

	public String getNewHeader() {
		return newHeader;
	}

}
