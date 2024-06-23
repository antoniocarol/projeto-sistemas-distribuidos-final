> Universidade Católica do Salvador
> 
> Professor: Everton Mendonça
> 
> Equipe: Antonio Carolino e Jean Alves Silva

# Sistema de Arquivos Distribuídos (DFS)

## Visão Geral

Este projeto implementa um Sistema de Arquivos Distribuídos (DFS) que permite o armazenamento e recuperação de arquivos de texto (.txt) contendo perfis de usuários. O sistema é composto por três aplicações principais:

- **dfs-app-a (Mestre):** Recebe requisições de leitura e escrita, gerencia o armazenamento dos arquivos nos nós (dfs-app-b e dfs-app-c) e notifica as alterações via WebSocket.
- **dfs-app-b e dfs-app-c (Nós):** Armazenam os arquivos e respondem às requisições do mestre.

## Funcionalidades

- **Armazenamento distribuído:** Os arquivos são armazenados de forma distribuída nos nós dfs-app-b e dfs-app-c.
- **Balanceamento de carga:** O mestre escolhe aleatoriamente um nó para armazenar cada arquivo, distribuindo a carga entre os nós.
- **Recuperação de arquivos:** O mestre localiza o arquivo no nó correto e retorna o conteúdo para o cliente.
- **Versionamento de arquivos:** Os arquivos são salvos com um sufixo de versão (ex: perfis_v1.txt, perfis_v2.txt), permitindo o controle de versões.
- **Notificação em tempo real:** As alterações nos arquivos são notificadas em tempo real aos clientes conectados via WebSocket.

## Componentes

- **CustomWebSocketHandler:** Gerencia as conexões WebSocket e envia notificações aos clientes.
- **RestTemplate:** Realiza as requisições HTTP entre as aplicações.
- **WebSocketConfig:** Configura o suporte a WebSocket.
- **DfsController:** Controla as operações de leitura, escrita e notificação no mestre (dfs-app-a).
- **ProfileController:** Controla as operações de leitura e escrita na aplicação de perfis (profile-app).
- **DnsController:** Resolve o endereço do serviço de DNS.
- **IspController:** Controla as operações de leitura e escrita na aplicação do provedor de internet (isp-app).
- **RestTemplateConfig:** Configura o RestTemplate.

## Fluxo de Trabalho

1. **Salvar arquivo:**
   - O cliente envia uma requisição para salvar um arquivo no mestre (dfs-app-a).
   - O mestre escolhe aleatoriamente um nó (dfs-app-b ou dfs-app-c) para armazenar o arquivo.
   - O mestre envia o arquivo para o nó escolhido e notifica os clientes via WebSocket sobre o sucesso da operação.

2. **Obter arquivo:**
   - O cliente envia uma requisição para obter um arquivo do mestre (dfs-app-a).
   - O mestre verifica em qual nó o arquivo está armazenado e retorna o conteúdo para o cliente.

## Pré-requisitos

- Java 17
- Maven 4.0.0
- Spring Boot 3.3.0
- Docker (opcional)

## Como executar:

1. Clone o repositório.
2. Inicie os serviços Eureka Server, dns-app, isp-app e profile-app (se ainda não estiverem em execução).
3. Inicie as aplicações dfs-app-a, dfs-app-b e dfs-app-c utilizando o Maven:

   ```bash
   mvn spring-boot:run
   ```

4. Acesse a interface da aplicação profile-app para interagir com o sistema de arquivos distribuídos.

## Exemplo de Uso

```
GET http://localhost:8082/dfs/obterArquivo/perfis_v1.txt

POST http://localhost:8082/dfs/salvarArquivo/perfis_v2.txt
```

## Observações

- Este é um exemplo simplificado de um sistema de arquivos distribuídos. Em um ambiente de produção, seria necessário implementar mecanismos de replicação, tolerância a falhas e segurança.
- O projeto utiliza o Eureka Server para descoberta de serviços. Em um ambiente de produção, você pode considerar o uso de um orquestrador de contêineres aprendido em sala, como o Kubernetes.

## Licença

Nenhuma
