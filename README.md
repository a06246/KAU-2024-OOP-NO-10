app/
├── build.gradle.kts                 # 앱 수준 빌드 설정
├── src/
│   └── main/
│       ├── java/com/example/accountbooks/
│       │   ├── data/                # 데이터 계층
│       │   │   ├── model/          # 데이터 모델 클래스들
│       │   │   │   ├── Transaction.kt        # 거래 데이터 모델
│       │   │   │   ├── User.kt              # 사용자 데이터 모델
│       │   │   │   ├── AccountBook.kt       # 가계부 데이터 모델
│       │   │   │   └── Friend.kt            # 친구 데이터 모델
│       │   │   │
│       │   │   ├── repository/     # 저장소 클래스들
│       │   │   │   ├── TransactionRepository.kt    # 거래 관련 저장소
│       │   │   │   ├── UserRepository.kt          # 사용자 관련 저장소
│       │   │   │   ├── AccountBookRepository.kt   # 가계부 관련 저장소
│       │   │   │   └── FriendRepository.kt        # 친구 관련 저장소
│       │   │   │
│       │   │   └── datasource/     # 데이터 소스 클래스들
│       │   │       ├── local/      # 로컬 데이터 소스
│       │   │       └── remote/     # 원격 데이터 소스 (Firebase)
│       │   │
│       │   ├── domain/             # 도메인 계층
│       │   │   └── usecase/        # 유스케이스 클래스들
│       │   │       ├── transaction/ # 거래 관련 유스케이스
│       │   │       ├── user/       # 사용자 관련 유스케이스
│       │   │       ├── accountbook/ # 가계부 관련 유스케이스
│       │   │       └── friend/      # 친구 관련 유스케이스
│       │   │
│       │   ├── presentation/       # 프레젠테이션 계층
│       │   │   ├── view/          # UI 컴포넌트들
│       │   │   │   ├── activity/  # 액티비티들
│       │   │   │   ├── fragment/  # 프래그먼트들
│       │   │   │   └── adapter/   # 리사이클러뷰 어댑터들
│       │   │   │
│       │   │   └── viewmodel/     # 뷰모델 클래스들
│       │   │
│       │   └── util/              # 유틸리티 클래스들
│       │
│       ├── res/                    # 리소스 파일들
│       └── AndroidManifest.xml     # 앱 매니페스트
└── build/                          # 빌드 출력 디렉토리
