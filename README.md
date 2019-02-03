# Twitter-Sentiment-Analysis
The objective of this project is to perform Sentiment analysis on Twitter dataset and find positive, negative and neutral tweets. We will use Hive and MapReduce along with AFINN-111 list to perform analysis.

### Prerequisites
Any flavor of linux with following installed
  - Hadoop 2.x or above.
  - JDK 8
  - Hive
  
### Steps:
  - Parse the  dataset which is in JSON format and remove all the special symbols, hyperlinks and stop words from tweets. 
  - Calculate the sentiment value of the tweet using AFINN-111 list.
  - Output id, processed Tweet and the sentiment value of that tweet.
  - Load this processed data into Hive and get positive , negative and neutral tweets.
  
  
### Steps:
   - We will use third party API like simple json-simple-1.1.1 API along with Apache's StringUtils API to parse the data. 
      - [JSON Simple Example.](https://www.geeksforgeeks.org/parse-json-java/)
      - [StringUtils API](https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringUtils.html#isNotBlank-java.lang.CharSequence-)
   - Add these external jars to you project. If you are using eclipse 
     - Right click on project -> build path -> configure build path -> libraries -> add external jars. 
   /// your picture here.
   - Inside *setup method* will read AFINN list stored in **distributed cache** and store the words as key and there number as value.We will make use of these values in *map method*
  
   ```java
   
	JSONParser parsing  = null;
	
	Map<String,String> dictionary = null;
	
	public void setup(Context context) throws IOException, InterruptedException
	{
		parsing = new JSONParser();
		dictionary = new HashMap<String,String>();
		
		
	  URI[] cacheFiles = context.getCacheFiles();
	  if (cacheFiles != null && cacheFiles.length > 0)
	  {  
	    	String line ="";
	        FileSystem fs = FileSystem.get(context.getConfiguration());
	        Path path = new Path(cacheFiles[0].toString());
	        BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(path)));
	    
	        while((line = reader.readLine())!=null)
	        {
	        	String []tokens = line.split("\t");
	        	dictionary.put(tokens[0], tokens[1]);
	        }
	
 	 }
   ```
   - Inside *map method* we first try to find  Twitters **quoted_status** object to get text and id inside  it, if not found we find it normally.   [Twitter objects](https://twittercommunity.com/t/api-payloads-to-include-original-quoted-tweet-objects/38184)
   
   ```java
    if(object!=null && StringUtils.isNotBlank(String.valueOf(object)) )
		{
	if(object.get("id")!=null && StringUtils.isNotBlank(String.valueOf(object.get("id")))    
	&& object.get("text")!=null && StringUtils.isNotBlank(String.valueOf(object.get("text"))))
			{
				
			 id = String.valueOf(object.get("id")).trim();
			
			 //calling remove method from specialSymbolsRemover class
			 processedTweet = specialSymbolsRemover.remove(String.valueOf(object.get("text")));
			 
			 
			 //calling remove method from stopWordRemover class
			 processedTweet = stopWordRemover.remove(processedTweet);
			
			 
			//taking words and finding there value in Affin dictionary
			 String []words = processedTweet.split(" ");
			 for(String temp:words)
			 {
				 if(dictionary.containsKey(temp))
				 {
					 sentiment_value += Long.parseLong(dictionary.get(temp));
				 }
		         }
	}
				
	   
else if(json.get("id")!=null && StringUtils.isNotBlank(String.valueOf(json.get("id")))
&&  json.get("text")!=null && StringUtils.isNoneBlank(String.valueOf(json.get("Text")))  )
		{
			id = String.valueOf(json.get("id")).trim();
			
			processedTweet = specialSymbolsRemover.remove(String.valueOf(object.get("text")));
			
			processedTweet = stopWordRemover.remove(processedTweet);
			
			
			 String []words = processedTweet.split(" ");
          			
			for(String temp:words)
			 {
				 if(dictionary.containsKey(temp))
				 {
					 sentiment_value += Long.parseLong(dictionary.get(temp));
				 }
			 }				
		}
			
   
   ```
  
  - Inside Driver we use **addCacheFile** to add our file in distributed cache.
  ```java
  	     try{
			//adding file to distributed cache	
			job.addCacheFile(new URI("hdfs://localhost:9000/cache/AFINN-111.txt"));
		}catch(Exception e)
		 {
				System.out.println("file not added to cache my dear");
				System.exit(1);
		 }
  ```
      
### Execution:
- Exporting jars:
  - Export the project we have made as a jar file. 
    - Right click on project folder -> export -> as jar file.
  - Go inside Hadoop_Home_directory and add the third party jars to the lib directory: **/share/common/hadoop/lib**.
- Distributed cache:   
  - Put  **AFINN-111.txt** file into HDFS from localfile system. 
   ``` putting the file present in Desktop to cache folder present in HDFS
   bin/hdfs dfs  -put   ../Desktop/AFINN-111.txt   /cache
   ```
   - Inside Hadoop_Home_directory go to hadoop directory: **/etc/hadoop** , then type 
   ```
   bin/hdfs dfs -cat core-site.xml
   ```
   this will print the contents of the file core-site.xml which contains the configuration settings of Hadoop core.Here check the value of default fs. 
   //add picture here
   
   
- Start the hadoop services
 `sbin/start-all.sh`

- Check services are running 
`jps`
- Submit the job.
`bin/yarn jar <jar file location>  <Driver class name> <input-path>  <output-path> 

 bin/yarn  jar ../Desktop/sentiment.jar  Analyzer.Driver   /input  /output 
   
  

