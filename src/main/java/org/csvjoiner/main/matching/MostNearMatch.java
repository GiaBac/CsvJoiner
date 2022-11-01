package org.csvjoiner.main.matching;

import org.apache.commons.csv.CSVRecord;

public class MostNearMatch {

	private final CSVRecord csvRecord2;
	private final String matchColumnName;
	private final Double distance;

	private final boolean isMatchByNumber;

	public MostNearMatch(CSVRecord csvRecord, String matchColumnName, Double distance) {
		this.csvRecord2 = csvRecord;
		this.matchColumnName = matchColumnName;
		this.distance = distance;
		this.isMatchByNumber = true;
	}

	public MostNearMatch(CSVRecord csvRecord) {
		this.csvRecord2 = csvRecord;
		this.matchColumnName = null;
		this.distance = null;
		isMatchByNumber = false;
	}

	public CSVRecord getCsvRecord2() {
		return csvRecord2;
	}

	public String getMatchColumnName() {
		return matchColumnName;
	}

	public Double getDistance() {
		return distance;
	}

	public boolean isMatchByNumber() {
		return isMatchByNumber;
	}
}
