package com.example.myapplication.network;


import com.example.myapplication.models.Compte;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @GET("banque/comptes")
    Call<List<Compte>> getComptes();
    @GET("banque/comptes")
    Call<List<Compte>> getComptesJson();

    @GET("banque/comptes")  // Replace with the correct endpoint
    Call<List<Compte>> getComptesXml();

    @POST("banque/comptes")
    Call<Compte> addCompte(@Body Compte compte);

    @DELETE("banque/comptes/{id}")
    Call<Void> deleteCompte(@Path("id") Long id);
}
