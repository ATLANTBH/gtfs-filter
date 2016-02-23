package com.atlantbh.gtfs.filter.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipHelper {

	/**
	 * Unzip it
	 * 
	 * @param zipFile
	 *            input zip file
	 * @param output
	 *            zip file output folder
	 */
	public static List<String> unZipIt(String zipFile, String outputFolder) {
		List<String> fileList = new ArrayList<String>();
		byte[] buffer = new byte[1024];
		try {
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				fileList.add(newFile.getAbsolutePath());
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return fileList;
	}

	/**
	 * Zip it
	 * 
	 * @param zipFile
	 *            output ZIP file location
	 */
	public static void zipIt(String zipFile, String folderPath) {
		byte[] buffer = new byte[1024];
		try {
			File zip = new File(zipFile);
			zip.getParentFile().mkdirs();
			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);
			File folder = new File(folderPath);
			String[] files = folder.list();
			for (String file : files) {
				ZipEntry ze = new ZipEntry(file);
				zos.putNextEntry(ze);
				FileInputStream in = new FileInputStream(folderPath + File.separator + file);
				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}
				in.close();
			}
			zos.closeEntry();
			zos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
