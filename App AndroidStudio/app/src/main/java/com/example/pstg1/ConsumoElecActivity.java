package com.example.pstg1;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class ConsumoElecActivity extends AppCompatActivity {

    private TextView[] tvRegleta = new TextView[6];
    private TextView[] tvConsumoRegleta = new TextView[6];
    private TextView[][] tvComputadora = new TextView[6][2];
    private final String[] codigosRegletas = {"4106", "4832", "42F6", "4E32", "CC0F","597C"};
    private DatabaseReference databaseRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumoelec);

        for (int i = 0; i < 6; i++) {
            tvRegleta[i] = findViewById(getResources().getIdentifier("tvRegleta" + (i + 1), "id", getPackageName()));
            tvConsumoRegleta[i] = findViewById(getResources().getIdentifier("tvConsumoRegleta" + (i + 1), "id", getPackageName()));
            tvComputadora[i][0] = findViewById(getResources().getIdentifier("tvComputadora" + (i + 1) + "_1", "id", getPackageName()));
            tvComputadora[i][1] = findViewById(getResources().getIdentifier("tvComputadora" + (i + 1) + "_2", "id", getPackageName()));
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("consumos/-OGqD9uX1oot7n0eaqXW");
        cargarDatosFirebase();

        Button btnRegresar = findViewById(R.id.btnRegresar);
        btnRegresar.setOnClickListener(v -> {
            Intent intent = new Intent(ConsumoElecActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void cargarDatosFirebase() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DecimalFormat df = new DecimalFormat("#.###"); // Formato con máximo 3 decimales

                    for (int i = 0; i < codigosRegletas.length; i++) {
                        String regletaCodigo = codigosRegletas[i];
                        DataSnapshot regletaSnapshot = snapshot.child(regletaCodigo);

                        if (regletaSnapshot.exists()) {
                            double consumoTotal = 0;
                            DataSnapshot enchufesSnapshot = regletaSnapshot.child("enchufes_activos");

                            if (enchufesSnapshot.exists()) {
                                int index = 0;

                                for (DataSnapshot enchufe : enchufesSnapshot.getChildren()) {
                                    Double consumo = enchufe.child("consumo").getValue(Double.class);
                                    String nombreEnchufe = enchufe.child("nombre").getValue(String.class);

                                    if (consumo != null) {
                                        consumoTotal += consumo;
                                        String estado = (consumo > 10) ? "ON" : "OFF";

                                        if (index < 2) {
                                            tvComputadora[i][index].setText(nombreEnchufe + ": " + estado);
                                            tvComputadora[i][index].setTextColor(estado.equals("ON") ? 0xFF00FF00 : 0xFFFF0000);
                                        }
                                        index++;
                                    }
                                }
                            }

                            tvRegleta[i].setText("Regleta " + (i + 1) + ": " + regletaCodigo);
                            tvConsumoRegleta[i].setText("Consumo Total: " + df.format(consumoTotal) + "W");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error al leer datos", error.toException());
            }
        });
    }
}

