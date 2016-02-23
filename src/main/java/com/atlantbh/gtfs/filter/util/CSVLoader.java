package com.atlantbh.gtfs.filter.util;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class CSVLoader {

	/**
	 * Don't touch this data
	 */
	public static final String COMMA = ",";
	public static final String QUOTE = "\"";
	public static final String FILE_SEPARATOR = "/";
	public static final String AGENCY_FILENAME = "agency.txt";
	public static final String CALENDAR_FILENAME = "calendar.txt";
	public static final String CALENDAR_DATES_FILENAME = "calendar_dates.txt";
	public static final String ROUTES_FILENAME = "routes.txt";
	public static final String SHAPES_FILENAME = "shapes.txt";
	public static final String STOP_TIMES_FILENAME = "stop_times.txt";
	public static final String STOPS_FILENAME = "stops.txt";
	public static final String TRIPS_FILENAME = "trips.txt";
	public static final String GTFS_ZIP_SUFFIX = "gtfs.zip";
	public static final String SHAPE_ID = "shape_id";
	public static final String SHAPE_PT_LAT = "shape_pt_lat";
	public static final String SHAPE_PT_LON = "shape_pt_lon";
	public static final String SHAPE_PT_SEQUENCE = "shape_pt_sequence";
	public static final String SHAPE_DIST_TRAVELED = "shape_dist_traveled";
	public static final String STOP_ID = "stop_id";
	public static final String STOP_NAME = "stop_name";
	public static final String STOP_LAT = "stop_lat";
	public static final String STOP_LON = "stop_lon";
	public static final String TRIP_ID = "trip_id";
	public static final String ARRIVAL_TIME = "arrival_time";
	public static final String DEPARTURE_TIME = "departure_time";
	public static final String STOP_SEQUENCE = "stop_sequence";
	public static final String STOP_HEADSIGN = "stop_headsign";
	public static final String PICKUP_TYPE = "pickup_type";
	public static final String DROP_OFF_TYPE = "drop_off_type";
	public static final String ROUTE_ID = "route_id";
	public static final String SERVICE_ID = "service_id";
	public static final String TRIP_HEADSIGN = "trip_headsign";
	public static final String DIRECTION_ID = "direction_id";
	public static final String AGENCY_ID = "agency_id";
	public static final String ROUTE_SHORT_NAME = "route_short_name";
	public static final String ROUTE_LONG_NAME = "route_long_name";
	public static final String ROUTE_TYPE = "route_type";

	/**
	 * Load routes.txt in Iterable<CSVRecord>
	 * 
	 * @param fileName
	 *            represents path to file in which are saved CSV records
	 * @return instance of {@link Iterator}
	 * @throws IOException
	 *             if file can't be loaded or processed
	 */
	public static Iterable<CSVRecord> loadRoutesRecords(String filePath) throws IOException {
		Reader reader = new FileReader(filePath);
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(
				ROUTE_ID, AGENCY_ID, ROUTE_SHORT_NAME,
				ROUTE_LONG_NAME, ROUTE_TYPE).parse(reader);
		return records;
	}
	/**
	 * Load trips.txt in Iterable<CSVRecord>
	 * 
	 * @param fileName
	 *            represents path to file in which are saved CSV records
	 * @return instance of {@link Iterator}
	 * @throws IOException
	 *             if file can't be loaded or processed
	 */
	public static Iterable<CSVRecord> loadTripsRecords(String filePath) throws IOException {
		Reader reader = new FileReader(filePath);
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(
				ROUTE_ID, SERVICE_ID, TRIP_ID,
				SHAPE_ID, TRIP_HEADSIGN, DIRECTION_ID).parse(reader);
		return records;
	}

	/**
	 * Load stops.txt in Iterable<CSVRecord>
	 * 
	 * @param fileName
	 *            represents path to file in which are saved CSV records
	 * @return instance of {@link Iterator}
	 * @throws IOException
	 *             if file can't be loaded or processed
	 */
	public static Iterable<CSVRecord> loadStopsRecords(String filePath) throws IOException {
		Reader reader = new FileReader(filePath);
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(
				STOP_ID, STOP_NAME, STOP_LAT, STOP_LON).parse(reader);
		return records;
	}

	/**
	 * Load stop_times.txt in Iterable<CSVRecord>
	 * 
	 * @param fileName
	 *            represents path to file in which are saved CSV records
	 * @return instance of {@link Iterator}
	 * @throws IOException
	 *             if file can't be loaded or processed
	 */
	public static Iterable<CSVRecord> loadStopTimesRecords(String filePath) throws IOException {
		Reader reader = new FileReader(filePath);
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(
				TRIP_ID, ARRIVAL_TIME, DEPARTURE_TIME, STOP_ID, STOP_SEQUENCE, STOP_HEADSIGN, PICKUP_TYPE, DROP_OFF_TYPE, SHAPE_DIST_TRAVELED).parse(reader);
		return records;
	}

	/**
	 * Load shapes.txt in Iterable<CSVRecord>
	 * 
	 * @param fileName
	 *            represents path to file in which are saved CSV records
	 * @return instance of {@link Iterator}
	 * @throws IOException
	 *             if file can't be loaded or processed
	 */
	public static Iterable<CSVRecord> loadShapesRecords(String filePath) throws IOException {
		Reader reader = new FileReader(filePath);
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(
				SHAPE_ID, SHAPE_PT_LAT, SHAPE_PT_LON, SHAPE_PT_SEQUENCE, SHAPE_DIST_TRAVELED).parse(reader);
		return records;
	}
}
