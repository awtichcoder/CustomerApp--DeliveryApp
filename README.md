# DeliveryCustomer Android

Ứng dụng Android dành cho khách hàng của DeliveryApp. Dự án dùng Kotlin, Jetpack Compose và kiến trúc MVVM theo chức năng. Các request nghiệp vụ được gửi qua API Gateway; bản đồ, gợi ý địa chỉ và tìm đường dùng các dịch vụ bản đồ công khai riêng, không nhận JWT của DeliveryApp.

## Trạng thái hiện tại

Đã hoàn thành phần nền tảng dùng chung:

- Project Android API 26 trở lên, Kotlin và Jetpack Compose.
- Điều hướng cho 9 màn hình theo luồng Customer Figma v3.
- Retrofit, OkHttp và các API interface cho Identity, Order, Delivery, Loyalty.
- DTO request và response khớp với Backend hiện tại.
- `AppContainer` cung cấp các API dùng chung cho toàn ứng dụng.
- Đã tích hợp luồng tài khoản từ nhánh `feature/customer-account` với phần đơn hàng.
- DataStore lưu access token và refresh token.
- `AuthInterceptor` tự gắn `Authorization: Bearer <accessToken>`.
- Token không được Android Backup hoặc device transfer sao lưu.
- HTTP chỉ được cho phép ở bản debug để kết nối Backend local.
- Có GitHub Actions kiểm tra unit test, lint và debug build.

Phần tài khoản, đơn hàng và theo dõi giao hàng đã được kết nối trong cùng ứng dụng. Luồng end-to-end vẫn cần Backend và một tài khoản tài xế đang online để kiểm thử nhận đơn thực tế.

## Yêu cầu môi trường

- Android Studio có JDK 17 đi kèm.
- Android SDK 36.
- Máy ảo Android API 26 trở lên.
- Docker Desktop để chạy Backend DeliveryApp.
- Không cần Google Maps API key; bản đồ tracking dùng MapLibre và OpenFreeMap.
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

## Bản đồ, GPS và tìm địa chỉ

Màn hình tracking dùng **MapLibre Native** với style bản đồ từ **OpenFreeMap**, không cần API key hoặc tài khoản thanh toán. Bản đồ luôn hiển thị; khi Backend trả về vị trí tài xế, ứng dụng tự căn camera và đặt biểu tượng xe giao hàng. Ứng dụng luôn hiển thị ghi công `© OpenStreetMap contributors` trên bản đồ.

Form tạo đơn có nút **Dùng vị trí hiện tại**. Android sẽ xin quyền GPS khi người dùng bấm nút. Hai ô địa chỉ lấy/giao hàng gọi Photon để gợi ý địa chỉ Việt Nam và tự lưu tọa độ sau khi người dùng chọn. Photon dùng một Retrofit/OkHttp riêng nên access token của DeliveryApp không được gửi sang dịch vụ bản đồ bên ngoài.

Khi tài xế nhận đơn, ứng dụng gọi OSRM để lấy hình tuyến đường theo mạng lưới đường bộ. Trước lúc lấy hàng, tuyến được vẽ từ xe đến điểm lấy; sau trạng thái `PICKED_UP`, đích tự chuyển sang điểm giao. Khoảng cách và thời gian dự kiến được hiển thị bên dưới bản đồ.

Thiết bị cần có Internet để tải style và dữ liệu bản đồ. Không xóa phần ghi công và không tải hàng loạt dữ liệu bản đồ để dùng ngoại tuyến nếu chưa chọn nhà cung cấp tile phù hợp.

Photon và OSRM public phù hợp cho học tập/demo, không phải hạ tầng production có SLA. Nếu triển khai nhiều người dùng, cần tự host hoặc chọn nhà cung cấp bản đồ có quota/SLA rõ ràng.

## Chạy ứng dụng

### Luồng tạo đơn và ưu đãi

