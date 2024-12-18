app/
├── build.gradle.kts                 # 앱 수준 빌드 설정
├── src/main/
    ├── java/com/example/accountbooks/
    │   ├── data/                    # 데이터 계층
    │   │   ├── model/              # 데이터 모델
    │   │   │   ├── Transaction.kt  # 거래 데이터
    │   │   │   ├── User.kt        # 사용자 데이터
    │   │   │   ├── AccountBook.kt # 가계부 데이터
    │   │   │   └── Friend.kt      # 친구 데이터
    │   │   │
    │   │   ├── repository/         # 데이터 저장소
    │   │   │   ├── TransactionRepository.kt
    │   │   │   ├── UserRepository.kt
    │   │   │   ├── AccountBookRepository.kt
    │   │   │   └── FriendRepository.kt
    │   │   │
    │   │   └── datasource/         # 데이터 소스
    │   │       ├── local/          # 로컬 저장소
    │   │       └── remote/         # Firebase 연동
    │   │
    │   ├── domain/                 # 비즈니스 로직
    │   │   └── usecase/           # 유스케이스
    │   │       ├── transaction/    # 거래 관련
    │   │       ├── user/          # 사용자 관련
    │   │       ├── accountbook/   # 가계부 관련
    │   │       └── friend/        # 친구 관련
    │   │
    │   ├── presentation/           # UI 계층
    │   │   ├── view/              # UI 컴포넌트
    │   │   │   ├── activity/      # 액티비티
    │   │   │   ├── fragment/      # 프래그먼트
    │   │   │   └── adapter/       # RecyclerView 어댑터
    │   │   │
    │   │   └── viewmodel/         # ViewModels
    │   │
    │   └── util/                  # 유틸리티 클래스
    │
    ├── res/                        # 리소스 파일
    └── AndroidManifest.xml         # 앱 매니페스트
