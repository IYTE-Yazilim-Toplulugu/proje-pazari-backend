# Sample Data for Testing

This directory contains sample data that is automatically loaded when the application starts.

## 📊 Sample Data Overview

### Users (6 total)

All users have the same password: **`Password123!`**

| Email | Role | Name | Description |
|-------|------|------|-------------|
| `admin@proje-pazari.com` | ADMIN | Admin User | System administrator |
| `john.doe@example.com` | USER | John Doe | Full-stack developer (English) |
| `jane.smith@example.com` | USER | Jane Smith | UI/UX designer (English) |
| `ahmet.yilmaz@example.com` | USER | Ahmet Yılmaz | Backend developer (Turkish) |
| `ayse.kaya@example.com` | USER | Ayşe Kaya | Data scientist (Turkish) |
| `mehmet.demir@example.com` | USER | Mehmet Demir | Mobile developer (Turkish) |

### Projects (9 total)

#### Active Projects (5)
1. **AI-Powered Chatbot for Customer Support**
   - Owner: John Doe
   - Category: Artificial Intelligence
   - Skills: Python, TensorFlow, NLP, Machine Learning, FastAPI, PostgreSQL

2. **Mobile Banking Application**
   - Owner: Jane Smith
   - Category: Mobile Development
   - Skills: React Native, Node.js, MongoDB, Security, iOS, Android

3. **E-Commerce Platform with Microservices**
   - Owner: Ahmet Yılmaz
   - Category: Web Development
   - Skills: Java, Spring Boot, Kubernetes, Docker, Microservices, PostgreSQL, Redis, Elasticsearch

4. **Smart Home IoT Dashboard**
   - Owner: Ayşe Kaya
   - Category: Internet of Things
   - Skills: IoT, MQTT, React, Node.js, Python, Raspberry Pi

5. **Healthcare Management System**
   - Owner: Mehmet Demir
   - Category: Healthcare
   - Skills: Java, Spring Boot, Angular, PostgreSQL, HIPAA Compliance, Security

#### Draft Projects (2)
6. **Social Media Analytics Tool**
   - Owner: John Doe
   - Category: Data Analytics
   - Skills: Python, Machine Learning, NLP, Data Analysis, API Integration

7. **Blockchain-based Supply Chain**
   - Owner: Jane Smith
   - Category: Blockchain
   - Skills: Blockchain, Ethereum, Solidity, Web3.js, Smart Contracts

#### Completed Projects (2)
8. **Online Learning Platform**
   - Owner: Ahmet Yılmaz
   - Category: Education
   - Skills: React, Node.js, MongoDB, Video Streaming, AWS

9. **Real Estate Listing Website**
   - Owner: Ayşe Kaya
   - Category: Web Development
   - Skills: Vue.js, Laravel, MySQL, Google Maps API, 3D Visualization

## 🔍 Testing Elasticsearch

After the application starts, the sample data will be automatically indexed to Elasticsearch. You can test search functionality with these example queries:

### Search Examples in Swagger UI

1. **Search by keyword**: 
   - `q=chatbot` → Finds AI Chatbot project
   - `q=banking` → Finds Mobile Banking project
   - `q=microservices` → Finds E-Commerce project

2. **Search by skill**:
   - `q=Python` → Finds projects requiring Python
   - `q=React Native` → Finds mobile projects
   - `q=Spring Boot` → Finds Java/Spring projects

3. **Search by category**:
   - `q=AI` or `q=Artificial Intelligence`
   - `q=Mobile Development`
   - `q=Healthcare`

4. **Filter by status**:
   - `status=ACTIVE` → Only active projects
   - `status=DRAFT` → Only draft projects
   - `status=COMPLETED` → Only completed projects

## 🚀 Quick Start

1. Start the application:
   ```bash
   make run
   ```

2. Wait for the data to load (you'll see log messages about data initialization)

3. Open Swagger UI:
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

4. **Login as any user:**
   - Go to `POST /api/v1/auth/login`
   - Username: `john.doe@example.com` (or any email from the list above)
   - Password: `Password123!`
   - Copy the JWT token from the response

5. **Authorize:**
   - Click the "Authorize" button (🔒)
   - Enter: `Bearer YOUR_JWT_TOKEN`
   - Click "Authorize"

6. **Test Elasticsearch:**
   - Go to `GET /api/v1/search/projects`
   - Enter a search query (e.g., `chatbot`, `banking`, `Python`)
   - Click "Execute"

## 🔄 Reindexing

If Elasticsearch indexes are out of sync, you can reindex all data:

1. Login as admin (`admin@proje-pazari.com`)
2. Use these endpoints:
   - `POST /api/v1/admin/elasticsearch/reindex/projects`
   - `POST /api/v1/admin/elasticsearch/reindex/users`

## 🗑️ Clearing Data

To remove all sample data and start fresh:

```sql
-- Connect to PostgreSQL
psql -U yazilim -d proje_pazari_db

-- Delete all data
DELETE FROM project_applications;
DELETE FROM project_entity_required_skills;
DELETE FROM projects;
DELETE FROM users;

-- Then restart the application to reload sample data
```

## 📝 Notes

- Data is loaded using `data.sql` script
- BCrypt password hashing is used (all passwords: `Password123!`)
- ULIDs are used for unique IDs
- Data includes Turkish and English content for i18n testing
- Projects have realistic descriptions and technical details
- Sample data covers various categories and technologies

## 🔐 Security Note

**⚠️ IMPORTANT:** These are sample credentials for development/testing only. 
**NEVER use these credentials in production!**
