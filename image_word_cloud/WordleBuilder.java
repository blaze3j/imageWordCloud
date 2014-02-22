package image_word_cloud;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class WordleBuilder {

	private String wordlePage = "HW4_wordle.html";
	/*
	 * Creates a post request
	 */
	private void doPost(String requestUrl, String wordQueryParameter) throws IOException{
		
		String urlParameters = String.format("text=%s", URLEncoder.encode(wordQueryParameter, "UTF-8"));
		String request = requestUrl;
		
		URL url = new URL(request); 
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false); 
		connection.setRequestMethod("POST"); 
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		connection.setRequestProperty("charset", "utf-8");
		connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
		connection.setUseCaches (false);

		DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
		
		//get the response
		int responseCode = connection.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(connection.getInputStream()));
		
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		
		FileOutputStream os = new FileOutputStream(wordlePage, false);
		os.write(response.toString().getBytes());
		os.close();
		 
		in.close();
	}
	
	/*
	 * Generated the word cloud
	 */
	public void createWordCloud(ArrayList<String> words) throws IOException{
		
		String separator = " ";
		String word_argument = "";
		
		for(int i=0; i< words.size(); i++){
			word_argument += words.get(i) + separator; 
		}
		
		doPost("http://www.wordle.net/advanced", word_argument);
	}
}
