# ChatBot Service

A Spring Boot application that provides an intelligent chatbot service with persistent chat history and context-aware responses using Google's Gemini AI.

## Features

- **AI-Powered Chat**: Integration with Google Gemini AI for intelligent responses
- **Persistent Chat History**: Automatic saving and retrieval of conversation history
- **Context-Aware Responses**: Uses previous conversations to provide more relevant answers
- **User Management**: User registration and management system
- **RESTful API**: Clean REST endpoints for all operations
- **Database Integration**: MySQL database with Aiven cloud hosting
- **Connection Pooling**: HikariCP for efficient database connections

## Architecture

### Tech Stack
- **Framework**: Spring Boot 3.5.5
- **Java Version**: 21
- **Database**: MySQL 8.0.35 (Aiven Cloud)
- **AI Service**: Google Gemini API
- **ORM**: JPA/Hibernate 6.6.26
- **Connection Pool**: HikariCP
- **Build Tool**: Maven

### Project Structure
```
src/
├── main/
│   ├── java/com/ChatBot/chatbot_service/
│   │   ├── controller/
│   │   │   ├── ChatController.java          # REST endpoints for chat operations
│   │   │   └── UserController.java          # User management endpoints
│   │   ├── service/
│   │   │   ├── ChatService.java             # Core chat logic with history management
│   │   │   └── UserService.java             # User management operations
│   │   ├── entity/
│   │   │   ├── User.java                    # User entity with relationships
│   │   │   └── ChatHistory.java             # Chat history entity
│   │   ├── repository/
│   │   │   ├── UserRepository.java          # User data access
│   │   │   └── ChatHistoryRepository.java   # Chat history data access
│   │   ├── dto/
│   │   │   ├── ChatRequest.java             # Chat request DTO
│   │   │   └── ChatResponse.java            # Chat response DTO
│   │   └── ChatBotServiceApplication.java   # Main application class
│   └── resources/
│       ├── application.yml                  # Application configuration
│       └── static/                          # Static resources
└── test/                                    # Test classes
```

## Key Features Implementation

### 1. Chat History Management
- **Automatic Saving**: Every conversation is automatically saved to the database
- **Context Building**: Retrieves last 10 conversations for context
- **Response Time Tracking**: Monitors and stores response times
- **User-Specific History**: Each user has their own conversation history

### 2. Context-Aware AI Responses
- **Historical Context**: Previous conversations are included in AI prompts
- **Improved Relevance**: AI responses consider conversation history
- **Seamless Integration**: Context is transparently added to Gemini API calls

### 3. Database Schema
```sql
-- Users table
Users (id, username, email, created_at)

-- Chat History table  
ChatHistory (id, user_id, user_message, bot_response, created_at, response_time)
```

## API Endpoints

### Chat Operations
```http
POST /api/chat
Content-Type: application/json

{
    "username": "john_doe",
    "message": "Hello, how are you?",
    "model": "gemini-1.5-flash"
}

Response:
{
    "response": "Hello! I'm doing well, thank you for asking...",
    "status": "success"
}
```

### User Management
```http
POST /api/users
Content-Type: application/json

{
    "username": "john_doe",
    "email": "john@example.com"
}
```

### Health Check
```http
GET /api/health-check

Response: "Hello, ChatBot Service is running!"
```

## Configuration

### Environment Variables (.env)
```env
GEMINI_API_KEY=your_gemini_api_key_here
DB_URL=your_database_url
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
```

### Application Configuration (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

gemini:
  api:
    key: ${GEMINI_API_KEY}
```

## How Chat History Works

1. **User Sends Message**: Client sends chat request with username and message
2. **User Validation**: System validates user exists in database
3. **Context Retrieval**: Fetches last 10 conversations for the user
4. **Context Formatting**: Formats conversation history as context string
5. **AI Request**: Sends message + context to Gemini API
6. **Response Processing**: Receives and processes AI response
7. **History Saving**: Saves both user message and bot response to database
8. **Response Return**: Returns AI response to client

### Context Format Example
```
Previous conversation context:
User: Hello, how are you?
Assistant: Hello! I'm doing well, thank you for asking...
User: What's the weather like?
Assistant: I don't have access to real-time weather data...

User: [Current message]
```

## Running the Application

### Prerequisites
- Java 21
- Maven 3.6+
- MySQL database (local or cloud)
- Google Gemini API key

### Setup Steps
1. Clone the repository
2. Configure environment variables in `.env` file
3. Update `application.yml` with your database settings
4. Run the application:
```bash
mvn spring-boot:run
```

### Build
```bash
mvn clean compile
mvn clean package
```

## Development Notes

### Database Relationships
- **User ↔ ChatHistory**: One-to-Many relationship
- **Foreign Key**: `user_id` in ChatHistory table
- **JPA Annotations**: `@OneToMany` and `@ManyToOne` with proper cascading

### Service Architecture
- **ChatService**: Handles all chat operations, history management, and AI integration
- **UserService**: Manages user creation and retrieval
- **Clear Separation**: Chat logic isolated from user management

### Error Handling
- User not found exceptions
- Database connection errors
- AI API failures
- Validation errors for required fields

## Recent Updates

- ✅ Implemented persistent chat history functionality
- ✅ Added context-aware AI responses using conversation history
- ✅ Integrated chat history saving with every conversation
- ✅ Enhanced ChatService with history management methods
- ✅ Proper service architecture with clear separation of concerns
- ✅ Database relationships between User and ChatHistory entities
- ✅ Automatic context building from last 10 conversations

## License

This project is part of a chatbot service implementation for educational and development purposes.
