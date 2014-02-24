package image_word_cloud;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class FilterOffensiveIDeasHITs {

	String uniqueideasfilename,inputfilename,outputfilename;
	FilterOffensiveIDeasHITs(String inputfile, String outputfile, String ideasfilename)
	{   
		inputfilename =  inputfile;
		outputfilename = outputfile;
		uniqueideasfilename = ideasfilename;
		
	}

	void filter_file()
	{
		try
		{
			
			//create BufferedReader to read csv file
			BufferedReader br1 = new BufferedReader( new FileReader(	inputfilename));
			BufferedReader br2 = new BufferedReader( new FileReader(	uniqueideasfilename));
			BufferedWriter bw = new BufferedWriter( new FileWriter(outputfilename));
			String csvLine 	  = null;
			String cvsSplitBy = ",";			
			
			
			Map<String, Integer> filteredUniqueWords = new HashMap<String, Integer>();
						
			int countLines = 0;
			
			while((csvLine = br2.readLine()) != null)
			{
				countLines ++;
				String[] splitString = csvLine.split(cvsSplitBy);
				splitString[0]=splitString[0].trim();
				filteredUniqueWords.put(splitString[0], Integer.valueOf(splitString[1]));
		
			}
			br2.close();
			
			
			countLines = 0;
			while ((csvLine = br1.readLine()) != null) 
			{
				countLines ++;
				String[] splitString = csvLine.split(cvsSplitBy);
				
				if (countLines > 1)// Non header
				{
					
					String[] splitWords	 = splitString[31].split("\\s+", 3);
					String[] splitWordsTemp = splitWords[1].split("\\|");
					for(String token: splitWordsTemp )
					{
//						if(token.contains("1"))
//							token = token.substring(1);// Remove 1 -> 1BadWord = BadWord
						
						if(filteredUniqueWords.containsKey(token))
							filteredUniqueWords.remove(token);
						
					}

				}//if (countLines > 1)// Non header
			}//while ((csvLine = br1.readLine()) != null) 
			br1.close();
			
			// Writing Filtered Unique Words in outputfile
			for (Map.Entry<String, Integer> entry : filteredUniqueWords.entrySet() )
			{
				String writeString = entry.getKey()+cvsSplitBy+entry.getValue();
				bw.write(writeString);
				bw.newLine();
			}
			bw.close();
			
			
		}//try
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}//catch(Exception ex)
	}

}

