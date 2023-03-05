package com.example.mosaic;

import android.annotation.SuppressLint;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MosaicModeSwitch implements CompoundButton.OnCheckedChangeListener
{
	private String switchText;
	private Switch mode;

	// ここはインターフェース経由でもらうようにあとで直す
	public MosaicModeSwitch( @SuppressLint( "UseSwitchCompatOrMaterialCode" ) Switch mode )
	{
		this.mode = mode;
		switchText = SwitchString.watchingMode.getText();
		this.mode.setText( switchText );
	}

	@Override
	public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
	{
		if( isChecked ) {
			switchText = SwitchString.mosaicMode.getText();
		}
		else {
			switchText = SwitchString.watchingMode.getText();
		}

		// モードを表示させる
		mode.setText( switchText );
	}

	private enum SwitchString
	{
		mosaicMode( "モザイクかけるモード" ),
		watchingMode( "みるだけモード" );
		private String now;

		SwitchString( String s )
		{
			this.now = s;
		}

		public String getText()
		{
			return this.now;
		}
	}
}
