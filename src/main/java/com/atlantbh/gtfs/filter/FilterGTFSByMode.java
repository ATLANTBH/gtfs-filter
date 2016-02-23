package com.atlantbh.gtfs.filter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.CopyOption;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.csv.CSVRecord;
import com.atlantbh.gtfs.filter.util.CSVLoader;
import com.atlantbh.gtfs.filter.util.ZipHelper;

/**
 * 
 * @author Kasim Kovacevic, Nedzad Hamzic
 *
 */
public class FilterGTFSByMode {

	/**
	 * Specify this data
	 */
	public static final String GTFS_RESOURCE_PATH = "!!!PATH TO ORIGINAL ZIPPED GTFS!!!";
	public static final String PATH_FOR_UNZIPPED_CONTENT = "!!!WHERE TO UNZIP ZIPPED GTFS!!!";
	/**
	 * provide empty folder
	 */
	public static final String PATH_FOR_NEW_FILTERED_GTFS_FILES = "!!!WHERE TO SAVE FILTERED FILES!!!";

	/**
	 * do not specify same output folder like PATH_FOR_NEW_FILTERED_GTFS_FILES (infinite loop on zipping)
	 */
	public static final String PATH_FOR_SAVING_NEW_ZIPPED_FILTERED_DATA = "!!!WHERE TO SAVE NEW GTFS ZIP FILE!!!";

	
	public enum Mode {
		TRAM(0, "Tram"), SUBWAY(1, "Subway"), RAIL(2, "Rail"), BUS(3, "Bus"), FERRY(4, "Ferry"), CABLE_CAR(5, "Cable car"), GONDOLA(6, "Gondola"), FUNICULAR(7, "Funicular");

		private Integer modeId;

		private String modeName;

		private Mode(final Integer modeId, final String modeName) {
			this.modeId = modeId;
			this.modeName = modeName;
		}

		public Integer getModeId() {
			return modeId;
		}

		public void setModeId(Integer modeId) {
			this.modeId = modeId;
		}

		public String getModeName() {
			return modeName;
		}

		public void setModeName(String modeName) {
			this.modeName = modeName;
		}

		public boolean equals(int mode) {
			return getModeId() == mode;
		}
	}

