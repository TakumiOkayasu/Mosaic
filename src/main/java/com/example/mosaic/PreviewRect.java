package com.example.mosaic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class PreviewRect extends AppCompatImageView
{
	private static final float STROKE_WIDTH = 10.f;
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

		drawPreviewRect( canvas );
	}

	private void drawPreviewRect( Canvas canvas )
	{
		var rect = new Rect( 5, 5, 250, 250 );
		var paint = new Paint();

		paint.setColor( Color.RED );
		paint.setStrokeWidth( STROKE_WIDTH );
		paint.setStyle( Paint.Style.STROKE );

		canvas.drawRect( rect, paint );
	}
}