package image_word_cloud;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ProcessIDeasHITs {

	String inputfilename,outputfilename;
	
	ProcessIDeasHITs(String inputfile, String outputfile)
	{
		inputfilename =  inputfile;
		outputfilename = outputfile;
	}
	// Reads the Responses from input file and stores the Unique responses.
	void process_file()
	{
		try
		{
			//create BufferedReader to read csv file
			BufferedReader br = new BufferedReader( new FileReader(inputfilename));
			BufferedWriter bw = new BufferedWriter( new FileWriter(outputfilename));
			String csvLine 	  = null;
			String cvsSplitBy = ",";
			
			Map<String, Integer> uniqueWords = new HashMap<String, Integer>();
			int countLines = 0; 
			String qote = "\"";
			
			while ((csvLine = br.readLine()) != null) 
			{	countLines ++;
				// use comma as separator
				String[] splitString = csvLine.split(cvsSplitBy);
				if (countLines > 1)// Non header
				{	// System.out.println(splitString.length);
				    // System.out.println(csvLine);
				    // System.out.println(splitString[splitString.length - 1]);
				    String[] splitWords	 = splitString[splitString.length - 1].split("Word");
					for (int i = 1; i< splitWords.length; i++)
					{	String[] token  =  splitWords[i].split("\\s+", 2);
					    
						token[1]  =  token[1].toLowerCase();
						if(token[1].endsWith(qote))// IF contains " at end 
							token[1] = token[1].substring(0, token[1].length()-2);
						
						if (uniqueWords.containsKey(token[1]))
						{   
							int value = uniqueWords.get(token[1]);
							uniqueWords.put(token[1], value+1);
						}
						else
						{
							uniqueWords.put(token[1], 1);
						}
					}//for (String token : splitString)
				}//	if (countLines > 1)// Non header	
			}//while ((csvLine = br.readLine()) != null) 
			
			br.close();
			
			
			// Writing Unique Words in outputfile
			for (Map.Entry<String, Integer> entry : uniqueWords.entrySet() )
			{
				String writeString = entry.getKey()+cvsSplitBy+entry.getValue();
				bw.write(writeString);
				bw.newLine();
			}
			bw.close();
			
		}//try
		catch (Exception ex) 
		{
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}// catch(Exception ex)	
		
	}
	
	
	
	
}
