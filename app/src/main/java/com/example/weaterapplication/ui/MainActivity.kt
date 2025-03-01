package com.example.weaterapplication.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weaterapplication.R
import com.example.weaterapplication.data.ForecaApi
import com.example.weaterapplication.data.authorisze.ForecaAuthRequest
import com.example.weaterapplication.data.authorisze.ForecaAuthResponse
import com.example.weaterapplication.data.location.ForecastLocation
import com.example.weaterapplication.data.location.ForecastResponse
import com.example.weaterapplication.data.location.LocationsResponse
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    companion object {
        const val BASE_URL = "https://fnw-us.foreca.com"
        const val USER = "godmordor"
        const val PASSWORD = "ujmbXdY0sQvU"
    }

    private var token = ""

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        // Добавляет CallAdapterFactory для RxJava
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    private val forecaService = retrofit.create(ForecaApi::class.java)

    private val locations = ArrayList<ForecastLocation>()
    private val adapter = LocationsAdapter {
        showWeather(it)
    }

    private lateinit var searchButton: Button
    private lateinit var queryInput: EditText
    private lateinit var placeholderMessage: TextView
    private lateinit var locationsList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        placeholderMessage = findViewById(R.id.placeholderMessage)
        searchButton = findViewById(R.id.searchButton)
        queryInput = findViewById(R.id.queryInput)
        locationsList = findViewById(R.id.locations)

        adapter.locations = locations

        locationsList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        locationsList.adapter = adapter

        searchButton.setOnClickListener {
            if (queryInput.text.isNotEmpty()) {
                authenticate()
            }
        }
    }

    private fun showMessage(text: String, additionalMessage: String) {
        if (text.isNotEmpty()) {
            placeholderMessage.visibility = View.VISIBLE
            locations.clear()
            adapter.notifyDataSetChanged()
            placeholderMessage.text = text
            if (additionalMessage.isNotEmpty()) {
                Toast.makeText(applicationContext, additionalMessage, Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            placeholderMessage.visibility = View.GONE
        }
    }

    private fun authenticate() {
        forecaService.authenticate(ForecaAuthRequest(USER, PASSWORD))
            .flatMap { tokenResponse ->
                // Конвертируем полученный accessToken в новый запрос
                token = tokenResponse.token

                // Переключаемся на следующий сетевой запрос
                val bearerToken = "Bearer ${tokenResponse.token}"
                forecaService.getLocations(bearerToken, queryInput.text.toString())
            }.retry { count, throwable ->
                count < 3 && throwable is HttpException && throwable.code() == 401
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { locationsResponse ->
                    // В итоговый subscribe теперь приходят локации
                    locations.clear()
                    locations.addAll(locationsResponse.locations)
                    adapter.notifyDataSetChanged()
                    showMessage("", "")
                },
                { error -> if (error is HttpException && error.code() == 401) {
                    // рекурсивно вызываем метод authenticate
                    authenticate()
                }}
            )

    }

private fun showWeather(location: ForecastLocation) {
    forecaService.getForecast("Bearer $token", location.id)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            {
                result ->
                val message =
                    "${location.name} t: ${result.current.temperature}\n(Ощущается как ${result.current.feelsLikeTemp})"
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            },
            {
                error -> Toast.makeText(applicationContext,error.toString(), Toast.LENGTH_LONG).show()
            }
        )

}
}