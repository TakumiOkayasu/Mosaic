package com.example.mosaic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
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
	private static final String LOG_TAG = "AppDebug";
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
	private ImageView selectedImage;
	private PreviewRect previewImage;
	private Button redoButton;
	@SuppressLint( "UseSwitchCompatOrMaterialCode" )
	private Switch mosaicMode;

	// アクセシビリティワーニングを黙らせる
	@SuppressLint( "ClickableViewAccessibility" )
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		mosaicLevelText = findViewById( R.id.tv_mosaic_level );
		mosaicLevel = findViewById( R.id.sb_mosaic_level );
		selectedImage = findViewById( R.id.iv_selected_img );
		previewImage = findViewById( R.id.pr_preview );
		mosaicMode = findViewById( R.id.sw_mosaic );
		redoButton = findViewById( R.id.btn_redo );

		mosaicMode.setOnClickListener( v -> mosaicMode.setText( mosaicMode.isChecked() ? "モザイクかけるモード" : "みるだけモード" ) );

		mosaicMode.setOnCheckedChangeListener( ( buttonView, isChecked ) -> {
			if( isChecked ) {
				// ここにモザイクをかける処理を書く

				Log.d( LOG_TAG, "Switch Changed!" );
			}
		} );

		// redoをする処理を書く
		redoButton.setOnClickListener( v -> {} );

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


		// 画像内タッチ
		selectedImage.setOnTouchListener( ( v, event ) ->
		{
			v.performClick();
			var displaySize = getDisplaySize( this );
			var imgSize = selectedImage.getDrawable().getBounds();

			Log.d( LOG_TAG, String.format( "ImgRect[%d, %d][%d, %d]", imgSize.top, imgSize.left, imgSize.bottom, imgSize.right ) );
			Log.d( LOG_TAG, String.format( "w, h[%d, %d]", selectedImage.getWidth(), selectedImage.getHeight() ) );

			// 画面上→画像上の座標に変換
			int tx = ( int ) ( event.getX() * Math.max( ( float ) imgSize.right / displaySize.x, 1.f ) );
			int ty = ( int ) ( event.getY() * Math.max( ( float ) imgSize.bottom / displaySize.y, 1.f ) );

			Log.d( LOG_TAG, String.format( "tx, ty[%d, %d]", tx, ty ) );


			// 画像外に出ないように調整
			int x = Utility.clamp( 0, tx, imgSize.width() - PreviewRect.CROP_SIZE );
			int y = Utility.clamp( 0, ty, imgSize.height() - PreviewRect.CROP_SIZE );

			setPreview( new Point( x, y ) );

			return true;
		} );

		// PhotoPickerで選択した画像を読み込む
		var pickMedia = registerForActivityResult( new ActivityResultContracts.PickVisualMedia(), uri ->
		{
			// 選択された後の処理
			if( uri != null ) {
				imageUri = uri;
				selectedImage.setImageURI( imageUri );
				setPreview( new Point( 0, 0 ) );
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

		// PhotoPickerを使う。ここに謎の型変換エラーメッセージが出るが、問題なく動く。
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

	// 中心座標をもらい、描画する
	private void setPreview( Point leftTop )
	{
		// プレビュー画面に画像をセット
		previewImage.setImageBitmap(
				Bitmap.createBitmap( ( ( BitmapDrawable ) selectedImage.getDrawable() ).getBitmap(), leftTop.x, leftTop.y, PreviewRect.CROP_SIZE, PreviewRect.CROP_SIZE )
		);
	}
}