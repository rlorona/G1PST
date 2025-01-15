package com.example.pstg1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private Button BotonTemp, BotonConsumo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inicializar los botones
        BotonTemp = findViewById(R.id.BotonTemp);
        BotonConsumo = findViewById(R.id.BotonConsumo);

        // Ajustar el padding de las vistas
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Listener para BotonTemp -> TempyhumedadActivity
        BotonTemp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TempyhumedadActivity.class);
            startActivity(intent);
        });

        // Listener para BotonConsumo -> ConsumoElecActivity
        BotonConsumo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConsumoElecActivity.class);
            startActivity(intent);
        });
    }
}
