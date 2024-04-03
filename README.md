# JPMC Test Project
## Setup
Configure the _testng.xml_ file to upload the iOS Simulator application.
### App Upload Method
#### Upload Before Test
  - Set **UploadApp** to *true* 
  - Set the file path to the simulator bundle in **AppPath** to upload relative to the project root directory
#### Upload Manually
 - Use cURL or Postman to upload the application bundle, then use the returned app URL
 - Set **UploadApp** to _false_
 - Set **AppURL** to the URL provided by the upload confirmation
## Run
- _mvn test_