# Plants Detection

This app is designed to help users detect plants using the camera on their Android devices. 
It is built with Android Jetpack Compose and TensorFlow Hub, specifically using the TensorFlow Lite model available at 
https://tfhub.dev/google/lite-model/aiy/vision/classifier/plants_V1/3.

To properly adapt the input images for the model, I used the resource available at 
https://www.tensorflow.org/lite/inference_with_metadata/lite_support?hl=es-419. 

Here is an example of how the input image processing was implemented in code:

``` java
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

I also used the resource available at 
https://engineering.premise.com/video-capture-with-camerax-and-jetpack-compose-2e1425847ae3 
to implement the camera for capturing video.

Overall, this app is a great example of how to integrate TensorFlow Hub models into Android applications, 
and I hope it serves as a helpful resource for others looking to do the same.
