package com.example.mosaic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class PreviewRect extends View
{
	private static final Rect PREVIEW_RECT = new Rect( 0, 0, 100, 100 );
	private final Paint paint;

	public PreviewRect( Context context )
	{
		super( context );
		this.paint = new Paint();
	}

	@Override
	protected void onDraw( Canvas canvas )
	{
		paint.setColor( Color.RED );
		canvas.drawRect( PREVIEW_RECT, paint );
	}
}