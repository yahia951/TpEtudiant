package com.example.tpetudiant.Adapter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tpetudiant.Classes.Etudiant;
import com.example.tpetudiant.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder> {
    private List<Etudiant> etudiants;
    private final Context context;
    private final OnStudentActionListener listener;

    private AlertDialog currentDialog;
    private ImageView currentImageView;
    private String currentImagePath;

    private final ActivityResultLauncher<Intent> imageResultLauncher;

    public EtudiantAdapter(Context context, List<Etudiant> etudiants, OnStudentActionListener listener) {
        this.context = context;
        this.etudiants = etudiants;
        this.listener = listener;

        imageResultLauncher = ((AppCompatActivity) context).registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        Intent data = result.getData();
                        handleImageResult(data);
                    }
                });
    }

    public void setEtudiants(List<Etudiant> newEtudiants) {
        this.etudiants = newEtudiants;
        notifyDataSetChanged();
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    private Bitmap loadImageFromPath(String imagePath) {
        try {
            File file = new File(imagePath);
            if (file.exists()) {
                return BitmapFactory.decodeFile(imagePath);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        return String.format(Locale.FRANCE, "%02d/%02d/%d", day, month, year);
    }

    @Override
    public EtudiantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate le layout pour chaque élément de la liste
        View view = LayoutInflater.from(context).inflate(R.layout.item_etudiant, parent, false);
        return new EtudiantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EtudiantViewHolder holder, int position) {
        if (etudiants != null && position < etudiants.size()) {
            Etudiant etudiant = etudiants.get(position);
            String iid=" ID: " + etudiant.getId();
            holder.id.setText(iid);
            holder.nom.setText(etudiant.getNom());
            holder.prenom.setText(etudiant.getPrenom());

            // Debug logs
            System.out.println("Étudiant position " + position +
                    " ID: " + etudiant.getId() +
                    " Date: " + etudiant.getDateNaissance());

            String dateDebug = "Date directe: " + etudiant.getDateNaissance();
            holder.dateNaissance.setText(etudiant.getDateNaissance());
            holder.dateNaissance.setVisibility(View.VISIBLE);

            // Afficher l'image à partir du chemin
            if (etudiant.getImage() != null && !etudiant.getImage().isEmpty()) {
                Bitmap bitmap = loadImageFromPath(etudiant.getImage());
                if (bitmap != null) {
                    holder.image.setImageBitmap(bitmap);
                    holder.image.setVisibility(View.VISIBLE);
                } else {
                    holder.image.setVisibility(View.GONE);
                }
            } else {
                holder.image.setVisibility(View.GONE);
            }

            // Gestion du clic pour afficher les options
            holder.itemView.setOnClickListener(v -> showOptionsDialog(etudiant));
        }
    }

    @Override
    public int getItemCount() {
        return etudiants != null ? etudiants.size() : 0;
    }

    public static class EtudiantViewHolder extends RecyclerView.ViewHolder {
        TextView id, nom, prenom, dateNaissance;
        ImageView image;

        public EtudiantViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.input_id);
            nom = itemView.findViewById(R.id.input_nom);
            prenom = itemView.findViewById(R.id.input_prenom);
            dateNaissance = itemView.findViewById(R.id.text_date_naissance);
            image = itemView.findViewById(R.id.image_etudiant);
        }
    }

    private void showOptionsDialog(Etudiant etudiant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Options");
        builder.setMessage("Que voulez-vous faire ?");

        builder.setPositiveButton("Modifier", (dialog, which) -> showEditDialog(etudiant));

        builder.setNegativeButton("Supprimer", (dialog, which) -> {
            if (listener != null) {
                listener.onDeleteStudent(etudiant.getId());
                Toast.makeText(context, "Étudiant supprimé", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNeutralButton("Annuler", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showEditDialog(Etudiant etudiant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Modifier l'étudiant");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.activity_student_edit, null);
        EditText inputNom = dialogView.findViewById(R.id.dialog_input_nom);
        EditText inputPrenom = dialogView.findViewById(R.id.dialog_input_prenom);
        TextView textDateNaissance = dialogView.findViewById(R.id.dialog_text_date);
        Button btnSelectDate = dialogView.findViewById(R.id.dialog_btn_date);
        ImageView imageView = dialogView.findViewById(R.id.dialog_image);
        Button btnSelectImage = dialogView.findViewById(R.id.dialog_btn_image);

        currentImagePath = etudiant.getImage();
        currentImageView = imageView;

        final String[] newDateNaissance = {etudiant.getDateNaissance()};

        inputNom.setText(etudiant.getNom());
        inputPrenom.setText(etudiant.getPrenom());

        if (etudiant.getDateNaissance() != null && !etudiant.getDateNaissance().isEmpty()) {
            textDateNaissance.setText(etudiant.getDateNaissance());
        } else {
            textDateNaissance.setText("Non sélectionnée");
        }

        // Charger l'image à partir du chemin stocké
        if (etudiant.getImage() != null && !etudiant.getImage().isEmpty()) {
            Bitmap bitmap = loadImageFromPath(etudiant.getImage());
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        btnSelectDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        newDateNaissance[0] = String.format(Locale.FRANCE, "%02d/%02d/%d",
                                selectedDay, selectedMonth + 1, selectedYear);
                        textDateNaissance.setText(newDateNaissance[0]);
                    }, year, month, day);
            datePickerDialog.show();
        });

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imageResultLauncher.launch(intent);
        });

        builder.setView(dialogView);

        builder.setPositiveButton("Modifier", (dialog, which) -> {
            String nouveauNom = inputNom.getText().toString().trim();
            String nouveauPrenom = inputPrenom.getText().toString().trim();

            if (nouveauNom.isEmpty() || nouveauPrenom.isEmpty()) {
                Toast.makeText(context, "Nom et prénom sont obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            etudiant.setNom(nouveauNom);
            etudiant.setPrenom(nouveauPrenom);
            etudiant.setDateNaissance(newDateNaissance[0]);
            etudiant.setImage(currentImagePath);

            if (listener != null) {
                listener.onUpdateStudent(etudiant);
                Toast.makeText(context, "Étudiant modifié", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        currentDialog = dialog;
        dialog.show();
    }

    public void handleImageResult(Intent data) {
        if (data != null && currentImageView != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                // Obtenir le chemin réel du fichier à partir de l'URI
                currentImagePath = getRealPathFromURI(imageUri);

                // Charger et afficher l'aperçu de l'image
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                    currentImageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public interface OnStudentActionListener {
        void onUpdateStudent(Etudiant etudiant);
        void onDeleteStudent(int id);
    }
}