package com.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.RemoteViews
import com.widget.remote.DoggyApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

private const val MY_ACTION = "MY_ACTION"

class MyWidgetClass : AppWidgetProvider() {

    private val doggyApi: DoggyApi by lazy {
        val retrofit = Retrofit.Builder().baseUrl("https://dog.ceo/api/breeds/image/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        retrofit.create(DoggyApi::class.java)
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != MY_ACTION) return
        receiveImage(context, false)
    }

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        receiveImage(context, true)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        receiveImage(context, true)
    }


    private fun receiveImage(
        context: Context, withPendingIntent: Boolean
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetId = ComponentName(context, MyWidgetClass::class.java)

        RemoteViews(
            context.packageName, R.layout.initial_layout
        ).apply {
            scope.launch {
                if (withPendingIntent) {

                    //        val intent = Intent(context, MyWidgetClass::class.java). apply {
//            action = MY_ACTION
//        }
//        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

                    val pendingIntent = Intent(context, MyWidgetClass::class.java).let { intent ->
                        intent.action = MY_ACTION
                        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                    }
                    setOnClickPendingIntent(R.id.doggy_image, pendingIntent)
                }
                val url = doggyApi.getRandoDog().message
                val bitmap = getBitmapFromURL(url)
                setImageViewBitmap(R.id.doggy_image, bitmap)
                appWidgetManager.updateAppWidget(appWidgetId, this@apply)
            }
        }
    }


    private fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            null
        }

    }

}