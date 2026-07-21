# Google Drive Documents Downloader

Spring Boot + Vue застосунок для скачування файлу з Google Drive папки за назвою файлу.

Папка за замовчуванням:

```text
https://drive.google.com/drive/folders/1hbWUcdEkdDcPMGi-9lTB1ykqcBsq1gTA
```

У Docker образі Vue UI збирається в static files і віддається тим самим Spring Boot застосунком. Після запуску потрібен тільки один порт: `8080`.

## Доступ до Google Drive

Для приватної або пошереної папки потрібні Google credentials.

Найпростіший серверний варіант:

1. У Google Cloud увімкнути Google Drive API.
2. Створити service account і скачати JSON key.
3. В Google Drive пошерити цю папку на email service account.
4. Покласти JSON key у `secrets/service-account.json`.

Google Docs експортуються як PDF, Google Sheets як XLSX, Google Slides як PPTX. Звичайні файли скачуються як є.

## Запуск через Docker Compose

```powershell
mkdir secrets
```

Покладіть service account key сюди:

```text
secrets/service-account.json
```

Потім:

```powershell
docker compose up --build
```

UI буде доступний на:

```text
http://127.0.0.1:8080
```

## Запуск готового образу

Зібрати образ:

```powershell
docker build -t google-drive-documents-downloader:latest .
```

Запустити:

```powershell
docker run --rm -p 8080:8080 `
  -e GOOGLE_DRIVE_FOLDER_ID="1hbWUcdEkdDcPMGi-9lTB1ykqcBsq1gTA" `
  -e GOOGLE_APPLICATION_CREDENTIALS="/run/secrets/google-service-account.json" `
  -v "${PWD}\secrets\service-account.json:/run/secrets/google-service-account.json:ro" `
  google-drive-documents-downloader:latest
```

## Передача образу

Варіант без registry, через файл:

```powershell
docker save google-drive-documents-downloader:latest -o google-drive-documents-downloader.tar
```

На іншій машині:

```powershell
docker load -i google-drive-documents-downloader.tar
```

Варіант через Docker registry:

```powershell
docker tag google-drive-documents-downloader:latest YOUR_REGISTRY/google-drive-documents-downloader:latest
docker push YOUR_REGISTRY/google-drive-documents-downloader:latest
```

На іншій машині:

```powershell
docker pull YOUR_REGISTRY/google-drive-documents-downloader:latest
```

## Конфігурація

Змінні середовища:

```text
GOOGLE_DRIVE_FOLDER_ID=1hbWUcdEkdDcPMGi-9lTB1ykqcBsq1gTA
GOOGLE_APPLICATION_CREDENTIALS=/run/secrets/google-service-account.json
SERVER_PORT=8080
```

API:

```text
GET http://127.0.0.1:8080/api/files/download?fileName=report.pdf
```

## Локальний запуск для розробки

Бекенд:

```powershell
cd backend
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\service-account.json"
mvn spring-boot:run
```

Фронтенд:

```powershell
cd frontend
npm install
npm run dev
```

Dev UI буде доступний на:

```text
http://127.0.0.1:5173
```
