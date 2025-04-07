package com.example.tpetudiant.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.tpetudiant.Classes.Etudiant;
import com.example.tpetudiant.util.MySQLiteHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EtudiantService {
    private static final String TABLE_NAME = "etudiant";
    private static final String KEY_ID = "id";
    private static final String KEY_NOM = "nom";
    private static final String KEY_PRENOM = "prenom";
    private static final String KEY_DATE_NAISSANCE = "date_naissance";
    private static final String KEY_IMAGE_PATH = "image";

    private static String[] COLUMNS = {KEY_ID, KEY_NOM, KEY_PRENOM, KEY_DATE_NAISSANCE, KEY_IMAGE_PATH};

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);

    private MySQLiteHelper helper;

    public EtudiantService(Context context) {
        this.helper = new MySQLiteHelper(context);
    }

    public void create(Etudiant e) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOM, e.getNom());
        values.put(KEY_PRENOM, e.getPrenom());
        values.put(KEY_DATE_NAISSANCE, e.getDateNaissance());

        // Stocker le chemin de l'image dans la base de données
        if (e.getImage() != null) {
            values.put(KEY_IMAGE_PATH, e.getImage());
        }

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void update(Etudiant e) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOM, e.getNom());
        values.put(KEY_PRENOM, e.getPrenom());
        values.put(KEY_DATE_NAISSANCE, e.getDateNaissance());

        // Mettre à jour le chemin de l'image
        if (e.getImage() != null) {
            values.put(KEY_IMAGE_PATH, e.getImage());
        }

        db.update(TABLE_NAME, values, "id = ?", new String[]{String.valueOf(e.getId())});
        db.close();
    }

    public Etudiant findById(int id) {
        Etudiant e = null;
        SQLiteDatabase db = this.helper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, COLUMNS, "id = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (c.moveToFirst()) {
            e = new Etudiant();
            e.setId(c.getInt(0));
            e.setNom(c.getString(1));
            e.setPrenom(c.getString(2));
            e.setDateNaissance(c.getString(3));

            // Récupérer le chemin de l'image
            String imagePath = c.getString(4);
            if (imagePath != null && !imagePath.isEmpty()) {
                e.setImage(imagePath);  // Assigner le chemin de l'image
            }
        }
        c.close();
        db.close();
        return e;
    }


    public void delete(Etudiant e) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        db.delete(TABLE_NAME, "id = ?", new String[]{String.valueOf(e.getId())});
        db.close();
    }

    public List<Etudiant> findAll() {
        List<Etudiant> eds = new ArrayList<>();
        SQLiteDatabase db = this.helper.getReadableDatabase(); // Utilisez writable pour pouvoir supprimer des données


        // Revenir en mode lecture pour récupérer les données
        db = this.helper.getReadableDatabase();
        String req = "SELECT * FROM " + TABLE_NAME;

        Log.d("INFO", "Exécution de la requête : " + req); // Affiche la requête SQL exécutée

        Cursor c = db.rawQuery(req, null);
        Etudiant e = null;

        // Vérifier s'il y a des colonnes avant d'extraire les données
        int columnCount = c.getColumnCount();
        Log.d("INFO", "Nombre de colonnes : " + columnCount); // Affiche le nombre de colonnes dans la réponse

        if (c.moveToFirst()) {
            do {
                e = new Etudiant();

                // Extraire l'id
                e.setId(c.getInt(0));
                Log.d("INFO", "ID extrait : " + e.getId()); // Affiche l'ID extrait

                // Extraire le nom
                e.setNom(c.getString(1));
                Log.d("INFO", "Nom extrait : " + e.getNom()); // Affiche le nom extrait

                // Extraire le prénom
                e.setPrenom(c.getString(2));
                Log.d("INFO", "Prénom extrait : " + e.getPrenom()); // Affiche le prénom extrait

                // Vérifier si la colonne date existe avant de l'utiliser
                if (columnCount > 3) {
                    String dateStr = c.getString(3);
                    Log.d("INFO", "Date brute extraite : " + dateStr); // Affiche la date brute extraite

                    if (dateStr != null && !dateStr.isEmpty()) {
                        // Solution directe : stockez la date telle quelle sans essayer de la parser
                        e.setDateNaissance(dateStr);
                        Log.d("INFO", "Date stockée directement : " + e.getDateNaissance());

                        /* Ancien code avec parsing qui causait l'erreur
                        try {
                            Date date = DATE_FORMAT.parse(dateStr);
                            e.setDateNaissance(DATE_FORMAT.format(date)); // Stocker la date formatée
                            Log.d("INFO", "Date formatée : " + e.getDateNaissance()); // Affiche la date formatée
                        } catch (ParseException ex) {
                            Log.e("DB_ERROR", "Erreur lors du parsing de la date : " + dateStr, ex);
                        }
                        */
                    } else {
                        Log.d("INFO", "Date vide ou null, aucune modification de la date."); // Affiche un message si la date est vide ou null
                    }
                }

                // Vérifier si la colonne image existe avant de l'utiliser
                if (columnCount > 4) {
                    String imagePath = c.getString(4);
                    Log.d("INFO", "Chemin d'image extrait : " + imagePath); // Affiche le chemin d'image extrait

                    if (imagePath != null && !imagePath.isEmpty()) {
                        e.setImage(imagePath); // Stocker le chemin de l'image
                        Log.d("INFO", "Image ajoutée : " + e.getImage()); // Affiche le chemin de l'image ajouté
                    } else {
                        Log.d("INFO", "Aucun chemin d'image fourni."); // Affiche un message si l'image est vide ou null
                    }
                }

                eds.add(e);
                Log.d("INFO", "Ajouté Etudiant - ID: " + e.getId() + ", Nom: " + e.getNom() + ", Prénom: " + e.getPrenom());

            } while (c.moveToNext());
        } else {
            Log.d("INFO", "Aucun étudiant trouvé dans la base de données.");
        }

        // Fermer le curseur et la connexion à la base
        c.close();
        db.close();

        // Afficher tous les étudiants restants dans la base
        Log.d("INFO", "Liste des étudiants restants après suppression : ");
        for (Etudiant etudiant : eds) {
            Log.d("INFO", "ID: " + etudiant.getId() + ", Nom: " + etudiant.getNom() + ", Prénom: " + etudiant.getPrenom());
        }

        Log.d("INFO", "Retour de la liste d'étudiants avec " + eds.size() + " éléments.");
        return eds;
    }




}