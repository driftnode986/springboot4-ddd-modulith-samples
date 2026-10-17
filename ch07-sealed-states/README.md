# 第7章　状態を型で表す

注文の状態を `sealed interface` で表し、その状態でのみ許される操作を型に結びつけます。

```bash
./gradlew test
```

Docker は不要です。

## 途中の版と、コンパイルが止まる確認用のコード

本文で `OrderState` は段階を追って書き換わります。途中の版は `steps/` にあり、`./gradlew test` で完成形のテストの後に続けてビルド・テストされます。

- `steps/s1-states/`: 5つの状態だけを並べた最初の版
- `steps/s2-checks/`: コンパクトコンストラクタで検査を入れた版
- `steps/s3-transitions/`: `reserve`・`pay`・`confirm` を足した版（`CancellableState` の前）
- `steps/s4-returned-default/`: `Returned` を足し、`switch` に `default` を書いた版（ビルドは通るが、返品が「処理中」と表示される）

`probes/` の下はビルドの対象外です。`src/` に写して `./gradlew compileJava` を実行すると、本文どおりコンパイルが止まります（各フォルダの `expected-output.txt`）。確かめたら元に戻してください。

- `probes/rogue/`: `permits` に無い実装を足す
- `probes/illegal-transition/`: 受付済から `pay` を呼ぶ
- `probes/returned/`: `Returned` を足し、`OrderStateView` を直さない（`OrderState.java` を置き換える）
- `probes/cancel-confirmed/`: 確定済の注文を `CancellableState` を受け取るメソッドに渡す

## この章の要点

`before/OrderWithEnum.java` が `enum` 版です。網羅性の検査が効かない様子を比べられます。

---

書籍『Spring Boot 4で始めるドメイン駆動設計 実装入門』のサンプルコードです。
各ディレクトリは独立した Gradle プロジェクトです。全体の説明は[リポジトリのルート](../)にあります。
