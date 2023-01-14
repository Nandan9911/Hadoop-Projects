import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Q2_2 extends Configured implements Tool{
	

	
	public static class MaxFlightMapper
     extends Mapper<LongWritable, Text, Text, Text>{

String origin;
String destination;
String year;
String[] values;

 public void map(LongWritable key, Text value, Context context
                 ) throws IOException, InterruptedException {
   
   values = value.toString().split(",");
   origin = values[17];
   destination = values[18];
   year = values[1];
   context.write(new Text(origin),new Text(year+":"+origin+":"+destination));
   
 }
}


public static class MaxFlightReducer
    extends Reducer<Text,Text,Text,IntWritable> {

 static long count;
 static int max;
 static String airportCode;
 static List<String> routes=new ArrayList<>();
 
 public void reduce(Text key, Iterable<Text> values,Context context) throws IOException, InterruptedException {
   
   int sum=0;
   String s;
   for(Text t:values)
   {
	  s=t.toString();
	  routes.add(s);
	  if(s.substring(0,s.indexOf(":")).equals("2004"))
	  {
		  sum++;
	  }
   }
   
   if(sum>max)
   {
	   max=sum;
	   airportCode=key.toString();
   }
   
 }
 
 
 public void cleanup(Context context) throws IOException, InterruptedException
 {
	 int totalFlights=0;
	 for(String s:routes)
	 {
		 if(s.contains(airportCode))
			 totalFlights++;
	 }
	 context.write(new Text(airportCode), new IntWritable(totalFlights));
 }
 
}

public int run(String[] args) throws Exception
{
	 Configuration conf = new Configuration();
	 conf.set("mapreduce.output.textoutputformat.separator",":");
	 Job job = Job.getInstance(conf, "unique routes count");
	 job.setJarByClass(Q2_2.class);
	 job.setMapperClass(MaxFlightMapper.class);
	 job.setReducerClass(MaxFlightReducer.class);
	 job.setNumReduceTasks(1);
	 job.setMapOutputKeyClass(Text.class);
	 job.setMapOutputValueClass(Text.class);
	 job.setOutputFormatClass(TextOutputFormat.class);
	 job.setOutputKeyClass(Text.class);
	 job.setOutputValueClass(IntWritable.class);
	 FileInputFormat.addInputPath(job, new Path(args[0]));
	 FileOutputFormat.setOutputPath(job, new Path(args[1]));
	 System.exit(job.waitForCompletion(true) ? 0 : 1);
	 return 0;
}



public static void main(String[] args) throws Exception {
	ToolRunner.run(new Configuration(), new Q2_2(),args);
    System.exit(0);
}
}
