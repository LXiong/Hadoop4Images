package sobel2;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import utils.ImageObject;
import utils.ImageInputFormat;
import utils.ImageOutputFormat;

public class Sobel2 extends Configured implements Tool {
	public static class Map extends
			Mapper<LongWritable, ImageObject, LongWritable, ImageObject> {

		@Override
		public void map(LongWritable key, ImageObject value, Context context)
				throws IOException, InterruptedException {

			IplImage src = value.getImage();
			IplImage dest = cvCreateImage(cvSize(src.width(), src.height()), 
					IPL_DEPTH_16S, src.nChannels());
			
			cvSobel( src, dest, 1, 0, 3);
			
			cvConvertScale(dest, src, 1.0, 0.0);
			
			context.write(key, value);
		}
	}

	public static class Reduce extends
			Reducer<LongWritable, ImageObject, LongWritable, ImageObject> {

		@Override
		public void reduce(LongWritable key, Iterable<ImageObject> values,
				Context context) throws IOException, InterruptedException {

			// Sum the parts
			Iterator<ImageObject> it = values.iterator();
			ImageObject img = null;
			ImageObject part = null;
			while (it.hasNext()) {
				part = (ImageObject) it.next();
				if(img == null){
					int height = part.getHeight();
					int width = part.getWidth();
					if(part.getRegion().isParentInfoValid()){
						height = part.getRegion().getParentHeight();
						width = part.getRegion().getParentWidth();
					}
					int depth = part.getDepth();
					int nChannel = part.getNumChannel();
					img = new ImageObject(height, width, depth, nChannel);
				}
				img.insertRegion(part);
			}

			context.write(key, img);
		}
	}

	public int run(String[] args) throws Exception {
		// Set various configuration settings
		Configuration conf = getConf();
		conf.setInt("mapreduce.imagerecordreader.windowsizepercent", 100);
		conf.setInt("mapreduce.imagerecordreader.borderPixel", 0);
		conf.setInt("mapreduce.imagerecordreader.iscolor", 0);
		
		// Create job
		Job job = new Job(conf);
		
		// Specify various job-specific parameters
		job.setJarByClass(Sobel2.class);
		job.setJobName("Sobel2");

		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(ImageObject.class);

		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setInputFormatClass(ImageInputFormat.class);
		job.setOutputFormatClass(ImageOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new Sobel2(), args);
		System.exit(res);
	}
}