	public static void main(String[] args) {

		List<Mode> modes = new ArrayList<Mode>();
		// add wanted modes in list, uncomment wanted, comment unwanted
		modes.add(Mode.TRAM);
		// modes.add(Mode.BUS);
		// modes.add(Mode.CABLE_CAR);
		// modes.add(Mode.FERRY);
		// modes.add(Mode.FUNICULAR);
		// modes.add(Mode.GONDOLA);
		// modes.add(Mode.RAIL);
		// modes.add(Mode.SUBWAY);

		System.out.println("Unziping...");
		List<String> files = ZipHelper.unZipIt(GTFS_RESOURCE_PATH, PATH_FOR_UNZIPPED_CONTENT);

		String routesFilePath = null;
		String tripsFilePath = null;
		String stopsFilePath = null;
		String stopTimesFilePath = null;
		String agencyFilePath = null;
		String calendarFilePath = null;
		String calendarDatesFilePath = null;
		String shapesFilePath = "";

		/**
		 * get paths for unzipped files
		 */
		for (String file : files) {
			if (file.endsWith(CSVLoader.AGENCY_FILENAME)) {
				agencyFilePath = file;
			} else if (file.endsWith(CSVLoader.CALENDAR_FILENAME)) {
				calendarFilePath = file;
			} else if (file.endsWith(CSVLoader.CALENDAR_DATES_FILENAME)) {
				calendarDatesFilePath = file;
			} else if (file.endsWith(CSVLoader.ROUTES_FILENAME)) {
				routesFilePath = file;
			} else if (file.endsWith(CSVLoader.SHAPES_FILENAME)) {
				shapesFilePath = file;
			} else if (file.endsWith(CSVLoader.STOP_TIMES_FILENAME)) {
				stopTimesFilePath = file;
			} else if (file.endsWith(CSVLoader.STOPS_FILENAME)) {
				stopsFilePath = file;
			} else if (file.endsWith(CSVLoader.TRIPS_FILENAME)) {
				tripsFilePath = file;
			}
		}

		/**
		 * Clear all data from output directory, new data will be saved in this directory
		 */
		clearOutputDir(PATH_FOR_NEW_FILTERED_GTFS_FILES);

		/**
		 * Define copy options
		 */
		CopyOption[] options = new CopyOption[] {
				StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.COPY_ATTRIBUTES };
		try {
			if (calendarDatesFilePath != null) {
				System.out.println("Copy calendar_dates.txt ...");
				/**
				 * copy calendar_dates.txt from PATH_FOR_UNZIPPED_CONTENT folder path to PATH_FOR_NEW_FILTERED_GTFS_FILES (if exist)
				 * no need to filter this file
				 */
				java.nio.file.Files.copy(Paths.get(calendarDatesFilePath), Paths.get(PATH_FOR_NEW_FILTERED_GTFS_FILES + "/calendar_dates.txt"), options);
			}
			if (agencyFilePath != null) {
				System.out.println("Copy agency.txt ...");
				/**
				 * copy agency.txt from PATH_FOR_UNZIPPED_CONTENT folder path to PATH_FOR_NEW_FILTERED_GTFS_FILES (if exist)
				 * no need to filter this file
				 */
				java.nio.file.Files.copy(Paths.get(agencyFilePath), Paths.get(PATH_FOR_NEW_FILTERED_GTFS_FILES + "/agency.txt"), options);
			}
			if (calendarFilePath != null) {
				System.out.println("Copy calendar.txt ..");
				/**
				 * copy calendar.txt from PATH_FOR_UNZIPPED_CONTENT folder path to PATH_FOR_NEW_FILTERED_GTFS_FILES (if exist)
				 * no need to filter this file
				 */
				java.nio.file.Files.copy(Paths.get(calendarFilePath), Paths.get(PATH_FOR_NEW_FILTERED_GTFS_FILES + "/calendar.txt"), options);
			}
			if (routesFilePath != null) {
				/**
				 * filter routes.txt by provided list of modes
				 */
				List<String> routeIds = filterRoutes(routesFilePath, modes);
				if (tripsFilePath != null && shapesFilePath != null) {
					/**
					 * filter trips.txt by routes, and shapes.txt by trips
					 */
					HashSet<String> tripIds = filterTripsAndShapes(tripsFilePath, shapesFilePath, routeIds);
					if (stopTimesFilePath != null) {
						/**
						 * filter stop_times.txt by trip_ids from trips
						 */
						HashSet<String> stopIds = filterStopTimes(stopTimesFilePath, tripIds);
						if (stopsFilePath != null) {
							/**
							 * filter stops.txt by stop_ids from stop_times
							 */
							filterStops(stopsFilePath, stopIds);
						}
					}
				}
			}
			System.out.println("Zipping...");
			ZipHelper.zipIt(PATH_FOR_SAVING_NEW_ZIPPED_FILTERED_DATA + getZipFileName(modes), PATH_FOR_NEW_FILTERED_GTFS_FILES);
			System.out.println("FINISHED!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return gtfs zip file name, depending on list of modes e.g. TRAMBUSgtfs.zip, TRAMgtfs.zip, BUSgtfs.zip
	 * 
	 * @param modes
	 *            represents list of {@link Mode}
	 * @return merged name by modes
	 */
	private static String getZipFileName(List<Mode> modes) {
		StringBuilder sb = new StringBuilder();
		for (Mode mode : modes) {
			sb.append(mode.getModeName());
		}
		sb.append(CSVLoader.GTFS_ZIP_SUFFIX);
		return sb.toString();
	}

	/**
	 * filter shapes
	 * 
	 * @param filePath
	 *            path to unzipped shapes.txt
	 * @param listOfShapeIds
	 *            return ids for filtered shapes
	 */
	private static void filterShapes(String filePath, List<String> listOfShapeIds) {
		System.out.println("Extracting shapes...");
		try {
			Iterable<CSVRecord> records = CSVLoader.loadShapesRecords(filePath);
			int iterator = 0;
			for (CSVRecord record : records) {
				iterator++;
				String shapeId = record.get(CSVLoader.SHAPE_ID);
				String shapePtLat = record.get(CSVLoader.SHAPE_PT_LAT);
				String shapePtLon = record.get(CSVLoader.SHAPE_PT_LON);
				String shapePtSequence = record.get(CSVLoader.SHAPE_PT_SEQUENCE);
				String shapeDistTraveled = record.get(CSVLoader.SHAPE_DIST_TRAVELED);
				if (iterator == 1) {
					StringBuilder newFile = new StringBuilder();
					newFile.append(shapeId).append(CSVLoader.COMMA).append(shapePtLat).append(CSVLoader.COMMA).append(shapePtLon).append(CSVLoader.COMMA).append(shapePtSequence)
							.append(CSVLoader.COMMA).append(shapeDistTraveled);
					saveNewLineInFile(PATH_FOR_NEW_FILTERED_GTFS_FILES + CSVLoader.FILE_SEPARATOR + CSVLoader.SHAPES_FILENAME, newFile.toString());
				} else if (shapeId != null && listOfShapeIds.contains(shapeId)) {
					StringBuilder newFile = new StringBuilder();
					newFile.append(CSVLoader.QUOTE).append(shapeId).append(CSVLoader.QUOTE).append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(shapePtLat).append(CSVLoader.QUOTE)
							.append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(shapePtLon).append(CSVLoader.QUOTE)
							.append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(shapePtSequence).append(CSVLoader.QUOTE).append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(shapeDistTraveled)
							.append(CSVLoader.QUOTE);
					saveNewLineInFile(PATH_FOR_NEW_FILTERED_GTFS_FILES + CSVLoader.FILE_SEPARATOR + CSVLoader.SHAPES_FILENAME, newFile.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Filter stops
	 * 
	 * @param filePath
	 *            represents path to unzipped stops.txt
	 * @param stopIds
	 *            represents list of stop ids
	 */
	private static void filterStops(String filePath, HashSet<String> stopIds) {
		System.out.println("Extracting stops...");
		try {
			Iterable<CSVRecord> records = CSVLoader.loadStopsRecords(filePath);
			int iterator = 0;
			for (CSVRecord record : records) {
				iterator++;
				String stopId = record.get(CSVLoader.STOP_ID);
				String stopName = record.get(CSVLoader.STOP_NAME);
				String stopLat = record.get(CSVLoader.STOP_LAT);
				String stopLon = record.get(CSVLoader.STOP_LON);
				if (stopId != null) {
					StringBuilder newFile = new StringBuilder();
					if (iterator == 1) {
						newFile.append(stopId).append(CSVLoader.COMMA).append(stopName).append(CSVLoader.COMMA).append(stopLat).append(CSVLoader.COMMA).append(stopLon);
						saveNewLineInFile(PATH_FOR_NEW_FILTERED_GTFS_FILES + CSVLoader.FILE_SEPARATOR + CSVLoader.STOPS_FILENAME, newFile.toString());
					} else if (stopIds.contains(stopId)) {
						newFile.append(CSVLoader.QUOTE).append(stopId).append(CSVLoader.QUOTE).append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(stopName).append(CSVLoader.QUOTE)
								.append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(stopLat).append(CSVLoader.QUOTE)
								.append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(stopLon).append(CSVLoader.QUOTE);
						saveNewLineInFile(PATH_FOR_NEW_FILTERED_GTFS_FILES + CSVLoader.FILE_SEPARATOR + CSVLoader.STOPS_FILENAME, newFile.toString());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Filter stop times by trip ids
	 * 
	 * @param filePath
	 *            represents path to unzipped trips.txt
	 * @param listOfTripIds
	 *            represents list of trip ids
	 * @return {@link HashSet} of stop ids
	 */
	private static HashSet<String> filterStopTimes(String filePath, HashSet<String> listOfTripIds) {
		System.out.println("Extracting stop times...");
		HashSet<String> listOfStopIds = new HashSet<String>();
		int iterator = 0;
		try {
			Iterable<CSVRecord> records = CSVLoader.loadStopTimesRecords(filePath);
			for (CSVRecord record : records) {
				iterator++;
				String tripId = record.get(CSVLoader.TRIP_ID);
				String arrivalTime = record.get(CSVLoader.ARRIVAL_TIME);
				String departureTime = record.get(CSVLoader.DEPARTURE_TIME);
				String stopId = record.get(CSVLoader.STOP_ID);
				String stopSequence = record.get(CSVLoader.STOP_SEQUENCE);
				String stopHeadsign = record.get(CSVLoader.STOP_HEADSIGN);
				String pikupType = record.get(CSVLoader.PICKUP_TYPE);
				String dropOffType = record.get(CSVLoader.DROP_OFF_TYPE);
				String shapeDistTraveled = record.get(CSVLoader.SHAPE_DIST_TRAVELED);
				if (tripId != null) {
					StringBuilder newFile = new StringBuilder();
					if (iterator == 1) {
						newFile.append(tripId).append(CSVLoader.COMMA).append(arrivalTime).append(CSVLoader.COMMA).append(departureTime).append(CSVLoader.COMMA).append(stopId).append(CSVLoader.COMMA)
								.append(stopSequence)
								.append(CSVLoader.COMMA)
								.append(stopHeadsign).append(CSVLoader.COMMA).append(pikupType).append(CSVLoader.COMMA).append(dropOffType).append(CSVLoader.COMMA).append(shapeDistTraveled);
						saveNewLineInFile(PATH_FOR_NEW_FILTERED_GTFS_FILES + CSVLoader.FILE_SEPARATOR + CSVLoader.STOP_TIMES_FILENAME, newFile.toString());
					} else if (listOfTripIds.contains(tripId)) {
						newFile.append(CSVLoader.QUOTE).append(tripId).append(CSVLoader.QUOTE).append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(arrivalTime).append(CSVLoader.QUOTE)
								.append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(departureTime).append(CSVLoader.QUOTE)
								.append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(stopId).append(CSVLoader.QUOTE).append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(stopSequence)
								.append(CSVLoader.QUOTE).append(CSVLoader.COMMA)
								.append(CSVLoader.QUOTE).append(stopHeadsign).append(CSVLoader.QUOTE).append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(pikupType).append(CSVLoader.QUOTE)
								.append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(dropOffType)
								.append(CSVLoader.QUOTE).append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(shapeDistTraveled).append(CSVLoader.QUOTE);
						listOfStopIds.add(stopId);
						saveNewLineInFile(PATH_FOR_NEW_FILTERED_GTFS_FILES + CSVLoader.FILE_SEPARATOR + CSVLoader.STOP_TIMES_FILENAME, newFile.toString());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listOfStopIds;
	}

	/**
	 * Filter trip.txt and shapes.txt
	 * 
	 * @param filePath
	 *            represents path to unzipped trips.txt
	 * @param shapeFilePath
	 *            represents path to unzipped shapes.txt
	 * @param listOfRouteIds
	 *            list of route ids
	 * @return {@link HashSet} of filtered trip ids
	 */
	private static HashSet<String> filterTripsAndShapes(String filePath, String shapeFilePath, List<String> listOfRouteIds) {
		System.out.println("Extracting trips...");
		HashSet<String> listOfTripIds = new HashSet<String>();
		List<String> listOfShapeIds = new ArrayList<String>();
		try {
			int iterator = 0;
			Iterable<CSVRecord> records = CSVLoader.loadTripsRecords(filePath);
			for (CSVRecord record : records) {
				iterator++;
				String routeId = record.get(CSVLoader.ROUTE_ID);
				String serviceId = record.get(CSVLoader.SERVICE_ID);
				String tripId = record.get(CSVLoader.TRIP_ID);
				String tripHeadSign = record.get(CSVLoader.TRIP_HEADSIGN);
				String shapeId = record.get(CSVLoader.SHAPE_ID);
				String directionId = record.get(CSVLoader.DIRECTION_ID);
				if (routeId != null) {
					StringBuilder newFile = new StringBuilder();
					if (iterator == 1) {
						newFile.append(routeId).append(CSVLoader.COMMA).append(serviceId).append(CSVLoader.COMMA).append(tripId).append(CSVLoader.COMMA).append(tripHeadSign).append(CSVLoader.COMMA)
								.append(shapeId).append(CSVLoader.COMMA)
								.append(directionId);
						saveNewLineInFile(PATH_FOR_NEW_FILTERED_GTFS_FILES + CSVLoader.FILE_SEPARATOR + CSVLoader.TRIPS_FILENAME, newFile.toString());
					} else if (listOfRouteIds.contains(routeId)) {
						newFile.append(CSVLoader.QUOTE).append(routeId).append(CSVLoader.QUOTE).append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(serviceId).append(CSVLoader.QUOTE)
								.append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(tripId).append(CSVLoader.QUOTE)
								.append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(tripHeadSign).append(CSVLoader.QUOTE).append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(shapeId)
								.append(CSVLoader.QUOTE).append(CSVLoader.COMMA)
								.append(CSVLoader.QUOTE).append(directionId).append(CSVLoader.QUOTE);
						listOfTripIds.add(tripId);
						listOfShapeIds.add(shapeId);
						saveNewLineInFile(PATH_FOR_NEW_FILTERED_GTFS_FILES + CSVLoader.FILE_SEPARATOR + CSVLoader.TRIPS_FILENAME, newFile.toString());
					}
				}
			}
			filterShapes(shapeFilePath, listOfShapeIds);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return listOfTripIds;
	}

	/**
	 * Filter routes.txt
	 * 
	 * @param filePath
	 *            represents path to unzipped routes.txt
	 * @param modes
	 *            represents list of modes
	 * @return list of filtered route ids
	 */
	private static List<String> filterRoutes(String filePath, List<Mode> modes) {
		System.out.println("Extracting routes...");
		List<String> listOfRouteIds = new ArrayList<String>();
		try {
			Iterable<CSVRecord> records = CSVLoader.loadRoutesRecords(filePath);
			int iterator = 0;
			for (CSVRecord record : records) {
				iterator++;
				String routeId = record.get(CSVLoader.ROUTE_ID);
				String agencyId = record.get(CSVLoader.AGENCY_ID);
				String routeShortName = record.get(CSVLoader.ROUTE_SHORT_NAME);
				String routeLongName = record.get(CSVLoader.ROUTE_LONG_NAME);
				String routeType = record.get(CSVLoader.ROUTE_TYPE);
				if (routeId != null && routeType != null) {
					StringBuilder newFile = new StringBuilder();
					if (iterator == 1) {
						newFile.append(routeId).append(CSVLoader.COMMA).append(agencyId).append(CSVLoader.COMMA).append(routeShortName).append(CSVLoader.COMMA).append(routeLongName)
								.append(CSVLoader.COMMA).append(routeType);
						saveNewLineInFile(PATH_FOR_NEW_FILTERED_GTFS_FILES + CSVLoader.FILE_SEPARATOR + CSVLoader.ROUTES_FILENAME, newFile.toString());
					} else {
						boolean routeTypeInListOfModes = isSelectedMode(routeType, modes);
						if (routeTypeInListOfModes) {
							agencyId = "1";
							newFile.append(CSVLoader.QUOTE).append(routeId).append(CSVLoader.QUOTE).append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(agencyId).append(CSVLoader.QUOTE)
									.append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(routeShortName)
									.append(CSVLoader.QUOTE)
									.append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(routeLongName).append(CSVLoader.QUOTE).append(CSVLoader.COMMA).append(CSVLoader.QUOTE).append(routeType)
									.append(CSVLoader.QUOTE);
							listOfRouteIds.add(routeId);
							saveNewLineInFile(PATH_FOR_NEW_FILTERED_GTFS_FILES + CSVLoader.FILE_SEPARATOR + CSVLoader.ROUTES_FILENAME, newFile.toString());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listOfRouteIds;

	}

	/**
	 * Append content to file
	 * 
	 * @param filePath
	 *            represents file path
	 * @param content
	 *            represents content for adding to file
	 */
	private static void saveNewLineInFile(String filePath, String content) {
		try {
			File fileForCreatingFolders = new File(filePath);
			fileForCreatingFolders.getParentFile().mkdirs();
			try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))) {
				out.println(content);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check is provided mode in list of chosen modes
	 * 
	 * @param modeType
	 *            represents mode for checking
	 * @param modes
	 *            represents list of modes
	 * @return true/false
	 */
	private static boolean isSelectedMode(String modeType, List<Mode> modes) {
		for (Mode mode : modes) {
			if (mode.equals(Integer.parseInt(modeType))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Delete all content from provided path
	 * 
	 * @param directory
	 *            represents path to directory
	 */
	private static void clearOutputDir(String directory) {
		try {
			File dir = new File(directory);
			for (File file : dir.listFiles())
				file.delete();
		} catch (Exception e) {

		}
	}

}
