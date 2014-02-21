package image_word_cloud;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.amazonaws.mturk.addon.HITProperties;
import com.amazonaws.mturk.addon.HITQuestion;
import com.amazonaws.mturk.addon.QAPValidator;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.service.exception.ValidationException;
import com.amazonaws.mturk.util.PropertiesClientConfig;

/**
 * The image word cloud application will create a HIT asking a worker to tag up to
 * three words given an image.
 * 
 * mturk.properties must be found in the current file path.
 * 
 * The following concepts are covered:
 * - Using the <FormattedContent> functionality in QAP
 * - File based QAP and HIT properties HIT loading 
 * - Validating the correctness of QAP
 * - Using a basic system qualification
 * - Previewing the HIT as HTML
 *
 */
public class ImageWordCloud {

    private RequesterService service;

    // Defining the location of the file containing the QAP and the properties of the HIT
    private String rootDir = ".";
    private String questionFile = rootDir + "/image_word_cloud.question";
    private String propertiesFile = rootDir + "/image_word_cloud.properties";

    /**
     * Constructor
     *
     */
    public ImageWordCloud() {
        service = new RequesterService(new PropertiesClientConfig("../mturk.properties"));
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

    public String getImagePath() {
        String defaultImagePath = ""; //"http://images.dailylife.com.au/2014/02/18/5175008/enhanced-buzz-wide-32746-1392314667-18.jpg?rand=1392679059978";
        
        if (0 < defaultImagePath.length())
            return defaultImagePath;
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter image path:");
        System.out.flush();
        String input = "";
        try {
            input = br.readLine();
        }
        catch(IOException exception) {
            System.err.println("Invalid input!");
        }
        return input;
    }
    
    public String overrideImagePath(String overview, String imagePath) {
        String changeMePath = "changemeintorealimagepath";
        
        Pattern p = Pattern.compile(changeMePath);
        Matcher m = p.matcher(overview);
        StringBuffer sb = new StringBuffer();

        if (m.find()) {
            m.appendReplacement(sb, imagePath);
        }

        m.appendTail(sb);

        overview = sb.toString();
        
        return overview;
    }

    /**
     * Creates the Image HIT
     * @param previewFile The filename of the preview file to be generated.  If null, no preview file will be generated
     * and the HIT will be created on Mechanical Turk.
     */
    public void createImageWordCloud(String previewFile) {
        try {

            //Loading the HIT properties file.  HITProperties is a helper class that contains the 
            //properties of the HIT defined in the external file.  This feature allows you to define
            //the HIT attributes externally as a file and be able to modify it without recompiling your code.
            //In this sample, the qualification is defined in the properties file.
            HITProperties props = new HITProperties(propertiesFile);

            //Loading the question (QAP) file.  
            HITQuestion question = new HITQuestion(questionFile);

            String input = this.getImagePath();
            System.out.println("Selected image path: " + input);

            question.setQuestion(this.overrideImagePath(question.getQuestion(), input));

            // Validate the question (QAP) against the XSD Schema before making the call.
            // If there is an error in the question, ValidationException gets thrown.
            // This method is extremely useful in debugging your QAP.  Use it often.
            QAPValidator.validate(question.getQuestion());

            //If a preview filename has been provided, the HIT will not be loaded. 
            //Instead a preview of the HIT will be generated as HTML.
            if (previewFile != null) {

                // Create a preview of the HIT in HTML
                System.out.println("--[Previewing HITs]--------");
                System.out.println("Saving preview to file: " + previewFile);

                if (rootDir != ".")
                    previewFile = rootDir + "/" + previewFile ;

                // There is no input file, so pass in null
                service.previewHIT(previewFile, null, props, question);
                System.out.println("Preview saved to: " + new File(previewFile).getAbsolutePath());

                //The preview file has not been provided so the HIT will be loaded into Mechanical Turk.
            } else {
                // System.out.println("props.getDescription(): " + question.getQuestion());
                
                // Create a HIT using the properties and question files
                HIT hit = service.createHIT(null, // HITTypeId 
                        props.getTitle(), 
                        props.getDescription(), props.getKeywords(), // keywords 
                        question.getQuestion(),
                        props.getRewardAmount(), props.getAssignmentDuration(),
                        props.getAutoApprovalDelay(), props.getLifetime(),
                        props.getMaxAssignments(), props.getAnnotation(), // requesterAnnotation 
                        props.getQualificationRequirements(),
                        null // responseGroup
                );

                System.out.println("Created HIT: " + hit.getHITId());

                System.out.println("You may see your HIT with HITTypeId '" 
                        + hit.getHITTypeId() + "' here: ");

                System.out.println(service.getWebsiteURL() 
                        + "/mturk/preview?groupId=" + hit.getHITTypeId());
            }
        } catch (ValidationException e) {
            //The validation exceptions will provide good insight into where in the QAP has errors.  
            //However, it is recommended to use other third party XML schema validators to make 
            //it easier to find and fix issues.
            System.err.println("QAP contains an error: " + e.getLocalizedMessage());  

        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        ImageWordCloud app = new ImageWordCloud();

        if (args.length == 1 && !args[0].equals("")) {
            app.createImageWordCloud(args[0]);
        } else if (app.hasEnoughFund()) {
            app.createImageWordCloud(null);
        }
    }
}
