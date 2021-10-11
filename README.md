# Pet Finder educational app

Educational project based on book 'Real-World Android by Tutorials'.

## Functionality

The app has several branches that provide different functionality or features built on top of each
other. Note, that the app's aim was not to create a perfect UI, but rather to learn various
techniques and best practices of Android Development like: App Architecture, Data Storage,
Networking, Security etc.

# Branch master 

Basic app functionality. The app allows a user to access the Petfinder database of hundreds of
thousands of pets ready for adoption and over ten thousand animal welfare organizations. When the
app is installed it requires the user to input a postal code (Petfinder currently doesn't function
in Russia) and max distance to the organization that is sheltering pets.

Data is retrieved from https://api.petfinder.com/v2/


### For testing purposes it is recommended to use 09079 as postal code and 200 as max distance.

It has two screens in the bottom navigation panel:
- Animals Near You
- Search

### Animals Near You
Retrieves data from the Petfinder database about adoptable pets within the radius specified 
on the first app launch after installation. Clicking on a pet takes user to the screen with 
Animal Details where user receives some basic info about the pet. User can share (simulated for test 
purposes only) the info on a social network via share button. 

### Search
User can search for a pet by some criteria: type (types are retrieved from the API) and age (any, baby,
young, adult, senior - all ages are also retrieved from the API). 

## App Architecture
The app uses Clean Architecture with MVVM. 

## Domain layer
Contains models, use-cases and repository interfaces that account for business logic. 

## Data layer
### API
Contains API models and related classes that account for networking. Retrofit and OkHttp are used to
interact with the server. 
There're three interceptors:
- AuthenticationInterceptor: checks if the client (app) is authorized to use PetFinder API by validating 
CLIENT_ID and CLIENT_SECRET and updating the API token if necessary.
- LoggingInterceptor: logs info about API calls
- NetworkStatusInterceptor: throws NetworkUnavailableException if network is unavailable

### Cache
Uses Room to store info about pets and organizations retrieved from the Internet. Data from the cache
is retrieved as RxJava Flowable objects. 

### PetFinderAnimalRepository
Class that implements domain AnimalRepository interface and handles all the logic of fetching data 
from the Internet and storing it in the cache. It uses cache as the single source of truth, i.e. if
cache has no data, then the repository loads data from the Internet, stores it in the cache and then
returns the data from the cache to a client (usually a ViewModel).

## Presentation layer
Deals with the User Interface. Uses ViewModels as an intermediary between Fragments and MainActivity 
(this app has a single activity). User triggers an event by interacting with the UI, a ViewModel
handles the event by triggering a corresponding Use-Case which updates State in the ViewModel and
Fragments or MainActivity observe changes to the State. Usually the State is a LiveData object, 
but Onboarding feature uses StateFlow to represent state and SharedFlow to represent view effects
collected in OnBoardingFragment.   

## Dependency Injection
Provided by means of Hilt and Dagger - for the dynamic feature because Hilt doesn't have the necessary
flexibility. 

# App Structure
The app has several modules (names speak to themselves):
- app 
- common 
- logging 
- features:animalsnearyou 
- features:onboarding
- features:search
- features:sharing (implemented as dynamic feature)

Common dependencies are specified in android-library.gradle file. The app uses latest (as of October 11, 2021)
libraries. 

# Branch animation

In this branch some animations are added to the app.
App uses Lottie for complex animation that is shown to the user when a pet card is clicked - 
a happy dog wiggling its tail. 
The user can double-click a pet's photo (or placeholder) in Animal Details fragment and a vector-drawable
animation is triggered - an outlined red heart is filled. 
Spring animation is used to attract user's attention to the call button in Animal Details fragment.
User can also fling the call button and if it lands on the pet's photo, a secret Fragment with cute dog 
is opened. 
MotionLayout is used in Animal Details fragment to provide complex animation of the pet image during swipe 
up. 
The UI is also enhanced by converting plain booleans to emoji symbols in the description of a pet.
A custom Adopt button is added to Animal Details fragment. Once clicked it animates to a custom
progress bar and after some time (a network activity is simulated) it animates to a button with check mark.

# Branch report
An anonymous report feature is added to the app. A new screen in the bottom navigation is available.
It allows the user to select a picture from the device's gallery and send (simulated network activity)
an anonymous report to a server. This functionality is added for educational purposes to study the 
complex subject of app security.

## App Security
Now a user has to signup with the app during first launch (simply add an email in the correct format
like test@test.com). It will trigger Biometric Prompt and store user's encrypted info in a file.

- App is allowed to be installed only in internal storage
- App doesn't export its content providers
- App requests user's permissions to access Gallery
- App's caches used in ReportDetailFragment are cleared when the app is in the background (onPause)
- Keyboard cache is disabled
- Screenshots are disabled
- App uses biometric prompt to login. When user logs in for the first time, a secret key is created and
tied to the user's credentials. User's credentials are encrypted by secret key.
- When a report is sent it is sent encrypted using AES (Advanced Encryption Standard) algorithm  
- Custom network config is used in the app. It prohibits clearTextTraffic and implements certificate
pinning by using the Petfinder certificate pin SHA256 values. (They may change over time!!!) Certificate pinning
for early android versions are implemented in APIModule.kt in provideOkHttpClient() method.
- Certificate transparency is added on top of certificate pinning. It informs about revocation of a certificate.
- App simulates user authentication on the server by using public and private keys for encrypting data transfer.

#Branch maintaining
// TODO

## COPYRIGHT

```text
/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
```

