package image_word_cloud;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

import com.amazonaws.mturk.service.exception.ValidationException;

public class WordCloudHelper {

    /**
     * Retrieves the list of words to be
     */
    public static ArrayList<String> getUniqueWords() throws IOException{
    	
    	ArrayList<String> words =  new ArrayList<String>();
    	FileInputStream fStream = new FileInputStream(ImageWordCloud.uniqueWordsFile);
    	BufferedReader reader = new BufferedReader(new InputStreamReader(fStream));
    	String line;
    	
    	while((line = reader.readLine()) != null){
    		
    		String[] fragments = line.split(",");
    		words.add(fragments[0].trim());
    	}
    	
    	reader.close();
    	
    	return words;
    }
    
    /**
     * 
     * @return a list of words considered valid
     * @throws IOException 
     */
    public static ArrayList<String> getWordsForWordle() throws IOException{

    	FilterOffensiveIDeasHITs proc = new FilterOffensiveIDeasHITs(FilterWordCloud.resultsFile, 
    																 ImageWordCloud.filteredWordsFile, 
    																 ImageWordCloud.uniqueWordsFile);
    	
    	proc.filter_file();
    	
    	ArrayList<String> words =  new ArrayList<String>();
    	FileInputStream fStream = new FileInputStream(ImageWordCloud.filteredWordsFile);
    	BufferedReader reader = new BufferedReader(new InputStreamReader(fStream));
    	String line;
    	
    	while((line = reader.readLine()) != null){
    		
    		String[] fragments = line.split(",");
    		String word = fragments[0].trim();
    		
    		for(int i=0; i< Integer.parseInt(fragments[1]); i++){			
    			words.add(word);
    		}
    	}
    	
    	reader.close();
    	
    	return words;
    }
    
    /**
     * Creates the word hit
     */
    public static void createImageWordHit(){
        //            if (args.length == 1 && !args[0].equals("")) {
        //                app.createImageWordCloud(args[0]);
        //            } else if (app.hasEnoughFund()) {
        //                app.createImageWordCloud(null);
        //            }
        ImageWordCloud app = new ImageWordCloud();

        if (app.hasEnoughFund()) {
            app.createImageWordCloud(null);
        }   
    }
    
    
    /**
     * Creates a print results
     */
    public static void createAndPrintImageWordResults(){
        System.out.println(
        		"Getting results");
        
        ImageWordCloud app = new ImageWordCloud();
        
        app.printResults();
    }
    
    /**
     * Creates a file which contains all unique results
     */
    public static void createUniqueWordsOutputResults(){
    	 System.out.println("Process results to unique words");
         
         ProcessIDeasHITs proc = new ProcessIDeasHITs(ImageWordCloud.resultsFile, 
        		 ImageWordCloud.uniqueWordsFile);
         try{
             proc.process_file();
         }
         catch (Exception ex) 
         {
             System.out.println(ex.getMessage());
             ex.printStackTrace();
         }// catch(Exception ex)
    }
    
    /**
     * Creates filter HITs for words filtering
     * @throws IOException
     * @throws ValidationException
     */
    public static void createFilterHits() throws IOException, ValidationException{
    	System.out.println(
        		"Creating Filter HITs");
        
        FilterWordCloud app = new FilterWordCloud();
       
        ArrayList<String> words = getUniqueWords();     
        
        app.createFilterHit(false, words);
    }
    
    /**
     * Creates a wordle based on the provided data.
     * @throws IOException 
     */
    public static void createWordle() throws IOException{
    	
    	ArrayList<String> words = getWordsForWordle();
    	
    	WordleBuilder builder = new WordleBuilder();
    	builder.createWordCloud(words);
    }
}
