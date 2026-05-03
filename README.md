# 📖 STO Reader

> A powerful Android app for reading and writing stories — your own personal authoring suite, right in your pocket.

---

## ✨ Features

### 🗂️ Story Management

- **Recent Stories Library** — Home screen with quick access to all recently opened or edited stories
- **Create New Stories** — Start fresh with a custom title and build from scratch
- **Delete Stories** — Remove stories from your local library with a confirmation safety check
- **Persistent Storage** — Story progress and metadata are automatically saved locally via a custom `RecentStoriesManager`

---

### 📚 Immersive Reader

- **Story Cover Page** — See the title, author, genre, description, and chapter/episode stats before diving in
- **Hierarchical Navigation** — Browse through a clean **Stories → Chapters → Episodes** structure
- **Chapter Controls** — Next and Previous buttons to move between chapters seamlessly
- **Edge-to-Edge UI** — Transparent status and navigation bars for a fully immersive reading experience

---

### ✍️ Story Editor _(Authoring Suite)_

- **Metadata Editing** — Update the story's Title, Author, Genre, and Description at any time
- **Chapter Management** — Add or remove chapters on the fly
- **Episode Editor**
  - Add or remove episodes within any chapter
  - Edit episode numbers, names, and full story content
  - Expandable/collapsible chapter cards to keep your workspace organized

---

### 📁 File Operations & Format Support

- **Native `.sto` Format** — A custom tag-based format using `[meta]`, `[chapter]`, `[episode]`, and `[story]` tags
- **External File Integration**
  - Open `.sto` files directly from Android's file manager or any compatible app
  - Import external files straight into your Recent Stories library
- **Export Options**
  - Export to the native `.sto` format for sharing or backup
  - Export to **Plain Text (`.txt`)** for easy reading in other apps

---

### ⚙️ Technical Highlights

| Feature | Details |
|---|---|
| 🛡️ Custom Crash Reporter | Captures crashes and shows a detailed report dialog on next launch |
| ⚡ Background Processing | Uses **Kotlin Coroutines** for all disk I/O — UI stays smooth always |
| 🎨 Responsive Layouts | Built with **ViewBinding** and **Material Design** components |

---

> [!NOTE]
> This app uses a custom `.sto` file format. Files can be shared and opened across devices that have STO-Reader installed.

---

_Made with 💙 for readers and writers._
