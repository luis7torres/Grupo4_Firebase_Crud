package com.example.grupo4_firebase_crud.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grupo4_firebase_crud.MainActivity;
import com.example.grupo4_firebase_crud.R;
import com.example.grupo4_firebase_crud.model.Contacto;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class ContactoAdapter extends FirestoreRecyclerAdapter<Contacto, ContactoAdapter.ViewHolder> {

    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    Activity activity;
    FragmentManager fm;


    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public ContactoAdapter(@NonNull FirestoreRecyclerOptions<Contacto> options,  Activity activity, FragmentManager fm) {
        super(options);

        this.activity = activity;
        this.fm = fm;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Contacto model) {

        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
        final String id = documentSnapshot.getId();

        holder.nombre.setText(model.getNombre());
        holder.apellido.setText(model.getApellido());
        String photoPersona = model.getPhoto();

        try {
            if (!photoPersona.equals(""))
                Picasso.with(activity.getApplicationContext())
                        .load(photoPersona)
                        .resize(150, 150)
                        .into(holder.photo_persona);
        }catch (Exception e){
            Log.d("Exception", "e: "+e);
        }


        //Evento para el boton de eliminar
        holder.btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                eliminarContacto(id);

            }
        });

        //Evento para el boton de editar
        holder.btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra("id_contacto", id);
                activity.startActivity(intent);
            }
        });

    }

    //Metodo para eliminar el contacto
    private void eliminarContacto(String id) {

        mFirestore.collection("contacto").document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Toast.makeText(activity, "Eliminado exitosamente!", Toast.LENGTH_LONG).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(activity, "Error al eliminar!", Toast.LENGTH_LONG).show();

            }
        });

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_contacto, parent, false);
        return new ViewHolder(v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nombre, apellido;
        ImageView btnEliminar, btnEditar, photo_persona;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nombre = itemView.findViewById(R.id.nombre);
            apellido = itemView.findViewById(R.id.apellido);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
            btnEditar = itemView.findViewById(R.id.btn_editar);
            photo_persona = itemView.findViewById(R.id.photo);
        }
    }


}
