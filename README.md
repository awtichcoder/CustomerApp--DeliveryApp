# DeliveryCustomer Android

Ứng dụng Android dành cho khách hàng của DeliveryApp. Dự án dùng Kotlin, Jetpack Compose và kiến trúc MVVM theo chức năng. Mọi request được gửi qua API Gateway của Backend.

## Trạng thái hiện tại

Đã hoàn thành phần nền tảng dùng chung:

- Project Android API 26 trở lên, Kotlin và Jetpack Compose.
- Điều hướng cho 9 màn hình theo luồng Customer Figma v3.
- Retrofit, OkHttp và các API interface cho Identity, Order, Delivery, Loyalty.
- DTO request và response khớp với Backend hiện tại.
- `AppContainer` cung cấp các API dùng chung cho toàn ứng dụng.
- DataStore lưu access token và refresh token.
- `AuthInterceptor` tự gắn `Authorization: Bearer <accessToken>`.
- Token không được Android Backup hoặc device transfer sao lưu.
- HTTP chỉ được cho phép ở bản debug để kết nối Backend local.
- Có GitHub Actions kiểm tra unit test, lint và debug build.

Phần 2 (Đơn hàng và theo dõi giao hàng) đã có Repository, ViewModel, giao diện Compose và unit test. Phần 1 (Tài khoản và trải nghiệm khách hàng) vẫn cần hoàn thiện để có luồng đăng nhập lấy JWT trước khi kiểm thử end-to-end trên ứng dụng.

## Yêu cầu môi trường

- Android Studio có JDK 17 đi kèm.
- Android SDK 36.
- Máy ảo Android API 26 trở lên.
- Docker Desktop để chạy Backend DeliveryApp.
- Git.

## Clone và mở dự án

```bash
git clone https://github.com/awtichcoder/CustomerApp--DeliveryApp.git
cd CustomerApp--DeliveryApp
```

Mở thư mục vừa clone bằng Android Studio, chọn **Trust Project** nếu được hỏi, sau đó chờ **Gradle Sync** hoàn tất. Không sao chép file `local.properties` từ máy khác; Android Studio sẽ tự tạo file này theo vị trí Android SDK của từng máy.

## Chạy Backend

Mở terminal tại repository Backend DeliveryApp.

Nếu dùng Git Bash:

```bash
cp .env.example .env
docker compose up --build -d
docker compose ps
curl http://localhost:6200/health/live
```

Nếu dùng PowerShell:

```powershell
Copy-Item .env.example .env
docker compose up --build -d
docker compose ps
curl http://localhost:6200/health/live
```

Gateway phải trả về trạng thái `Healthy` trước khi chạy ứng dụng Android.

## Kết nối API Gateway

Địa chỉ mặc định cho Android Emulator:

```text
http://10.0.2.2:6200/
```

`10.0.2.2` là địa chỉ để máy ảo Android truy cập `localhost` của máy tính. Cấu hình nằm trong `app/build.gradle.kts` dưới tên `BuildConfig.API_BASE_URL`.

Nếu chạy bằng điện thoại thật, thay `10.0.2.2` bằng IPv4 của máy tính trong cùng mạng Wi-Fi, ví dụ `http://192.168.1.10:6200/`. Firewall phải cho phép cổng 6200.

## Chạy ứng dụng

1. Trong Android Studio, chọn **Tools > Device Manager**.
2. Tạo hoặc mở máy ảo API 26 trở lên.
3. Chọn thiết bị ở thanh công cụ.
4. Bấm **Run app**.

Có thể kiểm tra bằng PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

APK debug được tạo tại `app/build/outputs/apk/debug/app-debug.apk`.

## Cấu trúc code

```text
com.deliveryapp.customer
├── AppContainer.kt
├── DeliveryCustomerApplication.kt
├── MainActivity.kt
├── navigation
│   ├── AppRoutes.kt
│   └── AppNavHost.kt
├── core
│   ├── network
│   │   ├── ApiResult.kt
│   │   ├── AuthInterceptor.kt
│   │   ├── NetworkFactory.kt
│   │   └── SafeApiCall.kt
│   └── storage
│       └── TokenManager.kt
├── data
│   ├── api
│   └── dto
├── feature
│   ├── auth
│   ├── home
│   ├── order
│   ├── tracking
│   ├── rewards
│   └── profile
└── ui
    ├── components
    └── theme
```

Không tạo Retrofit riêng trong từng màn hình. ViewModel hoặc Repository lấy API từ `DeliveryCustomerApplication.container` để cùng dùng một cấu hình Gateway và Bearer token.

## Phân công hai phần

### Phần 1 Tài khoản và trải nghiệm khách hàng

Branch: `feature/customer-account`

