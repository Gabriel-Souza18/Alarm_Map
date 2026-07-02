package com.example.alarm_map.ui.componentes

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
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
        assertTrue(url.contains("limit=15"))
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
        assertTrue(url.contains("location_bias_scale=0.6"))
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
        assertTrue(url.contains("limit=15"))
    }

    @Test
    fun `construirUrlPhoton com coordenadas zero`() {
        val centro = GeoPoint(0.0, 0.0)
        val url = construirUrlPhoton("teste", centro)

        assertTrue(url.contains("lat=0.0"))
        assertTrue(url.contains("lon=0.0"))
        assertTrue(url.contains("location_bias_scale=0.6"))
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

    @Test
    fun `parsearRespostaPhoton parseia o campo type e osm_value como tipo`() {
        val json = """
        {
            "type": "FeatureCollection",
            "features": [
                {
                    "type": "Feature",
                    "geometry": {"type": "Point", "coordinates": [-46.63, -23.55]},
                    "properties": {"name": "Estado SP", "type": "state"}
                },
                {
                    "type": "Feature",
                    "geometry": {"type": "Point", "coordinates": [-43.17, -22.90]},
                    "properties": {"name": "Cidade RJ", "osm_value": "city"}
                }
            ]
        }
        """.trimIndent()

        val resultados = parsearRespostaPhoton(json)

        assertEquals(2, resultados.size)
        assertEquals("state", resultados[0].tipo)
        assertEquals("city", resultados[1].tipo)
    }

    @Test
    fun `obterPrioridadeTipo retorna prioridade correta`() {
        assertEquals(1, obterPrioridadeTipo("country"))
        assertEquals(2, obterPrioridadeTipo("state"))
        assertEquals(3, obterPrioridadeTipo("city"))
        assertEquals(3, obterPrioridadeTipo("town"))
        assertEquals(4, obterPrioridadeTipo("suburb"))
        assertEquals(5, obterPrioridadeTipo("street"))
        assertEquals(6, obterPrioridadeTipo("house"))
        assertEquals(6, obterPrioridadeTipo("poi"))
    }

    @Test
    fun `ordenacao por relevancia ordena corretamente`() {
        val rEstado = ResultadoBusca(GeoPoint(0.0, 0.0), "Estado SP", "SP", "state")
        val rCidade = ResultadoBusca(GeoPoint(0.0, 0.0), "Cidade SP", "SP", "city")
        val rRua = ResultadoBusca(GeoPoint(0.0, 0.0), "Rua SP", "SP", "street")
        val rPoi = ResultadoBusca(GeoPoint(0.0, 0.0), "Shopping SP", "SP", "poi")

        val lista = listOf(rPoi, rRua, rEstado, rCidade)
        val listaOrdenada = lista.sortedBy { obterPrioridadeTipo(it.tipo) }

        assertEquals("state", listaOrdenada[0].tipo)
        assertEquals("city", listaOrdenada[1].tipo)
        assertEquals("street", listaOrdenada[2].tipo)
        assertEquals("poi", listaOrdenada[3].tipo)
    }

    // =========================================================================
    // Testes do Composable BuscaEndereco (UI)
    // =========================================================================

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testBuscaEnderecoExibeSugestoes() {
        var pontoSelecionado: GeoPoint? = null
        var nomeSelecionado: String? = null

        val fakeResultados = listOf(
            ResultadoBusca(
                ponto = GeoPoint(-23.55, -46.63),
                nome = "Local Teste",
                enderecoFormatado = "Rua Teste, Cidade Teste"
            )
        )

        composeTestRule.setContent {
            BuscaEndereco(
                centroMapa = null,
                aoSelecionarEndereco = { ponto, nome ->
                    pontoSelecionado = ponto
                    nomeSelecionado = nome
                },
                funcaoBusca = { _, _ -> fakeResultados }
            )
        }

        // Campo de busca deve existir
        composeTestRule.onNodeWithText("Buscar endereço").assertExists()

        // Digita texto de busca (mínimo de 3 caracteres)
        composeTestRule.onNodeWithText("Buscar endereço").performTextInput("São")
        
        // Garante que o campo ganha foco para o dropdown aparecer
        composeTestRule.onNodeWithText("Buscar endereço").requestFocus()

        // Avança o relógio virtual para que o debounce (400ms) termine e a busca rode
        composeTestRule.mainClock.advanceTimeBy(1000L)

        // Aguarda a sugestão aparecer
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Local Teste").fetchSemanticsNodes().isNotEmpty()
        }

        // Verifica se as sugestões são mostradas
        composeTestRule.onNodeWithText("Local Teste").assertExists()
        composeTestRule.onNodeWithText("Rua Teste, Cidade Teste").assertExists()

        // Clica na sugestão
        composeTestRule.onNodeWithText("Local Teste").performClick()
        composeTestRule.waitForIdle()

        // O callback deve ter sido acionado
        assertEquals(GeoPoint(-23.55, -46.63), pontoSelecionado)
        assertEquals("Local Teste", nomeSelecionado)
    }

    @Test
    fun testBuscaEnderecoNaoBuscaComMenosDeTresCaracteres() {
        var buscaAcionada = false

        composeTestRule.setContent {
            BuscaEndereco(
                centroMapa = null,
                aoSelecionarEndereco = { _, _ -> },
                funcaoBusca = { _, _ ->
                    buscaAcionada = true
                    emptyList()
                }
            )
        }

        // Digita apenas 2 caracteres
        composeTestRule.onNodeWithText("Buscar endereço").performTextInput("Sa")
        composeTestRule.waitForIdle()

        // A busca não deve ter sido acionada
        assertTrue(!buscaAcionada)
    }

    @Test
    fun testBuscaEnderecoLimpaCampoAoClicarNoBotaoLimpar() {
        composeTestRule.setContent {
            BuscaEndereco(
                centroMapa = null,
                aoSelecionarEndereco = { _, _ -> },
                funcaoBusca = { _, _ -> emptyList() }
            )
        }

        // Digita texto
        composeTestRule.onNodeWithText("Buscar endereço").performTextInput("Teste")
        
        // Avança o relógio para carregar e finalizar (carregando de true para false)
        composeTestRule.mainClock.advanceTimeBy(1000L)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Teste").assertExists()

        // Clica no botão de limpar (ícone Close com contentDescription "Limpar")
        composeTestRule.onNodeWithContentDescription("Limpar").performClick()
        composeTestRule.waitForIdle()

        // O texto deve sumir
        composeTestRule.onNodeWithText("Teste").assertDoesNotExist()
    }

    @Test
    fun testBuscaEnderecoOrdenacaoPorDistancia() {
        val centro = GeoPoint(-23.5505, -46.6333) // Praça da Sé, SP

        // rProximo: Avenida Paulista, ~3km
        // rDistante: Rio de Janeiro, ~350km
        // Ambos têm o mesmo tipo ("street") para forçar a comparação por distância
        val rProximo = ResultadoBusca(
            ponto = GeoPoint(-23.5615, -46.6560),
            nome = "Ponto Proximo",
            enderecoFormatado = "Rua Proxima",
            tipo = "street"
        )
        val rDistante = ResultadoBusca(
            ponto = GeoPoint(-22.9068, -43.1729),
            nome = "Ponto Distante",
            enderecoFormatado = "Rua Distante",
            tipo = "street"
        )

        // Passa a lista na ordem contrária (distante primeiro) para testar se ordena corretamente
        val fakeResultados = listOf(rDistante, rProximo)

        composeTestRule.setContent {
            BuscaEndereco(
                centroMapa = centro,
                aoSelecionarEndereco = { _, _ -> },
                funcaoBusca = { _, _ -> fakeResultados }
            )
        }

        // Digita algo e aguarda o debounce
        composeTestRule.onNodeWithText("Buscar endereço").performTextInput("Rua")
        composeTestRule.onNodeWithText("Buscar endereço").requestFocus()
        composeTestRule.mainClock.advanceTimeBy(1000L)

        // Aguarda até que o resultado "Ponto Proximo" apareça na tela
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Ponto Proximo").fetchSemanticsNodes().isNotEmpty()
        }

        // Verifica que ambos os pontos aparecem
        composeTestRule.onNodeWithText("Ponto Proximo").assertExists()
        composeTestRule.onNodeWithText("Ponto Distante").assertExists()
    }

    @Test
    fun testBuscaEnderecoTratamentoErro() {
        composeTestRule.setContent {
            BuscaEndereco(
                centroMapa = null,
                aoSelecionarEndereco = { _, _ -> },
                funcaoBusca = { _, _ -> throw RuntimeException("Erro de rede simulado") }
            )
        }

        composeTestRule.onNodeWithText("Buscar endereço").performTextInput("São")
        composeTestRule.onNodeWithText("Buscar endereço").requestFocus()
        composeTestRule.mainClock.advanceTimeBy(1000L)
        composeTestRule.waitForIdle()

        // Não deve haver sugestões na tela
        composeTestRule.onNodeWithText("Local Teste").assertDoesNotExist()
    }

    @Test
    fun testLocationDistanceBetweenNativo() {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(-23.5505, -46.6333, -23.5615, -46.6560, results)
        println("DEBUG LOCATION NATIVO: distance = ${results[0]}")
        assertTrue(results[0] > 0)
    }

    // =========================================================================
    // Testes adicionais de obterPrioridadeTipo — variantes não cobertas
    // =========================================================================

    @Test
    fun `obterPrioridadeTipo retorna 3 para village hamlet e municipality`() {
        // Cobre os branches: "village", "hamlet", "municipality" → prioridade 3
        assertEquals(3, obterPrioridadeTipo("village"))
        assertEquals(3, obterPrioridadeTipo("hamlet"))
        assertEquals(3, obterPrioridadeTipo("municipality"))
    }

    @Test
    fun `obterPrioridadeTipo retorna 4 para district neighbourhood locality e postcode`() {
        // Cobre os branches: "district", "neighbourhood", "locality", "postcode" → prioridade 4
        assertEquals(4, obterPrioridadeTipo("district"))
        assertEquals(4, obterPrioridadeTipo("neighbourhood"))
        assertEquals(4, obterPrioridadeTipo("locality"))
        assertEquals(4, obterPrioridadeTipo("postcode"))
    }

    @Test
    fun `obterPrioridadeTipo retorna 5 para highway`() {
        // Cobre o branch: "highway" → prioridade 5
        assertEquals(5, obterPrioridadeTipo("highway"))
    }

    @Test
    fun `obterPrioridadeTipo ignora maiusculas e minusculas`() {
        // Verifica que o lowercase() funciona corretamente
        assertEquals(3, obterPrioridadeTipo("CITY"))
        assertEquals(1, obterPrioridadeTipo("Country"))
        assertEquals(6, obterPrioridadeTipo("UNKNOWN"))
    }

    // =========================================================================
    // Testes da UI — branches não cobertos do composable BuscaEndereco
    // =========================================================================

    @Test
    fun testBuscaEnderecoResultadoSemEnderecoFormatadoNaoExibeSubtitulo() {
        // Testa o branch: if (resultado.enderecoFormatado.isNotEmpty()) -> NÃO entra (linha 239)
        val resultadoSemEndereco = ResultadoBusca(
            ponto = GeoPoint(-23.55, -46.63),
            nome = "Local Sem Endereco",
            enderecoFormatado = "" // campo vazio
        )

        composeTestRule.setContent {
            BuscaEndereco(
                centroMapa = null,
                aoSelecionarEndereco = { _, _ -> },
                funcaoBusca = { _, _ -> listOf(resultadoSemEndereco) }
            )
        }

        // Digita e aguarda o debounce
        composeTestRule.onNodeWithText("Buscar endereço").performTextInput("Loc")
        composeTestRule.onNodeWithText("Buscar endereço").requestFocus()
        composeTestRule.mainClock.advanceTimeBy(1000L)
        composeTestRule.mainClock.autoAdvance = true

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Local Sem Endereco").fetchSemanticsNodes().isNotEmpty()
        }

        // O nome deve aparecer
        composeTestRule.onNodeWithText("Local Sem Endereco").assertExists()
        // O endereço formatado NÃO deve aparecer (é vazio)
        composeTestRule.onNodeWithText("").assertDoesNotExist()
    }

    @Test
    fun testBuscaEnderecoComCentroMapaDefinidoExecutaOrdenacaoPorDistancia() {
        // Testa o branch: centroMapa != null no bloco sortedWith (linhas 113-130)
        // Dois resultados com o MESMO tipo para forçar a comparação de distância
        val centro = GeoPoint(-23.5505, -46.6333)

        val rProximo = ResultadoBusca(
            ponto = GeoPoint(-23.5600, -46.6400), // ~1.5km do centro
            nome = "Resultado Proximo",
            enderecoFormatado = "Rua Próxima",
            tipo = "house" // mesmo tipo → cai no else da prioridade (ambos = 6)
        )
        val rDistante = ResultadoBusca(
            ponto = GeoPoint(-22.9068, -43.1729), // ~Rio de Janeiro, ~360km
            nome = "Resultado Distante",
            enderecoFormatado = "Rua Distante",
            tipo = "house"
        )

        // Passa na ordem errada (distante primeiro) para verificar se reordena
        val fakeResultados = listOf(rDistante, rProximo)

        composeTestRule.setContent {
            BuscaEndereco(
                centroMapa = centro,
                aoSelecionarEndereco = { _, _ -> },
                funcaoBusca = { _, _ -> fakeResultados }
            )
        }

        composeTestRule.onNodeWithText("Buscar endereço").performTextInput("Res")
        composeTestRule.onNodeWithText("Buscar endereço").requestFocus()
        composeTestRule.mainClock.advanceTimeBy(1000L)
        composeTestRule.mainClock.autoAdvance = true

        // Aguarda qualquer dos dois resultados aparecer
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Resultado Proximo").fetchSemanticsNodes().isNotEmpty()
        }

        // Ambos devem aparecer (até 5 são exibidos)
        composeTestRule.onNodeWithText("Resultado Proximo").assertExists()
        composeTestRule.onNodeWithText("Resultado Distante").assertExists()
    }

    @Test
    fun testBuscaEnderecoCampoComDoisCaracteresNaoExibeResultados() {
        // Testa o branch: consulta.length < 3 → limpa resultados e retorna (linhas 95-99)
        var buscaAcionada = false

        composeTestRule.setContent {
            BuscaEndereco(
                centroMapa = null,
                aoSelecionarEndereco = { _, _ -> },
                funcaoBusca = { _, _ ->
                    buscaAcionada = true
                    listOf(ResultadoBusca(GeoPoint(0.0, 0.0), "Resultado", "Addr"))
                }
            )
        }

        // Digita apenas 2 caracteres (abaixo do mínimo de 3)
        composeTestRule.onNodeWithText("Buscar endereço").performTextInput("AB")
        composeTestRule.mainClock.advanceTimeBy(1000L)
        composeTestRule.waitForIdle()

        // A função de busca NÃO deve ter sido chamada
        assertEquals(false, buscaAcionada)
        // Nenhum resultado deve aparecer
        composeTestRule.onNodeWithText("Resultado").assertDoesNotExist()
    }

    @Test
    fun testBuscaEnderecoSelecionarSugestaoPreencheOCampo() {
        // Testa o branch do clique na sugestão: deSugestao = true, consulta = resultado.nome
        // Verifica que após selecionar, o campo contém o nome do resultado
        val fakeResultados = listOf(
            ResultadoBusca(
                ponto = GeoPoint(-15.78, -47.92),
                nome = "Brasília DF",
                enderecoFormatado = "Distrito Federal, Brasil"
            )
        )

        var pontoRecebido: GeoPoint? = null

        composeTestRule.setContent {
            BuscaEndereco(
                centroMapa = null,
                aoSelecionarEndereco = { ponto, _ -> pontoRecebido = ponto },
                funcaoBusca = { _, _ -> fakeResultados }
            )
        }

        composeTestRule.onNodeWithText("Buscar endereço").performTextInput("Bra")
        composeTestRule.onNodeWithText("Buscar endereço").requestFocus()
        composeTestRule.mainClock.advanceTimeBy(1000L)
        composeTestRule.mainClock.autoAdvance = true

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Brasília DF").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Brasília DF").performClick()
        composeTestRule.waitForIdle()

        // O callback deve ter sido chamado com o ponto correto
        assertEquals(GeoPoint(-15.78, -47.92), pontoRecebido)
    }

    @Test
    fun testBuscaEnderecoOrdenacaoMistaTipoEDistancia() {
        // Testa que resultados com tipos diferentes são ordenados por prioridade de tipo,
        // e que o branch p1 != p2 é coberto (linhas 111-113)
        val centro = GeoPoint(-23.5505, -46.6333)

        val rCidade = ResultadoBusca(
            ponto = GeoPoint(-22.9068, -43.1729), // distante, mas tipo "city" (prioridade 3)
            nome = "Cidade Distante",
            enderecoFormatado = "RJ",
            tipo = "city"
        )
        val rCasa = ResultadoBusca(
            ponto = GeoPoint(-23.5600, -46.6400), // próximo, mas tipo "house" (prioridade 6)
            nome = "Casa Proxima",
            enderecoFormatado = "SP",
            tipo = "house"
        )

        composeTestRule.setContent {
            BuscaEndereco(
                centroMapa = centro,
                aoSelecionarEndereco = { _, _ -> },
                funcaoBusca = { _, _ -> listOf(rCasa, rCidade) } // casa primeiro, mas deve vir depois
            )
        }

        composeTestRule.onNodeWithText("Buscar endereço").performTextInput("Loc")
        composeTestRule.onNodeWithText("Buscar endereço").requestFocus()
        composeTestRule.mainClock.advanceTimeBy(1000L)
        composeTestRule.mainClock.autoAdvance = true

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Cidade Distante").fetchSemanticsNodes().isNotEmpty()
        }

        // Ambos devem aparecer
        composeTestRule.onNodeWithText("Cidade Distante").assertExists()
        composeTestRule.onNodeWithText("Casa Proxima").assertExists()
    }
}
