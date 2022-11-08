package org.csvjoiner.main.matching;

import org.apache.commons.csv.CSVRecord;

public class MostNearMatch {

	private final CSVRecord csvRecord2;
	private final Double distance;

	private final boolean isMatchByNumber;

	public MostNearMatch(CSVRecord csvRecord, Double distance) {
		this.csvRecord2 = csvRecord;
		this.distance = distance;
		this.isMatchByNumber = true;
	}

	public CSVRecord getCsvRecord2() {
		return csvRecord2;
	}

	public Double getDistance() {
		return distance;
	}

	public boolean isMatchByNumber() {
		return isMatchByNumber;
	}
}
