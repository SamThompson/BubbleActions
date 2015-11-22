#BubbleActions

Inspired by the Pinterest Android app, `BubbleActions` make it easy to perform actions 
on ui elements by simply dragging your finger.

##Screenshots
1 | 2 | 3
--- | --- | --- 
![1](http://i.imgur.com/jbI6Bay.gif) | ![2](http://i.imgur.com/YEtNBmn.gif)  | ![3](http://i.imgur.com/BKllyFY.gif)


##Requirements and dependencies
`BubbleActions` works with api level 16 and higher. It also is dependent on appcompat-v7.


##Gradle
Coming soon


##Samples

####Building BubbleActions
`BubbleActions` are built using a [fluent interface](https://en.wikipedia.org/wiki/Fluent_interface) 
(similar to SnackBar) and supports adding up to 5 actions. You can build `BubbleActions` like this:
```java
BubbleActions.on(myView)                                                                              // Note 1
        .addAction("Star", R.drawable.ic_star, R.drawable.popup_item, new BubbleAction.Callback() {   // Note 2
            @Override
            public void doAction() {                                                                  // Note 3
                    Toast.makeText(v.getContext(), "Star pressed!", Toast.LENGTH_SHORT).show();
                }
            })
        // ... add more actions ...
        .show();                                                                                      // Note 4
```
1. We start off by declaring that we want `BubbleActions` on `myView`. Behind the scenes, the `BubbleActions` class uses
 reflection to get an instance of `ViewRootImpl` which will be used to detect touch events. If it doesn't find one, 
 it throws an `IllegalStateException`.
2. We add an action. Each action consists of a label, a foreground drawable/drawable resource id, a background 
drawable/drawable resource id, and a callback. The foreground drawable is the icon that appears inside the bubble, 
the background drawable controls the shape of the background and how it reacts to being selected, and the label
determines what text is displayed above the bubble.
3. When the user lifts their finger while over a bubble, the cooresponding callback is fired. 
This always happens on the main thread.
4. Show the `BubbleActions` by calling the `show()` method. Unlike SnackBar or AlertDialog, this method is not thread safe, so attempting
to show `BubbleActions` from a separate thread may lead to unexpected results. You do not necessarily have to show the 
`BubbleActions` immediately; the `BubbleActions` object can be stored for later use. The actions will remain visible as 
long as the user's finger is pressed to the screen.

####Basic example
In the activity we set a long click listener to show the `BubbleActions`:
```java
findViewById(R.id.my_view).setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(final View v) {
            BubbleActions.on(v)
                    .addAction("Star", R.drawable.ic_star, R.drawable.popup_item, new BubbleActions.Callback() {
                        @Override
                        public void doAction() {
                            Toast.makeText(v.getContext(), "Star pressed!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addAction("Share", R.drawable.ic_share, R.drawable.popup_item, new BubbleActions.Callback() {
                        @Override
                        public void doAction() {
                            Toast.makeText(v.getContext(), "Share pressed!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addAction("Hide", R.drawable.ic_hide, R.drawable.popup_item, new BubbleActions.Callback() {
                        @Override
                        public void doAction() {
                            Toast.makeText(v.getContext(), "Hide pressed on item!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
    });
```

####Changing the font
Use a custom font? Have no fear! You can configure the typeface of the bubble actions by using `withTypeface` when
you build your `BubbleActions`:
```
BubbleActions.on(myView).withTypeface(myFancyTypeface)
    // ... add actions ...
```

####Changing the indicator
The default indicator is a semi-transparent circle that appears where the last down touch event occurred before
showing the `BubbleActions`. You can change this indicator by using the `withIndicator` method 
when you build your `BubbleActions`:
```
BubbleActions.on(myView).withIndicator(R.drawable.my_fancy_indicator)
    // ... add actions ...
```


##License
```
Copyright 2015 Sam Thompson

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
