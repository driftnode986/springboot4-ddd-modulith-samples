# 第16章　イベントをテストする

非同期で繋いだ処理を、待ち条件つきで確かめます。

```bash
./gradlew test
```

Docker は必要です。Docker Desktop や OrbStack を起動しておいてください。

## テストが1件スキップされます

`ModularityTests` は `@Disabled` です（理由は [ch12-ci-guards](../ch12-ci-guards/) を参照）。
`StagedAdoptionTests` が代わりに動きます。

## この章の要点

待つ条件を書かずに検査すると、まだ何も起きていない状態を見て通ってしまいます。

---

書籍『Spring Boot 4で始めるドメイン駆動設計 実装入門』のサンプルコードです。
各ディレクトリは独立した Gradle プロジェクトです。全体の説明は[リポジトリのルート](../)にあります。
