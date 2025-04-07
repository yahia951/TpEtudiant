package com.example.tpetudiant;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tpetudiant.Adapter.EtudiantAdapter;
import com.example.tpetudiant.Classes.Etudiant;
import com.example.tpetudiant.service.EtudiantService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements EtudiantAdapter.OnStudentActionListener {

    private EditText inputNom, inputPrenom, inputId;
    private TextView textDateNaissance, textResultat;
    private Button btnValider, btnChercher, btnSupprimer, btnSelectDate, btnSelectImage, btnAfficherEtudiants;
    private RecyclerView recyclerView;
    private EtudiantAdapter adapter;
    private EtudiantService etudiantService;
    private ImageView imageEtudiant;
    private String imagePathSelected;
    private String dateNaissanceSelected;

    // Launcher pour la sélection d'image
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        // Redimensionner l'image pour éviter les problèmes de mémoire
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

                        // Sauvegarder l'image dans le stockage interne de l'application
                        String imagePath = saveImageToInternalStorage(resizedBitmap);
                        imagePathSelected = imagePath;

                        // Afficher l'image
                        imageEtudiant.setImageBitmap(resizedBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation du service étudiant
        etudiantService = new EtudiantService(this);

        // Liaison des vues
        inputNom = findViewById(R.id.input_nom);
        inputPrenom = findViewById(R.id.input_prenom);
        inputId = findViewById(R.id.input_id);
        textDateNaissance = findViewById(R.id.text_date_naissance);
        textResultat = findViewById(R.id.text_resultat);
        btnValider = findViewById(R.id.btn_valider);
        btnChercher = findViewById(R.id.btn_chercher);
        btnSupprimer = findViewById(R.id.btn_supprimer);
        btnSelectDate = findViewById(R.id.btn_select_date);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnAfficherEtudiants = findViewById(R.id.btn_afficher_etudiants);
        imageEtudiant = findViewById(R.id.image_etudiant);
        recyclerView = findViewById(R.id.recyclerView);

        // Configuration du RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EtudiantAdapter(this, etudiantService.findAll(), this);
        recyclerView.setAdapter(adapter);

        // Configuration du DatePicker
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        // Configuration de la sélection d'image
        btnSelectImage.setOnClickListener(v -> selectImage());

        // Action du bouton Valider
        btnValider.setOnClickListener(v -> {
            String nom = inputNom.getText().toString().trim();
            String prenom = inputPrenom.getText().toString().trim();

            if (nom.isEmpty() || prenom.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir au moins le nom et le prénom", Toast.LENGTH_SHORT).show();
                return;
            }

            // Vérifier si c'est une mise à jour ou un nouvel ajout
            String idText = inputId.getText().toString().trim();
            if (!idText.isEmpty()) {
                try {
                    int id = Integer.parseInt(idText);
                    Etudiant existingEtudiant = etudiantService.findById(id);
                    if (existingEtudiant != null) {
                        // Mise à jour
                        existingEtudiant.setNom(nom);
                        existingEtudiant.setPrenom(prenom);
                        existingEtudiant.setDateNaissance(dateNaissanceSelected);
                        existingEtudiant.setImage(imagePathSelected);

                        etudiantService.update(existingEtudiant);
                        Toast.makeText(this, "Étudiant mis à jour avec succès", Toast.LENGTH_SHORT).show();
                        clearFields();
                        loadStudents();
                        return;
                    }
                } catch (NumberFormatException e) {
                    // Ignorer, continuer avec la création
                }
            }

            // Création d'un nouvel étudiant
            Etudiant etudiant = new Etudiant(nom, prenom, dateNaissanceSelected, imagePathSelected);
            etudiantService.create(etudiant);
            Toast.makeText(this, "Étudiant ajouté avec succès", Toast.LENGTH_SHORT).show();
            clearFields();
            loadStudents();
        });

        // Action du bouton Chercher
        btnChercher.setOnClickListener(v -> {
            String idText = inputId.getText().toString().trim();

            if (idText.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un ID", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int studentId = Integer.parseInt(idText);
                Etudiant etudiant = etudiantService.findById(studentId);

                if (etudiant != null) {
                    StringBuilder infoBuilder = new StringBuilder();
                    infoBuilder.append("ID: ").append(etudiant.getId()).append("\n");
                    infoBuilder.append("Nom: ").append(etudiant.getNom()).append("\n");
                    infoBuilder.append("Prénom: ").append(etudiant.getPrenom());

                    if (etudiant.getDateNaissance() != null && !etudiant.getDateNaissance().isEmpty()) {
                        infoBuilder.append("\nDate: ").append(etudiant.getDateNaissance());
                    } else {
                        infoBuilder.append("\nDate: Non renseignée");
                    }

                    textResultat.setText(infoBuilder.toString());

                    // Afficher l'image si disponible
                    if (etudiant.getImage() != null && !etudiant.getImage().isEmpty()) {
                        try {
                            File imgFile = new File(etudiant.getImage());
                            if (imgFile.exists()) {
                                Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                imageEtudiant.setImageBitmap(bitmap);
                            } else {
                                imageEtudiant.setImageResource(android.R.drawable.ic_menu_gallery);
                            }
                        } catch (Exception e) {
                            imageEtudiant.setImageResource(android.R.drawable.ic_menu_gallery);
                            e.printStackTrace();
                        }
                    } else {
                        imageEtudiant.setImageResource(android.R.drawable.ic_menu_gallery);
                    }

                    // Préremplir les champs pour modification éventuelle
                    inputNom.setText(etudiant.getNom());
                    inputPrenom.setText(etudiant.getPrenom());
                    dateNaissanceSelected = etudiant.getDateNaissance();
                    if (dateNaissanceSelected != null && !dateNaissanceSelected.isEmpty()) {
                        textDateNaissance.setText(dateNaissanceSelected);
                    } else {
                        textDateNaissance.setText("Non sélectionnée");
                    }
                    imagePathSelected = etudiant.getImage();
                } else {
                    textResultat.setText("Étudiant introuvable");
                    imageEtudiant.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "ID invalide", Toast.LENGTH_SHORT).show();
            }
        });

        // Action du bouton Supprimer
        btnSupprimer.setOnClickListener(v -> {
            String idText = inputId.getText().toString().trim();

            if (idText.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un ID", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int studentId = Integer.parseInt(idText);
                Etudiant etudiant = etudiantService.findById(studentId);

                if (etudiant != null) {
                    etudiantService.delete(etudiant);
                    Toast.makeText(this, "Étudiant supprimé avec succès", Toast.LENGTH_SHORT).show();
                    clearFields();
                    loadStudents();
                } else {
                    Toast.makeText(this, "Étudiant introuvable", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "ID invalide", Toast.LENGTH_SHORT).show();
            }
        });

        // Action du bouton Afficher tous les étudiants
        btnAfficherEtudiants.setOnClickListener(v -> loadStudents());

        // Chargement initial des étudiants
        loadStudents();
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    dateNaissanceSelected = String.format(Locale.FRANCE, "%02d/%02d/%d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    textDateNaissance.setText(dateNaissanceSelected);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        try {
            // Créer un nom de fichier unique
            String fileName = "img_" + UUID.randomUUID().toString() + ".jpg";
            File directory = getFilesDir();
            File file = new File(directory, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
            fos.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void clearFields() {
        inputNom.setText("");
        inputPrenom.setText("");
        inputId.setText("");
        textDateNaissance.setText("Non sélectionnée");
        textResultat.setText("");
        imageEtudiant.setImageResource(android.R.drawable.ic_menu_gallery);
        dateNaissanceSelected = null;
        imagePathSelected = null;
    }

    private void loadStudents() {
        List<Etudiant> etudiants = etudiantService.findAll();
        adapter.setEtudiants(etudiants);  // Mettre à jour l'adaptateur avec les nouvelles données
    }

    // Implémentation des méthodes de l'interface OnStudentActionListener
    @Override
    public void onUpdateStudent(Etudiant etudiant) {
        if (etudiant != null) {
            etudiantService.update(etudiant);
            Toast.makeText(this, "Étudiant mis à jour avec succès", Toast.LENGTH_SHORT).show();
            loadStudents();
        }
    }

    @Override
    public void onDeleteStudent(int id) {
        Etudiant etudiant = etudiantService.findById(id);
        if (etudiant != null) {
            etudiantService.delete(etudiant);
            Toast.makeText(this, "Étudiant supprimé avec succès", Toast.LENGTH_SHORT).show();
            loadStudents();
        } else {
            Toast.makeText(this, "Étudiant introuvable", Toast.LENGTH_SHORT).show();
        }
    }
}