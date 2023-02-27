package com.example.mosaic;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

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
	private CustomTouchImageView selectedImage;
	private PreviewRect previewImage;
	private Point point = new Point();

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		mosaicLevelText = findViewById( R.id.tv_mosaic_level );
		mosaicLevel = findViewById( R.id.sb_mosaic_level );
		selectedImage = findViewById( R.id.tiv_selected_img );
		previewImage = findViewById( R.id.pr_preview );

		// 最初の一回はTextViewが表示されないので呼んであげる
		showMosaicLevel();

		// モザイクの大きさを変更する
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

		// タッチされた場所を通知するTouchImageViewの拡張クラスにあるインターフェース
		selectedImage.setTouchPointLister( () -> this.point );

		// 画像内タッチイベント
		findViewById( R.id.tiv_selected_img ).setOnTouchListener( ( v, event ) ->
		{
			v.performClick();
			var size = getDisplaySize( this );

			// TODO 画面サイズー＞画像サイズに対応するメソッドを作成する
			int x = Math.max( 0, Math.min( ( int ) event.getX(), size.x ) );
			int y = Math.max( 0, Math.min( ( int ) event.getY(), size.y ) );

			setPreview( x, y );

			return true;
		} );

		// PhotoPickerで選択した画像を読み込む
		var pickMedia = registerForActivityResult( new ActivityResultContracts.PickVisualMedia(), uri ->
		{
			// 選択された後の処理
			if( uri != null ) {
				imageUri = uri;
				selectedImage.setImageURI( imageUri );
				setPreview( 0, 0 );

				// Debug
				{
					var d = ( ( BitmapDrawable ) selectedImage.getDrawable() ).getBitmap();
					Log.d( "AppDebug", String.format( "Image   ( h, w ) = ( %d, %d )", d.getHeight(), d.getWidth() ) );
					var dsp = getDisplaySize( this );
					Log.d( "AppDebug", String.format( "Display ( x, y ) = ( %d, %d )", dsp.x, dsp.y ) );
				}
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

	private Point getDisplaySize( Activity activity )
	{
		var windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
		var h = windowMetrics.getBounds().height();
		var w = windowMetrics.getBounds().width();

		return new Point( w, h );
	}

	private void setPreview( int x, int y )
	{
		var display = getDisplaySize( this );           // ディスプレイサイズ
		var image = selectedImage.getDrawable().getBounds();    // 画像サイズ

		this.point.set( x, y );

		// プレビュー画面に画像をセット
		previewImage.setImageBitmap(
				Bitmap.createBitmap( ( ( BitmapDrawable ) selectedImage.getDrawable() ).getBitmap(), x, y, PreviewRect.CROP_SIZE, PreviewRect.CROP_SIZE )
		);
	}
}