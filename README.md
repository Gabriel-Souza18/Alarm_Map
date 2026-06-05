# AlarmMap

O AlarmMap é um aplicativo Android desenvolvido em Kotlin que permite disparar alarmes baseados na localização geográfica do dispositivo, em vez de horários fixos. O aplicativo monitora a posição em segundo plano e notifica o usuário ao entrar no raio definido.

---

## Funcionalidades Principais

*   **Seleção Interativa no Mapa**: Definição da localização do alarme tocando no mapa interativo (OSMDroid).
*   **Busca por Endereço**: Pesquisa de locais digitando um endereço na barra de pesquisa (API Nominatim do OpenStreetMap).
*   **Ajuste de Raio Customizado**: Configuração da área de cobertura do alarme de 50 a 2000 metros através de um Slider.
*   **Modo de Alerta Personalizável**:
    *   **Tocar e Vibrar**: Emite o som de alarme padrão do celular em repetição com vibrações.
    *   **Apenas Vibrar**: Vibra de forma contínua no modo silencioso.
*   **Edição de Alarmes**: Edição do nome, raio, modo de alerta ou posição de alarmes criados previamente.
*   **Nome Padrão**: Se o nome do alarme for deixado em branco, o sistema define automaticamente o nome "Alarme".
*   **Ativação Rápida**: Ativação ou desativação de alarmes na lista principal via switch, além de opção para exclusão.
*   **Alerta em Tela Bloqueada**: Abertura de tela cheia dedicada com animação para desligar o alerta, funcionando mesmo com o celular bloqueado ou o aplicativo fechado.

---

## Tecnologias Utilizadas

*   **Linguagem**: Kotlin nativo
*   **Interface**: Jetpack Compose (Material Design 3)
*   **Banco de Dados**: SQLite nativo (com suporte a migrações de versão)
*   **Mapa**: OSMDroid (OpenStreetMap)
*   **Localização**: Google Play Services (FusedLocationProviderClient)
*   **Serviços**: Android Foreground Service (Serviço em Primeiro Plano com notificação persistente)

---

## Estrutura do Código

```
app/src/main/java/com/example/alarm_map/
├── ActivityAlarmeDisparado.kt   # Activity em tela cheia para desligar o alarme
├── MainActivity.kt              # Gerenciador de navegação e permissões
├── banco/
│   └── BancoAlarmes.kt          # SQLiteOpenHelper e migrações
├── modelo/
│   └── Alarme.kt                # Data class da entidade Alarme
├── repositorio/
│   └── RepositorioAlarme.kt     # Operações de banco de dados
├── servico/
│   └── ServicoLocalizacao.kt    # Foreground Service para monitoramento
└── ui/
    ├── componentes/
    │   └── CardAlarme.kt        # Item visual da lista de alarmes
    └── telas/
        ├── TelaListaAlarmes.kt  # Lista dos alarmes cadastrados
        └── TelaMapa.kt          # Tela do mapa para criação e edição
```

---

## Como Rodar o Projeto

1.  **Requisitos**:
    *   Android Studio instalado.
    *   Dispositivo Android físico ou emulador com serviços do Google Play.
2.  **Permissões**:
    *   O aplicativo solicita permissões de localização (aproximada e precisa) ao iniciar.
    *   Em versões mais recentes do Android, pode ser necessário permitir o acesso à localização em tempo integral nas configurações do sistema.
3.  **Compilação**:
    Execute na raiz do projeto:
    ```bash
    ./gradlew assembleDebug
    ```

