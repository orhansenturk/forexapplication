# Foreign Exchange Application

A Spring Boot application for currency conversion and exchange rate tracking.

## Features

- Get current exchange rates
- Convert between currencies
- Track conversion history
- Bulk currency conversion via CSV files
- External exchange rate API integration
- RESTful API with Swagger documentation

## Prerequisites

- Docker
- Docker Compose

## Running with Docker

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd forex
   ```

2. Configure your API key:
    - Open `docker-compose.yml`
    - Replace `your_api_key_here` with your actual Exchange Rate API key

3. Build and run the application:
   ```bash
   docker-compose up --build
   ```

   The application will be available at http://localhost:8080

## API Documentation

Once the application is running, you can access the Swagger UI documentation at:
http://localhost:8080/swagger-ui.html

## Environment Variables

The following environment variables can be configured:

- `SERVER_PORT`: The port on which the application runs (default: 8080)
- `EXCHANGE_RATE_API_URL`: The URL of the exchange rate API
- `EXCHANGE_RATE_API_KEY`: Your API key for the exchange rate service

## Building Without Docker

If you prefer to run without Docker, you'll need:
- Java 21
- Maven 3.9+

Then run:
```bash
mvn clean package
java -jar target/*.jar
```

## API Endpoints

- `GET /api/exchange-rate`: Get current exchange rate
- `POST /api/convert`: Convert currency
- `GET /api/conversions`: Get conversion history
- `POST /api/bulk-convert`: Bulk convert currencies from CSV

## Security Note

Make sure to keep your API key secure and never commit it to version control. 
In production, use secure methods to provide the API key. 


## Other Notes

Conversions are handled in ConcurrentHashMap to make it more lightweight. 
Dependencies have H2 in memory database and jpa data but they did not used in this case. 
They can be used in later implementations to enhance the application.
Redis can be used for caching purposes in the future changes.