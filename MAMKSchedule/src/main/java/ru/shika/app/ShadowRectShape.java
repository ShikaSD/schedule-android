package ru.shika.app;

import android.graphics.*;
import android.graphics.drawable.shapes.RectShape;

public class ShadowRectShape extends RectShape
{
	private static final int KEY_SHADOW_COLOR = 0x4E000000;
	private static final int FILL_SHADOW_COLOR = 0x3D000000;

	private LinearGradient mGradient;
	private int mShadowRadius;
	private Paint mShadowPaint;

	public ShadowRectShape(int shadowRadius, float width, float height) {
		super();
		mShadowPaint = new Paint();
		mShadowRadius = shadowRadius;
		/*mGradient = new LinearGradient(width, height, width + mShadowRadius, new int[] {
			FILL_SHADOW_COLOR, Color.TRANSPARENT
		}, null, Shader.TileMode.CLAMP);
		mShadowPaint.setShader(mGradient);*/
	}
}
