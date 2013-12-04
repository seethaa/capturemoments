package com.exercise.voicerecognitionexample;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	ImageView img1;
	ImageView img2;
	ImageView img3;
	ImageView img4;

	// * @param cacheDir Cache directory path (e.g.: "AppCacheDir",
	// "AppDir/cache/images")
	boolean isBlurred(String imageLocation) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inDither = true;
		opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
		// Bitmap image = BitmapFactory.decodeResource(getResources(),
		// R.drawable.people_blurred_medium);
		File f = new File(imageLocation);
		File f1 = new File(Environment.getExternalStorageDirectory(),
				"Flash/raw/raw01.jpg/");

		Bitmap image = BitmapFactory.decodeFile(f.getAbsolutePath());
		Bitmap image1 = BitmapFactory.decodeFile(f.getAbsolutePath());
		img1.setImageBitmap(image);
		// Bitmap image = BitmapFactory.decodeByteArray(im, 0, im.length);
		int l = CvType.CV_8UC1; // 8-bit grey scale image
		Mat matImage = new Mat();
		Utils.bitmapToMat(image, matImage);
		Mat matImageGrey = new Mat();
		Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY);

		Bitmap destImage;
		destImage = Bitmap.createBitmap(image);
		Mat dst2 = new Mat();
		Utils.bitmapToMat(destImage, dst2);
		img2.setImageBitmap(destImage);
		Mat laplacianImage = new Mat();
		dst2.convertTo(laplacianImage, l);
		Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U);
		Mat laplacianImage8bit = new Mat();
		laplacianImage.convertTo(laplacianImage8bit, l);

		Bitmap bmp = Bitmap.createBitmap(laplacianImage8bit.cols(),
				laplacianImage8bit.rows(), Bitmap.Config.ARGB_8888);

		Utils.matToBitmap(laplacianImage8bit, bmp);
		img3.setImageBitmap(bmp);
		int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
		bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(),
				bmp.getHeight());

		int maxLap = -16777216;

		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] > maxLap)
				maxLap = pixels[i];
		}

		int soglia = -8900000;// -6118750;

		// blurred: -12500671
		// nonblurred: -8487298
		// medium blurred: -8947849
		if (maxLap < soglia || maxLap == soglia) {
			String result = "Blurred";
			Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG)
					.show();
			System.out.println("img is: " + maxLap + " blurred");
			Log.d("DEBUG", "" + maxLap);
			return true;
		} else {
			String result = "Not blurred";
			Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG)
					.show();
			Log.d("DEBUG", "" + maxLap);
			System.out.println("img is: " + maxLap + " not blurred");
			return false;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!OpenCVLoader.initDebug()) {
			// Handle initialization error
			System.out.println("HIMZ: Test ");
		}
		// System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		setContentView(R.layout.activity_main);
		img1 = (ImageView) findViewById(R.id.imageView1);
		img2 = (ImageView) findViewById(R.id.imageView2);
		img3 = (ImageView) findViewById(R.id.imageView3);

		/**
		 * Get all the pics in to the /Flash/raw folder and push the good photos on to /Flash/final folder
		 */
		
		
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Flash/raw/" ;
		String dir1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Flash/good/";
		System.out.println(dir);
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
		String files = new String();
		List<String> fileNames = new ArrayList<String>();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				files = listOfFiles[i].getName();
				System.out.println("----------" + dir + files);
				// Call isBlurred function on the file
				boolean blurred = isBlurred(dir + files);
				if (blurred == false) {
					// Add the file to the final folder and delete from raw
					// folder
					moveToFinal(files, dir, dir1);
				}
			} else {
				fileNames.add(dir + files);
			}
		}
		
		
		
		/*
		isBlurred(android.os.Environment.getExternalStorageDirectory()
				+ "/Flash/raw/raw01.jpg/");*/

	}

	private void moveToFinal(String imageName, String sourceDirectory, String destinationDirectory) {
		// TODO Auto-generated method stub
    	try{
    		 
     	   File afile =new File(sourceDirectory+imageName);
  
     	   if(afile.renameTo(new File(destinationDirectory+imageName))){
     		System.out.println("File is moved successful!");
     	   }else{
     		System.out.println("File is failed to move!");
     	   }
  
     	}catch(Exception e){
     		e.printStackTrace();
     	}
		
	}


}
