package com.example.entregaindividual_2_anelopezmena.fragments.ui.nested;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.entregaindividual_2_anelopezmena.R;
import com.example.entregaindividual_2_anelopezmena.activities.Act_Mapa;
import com.example.entregaindividual_2_anelopezmena.databinding.FragmentNestedDrawerBinding;
import com.example.entregaindividual_2_anelopezmena.listasRecyclerView.nestedRecyclerView.ChildModelClass;
import com.example.entregaindividual_2_anelopezmena.listasRecyclerView.nestedRecyclerView.ParentAdapter;
import com.example.entregaindividual_2_anelopezmena.listasRecyclerView.nestedRecyclerView.ParentModelClass;

import java.util.ArrayList;


/*******************************************************************/
/** ----------------------- FRAG_NESTED ------------------------- **/
/*******************************************************************/
// Este es uno de los 3 Fragmentos que conviven dentro del Navigation Drawer
// que se muestra como menú principal de la aplicación. Este fragment en concreto,
// Muestra la lista vertical con listas horizontales implementadas en las clases
// del paquete 'nestedRecyclerView' del proyecto. Consiste en una pantalla
// en la que se visualizan las carátulas de las películas añadidas a la aplicación,
// además de un práctico mapa que ubica los cines 'BlockBuster!' a nivel nacional.

public class Frag_Nested extends Fragment {

    // Atributos privados de la clase
    private String email;
    private String usuario;
    private RecyclerView recyclerView;
    private FragmentNestedDrawerBinding binding;
    private ArrayList<ParentModelClass> parentModelClassArrayList;
    private ArrayList<ChildModelClass> childModelClassArrayList;

    // 3 Listas horizontales(child rv) que se muestran dentro de la vertical(parent rv)
    private ArrayList<ChildModelClass> listaFavoritos;
    private ArrayList<ChildModelClass> listaNuevos;
    private ArrayList<ChildModelClass> listaValorados;

    // Declarar Adapter del Recycler View 'Padre'
    ParentAdapter parentAdapter;

    //---------------------------------------------------------------------------------
    // 1.1) Método constructor
    public Frag_Nested(String email, String u) {
        this.email = email;
        this.usuario=u;
    }
    //---------------------------------------------------------------------------------
    // 1.2) Método constructor (Vacío)
    public Frag_Nested() {    }

    //---------------------------------------------------------------------------------
    // 2) Método ON_CREATE
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // ------------- onCreate -------------//
        // 1er evento
        super.onCreate(savedInstanceState);
        childModelClassArrayList = new ArrayList<>();
        parentModelClassArrayList = new ArrayList<>();
        listaFavoritos = new ArrayList<>();
        listaNuevos = new ArrayList<>();
        listaValorados = new ArrayList<>();
        recyclerView = new RecyclerView(getActivity());

        // Instanciar Adapter del Recycler View 'Padre'
        parentAdapter = new ParentAdapter(parentModelClassArrayList, getActivity());

        // Cargar los datos y notificar al adapter
        recyclerView.setAdapter(parentAdapter);
        parentAdapter.notifyDataSetChanged();
    }

    //---------------------------------------------------------------------------------
    // 3) Método ON_CREATE_VIEW
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // binding = vista
        binding = FragmentNestedDrawerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Obtener elemento 'recycler view'
        recyclerView = binding.getRoot().findViewById(R.id.rv_parent);
        // Settear el layout de la lista, LINEAR LAYOUT(VERTICAL), en este caso
        LinearLayoutManager vertical = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(vertical);

        Button mapa = binding.getRoot().findViewById(R.id.b_mapa);
        mapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //-- **## GEOLOCALIZACIÓN ##**--//
                // Pedir permiso al usuario para conocer su ubicación actual
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(getActivity(), Act_Mapa.class);
                    i.putExtra("var_usuario", usuario);
                    i.putExtra("var_email", email);

                    // Lanzar intent
                    startActivity(i);
                }else{
                // Pedir permiso al usuario para conocer su ubicación actual
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 15);
                    Toast.makeText(getActivity(), getResources().getString(R.string.permisosMaps), Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Inicializar las listas
        parentModelClassArrayList = new ArrayList<>();
        listaFavoritos = new ArrayList<>();
        listaNuevos = new ArrayList<>();
        listaValorados = new ArrayList<>();

        // Añadir elementos de muestra
        listaNuevos.add(new ChildModelClass(R.drawable.post_judgement_of_paris));
        listaNuevos.add(new ChildModelClass(R.drawable.post_in_time));
        listaNuevos.add(new ChildModelClass(R.drawable.post_john_wick_4));
        listaNuevos.add(new ChildModelClass(R.drawable.post_robin_hood));
        listaNuevos.add(new ChildModelClass(R.drawable.post_matrix_resurrections));
        listaNuevos.add(new ChildModelClass(R.drawable.post_muerte_en_el_nilo));


        // RecyclerView 'Padre', que contiene el título y el Recycler View 'Hijo'
        parentModelClassArrayList.add(new ParentModelClass(getResources().getString(R.string.novedades), listaNuevos));

        // Añadir elementos de muestra
        listaValorados.add(new ChildModelClass(R.drawable.post_bajocero));
        listaValorados.add(new ChildModelClass(R.drawable.post_glass_onion));
        listaValorados.add(new ChildModelClass(R.drawable.post_luther));
        listaValorados.add(new ChildModelClass(R.drawable.post_punales_por_la_espalda));
        listaValorados.add(new ChildModelClass(R.drawable.post_top_gun_maverick));
        listaValorados.add(new ChildModelClass(R.drawable.post_john_wick_parabellum));
        listaValorados.add(new ChildModelClass(R.drawable.post_matrix_resurrections));

        // RecyclerView 'Padre', que contiene el título y el Recycler View 'Hijo'
        parentModelClassArrayList.add(new ParentModelClass(getResources().getString(R.string.valoradas), listaValorados));

        // Añadir elementos de muestra
        listaFavoritos.add(new ChildModelClass(R.drawable.post_dune));
        listaFavoritos.add(new ChildModelClass(R.drawable.post_cartelera));
        listaFavoritos.add(new ChildModelClass(R.drawable.post_buscando_a_nemo));
        listaFavoritos.add(new ChildModelClass(R.drawable.post_uncharted));
        listaFavoritos.add(new ChildModelClass(R.drawable.post_in_time));
        listaFavoritos.add(new ChildModelClass(R.drawable.post_mision_imposible_fallout));
        listaFavoritos.add(new ChildModelClass(R.drawable.post_inception));

        // RecyclerView 'Padre', que contiene el título y el Recycler View 'Hijo'
        parentModelClassArrayList.add(new ParentModelClass(getResources().getString(R.string.interes), listaFavoritos));

        parentAdapter = new ParentAdapter(parentModelClassArrayList, getActivity());
        // Cargar datos de las listas
        recyclerView.setAdapter(parentAdapter);
        return root;
    }

    //---------------------------------------------------------------------------------
    // 4) Método ON_DESTROY_VIEW
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //---------------------------------------------------------------------------------
    // 5) Método ON_REQUEST_PERMISSIONS_RESULT
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 15) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Cuando se hayan concedido los permisos, llamar al método
                // que obtiene nuestra ubicación actual
                Intent i = new Intent(getActivity(), Act_Mapa.class);
                i.putExtra("var_usuario", usuario);
                i.putExtra("var_email", email);
                // i.putExtra("var_idioma", idiomaActual);
                // Lanzar intent
                startActivity(i);
            }
        }
    }

}