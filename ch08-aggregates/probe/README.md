# probe/

コンパイルが通らないことを確かめるためのファイルを置いている。
Gradle のソースセットには含めず、javac に直接渡して使う。

```bash
./gradlew classes
javac -cp build/classes/java/main -d /tmp/probe-out probe/DirectConstruction.java
javac -cp build/classes/java/main -d /tmp/probe-out probe/DirectFieldAccess.java
```
