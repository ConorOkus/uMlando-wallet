# uMlando-wallet
Lightning Dev Kit Android Demo Wallet 

This project uses a .aar package for the Android platforms that provide language bindings for the [LDK](https://lightningdevkit.org/). The Kotlin 
language bindings are created by the [ldk-garbagecollected](https://github.com/lightningdevkit/ldk-garbagecollected) project and the latest release can be found
[here](https://github.com/lightningdevkit/ldk-garbagecollected/releases).

To setup a local regtest development environment check out this [blog post](https://thunderbiscuit.com/posts/regtest-galore/) by [thunderbiscuit](https://twitter.com/thunderB__).

## How to use
To include the LDK Kotlin bindings in an Android project download the latest binary from [here](https://github.com/lightningdevkit/ldk-garbagecollected/releases)
and place it in your `libs` directory.

Then add to your `build.gradle` file:
```groovy
dependencies {
    // ...
    implementation fileTree(include: ['*.aar'], dir: 'libs')
    // ...
}
```

You may then import and use the `org.ldk` library in your Kotlin code. For example:
```kotlin 
import org.ldk.structs.*
```

_Note: i686 builds are not included in aars in order to preserve space - there are builds for x86_64, so make sure to install a compatible emulator_

## Features
* [x] On-chain BDK Wallet
* [x] Connect to a peer
* [x] List peers
* [x] Get Node ID
* [x] Open Channel
* [x] List Channels
* [ ] Send payment
* [ ] Receive payment 
* [ ] Close channel


