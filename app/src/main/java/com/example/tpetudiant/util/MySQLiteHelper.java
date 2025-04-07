package com.example.tpetudiant.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 20;
    private static final String DATABASE_NAME = "ecole";

    // Table et colonnes
    public static final String TABLE_ETUDIANT = "etudiant";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOM = "nom";
    public static final String COLUMN_PRENOM = "prenom";
    public static final String COLUMN_DATE_NAISSANCE = "date_naissance";
    public static final String COLUMN_IMAGE = "image";

    // Script de création de la table avec les nouveaux champs
    private static final String CREATE_TABLE_ETUDIANT = "CREATE TABLE " + TABLE_ETUDIANT + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NOM + " TEXT,"
            + COLUMN_PRENOM + " TEXT,"
            + COLUMN_DATE_NAISSANCE + " TEXT,"
            + COLUMN_IMAGE + " BLOB)";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ETUDIANT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Dans une application en production, vous pourriez vouloir migrer les données
        // plutôt que de supprimer et recréer la table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ETUDIANT);
        onCreate(db);
    }
}