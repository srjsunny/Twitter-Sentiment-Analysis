package analyze;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;



public class Driver {

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "SentimentAnalysis Preprocessing");
		job.setJarByClass(Driver.class);
		job.setMapperClass(TwitterMapper.class);
		job.setNumReduceTasks(0);
		
		job.setMapOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		
		
		try{

			//adding file to distributed cache	
			job.addCacheFile(new URI("hdfs://localhost:9000/cache/AFINN-111.txt"));
			}catch(Exception e)
			{
				System.out.println("file not added my dear");
				System.exit(1);
			}
		
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		System.exit(job.waitForCompletion(true)?0:1);
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
