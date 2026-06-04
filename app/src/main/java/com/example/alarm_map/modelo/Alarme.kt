package com.example.alarm_map.modelo

/**
 * Representa um alarme baseado em localização.
 *
 * @param id         Identificador único no banco de dados (0 = ainda não salvo)
 * @param nome       Nome amigável do alarme (ex: "Casa", "Trabalho")
 * @param latitude   Latitude do ponto central do alarme
 * @param longitude  Longitude do ponto central do alarme
 * @param raioMetros Raio em metros a partir do ponto central
 * @param ativo      Se verdadeiro, o serviço verifica este alarme; caso contrário, ignora
 */
data class Alarme(
    val id: Long = 0,
    val nome: String,
    val latitude: Double,
    val longitude: Double,
    val raioMetros: Int = 200,
    val ativo: Boolean = true,
    val apenasVibrar: Boolean = false
)
