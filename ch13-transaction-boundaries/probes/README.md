# probes/

ビルドが止まることを確かめるためのファイルです。Gradle のビルドの対象には入っていません。

- `src/main/java/com/example/shop/inventory/package-info.java`: 第5章で書いた在庫モジュールの宣言（`allowedDependencies = {}`）。
  第13章でイベントを受け取るようにしたあと、この宣言のままだと検査が落ちることを確かめます。

```bash
cp -R probes/src .
./gradlew test    # StagedAdoptionTests（既知の違反を除いて、新しい違反がない）が「Module 'inventory' depends on named interface(s) 'order :: spi'」で落ちる
git checkout -- src/main/java/com/example/shop/inventory/package-info.java   # 元に戻す
```
