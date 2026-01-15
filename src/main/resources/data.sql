-- Sample data for testing Proje Pazari application
-- This script will be automatically executed when Spring Boot starts
-- Passwords are BCrypt hashed (all passwords are: Password123!)

-- Clean up existing data (optional, comment out if you want to preserve data)
-- DELETE FROM project_applications;
-- DELETE FROM project_entity_required_skills;
-- DELETE FROM projects;
-- DELETE FROM users;

-- Insert sample users
-- Password for all users: Password123!
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMye7L8K7mF7q5W0MzJWOWELz8Y5E5Q0W9K

INSERT INTO users (id, email, password, first_name, last_name, role, description, profile_picture_url, linkedin_url, github_url, preferred_language, created_at, updated_at, is_active)
VALUES
    ('01HQXV5KXBW9FYMN8CJZSP2R4H', 'admin@proje-pazari.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7L8K7mF7q5W0MzJWOWELz8Y5E5Q0W9K', 'Admin', 'User', 'ADMIN', 'System administrator account', NULL, NULL, NULL, 'tr', NOW(), NOW(), true),
    ('01HQXV5KXBW9FYMN8CJZSP2R4I', 'john.doe@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7L8K7mF7q5W0MzJWOWELz8Y5E5Q0W9K', 'John', 'Doe', 'USER', 'Full-stack developer with 5 years of experience in web and mobile development. Passionate about AI and machine learning.', NULL, 'https://linkedin.com/in/johndoe', 'https://github.com/johndoe', 'en', NOW(), NOW(), true),
    ('01HQXV5KXBW9FYMN8CJZSP2R4J', 'jane.smith@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7L8K7mF7q5W0MzJWOWELz8Y5E5Q0W9K', 'Jane', 'Smith', 'USER', 'UI/UX designer and frontend developer. Love creating beautiful and intuitive user interfaces.', NULL, 'https://linkedin.com/in/janesmith', 'https://github.com/janesmith', 'en', NOW(), NOW(), true),
    ('01HQXV5KXBW9FYMN8CJZSP2R4K', 'ahmet.yilmaz@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7L8K7mF7q5W0MzJWOWELz8Y5E5Q0W9K', 'Ahmet', 'Yılmaz', 'USER', 'Backend developer ve sistem mimarı. Java, Spring Boot ve microservices konusunda uzmanım.', NULL, 'https://linkedin.com/in/ahmetyilmaz', 'https://github.com/ahmetyilmaz', 'tr', NOW(), NOW(), true),
    ('01HQXV5KXBW9FYMN8CJZSP2R4L', 'ayse.kaya@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7L8K7mF7q5W0MzJWOWELz8Y5E5Q0W9K', 'Ayşe', 'Kaya', 'USER', 'Data scientist ve makine öğrenmesi uzmanı. Python ve TensorFlow ile çalışıyorum.', NULL, 'https://linkedin.com/in/aysekaya', 'https://github.com/aysekaya', 'tr', NOW(), NOW(), true),
    ('01HQXV5KXBW9FYMN8CJZSP2R4M', 'mehmet.demir@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7L8K7mF7q5W0MzJWOWELz8Y5E5Q0W9K', 'Mehmet', 'Demir', 'USER', 'Mobile developer. React Native ve Flutter ile iOS ve Android uygulamaları geliştiriyorum.', NULL, 'https://linkedin.com/in/mehmetdemir', 'https://github.com/mehmetdemir', 'tr', NOW(), NOW(), true)
ON CONFLICT (id) DO NOTHING;

