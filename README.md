# ChatBot Service

A Spring Boot application that provides an intelligent chatbot service with persistent chat history, context-aware responses, and advanced conversation summarization using Google's Gemini AI.

## 🚀 Features

- **🤖 AI-Powered Chat**: Integration with Google Gemini AI for intelligent responses
- **💾 Persistent Chat History**: Automatic saving and retrieval of conversation history
- **🧠 Context-Aware Responses**: Uses previous conversations to provide more relevant answers
- **📊 Smart Conversation Summarization**: AI-powered summarization of older conversations
- **👥 User Management**: User registration and management system
- **🔗 RESTful API**: Clean REST endpoints for all operations
- **🗄️ Database Integration**: MySQL database with Aiven cloud hosting
- **⚡ Connection Pooling**: HikariCP for efficient database connections
- **🔄 Intelligent Context Management**: Optimized context building with recent detail + summarized history

## 🏗️ Architecture

### Tech Stack
- **Framework**: Spring Boot 3.5.5
- **Java Version**: 21
- **Database**: MySQL 8.0.35 (Aiven Cloud)
- **AI Service**: Google Gemini API (gemini-1.5-flash)
- **ORM**: JPA/Hibernate 6.6.26
- **Connection Pool**: HikariCP
- **Build Tool**: Maven
- **Environment Management**: spring-dotenv for .env file support

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

## 🔧 Key Features Implementation

### 1. 💬 Advanced Chat History Management
- **Automatic Saving**: Every conversation is automatically saved to the database
- **Smart Context Building**: Intelligent retrieval of conversation history for context
- **Response Time Tracking**: Monitors and stores response times for performance analysis
- **User-Specific History**: Each user has their own isolated conversation history
- **Conversation Summarization**: AI-powered summarization of older conversations

### 2. 🎯 Context-Aware AI Responses
- **Historical Context**: Previous conversations are intelligently included in AI prompts
- **Improved Relevance**: AI responses consider conversation history for better continuity
- **Seamless Integration**: Context is transparently added to Gemini API calls
- **Optimized Context Length**: Smart summarization reduces token usage while preserving quality

### 3. 🧠 Intelligent Conversation Summarization
- **Automatic Triggering**: Starts summarizing after 15+ conversations per user
- **Recent Detail Preservation**: Last 5 conversations kept in full detail
- **AI-Generated Summaries**: Uses Gemini API to create meaningful conversation summaries
- **Batch Processing**: Groups conversations efficiently for summarization
- **Fallback Mechanisms**: Keyword extraction backup if AI summarization fails
- **Performance Optimization**: Reduces context length by ~60% while maintaining quality

### 4. 🗄️ Enhanced Database Schema
```sql
-- Users table
Users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(255) UNIQUE NOT NULL,
  email VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)

-- Enhanced ChatHistory table with summarization support
ChatHistory (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  user_message VARCHAR(1000) NOT NULL,
  bot_response TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  response_time BIGINT,
  is_summarized BOOLEAN DEFAULT FALSE,    -- NEW: Tracks summarization status
  summary TEXT,                           -- NEW: Stores AI-generated summaries
  FOREIGN KEY (user_id) REFERENCES Users(id)
)
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

## 🔄 How Enhanced Chat History Works

### Conversation Flow with Summarization
1. **User Sends Message**: Client sends chat request with username and message
2. **User Validation**: System validates user exists in database
3. **Summarization Check**: If user has 15+ conversations, trigger intelligent summarization
4. **Context Retrieval**: 
   - Fetch last 5 conversations in full detail
   - Retrieve AI-generated summaries of older conversations
5. **Context Formatting**: Combine summarized context + recent detailed conversations
6. **AI Request**: Send optimized context + new message to Gemini API
7. **Response Processing**: Receive and process AI response
8. **History Saving**: Save user message, bot response, and metadata to database
9. **Response Return**: Return AI response to client

### Intelligent Context Format
```
Previous conversation summary:
The user previously discussed [AI-generated summary of older conversations]...

Recent conversation history:
User: What's the weather like today?
Assistant: I don't have access to real-time weather data...
User: Can you help me with coding?
Assistant: Absolutely! I'd be happy to help you with coding...

