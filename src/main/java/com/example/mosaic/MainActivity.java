package com.example.mosaic;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

public class MainActivity extends AppCompatActivity
{
	private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback( this )
	{
		@Override
		public void onManagerConnected( int status )
		{
			if( status == LoaderCallbackInterface.SUCCESS ) {
				Log.i( "OpenCV", "OpenCV loaded successfully" );
			}
			else {
				super.onManagerConnected( status );
			}
		}
	};
	private static final int CROP_SIZE = 100;
	private TextView mosaicLevelText;
	private SeekBar mosaicLevel;
	private Uri imageUri;
	private ImageView selectedImage;
	private ImageView previewImage;

	/**
	 * ROIを指定してモザイク処理
	 *
	 * @param image      元画像となる {@link Mat}
	 * @param rectangles モザイク対象のROIを指定した {@link MatOfRect}
	 * @param size       モザイクのピクセル幅
	 * @return モザイク処理後の {@link Mat}
	 */
	public static Mat drawMosaic( Mat image, MatOfRect rectangles, int size )
	{
		Mat dstImage = image.clone();
		for( Rect rect : rectangles.toArray() ) {
			Mat imageROI = new Mat( image, rect );
			Mat dstImageROI = new Mat( dstImage, rect );

			for( int y = 0 ; y < imageROI.height() ; y += size ) {
				for( int x = 0 ; x < imageROI.width() ; x += size ) {
					int yLimit = y + size;

					if( yLimit >= imageROI.height() ) {
						yLimit = imageROI.height();
					}

					int xLimit = x + size;

					if( xLimit >= imageROI.width() ) {
						xLimit = imageROI.width();
					}

					double b = 0., g = 0., r = 0.;
					int winSize = 0;

					for( int i = y ; i < yLimit ; i++ ) {
						for( int j = x ; j < xLimit ; j++ ) {
							double[] pixel = imageROI.get( j, i );
							b += pixel[ 0 ];
							g += pixel[ 1 ];
							r += pixel[ 2 ];
							winSize++;
						}
					}

					b /= winSize;
					g /= winSize;
					r /= winSize;

					for( int i = y ; i < yLimit ; i++ ) {
						for( int j = x ; j < xLimit ; j++ ) {
							dstImageROI.put( j, i, b, g, r );
						}
					}
				}
			}
		}

		return dstImage;
	}

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		mosaicLevelText = findViewById( R.id.tv_mosaic_level );
		mosaicLevel = findViewById( R.id.sb_mosaic_level );
		selectedImage = findViewById( R.id.iv_selected_img );
		previewImage = findViewById( R.id.iv_preview );

		showMosaicLevel();

		mosaicLevel.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
			{
				showMosaicLevel();
			}

			@Override
			public void onStartTrackingTouch( SeekBar seekBar ) {}

			@Override
			public void onStopTrackingTouch( SeekBar seekBar ) {}
		} );

		// Image Pickerで選択した画像を読み込む
		var pickMedia = registerForActivityResult( new ActivityResultContracts.PickVisualMedia(), uri ->
		{
			// 選択された後の処理
			if( uri != null ) {
				imageUri = uri;
				selectedImage.setImageURI( imageUri );
				previewImage.setImageBitmap(
						Bitmap.createBitmap( ( ( BitmapDrawable ) ( selectedImage.getDrawable() ) ).getBitmap(), 0, 0, CROP_SIZE, CROP_SIZE )
				);
			}
			else {
				// 画像が選択されなければアプリを終了させる
				new AlertDialog
					.Builder( this )
					.setTitle( "お知らせ" )
					.setMessage( "画像が選択されなかったので終了します。" )
					.setPositiveButton( "はい", ( dialog, which ) -> finish()
				).show();
			}
		} );

		// TODO openCVの顔認識して OR タッチした部分にモザイクをかける

		// PhotoPickerを使う。ここに謎の型変換エラーが出るが、問題なく動く。
		var type = ( ActivityResultContracts.PickVisualMedia.VisualMediaType ) ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE;
		pickMedia.launch( new PickVisualMediaRequest.Builder().setMediaType( type ).build() );

		Toast.makeText( this, "画像を選択してください。", Toast.LENGTH_SHORT ).show();
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if( !OpenCVLoader.initDebug() ) {
			Log.d( "OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization" );
			OpenCVLoader.initAsync( OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback );
		}
		else {
			Log.d( "OpenCV", "OpenCV library found inside package. Using it!" );
			mLoaderCallback.onManagerConnected( LoaderCallbackInterface.SUCCESS );
		}
	}

	private void showMosaicLevel()
	{
		var v = String.valueOf( mosaicLevel.getProgress() );
		mosaicLevelText.setText( String.format( "%s×%s", v, v ) );
	}

	private void setTouchArea( int x, int y )
	{
		previewImage.setImageBitmap(
				Bitmap.createBitmap( ( ( BitmapDrawable ) selectedImage.getDrawable() ).getBitmap(), x, y, CROP_SIZE, CROP_SIZE )
		);
	}
}