# flutter_rtmp_publisher

This Plugins helps to brodcast Live via RTMP right from your flutter application. In a single Line of code.  

Under the hood plugins uses [SRS(Simple-RTMP-Server)](https://github.com/ossrs/srs) for Android and [LFLiveKit](https://github.com/LaiFengiOS/LFLiveKit) for iOS

> Supported Platforms

>  * Android
>  * iOS

## How to use

```yaml
# add this line to your dependencies
flutter_rtmp_publisher: ^0.0.1
```

```dart
import 'package:flutter_rtmp_publisher/flutter_rtmp_publisher.dart';
```

```dart
// To start the stream call
RTMPPublisher.streamVideo("<PLACE_YOUR_RTMP_STREAM_URL>");
```
It only takes one required parameter i.e., url

> * Make sure  your app has camera permission
> 
## Preview Images

<img src="https://raw.githubusercontent.com/PonnamKarthik/FlutterRtmpPlublisher/master/screenshot/1.png" width="320px" />

## If you need any features suggest


> if you like it please star

