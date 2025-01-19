package com.example.pstg1;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TempyhumedadActivity extends AppCompatActivity {

    private EditText editTextTemperatura, editTextHumedad;
    private LineChart lineChart;
    private Handler handler = new Handler();
    private static final String SENSOR_ID = "66bccbf9853ca8b035a7848a";
    private static final int UPDATE_INTERVAL = 5000; // 5 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tempyhumedad);

        // Vincular elementos de la interfaz
        editTextTemperatura = findViewById(R.id.editTextText);
        editTextHumedad = findViewById(R.id.editTextText2);
        lineChart = findViewById(R.id.line_chart);

        // Vincular el botón de regresar
        Button btnRegresar = findViewById(R.id.btn_regresar);
        btnRegresar.setOnClickListener(v -> {
            // Finaliza esta actividad y regresa a la actividad previa (MainActivity)
            finish();
        });

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://aias.espol.edu.ec/") // URL base
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        HayiotApi api = retrofit.create(HayiotApi.class);

        // Configurar el gráfico
        setupChart();

        // Iniciar actualización en tiempo real
        handler.post(new Runnable() {
            @Override
            public void run() {
                fetchSensorData(api);
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        });
    }


    private static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }

    private static String getStartDate(int hoursBefore) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -hoursBefore);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);

        // Configuración de los ejes
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return new SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(new Date((long) value));
            }
        });
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45);

        // Leyenda ajustada
        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setYOffset(30f); // Baja la leyenda
        legend.setDrawInside(false);
    }


    private void fetchSensorData(HayiotApi api) {
        String startDate = getStartDate(24); // Últimas 24 horas
        String endDate = getCurrentDate();  // Fecha actual

        api.getSensorData(SENSOR_ID, startDate, endDate).enqueue(new Callback<List<SensorData>>() {
            @Override
            public void onResponse(Call<List<SensorData>> call, Response<List<SensorData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SensorData> sensorDataList = response.body();

                    // Filtrar datos a las últimas 24 horas
                    long currentTime = System.currentTimeMillis();
                    long oneDayMillis = 24 * 60 * 60 * 1000; // 24 horas en milisegundos
                    List<SensorData> filteredData = new ArrayList<>();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    for (SensorData data : sensorDataList) {
                        try {
                            Date date = dateFormat.parse(data.getSensedAt());
                            if (date != null && currentTime - date.getTime() <= oneDayMillis) {
                                filteredData.add(data);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    // Procesar datos para el gráfico
                    List<Entry> temperatureEntries = new ArrayList<>();
                    List<Entry> humidityEntries = new ArrayList<>();
                    SensorData latestTemperature = null;
                    SensorData latestHumidity = null;

                    for (SensorData data : filteredData) {
                        try {
                            Date date = dateFormat.parse(data.getSensedAt());
                            if (date != null) {
                                float time = date.getTime();

                                // Agregar datos al gráfico
                                if ("temp".equals(data.getType())) {
                                    temperatureEntries.add(new Entry(time, (float) data.getData()));
                                    latestTemperature = data;
                                } else if ("humidity".equals(data.getType())) {
                                    humidityEntries.add(new Entry(time, (float) data.getData()));
                                    latestHumidity = data;
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    // Actualizar la interfaz con los datos más recientes
                    if (latestTemperature != null) {
                        editTextTemperatura.setText(String.format(Locale.getDefault(), "%.2f °C", latestTemperature.getData()));
                    }
                    if (latestHumidity != null) {
                        editTextHumedad.setText(String.format(Locale.getDefault(), "%.2f %%", latestHumidity.getData()));
                    }

                    // Dibujar el gráfico si hay datos
                    updateChart(temperatureEntries, humidityEntries);
                } else {
                    Toast.makeText(TempyhumedadActivity.this, "Error al obtener los datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SensorData>> call, Throwable t) {
                Toast.makeText(TempyhumedadActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateChart(List<Entry> temperatureEntries, List<Entry> humidityEntries) {
        LineDataSet temperatureDataSet = new LineDataSet(temperatureEntries, "Temperatura");
        LineDataSet humidityDataSet = new LineDataSet(humidityEntries, "Humedad");

        // Configuración de estilo para la temperatura
        temperatureDataSet.setColor(getResources().getColor(android.R.color.holo_red_light));
        temperatureDataSet.setDrawCircles(false); // No mostrar puntos
        temperatureDataSet.setDrawValues(false); // No mostrar valores de puntos
        temperatureDataSet.setLineWidth(2f); // Grosor de la línea

        // Configuración de estilo para la humedad
        humidityDataSet.setColor(getResources().getColor(android.R.color.holo_blue_light));
        humidityDataSet.setDrawCircles(false); // No mostrar puntos
        humidityDataSet.setDrawValues(false); // No mostrar valores de puntos
        humidityDataSet.setLineWidth(2f); // Grosor de la línea

        // Asignar los conjuntos de datos al gráfico
        LineData data = new LineData(temperatureDataSet, humidityDataSet);
        lineChart.setData(data);
        lineChart.invalidate(); // Refrescar el gráfico
    }
}
