package ear.life.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ear.life.R;


public class AlertDialog {
	private Context context;
	private Dialog dialog;
	private LinearLayout lLayout_bg;
	private TextView txt_title;
	private RelativeLayout rl_view;
	private TextView txt_msg;
	private Button btn_neg;
	private Button btn_pos;
	private ImageView img_line;
	private ImageView iv_alertdialog_line;
	private Display display;
	private boolean showTitle = false;
	private boolean showView = false;
	private boolean showMsg = false;
	private boolean showPosBtn = false;
	private boolean showNegBtn = false;

	public AlertDialog(Context context) {
		this.context = context;
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		display = windowManager.getDefaultDisplay();
	}

	public AlertDialog builder() {
		return builder((int) (display.getWidth() * 0.85));
	}

	public AlertDialog builder(int width) {
		// 获取Dialog布局
		View view = LayoutInflater.from(context).inflate(R.layout.view_alertdialog, null);

		// 获取自定义Dialog布局中的控件
		lLayout_bg = (LinearLayout) view.findViewById(R.id.lLayout_bg);
		txt_title = (TextView) view.findViewById(R.id.txt_title);
		txt_title.setVisibility(View.GONE);
		rl_view = (RelativeLayout) view.findViewById(R.id.rl_view);
		rl_view.setVisibility(View.GONE);
		txt_msg = (TextView) view.findViewById(R.id.txt_msg);
		txt_msg.setVisibility(View.GONE);
		btn_neg = (Button) view.findViewById(R.id.btn_neg);
		btn_neg.setVisibility(View.GONE);
		btn_pos = (Button) view.findViewById(R.id.btn_pos);
		btn_pos.setVisibility(View.GONE);
		img_line = (ImageView) view.findViewById(R.id.img_line);
		img_line.setVisibility(View.GONE);
		iv_alertdialog_line = (ImageView) view.findViewById(R.id.iv_alertdialog_line);

		// 定义Dialog布局和参数
		dialog = new Dialog(context, R.style.AlertDialogStyle);
		dialog.setContentView(view);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
		// 调整dialog背景大小
		lLayout_bg.setLayoutParams(new FrameLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT));

		return this;
	}

	public AlertDialog setTitle(String title) {
		showTitle = true;
		if ("".equals(title)) {
			txt_title.setText("标题");
		} else {
			txt_title.setText(title);
		}
		return this;
	}

	public AlertDialog setView(View v) {
		showView = true;
		rl_view.removeAllViews();
		rl_view.addView(v);
		return this;
	}

	public AlertDialog setView(View v, boolean hideOther) {
		if (hideOther) {
			showTitle = false;
			showMsg = false;
			showPosBtn = false;
			showNegBtn = false;

			iv_alertdialog_line.setVisibility(View.GONE);
		}
		return setView(v);
	}

	public AlertDialog setMsg(String msg) {
		showMsg = true;
		if ("".equals(msg)) {
			txt_msg.setText("内容");
		} else {
			txt_msg.setText(msg);
		}
		return this;
	}

	public AlertDialog setMsg(String msg, int gravity) {
		txt_msg.setGravity(gravity);
		return setMsg(msg);
	}

	public AlertDialog setCancelable(boolean cancel) {
		dialog.setCancelable(cancel);
		return this;
	}

	public AlertDialog setPositiveButton(String text, final OnClickListener listener) {
		showPosBtn = true;
		if ("".equals(text)) {
			btn_pos.setText("确定");
		} else {
			btn_pos.setText(text);
		}
		btn_pos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClick(v);
				dialog.dismiss();
			}
		});
		return this;
	}

	public AlertDialog setNegativeButton(String text, final OnClickListener listener) {
		showNegBtn = true;
		if ("".equals(text)) {
			btn_neg.setText("取消");
		} else {
			btn_neg.setText(text);
		}
		btn_neg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClick(v);
				dialog.dismiss();
			}
		});
		return this;
	}

	private void setLayout() {
		// if (!showTitle && !showMsg) {
		// txt_title.setText("提示");
		// txt_title.setVisibility(View.VISIBLE);
		// }

		if (showTitle) {
			txt_title.setVisibility(View.VISIBLE);
		}

		if (showMsg) {
			txt_msg.setVisibility(View.VISIBLE);
			rl_view.setVisibility(View.GONE);
		}
		if (showView) {
			txt_msg.setVisibility(View.GONE);
			rl_view.setVisibility(View.VISIBLE);
		}

		// if (!showPosBtn && !showNegBtn) {
		// btn_pos.setText("确定");
		// btn_pos.setVisibility(View.VISIBLE);
		// btn_pos.setBackgroundResource(R.drawable.alertdialog_single_selector);
		// btn_pos.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// dialog.dismiss();
		// }
		// });
		// }

		if (showPosBtn && showNegBtn) {
			btn_pos.setVisibility(View.VISIBLE);
			btn_pos.setBackgroundResource(R.drawable.alertdialog_right_selector);
			btn_neg.setVisibility(View.VISIBLE);
			btn_neg.setBackgroundResource(R.drawable.alertdialog_left_selector);
			img_line.setVisibility(View.VISIBLE);
		}

		if (showPosBtn && !showNegBtn) {
			btn_pos.setVisibility(View.VISIBLE);
			btn_pos.setBackgroundResource(R.drawable.alertdialog_single_selector);
		}

		if (!showPosBtn && showNegBtn) {
			btn_neg.setVisibility(View.VISIBLE);
			btn_neg.setBackgroundResource(R.drawable.alertdialog_single_selector);
		}
	}

	public void show() {
		setLayout();
		dialog.show();
	}

	public void dismiss() {
		dialog.dismiss();
	}

	public AlertDialog setOnDismiss(OnDismissListener listener) {
		dialog.setOnDismissListener(listener);
		return this;
	}

}
