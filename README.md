# springboot4-ddd-modulith-samples

『Spring Boot 4で始めるドメイン駆動設計 実装入門』のサンプルコードです。

章ごとにディレクトリを分けています。各ディレクトリで `./gradlew test` を実行できます。

- `ch02-first-run/` — 第2章。4モジュールの立ち上げとモジュール構造の検証
- `ch05-package-by-feature/` — 第5章。機能で切る構成と、公開面の宣言
- `ch06-value-objects/` — 第6章。値オブジェクトをテストから書く
- `ch07-sealed-states/` — 第7章。状態を sealed interface で表す
- `ch08-aggregates/` — 第8章。集約に不変条件を集める
- `ch09-repositories/` — 第9章。リポジトリで保存先を隠す
- `ch10-application-services/` — 第10章。アプリケーションサービスと入り口
- `ch11-redrawing-boundaries/` — 第11章。腐敗防止層と、出荷モジュールの切り出し

## 前提

- Java 21
- Docker (PostgreSQL を起動するため)
