package com.example.mosaic;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Point;
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

import com.ortiz.touchview.TouchImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

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
	private TextView mosaicLevelText;
	private SeekBar mosaicLevel;
	private Uri imageUri;
	private TouchImageView selectedImage;
	private ImageView previewImage;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		mosaicLevelText = findViewById( R.id.tv_mosaic_level );
		mosaicLevel = findViewById( R.id.sb_mosaic_level );
		selectedImage = findViewById( R.id.tiv_selected_img );
		previewImage = findViewById( R.id.pr_preview );

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

		// PhotoPickerで選択した画像を読み込む
		var pickMedia = registerForActivityResult( new ActivityResultContracts.PickVisualMedia(), uri ->
		{
			// 選択された後の処理
			if( uri != null ) {
				imageUri = uri;
				selectedImage.setImageURI( imageUri );
				setPreview( 0, 0 );
				Log.d( "AppDebug", String.format( "imgSize = ( %d, %d )", selectedImage.getWidth(), selectedImage.getWidth() ) );
			}
			else {
				// 画像が選択されなければアプリを終了させる
				new AlertDialog
						    .Builder( this )
						.setTitle( "お知らせ" )
						.setMessage( "画像が選択されなかったので終了します。" )
						.setPositiveButton( "はい", ( dialog, which ) -> finish() )
						.show();
			}
		} );

		// TODO openCVの顔認識して OR タッチした部分にモザイクをかける

		// PhotoPickerを使う。ここに謎の型変換エラーが出るが、問題なく動く。
		var type = ( ActivityResultContracts.PickVisualMedia.VisualMediaType ) ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE;
		pickMedia.launch( new PickVisualMediaRequest.Builder().setMediaType( type ).build() );

		// 画像内のタッチイベント
		findViewById( R.id.tiv_selected_img ).setOnTouchListener( ( v, event ) ->
		{
			// 呼ばないとアノテーションつけろってうるさい
			v.performClick();

			var size = getDisplaySize( this );

			// TODO 画面サイズー＞画像サイズに対応するメソッドを作成する
			int x = Math.max( 0, Math.min( ( int ) event.getX(), size.x ) );
			int y = Math.max( 0, Math.min( ( int ) event.getY(), size.y ) );

			Log.i( "AppDebug", String.format( "( x, y ) = ( %d, %d )", x, y ) );

			setPreview( x, y );
			return true;
		} );

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

	private void setPreview( int x, int y )
	{
		var size = getDisplaySize( this );

		x = Math.max( 0, Math.min( x + 100, size.x ) );
		y = Math.max( 0, Math.min( y - 100, size.y ) );
		final int CROP_SIZE = 500;

		previewImage.setImageBitmap(
				Bitmap.createBitmap( ( ( BitmapDrawable ) selectedImage.getDrawable() ).getBitmap(), x, y, CROP_SIZE, CROP_SIZE )
		);
	}

	private Point getDisplaySize( Activity activity )
	{
		var windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
		var h = windowMetrics.getBounds().height();
		var w = windowMetrics.getBounds().width();

		return new Point( w, h );
	}

}