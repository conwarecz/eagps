package net.aineuron.eagps.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * Created by Vit Veres on 15-May-17
 * as a part of Android-EAGPS project.
 */

public class IntentUtils {
	public static void openUrl(Context context, String url) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		try {
			i.setData(Uri.parse(url));
			context.startActivity(i);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Please install a web browser", Toast.LENGTH_SHORT).show();
		}
	}
}
