package com.example.mosaic;

public class Utility
{
	/**
	 * 数値を min <= value <= max に収める
	 * @param min       最小値
	 * @param max       最大値
	 */
	public static int clamp( int min, int value, int max )
	{
		return Math.max( min, Math.min( value, max ) );
	}
	public static float clamp( float min, float value, float max )
	{
		return Math.max( min, Math.min( value, max ) );
	}
}
