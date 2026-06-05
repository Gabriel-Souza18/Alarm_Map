# Guia Prático: Criar Release do Alarm_Map

## Passo 1: Gerar Keystore (primeira vez)

Execute este comando e anote a senha:

```bash
keytool -genkeypair -v -keystore alarmmap-release.jks -alias alarmmap \
  -keyalg RSA -keysize 2048 -validity 10000
```

Responda às perguntas:
- **Tamanho de chave do RSA**: 2048 ✓ (padrão)
- **Alias**: alarmmap ✓ (padrão, deixe em branco para aceitar)
- **Nome**: seu nome ou "AlarmMap"
- **Unidade organizacional**: "Dev" ou deixe em branco
- **Organização**: seu nome/empresa
- **Cidade**: sua cidade
- **Estado**: seu estado/região
- **País**: seu código do país (ex: BR, US)
- **Confirme**: s
- **Senha do keystore**: **ANOTE ISSO** (usar em keystore.properties)
- **Senha da chave**: pode ser a mesma

**Resultado**: arquivo `alarmmap-release.jks` criado no diretório atual

---

## Passo 2: Configurar Credenciais Locais

Edite o arquivo `/home/gabriel/Documentos/Alarm_Map/keystore.properties`:

```properties
storeFile=/caminho/completo/para/alarmmap-release.jks
storePassword=SUA_SENHA_STORE
keyAlias=alarmmap
keyPassword=SUA_SENHA_KEY
```

**Exemplo real**:
```properties
storeFile=/home/gabriel/keystore/alarmmap-release.jks
storePassword=minhasenha123
keyAlias=alarmmap
keyPassword=minhasenha123
```

**Importante**: Este arquivo nunca será commitado (está em `.gitignore`)

---

## Passo 3: Compilar APK Release

```bash
cd /home/gabriel/Documentos/Alarm_Map
./gradlew assembleRelease
```

**Saída esperada**:
```
...
BUILD SUCCESSFUL in XXs
```

APK gerado em:
```
app/build/outputs/apk/release/app-release.apk
```

---

## Passo 4: Verificar Assinatura (opcional)

```bash
jarsigner -verify -verbose app/build/outputs/apk/release/app-release.apk
```

Deve conter:
```
jar signed.
```

---

## Passo 5: Upload no GitHub

1. Acesse: https://github.com/seu-usuario/Alarm_Map/releases/new
2. **Tag version**: v1.0.0 (ou próxima versão)
3. **Release title**: Alarme 1.0 Release
4. **Description**: Escreva um resumo das mudanças
5. **Attach binaries**: Arraste `app-release.apk` aqui
6. **Publish release**

---

## Troubleshooting

### ❌ "BUILD FAILED - keystore.properties not found"
Verifique se o arquivo `keystore.properties` existe na raiz do projeto com as credenciais corretas.

### ❌ "BUILD FAILED - Keystore was tampered with or password was incorrect"
Verifique as senhas em `keystore.properties` (storePassword e keyPassword).

### ❌ "BUILD FAILED - keytool: error: java.io.FileNotFoundException"
Verifique se o caminho do `storeFile` em `keystore.properties` está correto e o arquivo `.jks` existe.

---

## Segurança

✅ **Boas práticas**:
- Keystore nunca é versionado (`.gitignore`)
- Senhas nunca aparecem em logs ou terminal permanentemente
- Use um local seguro para guardar o `alarmmap-release.jks` (ex: pasta criptografada)

❌ **Evitar**:
- Não commit do arquivo `.jks` ou `keystore.properties`
- Não compartilhe senhas em Slack/Discord
- Não use a mesma senha para múltiplos projetos

---

## Próximos Passos

Se quiser automatizar com GitHub Actions (CI/CD):
- Use GitHub Secrets para armazenar as senhas
- Configure um workflow `.github/workflows/release.yml` para gerar APK automaticamente


