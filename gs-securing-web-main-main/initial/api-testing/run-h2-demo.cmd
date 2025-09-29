@echo off
REM Build & chạy ứng dụng với profile H2 (seed admin/user) trên Windows CMD
setlocal

if not exist mvnw.cmd (
  echo [ERROR] File mvnw.cmd khong ton tai. Hay chay script nay trong thu muc chua pom.xml.
  exit /b 1
)

echo === Build (skip tests) ===
call mvnw.cmd -DskipTests clean package || goto :error

echo === Chay ung dung voi profile H2 ===
java -jar target\securing-web-complete-0.0.1-SNAPSHOT.jar --spring.profiles.active=h2

goto :eof
:error
echo Build failed.
exit /b 1