-- Insert sample projects
INSERT INTO projects (id, title, description, summary, status, owner_id, max_team_size, current_team_size, category, deadline, created_at, updated_at)
VALUES
    -- Open Projects
    ('01HQXV6KXBW9FYMN8CJZSP3R4H', 
     'AI-Powered Chatbot for Customer Support', 
     'We are building an intelligent chatbot that uses natural language processing (NLP) and machine learning to provide automated customer support. The chatbot will be able to understand customer queries, provide relevant answers, and escalate complex issues to human agents. This project aims to reduce response time and improve customer satisfaction. Technologies: Python, TensorFlow, NLTK, FastAPI, PostgreSQL, Docker.',
     'AI chatbot with NLP capabilities for automated customer support',
     'OPEN',
     '01HQXV5KXBW9FYMN8CJZSP2R4I',
     5,
     2,
     'Artificial Intelligence',
     '2025-12-31 23:59:59',
     NOW(),
     NOW()),
    
    ('01HQXV6KXBW9FYMN8CJZSP3R4I',
     'Mobile Banking Application',
     'Development of a secure mobile banking application for iOS and Android platforms. The app will feature biometric authentication (fingerprint/face ID), real-time transaction monitoring, push notifications, bill payments, fund transfers, and investment portfolio management. Security is our top priority with end-to-end encryption and multi-factor authentication. Looking for experienced mobile developers and security experts.',
     'Secure mobile banking app with biometric authentication',
     'OPEN',
     '01HQXV5KXBW9FYMN8CJZSP2R4J',
     8,
     3,
     'Mobile Development',
     '2025-09-30 23:59:59',
     NOW(),
     NOW()),
    
    ('01HQXV6KXBW9FYMN8CJZSP3R4J',
     'E-Commerce Platform with Microservices',
     'Building a scalable e-commerce platform using microservices architecture. Services include: User Management, Product Catalog, Shopping Cart, Order Processing, Payment Gateway, Inventory Management, and Notification Service. The platform will use event-driven architecture with message queues for inter-service communication. Technologies: Java, Spring Boot, Kubernetes, Docker, RabbitMQ, PostgreSQL, Redis, Elasticsearch.',
     'Scalable e-commerce platform with microservices architecture',
     'IN_PROGRESS',
     '01HQXV5KXBW9FYMN8CJZSP2R4K',
     10,
     4,
     'Web Development',
     '2026-03-31 23:59:59',
     NOW(),
     NOW()),
    
    ('01HQXV6KXBW9FYMN8CJZSP3R4K',
     'Smart Home IoT Dashboard',
     'Creating a comprehensive IoT dashboard for smart home management. The dashboard will integrate with various IoT devices (lights, thermostats, security cameras, door locks) and provide real-time monitoring, automation rules, energy consumption analytics, and remote control capabilities. The system will use MQTT protocol for device communication.',
     'IoT dashboard for smart home device management',
     'OPEN',
     '01HQXV5KXBW9FYMN8CJZSP2R4L',
     6,
     2,
     'Internet of Things',
     '2025-08-15 23:59:59',
     NOW(),
     NOW()),
    
    ('01HQXV6KXBW9FYMN8CJZSP3R4L',
     'Healthcare Management System',
     'Comprehensive healthcare management system for hospitals and clinics. Features include: Patient Records Management, Appointment Scheduling, Prescription Management, Lab Results, Billing, Insurance Claims, and Analytics Dashboard. The system will comply with HIPAA regulations and ensure data privacy and security.',
     'Complete healthcare management system for hospitals',
     'IN_PROGRESS',
     '01HQXV5KXBW9FYMN8CJZSP2R4M',
     12,
     5,
     'Healthcare',
     '2026-06-30 23:59:59',
     NOW(),
     NOW()),
    
    -- Draft Projects
    ('01HQXV6KXBW9FYMN8CJZSP3R4M',
     'Social Media Analytics Tool',
     'Developing a tool to analyze social media trends, sentiment analysis, and engagement metrics across multiple platforms (Twitter, Instagram, Facebook, LinkedIn). The tool will provide actionable insights for businesses and marketers using machine learning algorithms.',
     'Social media analytics with sentiment analysis',
     'DRAFT',
     '01HQXV5KXBW9FYMN8CJZSP2R4I',
     7,
     1,
     'Data Analytics',
     '2025-11-30 23:59:59',
     NOW(),
     NOW()),
    
    ('01HQXV6KXBW9FYMN8CJZSP3R4N',
     'Blockchain-based Supply Chain',
     'Implementing a blockchain-based supply chain management system for transparency and traceability. The system will track products from manufacturer to end consumer, ensuring authenticity and reducing counterfeit products.',
     'Blockchain supply chain for product traceability',
     'DRAFT',
     '01HQXV5KXBW9FYMN8CJZSP2R4J',
     8,
     0,
     'Blockchain',
     '2026-02-28 23:59:59',
     NOW(),
     NOW()),
    
    -- Completed Projects
    ('01HQXV6KXBW9FYMN8CJZSP3R4O',
     'Online Learning Platform',
     'A complete online learning platform with video courses, quizzes, certificates, and progress tracking. Successfully launched and serving 10,000+ students.',
     'Online learning platform with video courses',
     'COMPLETED',
     '01HQXV5KXBW9FYMN8CJZSP2R4K',
     6,
     6,
     'Education',
     '2024-12-31 23:59:59',
     NOW() - INTERVAL '3 months',
     NOW() - INTERVAL '1 month'),
    
    ('01HQXV6KXBW9FYMN8CJZSP3R4P',
     'Real Estate Listing Website',
     'Real estate website with advanced search, virtual tours, mortgage calculator, and agent matching. Successfully completed and launched.',
     'Real estate platform with virtual tours',
     'COMPLETED',
     '01HQXV5KXBW9FYMN8CJZSP2R4L',
     5,
     5,
     'Web Development',
     '2024-10-31 23:59:59',
     NOW() - INTERVAL '5 months',
     NOW() - INTERVAL '2 months')
