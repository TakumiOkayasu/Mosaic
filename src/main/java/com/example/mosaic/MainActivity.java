package com.example.mosaic;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
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
	private Uri imageUri;

	// アクセシビリティワーニングを黙らせる
	@SuppressLint( "ClickableViewAccessibility" )
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		SeekBar mosaicLevel = findViewById( R.id.sb_mosaic_level );
		ImageView selectedImage = findViewById( R.id.iv_selected_img );
		@SuppressLint( "UseSwitchCompatOrMaterialCode" )
		Switch mosaicMode = findViewById( R.id.sw_mosaic );
		Button undoButton = findViewById( R.id.btn_undo );

		// モザイクモードと閲覧モードの切り替えスイッチ
		mosaicMode.setOnCheckedChangeListener( new MosaicModeSwitch( mosaicMode ) );
		// redoをするボタン処理
		undoButton.setOnClickListener( new MosaicUnDo() );

		// モザイクの大きさを変更するシークバー登録
		var mosaicSeekBar = new MosaicLevelSeekbar( findViewById( R.id.tv_mosaic_level ) );
		mosaicLevel.setOnSeekBarChangeListener( mosaicSeekBar );

		// 画像内タッチ
		selectedImage.setOnTouchListener( ( v, event ) ->
		{
			v.performClick();
			final var max_x = selectedImage.getWidth();
			final var max_y = selectedImage.getHeight();

			// 画面範囲ではみ出し調整
			var tx = Utility.clamp( 0, ( int ) event.getX(), max_x );
			var ty = Utility.clamp( 0, ( int ) event.getY(), max_y );

			Log.d( LOG_TAG, String.format( "touch( %d, %d )", tx, ty ) );
			return true;
		} );

		// PhotoPickerで選択した画像を読み込む
		var pickMedia = registerForActivityResult(
				new ActivityResultContracts.PickVisualMedia(),
				uri ->
				{
					// 選択された後の処理
					if( uri != null ) {
						imageUri = uri;
						selectedImage.setImageURI( imageUri );
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
				}
		);

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
}