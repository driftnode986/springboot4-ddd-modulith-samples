# 第6章　値オブジェクト

金額・数量・識別子を型にします。`int` や `String` のままでは通ってしまう間違いを、型で塞ぎます。

```bash
./gradlew test
```

Docker は不要です。

## 途中の版と、コンパイルが止まる確認用のコード

- `steps/strings-only/`: 呼び出し側を書く前の `PlaceOrderWithStrings`
- `steps/email-first-test/`: 最初に書く `EmailTest`（テスト1件の版）

`steps/` は `./gradlew test` で完成形のテストの後に続けてビルド・テストされます。

- `probes/typed-mix-up/`: 型を分けた `PlaceOrder` で、顧客と商品の ID を取り違える版。ビルドの対象外です。
  `src/` に上書きで写して `./gradlew compileJava` を実行すると、コンパイルが止まります（`expected-output.txt`）。確かめたら元に戻してください

## この章の要点

`before/PlaceOrderWithStrings.java` に、型にする前の姿を残してあります。

---

書籍『Spring Boot 4で始めるドメイン駆動設計 実装入門』のサンプルコードです。
各ディレクトリは独立した Gradle プロジェクトです。全体の説明は[リポジトリのルート](../)にあります。
