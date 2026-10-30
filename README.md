# LocalExperts+ — Java Spring Boot Backend

Smart City Business Discovery Platform

## Tech Stack
- **Language:** Java 17
- **Framework:** Spring Boot 3.2
- **Database:** MongoDB
- **Security:** Spring Security + JWT
- **Build Tool:** Maven

## Project Structure
```
src/main/java/com/localexperts/
├── LocalExpertsApplication.java     ← Main entry point
├── model/
│   ├── User.java                    ← User model
│   ├── Business.java                ← Business model
│   └── City.java                    ← City model
├── repository/
│   ├── UserRepository.java
│   ├── BusinessRepository.java
│   └── CityRepository.java
├── controller/
│   ├── AuthController.java          ← /api/auth/*
│   ├── BusinessController.java      ← /api/businesses/*
│   ├── CityController.java          ← /api/cities/*
│   ├── AnalyticsController.java     ← /api/analytics/*
│   ├── ReviewController.java        ← /api/reviews/*
│   └── HealthController.java        ← /api/health
└── security/
    ├── JwtUtil.java                 ← JWT generate/validate
    ├── JwtAuthFilter.java           ← JWT middleware
    └── SecurityConfig.java          ← Spring Security config
```

## Prerequisites
- Java 17 or higher → https://adoptium.net
- MongoDB running on localhost:27017
- Maven 3.8+ (or use included mvnw wrapper)

## How to Run

### Step 1: Install Java 17
Download from: https://adoptium.net/temurin/releases/

### Step 2: Start MongoDB
```bash
mongod
```

### Step 3: Build the project
```bash
cd localexperts-java
mvn clean install
```

### Step 4: Run the server
```bash
mvn spring-boot:run
```

Server starts at: http://localhost:5000

## API Endpoints

### Auth
| Method | URL | Description |
|--------|-----|-------------|
| POST | /api/auth/signup | Register new user |
| POST | /api/auth/login | Login |
| GET | /api/auth/me | Get current user |
| PUT | /api/auth/update-profile | Update profile |

### Businesses
| Method | URL | Description |
|--------|-----|-------------|
| GET | /api/businesses | Get all businesses |
| GET | /api/businesses/search?q=hotel | Search businesses |
| GET | /api/businesses/recommend | Get recommendations |
| GET | /api/businesses/:id | Get one business |
| POST | /api/businesses | Add business (auth) |
| PUT | /api/businesses/:id | Update business |
| DELETE | /api/businesses/:id | Delete (admin) |
| POST | /api/businesses/:id/reviews | Add review |
| PUT | /api/businesses/:id/approve | Approve (admin) |

### Other
| Method | URL | Description |
|--------|-----|-------------|
| GET | /api/cities | Get all cities |
| GET | /api/analytics/dashboard | Dashboard stats |
| GET | /api/analytics/city-stats | City-wise stats |
| GET | /api/reviews/recent | Recent reviews |
| GET | /api/health | Health check |

## Demo Login Credentials
| Role | Email | Password |
|------|-------|----------|
| Admin | admin@localexperts.com | Admin@1234 |
| User | user@localexperts.com | User@1234 |

## Recommendation Algorithm
Score = (Rating × 0.6) + (Popularity × 0.4)
- Rating weight: 60%
- Popularity (review count) weight: 40%
