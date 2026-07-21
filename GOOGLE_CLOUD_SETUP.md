# Google Cloud Setup

Покрокова інструкція для підключення Docker-застосунку до Google Drive папки.

Цільова папка:

```text
https://drive.google.com/drive/folders/1hbWUcdEkdDcPMGi-9lTB1ykqcBsq1gTA
```

## 1. Зайти в Google Cloud

Відкрий:

```text
https://console.cloud.google.com/
```

Залогінься тим Google-акаунтом, який має доступ до потрібної Google Drive папки.

У верхній панелі Google Cloud є селектор проєкту. Натисни його.

## 2. Створити Google Cloud project

У селекторі проєкту натисни `New Project`.

Заповни:

```text
Project name: drive-downloader
Organization: No organization або поточна організація
Location: можна лишити як є
```

Натисни `Create`.

Після створення обери цей проєкт у верхньому селекторі.

## 3. Увімкнути Google Drive API

У лівому меню відкрий:

```text
APIs & Services -> Library
```

У пошуку введи:

```text
Google Drive API
```

Відкрий `Google Drive API` і натисни `Enable`.

Це потрібно, щоб Spring Boot застосунок міг викликати Drive API.

## 4. Створити service account

У лівому меню відкрий:

```text
IAM & Admin -> Service Accounts
```

Натисни `Create service account`.

Заповни:

```text
Service account name: drive-downloader
Service account ID: drive-downloader
Description: Downloads files from shared Google Drive folder
```

Натисни `Create and continue`.

На кроці ролей можна нічого не додавати. Для Google Drive доступу роль у Google Cloud не головна; доступ до Drive дається через share самої папки.

Натисни `Continue`, потім `Done`.

## 5. Скопіювати email service account

Після створення service account з'явиться у списку.

Email буде приблизно такий:

```text
drive-downloader@your-project-id.iam.gserviceaccount.com
```

Скопіюй цей email. Він потрібен для доступу до Google Drive папки.

## 6. Створити JSON key

У списку service accounts натисни на створений `drive-downloader`.

Перейди у вкладку:

```text
Keys
```

Натисни:

```text
Add key -> Create new key
```

Обери:

```text
JSON
```

Натисни `Create`.

Браузер скачає `.json` файл. Це секретний ключ. Його не можна комітити в git або передавати стороннім людям.

## 7. Пошерити Google Drive папку на service account

Відкрий папку:

```text
https://drive.google.com/drive/folders/1hbWUcdEkdDcPMGi-9lTB1ykqcBsq1gTA
```

Натисни `Share`.

У поле людей додай email service account, наприклад:

```text
drive-downloader@your-project-id.iam.gserviceaccount.com
```

Роль достатньо виставити:

```text
Viewer
```

Натисни `Send` або `Share`.

Важливо: service account не бачить твій Google Drive автоматично. Навіть якщо твій користувач має доступ, service account є окремою технічною особою. Тому папку треба пошерити саме на email service account.

## 8. Покласти JSON key у проєкт

У корені проєкту створи папку:

```powershell
mkdir secrets
```

Перейменуй скачаний JSON файл у:

```text
service-account.json
```

Поклади його сюди:

```text
C:\Projects\raf\secrets\service-account.json
```

Папка `secrets/` вже додана в `.gitignore`, тобто ключ не має потрапити в репозиторій.

## 9. Запустити через Docker Compose

У корені проєкту:

```powershell
cd C:\Projects\raf
docker compose up --build
```

Після старту відкрий:

```text
http://127.0.0.1:8080
```

Введи точну назву файлу з Google Drive папки і натисни `Скачати`.

## 10. Запуск готового image без rebuild

Якщо образ уже зібраний:

```powershell
docker run --rm -p 8080:8080 `
  -e GOOGLE_DRIVE_FOLDER_ID="1hbWUcdEkdDcPMGi-9lTB1ykqcBsq1gTA" `
  -e GOOGLE_APPLICATION_CREDENTIALS="/run/secrets/google-service-account.json" `
  -v "${PWD}\secrets\service-account.json:/run/secrets/google-service-account.json:ro" `
  google-drive-documents-downloader:latest
```

## 11. Передати образ на іншу машину

На твоїй машині:

```powershell
docker save google-drive-documents-downloader:latest -o google-drive-documents-downloader.tar
```

На іншій машині:

```powershell
docker load -i google-drive-documents-downloader.tar
```

Потім там також треба покласти `service-account.json` і запустити `docker run` або `docker compose up`.

## Типові проблеми

Якщо файл не знаходиться:

- перевір точну назву файлу, включно з розширенням;
- перевір, що файл лежить саме в цій папці, не в підпапці;
- перевір, що папка пошерена на service account email.

Якщо помилка доступу:

- перевір `GOOGLE_APPLICATION_CREDENTIALS`;
- перевір, що JSON файл реально змонтований;
- перевір, що Google Drive API enabled у тому project, де створено service account.

## Корисні офіційні посилання

```text
https://docs.cloud.google.com/iam/docs/service-accounts-create
https://docs.cloud.google.com/iam/docs/keys-create-delete
https://support.google.com/googleapi/answer/6158841
https://support.google.com/drive/answer/7166529
```
