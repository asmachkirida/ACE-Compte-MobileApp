package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.adapters.CompteAdapter;
import com.example.myapplication.models.Compte;
import com.example.myapplication.network.ApiService;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.simpleframework.xml.core.Persister;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity implements CompteAdapter.OnDeleteClickListener {

    private RecyclerView recyclerView;
    private CompteAdapter compteAdapter;
    private ApiService apiService;
    private Button addAccountButton;
    private RadioGroup formatRadioGroup;  // RadioGroup to select format (JSON or XML)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // Replace with your server's IP
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
        addAccountButton = findViewById(R.id.addAccountButton);
        formatRadioGroup = findViewById(R.id.radioGroup); // Corrected line: Initialize the RadioGroup

        addAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddAccountDialog();
            }
        });

        formatRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioJson) {  // JSON format selected
                    fetchComptesJson();
                } else if (checkedId == R.id.radioXml) {  // XML format selected
                    fetchComptesXml();
                }
            }
        });

     // its JSON By default
        fetchComptesJson();
    }

    // Fetch and display the list of comptes as JSON
    private void fetchComptesJson() {
        apiService.getComptesJson().enqueue(new Callback<List<Compte>>() {
            @Override
            public void onResponse(Call<List<Compte>> call, Response<List<Compte>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Compte> comptes = response.body();
                    Log.d("MainActivity", "Response as JSON: " + new Gson().toJson(comptes));

                    compteAdapter = new CompteAdapter(comptes, MainActivity.this);
                    recyclerView.setAdapter(compteAdapter);
                } else {
                    Log.e("MainActivity", "Response failed. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Compte>> call, Throwable t) {
                Log.e("MainActivity", "Error fetching JSON data: " + t.getMessage());
            }
        });
    }


    private void fetchComptesXml() {
        apiService.getComptesXml().enqueue(new Callback<List<Compte>>() {
            @Override
            public void onResponse(Call<List<Compte>> call, Response<List<Compte>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Compte> comptes = response.body();
                    try {
                        String xmlResponse = convertToXml(comptes);
                        Log.d("MainActivity", "Response as XML: " + xmlResponse);
                    } catch (Exception e) {
                        Log.e("MainActivity", "Error converting to XML", e);
                    }

                    compteAdapter = new CompteAdapter(comptes, MainActivity.this);
                    recyclerView.setAdapter(compteAdapter);
                } else {
                    Log.e("MainActivity", "Response failed. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Compte>> call, Throwable t) {
                Log.e("MainActivity", "Error fetching XML data: " + t.getMessage());
            }
        });
    }

    private String convertToXml(List<Compte> comptes) throws Exception {
        Persister persister = new Persister();
        StringWriter writer = new StringWriter();
        for (Compte compte : comptes) {
            persister.write(compte, writer);
        }
        return writer.toString();
    }

    private void showAddAccountDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_account, null);

        EditText soldeEditText = dialogView.findViewById(R.id.soldeEditText);
        RadioGroup typeRadioGroup = dialogView.findViewById(R.id.typeRadioGroup);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Account")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    double solde = Double.parseDouble(soldeEditText.getText().toString());
                    int selectedRadioButtonId = typeRadioGroup.getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = dialogView.findViewById(selectedRadioButtonId);
                    String typeText = selectedRadioButton.getText().toString();
                    String type = mapToString(typeText);
                    String currentDate = getCurrentDate();

                    Compte newCompte = new Compte();
                    newCompte.setSolde(solde);
                    newCompte.setType(type);
                    newCompte.setDateCreation(currentDate);

                    apiService.addCompte(newCompte).enqueue(new Callback<Compte>() {
                        @Override
                        public void onResponse(Call<Compte> call, Response<Compte> response) {
                            if (response.isSuccessful()) {
                                fetchComptesJson(); // Refresh list after adding
                            }
                        }

                        @Override
                        public void onFailure(Call<Compte> call, Throwable t) {
                            Log.e("MainActivity", "Failed to add account: " + t.getMessage());
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String mapToString(String typeText) {
        if ("Savings".equals(typeText)) {
            return "EPARGNE";
        } else if ("Current".equals(typeText)) {
            return "COURANT";
        }
        return null;
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    @Override
    public void onDeleteClick(Compte compte) {
        apiService.deleteCompte(compte.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchComptesJson(); // Refresh the list after deletion
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("MainActivity", "Failed to delete account: " + t.getMessage());
            }
        });
    }
}