User: [Current message]
```

### Summarization Process
```
Trigger (15+ conversations) → 
├── Identify conversations to summarize (older than recent 5)
├── Group into batches of 8 conversations
├── Generate AI summaries using Gemini API
├── Store summaries in database
├── Mark conversations as summarized
└── Build context: [Summaries] + [Recent 5 detailed]
```

## 🚀 Running the Application

### Prerequisites
- ☕ Java 21
- 📦 Maven 3.6+
- 🗄️ MySQL database (local or cloud)
- 🔑 Google Gemini API key
- 🌐 Internet connection for Aiven database

### Setup Steps
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd chatbot-service
   ```

2. **Configure environment variables**
   Create a `.env` file in the root directory:
   ```env
   GEMINI_API_KEY=your_gemini_api_key_here
   DB_URL=your_aiven_database_url
   DB_USERNAME=your_db_username
   DB_PASSWORD=your_db_password
   ```

3. **Update application configuration**
   Verify `application.yml` settings match your environment

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

### Build Commands
```bash
# Clean and compile
mvn clean compile

# Create executable JAR
mvn clean package

# Run tests
mvn test

# Clean, test, and package
mvn clean install
```

### Docker Support (Optional)
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/chatbot-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🛠️ Development Notes

### Database Architecture
- **User ↔ ChatHistory**: One-to-Many relationship with proper cascading
- **Foreign Key Constraints**: `user_id` in ChatHistory table ensures data integrity
- **JPA Annotations**: `@OneToMany` and `@ManyToOne` with lazy loading for performance
- **Indexing**: Optimized indexes on `user_id` and `created_at` for fast querying

### Service Layer Architecture
- **ChatService**: 
  - Core chat operations and AI integration
  - Conversation summarization logic
  - Context building and management
  - Response time tracking
- **UserService**: 
  - User creation and management
  - User validation and retrieval
  - Isolated from chat logic for clean separation
- **Repository Layer**: 
  - Custom JPA queries for efficient data access
  - Optimized methods for conversation retrieval
  - Batch operations for summarization

### Error Handling & Resilience
- **User Validation**: Comprehensive user existence checks
- **Database Errors**: Graceful handling of connection and query failures
- **AI API Failures**: Fallback mechanisms for Gemini API unavailability
- **Validation Errors**: Input validation for all required fields
- **Summarization Fallbacks**: Keyword extraction when AI summarization fails

### Performance Optimizations
- **Connection Pooling**: HikariCP with optimized pool settings
- **Lazy Loading**: JPA entities use lazy loading to reduce memory usage
- **Batch Processing**: Efficient conversation summarization in batches
- **Context Optimization**: Smart context building reduces token usage
- **Database Indexing**: Strategic indexes for fast conversation retrieval

### Configuration Management
- **Environment Variables**: Secure handling of sensitive configuration
- **Profile-based Configuration**: Different settings for dev/prod environments
- **Spring Boot Auto-configuration**: Leverages Spring Boot's intelligent defaults
- **Custom Properties**: Configurable thresholds for summarization triggers

## 🔄 Deployment Considerations

### Production Readiness
- **Health Checks**: Built-in Spring Boot Actuator endpoints
- **Logging**: Comprehensive logging with configurable levels
- **Error Handling**: Graceful degradation and error responses
- **Security**: Environment-based configuration for sensitive data

### Monitoring & Observability
- **Response Time Tracking**: Built-in performance monitoring
- **Database Connection Monitoring**: HikariCP metrics
- **AI API Usage Tracking**: Request/response logging
- **Conversation Analytics**: Summary generation statistics

### Scaling Considerations
- **Database Connection Pooling**: Configured for high concurrency
- **Stateless Design**: Service layer is fully stateless for horizontal scaling
- **Efficient Queries**: Optimized database queries for performance
- **Context Management**: Intelligent summarization for memory efficiency

## 📈 Recent Updates & Features

- ✅ **Persistent Chat History**: Complete conversation storage and retrieval system
- ✅ **Context-Aware AI**: Intelligent response generation using conversation history
- ✅ **Automatic History Saving**: Every conversation automatically persisted to database
- ✅ **Enhanced ChatService Architecture**: Proper separation of concerns with clean service layer
- ✅ **Database Relationships**: Robust JPA entities with proper foreign key relationships
- ✅ **Response Time Tracking**: Performance monitoring for chat response times

