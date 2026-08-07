# Spring Boot 4で始めるドメイン駆動設計 実装入門 サンプルコード

書籍『Spring Boot 4で始めるドメイン駆動設計 実装入門』のサンプルコードです。

Spring Boot 4.1 と Spring Modulith 2.1 で、注文・在庫・支払い・カタログの4つを
別々のモジュールに分けた EC アプリケーションを、1つのプロセスとして組み立てます。
モジュールの境界をテストで守り、崩れたら CI で落とすところまでを扱います。

## 動かしてみる

必要なのは **Java 21** と **Docker** の2つです。Gradle は同梱の wrapper を使うので、
別途インストールする必要はありません。

まず、Docker が要らない章から試すのが早いです。

```bash
git clone https://github.com/driftnode986/springboot4-ddd-modulith-samples.git
cd springboot4-ddd-modulith-samples/ch06-value-objects
./gradlew test
```

`BUILD SUCCESSFUL` が出れば環境は整っています。

第9章以降はテストで実際の PostgreSQL を起動するため、**Docker を動かしておいてください**。
H2 などのインメモリ DB は使っていません。本番と同じデータベースで確かめるためです。

```bash
cd ../ch09-repositories
./gradlew test          # 初回は PostgreSQL イメージの取得に少し時間がかかります
```

## ディレクトリの構成

**章ごとに独立した Gradle プロジェクト**になっています。前の章の続きから始めたいときは、
対応するディレクトリをコピーして使ってください。

第1章・第3章・第4章には対応するディレクトリがありません。独立した Gradle プロジェクトとして
動かすコードがなく、本文の中で図とコード片だけを使って説明する章です
（第4章は第2部の冒頭にあたります）。

### 第1部　なぜ境界が壊れるのか

| ディレクトリ | 章 | 内容 | Docker |
|---|---|---|---|
| `ch02-first-run/` | 第2章 | 4モジュールの立ち上げと、構造を検証するテスト | テストは不要 |

### 第2部　境界を作る：ドメインモデルとモジュール

| ディレクトリ | 章 | 内容 | Docker |
|---|---|---|---|
| `ch05-package-by-feature/` | 第5章 | 機能で切る構成と、公開面の宣言 | 不要 |
| `ch06-value-objects/` | 第6章 | 値オブジェクトをテストから書く | 不要 |
| `ch07-sealed-states/` | 第7章 | 状態を `sealed interface` で表す | 不要 |
| `ch08-aggregates/` | 第8章 | 集約に不変条件を集める | 不要 |
| `ch09-repositories/` | 第9章 | リポジトリで永続化を境界の外に置く | 必要 |
| `ch10-application-services/` | 第10章 | アプリケーションサービスと入り口 | 必要 |
| `ch11-redrawing-boundaries/` | 第11章 | 境界の引き直しと、出荷モジュールの切り出し | 必要 |
| `ch12-ci-guards/` | 第12章 | 境界の検査を CI に載せ、ArchUnit で層構造も見る | 必要 |

### 第3部　境界をまたぐ：イベント・データ・外部サービス

| ディレクトリ | 章 | 内容 | Docker |
|---|---|---|---|
| `ch13-transaction-boundaries/` | 第13章 | 直接呼び出しとドメインイベントのトランザクション境界 | 必要 |
| `ch14-domain-events/` | 第14章 | イベントで在庫を減らし、重複前提で受ける | 必要 |
| `ch15-delivery-guarantees/` | 第15章 | 配送保証と、残った記録の扱い | 必要 |
| `ch16-testing-events/` | 第16章 | イベントで繋いだ処理をテストする | 必要 |
| `ch17-schema-separation/` | 第17章 | モジュールごとにスキーマを分ける | 必要 |
| `ch18-external-boundary/` | 第18章 | 外部の決済サービスとの境界 | 必要 |
| `ch19-idempotency/` | 第19章 | 冪等キーで二重課金を防ぐ | 必要 |
| `ch20-compensation/` | 第20章 | 補償トランザクションで引き当てを戻す | 必要 |

`ch20-compensation/` が最終形です。全体を先に見たいときはここから読んでください。

## 使っている版

| | |
|---|---|
| Java | 21 (LTS) |
| Spring Boot | 4.1.0 |
| Spring Framework | 7.0.8 |
| Spring Modulith | 2.1.0 |
| Gradle | 9.5.0 (wrapper 同梱・Groovy DSL) |
| PostgreSQL | 18 (Testcontainers が起動します) |

本書に載せている実行結果・エラーメッセージ・実行時間は、すべてこの環境で実際に走らせて
採取したものです。

## テストが1件スキップされます

第12章以降のディレクトリでは、`ModularityTests` が `@Disabled` になっています。
第11章までは有効で、通常どおり緑になります。故障ではありません。

このプロジェクトは**既知の違反を1件わざと持ったまま**進みます。第12章の
「既存のプロジェクトに後から入れる」で、すでに動いているアプリケーションに検査を
導入するとき、違反をいきなり全部直すことはできない、という状況を扱うためです。

代わりに `StagedAdoptionTests` が動いていて、**新しい違反が増えたときだけ落ちる**
形になっています。

## うまくいかないときは

**`Could not find a valid Docker environment` と出る**
Docker が起動していません。Docker Desktop や OrbStack を立ち上げてから、もう一度実行してください。

**初回のテストが極端に遅い**
PostgreSQL 18 のイメージを取得しています。2回目以降は速くなります。

**`UnsupportedClassVersionError` と出る**
Java 21 以外で動いています。`java -version` を確認してください。
