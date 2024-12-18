app/
├── build.gradle.kts                 # App level build configuration
├── src/main/
    ├── java/com/example/accountbooks/
    │   ├── data/                    # Data Layer
    │   │   ├── model/              # Data Models
    │   │   │   ├── Transaction.kt  # Transaction data
    │   │   │   ├── User.kt        # User data
    │   │   │   ├── AccountBook.kt # Account book data
    │   │   │   └── Friend.kt      # Friend data
    │   │   │
    │   │   ├── repository/         # Data Repositories
    │   │   │   ├── TransactionRepository.kt
    │   │   │   ├── UserRepository.kt
    │   │   │   ├── AccountBookRepository.kt
    │   │   │   └── FriendRepository.kt
    │   │   │
    │   │   └── datasource/         # Data Sources
    │   │       ├── local/          # Local storage
    │   │       └── remote/         # Firebase integration
    │   │
    │   ├── domain/                 # Business Logic
    │   │   └── usecase/           # Use Cases
    │   │       ├── transaction/    # Transaction related
    │   │       ├── user/          # User related
    │   │       ├── accountbook/   # Account book related
    │   │       └── friend/        # Friend related
    │   │
    │   ├── presentation/           # UI Layer
    │   │   ├── view/              # UI Components
    │   │   │   ├── activity/      # Activities
    │   │   │   ├── fragment/      # Fragments
    │   │   │   └── adapter/       # RecyclerView Adapters
    │   │   │
    │   │   └── viewmodel/         # ViewModels
    │   │
    │   └── util/                  # Utility Classes
    │
    ├── res/                        # Resource Files
    └── AndroidManifest.xml         # App Manifest
