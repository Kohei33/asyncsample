package com.websarva.wings.android.asyncsample

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val lvCityList = findViewById<ListView>(R.id.lvCityList)
        val cityList: MutableList<MutableMap<String, String>> = mutableListOf()
        //var city = mutableMapOf("name" to "大阪", "id" to "270000")
        var city = mutableMapOf("name" to "大阪", "id" to "osaka")
        cityList.add(city)
//        city = mutableMapOf("name" to "神戸", "id" to "280010")
        city = mutableMapOf("name" to "神戸", "id" to "kobe")
        cityList.add(city)
        city = mutableMapOf("name" to "東京", "id" to "tokyo")
        cityList.add(city)
        val from = arrayOf("name")
        val to = intArrayOf(android.R.id.text1)
        val adapter = SimpleAdapter(applicationContext, cityList, android.R.layout.simple_expandable_list_item_1, from, to)
        lvCityList.adapter = adapter
        lvCityList.onItemClickListener = ListItemClickListener()
    }

    private inner class ListItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val item = parent.getItemAtPosition(position) as Map<String, String>
            val cityName = item["name"]
            val cityId = item["id"]
            val tyCityName = findViewById<TextView>(R.id.tvCityName)
            tvCityName.setText(cityName + "の天気: ")
            val receiver = WeatherInfoReceiver()
            receiver.execute(cityId)
        }
    }

    private inner class WeatherInfoReceiver(): AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String?): String {
            val id = params[0]
//            val urlStr = "http://weather.livedoor.com/forecast/webservice/json/v1?city=${id}"
            //open weather mapのお天気apiを使用。q:都市名、units:気温を摂氏で取得、appid:APIを利用するために取得したAPIキー。
            val urlStr = "http://api.openweathermap.org/data/2.5/weather?q=${id}&units=metric&appid=99a07fc309d90568ee8c9b885c6e87c1"

            val url = URL(urlStr)
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.connect()
            val stream = con.inputStream
            val result = is2String(stream)
            con.disconnect()
            stream.close()

            return result
        }

        private fun is2String(stream: InputStream): String {
            val sb =StringBuilder()
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            var line = reader.readLine()
            while(line != null) {
                sb.append(line)
                line = reader.readLine()
            }
            reader.close()
            return sb.toString()
        }

        override fun onPostExecute(result: String) {
        val rootJSON = JSONObject(result)
            //open weather map apiのjsonオブジェクトの構成に従っている。weather/0/配下のmain,descriptionを取得。
            val descriptionJSON = rootJSON.getJSONArray("weather")
            val index = descriptionJSON.getJSONObject(0)
            val telop = index.getString("main")
            val desc = index.getString("description")

//        val descriptionJSON = rootJSON.getJSONObject("description")
//        val desc = descriptionJSON.getString("text")
//        val forecasts = rootJSON.getJSONArray("forecasts")
//        val forecastNow = forecasts.getJSONObject(0)
//        val telop = forecastNow.getString("telop")

        val tvWeatherTelop = findViewById<TextView>(R.id.tvWeatherTelop)
        val tvWeatherDesc = findViewById<TextView>(R.id.tvWeatherDesc)
        tvWeatherTelop.text = telop
        tvWeatherDesc.text = desc
        }
    }
}