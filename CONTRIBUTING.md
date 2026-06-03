# Contribuindo com o WAR Tech Scanner

Obrigado pelo interesse em contribuir! Este documento descreve como participar do projeto.

Por favor, note que este projeto segue um [Código de Conduta](CODE_OF_CONDUCT.md). Ao participar, você concorda em respeitar seus termos.

## Como contribuir

### Reportando bugs

Antes de abrir uma issue, verifique se o problema já não foi reportado na [lista de issues](https://github.com/darioajr/war-tech-scanner/issues).

Ao reportar um bug, inclua:

- Versão do Java e sistema operacional
- Comando exato executado
- Arquivo analisado (tipo: WAR, EAR, JAR) e tecnologias esperadas
- Saída completa (incluindo erros)
- Comportamento esperado vs. comportamento observado

### Sugerindo melhorias

Abra uma [issue](https://github.com/darioajr/war-tech-scanner/issues) descrevendo a melhoria antes de enviar um pull request, para que possamos discutir a proposta antes do desenvolvimento.

Isso evita retrabalho e garante que a contribuição esteja alinhada com a direção do projeto.

### Enviando Pull Requests

1. Faça um fork do repositório
2. Crie um branch a partir de `main`:
   ```bash
   git checkout -b feat/minha-contribuicao
   ```
3. Implemente as alterações seguindo as convenções do projeto
4. Adicione ou atualize testes quando aplicável
5. Garanta que o build e os testes passam:
   ```bash
   mvn verify
   ```
6. Faça commit com mensagem clara e assine com DCO (veja abaixo):
   ```bash
   git commit -s -m "feat: descrição da mudança"
   ```
7. Abra o pull request descrevendo o que foi feito e por quê

## Convenções de código

- Java 21 — use `var`, records, switch expressions e outras features modernas quando apropriado
- Sem dependências externas novas sem discussão prévia — o projeto mantém footprint mínimo
- Testes JUnit 5 para toda lógica de detecção e geração de comandos
- Mensagens de commit em inglês, no formato `<tipo>: <descrição>` (ex: `fix:`, `feat:`, `docs:`, `refactor:`)

## Áreas de contribuição

- **Novas tecnologias detectadas** — adicionar suporte a outros frameworks em `TechnologyCatalog.java`
- **Mapeamentos MTA** — melhorar `TECH_TO_CANDIDATE_SOURCES` e `TECH_TO_TARGETS` em `MtaCommandBuilder.java`
- **Suporte a novos tipos de instalação MTA** — ex: Podman Compose, Helm
- **Testes** — cobertura de WARs/EARs com diferentes combinações de tecnologias
- **Documentação** — exemplos, tutoriais, casos de uso reais

## Legal — Developer Certificate of Origin (DCO)

Este projeto usa o [Developer Certificate of Origin 1.1](https://developercertificate.org/) para gerenciar contribuições de código, o mesmo modelo adotado pelo kernel Linux.

Ao enviar uma contribuição, você certifica que:

```
Developer Certificate of Origin
Version 1.1

By making a contribution to this project, I certify that:

(a) The contribution was created in whole or in part by me and I have
    the right to submit it under the open source license indicated in
    the file; or

(b) The contribution is based upon previous work that, to the best of
    my knowledge, is covered under an appropriate open source license
    and I have the right under that license to submit that work with
    modifications, whether created in whole or in part by me, under
    the same open source license, as indicated in the file; or

(c) The contribution was provided directly to me by some other person
    who certified (a), (b) or (c) and I have not modified it.

(d) I understand and agree that this project and the contribution are
    public and that a record of the contribution is maintained
    indefinitely and may be redistributed consistent with this project
    or the open source license(s) involved.
```

Para assinar, adicione ao final da mensagem de commit:

```
Signed-off-by: Seu Nome <seu@email.com>
```

Usando seu nome real. O Git faz isso automaticamente com:

```bash
git commit -s
```

## Cabeçalho de licença

Todo arquivo-fonte Java deve incluir o cabeçalho da Apache License 2.0:

```java
/*
 * Copyright 2024-present Dario Alves Junior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```

## Dúvidas

Abra uma [issue](https://github.com/darioajr/war-tech-scanner/issues) com a tag `question` ou entre em contato pelo e-mail darioajr@gmail.com.
