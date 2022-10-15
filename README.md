# BLEDemoApplication

This is the demo repository for the Bluetooth Low Energy (BLE) connect Application.

- Scan and Connect the nearby BLE device.

- Read the emit value by the device.

- Save those value  in Local DB, Room DB and also save it in server.

- Once Successfully Saved the value, trigger the push notifcation from FCM

1. Screen - Splash Screen 
	- Here, we get the Firebase token by initiating the firebase token listener. Send this token to server for triggerring the push notification once saved the data.
	
<img src="https://user-images.githubusercontent.com/94950611/195990441-95ec7218-3520-425d-b0d2-e72d7d374c82.png" width="25%" height="3%">

2. Main Screen - Asking Bluetooth to enable

<img src="https://user-images.githubusercontent.com/94950611/195990622-3a7a4fca-8cc5-4c7d-8d5c-16670bd4db9f.png" width="25%" height="3%">

3. Main Screen - Asking Location permission (Support given to both Coarse and Fine Location)

<img src="https://user-images.githubusercontent.com/94950611/195990696-7a9a6b2c-5e0c-457a-bdbd-3bfa09261ac7.png" width="25%" height="3%">

4. Main Screen - Asking GPS to enable
- Requires permission above Marshmallow (6.0), to access the hardware identifiers by bluetooth.

<img src="https://user-images.githubusercontent.com/94950611/195990922-15adc389-2ffa-4a78-9b7f-8892454348ea.png" width="25%" height="3%">

5. Main Screen - After given all required permissions

<img src="https://user-images.githubusercontent.com/94950611/195990963-dfeccd6a-6f68-4440-83a2-468aacff9230.png" width="25%" height="3%">

6. Main Screen - Fetching Nearby BLE Device
- Click start scan button to initiate the bluetooth scan

<img src="https://user-images.githubusercontent.com/94950611/195991366-6fef43a5-5c54-457b-8ad8-419cc5d771e3.png" width="25%" height="3%">

7. Main Screen - Nearby BLE Devices

<img src="https://user-images.githubusercontent.com/94950611/195991444-19ff3ae0-0007-4c8b-9ec1-741c1e2b6a38.png" width="25%" height="3%">

8. Main Screen  - Not Supported device
- Show proper toast message when clicked not supported device 

<img src="https://user-images.githubusercontent.com/94950611/195991497-7dc07732-16f6-4793-9a73-4ae968224f89.png" width="25%" height="3%">

9. Main Screen - Supported device

<img src="https://user-images.githubusercontent.com/94950611/195992196-6bb0eff3-d13e-460d-a620-77ba1a766ffe.png" width="25%" height="3%">

10. Second Screen 
- When clicked the ble supported device and its emitted value with data emittetd device name.
- Click the Save button, to send this value to the server then DB.
- Server url need to specify

<img src="https://user-images.githubusercontent.com/94950611/195991581-89d82706-2e93-4bf5-87e9-93ba624e3a21.png" width="25%" height="3%">

11. Main Screen 	
- Once saved the value, show push notification

<img src="https://user-images.githubusercontent.com/94950611/195992087-1553d40f-378d-44a3-998d-8ed09f668544.png" width="25%" height="3%">
