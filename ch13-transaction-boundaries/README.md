# 第13章　トランザクション境界

直接呼び出しとドメインイベントで、トランザクションの掛かり方がどう変わるかを確かめます。

```bash
./gradlew test
```

Docker は必要です。Docker Desktop や OrbStack を起動しておいてください。

## テストが1件スキップされます

`ModularityTests` は `@Disabled` です（理由は [ch12-ci-guards](../ch12-ci-guards/) を参照）。
`StagedAdoptionTests` が代わりに動きます。

## この章の要点

`@EnableAsync` がないと `@Async` は無視されますが、**テストは緑のまま**です。

---

書籍『Spring Boot 4で始めるドメイン駆動設計 実装入門』のサンプルコードです。
各ディレクトリは独立した Gradle プロジェクトです。全体の説明は[リポジトリのルート](../)にあります。