### 🆕 **Latest: AI-Powered Conversation Summarization System**

#### ✨ Key Capabilities
- **🤖 Automatic AI Summarization**: Uses Gemini API to create intelligent conversation summaries
- **📊 Smart Context Management**: Recent 5 messages in detail + summarized older context
- **⚙️ Configurable Thresholds**: Starts summarizing after 15+ total messages per user
- **💾 Efficient Storage**: Reduces context length by ~60% while preserving conversation continuity
- **🛡️ Robust Fallback**: Simple keyword extraction if AI summarization fails
- **🔄 Background Processing**: Summarization happens seamlessly during normal chat flow
- **📦 Batch Processing**: Groups 8 conversations at a time for optimal efficiency

#### 🎯 Performance Benefits
- **⚡ Faster API Calls**: Reduced context length improves response times
- **💰 Lower Costs**: Optimized token usage while maintaining conversation quality
- **📈 Scalability**: Efficiently handles users with hundreds of conversations
- **🧠 Better Memory**: Long-term conversation continuity through intelligent summarization

#### 🔧 Technical Implementation
- **Database Fields**: Added `is_summarized` boolean and `summary` text fields
- **Repository Methods**: Enhanced queries for efficient conversation retrieval
- **Service Layer**: Comprehensive summarization logic with error handling
- **AI Integration**: Seamless Gemini API integration for summary generation

### 🔄 Context Building Algorithm
```
User Message → Check Conversation Count
├── < 15 conversations: Use recent 5 in detail
└── ≥ 15 conversations: 
    ├── Trigger summarization for older conversations
    ├── Batch process 8 conversations at a time
    ├── Generate AI summaries using Gemini
    ├── Store summaries in database
    └── Build context: [Summaries] + [Recent 5 detailed]
```

## Enhanced Chat History Features

### Conversation Summarization
The system now intelligently manages conversation context by:

1. **Keeping Recent Detail**: Last 5 conversations are kept in full detail for immediate context
2. **Summarizing Older Context**: Conversations older than the recent 5 are automatically summarized using AI
3. **Smart Triggering**: Summarization begins when a user has more than 15 total conversations
4. **Batch Processing**: Groups 8 conversations at a time for efficient summarization
5. **AI-Generated Summaries**: Uses Gemini API to create meaningful 2-3 sentence summaries
6. **Fallback Protection**: If AI summarization fails, uses keyword extraction as backup

### Database Schema Updates
```sql
-- Enhanced ChatHistory table with summarization support
ChatHistory (
  id, 
  user_id, 
  user_message, 
  bot_response, 
  created_at, 
  response_time,
  is_summarized,    -- NEW: Tracks if conversation is part of a summary
  summary           -- NEW: Stores AI-generated conversation summary
)
```

### Context Building Process
1. **Check Summary Trigger**: If user has 15+ conversations, trigger summarization
2. **Get Recent Messages**: Fetch last 5 conversations in full detail
3. **Get Summarized Context**: Retrieve summaries of older conversations
4. **Format Context**: Combine summarized context + detailed recent context
5. **Send to AI**: Enhanced prompt with both summary and recent details

### Benefits
- **Reduced Token Usage**: Shorter context while maintaining conversation continuity
- **Better Performance**: Faster API calls with optimized context length
- **Preserved History**: Long-term conversation memory through intelligent summarization
- **Scalable**: Handles users with hundreds of conversations efficiently

## 📄 License

This project is part of a chatbot service implementation for educational and development purposes.

---

## 🤝 Contributing

We welcome contributions! Please feel free to submit issues, feature requests, or pull requests.

### Development Workflow
1. Fork the repository
2. Create a feature branch
3. Implement your changes
4. Add tests if applicable
5. Submit a pull request

### Code Style
- Follow Java coding conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Ensure proper error handling

---

## 📞 Support

For questions or support, please:
- Create an issue in the repository
- Review the documentation thoroughly
- Check existing issues for similar problems

---

**Built with ❤️ using Spring Boot, Google Gemini AI, and modern Java practices**
