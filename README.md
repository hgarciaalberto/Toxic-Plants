# Plants Detection

This app is designed to help users detect plants using the camera on their Android devices. 

It is built with Android Jetpack Compose and TensorFlow Hub, specifically using the TensorFlow Lite model available at 
https://tfhub.dev/google/lite-model/aiy/vision/classifier/plants_V1/3.

To properly adapt the input images for the model, I used the resource available at 
https://www.tensorflow.org/lite/inference_with_metadata/lite_support?hl=es-419. 

 The TensorFlow Lite model used in this app is hosted on Firebase and must be accessed by creating or having access to a Firebase project and adding the provided JSON file.

To use Firebase in this project, the following dependencies were added to the app-level build.gradle file:
```gradle
// Import the BoM for the Firebase platform
implementation platform('com.google.firebase:firebase-bom:31.2.3')
implementation 'com.google.firebase:firebase-ml-modeldownloader-ktx'
```

In addition to Firebase, the following TensorFlow dependencies were added:
```gradle
// TensorFlow
implementation 'org.tensorflow:tensorflow-lite:2.5.0'
implementation 'org.tensorflow:tensorflow-lite-support:0.1.0'
implementation 'org.tensorflow:tensorflow-lite-metadata:0.2.0'
```

To handle permissions in the app, the [Accompanist](https://google.github.io/accompanist/permissions/) library was used:
```gradle
implementation "com.google.accompanist:accompanist-permissions:0.30.0"
```

The following code was used to preprocess the input image:
```java
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

// Initialization code
// Create an ImageProcessor with all ops required. For more ops, please
// refer to the ImageProcessor Architecture section in this README.
ImageProcessor imageProcessor =
    new ImageProcessor.Builder()
        .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
        .build();

// Create a TensorImage object. This creates the tensor of the corresponding
// tensor type (uint8 in this case) that the TensorFlow Lite interpreter needs.
TensorImage tensorImage = new TensorImage(DataType.UINT8);

// Analysis code for every frame
// Preprocess the image
tensorImage.load(bitmap);
tensorImage = imageProcessor.process(tensorImage);
```

The app integrates the functionality of the CameraX API for capturing images and videos, and the TensorFlow Hub model for plant detection. 
To learn more about how the app integrates CameraX and Jetpack Compose, you can refer to this [Medium article](https://engineering.premise.com/video-capture-with-camerax-and-jetpack-compose-2e1425847ae3) for more details.

Overall, this app is a great example of how to integrate Firebase-hosted TensorFlow Lite models into Android applications, and we hope it serves as a helpful resource for others looking to do the same.
