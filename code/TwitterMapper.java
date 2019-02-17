package analyze;


import java.io.*;
import java.util.*;
import java.net.URI;


import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import org.apache.commons.lang3.StringUtils;

public class TwitterMapper extends Mapper<LongWritable,Text,NullWritable,Text> {

	JSONParser parser = null;
	Map<String,String> dictionary = null;
	
	
	@Override
	protected void setup(Context context)throws IOException,InterruptedException
	{
		parser = new JSONParser();
		dictionary = new HashMap<String,String>();
		
		
		
			URI[] cacheFiles = context.getCacheFiles();
		
			if (cacheFiles != null && cacheFiles.length > 0)
			  {
			    try
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
			    
			    
			    
			    }catch(Exception e)
			    {
			    System.out.println("Unable to read the cached filed");
			    System.exit(1);
			    }
			  }
			
		}
		
		
		
	
	
	@Override
	public void map(LongWritable key, Text value,Context context) throws IOException,InterruptedException
	{
		long sentiment_value = 0;
		JSONObject json = null; 
		try{
			
			json = (JSONObject) parser.parse(value.toString());
		    JSONObject object = (JSONObject)json.get("quoted_tweet");
			String id=null;
			String processedTweet= null;
		    
		    
		    
		    //if found quoted_tweet object and not null
			if(null != object && StringUtils.isNotBlank(String.valueOf(object)))
			{
				
				if(object.get("id") !=null &&  object.get("text")!=null
						&& StringUtils.isNotBlank(String.valueOf(object.get("id")))
						&& StringUtils.isNotBlank(String.valueOf(object.get("text"))))
				{
					
					id = String.valueOf(object.get("id")).trim();
					processedTweet = specialSymbolRemover.remove(String.valueOf(object.get("text")));
					
					processedTweet = stopWordsRemover.remove(processedTweet);
					
					String []words = processedTweet.split(" ");
					
					for(String temp:words)
					{
						if(dictionary.containsKey(temp))
						{
							sentiment_value+=Long.parseLong(dictionary.get(temp));
						}
					}
				
				
				
				
				}
			}
			
			else if (json.get("id")!=null && json.get("text")!=null
					&& StringUtils.isNotBlank(String.valueOf(json.get("id")))
					&& StringUtils.isNotBlank(String.valueOf(json.get("text"))))
			{
				
				id = String.valueOf(json.get("id")).trim();
				
				processedTweet = specialSymbolRemover.remove(String.valueOf(json.get("text")));
				
				processedTweet = stopWordsRemover.remove(processedTweet);
				
				String []words = processedTweet.split(" ");
				
				for(String temp:words)
				{
					if(dictionary.containsKey(temp))
					{
						sentiment_value+=Long.parseLong(dictionary.get(temp));
						
					}
					
					
				}
				
				
			}
				
		
		//making one final check 	
		if(StringUtils.isNotBlank(id) && StringUtils.isNotBlank(processedTweet))
		{
			
			context.write(NullWritable.get(),new Text(id+"\t"+processedTweet+"\t"+sentiment_value));
			
		}
			
				
				
				
	    }catch(ParseException e)
		{
			e.printStackTrace();
		}
		
		
		
		
	
		
		
		
	}
	
	
	
	
}
