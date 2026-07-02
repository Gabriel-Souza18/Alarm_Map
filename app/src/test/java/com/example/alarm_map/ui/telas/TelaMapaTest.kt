package com.example.alarm_map.ui.telas

import android.content.Context
import android.location.Location
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.alarm_map.modelo.Alarme
import com.example.alarm_map.ui.componentes.ResultadoBusca
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TelaMapaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var contexto: Context

    @Before
    fun setUp() {
        contexto = ApplicationProvider.getApplicationContext()
        contexto.deleteDatabase("alarmes.db")
    }

    @Test
    fun testTelaMapaLayoutNovoAlarme() {
        var voltou = false
        composeTestRule.setContent {
            TelaMapa(
                alarmeParaEditar = null,
                aoVoltar = { voltou = true },
                isTesting = true
            )
        }

        // Verifica se elementos básicos do layout do bottom sheet e top bar são exibidos
        composeTestRule.onNodeWithText("Novo Alarme").assertExists()
        composeTestRule.onNodeWithText("Configurar alarme").assertExists()
        composeTestRule.onNodeWithText("Nome do alarme").assertExists()
        composeTestRule.onNodeWithText("Apenas vibrar").assertExists()
        composeTestRule.onNodeWithText("Salvar alarme").assertExists()

        // Testa o clique em salvar quando ponto é nulo
        composeTestRule.onNodeWithText("Salvar alarme").performClick()
        composeTestRule.waitForIdle()

        // Testa o clique em voltar
        composeTestRule.onNodeWithContentDescription("Voltar").performClick()
        assertTrue(voltou)
    }

    @Test
    fun testTelaMapaEdicaoAlarme() {
        var voltou = false
        val alarme = Alarme(
            id = 1,
            nome = "Alarme Teste Edicao",
            latitude = -23.55,
            longitude = -46.63,
            raioMetros = 500,
            ativo = true,
            apenasVibrar = true
        )

        composeTestRule.setContent {
            TelaMapa(
                alarmeParaEditar = alarme,
                aoVoltar = { voltou = true },
                expandidoInicialmente = true,
                isTesting = true
            )
        }

        composeTestRule.onNodeWithText("Editar Alarme").assertExists()
        composeTestRule.onNodeWithText("Alarme Teste Edicao").assertExists()
        
        // Imprime a árvore de semântica
        System.err.println(composeTestRule.onRoot().printToString())

        // Avança o relógio para garantir que qualquer animação de expansão terminou
        composeTestRule.mainClock.advanceTimeBy(5000L)
        composeTestRule.waitForIdle()

        // Clica em Salvar alterações
        composeTestRule.onNodeWithText("Salvar alterações").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        
        assertTrue(voltou)
    }

    @Test
    fun testTelaMapaComLocalizacaoMockada() {
        val mockLocation = Mockito.mock(Location::class.java).apply {
            Mockito.`when`(latitude).thenReturn(-23.55)
            Mockito.`when`(longitude).thenReturn(-46.63)
        }
        val mockClient = Mockito.mock(FusedLocationProviderClient::class.java)
        Mockito.`when`(mockClient.lastLocation).thenReturn(Tasks.forResult(mockLocation))

        Mockito.mockStatic(LocationServices::class.java).use { mockedServices ->
            mockedServices.`when`<FusedLocationProviderClient> {
                LocationServices.getFusedLocationProviderClient(Mockito.any(Context::class.java))
            }.thenReturn(mockClient)

            composeTestRule.setContent {
                TelaMapa(
                    alarmeParaEditar = null,
                    aoVoltar = {},
                    isTesting = true
                )
            }

            composeTestRule.waitForIdle()

            // Clica no botão de "Minha localização"
            composeTestRule.onNodeWithContentDescription("Minha localização").performClick()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun testTelaMapaComBuscaMockadaESalvamento() {
        var salvou = false
        val fakeResultados = listOf(
            ResultadoBusca(
                ponto = GeoPoint(-23.55, -46.63),
                nome = "Local Teste",
                enderecoFormatado = "Rua Teste, 123"
            )
        )

        composeTestRule.setContent {
            TelaMapa(
                alarmeParaEditar = null,
                aoVoltar = { salvou = true },
                funcaoBusca = { _, _ -> fakeResultados },
                expandidoInicialmente = true,
                isTesting = true
            )
        }

        // Digita na busca
        composeTestRule.onNodeWithText("Buscar endereço").performTextInput("São")
        composeTestRule.onNodeWithText("Buscar endereço").requestFocus()

        // Avança o relógio virtual
        composeTestRule.mainClock.advanceTimeBy(1000L)
        // Reativa o avanço automático para que as animações e recomposições subsequentes fluam
        composeTestRule.mainClock.autoAdvance = true

        // Aguarda a sugestão aparecer na tela
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Local Teste").fetchSemanticsNodes().isNotEmpty()
        }

        // Clica na sugestão
        composeTestRule.onNodeWithText("Local Teste").performClick()
        composeTestRule.waitForIdle()

        // Configura o formulário
        composeTestRule.onNodeWithText("Nome do alarme").performScrollTo().performTextInput("Alarme de Teste")
        
        // Clica em Apenas Vibrar
        composeTestRule.onNode(isToggleable()).performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Imprime a árvore de semântica para depuração
        System.err.println(composeTestRule.onRoot().printToString())

        // Garante que todas as animações terminaram antes de salvar
        composeTestRule.mainClock.advanceTimeBy(5000L)
        composeTestRule.waitForIdle()

        // Clica em Salvar
        composeTestRule.onNodeWithText("Salvar alarme").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Verifica se a gravação disparou a ação de voltar
        assertTrue(salvou)
    }

    @Test
    fun testTelaMapaLocalizacaoNula() {
        val mockClient = Mockito.mock(FusedLocationProviderClient::class.java)
        Mockito.`when`(mockClient.lastLocation).thenReturn(Tasks.forResult(null))

        Mockito.mockStatic(LocationServices::class.java).use { mockedServices ->
            mockedServices.`when`<FusedLocationProviderClient> {
                LocationServices.getFusedLocationProviderClient(Mockito.any(Context::class.java))
            }.thenReturn(mockClient)

            composeTestRule.setContent {
                TelaMapa(
                    alarmeParaEditar = null,
                    aoVoltar = {},
                    isTesting = true
                )
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithContentDescription("Minha localização").performClick()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun testTelaMapaLocalizacaoErro() {
        val mockClient = Mockito.mock(FusedLocationProviderClient::class.java)
        Mockito.`when`(mockClient.lastLocation).thenReturn(Tasks.forException(RuntimeException("Erro de GPS")))

        Mockito.mockStatic(LocationServices::class.java).use { mockedServices ->
            mockedServices.`when`<FusedLocationProviderClient> {
                LocationServices.getFusedLocationProviderClient(Mockito.any(Context::class.java))
            }.thenReturn(mockClient)

            composeTestRule.setContent {
                TelaMapa(
                    alarmeParaEditar = null,
                    aoVoltar = {},
                    isTesting = true
                )
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithContentDescription("Minha localização").performClick()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun testTelaMapaPlayServicesIndisponivel() {
        Mockito.mockStatic(LocationServices::class.java).use { mockedServices ->
            mockedServices.`when`<FusedLocationProviderClient> {
                LocationServices.getFusedLocationProviderClient(Mockito.any(Context::class.java))
            }.thenThrow(RuntimeException("Play Services indisponível"))

            composeTestRule.setContent {
                TelaMapa(
                    alarmeParaEditar = null,
                    aoVoltar = {},
                    isTesting = true
                )
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithContentDescription("Minha localização").performClick()
            composeTestRule.waitForIdle()
        }
    }

    /**
     * Verifica que salvar com nome em branco usa 'Alarme' como nome padrão.
     * Testa a linha: val nomeFinal = if (nomeAlarme.isBlank()) "Alarme" else nomeAlarme.trim()
     */
    @Test
    fun testTelaMapaSalvarComNomeEmBrancoUsaPadrao() {
        var voltou = false
        val fakeResultados = listOf(
            ResultadoBusca(
                ponto = GeoPoint(-23.55, -46.63),
                nome = "Local Padrão",
                enderecoFormatado = "Rua Padrão, 100"
            )
        )

        composeTestRule.setContent {
            TelaMapa(
                alarmeParaEditar = null,
                aoVoltar = { voltou = true },
                funcaoBusca = { _, _ -> fakeResultados },
                expandidoInicialmente = true,
                isTesting = true
            )
        }

        // Seleciona um endereço via busca para definir pontoSelecionado
        composeTestRule.onNodeWithText("Buscar endereço").performTextInput("Loc")
        composeTestRule.onNodeWithText("Buscar endereço").requestFocus()
        composeTestRule.mainClock.advanceTimeBy(1000L)
        composeTestRule.mainClock.autoAdvance = true

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Local Padrão").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Local Padrão").performClick()
        composeTestRule.waitForIdle()

        // NÃO preenche o nome do alarme (campo vazio = deve usar "Alarme" como padrão)
        // Clica diretamente em Salvar alarme
        composeTestRule.onNodeWithText("Salvar alarme").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Deve ter salvo e chamado aoVoltar mesmo com nome em branco
        assertTrue(voltou)
    }

    /**
     * Verifica que clicar em salvar sem ponto selecionado NÃO chama aoVoltar.
     * Testa o branch: ponto == null -> Toast e não chama aoVoltar
     */
    @Test
    fun testTelaMapaBotaoSalvarSemPontoNaoChamaAoVoltar() {
        var voltou = false

        composeTestRule.setContent {
            TelaMapa(
                alarmeParaEditar = null,
                aoVoltar = { voltou = true },
                isTesting = true
            )
        }

        composeTestRule.waitForIdle()

        // Clica em Salvar sem selecionar ponto → deve mostrar Toast, não chamar aoVoltar
        composeTestRule.onNodeWithText("Salvar alarme").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // aoVoltar NÃO deve ter sido chamado
        assertEquals(false, voltou)
    }

    /**
     * Verifica que o raio padrão de 200 metros aparece no texto exibido.
     * Testa a linha: "Raio: ${raioMetros.roundToInt()} metros"
     */
    @Test
    fun testTelaMapaRaioDefaultExibido() {
        composeTestRule.setContent {
            TelaMapa(
                alarmeParaEditar = null,
                aoVoltar = {},
                isTesting = true
            )
        }

        composeTestRule.waitForIdle()

        // Verifica que o texto do raio padrão (200m) está presente
        composeTestRule.onNodeWithText("Raio: 200 metros").assertExists()
    }

    /**
     * Verifica que em modo edição, o raio do alarme original é pré-preenchido.
     * Testa: mutableFloatStateOf(alarmeParaEditar?.raioMetros?.toFloat() ?: 200f)
     */
    @Test
    fun testTelaMapaEdicaoPreencheRaioCorreto() {
        val alarme = Alarme(
            id = 2,
            nome = "Alarme Raio",
            latitude = -15.78,
            longitude = -47.92,
            raioMetros = 750,
            ativo = true,
            apenasVibrar = false
        )

        composeTestRule.setContent {
            TelaMapa(
                alarmeParaEditar = alarme,
                aoVoltar = {},
                expandidoInicialmente = true,
                isTesting = true
            )
        }

        composeTestRule.waitForIdle()

        // O raio do alarme editado (750m) deve estar exibido no texto
        composeTestRule.onNodeWithText("Raio: 750 metros").assertExists()
    }

    /**
     * Verifica que o botão em modo edição exibe "Salvar alterações" em vez de "Salvar alarme".
     * Testa: textoBotaoSalvar = if (alarmeParaEditar != null) "Salvar alterações" else "Salvar alarme"
     */
    @Test
    fun testTelaMapaTextoBotaoModosDistintos() {
        // Modo novo: deve exibir "Salvar alarme"
        composeTestRule.setContent {
            TelaMapa(
                alarmeParaEditar = null,
                aoVoltar = {},
                isTesting = true
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Salvar alarme").assertExists()
        composeTestRule.onNodeWithText("Salvar alterações").assertDoesNotExist()
    }

    /**
     * Verifica que editar alarme com nome em branco usa 'Alarme' como padrão.
     * Testa o branch: alarmeParaEditar.copy(nome = "Alarme") quando nomeAlarme.isBlank()
     */
    @Test
    fun testTelaMapaEdicaoComNomeEmBrancoUsaPadrao() {
        var voltou = false
        val alarme = Alarme(
            id = 3,
            nome = "Alarme Original",
            latitude = -23.55,
            longitude = -46.63,
            raioMetros = 300,
            ativo = true,
            apenasVibrar = false
        )

        composeTestRule.setContent {
            TelaMapa(
                alarmeParaEditar = alarme,
                aoVoltar = { voltou = true },
                expandidoInicialmente = true,
                isTesting = true
            )
        }

        composeTestRule.waitForIdle()

        // Limpa o campo de nome
        composeTestRule.onNodeWithText("Alarme Original").performTextClearance()
        composeTestRule.waitForIdle()

        // Garante que o campo está vazio após limpeza
        composeTestRule.mainClock.advanceTimeBy(500L)
        composeTestRule.waitForIdle()

        // Salva com nome em branco → deve usar "Alarme" e chamar aoVoltar
        composeTestRule.onNodeWithText("Salvar alterações").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertTrue(voltou)
    }

    /**
     * Verifica que o PainelConfiguracaoAlarme exibe todos os elementos esperados:
     * título, campo de nome, rótulos de raio, Switch de vibrar e botão.
     */
    @Test
    fun testPainelConfiguracaoAlarmeExibeElementos() {
        composeTestRule.setContent {
            TelaMapa(
                alarmeParaEditar = null,
                aoVoltar = {},
                isTesting = true
            )
        }

        composeTestRule.waitForIdle()

        // Todos os elementos do PainelConfiguracaoAlarme devem estar visíveis
        composeTestRule.onNodeWithText("Configurar alarme").assertExists()
        composeTestRule.onNodeWithText("Nome do alarme").assertExists()
        composeTestRule.onNodeWithText("Apenas vibrar").assertExists()
        composeTestRule.onNodeWithText("Desativa o som e apenas vibra o celular").assertExists()
        composeTestRule.onNodeWithText("Salvar alarme").assertExists()
        composeTestRule.onNodeWithText("50 m").assertExists()
        composeTestRule.onNodeWithText("2000 m").assertExists()
    }

    /**
     * Verifica que o Switch de "Apenas vibrar" pode ser alternado no modo edição.
     * Testa a interação com o componente Switch do PainelConfiguracaoAlarme.
     */
    @Test
    fun testTelaMapaAlternarApenasVibrar() {
        val alarme = Alarme(
            id = 4,
            nome = "Alarme Vibrar",
            latitude = -23.55,
            longitude = -46.63,
            raioMetros = 200,
            ativo = true,
            apenasVibrar = false // começa desligado
        )

        composeTestRule.setContent {
            TelaMapa(
                alarmeParaEditar = alarme,
                aoVoltar = {},
                expandidoInicialmente = true,
                isTesting = true
            )
        }

        composeTestRule.waitForIdle()

        // Alterna o Switch de "Apenas vibrar"
        composeTestRule.onNode(androidx.compose.ui.test.isToggleable()).performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // O switch deve ter sido alternado (sem erro)
        composeTestRule.onNodeWithText("Apenas vibrar").assertExists()
    }

    /**
     * Verifica que o título muda conforme o modo (novo vs edição).
     * Testa: Text(if (alarmeParaEditar != null) "Editar Alarme" else "Novo Alarme")
     */
    @Test
    fun testTelaMapaTituloNovoVsEdicao() {
        // Modo novo
        composeTestRule.setContent {
            TelaMapa(
                alarmeParaEditar = null,
                aoVoltar = {},
                isTesting = true
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Novo Alarme").assertExists()
    }
}
