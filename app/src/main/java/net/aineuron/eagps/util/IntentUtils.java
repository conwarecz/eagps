package net.aineuron.eagps.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.MainActivity_;
import net.aineuron.eagps.model.database.order.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vit Veres on 15-May-17
 * as a part of Android-EAGPS project.
 */

public class IntentUtils {
	public static void openMainActivity(Context context) {
        MainActivity_.intent(context).start();
    }

    public static void openMainActivityWithStateSelect(Context context) {
        MainActivity_.intent(context).extra("stateSelect", true).start();
    }

    public static void openNewMainActivity(Context context) {
        MainActivity_.intent(context).flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK).start();
    }

    public static Intent mainActivityIntent(Context context, Long messageId) {
        return MainActivity_.intent(context)
                .extra("messageId", messageId).get();
    }

    public static Intent mainActivityIntent(Context context, Long messageId, boolean cancelTender) {
        return MainActivity_.intent(context)
                .extra("messageId", messageId)
                .extra("cancelTender", cancelTender).get();
    }

	public static void openUrl(Context context, String url) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		try {
			i.setData(Uri.parse(url));
			context.startActivity(i);
		} catch (Exception e) {
			e.printStackTrace();
            Toast.makeText(context, R.string.intent_browser, Toast.LENGTH_SHORT).show();
        }
	}

	public static boolean shareText(Context context, String text) {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
		try {
			context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.share_text)));
		} catch (Exception e) {
			e.printStackTrace();
            Toast.makeText(context, R.string.intent_text, Toast.LENGTH_LONG).show();
        }
		return false;
	}

	public static void copyToClipboard(Context context, String text) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("EAGPS", text);
		try {
			clipboard.setPrimaryClip(clip);
			Toast.makeText(context, R.string.share_copy, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void dialPhone(Context context, @NonNull String phoneNumber) {
		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));

		try {
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
            Toast.makeText(context, R.string.intent_dialer, Toast.LENGTH_LONG).show();
        }
	}

	public static void openCamera(Context context) {
		Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
            Toast.makeText(context, R.string.intent_camera, Toast.LENGTH_LONG).show();
        }
	}

	public static void openMapLocation(Context context, Location location, String label) {
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		String uriBegin = "geo:" + latitude + "," + longitude;
		String query = latitude + "," + longitude + "(" + label + ")";
		String encodedQuery = Uri.encode(query);
		String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
		Uri uri = Uri.parse(uriString);
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
            Toast.makeText(context, R.string.intent_gmaps, Toast.LENGTH_LONG).show();
        }
	}

	public static void openRoute(Context context, @NonNull Location destination, @NonNull Location waypoint) {
		List<Location> waypoints = new ArrayList<>();
		waypoints.add(waypoint);

		openRoute(context, destination, waypoints);
	}

	public static void openRoute(Context context, @NonNull Location destination, List<Location> waypoints) {
		Uri.Builder builder = new Uri.Builder();


		builder.scheme("https")
				.authority("www.google.com").appendPath("maps").appendPath("dir").appendPath("").appendQueryParameter("api", "1")
				.appendQueryParameter("destination", destination.getLatitude() + "," + destination.getLongitude());

		if (waypoints != null && waypoints.size() > 0) {
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < waypoints.size(); i++) {
				Location loca = waypoints.get(i);
				stringBuilder.append(loca.getLatitude());
				stringBuilder.append(",");
				stringBuilder.append(loca.getLongitude());
			}
			builder.appendQueryParameter("waypoints", stringBuilder.toString());
		}

		String url = builder.build().toString();
		Log.d("Directions", url);
		Intent i = new Intent(Intent.ACTION_VIEW);

		i.setData(Uri.parse(url));
		try {
			context.startActivity(i);
		} catch (Exception e) {
			e.printStackTrace();
            Toast.makeText(context, R.string.intent_gmaps, Toast.LENGTH_LONG).show();
        }
	}
}
