# 第2章　まず動かす

4つのモジュールを立ち上げ、構造を検証するテストを書きます。壁を壊すとテストが赤くなることまで確かめます。

```bash
./gradlew test
```

`./gradlew test` だけなら Docker は不要です。ただし本章の後半でアプリケーションを
起動するときは PostgreSQL が要ります。

```bash
docker compose up -d
./gradlew bootRun
```

## 途中の版と、わざと壊す版

- `steps/first-test/`: 本文で最初に書く `ModularityTests`（`verify()` だけの版）。`./gradlew test` で完成形のテストの後に続けて回ります
- `probes/order-uses-internal/`: `order` から `inventory` の内部を直接使う版。ビルドの対象外です。
  `src/` に上書きで写して `./gradlew test` を実行すると、`verify()` が落ちます（実際のメッセージは `expected-output.txt`）。確かめたら元に戻してください

## この章の要点

パッケージを分けただけでは何も強制されません。`ModularityTests` が壁の役目をします。

---

書籍『Spring Boot 4で始めるドメイン駆動設計 実装入門』のサンプルコードです。
各ディレクトリは独立した Gradle プロジェクトです。全体の説明は[リポジトリのルート](../)にあります。
