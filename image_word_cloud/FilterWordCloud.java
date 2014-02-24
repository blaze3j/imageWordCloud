package image_word_cloud;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.amazonaws.mturk.service.axis.RequesterService;

import com.amazonaws.mturk.addon.HITDataCSVReader;
import com.amazonaws.mturk.addon.HITDataCSVWriter;
import com.amazonaws.mturk.addon.HITDataInput;
import com.amazonaws.mturk.addon.HITProperties;
import com.amazonaws.mturk.addon.HITQuestion;
import com.amazonaws.mturk.addon.HITTypeResults;
import com.amazonaws.mturk.addon.QAPValidator;
import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.AssignmentStatus;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.exception.ValidationException;
import com.amazonaws.mturk.util.PropertiesClientConfig;

public class FilterWordCloud {
	
	public static String resultsFile = "filter_word_cloud.results";
	
	private RequesterService service;
	private String propertiesFile = "filter_word_cloud.properties";
	private String rootDir = ".";
	private String successFile = "filter_word_cloud.success";
	
	public FilterWordCloud(){
		service = new RequesterService(new PropertiesClientConfig("./mturk.properties"));
	}
	
	
    /**
     * Check to see if your account has sufficient funds
     * @return true if there are sufficient funds. False if not.
     */
    public boolean hasEnoughFund() {
        double balance = service.getAccountBalance();
        System.out.println("Got account balance: " + RequesterService.formatCurrency(balance));
        return balance > 0;
    }
    
    private String buildQuestion(ArrayList<String> words){
    	
    	StringBuilder question = new StringBuilder();
    	
    	question.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
		+ "<QuestionForm xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd\">"
		+ "  <Overview>"
		+ "    <FormattedContent><![CDATA["
		+ "      <h1 align=\"center\">Check offensive or NON-related words based on image</h1>"
		+ "      <h2>Instructions</h2>"
		+ "      Your task is to check words you believe are <strong>offensive</strong> or NOT related to the image."
		+ "      <br/>"
		+ "      <br/>"
		+ "      <h2>Task</h2>"
		+ "    ]]></FormattedContent>"
		+ "    <FormattedContent><![CDATA["
		+ "    <img src=\"http://images.dailylife.com.au/2014/02/18/5175008/enhanced-buzz-wide-32746-1392314667-18.jpg?rand=1392679059978\" alt=\"provocative image\" />"
		+ "    ]]></FormattedContent>"
		+ "  </Overview>"
    	+ "  <Question>"
        + "  <QuestionIdentifier>1</QuestionIdentifier>"
        + "  <QuestionContent>"
        + "    <Text>Please check words which are either offensive or NOT related to the image</Text>"
        + "  </QuestionContent>"
        + "  <AnswerSpecification>"
        + "    <SelectionAnswer>"
        + "      <MinSelectionCount>1</MinSelectionCount>"
        + "      <MaxSelectionCount>7</MaxSelectionCount>"
        + "      <StyleSuggestion>checkbox</StyleSuggestion>"
        + "      <Selections>");
    	
    	for(int i=0; i < words.size(); i++){
    		
    		question.append(String.format(
    	  "			<Selection>"
    	+ "           <SelectionIdentifier>%s</SelectionIdentifier>"
    	+ "           <Text>%s</Text>"
    	+ "         </Selection>", words.get(i), words.get(i)));
    				
    	}
    	
    	question.append("</Selections>"
    			+ "      </SelectionAnswer>"
    			+ "    </AnswerSpecification>"
    			+ "  </Question>"
    			+ "</QuestionForm>");
    	
