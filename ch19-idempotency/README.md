# 第19章　冪等キー

応答が届かなかった請求を送り直しても、二重に課金されないようにします。

```bash
./gradlew test
```

Docker は必要です。Docker Desktop や OrbStack を起動しておいてください。

## テストが1件スキップされます

`ModularityTests` は `@Disabled` です（理由は [ch12-ci-guards](../ch12-ci-guards/) を参照）。
`StagedAdoptionTests` が代わりに動きます。

## この章の要点

送り直しと冪等キーは、片方だけでは成立しません。

---

書籍『Spring Boot 4で始めるドメイン駆動設計 実装入門』のサンプルコードです。
各ディレクトリは独立した Gradle プロジェクトです。全体の説明は[リポジトリのルート](../)にあります。
