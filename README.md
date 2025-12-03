# Crypto Recommendations Service

A Spring Boot application that provides cryptocurrency recommendations and statistics based on historical price data. The service reads price data from CSV files and exposes a REST API for querying normalized ranges, statistics, and more.

## Features

- Reads crypto price data from CSV files in the `prices/` directory.
- Calculates oldest, newest, minimum, and maximum prices for each crypto.
- Returns cryptos sorted by normalized range.
- Provides stats for a specific crypto, for a specific period, or for the last month.
- Returns the crypto with the highest normalized range for a given date or time.
- Supports dynamic addition of new cryptos (just add a new CSV file).
- Rate limiting (per IP) via NGINX Ingress or Bucket4j.
- OpenAPI/Swagger documentation for easy API exploration.
- Unit and integration tests included.

## Prerequisites

- Java 21
- Gradle
- Docker (for containerization)
- Kubernetes (for deployment, optional)

## Running Locally

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/crypto-recommendations-service.git
   cd crypto-recommendations-service
2. **Build the project:**
   ```bash
   ./gradlew clean build
3. **Run the application**
    ```bash
   ./gradlew bootRun

## Swagger can be accessed 
    http://localhost:8080/swagger-ui.html

## Running with Docker
1. ```bash
   docker build -t crypto-recommendations:latest .
   docker run -p 8080:8080 crypto-recommendations:latest

Access the app at: http://localhost:8080
## Deploying to Kubernetes
```bash
    kubectl apply -f deployment.yaml
    kubectl apply -f ingress.yaml