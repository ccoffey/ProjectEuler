package ie.cathalcoffey.android;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MyAppWidgetProvider extends AppWidgetProvider 
{	
	Context context;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) 
	{
		this.context = context;
		
		for (int i = 0; i < appWidgetIds.length; i++) 
		{
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
	}
	
	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
	{	
		SharedPreferences sharedPreferences = context.getSharedPreferences(MyAppWidgetProvider.class.toString() + "_" + appWidgetId, Context.MODE_PRIVATE);
	    
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
		remoteViews.setTextViewText(R.id.username, sharedPreferences.getString("username", ""));
		remoteViews.setTextViewText(R.id.level, sharedPreferences.getString("level", ""));
		remoteViews.setTextViewText(R.id.solved, sharedPreferences.getString("solved", ""));
		remoteViews.setProgressBar(R.id.progressBar, sharedPreferences.getInt("total", 0), sharedPreferences.getInt("progress", 0), false);
		
		Intent active = new Intent(context, Tabs.class);
		active.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		active.putExtra("mAppWidgetId", appWidgetId);
		
		Uri data = Uri.withAppendedPath(Uri.parse("ProjectEuler://widget/id/") ,String.valueOf(appWidgetId));
		active.setData(data);
			
		PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, active, PendingIntent.FLAG_UPDATE_CURRENT);		
		remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
		
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews); 
	}
}