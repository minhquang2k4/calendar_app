# Hướng dẫn Git Flow để tạo tính năng mới

Quy trình làm việc với Git Flow khi tạo một tính năng mới bao gồm các bước sau:

## 1. Pull nhánh `main`

Trước khi bắt đầu, hãy đảm bảo bạn đang ở nhánh `main` và pull code mới nhất từ remote:

```bash
git checkout main
git pull origin main
```
## 2. Tạo nhánh mới

Tạo một nhánh mới từ nhánh `main` với tên phù hợp với tính năng bạn đang phát triển:

```bash
gitcheckout -b feature/ten-tinh-nang
```
## 3. Code và commit

Thực hiện các thay đổi cần thiết cho tính năng mới, sau đó commit lại:

```bash
git add .
git commit -m "Mô tả ngắn gọn về thay đổi"
```
## 4. Kéo code mới nhất về từ nhánh `main`
Trước khi push nhánh mới, hãy đảm bảo bạn đã cập nhật code mới nhất từ nhánh `main` để tránh xung đột:
```
git checkout main
git pull origin main
git checkout feature/ten-tinh-nang
git merge main
```
## 5. Xử lý conflict (nếu có)
Nếu có xung đột xảy ra trong quá trình merge, hãy mở các file bị xung đột, xử lý chúng, sau đó:
```bash
git add .
git commit -m "Resolve merge conflicts"
```
## 6. Push nhánh mới

Khi đã hoàn tất, push nhánh mới lên remote:
```bash
git push origin feature/ten-tinh-nang
```
## 7. Tạo Pull Request (PR)
Truy cập vào repository trên GitHub và tạo một Pull Request từ nhánh feature/ten-tinh-nang vào nhánh main. Nhờ người review và merge khi đã được chấp thuận.