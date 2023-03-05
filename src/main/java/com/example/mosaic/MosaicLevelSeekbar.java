package com.example.mosaic;

import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class MosaicLevelSeekbar implements SeekBar.OnSeekBarChangeListener, GetMosaicLevel
{
	private int progress;
	private TextView view;

	public MosaicLevelSeekbar( TextView view )
	{
		this.progress = 0;
		this.view = view;
		this.view.setText( String.valueOf( getLevel() ) );
	}

	@Override
	public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
	{
		this.progress = progress;
		this.view.setText( String.valueOf( getLevel() ) );
	}

	@Override
	public void onStartTrackingTouch( SeekBar seekBar )
	{
	}

	@Override
	public void onStopTrackingTouch( SeekBar seekBar )
	{
	}

	@Override
	public int getLevel()
	{
		return this.progress;
	}
}
