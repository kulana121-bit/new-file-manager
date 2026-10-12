# New File Manager — Build Plan

## App Identity
- **Name:** Files
- **Package:** `com.ravi.newfilemanager`
- **Repo:** kulana121-bit/new-file-manager
- **Min SDK:** 29 (Android 10) — Ravi's phone
- **Target SDK:** 34
- **UI:** Jetpack Compose + Material3

## Design Language
- **Dashboard:** LOCKED — User's reference image (2026-10-12)
  - Reference: `workspace/user/media_library/image/ae/ae0aceeb106ef7c5c8b6034170d47895da1bb16c2028b93ebecd7a70dc313c27.jpg`
  - Style: Neumorphic/soft UI, pastel tones, light background
  - Layout:
    - Top: Search bar (rounded) + notification bell
    - "Storage" section: card with circular progress + "X GB / Y GB" + bar + "Free space"
    - "Quick Access": 2x2 pastel cards (Images=peach, Videos=purple, Documents=blue, Music=green)
    - "Recent Files": list with file-type icons + "..." menu
    - Bottom: floating dock (home, grid, settings)

## Features — v1 (Core)
1. **File Browsing** — list/grid, breadcrumbs, pull-to-refresh
2. **Dashboard** — LOCKED UI (search + storage card + quick access + recents + dock)
3. **Search** — by name, with filters
4. **File Operations** — open, share, rename, copy, move, delete, details
5. **Smart Cleaner** — cache, WhatsApp junk, APKs, temp files
6. **Themes** — light/dark mode, soft pastel accent colors
7. **Categories** — Images, Videos, Music, Documents
8. **Locked Files** — LOCKED UI (Settings option)
   - Reference: `workspace/user/media_library/image/9b/9b57780cbe33ab6c1050dea2f3977c9c2b32a0949175bf823d4cc84703da215f.jpg`
   - 4-digit PIN keypad, lock icon, secured storage
   - Access via Settings
   - **Unlocked state UI** — LOCKED (2026-10-12)
   - Reference: `workspace/user/media_library/image/6f/6f1f3996489788220e3fa455fa2234c0eb38af2f9ee066d0aff164891d3ef97a.jpg`
   - Status card (Unlocked), Vault Highlights, Locked Categories (horizontal), Locked Files list
   - **Private Photos grid** — LOCKED (2026-10-12)
   - Reference: `workspace/user/media_library/image/8a/8a040ef8ba24788a0ba8dd276a581e85cf841397cd93c31d499e2b7e447a88a9.jpg`
   - Search bar, Category Details (count + size), 3-col photo grid with lock badges, Add Photo + Bulk Options buttons
   - **Add Photo bottom sheet** — LOCKED (2026-10-12)
   - Reference: `workspace/user/media_library/image/64/64bba9775bb06512fa6beba1408cf2ca214af8ce7312b2da9171675f818dbf1b.jpg`
   - Take Photo, Import from Gallery, Upload from Files, Cloud Import, Cancel
   - **Delete Photo bottom sheet** — LOCKED (2026-10-12)
   - Reference: `workspace/user/media_library/image/88/88cc7f3b5749b5a7598abdfdb7c00dd02cf1c3a8dbc58987269edf6fa06c4411.jpg`
   - Remove from device, Remove from cloud, Delete from both, Cancel

## Features — v2 (Later)
- Private Vault (PIN + encryption)
- Google Drive integration
- Archive support (ZIP/7Z/RAR extract + create)
- Network storage (FTP/SMB)
- WiFi Transfer

## Build System
- GitHub Actions for release APK builds
- Debug + Release workflows

## Status (2026-10-12)
- [x] Repo created
- [x] Full app written — all 13 locked screens + 2 bottom sheets
  (dashboard, browse, search, settings, PIN, vault, private photos,
  internal storage, photo/video/audio players, PDF reader, HTML viewer)
- [x] Real backends: MediaStore, StatFs, AES-256-GCM vault (PBKDF2),
  SAF folders, PdfRenderer, MediaPlayer/VideoView, WebView
- [x] Cloud/Drive stubbed behind CloudProvider (enable later, no rework)
- [x] R8 release build wired (proven keep-all recipe), signed via GitHub Secrets
- [ ] Actions release build + APK download + delivery