ON CONFLICT (id) DO NOTHING;

-- Insert required skills for projects
INSERT INTO project_entity_required_skills (project_entity_id, required_skill)
VALUES
    -- AI Chatbot
    ('01HQXV6KXBW9FYMN8CJZSP3R4H', 'Python'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4H', 'TensorFlow'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4H', 'NLP'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4H', 'Machine Learning'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4H', 'FastAPI'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4H', 'PostgreSQL'),
    
    -- Mobile Banking
    ('01HQXV6KXBW9FYMN8CJZSP3R4I', 'React Native'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4I', 'Node.js'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4I', 'MongoDB'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4I', 'Security'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4I', 'iOS'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4I', 'Android'),
    
    -- E-Commerce
    ('01HQXV6KXBW9FYMN8CJZSP3R4J', 'Java'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4J', 'Spring Boot'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4J', 'Kubernetes'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4J', 'Docker'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4J', 'Microservices'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4J', 'PostgreSQL'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4J', 'Redis'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4J', 'Elasticsearch'),
    
    -- Smart Home IoT
    ('01HQXV6KXBW9FYMN8CJZSP3R4K', 'IoT'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4K', 'MQTT'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4K', 'React'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4K', 'Node.js'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4K', 'Python'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4K', 'Raspberry Pi'),
    
    -- Healthcare
    ('01HQXV6KXBW9FYMN8CJZSP3R4L', 'Java'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4L', 'Spring Boot'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4L', 'Angular'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4L', 'PostgreSQL'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4L', 'HIPAA Compliance'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4L', 'Security'),
    
    -- Social Media Analytics
    ('01HQXV6KXBW9FYMN8CJZSP3R4M', 'Python'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4M', 'Machine Learning'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4M', 'NLP'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4M', 'Data Analysis'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4M', 'API Integration'),
    
    -- Blockchain Supply Chain
    ('01HQXV6KXBW9FYMN8CJZSP3R4N', 'Blockchain'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4N', 'Ethereum'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4N', 'Solidity'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4N', 'Web3.js'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4N', 'Smart Contracts'),
    
    -- Online Learning
    ('01HQXV6KXBW9FYMN8CJZSP3R4O', 'React'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4O', 'Node.js'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4O', 'MongoDB'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4O', 'Video Streaming'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4O', 'AWS'),
    
    -- Real Estate
    ('01HQXV6KXBW9FYMN8CJZSP3R4P', 'Vue.js'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4P', 'Laravel'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4P', 'MySQL'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4P', 'Google Maps API'),
    ('01HQXV6KXBW9FYMN8CJZSP3R4P', '3D Visualization')
ON CONFLICT DO NOTHING;
