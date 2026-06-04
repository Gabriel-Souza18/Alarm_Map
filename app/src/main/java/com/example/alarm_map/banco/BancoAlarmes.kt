package com.example.alarm_map.banco

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Helper do banco SQLite.
 * Cria e gerencia a tabela de alarmes.
 */
class BancoAlarmes(contexto: Context) : SQLiteOpenHelper(
    contexto,
    NOME_BANCO,
    null,
    VERSAO
) {
    companion object {
        const val NOME_BANCO = "alarmes.db"
        const val VERSAO = 1

        // Tabela
        const val TABELA = "alarmes"

        // Colunas
        const val COLUNA_ID = "id"
        const val COLUNA_NOME = "nome"
        const val COLUNA_LATITUDE = "latitude"
        const val COLUNA_LONGITUDE = "longitude"
        const val COLUNA_RAIO_METROS = "raio_metros"
        const val COLUNA_ATIVO = "ativo"

        private const val CRIAR_TABELA = """
            CREATE TABLE $TABELA (
                $COLUNA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUNA_NOME TEXT NOT NULL,
                $COLUNA_LATITUDE REAL NOT NULL,
                $COLUNA_LONGITUDE REAL NOT NULL,
                $COLUNA_RAIO_METROS INTEGER NOT NULL DEFAULT 200,
                $COLUNA_ATIVO INTEGER NOT NULL DEFAULT 1
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CRIAR_TABELA)
    }

    override fun onUpgrade(db: SQLiteDatabase, versaoAntiga: Int, versaoNova: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABELA")
        onCreate(db)
    }
}
