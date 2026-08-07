# 第18章　外部サービスとの境界

外部の決済サービスをポートで切り、相手が壊れてもこちらが止まらないようにします。

```bash
./gradlew test
```

Docker は必要です。Docker Desktop や OrbStack を起動しておいてください。

## テストが1件スキップされます

`ModularityTests` は `@Disabled` です（理由は [ch12-ci-guards](../ch12-ci-guards/) を参照）。
`StagedAdoptionTests` が代わりに動きます。

## この章の要点

待ち時間の上限を決め、失敗を自分の語彙に翻訳します。

---

書籍『Spring Boot 4で始めるドメイン駆動設計 実装入門』のサンプルコードです。
各ディレクトリは独立した Gradle プロジェクトです。全体の説明は[リポジトリのルート](../)にあります。
