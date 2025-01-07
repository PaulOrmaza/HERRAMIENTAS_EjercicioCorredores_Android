package com.spoj.corredores;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;

import android.widget.Button;


    public class MainActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            Button btnGestionCorredores = findViewById(R.id.btn_gestion_corredores);
            Button btnSimularCarrera = findViewById(R.id.btn_simular_carrera);

            // Navegar a la gestión de corredores
            btnGestionCorredores.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GestionCorredores.class);
                startActivity(intent);
            });

            // Navegar a la simulación de carrera
            btnSimularCarrera.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, Carrera.class);
                startActivity(intent);
            });
        }
    }

