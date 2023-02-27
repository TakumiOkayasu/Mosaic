package com.example.mosaic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ortiz.touchview.TouchImageView;

public class CustomTouchImageView extends TouchImageView
{
	public static final float STROKE_WIDTH = 10.f;
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

	private void drawTouchArea( Canvas canvas )
	{
		if( touchPointLister == null ) {
			return;
		}

		var paint = new Paint();
		var pos = touchPointLister.getPoint(); // 触った場所
		Log.d( "AppDebug", String.format( "touch!( x, y ) = ( %d, %d )", pos.x, pos.y ) );
		final var RECT_SIZE = PreviewRect.CROP_SIZE / 2;
		Point min = new Point( pos.x - RECT_SIZE, pos.y - RECT_SIZE );
		Point max = new Point( pos.x + RECT_SIZE, pos.y + RECT_SIZE );
		Rect rect = new Rect( min.x, min.y, max.x, max.y );
		Log.d( "AppDebug", String.format( "Rect( w, h ) = ( %d, %d )", rect.width(), rect.height() ) );
		// 色・太さ・塗りつぶさない設定
		paint.setColor( Color.BLUE );
		paint.setStrokeWidth( STROKE_WIDTH );
		paint.setStyle( Paint.Style.STROKE );

		canvas.drawRect( rect, paint );
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
