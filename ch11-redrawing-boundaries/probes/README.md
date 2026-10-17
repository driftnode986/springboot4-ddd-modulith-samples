# probes/

本文で「一時的に書き換えて確かめる」と書いた状態のファイルです。
Gradle のビルドの対象には入れていません。確かめるときは `src/` に写し、終わったら戻します。

- `direct-call/` … 出荷の公開面を作る前に、注文のユースケースが出荷の実装クラスを直接受け取る状態。
  コンパイルは通り、`ModularityTests` の `verify()` が失敗します。
- `unused-dependency/` … `catalog` に使わない依存を宣言した状態。`verify()` は通ります。

```bash
cp -R probes/direct-call/src .
./gradlew test --tests com.example.shop.ModularityTests
git checkout -- src   # 元に戻す
```
