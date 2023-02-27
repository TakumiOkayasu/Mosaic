package com.example.mosaic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ortiz.touchview.TouchImageView;

public class CustomTouchImageView extends TouchImageView
{
	private OnTouchPointLister touchPointLister = null;

	public CustomTouchImageView( @NonNull Context context, @Nullable AttributeSet attrs )
	{
		super( context, attrs );
	}

	public CustomTouchImageView( @NonNull Context context )
	{
		super( context );
	}

	public CustomTouchImageView( @NonNull Context context, @Nullable AttributeSet attrs, int defStyle )
	{
		super( context, attrs, defStyle );
	}

	public void setTouchPointLister( OnTouchPointLister l )
	{
		if( touchPointLister != null ) {
			this.touchPointLister = l;
		}
	}

	private void drawTouchArea( Canvas ignoredCanvas )
	{
		var pos = touchPointLister.getPoint(); // 触った場所
		Log.d( "AppDebug", String.format( "touch ( %d, %d )", pos.x, pos.y ) );
	}

	@Override
	protected void onDraw( @NonNull Canvas canvas )
	{
		super.onDraw( canvas );
		drawTouchArea( canvas );
	}

	public interface OnTouchPointLister
	{
		Point getPoint();
	}
}
