# Implementação do Algoritmo Bully - Sistemas Distribuídos

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)

Este repositório contém a implementação do **Algoritmo Bully (Bully Algorithm)** para eleição de líder em sistemas distribuídos. O projeto foi desenvolvido como requisito da disciplina de Sistemas Distribuídos no curso de Ciência da Computação da **Universidade Federal do Piauí (UFPI)**.

## 📋 Visão Geral

O sistema simula um ambiente distribuído composto por múltiplos nós (microsserviços Spring Boot). Quando o coordenador atual falha, os nós remanescentes iniciam um processo de eleição para determinar um novo líder baseando-se no ID do processo (o maior ID vence).

O projeto inclui uma interface visual (`dashboard.html`) para monitorar o estado dos nós e visualizar a troca de mensagens em tempo real.

### 📷 Fluxo do Processo de Eleição

O diagrama abaixo ilustra o comportamento implementado nos nós durante o processo de eleição:

![Diagrama do Processo de Eleição](fluxo-eleicao.png)
*(Certifique-se de adicionar a imagem gerada no diretório raiz com este nome)*

## 🚀 Tecnologias Utilizadas

* **Java 17+**
* **Spring Boot 3.x** (Web, Actuator)
* **Maven** (Gerenciamento de dependências)
* **JavaScript/HTML/CSS** (Dashboard de visualização)
* **HTTP/REST** (Comunicação entre os nós)

## ⚙️ Arquitetura

O sistema é composto por:

1.  **Nós (Backend):** Aplicações Java independentes que expõem endpoints REST para comunicação (`/election`, `/coordinator`, `/ping`).
2.  **Dashboard (Frontend):** Arquivo estático (`dashboard.html`) que consome o status dos nós e permite simular falhas ou iniciar eleições manualmente.

## 📦 Como Executar

### Pré-requisitos
* JDK 17 ou superior instalado.
* Maven instalado.

### 1. Clonar o repositório
```bash
git clone [https://github.com/seu-usuario/nome-do-repo.git](https://github.com/seu-usuario/nome-do-repo.git)
cd nome-do-repo
