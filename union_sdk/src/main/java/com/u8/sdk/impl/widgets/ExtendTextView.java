package com.u8.sdk.impl.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

public class ExtendTextView extends TextView {

	public ExtendTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ExtendTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ExtendTextView(Context context) {
		super(context);
	}

	protected void onDraw(Canvas canvas) {
		Drawable drawables[] = getCompoundDrawables();
		if (drawables != null) {
			Drawable drawableLeft = drawables[0];
			if (drawableLeft != null) {
				float textWidth = getPaint().measureText(getText().toString());
				int drawablePadding = getCompoundDrawablePadding();
				int drawableWidth = 0;
				drawableWidth = drawableLeft.getIntrinsicWidth();
				float bodyWidth = textWidth + (float) drawableWidth + (float) drawablePadding;
				canvas.translate(((float) getWidth() - bodyWidth) / 2.0F, 0.0F);
			}
		}
		super.onDraw(canvas);
	}
}
