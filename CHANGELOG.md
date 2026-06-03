# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/),
e este projeto adota [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [Unreleased]

### Added
- Detecção de tecnologias Java EE / Jakarta EE via bytecode (ASM), descritores XML e bibliotecas
- Suporte a arquivos `.war`, `.ear`, `.jar` e `.rar`, incluindo arquivos aninhados
- UI rica no terminal com spinner, barra de progresso e gráfico de barras (ANSI + Unicode)
- Saída em JSON com `--json`
- Dicas de migração para JBoss EAP 7.x / 8.x e Java 11 / 17 / 21 via `--target-eap` e `--target-java`
- Geração de comandos MTA por instalação via `--mta-config`:
  - Tipo `BARE_METAL`: executa o binário local com targets/sources resolvidos automaticamente
  - Tipo `CONTAINER`: gera comando Docker/Podman com imagens oficiais `registry.redhat.io`
  - Tipo `OPENSHIFT`: gera `oc apply` com Subscription OLM + chamadas à MTA Hub API
- Discovery automática de sources e targets via `--list-sources` / `--list-targets` do MTA CLI
- Fallback para mapeamentos estáticos quando o binário/imagem não está disponível
- `THIRD-PARTY-NOTICES.txt` gerado automaticamente pelo `license-maven-plugin`

[Unreleased]: https://github.com/darioajr/war-tech-scanner/compare/HEAD...HEAD
