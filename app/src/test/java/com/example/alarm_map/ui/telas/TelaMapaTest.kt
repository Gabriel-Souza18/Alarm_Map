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
}
