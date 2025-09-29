<#!
Usage (PowerShell):
  Set-Location "D:/StudyDoc/NAM3/PTUDDN/gs-securing-web-main-main/gs-securing-web-main-main/initial"
  # Start app first (separate terminal): .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2
  # Then run tests:
  .\api-testing\test-api.ps1 -BaseUrl "http://localhost:8080" -AdminUser admin -AdminPass adminpass -UserUser user -UserPass password

Parameters:
  -BaseUrl    Base URL of the running server
  -AdminUser  Admin username
  -AdminPass  Admin password
  -UserUser   Normal user username
  -UserPass   Normal user password
!#>
param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$AdminUser = "admin",
  [string]$AdminPass = "adminpass",
  [string]$UserUser = "user",
  [string]$UserPass = "password"
)

function Login($u,$p) {
  $body = @{ username = $u; password = $p } | ConvertTo-Json -Compress
  $resp = curl.exe -s -X POST "$BaseUrl/api/auth/login" -H "Content-Type: application/json" -d $body
  if (-not $resp) { throw "Empty response for login $u" }
  try { ($resp | ConvertFrom-Json).token } catch { throw "Login parse failed: $resp" }
}

Write-Host "== Logging in =="
$adminToken = Login $AdminUser $AdminPass
$userToken  = Login $UserUser  $UserPass
Write-Host "Admin Token: $($adminToken.Substring(0,25))..." -ForegroundColor Yellow
Write-Host "User  Token: $($userToken.Substring(0,25))..." -ForegroundColor Yellow

function CallJson($method,$url,$token,$data) {
  $headers = @()
  if ($token) { $headers += "Authorization: Bearer $token" }
  if ($data) { $headers += "Content-Type: application/json" }
  $args = @('-s','-X',$method,$url)
  foreach($h in $headers){ $args += @('-H',$h) }
  if ($data) { $args += @('-d',$data) }
  $raw = curl.exe @args
  return $raw
}

Write-Host "== USER creates a blog =="
$blogCreate = CallJson POST "$BaseUrl/api/blogs" $userToken '{"title":"Demo Blog","content":"Created via script"}'
Write-Host $blogCreate

Write-Host "== USER list own blogs =="
$userBlogs = CallJson GET "$BaseUrl/api/blogs" $userToken $null
Write-Host $userBlogs

# Extract first blog id (simple regex parse)
$firstId = ($userBlogs | Select-String -Pattern '"id":\s*(\d+)' -AllMatches).Matches[0].Groups[1].Value
if (-not $firstId) { Write-Warning "Could not parse blog id" } else { Write-Host "Parsed blog id=$firstId" }

Write-Host "== USER update blog =="
if ($firstId) {
  $updated = CallJson PUT "$BaseUrl/api/blogs/$firstId" $userToken '{"title":"Updated Title"}'
  Write-Host $updated
}

Write-Host "== USER try delete blog (should 403) =="
if ($firstId) {
  $delUser = curl.exe -s -o /dev/null -w "%{http_code}" -X DELETE "$BaseUrl/api/blogs/$firstId" -H "Authorization: Bearer $userToken"
  Write-Host "HTTP Status: $delUser (expected 403)"
}

Write-Host "== ADMIN delete blog (should 204) =="
if ($firstId) {
  $delAdmin = curl.exe -s -o /dev/null -w "%{http_code}" -X DELETE "$BaseUrl/api/blogs/$firstId" -H "Authorization: Bearer $adminToken"
  Write-Host "HTTP Status: $delAdmin (expected 204)"
}

Write-Host "== ADMIN list users =="
$users = CallJson GET "$BaseUrl/api/users" $adminToken $null
Write-Host $users

Write-Host "== ADMIN create new user =="
$newUserJson = '{"username":"auto_user_' + [int](Get-Random -Maximum 10000) + '","roles":["USER"]}'
$newUserResp = CallJson POST "$BaseUrl/api/users" $adminToken $newUserJson
Write-Host $newUserResp

Write-Host "== DONE ==" -ForegroundColor Green

