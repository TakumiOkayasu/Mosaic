package com.example.mosaic;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class MosaicImageTouch implements View.OnTouchListener
{
	private ImageView parent;
	public MosaicImageTouch( ImageView parent )
	{
		this.parent = parent;
	}

	@Override
	public boolean onTouch( View v, MotionEvent event )
	{
		v.performClick();
		final var max_x = parent.getWidth();
		final var max_y = parent.getHeight();

		// 画面範囲ではみ出し調整
		var tx = Utility.clamp( 0, ( int ) event.getX(), max_x );
		var ty = Utility.clamp( 0, ( int ) event.getY(), max_y );

		return true;
	}
}
