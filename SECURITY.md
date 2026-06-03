# Política de Segurança

## Versões suportadas

| Versão | Suportada |
|--------|-----------|
| 0.1.x (latest) | ✅ |

## Reportando uma vulnerabilidade

**Não abra uma issue pública para vulnerabilidades de segurança.**

Por favor, reporte de forma responsável pelo e-mail:

📧 **darioajr@gmail.com**

Inclua na mensagem:
- Descrição da vulnerabilidade
- Passos para reproduzir
- Impacto potencial
- Sugestão de correção (opcional)

Você receberá uma resposta em até **72 horas**. Após confirmação e correção, a vulnerabilidade será divulgada publicamente junto com os créditos ao relator (a menos que prefira anonimato).

## Escopo

Este projeto é uma ferramenta CLI de análise estática. As superfícies de ataque relevantes são:

- **Leitura de arquivos ZIP/JAR/WAR/EAR maliciosos** — o scanner abre e itera entradas de arquivos compactados
- **Execução de processos externos** (`mta-cli`) — via `ProcessBuilder` com argumentos controlados pelo usuário
- **Desserialização JSON** (Jackson) — configuração do MTA via `--mta-config`

## Dependências

As dependências de terceiros e suas licenças estão listadas em [THIRD-PARTY-NOTICES.txt](THIRD-PARTY-NOTICES.txt).
Atualizações de segurança nas dependências são aplicadas nas releases regulares.
