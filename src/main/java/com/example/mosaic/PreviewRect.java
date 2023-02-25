package com.example.mosaic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PreviewRect extends androidx.appcompat.widget.AppCompatImageView
{
	private static final float STROKE_WIDTH = 20.f;
	private final Rect rect = new Rect( 10, 10, 251, 251 );
	private final Paint paint = new Paint();

	public PreviewRect( Context context )
	{
		super( context );
	}

	public PreviewRect( Context context, AttributeSet attrs )
	{
		super( context, attrs );
	}

	public PreviewRect( Context context, AttributeSet attrs, int defStyleAttr )
	{
		super( context, attrs, defStyleAttr );
	}

	@Override
	protected void onDraw( Canvas canvas )
	{
		super.onDraw( canvas );
		paint.setColor( Color.RED );
		paint.setStrokeWidth( STROKE_WIDTH );
		paint.setStyle( Paint.Style.STROKE );

		canvas.drawRect( rect, paint );
	}


}