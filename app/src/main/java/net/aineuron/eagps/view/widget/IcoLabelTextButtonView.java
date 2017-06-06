package net.aineuron.eagps.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import net.aineuron.eagps.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Vit Veres on 06-Jun-17
 * as a part of Android-EAGPS project.
 */

@EViewGroup(R.layout.widget_label_text_button)
public class IcoLabelTextButtonView extends ConstraintLayout {

	@ViewById(R.id.label)
	TextView labelView;

	@ViewById(R.id.text)
	TextView textView;

	@ViewById(R.id.icon)
	ImageView iconView;

	@ViewById(R.id.textButton)
	TextView extendedDescriptionView;

	private String labelText = "";
	private String text = "";

	private int labelTextColor = 0;
	private int textColor = 0;

	private int icoSize = 0;
	private float labelTextSize = 0;
	private float textSize = 0;
	private boolean isExtendedDescription;
	private Drawable iconDrawable = null;

	public IcoLabelTextButtonView(@NonNull Context context) {
		super(context);
	}

	public IcoLabelTextButtonView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		parseAttrs(attrs);
	}

	public IcoLabelTextButtonView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		parseAttrs(attrs);
	}

	@AfterViews
	public void afterViews() {
		refreshUi();
		setListener();
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

	public int getIcoSize() {
		return icoSize;
	}

	public void setIcoSize(int icoSize) {
		this.icoSize = icoSize;
	}

	public float getLabelTextSize() {
		return labelTextSize;
	}

	public void setLabelTextSize(float labelTextSize) {
		this.labelTextSize = labelTextSize;
	}

	public float getTextSize() {
		return textSize;
	}

	public void setTextSize(float textSize) {
		this.textSize = textSize;
	}

	public boolean isExtendedDescription() {
		return isExtendedDescription;
	}

	public void setExtendedDescription(boolean extendedDescription) {
		isExtendedDescription = extendedDescription;
		setListener();
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
			icoSize = a.getDimensionPixelSize(R.styleable.IcoLabelTextView_ilt_icoSize, 0);
			labelTextSize = a.getDimension(R.styleable.IcoLabelTextView_ilt_labelTextSize, 0);
			textSize = a.getDimension(R.styleable.IcoLabelTextView_ilt_textSize, 0);
		} finally {
			a.recycle();
		}
	}

	private void refreshUi() {
		if (labelText != null) {
			labelView.setText(labelText);
		} else {
			labelView.setVisibility(GONE);
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

		// Size
		if (icoSize > 0) {
			ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
			layoutParams.height = icoSize;
			layoutParams.width = icoSize;
			iconView.requestLayout();
		}

		if (labelTextSize > 0) {
			labelView.setTextSize(labelTextSize);
		}

		if (textSize > 0) {
			textView.setTextSize(textSize);
			extendedDescriptionView.setTextSize(textSize);
		}

		if (isExtendedDescription) {
			textView.setVisibility(GONE);
			extendedDescriptionView.setVisibility(VISIBLE);
		} else {
			textView.setVisibility(VISIBLE);
			extendedDescriptionView.setVisibility(GONE);
		}

	}

	private void setListener() {
		if (isExtendedDescription) {
			this.setOnClickListener(v -> new MaterialDialog
					.Builder(getContext())
					.title("Limit detail")
					.content(text)
					.positiveText("OK")
					.show()
			);
		} else {
			this.setOnClickListener(null);
		}
	}
}
