# 💜 Hesabak – Personal Finance Manager

**Hesabak (حسابک)** is a modern, offline Android application designed to help users manage their personal finances efficiently. It provides a simple and user-friendly interface for tracking income, expenses, and financial goals.

Built with **Java, XML, and SQLite**, Hesabak offers a clean, dark-themed interface with full support for the Dari language.

## ✨ Features

### 🔐 PIN Authentication
- Secure four-digit PIN authentication.
- PIN hashing using PBKDF2 with a random salt.
- Offline authentication without internet access.

### 📊 Financial Dashboard
- View total account balance.
- Track monthly income and expenses.
- Calculate monthly remaining balance.
- View recent financial transactions.
- Quick access to transaction management and financial reports.

### 💸 Transaction Management
- Add new income and expense transactions.
- Edit existing transactions.
- Delete transactions with confirmation.
- View all transaction history.
- Organize transactions by predefined categories.
- Add transaction dates and optional notes.

### 🔎 Search and Filters
- Search transactions by title or notes.
- Filter transactions by category.
- Filter by income or expense.
- Filter by date:
  - Today
  - Last 7 days
  - This month
  - Last month
  - This year
  - All time
  - Custom date range

### 📈 Financial Reports
- View income and expense summaries.
- Compare income and expenses using charts.
- Analyze spending by category.
- View spending amounts and percentages.
- Filter reports by time period.

### 🎨 User Interface
- Modern dark-mode design.
- Purple-accented dashboard.
- Fully right-to-left (RTL) interface.
- Dari-language labels.
- Responsive and user-friendly layouts.

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Application logic |
| XML | User interface |
| SQLite | Local database |
| SQLiteOpenHelper | Database management |
| RecyclerView | Transaction lists |
| Android Studio | Development environment |
| Gradle | Build automation |

## 🗄️ Database Structure

Hesabak uses a local SQLite database named `Hesabak.db`.

**Main tables:**

- `settings` – Stores hashed PIN and application settings.
- `transactions` – Stores income and expense records.
- `budgets` – Reserved for potential budget management functionality.

## 📱 Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/YOUR_USERNAME/Hesabak.git
   ```

2. Open the project in Android Studio.
3. Allow Gradle to synchronize.
4. Connect an Android device or start an emulator.
5. Run the application.

Alternatively, install the APK from the [latest release](https://github.com/Mahdyar-code/Hesabak/releases/latest).

You can see the APK in apk_file folder

## 🔒 Privacy and Security

Hesabak works completely offline. Financial information is stored locally on the user's device using SQLite.

- No internet connection required.
- No external server or cloud database.
- No user registration required.
- PIN verification uses a stored hash rather than plaintext.

**Note:** Device data removal or application uninstallation may permanently delete locally stored financial records.

## 🚀 Future Improvements

- PIN recovery functionality.
- Advanced financial visualization.
- Data backup and restoration.
- Export financial reports to PDF or Excel.
- Additional customization options.

## 👨‍💻 Developer

Developed by **Mohammad Mosa Mahdyar**

**Project:** Hesabak – Personal Finance Manager

**Platform:** Android

**Language:** Java

## 📄 License

This project was developed for educational and personal learning purposes. No open-source license has been specified yet.

---

**Made with ❤️ using Java, XML, and SQLite.**

*Hesabak – Manage your money, understand your spending.*
