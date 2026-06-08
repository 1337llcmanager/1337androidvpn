# WebSocket VPN Server for v86 Guest Connectivity

This implementation adds WebSocket server capability to the WireGuard Android app, enabling v86 (copy.sh) virtual machine guests to connect to the VPN for internet access.

## Files Added

- `WebSocketVpnServer.kt` - Core WebSocket server service handling client connections and VPN commands
- `VpnWebSocketManager.kt` - Lifecycle manager for the WebSocket service

## Installation

### 1. Add Dependency to build.gradle

Add the following to your `ui/build.gradle` dependencies section:

```gradle
dependencies {
    implementation 'org.java-websocket:Java-WebSocket:1.5.4'
}
```

### 2. Update AndroidManifest.xml

Add the following permissions and service declaration:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.BIND_VPN_SERVICE" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<service
    android:name=".websocket.WebSocketVpnServer"
    android:permission="android.permission.BIND_VPN_SERVICE"
    android:exported="false" />
```

### 3. Integration with VPN Activity

```kotlin
// In your main VPN Activity or ViewModel
private val wsManager = VpnWebSocketManager(context)

// Start WebSocket server when VPN connects
fun onVpnConnected() {
    wsManager.startWebSocketServer()
}

// Stop WebSocket server when VPN disconnects
fun onVpnDisconnected() {
    wsManager.stopWebSocketServer()
}
```

## Usage

### Server Details

- **Port**: 8888 (configurable in WebSocketVpnServer.kt)
- **Host**: 0.0.0.0 (all interfaces)
- **Protocol**: WebSocket (ws://)

### v86 Guest Connection

```javascript
const ws = new WebSocket('ws://android-device-ip:8888');

ws.onopen = () => {
    console.log('Connected to VPN WebSocket Server');
    ws.send('status');
};

ws.onmessage = (event) => {
    console.log('Server response:', event.data);
};

ws.onerror = (error) => {
    console.error('WebSocket error:', error);
};

ws.onclose = () => {
    console.log('Disconnected from VPN WebSocket Server');
};
```

## Supported Commands

- `connect` - Initiate VPN connection
- `status` - Get current VPN status
- Any other message will be echoed back with confirmation

## Features

- Multiple client support with message broadcasting
- Graceful connection lifecycle management
- Error handling and logging
- Thread-safe client management
- Service binding for proper lifecycle

## Future Enhancements

- SSL/TLS support for secure connections
- Authentication mechanism
- Command protocol specification
- Traffic routing through VPN tunnel
- Performance optimization for low-latency connections
- Bandwidth management for guest VMs
