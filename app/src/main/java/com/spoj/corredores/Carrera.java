package com.spoj.corredores;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Carrera extends AppCompatActivity {

    private EditText editNumCorredores, editDistancia;
    private TextView txtResultadosLabel;
    private LinearLayout resultadosContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrera);

        // Referenciar elementos de la vista
        editNumCorredores = findViewById(R.id.edit_num_corredores);
        editDistancia = findViewById(R.id.edit_distancia);
        Button btnIniciarCarrera = findViewById(R.id.btn_iniciar_carrera);
        txtResultadosLabel = findViewById(R.id.txt_resultados_label);
        resultadosContainer = findViewById(R.id.resultados_container);

        // Configurar el botón para iniciar la carrera
        btnIniciarCarrera.setOnClickListener(v -> iniciarCarrera());
    }

    private void iniciarCarrera() {
        // Validar entrada de datos
        String numCorredoresStr = editNumCorredores.getText().toString();
        String distanciaStr = editDistancia.getText().toString();

        if (numCorredoresStr.isEmpty() || distanciaStr.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int numCorredores = Integer.parseInt(numCorredoresStr);
        double distancia = Double.parseDouble(distanciaStr);

        if (numCorredores <= 0 || distancia <= 0) {
            Toast.makeText(this, "Los valores deben ser mayores a cero", Toast.LENGTH_SHORT).show();
            return;
        }

        // Realizar la simulación en un hilo secundario
        simularCarrera(numCorredores, distancia);
    }

    private void simularCarrera(int numCorredores, double distancia) {
        Thread thread = new Thread(() -> {
            try {
                String urlString = "http://192.168.x.x:3003/carrera/" + numCorredores + "/" + distancia; // Asegúrate de que la IP sea correcta
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);  // Tiempo de espera para la conexión
                connection.setReadTimeout(5000);     // Tiempo de espera para la lectura

                // Log de depuración
                Log.d("Carrera", "Conectando a la URL: " + urlString);

                // Verificar el código de respuesta
                int responseCode = connection.getResponseCode();
                Log.d("Carrera", "Código de respuesta: " + responseCode);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception("Código de respuesta no válido: " + responseCode);
                }

                // Leer la respuesta
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Mostrar resultados en el hilo principal
                runOnUiThread(() -> mostrarResultados(response.toString()));

            } catch (Exception e) {
                // Mostrar detalles de la excepción para depuración
                Log.e("Carrera", "Error al conectar con el servidor", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al conectar con el servidor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });

        thread.start();
    }


    private void mostrarResultados(String response) {
        try {
            JSONObject json = new JSONObject(response);

            // Extraer información
            JSONObject ganador = json.getJSONObject("ganador");
            JSONArray resultados = json.getJSONArray("resultados");

            // Mostrar el ganador
            txtResultadosLabel.setVisibility(View.VISIBLE);
            resultadosContainer.setVisibility(View.VISIBLE);
            resultadosContainer.removeAllViews();

            TextView ganadorView = new TextView(this);
            ganadorView.setText("Ganador: " + ganador.getString("nombre") + " - Tiempo: " + ganador.getString("tiempo") + " segundos");
            ganadorView.setTextSize(16);
            resultadosContainer.addView(ganadorView);

            // Mostrar la clasificación completa
            for (int i = 0; i < resultados.length(); i++) {
                JSONObject corredor = resultados.getJSONObject(i);

                TextView corredorView = new TextView(this);
                corredorView.setText((i + 1) + ". " + corredor.getString("nombre") + " - Tiempo: " + corredor.getString("tiempo") + " segundos");
                corredorView.setTextSize(14);
                resultadosContainer.addView(corredorView);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error al procesar los resultados", Toast.LENGTH_SHORT).show();
        }
    }
}
