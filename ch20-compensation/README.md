# 第20章　補償トランザクション

支払いが成立しなかったとき、引き当てた在庫を戻します。本書の最終形です。

```bash
./gradlew test
```

Docker は必要です。Docker Desktop や OrbStack を起動しておいてください。

## テストが1件スキップされます

`ModularityTests` は `@Disabled` です（理由は [ch12-ci-guards](../ch12-ci-guards/) を参照）。
`StagedAdoptionTests` が代わりに動きます。

## この章の要点

補償は「打ち消し」ではなく「逆向きの加算」です。二度実行すると在庫が増えます。

---

書籍『Spring Boot 4で始めるドメイン駆動設計 実装入門』のサンプルコードです。
各ディレクトリは独立した Gradle プロジェクトです。全体の説明は[リポジトリのルート](../)にあります。
