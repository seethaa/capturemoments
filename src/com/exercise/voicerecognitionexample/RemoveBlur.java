package com.exercise.voicerecognitionexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class RemoveBlur {

	BitmapFactory.Options opt = new BitmapFactory.Options();
	opt.inDither = true;
	opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
	Bitmap image = BitmapFactory.decodeByteArray(im, 0, im.length);
	int l = CvType.CV_8UC1; //8-bit grey scale image
	Mat matImage = new Mat();
	Utils.bitmapToMat(image, matImage);
	Mat matImageGrey = new Mat();
	Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY);

	Bitmap destImage;
	destImage = Bitmap.createBitmap(image);             
	Mat dst2 = new Mat();
	Utils.bitmapToMat(destImage, dst2);
	Mat laplacianImage = new Mat();
	dst2.convertTo(laplacianImage, l);
	Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U);
	Mat laplacianImage8bit = new Mat();
	laplacianImage.convertTo(laplacianImage8bit, l);

	Bitmap bmp = Bitmap.createBitmap(laplacianImage8bit.cols(),
			laplacianImage8bit.rows(), Bitmap.Config.ARGB_8888);
	Utils.matToBitmap(laplacianImage8bit, bmp);
	int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
	bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(),
			bmp.getHeight());

	int maxLap = -16777216;

	for (int i = 0; i < pixels.length; i++) {
		if (pixels[i] > maxLap)
			maxLap = pixels[i];
	}

	int soglia = -6118750;      

	if (maxLap < soglia || maxLap == soglia) {
		Log.d(MIOTAG, "blur image");
	}

}
