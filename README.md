# springboot4-ddd-modulith-samples

『Spring Boot 4で始めるドメイン駆動設計 実装入門』のサンプルコードです。

章ごとにディレクトリを分けています。各ディレクトリで `./gradlew test` を実行できます。

第1章・第3章・第4章はコードを書かない章なので、対応するディレクトリはありません。

- `ch02-first-run/` — 第2章。4モジュールの立ち上げとモジュール構造の検証
- `ch05-package-by-feature/` — 第5章。機能で切る構成と、公開面の宣言
- `ch06-value-objects/` — 第6章。値オブジェクトをテストから書く
- `ch07-sealed-states/` — 第7章。状態を sealed interface で表す
- `ch08-aggregates/` — 第8章。集約に不変条件を集める
- `ch09-repositories/` — 第9章。リポジトリで保存先を隠す
- `ch10-application-services/` — 第10章。アプリケーションサービスと入り口
- `ch11-redrawing-boundaries/` — 第11章。腐敗防止層と、出荷モジュールの切り出し
- `ch12-ci-guards/` — 第12章。境界の検査を CI に載せ、ArchUnit で層構造も見る
- `ch13-transaction-boundaries/` — 第13章。直接呼び出しとドメインイベントのトランザクション境界
- `ch14-domain-events/` — 第14章。イベントで在庫を減らし、重複前提で受ける
- `ch15-delivery-guarantees/` — 第15章。配送保証、残った記録の扱い
- `ch16-testing-events/` — 第16章。イベントで繋いだ処理をテストする
- `ch17-schema-separation/` — 第17章。モジュールごとにスキーマを分ける
- `ch18-external-boundary/` — 第18章。外部の決済サービスとの境界
- `ch19-idempotency/` — 第19章。冪等キーで二重課金を防ぐ
- `ch20-compensation/` — 第20章。補償トランザクションで引き当てを戻す

## 前提

- Java 21
- Docker (PostgreSQL を起動するため)
