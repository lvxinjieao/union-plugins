package com.u8.sdk.impl.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.u8.sdk.utils.ResourceHelper;

public class ExtendEditText extends EditText {
    private Drawable mRightDrawable;
    private boolean isHasFocus;
    private boolean isVisb = false;
    public OndelTouched OndelTouched;

    public ExtendEditText(Context context) {
        super(context);
        if (!isInEditMode())
            init();
    }

    public ExtendEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode())
            init();
    }

    public ExtendEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode())
            init();
    }

    private void init() {
        this.mRightDrawable = ResourceHelper.getDrawable(this.getContext(), "R.drawable.x_common_input_clear");
        this.mRightDrawable.setBounds(0, 0, this.mRightDrawable.getMinimumWidth(),
                this.mRightDrawable.getMinimumHeight());

        setOnFocusChangeListener(new FocusChangeListenerImpl());

        addTextChangedListener(new TextWatcherImpl());

        setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                setClearDrawableVisible(false);
                return false;
            }
        });

        setClearDrawableVisible(false);
        setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_DONE);
    }

    public void setnextedit(final ExtendEditText editText) {
        setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_NEXT);
        setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int arg1, KeyEvent arg2) {
                if (arg1 == 5) {
                    editText.setFocusable(true);
                    editText.setFocusableInTouchMode(true);
                    editText.requestFocus();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (this.isVisb) {
                    boolean isClean = (event.getX() > getWidth() - getTotalPaddingRight())
                            && (event.getX() < getWidth() - getPaddingRight());
                    if (isClean) {
                        setText("");
                        if (this.OndelTouched != null)
                            this.OndelTouched.onItemOntouch();
                    }
                }
                boolean isVisible = getText().toString().length() >= 1;
                setClearDrawableVisible(isVisible);
                performClick();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return super.onTouchEvent(event);
    }

    public void setOndelTouched(OndelTouched OnDelTouched) {
        this.OndelTouched = OnDelTouched;
    }

    public void setClearDrawableVisible(boolean isVisible) {
        Drawable rightDrawable = null;
        if (isVisible) {
            if (!this.isVisb) {
                rightDrawable = this.mRightDrawable;
                this.isVisb = true;
            } else {
                return;
            }
        } else {
            if (this.isVisb) {
                rightDrawable = null;
                this.isVisb = false;
            } else {
                return;
            }
        }
        setCompoundDrawables(null, null, rightDrawable, null);
    }

    public void setShakeAnimation() {
        setAnimation(shakeAnimation(5));
    }

    public Animation shakeAnimation(int CycleTimes) {
        Animation translateAnimation = new TranslateAnimation(0.0F, 10.0F, 0.0F, 10.0F);
        translateAnimation.setInterpolator(new CycleInterpolator(CycleTimes));
        translateAnimation.setDuration(1000L);
        return translateAnimation;
    }

    private class FocusChangeListenerImpl implements OnFocusChangeListener {
        private FocusChangeListenerImpl() {
        }

        public void onFocusChange(View v, boolean hasFocus) {
            ExtendEditText.this.isHasFocus = hasFocus;
            if (ExtendEditText.this.isHasFocus) {
                boolean isVisible = ExtendEditText.this.getText().toString().length() >= 1;
                ExtendEditText.this.setClearDrawableVisible(isVisible);
            } else {
                ExtendEditText.this.setClearDrawableVisible(false);
            }
        }
    }

    public static abstract interface OndelTouched {
        public abstract void onItemOntouch();
    }

    private class TextWatcherImpl implements TextWatcher {
        private TextWatcherImpl() {
        }

        public void afterTextChanged(Editable s) {
            if (ExtendEditText.this.isHasFocus) {
                boolean isVisible = ExtendEditText.this.getText().toString().length() >= 1;
                ExtendEditText.this.setClearDrawableVisible(isVisible);
            } else {
                ExtendEditText.this.setClearDrawableVisible(false);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}