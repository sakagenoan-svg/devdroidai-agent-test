# セキュリティガイドライン

**⚠️ 注意**: このリポジトリは DevDroidAI Phase α の dogfood テスト用リポジトリです。
本番環境での使用を想定していないため、セキュリティ管理に関しては段階的に整備されています。
テスト性質の機密情報であっても、漏洩防止の観点から本ガイドラインに従ってください。

このリポジトリに機密情報（APIキー、署名鍵、サービスアカウントなど）を絶対にコミット・プッシュしないためのルールをまとめます。

## コミット禁止ファイル

以下は `.gitignore` で除外済みです。**追加しないでください**。

| 種類 | 代表的なファイル |
| --- | --- |
| Android SDK パス | `local.properties` |
| 署名鍵 / キーストア | `*.jks`, `*.keystore`, `keystore.properties`, `signing.properties` |
| 環境変数 / シークレット | `.env`, `.env.*`, `secrets.properties`, `api_keys.properties` |
| Firebase / GCP | `google-services.json`, `firebase-adminsdk*.json`, `*-service-account.json` |
| 証明書 | `*.pem`, `*.p12`, `*.pfx` |
| Claude Code ローカル設定 | `.claude/` |

## シークレットの取り扱い方

### 1. APIキー等は `local.properties` または環境変数で管理

`local.properties` に書いて `BuildConfig` 経由で読み込む例:

```properties
# local.properties（コミット禁止）
MY_API_KEY=xxxxxxxxxxxx
```

```kotlin
// app/build.gradle.kts
import java.util.Properties
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}
android {
    defaultConfig {
        buildConfigField("String", "MY_API_KEY", "\"${localProps.getProperty("MY_API_KEY", "")}\"")
    }
    buildFeatures { buildConfig = true }
}
```

### 2. 署名鍵は `keystore.properties` に分離

```properties
# keystore.properties（コミット禁止）
storeFile=../release.jks
storePassword=xxxx
keyAlias=xxxx
keyPassword=xxxx
```

`release.jks` 本体もリポジトリには置かず、ローカルまたはシークレットストレージ（GitHub Actions Secrets など）で管理します。

### 3. CI/CD では GitHub Actions Secrets を使う

キーストアは Base64 化して Secret に保存し、ワークフロー内でデコードして使います。平文で `.yml` に書かない。

## プッシュ前チェック

プッシュ前に以下を実行して、誤ってシークレットをステージしていないか確認してください。

```bash
# ステージ対象にシークレットっぽいファイルがないか
git status
git diff --cached

# 追跡対象に危険なファイルがないか
git ls-files | grep -E '\.(jks|keystore|env|pem|p12)$|local\.properties|google-services\.json'
```

## もし誤ってコミットしてしまったら

1. **即座にそのキーを無効化・再発行する**（履歴から消しても漏洩前提で動く）
2. 履歴から除去: `git filter-repo` もしくは [BFG Repo-Cleaner](https://rtyley.github.io/bfg-repo-cleaner/) を使用
3. force push は共有ブランチでは原則避ける。やむを得ない場合は関係者に連絡してから

## リポジトリのリモート

```
origin         https://github.com/inaguchi/IdleGame.git
loveriko4-max  https://github.com/loveriko4-max/IdleGame.git
```

両方ともパブリック想定なら、**シークレットは絶対にコミットしない**を徹底してください。
