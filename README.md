# Secure Full-Stack School Management System

An event-driven, secure enterprise application built to handle student workflows, dynamic administrative grids, and real-time operational integration.

## 🛠️ Technical Ecosystem
- [cite_start]**Frontend:** React, Tailwind CSS, Bootstrap [cite: 62]
- [cite_start]**Backend Core:** Java, Spring Boot, Spring Security [cite: 63, 79]
- [cite_start]**Data Layer & Distributed Caching:** PostgreSQL, Redis [cite: 64, 79]
- [cite_start]**Message Streaming & Event-Driven Integration:** Apache Kafka Cluster, Debezium (CDC), Apache Camel [cite: 65, 79]
- [cite_start]**Identity & Access Management:** Keycloak IAM (OAuth2 / OIDC) [cite: 65, 79]
- [cite_start]**Container Deployment:** Docker [cite: 65, 79]

## 🚀 Architectural Deep-Dive
- **Change Data Capture (CDC):** Configured a **Debezium** connector monitoring the core PostgreSQL database, streaming operational transactions natively as immutable message frames into dedicated event topics.
- [cite_start]**Distributed Streaming:** Architected an asynchronous messaging layer via an **Apache Kafka Cluster** and **Apache Camel** to achieve fully decoupled, fault-tolerant communication pipelines. [cite: 82]
- [cite_start]**Identity Security:** Integrated **Keycloak** to enforce granular Role-Based Access Control (RBAC), securing application endpoints and managing profile visibility rules between student and administrative account tokens. [cite: 80]
- **Memory Optimization:** Implemented **Redis** to store ephemeral lookup states and rapid grid telemetry, minimizing direct database read stress and optimizing client data grid rendering speeds.
