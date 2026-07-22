# Project Notes For Codex

This repository is used from Windows PowerShell at `C:\Projects\raf`.

## Git

- The working tree uses `.gitdata` instead of a normal `.git` directory.
- Use explicit Git paths:
  - `git --git-dir=.gitdata --work-tree=. status --short`
  - `git --git-dir=.gitdata --work-tree=. diff -- <paths>`
- Plain `git status` from the repo root fails with `fatal: not a git repository`.

## Frontend

- Run frontend commands from `C:\Projects\raf\frontend`.
- Use `npm.cmd`, not `npm`, in PowerShell.
- `npm run build` can fail because `npm.ps1` is blocked by Windows execution policy.
- Verified build command:
  - `npm.cmd run build`

## Dev Server

- `Start-Process` can fail on this machine with duplicate `Path`/`PATH` environment keys.
- A foreground Vite process can exit when the shell command session ends.
- Prefer verifying the code with `npm.cmd run build` first.
- If a dev server is needed, use the existing approved direct Node/Vite command when possible:
  - `C:\WINDOWS\System32\WindowsPowerShell\v1.0\powershell.exe -Command "& 'C:\nvm4w\nodejs\node.exe' node_modules\vite\bin\vite.js --host 127.0.0.1"`
- After starting, verify with:
  - `Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue`
  - `Invoke-WebRequest -UseBasicParsing http://127.0.0.1:5173/`

## Deploy Checks

- Do not verify deployment by grepping UI text or built JS assets.
- UI copy changes frequently and Vite bundles can encode text differently.
- Use `GET /api/health`, which returns `ok`, for server readiness checks.

## Current Feature Context

- Customer requested a shared `code` field above the road-number inputs.
- Expected behavior: if `code` is `afg` and a user enters `1234`, the frontend should download `1234afg.pdf`.
- Existing backend already accepts complete file names, so this behavior belongs in the frontend before calling `/api/files/download` or `/api/files/download-batch`.
