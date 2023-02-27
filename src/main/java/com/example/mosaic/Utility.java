package com.example.mosaic;

public class Utility
{
	public static int clamp( int min, int value, int max )
	{
		return Math.max( min, Math.min( value, max ) );
	}
}
