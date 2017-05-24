package net.aineuron.eagps.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.aineuron.eagps.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Vit Veres on 24-May-17
 * as a part of Android-EAGPS project.
 */

@EViewGroup(R.layout.widget_label_text)
public class IcoLabelTextView extends FrameLayout {

	@ViewById(R.id.label)
	TextView labelView;

	@ViewById(R.id.text)
	TextView textView;

	@ViewById(R.id.icon)
	ImageView iconView;

	private String labelText = "";
	private String text = "";

	private int labelTextColor = 0;
	private int textColor = 0;

	private Drawable iconDrawable = null;

	public IcoLabelTextView(@NonNull Context context) {
		super(context);
	}

	public IcoLabelTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		parseAttrs(attrs);
	}

	public IcoLabelTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		parseAttrs(attrs);
	}

	@TargetApi(21)
	public IcoLabelTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		parseAttrs(attrs);
	}

	@AfterViews
	public void afterViews() {
		refreshUi();
	}

	public String getLabelText() {
		return labelText;
	}

	public void setLabelText(String labelText) {
		this.labelText = labelText;
		refreshUi();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		refreshUi();
	}

	public int getLabelTextColor() {
		return labelTextColor;
	}

	public void setLabelTextColor(int labelTextColor) {
		this.labelTextColor = labelTextColor;
		refreshUi();
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
		refreshUi();
	}

	public Drawable getIconDrawable() {
		return iconDrawable;
	}

	public void setIconDrawable(Drawable iconDrawable) {
		this.iconDrawable = iconDrawable;
		refreshUi();
	}

	private void parseAttrs(AttributeSet attrs) {
		TypedArray a = getContext().getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.IcoLabelTextView,
				0, 0);

		try {
			labelText = a.getString(R.styleable.IcoLabelTextView_ilt_labelText);
			text = a.getString(R.styleable.IcoLabelTextView_ilt_text);
			labelTextColor = a.getColor(R.styleable.IcoLabelTextView_ilt_labelTextColor, getContext().getResources().getColor(R.color.grayText));
			textColor = a.getColor(R.styleable.IcoLabelTextView_ilt_textColor, getContext().getResources().getColor(R.color.grayText));
			iconDrawable = a.getDrawable(R.styleable.IcoLabelTextView_ilt_icoResource);
		} finally {
			a.recycle();
		}
	}

	private void refreshUi() {
		if (labelText != null) {
			labelView.setText(labelText);
		}

		if (text != null) {
			textView.setText(text);
		}

		if (iconDrawable != null) {
			iconView.setImageDrawable(iconDrawable);
		} else {
			iconView.setVisibility(GONE);
		}

		labelView.setTextColor(labelTextColor);
		textView.setTextColor(textColor);
	}
}
