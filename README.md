# Fpl-Stats
# FPL Stats Tracker

A full-stack application for tracking Fantasy Premier League (FPL) player statistics, performance history, and user team insights.

## 🧩 Technologies Used

### Backend (Spring Boot)
- Java 17
- Spring Web, Caching
- RESTful APIs
- Custom services for FPL data integration

### Frontend (Angular)
- Angular 17+
- RxJS
- Bootstrap / Tailwind CSS

## 📂 Project Structure

fpl-stats/
│
├── backend/ # Spring Boot application
│ └── src/
│ └── main/java/com/fpl/stats/...
│
├── web/ # Angular application
│ └── src/app/...
│
├── README.md
└── .gitignore

📈 Features
Retrieve real-time player history from FPL API

Cache and optimize player/team data

Calculate average points, total points per team

Determine captains, triple captains, and bench history

Easily extendable and scalable