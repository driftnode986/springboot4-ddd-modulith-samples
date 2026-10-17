# 第12章　CI で境界を守り続ける

境界の検査を CI に載せ、ArchUnit でモジュールの内側の層構造も見ます。

```bash
./gradlew test
```

Docker は必要です。Docker Desktop や OrbStack を起動しておいてください。

## テストが1件スキップされます

`ModularityTests` は `@Disabled` にしてあります。故障ではありません。
このプロジェクトは既知の違反を1件わざと持ったまま進みます。すでに動いている
アプリケーションに検査を導入するとき、違反をいきなり全部直すことはできない、という
状況を扱うためです（本章「既存のプロジェクトに後から入れる」）。

代わりに `StagedAdoptionTests` が動いていて、**新しい違反が増えたときだけ落ちます**。

## probes/ と steps/

- `probes/` … 本文で「置いてビルドすると止まる」確認用のファイル（`LayerViolationProbe`・`PaymentCatalogPeek`）。ビルドの対象外です。`cp -R probes/src .` で写して確かめ、確かめたら消してください。`LayerViolationProbe` は、本文のとおり `JpaOrderRepository` のクラスと構築子を一時的に `public` にしないとコンパイルできません
- `steps/single-job/` … 2段に分ける前の、最初のワークフロー
- `.github/workflows/build.yml` は見本です。GitHub Actions はリポジトリ直下の `.github/workflows` しか読まないので、このディレクトリの下では動きません。自分のリポジトリの直下に置いて使います

## この章の要点

`verify()` はモジュールの**間**、ArchUnit はモジュールの**内側**を見ます。

---

書籍『Spring Boot 4で始めるドメイン駆動設計 実装入門』のサンプルコードです。
各ディレクトリは独立した Gradle プロジェクトです。全体の説明は[リポジトリのルート](../)にあります。
