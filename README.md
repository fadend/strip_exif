# ExifStripper

Don't use this. Use https://exiftool.org/ instead.

I have used this project as an opportunity to learn
about dependency management in Gradle (and Gradle itself)
as well as the
[Apache Commons Imaging Library](https://commons.apache.org/proper/commons-imaging/)
and [picocli](https://picocli.info/).

Parts of the code closely follow this example:
https://github.com/apache/commons-imaging/blob/master/src/test/java/org/apache/commons/imaging/examples/WriteExifMetadataExample.java

## Example usage

Like I said, https://exiftool.org/ is the better option. But
here's one way to use it:

```
% ./gradlew run --args="--jpeg-file $HOME/Downloads/PXL_20241226_183115410.jpg \
  --strip-gps=true --tags ApertureValue,ShutterSpeedValue"
```