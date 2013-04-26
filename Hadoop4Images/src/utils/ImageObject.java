package utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import com.googlecode.javacpp.BytePointer;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

public class ImageObject implements Writable {

	private static final Log LOGGER = LogFactory.getLog(ImageObject.class);

	public ImageObject() {
	}

	// Create ImageObject from IplImage
	public ImageObject(IplImage image) {
		this.imgObject = image;
		this.areaSlice = new AreaSlice();
	}
	
	// Create empty ImageObject
	public ImageObject(int height, int width, int depth, int nChannels){
		this.imgObject = cvCreateImage(cvSize(width, height), depth, nChannels);
		this.areaSlice = new AreaSlice();
	}

	// Create ImageObject from IplImage and IplROI
	public ImageObject(IplImage image, AreaSlice window) {
		this.imgObject = image;
		this.areaSlice = window;
	}

	public IplImage getImage() {
		return imgObject;
	}

	// get areaSlice where imgObject came from
	public AreaSlice getRegion() {
		return areaSlice;
	}

	// Pixel depth in bits
	// PL_DEPTH_8U - Unsigned 8-bit integer
	// IPL_DEPTH_8S - Signed 8-bit integer
	// IPL_DEPTH_16U - Unsigned 16-bit integer
	// IPL_DEPTH_16S - Signed 16-bit integer
	// IPL_DEPTH_32S - Signed 32-bit integer
	// IPL_DEPTH_32F - Single-precision floating point
	// IPL_DEPTH_64F - Double-precision floating point
	public int getDepth() {
		return imgObject.depth();
	}

	// Number of channels.
	public int getNumChannel() {
		return imgObject.nChannels();
	}

	// ImageObject height in pixels
	public int getHeight() {
		return imgObject.height();
	}

	// ImageObject width in pixels
	public int getWidth() {
		return imgObject.width();
	}

	// The size of an aligned imgObject row, in bytes
	public int getWidthStep() {
		return imgObject.widthStep();
	}

	// ImageObject data size in bytes.
	public int getImageSize() {
		return imgObject.imageSize();
	}

	/* Copies one imgObject into current imgObject using 
	information contained in AreaSlice Object */
	
	public void insertRegion(ImageObject sourceImage){
		IplImage img1 = this.imgObject;
		IplImage img2 = sourceImage.getImage();
		AreaSlice regionArea = sourceImage.getRegion();
		
		// set the Region of Interest on destination imgObject
		if(regionArea.isParentInfoValid()){
			cvSetImageROI(img1, cvRect(regionArea.getParentXOffset(),
					regionArea.getParentYOffset(),regionArea.getWidth(), regionArea.getHeight()));
		}
		
		// set the Region Of Interest on source imgObject
		if(regionArea.isBorderValid()){
			cvSetImageROI(img2, cvRect(regionArea.getBorderLeft(), regionArea.getBorderTop(), regionArea.getWidth(), regionArea.getHeight()));
		}
		
		cvCopy(img2, img1, null);
		 
		// reset the ROI
		cvResetImageROI(img1);
	}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		// Read imgObject information
		int height = WritableUtils.readVInt(in);
		int width = WritableUtils.readVInt(in);
		int depth = WritableUtils.readVInt(in);
		int nChannels = WritableUtils.readVInt(in);
		int imageSize = WritableUtils.readVInt(in);

		// Read areaSlice information
		int windowXOffest = WritableUtils.readVInt(in);
		int windowYOffest = WritableUtils.readVInt(in);
		int windowHeight = WritableUtils.readVInt(in);
		int windowWidth = WritableUtils.readVInt(in);
		
		int top = WritableUtils.readVInt(in);
		int bottom = WritableUtils.readVInt(in);
		int left = WritableUtils.readVInt(in);
		int right = WritableUtils.readVInt(in);
		
		int h = WritableUtils.readVInt(in);
		int w = WritableUtils.readVInt(in);
		
		areaSlice = new AreaSlice();
		areaSlice.setParentInfo(windowXOffest, windowYOffest, windowHeight,windowWidth);
		areaSlice.setBorder(top, bottom, left, right);
		areaSlice.setWindowSize(h, w);

		// Read imgObject bytes
		byte[] bytes = new byte[imageSize];
		in.readFully(bytes, 0, imageSize);

		imgObject = cvCreateImage(cvSize(width, height), depth, nChannels);
		imgObject.imageData(new BytePointer(bytes));
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// Write imgObject information
		
		WritableUtils.writeVInt(out, imgObject.height());
		WritableUtils.writeVInt(out, imgObject.width());
		WritableUtils.writeVInt(out, imgObject.depth());
		WritableUtils.writeVInt(out, imgObject.nChannels());
		WritableUtils.writeVInt(out, imgObject.imageSize());

		// Write areaSlice information
		WritableUtils.writeVInt(out, areaSlice.getParentXOffset());
		WritableUtils.writeVInt(out, areaSlice.getParentYOffset());
		WritableUtils.writeVInt(out, areaSlice.getParentHeight());
		WritableUtils.writeVInt(out, areaSlice.getParentWidth());
		
		WritableUtils.writeVInt(out, areaSlice.getBorderTop());
		WritableUtils.writeVInt(out, areaSlice.getBorderBottom());
		WritableUtils.writeVInt(out, areaSlice.getBorderLeft());
		WritableUtils.writeVInt(out, areaSlice.getBorderRight());
		
		WritableUtils.writeVInt(out, areaSlice.getHeight());
		WritableUtils.writeVInt(out, areaSlice.getWidth());

		// Write imgObject bytes
		ByteBuffer buffer = imgObject.getByteBuffer();
		while (buffer.hasRemaining()) {
			out.writeByte(buffer.get());
		}
	}
	
	// IPL imgObject
	private IplImage imgObject = null;
	private AreaSlice areaSlice = null;

}
