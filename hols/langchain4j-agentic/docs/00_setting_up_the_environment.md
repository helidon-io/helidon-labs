# 1. Setting Up the Environment

Before you start editing code, make sure your machine can build and run the lab projects.

In this section, we will:

- Verify Java and Maven.
- Set required environment variables.
- Confirm folder locations used in this HOL.

---

## 1. Verify Java

Use Java 21+ (Java 25 reccomended).

```sh
java --version
```

This lab uses the maven wrapper (`mvnw`) so that you do not need to install maven on your system if you do not have it already.

## 2. Set OCI API key

This project uses the OpenAI-compatible OCI Generative AI endpoint configured in `application.yaml`.
Set the `OCI_API_KEY` environment variable before running the app:

```sh
export OCI_API_KEY="<your-oci-api-key>"
```

---

### Next Step -> [Preparing Embeddings with Embedding Ingestor](01_preparing_embeddings_with_embedding_ingestor.md)