- Chọn mã từ danh sách do `GET /api/promotion` trả về hoặc chọn **Không dùng mã**.
- Bấm **Xem phí và điểm sử dụng**: app gọi `POST /api/order/preview`, sau đó `POST /api/loyalty/preview` bằng phí còn lại sau khuyến mãi. Xem trước không giữ điểm/mã.
- App hiển thị từng khoản phí, điểm khả dụng, điểm có thể dùng và tổng dự kiến; yêu cầu xác nhận lần nữa để tạo đơn. Sửa thông tin sẽ hủy preview cũ và yêu cầu tính lại. Nếu vượt số điểm có thể dùng, chọn số gợi ý trước khi xác nhận.
- Màn hình điểm thưởng hiển thị tổng điểm và điểm khả dụng riêng. SILVER từ 100, GOLD từ 500 điểm. Ưu đãi hạng 5%/10% chỉ là thông tin, chưa trừ vào phí.
- Chuông trang chủ mở danh sách cập nhật trạng thái đơn, có làm mới và mở chi tiết. Đây là cập nhật trong app, chưa phải push notification chạy nền.
- Trợ giúp ở tracking mở hướng dẫn, cho phép kết nối lại hoặc về chi tiết đơn.

Kiểm tra contract thật bằng Retrofit/Gson của FE với BE Docker (tài khoản demo cần có ít nhất một đơn COMPLETED):

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:RUN_BACKEND_CONTRACT = "1"
.\gradlew.bat testDebugUnitTest --rerun-tasks
```

Có thể đặt `TEST_CUSTOMER_EMAIL` và `TEST_CUSTOMER_PASSWORD` cho tài khoản thử riêng. Khi không đặt `RUN_BACKEND_CONTRACT`, bài test live này được bỏ qua; unit test còn lại không cần Docker. Contract test xác minh DTO Loyalty, Promotion, bốn tọa độ Delivery và chuỗi preview; test ViewModel xác minh đổi đích tuyến từ pickup sang dropoff sau khi lấy hàng.

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
- MapLibre hiển thị marker và tự cập nhật camera theo vị trí tài xế.
- Unit test cho OrderViewModel và logic polling.

Trạng thái triển khai:

- Đã hoàn thành form tạo đơn, validation và UUID `clientRequestId`.
- Đã khóa thao tác khi đang gửi để một lần bấm nhanh chỉ tạo một request.
- Đã hiển thị đầy đủ phí cơ bản, phụ phí, phí nền tảng, giảm khuyến mãi, giảm điểm và tổng phí.
- Đã hoàn thành danh sách đơn, lọc theo toàn bộ trạng thái và phân trang.
- Đã hoàn thành chi tiết đơn và hủy đơn bằng `reasonCode` + `version`.
- Khi gặp `ORDER_VERSION_CONFLICT`, ứng dụng tự tải lại phiên bản mới nhất.
- Đã polling trạng thái tìm tài xế mỗi 3 giây và dữ liệu tracking mỗi 30 giây.
- Polling được hủy bằng `DisposableEffect` khi rời màn hình.
- Đã có trạng thái loading, empty, lỗi API, mất kết nối và nút thử lại.
- Đã thêm unit test cho validation, chống gửi trùng, lọc/phân trang, version conflict và dừng polling.
- Giao diện dùng token màu của Figma Customer v3 (`#16A36A`, `#F6F8F7`, `#12211B`).

MapLibre đã được nhúng với nguồn bản đồ OpenFreeMap và không cần API key. Bản đồ vẫn hiển thị khi chưa có tọa độ và chuyển sang biểu tượng xe khi nhận vị trí tài xế.

Ứng dụng Customer chỉ **đọc** vị trí mới nhất từ Backend. Ứng dụng Shipper phải gửi GPS mới khoảng 30 giây/lần bằng `PUT /api/driver/me/location`, dùng Bearer token có role `DRIVER` và `recordedAt` theo UTC. Nếu app Shipper không gửi dữ liệu thì Customer vẫn thấy bản đồ nhưng không thể thấy xe di chuyển.

## Kiểm thử Phần 2

Sau khi Phần 1 đăng nhập và lưu JWT thành công:

1. Mở **Tạo đơn** và thử gửi thiếu dữ liệu để kiểm tra validation tại trường nhập.
2. Nhập ít nhất 3 ký tự ở hai ô địa chỉ rồi chọn đúng một gợi ý; hoặc dùng GPS cho địa chỉ lấy hàng. Nhập số điện thoại, mã ưu đãi/điểm nếu có rồi bấm tạo đơn liên tục; Backend chỉ được có một đơn cho cùng `clientRequestId`.
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
