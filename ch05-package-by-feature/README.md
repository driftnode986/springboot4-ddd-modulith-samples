# 第5章　機能でモジュールを組む

技術の役割ではなく業務の単位でパッケージを切り、公開する面と依存の向きを宣言します。

```bash
./gradlew test
```

Docker は不要です。

## わざと規約を破る確認用のクラス

`probes/` の下は、本文で「一時的に置いて確かめる」クラスです。ビルドの対象外です。
`src/` に写して確かめ、確かめたら消してください（実際のメッセージは各フォルダの `expected-output.txt`）。

- `probes/visibility/`: `./gradlew compileJava` は通る（Java の可視性は変わらない）
- `probes/bad-internal-access/`: `order` が `inventory` の内部の `Stock` を参照する → `./gradlew test` で `verify()` が落ちる
- `probes/bad-undeclared/`: 依存を宣言していない `inventory` が `catalog` を参照する → 落ちる
- `probes/cycle/`: `inventory` が `order` の公開面を参照して循環する（`inventory` の宣言も写し替える）→ 落ちる

## この章の要点

`package-info.java` の `allowedDependencies` が、参照してよい相手を決めます。

---

書籍『Spring Boot 4で始めるドメイン駆動設計 実装入門』のサンプルコードです。
各ディレクトリは独立した Gradle プロジェクトです。全体の説明は[リポジトリのルート](../)にあります。
