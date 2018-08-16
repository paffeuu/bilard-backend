package pl.ncdc.hot3.pooltable.PoolTable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import pl.ncdc.hot3.pooltable.PoolTable.exceptions.PropertiesReaderException;

public class PropertiesReader  {
	Properties properties;

	public PropertiesReader(String filename) throws PropertiesReaderException {
		Properties properties = new Properties();
		try {
			// read properties from file
			File file = new File("src//main//resources//" + filename);
			FileInputStream fileInput = new FileInputStream(file);
			properties.load(fileInput);
			fileInput.close();
		} catch (FileNotFoundException e) {
			throw new PropertiesReaderException(e);
		} catch (IOException e) {
			throw new PropertiesReaderException(e);
		}

		this.properties = properties;
	}
	
	public String getStringProperty(String key) throws PropertiesReaderException {
		String value = null;
		try {
			// read
			value = properties.getProperty(key);
			if (value == null) {
				throw new PropertiesReaderException("key does not exist");
			}
		}catch(Exception e) {
			
		}

		return value;

	}

	public int getIntProperty(String key) throws PropertiesReaderException {
		int value = 0;
		try {
			// read
			String strValue = properties.getProperty(key);
			if (strValue == null) {
				throw new PropertiesReaderException("key does not exist");
			}
			// parse
			value = Integer.parseInt(strValue);
		} catch (NumberFormatException e) {
			throw new PropertiesReaderException(e);
		}

		return value;

	}

	public double getDoubleProperty(String key) throws PropertiesReaderException {
		double value = 0;
		try {
			// read
			String strValue = properties.getProperty(key);
			if (strValue == null) {
				throw new PropertiesReaderException("key does not exist");
			}
			// parse
			value = Double.parseDouble(strValue);
		} catch (NumberFormatException e) {
			throw new PropertiesReaderException(e);
		}

		return value;

	}
}
