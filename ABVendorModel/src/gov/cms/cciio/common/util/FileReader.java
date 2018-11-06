package gov.cms.cciio.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileReader {
	
	static String  path = "/home/sjmeyer/Documents/projects/CMS/data_integration/SHOP";
	static String fileName = "EIDM-AgentIDs.csv";

	public FileReader() {
		
	
	}
	
	public static List<String> readFileLines(String path, String name){
		
		try{
			BufferedReader reader = 
					Files.newBufferedReader(
							FileSystems.getDefault().getPath(path, fileName),
							Charset.defaultCharset());
			List<String> lines = new ArrayList<String>();
			String line = null;
			while ( (line = reader.readLine()) != null){
				lines.add(line);
			}
			return lines;
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		return null;
    }
public static List<String> readFileLines( String file){
		
		try{
			BufferedReader reader = 
					Files.newBufferedReader(
							FileSystems.getDefault().getPath(file),
							Charset.defaultCharset());
			List<String> lines = new ArrayList<String>();
			String line = null;
			while ( (line = reader.readLine()) != null){
				lines.add(line);
			}
			return lines;
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		return null;
    }

}
