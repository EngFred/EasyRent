# 🏠 EasyRent - Offline-First Rental Management App

## 📌 Overview
EasyRent is an **offline-first** rental management application designed for landlords to efficiently manage their properties and tenants. It provides local storage using **Room Database** and syncs data with **Supabase** for cloud backup. The app allows landlords to track **rent payments, expenses, and monthly income** while ensuring a smooth rental management experience.

## ✨ Features
✅ **User Authentication** - Secure login system.  
✅ **Create & Manage Rentals** - Add new rooms and tenants.  
✅ **Payment Tracking** - Manually enter payments (no integrated payment gateway).  
✅ **Expense Management** - Log and categorize expenses.  
✅ **Income Monitoring** - Track total monthly earnings.  
✅ **Automated Balance Reset** - Resets tenant balances at the end of each month.  
✅ **Email Reminders** - Notify tenants via email when rent is due. *(Uses Android email intent, no third-party service required.)*  
✅ **Cloud Syncing** - Background workers keep data synchronized with Supabase.  

## 📱 Screenshots
<!-- Add images here -->
<img src="https://github.com/user-attachments/assets/0816ce31-a6f2-4f19-b765-8770558d2b7a" width="450"> <img src="https://github.com/user-attachments/assets/311e8129-6b35-4fad-ba35-b20e93b655b6" width="450">

---

## 🛠 Tech Stack
- **Language**: Kotlin  
- **UI**: Jetpack Compose  
- **Architecture**: Clean Architecture, MVVM  
- **Dependency Injection**: Dagger Hilt  
- **Local Storage**: Room Database  
- **Cloud Storage**: Supabase  
- **Background Workers**: WorkManager  
- **Navigation**: Jetpack Navigation  

---
