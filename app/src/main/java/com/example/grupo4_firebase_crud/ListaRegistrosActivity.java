package com.example.grupo4_firebase_crud;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.SearchView;

import com.example.grupo4_firebase_crud.adapter.ContactoAdapter;
import com.example.grupo4_firebase_crud.model.Contacto;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ListaRegistrosActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ContactoAdapter mAdpater;
    FirebaseFirestore mFirestore;
    SearchView searchView;
    Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_registros);

        mFirestore = FirebaseFirestore.getInstance();
        searchView = (SearchView) findViewById(R.id.search);

        /*recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Query query = mFirestore.collection("contacto");

        FirestoreRecyclerOptions<Contacto> firestoreRecyclerOptions =
                new FirestoreRecyclerOptions.Builder<Contacto>().setQuery(query, Contacto.class).build();*/

        /*mAdpater = new ContactoAdapter(firestoreRecyclerOptions, this);
        mAdpater.notifyDataSetChanged();*/
        //recyclerView.setAdapter(mAdpater);

        setUpRecyclerView();
        search_view();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setUpRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        query = mFirestore.collection("contacto");

        FirestoreRecyclerOptions<Contacto> firestoreRecyclerOptions =
                new FirestoreRecyclerOptions.Builder<Contacto>().setQuery(query, Contacto.class).build();

        mAdpater = new ContactoAdapter(firestoreRecyclerOptions, this, getSupportFragmentManager());
        mAdpater.notifyDataSetChanged();
        recyclerView .setAdapter(mAdpater);
    }


    //Metodo para buscar registros
    private void search_view() {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                textSearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                textSearch(s);
                return false;
            }
        });
    }

    public void textSearch(String s){
        //Query query = mFirestore.collection( "contacto");
        FirestoreRecyclerOptions<Contacto> firestoreRecyclerOptions =
                new FirestoreRecyclerOptions.Builder<Contacto>()
                        .setQuery(query.orderBy("nombre")
                                .startAt(s).endAt(s+"~"), Contacto.class).build();
        mAdpater = new ContactoAdapter(firestoreRecyclerOptions, this, getSupportFragmentManager());
        mAdpater.startListening();
        recyclerView.setAdapter(mAdpater);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAdpater.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdpater.stopListening();
    }
}