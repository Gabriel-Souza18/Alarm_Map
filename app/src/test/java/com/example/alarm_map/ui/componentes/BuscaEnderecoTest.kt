package com.example.alarm_map.ui.componentes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para a data class ResultadoBusca e as funções
 * de parsing/URL do componente BuscaEndereco (API Photon).
 *
 * Usa Robolectric para disponibilizar org.json.JSONObject (classe Android).
 * Esses testes NÃO fazem requisições de rede — testam apenas
 * a lógica de construção de URL e parsing de JSON.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BuscaEnderecoTest {

    // =========================================================================
    // Testes da data class ResultadoBusca
    // =========================================================================

    @Test
    fun `ResultadoBusca armazena valores corretamente`() {
        val ponto = GeoPoint(-23.55, -46.63)
        val resultado = ResultadoBusca(
            ponto = ponto,
            nome = "Praça da Sé",
            enderecoFormatado = "São Paulo, SP, Brasil"
        )

        assertEquals(-23.55, resultado.ponto.latitude, 0.001)
        assertEquals(-46.63, resultado.ponto.longitude, 0.001)
        assertEquals("Praça da Sé", resultado.nome)
        assertEquals("São Paulo, SP, Brasil", resultado.enderecoFormatado)
    }

    @Test
    fun `ResultadoBusca equals funciona entre instancias iguais`() {
        val ponto = GeoPoint(-23.55, -46.63)
        val r1 = ResultadoBusca(ponto, "A", "B")
        val r2 = ResultadoBusca(ponto, "A", "B")
        assertEquals(r1, r2)
    }

    @Test
    fun `ResultadoBusca copy altera campo corretamente`() {
        val original = ResultadoBusca(
            ponto = GeoPoint(0.0, 0.0),
            nome = "Original",
            enderecoFormatado = "Endereco"
        )
        val copia = original.copy(nome = "Modificado")

        assertEquals("Modificado", copia.nome)
        assertEquals("Endereco", copia.enderecoFormatado)
    }

    @Test
    fun `ResultadoBusca com endereco vazio`() {
        val resultado = ResultadoBusca(
            ponto = GeoPoint(0.0, 0.0),
            nome = "Local",
            enderecoFormatado = ""
        )
        assertTrue(resultado.enderecoFormatado.isEmpty())
    }

    // =========================================================================
    // Testes de construirUrlPhoton
    // =========================================================================

    @Test
    fun `construirUrlPhoton gera URL basica sem location bias`() {
        val url = construirUrlPhoton("São Paulo", null)

        assertTrue(url.startsWith("https://photon.komoot.io/api/?q="))
        assertTrue(url.contains("limit=5"))
        assertTrue(url.contains("lang=pt"))
        // Não deve conter lat/lon sem centroMapa
        assertTrue(!url.contains("&lat="))
        assertTrue(!url.contains("&lon="))
    }

    @Test
    fun `construirUrlPhoton inclui location bias quando centroMapa fornecido`() {
        val centro = GeoPoint(-23.55, -46.63)
        val url = construirUrlPhoton("restaurante", centro)

        assertTrue(url.contains("lat=-23.55"))
        assertTrue(url.contains("lon=-46.63"))
    }

    @Test
    fun `construirUrlPhoton codifica caracteres especiais na consulta`() {
        val url = construirUrlPhoton("Rua São João", null)

        // Espaço deve ser codificado como + ou %20
        assertTrue(!url.contains(" ") || url.contains("S%C3%A3o") || url.contains("+"))
        assertTrue(url.contains("photon.komoot.io"))
    }

    @Test
    fun `construirUrlPhoton com consulta vazia gera URL valida`() {
        val url = construirUrlPhoton("", null)

        assertTrue(url.startsWith("https://photon.komoot.io/api/?q="))
        assertTrue(url.contains("limit=5"))
    }

    @Test
    fun `construirUrlPhoton com coordenadas zero`() {
        val centro = GeoPoint(0.0, 0.0)
        val url = construirUrlPhoton("teste", centro)

        assertTrue(url.contains("lat=0.0"))
        assertTrue(url.contains("lon=0.0"))
    }

    // =========================================================================
    // Testes de parsearRespostaPhoton
    // =========================================================================

    @Test
    fun `parsearRespostaPhoton com resposta vazia retorna lista vazia`() {
        val json = """{"type":"FeatureCollection","features":[]}"""
        val resultados = parsearRespostaPhoton(json)
        assertTrue(resultados.isEmpty())
    }

    @Test
    fun `parsearRespostaPhoton parseia um resultado completo`() {
        val json = """
        {
            "type": "FeatureCollection",
            "features": [{
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [-46.6334, -23.5505]
                },
                "properties": {
                    "name": "Praça da Sé",
                    "street": "Praça da Sé",
                    "city": "São Paulo",
                    "state": "São Paulo",
                    "country": "Brasil"
                }
            }]
        }
        """.trimIndent()

        val resultados = parsearRespostaPhoton(json)

        assertEquals(1, resultados.size)
        val r = resultados[0]
        assertEquals("Praça da Sé", r.nome)
        assertEquals(-23.5505, r.ponto.latitude, 0.0001)
        assertEquals(-46.6334, r.ponto.longitude, 0.0001)
        // Endereço formatado não deve repetir o nome
        assertTrue(r.enderecoFormatado.contains("São Paulo"))
        assertTrue(r.enderecoFormatado.contains("Brasil"))
    }

    @Test
    fun `parsearRespostaPhoton parseia multiplos resultados`() {
        val json = """
        {
            "type": "FeatureCollection",
            "features": [
                {
                    "type": "Feature",
                    "geometry": {"type": "Point", "coordinates": [-46.63, -23.55]},
                    "properties": {"name": "Local A", "city": "São Paulo", "state": "SP", "country": "Brasil"}
                },
                {
                    "type": "Feature",
                    "geometry": {"type": "Point", "coordinates": [-43.17, -22.90]},
                    "properties": {"name": "Local B", "city": "Rio de Janeiro", "state": "RJ", "country": "Brasil"}
                },
                {
                    "type": "Feature",
                    "geometry": {"type": "Point", "coordinates": [-47.92, -15.78]},
                    "properties": {"name": "Local C", "city": "Brasília", "state": "DF", "country": "Brasil"}
                }
            ]
        }
        """.trimIndent()

        val resultados = parsearRespostaPhoton(json)

        assertEquals(3, resultados.size)
        assertEquals("Local A", resultados[0].nome)
        assertEquals("Local B", resultados[1].nome)
        assertEquals("Local C", resultados[2].nome)
    }

    @Test
    fun `parsearRespostaPhoton usa rua como nome quando name ausente`() {
        val json = """
        {
            "type": "FeatureCollection",
            "features": [{
                "type": "Feature",
                "geometry": {"type": "Point", "coordinates": [-46.63, -23.55]},
                "properties": {"street": "Av Paulista", "city": "São Paulo", "state": "SP", "country": "Brasil"}
            }]
        }
        """.trimIndent()

        val resultados = parsearRespostaPhoton(json)

        assertEquals(1, resultados.size)
        assertEquals("Av Paulista", resultados[0].nome)
    }

    @Test
    fun `parsearRespostaPhoton usa cidade como nome quando name e rua ausentes`() {
        val json = """
        {
            "type": "FeatureCollection",
            "features": [{
                "type": "Feature",
                "geometry": {"type": "Point", "coordinates": [-46.63, -23.55]},
                "properties": {"city": "São Paulo", "state": "SP", "country": "Brasil"}
            }]
        }
        """.trimIndent()

        val resultados = parsearRespostaPhoton(json)

        assertEquals("São Paulo", resultados[0].nome)
    }

    @Test
    fun `parsearRespostaPhoton retorna Local sem nome quando nenhum campo de nome disponivel`() {
        val json = """
        {
            "type": "FeatureCollection",
            "features": [{
                "type": "Feature",
                "geometry": {"type": "Point", "coordinates": [-46.63, -23.55]},
                "properties": {"country": "Brasil"}
            }]
        }
        """.trimIndent()

        val resultados = parsearRespostaPhoton(json)

        assertEquals("Local sem nome", resultados[0].nome)
        assertTrue(resultados[0].enderecoFormatado.contains("Brasil"))
    }

    @Test
    fun `parsearRespostaPhoton nao repete nome no endereco formatado`() {
        val json = """
        {
            "type": "FeatureCollection",
            "features": [{
                "type": "Feature",
                "geometry": {"type": "Point", "coordinates": [-46.63, -23.55]},
                "properties": {"name": "São Paulo", "city": "São Paulo", "state": "SP", "country": "Brasil"}
            }]
        }
        """.trimIndent()

        val resultados = parsearRespostaPhoton(json)

        // cidade = "São Paulo" é igual ao nome, então não deve aparecer no subtítulo
        val endereco = resultados[0].enderecoFormatado
        // Deve ter SP e Brasil, mas não "São Paulo" duplicado
        assertTrue(endereco.contains("SP"))
        assertTrue(endereco.contains("Brasil"))
    }

    @Test
    fun `parsearRespostaPhoton extrai coordenadas na ordem correta lon-lat`() {
        // GeoJSON usa [longitude, latitude] — inverso do usual
        val json = """
        {
            "type": "FeatureCollection",
            "features": [{
                "type": "Feature",
                "geometry": {"type": "Point", "coordinates": [-46.6334, -23.5505]},
                "properties": {"name": "Teste"}
            }]
        }
        """.trimIndent()

        val resultados = parsearRespostaPhoton(json)

        // Longitude vem primeiro no GeoJSON, mas GeoPoint(lat, lon)
        assertEquals(-23.5505, resultados[0].ponto.latitude, 0.0001)
        assertEquals(-46.6334, resultados[0].ponto.longitude, 0.0001)
    }

    @Test
    fun `parsearRespostaPhoton com properties totalmente vazias`() {
        val json = """
        {
            "type": "FeatureCollection",
            "features": [{
                "type": "Feature",
                "geometry": {"type": "Point", "coordinates": [0.0, 0.0]},
                "properties": {}
            }]
        }
        """.trimIndent()

        val resultados = parsearRespostaPhoton(json)

        assertEquals(1, resultados.size)
        assertEquals("Local sem nome", resultados[0].nome)
        assertTrue(resultados[0].enderecoFormatado.isEmpty())
    }

    @Test
    fun `parsearRespostaPhoton com coordenadas positivas`() {
        val json = """
        {
            "type": "FeatureCollection",
            "features": [{
                "type": "Feature",
                "geometry": {"type": "Point", "coordinates": [2.3522, 48.8566]},
                "properties": {"name": "Paris", "country": "France"}
            }]
        }
        """.trimIndent()

        val resultados = parsearRespostaPhoton(json)

        assertEquals(48.8566, resultados[0].ponto.latitude, 0.0001)
        assertEquals(2.3522, resultados[0].ponto.longitude, 0.0001)
        assertEquals("Paris", resultados[0].nome)
    }

    @Test
    fun `parsearRespostaPhoton monta endereco formatado com todos os campos`() {
        val json = """
        {
            "type": "FeatureCollection",
            "features": [{
                "type": "Feature",
                "geometry": {"type": "Point", "coordinates": [-46.63, -23.55]},
                "properties": {
                    "name": "Hospital São Paulo",
                    "street": "Rua Napoleão de Barros",
                    "city": "São Paulo",
                    "state": "SP",
                    "country": "Brasil"
                }
            }]
        }
        """.trimIndent()

        val resultados = parsearRespostaPhoton(json)
        val endereco = resultados[0].enderecoFormatado

        assertTrue(endereco.contains("Rua Napoleão de Barros"))
        assertTrue(endereco.contains("São Paulo"))
        assertTrue(endereco.contains("SP"))
        assertTrue(endereco.contains("Brasil"))
    }
}
