# 🐋 NarwhalApplication  

**NarwhalApplication** não é um aplicativo tradicional. Trata-se de um projeto de **teste e aprendizado** para explorar conceitos de **Koog** e desenvolvimento de **agentes de IA usando Kotlin DSL**.  

O objetivo principal foi **aprender a criar agentes capazes de receber inputs complexos e utilizar tools para realizar operações específicas**, além de experimentar diferentes arquiteturas de agentes e fluxos de decisão.  

> 🔎 Disponível apenas para **uso acadêmico e estudo de conceitos de IA e Koog**.  

---

## 🏗️ Estrutura do Projeto

O projeto é composto por **três módulos principais**, cada um focado em um tipo de agente de IA:  

### 1️⃣ Agente de Operações Matemáticas
- Permite que o agente receba **operações matemáticas** a serem realizadas  
- Cada operação é executada usando **tools específicas**  
- Objetivo: testar a integração entre agentes e tools, e o fluxo de execução baseado em comandos  

### 2️⃣ Agente de Análise de Imagens
- Recebe **imagens do diretório atual**  
- Analisa e gera a **melhor descrição de post para Instagram**  
- Objetivo: experimentar análise de imagens com agentes e geração de conteúdo automatizado  

### 3️⃣ Agente de Banking
- Sistema mais complexo, envolvendo **transações e análise de gastos**  
- Funcionalidades:
  - Realizar transações entre usuários  
  - Analisar gastos por categoria  
- Duas abordagens implementadas:
  1. **Estratégia personalizada**: um ciclo principal decide se a requisição é de **transação ou análise**, e subgrafos especializados cuidam de cada funcionalidade  
  2. **Agentes como tools**: cada subgrafo é transformado em um agente separado, e um **agente principal** coordena as operações. Facilita o fluxo e manutenção, mas **reduz garantias e testabilidade dos resultados**  

---

## 🎯 Objetivo do projeto
O projeto foi criado com finalidade **pessoal**, com foco em aprendizado e experimentação:  

- Aprender a utilizar **Koog** e **Kotlin DSL** para criar agentes inteligentes  
- Testar **arquiteturas de agentes** e estratégias de decisão  
- Integrar agentes com **tools especializadas** para executar tarefas  
- Explorar **análise de dados, operações matemáticas e processamento de imagens**  
- Entender **prós e contras** de diferentes abordagens de composição de agentes (subgrafos vs. agentes como tools)  

---

## 🛠️ Tecnologias e Conceitos
- **Kotlin DSL** para definição e execução de agentes  
- **Koog** como framework de agentes de IA  
- **Tools** especializadas para operações matemáticas, análise de imagens e transações bancárias  
- **Arquitetura modular** com subgrafos e agentes principais  
- **Testes de conceito** para fluxo de decisão, execução de tools e análise de resultados  

---

## 📌 Status
- Projeto **experimental e em aprendizado**, não destinado a produção  
- Todas as funcionalidades são **conceituais**, focadas em estudar fluxos de decisão, agentes e integração com tools  

---
