package com.example.grupo4_firebase_crud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText nombre, apellido, correo, fecha;
    ImageView imageView;
    ImageButton btnFoto;
    Button btnGuardar, btnRegistros;
    private FirebaseFirestore mfirestore;
    private static final int COD_SEL_STORAGE = 200;
    private static final int COD_SEL_IMAGE = 300;
    private Uri image_url;
    //private FirebaseAuth mAuth;
    StorageReference storageReference;
    String storage_path = "contacto/*";
    ProgressDialog progressDialog;
    String idd;
    String photo = "photo";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String id = getIntent().getStringExtra("id_contacto");

        //Obtener la instancia de Firebase Firestore (AQUI ESTAMOS APUNTANDO A LA BASE DE DATOS)
        mfirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        nombre = (EditText) findViewById(R.id.txtNombre);
        apellido = (EditText) findViewById(R.id.txtApellido);
        correo = (EditText) findViewById(R.id.txtCorreo);
        fecha = (EditText) findViewById(R.id.txtFecha);
        imageView = (ImageView) findViewById(R.id.imageView);
        btnFoto = (ImageButton) findViewById(R.id.btnFoto);
        btnGuardar = (Button) findViewById(R.id.btnGuardar);
        btnRegistros = (Button) findViewById(R.id.btnRegistros);

        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadPhoto();
            }
        });


        if(id == null || id == ""){

            //Evento para el boton Guardar
            btnGuardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String nombreContacto = nombre.getText().toString().trim();
                    String apellidoContacto = apellido.getText().toString().trim();
                    String correoContacto = correo.getText().toString().trim();
                    String fechaContacto = fecha.getText().toString().trim();

                    //Validacion de campos vacios
                    if(nombreContacto.isEmpty() || apellidoContacto.isEmpty() || correoContacto.isEmpty() || fechaContacto.isEmpty()){

                        Toast.makeText(getApplicationContext(), "Campos Vacios!", Toast.LENGTH_LONG).show();

                    }else{

                        postContacto(nombreContacto, apellidoContacto, correoContacto, fechaContacto);
                        clear();

                    }
                }
            });

        }else{

            idd = id;

            //Actuaizar el nombre del boton
            btnGuardar.setText("Actualizar");

            //Llamamos al metodo de obtener datos
            getContacto(id);

            btnGuardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String nombreContacto = nombre.getText().toString().trim();
                    String apellidoContacto = apellido.getText().toString().trim();
                    String correoContacto = correo.getText().toString().trim();
                    String fechaContacto = fecha.getText().toString().trim();

                    //Validacion de campos vacios
                    if(nombreContacto.isEmpty() || apellidoContacto.isEmpty() || correoContacto.isEmpty() || fechaContacto.isEmpty()){

                        Toast.makeText(getApplicationContext(), "Campos Vacios!", Toast.LENGTH_LONG).show();

                    }else{

                        updateContacto(nombreContacto, apellidoContacto, correoContacto, fechaContacto, id);
                        clear();

                    }
                }
            });
        }


        //Evento para el boton Registros
        btnRegistros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ListaRegistrosActivity.class);
                startActivity(intent);
            }
        });
    }

    //Metodo para subir la foto
    private void uploadPhoto() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, COD_SEL_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if (requestCode == COD_SEL_IMAGE){
                image_url = data.getData();
                subirPhoto(image_url);
                //imageView.setImageURI(image_url);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Metodo para subir la foto desde la galeria
    private void subirPhoto(Uri image_url) {
        /*progressDialog.setMessage("Actualizando foto");
        progressDialog.show();*/
        String rute_storage_photo = storage_path + "" + photo + "" + idd;
        StorageReference reference = storageReference.child(rute_storage_photo);
        reference.putFile(image_url).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                if (uriTask.isSuccessful()){
                    uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String download_uri = uri.toString();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("photo", download_uri);
                            mfirestore.collection("contacto").document(idd).update(map);
                            Toast.makeText(MainActivity.this, "Foto actualizada", Toast.LENGTH_SHORT).show();
                            //progressDialog.dismiss();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error al cargar foto", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //Metodo para actualizar los datos en la base de datos
    private void updateContacto(String nombreContacto, String apellidoContacto, String correoContacto, String fechaContacto, String id) {

        Map<String, Object> map = new HashMap<>();
        map.put("nombre", nombreContacto );
        map.put("apellido", apellidoContacto);
        map.put("correo", correoContacto );
        map.put("fechaNacimiento", fechaContacto );

        mfirestore.collection("contacto").document(id).update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Toast.makeText(getApplicationContext(), "Registro actualizado!", Toast.LENGTH_LONG).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getApplicationContext(), "Error al actualizar!", Toast.LENGTH_LONG).show();

            }
        });
    }

    //Metodo para insertar los datos en la base de datos
    private void postContacto(String nombreContacto, String apellidoContacto, String correoContacto, String fechaContacto) {

        Map<String, Object> map = new HashMap<>();
        map.put("nombre", nombreContacto );
        map.put("apellido", apellidoContacto);
        map.put("correo", correoContacto );
        map.put("fechaNacimiento", fechaContacto );

        mfirestore.collection("contacto").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

                Toast.makeText(getApplicationContext(), "Registro Exitoso!", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getApplicationContext(), "Error al ingresar!", Toast.LENGTH_LONG).show();
            }
        });
    }

    //Metodo para obtener toda la informacion al momento de actualizarla
    private void getContacto(String id){

        mfirestore.collection("contacto").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String nombrePersona = documentSnapshot.getString("nombre");
                String apellidoPersona = documentSnapshot.getString("apellido");
                String correoPersona = documentSnapshot.getString("correo");
                String fechaNacPersona = documentSnapshot.getString("fechaNacimiento");
                String photoPersona = documentSnapshot.getString("photo");

                nombre.setText(nombrePersona);
                apellido.setText(apellidoPersona);
                correo.setText(correoPersona);
                fecha.setText(fechaNacPersona);

                try {
                    if(!photoPersona.equals("")){
                        Toast toast = Toast.makeText(getApplicationContext(), "Cargando foto", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP,0,200);
                        toast.show();

                        Picasso.with(MainActivity.this)
                                .load(photoPersona)
                                .resize(150, 150)
                                .into(imageView);
                    }
                }catch (Exception e){
                    Log.v("Error", "e: " + e);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getApplicationContext(), "Error al obtener los datos!", Toast.LENGTH_LONG).show();

            }
        });

    }

    //Metodo para limpiar las cajas de texto
    private void clear(){
        nombre.setText("");
        apellido.setText("");
        correo.setText("");
        fecha.setText("");
        //imagen.setImageResource(R.drawable.usuario);
    }
}