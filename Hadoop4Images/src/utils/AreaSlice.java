package utils;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import static com.googlecode.javacv.cpp.opencv_core.cvRect;



public class AreaSlice {
	
	// Amount of border
	private int borderTop = -1;
	private int borderBottom = -1;
	private int borderLeft = -1;
	private int borderRight = -1;
	
	// Size of parent image
	private int sourceWidth = -1;
	private int sourceHeight = -1;
	
	// Location of window in source image
	private int sourceXOffset = -1;
	private int sourceYOffset = -1;
	
	private int width = -1;
	private int height = -1;
	
	public AreaSlice(){}
	
		
	public boolean isParentInfoValid(){
		if (sourceWidth < 0 || sourceHeight < 0 || sourceXOffset < 0 || sourceYOffset < 0){
			return false;
		}
		
		return true;
	}
	
	public void setBorder(int borderTop, int borderBottom, int borderLeft, int borderRight){
		this.borderTop = borderTop;
		this.borderBottom = borderBottom;
		this.borderLeft = borderLeft;
		this.borderRight = borderRight;
	}
	
	public boolean isBorderValid(){
		if (borderTop < 0 || borderBottom < 0 || borderLeft < 0 || borderRight < 0){
			return false;
		}
		
		return true;
	}
	
	public void setParentInfo(int parentXOffset, int parentYOffset, int parentHeight, int parentWidth){
		this.sourceWidth = parentWidth;
		this.sourceHeight = parentHeight;
		this.sourceXOffset = parentXOffset;
		this.sourceYOffset = parentYOffset;
	}
	public void setWindowSize(int height, int width){
		this.height = height;
		this.width = width;
	}
	
	public boolean isWindowSizeValid(){
		if (height < 0 || width < 0 ){
			return false;
		}
		
		return true;
	}
	
	public CvRect computeROI(){
		int newX = sourceXOffset - borderLeft;
		int newY = sourceYOffset - borderTop;
		int newWidth = width + borderLeft + borderRight;
		int newHeight = height + borderTop + borderBottom;
		return cvRect(newX, newY, newWidth, newHeight);
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getParentWidth() {
		return sourceWidth;
	}
	
	public int getParentHeight() {
		return sourceHeight;
	}
	
	public int getParentXOffset() {
		return sourceXOffset;
	}
	
	public int getParentYOffset() {
		return sourceYOffset;
	}
	public int getBorderTop() {
		return borderTop;
	}

	public int getBorderBottom() {
		return borderBottom;
	}

	public int getBorderLeft() {
		return borderLeft;
	}

	public int getBorderRight() {
		return borderRight;
	}
}
