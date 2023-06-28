# QR-code-app-sample  

This repository contains a QR Code mobile app developed using Android Studio. The app utilizes a client/server architecture to generate and scan QR codes.  

# Features  

QR Code Generation: Generate custom QR codes in form of string using product informations.  

QR Code Scanning: Use device's camera or gallery to scan QR codes and extract the encoded id of the specific product.  

Offline Mode: Continue using the app's scanning and generation features even without an internet connection.  

# Architecture  

The QR Code mobile app follows a client/server architecture to facilitate the generation and scanning of QR codes. The key components of the architecture are as follows:  


Client-Side (Mobile App):  

User Interface: Provides a user-friendly interface for generating and scanning QR codes.  

QR Code Generation: Utilizes the app's logic to generate QR codes based on user input of a product.  

QR Code Scanning: Utilizes the device's camera or gallery to scan QR codes and extract data.  

Data Processing: Processes the scanned QR code data and performs necessary actions based on the content.  


Server-Side:  


Data Verification: Ensures the integrity and validity of the scanned QR code data.  

Database: a MySQL Database Stores products generated QR codes and retrieves scanned QR code data to search for a specific product.  

# Technologies Used  

The QR Code mobile app is built using the following technologies and frameworks:  


Android Studio: The primary IDE for developing Android applications.  

Java: The programming language used for Android app development.  

Android SDK: Provides the necessary tools and libraries for building Android apps.  


MySQL: A relational database used for storing products generated QR code string.  

Git: Version control system for collaborative development and code management.  

# Installation and Usage  

To install and use the QR Code mobile app, follow these steps:  

Clone the repository: git clone <repository-url>.  

Open the project in Android Studio.  

Build and run the app on an Android device or emulator.  

Use the app's features to generate and scan QR codes.  

# Contributing  

Contributions to this project are welcome. If you have any ideas, suggestions, or bug fixes, feel free to open an issue or submit a pull request.  


# License
This project is licensed under the MIT License. Feel free to use, modify, and distribute the code as per the license terms.  
