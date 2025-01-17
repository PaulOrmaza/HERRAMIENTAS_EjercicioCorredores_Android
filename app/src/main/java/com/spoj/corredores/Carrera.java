package com.spoj.corredores;

import android.os.Bundle;
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

        // Referenciar los elementos de la vista
        editNumCorredores = findViewById(R.id.edit_num_corredores);
        editDistancia = findViewById(R.id.edit_distancia);
        Button btnIniciarCarrera = findViewById(R.id.btn_iniciar_carrera);
        txtResultadosLabel = findViewById(R.id.txt_resultados_label);
        resultadosContainer = findViewById(R.id.resultados_container);

        // Configurar el botón para iniciar la carrera
        btnIniciarCarrera.setOnClickListener(v -> iniciarCarrera());
    }

    private void iniciarCarrera() {
        // Validar la entrada de datos
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
                // Configurar conexión al servidor
                String urlString = "http://192.168.1.19:3003/carrera/" + numCorredores + "/" + distancia;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Leer la respuesta del servidor
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Procesar resultados en el hilo principal
                runOnUiThread(() -> mostrarResultados(response.toString()));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
                });
            }
        });

        thread.start();
    }

    private void mostrarResultados(String response) {
        try {
            JSONObject json = new JSONObject(response);

            // Extraer información
            JSONArray resultados = json.getJSONArray("resultados");

            // Mostrar el mensaje de resultados
            txtResultadosLabel.setVisibility(View.VISIBLE);
            resultadosContainer.setVisibility(View.VISIBLE);
            resultadosContainer.removeAllViews();

            // Mostrar los resultados
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
