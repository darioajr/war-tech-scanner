# WAR Tech Scanner

[![CI/CD](https://github.com/darioajr/war-tech-scanner/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/darioajr/war-tech-scanner/actions/workflows/ci-cd.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.darioajr/war-tech-scanner.svg)](https://central.sonatype.com/artifact/io.github.darioajr/war-tech-scanner)
[![Coverage](https://codecov.io/gh/darioajr/war-tech-scanner/branch/main/graph/badge.svg)](https://codecov.io/gh/darioajr/war-tech-scanner)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=darioajr_war-tech-scanner&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=darioajr_war-tech-scanner)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-21%2B-orange.svg)](https://adoptium.net)

CLI em Java para detectar tecnologias em arquivos `.war`, `.ear`, `.jar` e `.rar`, com foco em inventário para migração JBoss EAP / Jakarta EE.

## Tecnologias detectadas

| Tecnologia | Fontes de evidência |
|---|---|
| EJB | `@Stateless`, `@Stateful`, `@MessageDriven`, `ejb-jar.xml`, `jboss-ejb3.xml` |
| JPA | `@Entity`, `@PersistenceContext`, `persistence.xml` |
| Hibernate | `SessionFactory`, `hibernate.cfg.xml`, `*.hbm.xml`, `hibernate-core-*.jar` |
| CDI | `@Inject`, `@ApplicationScoped`, `beans.xml` |
| JSF | `@ManagedBean`, `faces-config.xml`, `*.xhtml` |
| JAX-RS | `@Path`, `@GET`, `@POST`, `resteasy-*.jar`, `jersey-*.jar` |
| JAX-WS/SOAP | `@WebService`, `@WebMethod`, `cxf-*.jar`, `axis-*.jar` |
| Servlet | `HttpServlet`, `web.xml`, `servlet-api-*.jar` |
| Spring | `@Component`, `@Service`, `applicationcontext.xml`, `spring-*.jar` |
| Struts | `struts.xml`, `struts-*.jar` |

A detecção usa três camadas:

1. **Bytecode** — leitura de `.class` com ASM para encontrar anotações e tipos `javax.*`, `jakarta.*`, `org.hibernate.*`, etc.
2. **Descritores XML** — `persistence.xml`, `ejb-jar.xml`, `hibernate.cfg.xml`, `*.hbm.xml`, `beans.xml`, `faces-config.xml`, `web.xml`, etc.
3. **Bibliotecas** — nomes de JARs dentro de `WEB-INF/lib` e arquivos aninhados.

## Pré-requisitos

- Java 21+
- Maven 3.9+ (apenas para build)

## Build

```bash
mvn -DskipTests package
```

O artefato gerado é `target/war-tech-scanner-0.1.0-SNAPSHOT.jar` (fat JAR com todas as dependências).

## Uso

```
java -jar target/war-tech-scanner-0.1.0-SNAPSHOT.jar <artifact> [opções]
```

### Parâmetros

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `<artifact>` | posicional | Arquivo `.war`, `.ear`, `.jar` ou `.rar` a analisar |
| `--json` | flag | Imprime resultado em JSON (desativa UI rica) |
| `--no-nested` | flag | Não analisa arquivos aninhados (JARs dentro de WARs, etc.) |
| `--max-evidence=N` | inteiro | Máximo de evidências listadas por tecnologia (padrão: `5`) |
| `--target-eap=X.Y` | string | Versão alvo do JBoss EAP (ex: `7.4`, `8.0`, `8.1`) |
| `--target-java=N` | inteiro | Versão alvo do Java (ex: `11`, `17`, `21`) |
| `--mta-config=PATH` | caminho | Arquivo de configuração do MTA. **Obrigatório** para gerar sugestões de comando `mta-cli` |
| `-h`, `--help` | flag | Exibe ajuda |
| `-V`, `--version` | flag | Exibe versão |

### Exemplos

**Scan básico com UI rica:**
```bash
java -jar target/war-tech-scanner-0.1.0-SNAPSHOT.jar minha-app.war
```

**Saída JSON:**
```bash
java -jar target/war-tech-scanner-0.1.0-SNAPSHOT.jar minha-app.war --json > report.json
```

**Análise de migração para EAP 8.1 + Java 21:**
```bash
java -jar target/war-tech-scanner-0.1.0-SNAPSHOT.jar minha-app.ear \
  --target-eap=8.1 \
  --target-java=21
```

**Gerar sugestão de comando MTA:**
```bash
java -jar target/war-tech-scanner-0.1.0-SNAPSHOT.jar minha-app.ear \
  --target-eap=8.1 \
  --target-java=21 \
  --mta-config war-tech-scanner-config.json \
  --json > report.json
```

**Sem analisar JARs aninhados:**
```bash
java -jar target/war-tech-scanner-0.1.0-SNAPSHOT.jar minha-app.war --no-nested
```

**Inventário em massa:**
```bash
find /apps -type f \( -name "*.war" -o -name "*.ear" \) \
  -exec java -jar target/war-tech-scanner-0.1.0-SNAPSHOT.jar {} \
    --target-eap=8.1 --target-java=21 --json \; \
  > inventory.jsonl
```

## Sugestão de comando MTA

Quando `--mta-config` é informado, o scanner executa cada instalação MTA configurada para descobrir os `sources`, `targets` e `providers` disponíveis, cruza com as tecnologias detectadas e gera um comando pronto por instalação.

### Arquivo de configuração (`war-tech-scanner-config.json`)

```json
{
  "mtaInstallations": [
    {
      "label": "MTA 7.2 - Local",
      "type": "BARE_METAL",
      "path": "/opt/mta-7.2/bin/mta-cli"
    },
    {
      "label": "MTA 7.2 - Docker",
      "type": "CONTAINER",
      "image": "registry.redhat.io/mta/mta-cli-rhel9:7.2",
      "containerEngine": "docker"
    },
    {
      "label": "MTA 7.2 - Podman",
      "type": "CONTAINER",
      "image": "registry.redhat.io/mta/mta-cli-rhel9:7.2",
      "containerEngine": "podman"
    },
    {
      "label": "MTA 6.2 - Docker",
      "type": "CONTAINER",
      "image": "registry.redhat.io/mta/mta-cli-rhel8:6.2",
      "containerEngine": "docker"
    },
    {
      "label": "MTA 6.2 - Podman",
      "type": "CONTAINER",
      "image": "registry.redhat.io/mta/mta-cli-rhel8:6.2",
      "containerEngine": "podman"
    },
    {
      "label": "MTA 7.2 - OpenShift",
      "type": "OPENSHIFT",
      "namespace": "mta",
      "hubRoute": "https://mta-mta.apps.cluster.example.com",
      "operatorChannel": "stable-v7",
      "operatorCatalog": "redhat-operators"
    }
  ]
}
```

### Tipos de instalação

#### `BARE_METAL`

Executa o binário `mta-cli` instalado localmente.

| Campo | Obrigatório | Descrição |
|---|---|---|
| `path` | sim | Caminho absoluto para o binário `mta-cli` |

Comando gerado:
```bash
/opt/mta-7.2/bin/mta-cli analyze \
  --input minha-app.ear \
  --output ./mta-report \
  --target eap81,java21 \
  --source ejb,jpa,hibernate
```

#### `CONTAINER`

Executa via Docker ou Podman usando imagens oficiais da Red Hat em `registry.redhat.io`.

| Campo | Obrigatório | Padrão | Descrição |
|---|---|---|---|
| `image` | sim | — | Imagem do MTA CLI. Use `registry.redhat.io/mta/mta-cli-rhel9:<versão>` (MTA 7.x) ou `registry.redhat.io/mta/mta-cli-rhel8:<versão>` (MTA 6.x) |
| `containerEngine` | não | `docker` | Engine de container: `docker` ou `podman` |

> **Atenção:** `registry.redhat.io` exige autenticação com conta Red Hat (<https://access.redhat.com>).

Comando gerado:
```bash
docker login registry.redhat.io
docker run --rm \
  -v minha-app.ear:/app/input/minha-app.ear:ro,z \
  -v $(pwd)/mta-report:/app/output:z \
  registry.redhat.io/mta/mta-cli-rhel9:7.2 analyze \
  --input /app/input/minha-app.ear \
  --output /app/output \
  --target eap81,java21 \
  --source ejb,jpa
```

#### `OPENSHIFT`

Instala o operador MTA via OLM a partir do catálogo `redhat-operators` e cria a análise via MTA Hub API.

| Campo | Obrigatório | Padrão | Descrição |
|---|---|---|---|
| `namespace` | não | `mta` | Namespace onde o operador será instalado |
| `hubRoute` | sim | — | URL base do MTA Hub exposta pelo operador |
| `operatorChannel` | não | `stable-v7` | Canal OLM: `stable-v7` (MTA 7.x) ou `stable-v6` (MTA 6.x) |
| `operatorCatalog` | não | `redhat-operators` | CatalogSource do OLM |

Comando gerado (dois passos):
```bash
# Passo 1 — instalar o operador MTA via OLM
oc apply -f - <<'EOF'
apiVersion: v1
kind: Namespace
metadata:
  name: mta
---
apiVersion: operators.coreos.com/v1
kind: OperatorGroup
metadata:
  name: mta-operatorgroup
  namespace: mta
spec:
  targetNamespaces: [mta]
---
apiVersion: operators.coreos.com/v1alpha1
kind: Subscription
metadata:
  name: mta
  namespace: mta
spec:
  channel: stable-v7
  installPlanApproval: Automatic
  name: mta
  source: redhat-operators
  sourceNamespace: openshift-marketplace
EOF

# Passo 2 — criar análise via MTA Hub API (aguardar o operador estar Running)
MTA_HUB=https://mta-mta.apps.cluster.example.com
curl -s -X POST "$MTA_HUB/hub/applications" \
  -H "Content-Type: application/json" \
  -d '{"name":"minha-app","bucket":{"name":"minha-app"}}' | tee /tmp/mta-app.json

APP_ID=$(jq -r '.id' /tmp/mta-app.json)
curl -s -X POST "$MTA_HUB/hub/analyses" \
  -H "Content-Type: application/json" \
  -d '{
    "application":{"id":'$APP_ID'},
    "sources":["ejb","jpa"],
    "targets":["eap81","java21"]
  }'
```

### Descoberta automática de sources/targets

Quando o binário (BARE_METAL) ou a imagem (CONTAINER) está disponível localmente, o scanner executa:

```
mta-cli analyze --list-sources
mta-cli analyze --list-targets
mta-cli analyze --list-providers
```

Os tokens retornados são cruzados com as tecnologias detectadas para produzir um comando com apenas os `--source` e `--target` realmente suportados pela versão instalada. Se o binário/imagem não estiver disponível, o comando é gerado com base em mapeamentos estáticos e uma nota de aviso é incluída.

### Estrutura do JSON de saída (`--json`)

```json
{
  "artifact": "/path/to/app.ear",
  "artifactType": "EAR",
  "scannedAt": "2026-06-03T15:00:00Z",
  "technologies": [
    { "name": "EJB", "score": 42, "evidences": ["..."] }
  ],
  "descriptors": ["META-INF/persistence.xml"],
  "libraries": ["hibernate-core-5.6.jar"],
  "classesWithEvidence": ["com/example/MyBean.class"],
  "warnings": [],
  "migrationHints": [
    "[EAP 8.1] EJB: substituir javax.ejb.* por jakarta.ejb.*"
  ],
  "mtaSuggestions": [
    {
      "mtaLabel": "MTA 7.2 - Docker",
      "mtaPath": "registry.redhat.io/mta/mta-cli-rhel9:7.2",
      "installationType": "CONTAINER",
      "command": "docker login registry.redhat.io\ndocker run ...",
      "resolvedSources": ["ejb", "jpa"],
      "resolvedTargets": ["eap81", "java21"],
      "note": null
    }
  ]
}
```

## Publicação no Maven Central

O `pom.xml` já inclui metadados exigidos, `sources`, `javadocs`, assinatura GPG e `central-publishing-maven-plugin`.

Configure `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>${env.CENTRAL_USERNAME}</username>
      <password>${env.CENTRAL_PASSWORD}</password>
    </server>
  </servers>
</settings>
```

Depois:

```bash
mvn clean verify
mvn deploy -DskipTests
```

## Licenças

Este projeto é distribuído sob a **Apache License 2.0**. Veja [LICENSE](LICENSE).

As dependências de terceiros e suas licenças estão listadas em [THIRD-PARTY-NOTICES.txt](THIRD-PARTY-NOTICES.txt).
