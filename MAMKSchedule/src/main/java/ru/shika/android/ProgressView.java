package ru.shika.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import ru.shika.mamkschedule.mamkschedule.R;

/**
 * Simplest custom view possible, using CircularProgressDrawable
 */
public class ProgressView extends View
{

	private CircularProgressDrawable mDrawable;

	public ProgressView(Context context) {
		this(context, null);
	}

	public ProgressView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		mDrawable = new CircularProgressDrawable(getResources().getColor(R.color.light_blue), 4);
		mDrawable.setCallback(this);
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (visibility == VISIBLE) {
			mDrawable.start();
		} else {
			mDrawable.stop();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mDrawable.setBounds(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		mDrawable.draw(canvas);
	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
		return who == mDrawable || super.verifyDrawable(who);
	}

	public void start()
	{
		mDrawable.start();
	}
}