# devdroidai-agent-test

DevDroidAI Phase α agent worker E2E test target。Phase 2 dogfood と Phase α dogfood の B+D PASS rate 比較用 baseline。

## baseline 内容

- **source**: `loveriko4-max/IdleGame@7d282dc` (PR #36 memory leak 修正後、PR #87+ DataStore 導入 **直前** の clean state)
- **state**: Idle game core (tap → coin / upgrade purchase / 自動 generation)、**data 永続化なし** (in-memory のみ)、Compose UI minimal、Material 3 darkColorScheme
- **purpose**: Phase α dogfood で同 instruction (Foreground Service 追加 = PR #92 instruction) を実行、Phase 2 (60% B+D PASS) からの改善検証

## Phase 2 dogfood 比較対象

- PR #92 (merged): "IdleGame に Foreground Service で毎秒生成をバックグラウンド継続化する機能を追加" → CoinGenerationService.kt + GameViewModel.kt + MainActivity.kt + DataStore 統合
- PR #93 (closed、variability demo): 同 instruction で application attribute 全削除 = variability 100% 実証

→ Phase α では SDK + Container + Gateway + 4 tool MCP + AGENT_SYSTEM_PROMPT の architecture で同 instruction 再現、規律違反 / variability 改善測定

## 関連 memory (DevDroidAI repo `.claude/memory/`)

- `project_phase_alpha_day11_step5_prelude.md`: Day 11 Step 5 1 ケース dogfood framework
- `project_phase_alpha_day5_dogfood_framework.md`: Day 5 dogfood 5-10 ケース framework
- `project_phase_alpha_economic_design.md` Section 4: dogfood metrics 収集設計
- `project_phase2_dogfood_actuals.md`: Phase 2 N=7 actuals (PR #87-93)
- `project_phase2_dogfood_instruction_archive.md`: PR #92/#93 instruction 詳細 (本 commit と並行 memory 化)

## build / run

```bash
./gradlew build
./gradlew installDebug
```

requires: Android SDK 34+, JDK 17+, Kotlin 2.0.21+

## 関連 repo

- `devdroidai`: 本体 Cloudflare Worker (POST /agent → Queue → handleAgentMessage → runAgentLoop wrapper → agent worker delegate)
- `devdroidai-agent`: agent worker (Container + claude-agent-sdk + 4 tool MCP)、Day 9 Session B 動作実証 5/5 success