Phạm vi chính:

- `feature/auth`
- `feature/home`
- `feature/profile`
- `feature/rewards`
- `core/storage`
- `IdentityApi.kt` và `LoyaltyApi.kt`

Đầu việc:

- Đăng ký, đăng nhập, refresh token và đăng xuất.
- Trang chủ, hồ sơ người dùng và điểm thưởng.
- Validation, loading, lỗi mạng và session expired.
- Unit test cho AuthViewModel và TokenManager.

### Phần 2 Đơn hàng và theo dõi giao hàng

Branch: `feature/customer-order`

Phạm vi chính:

- `feature/order`
- `feature/tracking`
- `OrderApi.kt` và `DeliveryApi.kt`

Đầu việc:

- Tạo đơn, danh sách đơn, chi tiết và hủy đơn.
- Polling trạng thái tìm tài xế và vị trí tài xế.
- Tích hợp bản đồ sau khi có Google Maps API key.
- Unit test cho OrderViewModel và logic polling.

Trạng thái triển khai:

- Đã hoàn thành form tạo đơn, validation và UUID `clientRequestId`.
- Đã khóa thao tác khi đang gửi để một lần bấm nhanh chỉ tạo một request.
- Đã hiển thị đầy đủ phí cơ bản, phụ phí, phí nền tảng, giảm khuyến mãi, giảm điểm và tổng phí.
- Đã hoàn thành danh sách đơn, lọc theo toàn bộ trạng thái và phân trang.
- Đã hoàn thành chi tiết đơn và hủy đơn bằng `reasonCode` + `version`.
- Khi gặp `ORDER_VERSION_CONFLICT`, ứng dụng tự tải lại phiên bản mới nhất.
- Đã polling trạng thái tìm tài xế mỗi 3 giây và vị trí tài xế mỗi 5 giây.
- Polling được hủy bằng `DisposableEffect` khi rời màn hình.
- Đã có trạng thái loading, empty, lỗi API, mất kết nối và nút thử lại.
- Đã thêm unit test cho validation, chống gửi trùng, lọc/phân trang, version conflict và dừng polling.
- Giao diện dùng token màu của Figma Customer v3 (`#16A36A`, `#F6F8F7`, `#12211B`).

Google Maps chưa được nhúng vì repository chưa có Maps API key. Màn hình tracking vẫn hiển thị tọa độ mới nhất, thời gian cập nhật, trạng thái kết nối và timeline giao hàng. Không commit Maps API key lên Git; khi nhóm cung cấp key, lưu key trong `local.properties` hoặc `secrets.properties` đã được `.gitignore` loại trừ.

## Kiểm thử Phần 2

Sau khi Phần 1 đăng nhập và lưu JWT thành công:

1. Mở **Tạo đơn** và thử gửi thiếu dữ liệu để kiểm tra validation tại trường nhập.
2. Nhập hai địa chỉ, tọa độ hợp lệ, số điện thoại, mã ưu đãi/điểm nếu có rồi bấm tạo đơn liên tục; Backend chỉ được có một đơn cho cùng `clientRequestId`.
3. Kiểm tra chi tiết phí trả về và màn hình trạng thái tìm tài xế.
4. Mở **Đơn hàng**, đổi bộ lọc và bấm **Tải thêm** khi có trên 20 đơn.
5. Hủy một đơn còn cho phép hủy; kiểm tra trạng thái `CANCELLED` và thông báo version conflict nếu dữ liệu đã thay đổi.
6. Khi tài xế nhận đơn, mở tracking và quan sát vị trí/timeline cập nhật. Rời màn hình để xác nhận polling dừng.

Chạy kiểm thử tự động:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

## Quy trình Git

Mỗi phần tạo branch từ cùng phiên bản `main`:

```bash
git switch main
git pull
git switch -c feature/customer-account
```

Phần 2 dùng tên branch `feature/customer-order`. Không làm trực tiếp trên `main`. Trước khi mở Pull Request phải chạy:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

Không commit `local.properties`, thư mục build, file APK, access token, mật khẩu hoặc API key.

## Tiêu chí nghiệm thu chung

- Project clone về Gradle Sync và build được mà không cần sao chép file máy cá nhân.
- Đăng ký, đăng nhập, refresh token và đăng xuất hoạt động qua Gateway.
- Tạo đơn, xem danh sách, xem chi tiết và hủy đơn hoạt động.
- Trạng thái tìm tài xế và vị trí tài xế được cập nhật đúng.
- Điểm thưởng hiển thị và dùng được khi tạo đơn.
- Giao diện bám Figma Customer v3.
- Có trạng thái loading, empty, validation, lỗi mạng và session expired.
- Không chứa secret trong Git.
