package pl.ncdc.hot3.pooltable.PoolTable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class WritePropertiesFile {
	public static void writeProperty(String filename, String key, String value) {
		try {
			Properties properties = new Properties();
			properties.setProperty(key, value);

			File file = new File("src\\main\\resources\\"+filename);
			FileOutputStream fileOut = new FileOutputStream(file);
			properties.store(fileOut, "Favorite Things");
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void writeProperty(String filename, String key, Double Dvalue) {
		try {
			String value = Dvalue.toString();
			Properties properties = new Properties();
			properties.setProperty(key, value);

			File file = new File("src\\main\\resources\\"+filename);
			FileOutputStream fileOut = new FileOutputStream(file);
			properties.store(fileOut, "Favorite Things");
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
