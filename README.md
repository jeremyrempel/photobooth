## Overview
Drawing application that allows users to take a photo, select colors, draw. Additional features include saving, undoing and reset.

## Setup
To start application, open in Android Studio and run.

## Drawing
- Drawing is done by creating a path and calculating a route between paths. Drawing circles directly to canvas based on movements could led to "gaps" if movement was quick. ie. If user goes from x:0 -> x:100 very quickly with a brush width of 100 need to ensure that there is 100 unique circles drawn

## Saving
- Photos are only accessible via app in external dir, can be accessed externally on emulator or via rooted device
- Each layer is saved as a PNG with background

## Tests
- Only basicpPresenter tests were created, testing additional functionality would require significant mocking/ui/screenshot tests
- Test can be run with ./gradlew check

## Implementation Notes
- File operations completed on main thread, future enhancement offload and provide appropriate loaders
- Does not handle lifecycle events such as rotations. Would require persistance, reload mechanisms. Bundle not appropriate for images. Future enhancement