    	return question.toString();
    	
    }
    
    public void saveHITInfo(ArrayList<HIT> hits) {
        try {
            //System.out.println("hitid\thittypeid");
            //System.out.println(hit.getHITId() + "\t" + hit.getHITTypeId());

            PrintWriter writer = new PrintWriter(successFile, "UTF-8");
            writer.println("hitid\thittypeid");
            for(HIT hit: hits){     	
            	writer.println(hit.getHITId() + "\t" + hit.getHITTypeId());
            }
            writer.close();
            
        } catch (Exception e) {
            System.err.println("ERROR: Could not print results: " + e.getLocalizedMessage());
        }
    }
    
    /**
     * Prints filtered results
     */
    public void printFilteredResults() {
        try {
        	
            boolean completed = false;
            
            //retrieve all Hits and check completion status of assignments
            BufferedReader reader = new  BufferedReader(new FileReader(successFile));
            String line;
            
            ArrayList<String> hitIds = new ArrayList<String>();
            reader.readLine();

            while((line = reader.readLine())!= null){	
            	hitIds.add(line.split("\t")[0]);
            }
            
            reader.close();
            
            while(!completed){

	            completed = WordCloudHelper.hitsCompleted(hitIds, service);
	            
	            if (!completed)
	            	Thread.sleep(1000 * 60 * 10); //sleep for 10 minutes and check again until completion
            }
            
            System.out.println("Using hit success file: " + successFile);
            System.out.println("Storing results to file: " + resultsFile);
            
            //Loads the .success file containing the HIT IDs and HIT Type IDs of HITs to be retrieved.
            HITDataInput success = new HITDataCSVReader(successFile);

            //Retrieves the submitted results of the specified HITs from Mechanical Turk
            HITTypeResults results = service.getHITTypeResults(success);
            results.setHITDataOutput(new HITDataCSVWriter(resultsFile, ',', false));

            //Writes the submitted results to the defined output file.
            //The output file is a tab delimited file containing all relevant details
            //of the HIT and assignments.  The submitted results are included as the last set of fields
            //and are represented as tab separated question/answer pairs
            results.writeResults();
            
            System.out.println("Results have been written to: " + resultsFile);

        } catch (Exception e) {
            System.err.println("ERROR: Could not print results: " + e.getLocalizedMessage());
        }
    }
	
    /*
     * Creates as many distinct HITs as necessary to filter words
     */
	public void createFilterHit(Boolean preview, ArrayList<String> words) throws IOException, ValidationException{ 
		
		HITProperties props = new HITProperties(propertiesFile);
		int num_words = words.size();
		int num_hits = num_words / 4;
		
		ArrayList<HIT> hits = new ArrayList<HIT>();
		
		for(int i=0; i < num_hits; i++ ){
			
			ArrayList<String> questionWords = new ArrayList<String>();
			
			for(int j=0; j<4 && (i*4 + j< words.size()) ; j++)
				questionWords.add(words.get(i*4 + j));
			
			HITQuestion question = new HITQuestion();		
			
			String questionXml = buildQuestion(questionWords);
 
			question.setQuestion(questionXml);
			
			//validate the question
			QAPValidator.validate(question.getQuestion());
			
			if (preview){
				
				String previewFile = "preview_sv" + i;
				
				// Create a preview of the HIT in HTML
	            System.out.println("--[Previewing HITs]--------");
	            System.out.println("Saving preview to file: " + previewFile);

	            
	            if (rootDir != ".")
	                previewFile = rootDir + "/" + previewFile;

	            // There is no input file, so pass in null
	            service.previewHIT(previewFile, null, props, question);
	            System.out.println("Preview saved to: " + new File(previewFile).getAbsolutePath());
			}
			else{

				HIT hit = service.createHIT(null, // HITTypeId 
	                    props.getTitle(), 
	                    props.getDescription(), props.getKeywords(), // keywords 
	                    question.getQuestion(),
	                    props.getRewardAmount(), props.getAssignmentDuration(),
	                    props.getAutoApprovalDelay(), props.getLifetime(),
	                    1, 
	                    props.getAnnotation(), // requesterAnnotation 
	                    props.getQualificationRequirements(),
	                    null // responseGroup
	            );
				
				System.out.println("Created HIT: " + hit.getHITId());
	
	            System.out.println("You may see your HIT with HITTypeId '" 
	                    + hit.getHITTypeId() + "' here: ");
	
	            System.out.println(service.getWebsiteURL() 
	                    + "/mturk/preview?groupId=" + hit.getHITTypeId());
	            
	            hits.add(hit);           
			}
		}
		
		saveHITInfo(hits);
	}
}
